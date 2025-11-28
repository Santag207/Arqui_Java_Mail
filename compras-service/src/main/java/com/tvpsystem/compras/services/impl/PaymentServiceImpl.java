package com.tvpsystem.compras.services.impl;

import com.tvpsystem.compras.services.IPaymentService;
import com.tvpsystem.compras.services.models.PaymentRequestDTO;
import com.tvpsystem.compras.services.models.PaymentResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements IPaymentService {

    @Value("${payment.service.url}")
    private String paymentServiceUrl;

    private final WebClient.Builder webClientBuilder;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public PaymentResponseDTO validarPago(PaymentRequestDTO request) {
        try {
            log.info("Llamando a payment-service para validar pago para cliente: {}", request.getClienteId());
            
            // Obtener el email ANTES de llamar al payment-service
            String emailCliente = obtenerEmailDelCliente(request.getClienteId());
            log.info("Email obtenido para cliente {}: {}", request.getClienteId(), emailCliente);
            
            // Llamar al payment-service
            PaymentResponseDTO response = webClientBuilder.build()
                    .post()
                    .uri(paymentServiceUrl + "/validate")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(PaymentResponseDTO.class)
                    .block();

            // Enviar notificación a RabbitMQ para email
            enviarNotificacionEmail(request, response, emailCliente);
            
            return response;
            
        } catch (WebClientResponseException e) {
            log.error("Error en payment-service: {}", e.getResponseBodyAsString());
            PaymentResponseDTO resp = new PaymentResponseDTO();
            resp.setAprobado(false);
            resp.setMensaje("Error en payment-service: " + e.getMessage());
            
            // Enviar notificación de error
            String emailCliente = obtenerEmailDelCliente(request.getClienteId());
            enviarNotificacionEmail(request, resp, emailCliente);
            return resp;
            
        } catch (Exception e) {
            log.error("Error llamando a payment-service: {}", e.getMessage());
            PaymentResponseDTO resp = new PaymentResponseDTO();
            resp.setAprobado(false);
            resp.setMensaje("Error llamando a payment-service: " + e.getMessage());
            
            // Enviar notificación de error
            String emailCliente = obtenerEmailDelCliente(request.getClienteId());
            enviarNotificacionEmail(request, resp, emailCliente);
            return resp;
        }
    }

    // MÉTODO CORREGIDO: Enviar notificación para email
    private void enviarNotificacionEmail(PaymentRequestDTO request, PaymentResponseDTO response, String emailCliente) {
        try {
            // Obtener datos completos del usuario desde payment-service
            String clienteId = request.getClienteId();
            String cedula = "N/A";
            String direccion = "N/A";
            
            try {
                Map<String, Object> userData = webClientBuilder.build()
                    .get()
                    .uri(paymentServiceUrl + "/api/payment/users/get-user/" + clienteId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
                
                if (userData != null) {
                    cedula = (String) userData.getOrDefault("cedula", "N/A");
                    direccion = (String) userData.getOrDefault("direccion", "N/A");
                    log.info("✅ Datos de usuario obtenidos: Cédula={}, Dirección={}", cedula, direccion);
                }
            } catch (Exception e) {
                log.warn("⚠️ No se pudieron obtener datos adicionales del usuario: {}", e.getMessage());
            }
            
            // Validar que el email no sea null
            if (emailCliente == null || emailCliente.trim().isEmpty()) {
                log.error("❌ Email es null o vacío para cliente: {}", clienteId);
                emailCliente = generarEmailDefault(clienteId);
                log.warn("⚠️ Usando email por defecto: {}", emailCliente);
            }
            
            // Crear notificación con estructura CORRECTA (incluyendo cédula y dirección)
            Map<String, Object> notificacion = new HashMap<>();
            notificacion.put("clienteId", clienteId);
            notificacion.put("email", emailCliente);
            notificacion.put("cedula", cedula);
            notificacion.put("direccion", direccion);
            notificacion.put("total", request.getTotal() != null ? request.getTotal() : 0.0);
            notificacion.put("codigosPaquetes", request.getCodigosPaquetes() != null ? request.getCodigosPaquetes() : java.util.Collections.emptyList());
            notificacion.put("aprobado", response.isAprobado());
            notificacion.put("mensaje", response.getMensaje() != null ? response.getMensaje() : "Sin mensaje");
            notificacion.put("paquetesFallidos", response.getPaquetesFallidos() != null ? response.getPaquetesFallidos() : java.util.Collections.emptyList());
            notificacion.put("tipo", "NOTIFICACION_PAGO");
            notificacion.put("timestamp", System.currentTimeMillis());
            
            rabbitTemplate.convertAndSend("compra.exchange", "pago.routingkey", notificacion);
            log.info("✅ Notificación de pago enviada a RabbitMQ para cliente: {} -> Email: {} (Con Cédula y Dirección)", 
                    clienteId, emailCliente);
            
        } catch (Exception e) {
            log.error("❌ Error enviando notificación a RabbitMQ para cliente {}: {}", 
                     request.getClienteId(), e.getMessage());
        }
    }

    // MÉTODO MEJORADO: Obtener email del cliente - Primero del cache, luego del mapeo
    private String obtenerEmailDelCliente(String clienteId) {
        try {
            // 1. Intentar obtener del cache de payment-service
            try {
                String url = "http://localhost:8090/api/payment/users/get-email/" + clienteId;
                Map<String, String> response = webClientBuilder.build()
                        .get()
                        .uri(url)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block();
                
                if (response != null && response.containsKey("email")) {
                    String email = response.get("email").toString();
                    log.info("📧 EMAIL OBTENIDO DEL CACHE: {} -> {}", clienteId, email);
                    return email;
                }
            } catch (Exception e) {
                log.warn("⚠️ No se pudo obtener email del cache de payment-service: {}", e.getMessage());
            }
            
            // 2. Si no está en cache, usar mapeo local
            Map<String, String> clientesEmails = Map.of(
                "1", "castrosantiago476@gmail.com",
                "CLI-1001", "castrozsantiago@javeriana.edu.co",
                "CLI-2002", "castrosantiago476@gmail.com", 
                "CLI-3003", "castrosantiago3@gmail.com",
                "CLI-4004", "santiago.castro@example.com",
                "CLI-5005", "usuario.prueba@example.com"
            );
            
            String email = clientesEmails.get(clienteId);
            
            if (email == null) {
                log.warn("⚠️ Cliente no encontrado en mapeo: {}, usando email por defecto", clienteId);
                email = generarEmailDefault(clienteId);
            }
            
            log.debug("📧 Email asignado para {}: {}", clienteId, email);
            return email;
            
        } catch (Exception e) {
            log.error("❌ Error obteniendo email para cliente {}: {}", clienteId, e.getMessage());
            return generarEmailDefault(clienteId);
        }
    }

    // MÉTODO NUEVO: Generar email por defecto si no se encuentra
    private String generarEmailDefault(String clienteId) {
        return clienteId.toLowerCase() + "@toursadventure.com";
    }
}