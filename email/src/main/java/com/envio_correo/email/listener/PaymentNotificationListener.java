package com.envio_correo.email.listener;

import com.envio_correo.email.services.IEmailService;
import com.envio_correo.email.services.models.EmailDTO;
import com.envio_correo.email.services.event.UserRegisteredListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentNotificationListener {

    private final IEmailService emailService;
    private final RestTemplate restTemplate;

    @RabbitListener(queues = "pago.queue")
    public void handlePaymentNotification(Map<String, Object> notification) {
        try {
            log.info("📩 RECIBIENDO NOTIFICACIÓN - Keys disponibles: {}", notification.keySet());
            
            String clienteId = obtenerValorSeguro(notification, "clienteId", "ClienteDesconocido");
            
            // OBTENER EMAIL DE FORMA DINÁMICA - Primero del cache de payment-service
            String email = obtenerEmailDinamicamente(clienteId);
            log.info("🎯 EMAIL ASIGNADO DINÁMICAMENTE: {} -> {}", clienteId, email);
            
            // Obtener cédula y dirección
            String cedula = obtenerValorSeguro(notification, "cedula", "N/A");
            String direccion = obtenerValorSeguro(notification, "direccion", "N/A");
            
            Boolean aprobado = obtenerValorSeguroBoolean(notification, "aprobado");
            String mensaje = obtenerValorSeguro(notification, "mensaje", "Sin mensaje");
            Double total = obtenerValorSeguroDouble(notification, "total", 0.0);
            
            @SuppressWarnings("unchecked")
            List<String> codigosPaquetes = (List<String>) notification.get("codigosPaquetes");
            
            log.info("🚀 ENVIANDO EMAIL A: {} para cliente: {} (Cédula: {}, Dirección: {})", 
                email, clienteId, cedula, direccion);
            
            if (aprobado != null && aprobado) {
                enviarEmailConfirmacion(email, clienteId, total, codigosPaquetes, mensaje, cedula, direccion);
            } else {
                enviarEmailRechazo(email, clienteId, mensaje);
            }
            
        } catch (Exception e) {
            log.error("❌ Error procesando notificación de pago: ", e);
        }
    }

    // MÉTODO PARA OBTENER EMAIL DE FORMA DINÁMICA
    private String obtenerEmailDinamicamente(String clienteId) {
        try {
            // 1. Intentar obtener del cache de payment-service
            try {
                String url = "http://localhost:8090/api/payment/users/get-email/" + clienteId;
                Map<String, String> response = restTemplate.getForObject(url, Map.class);
                
                if (response != null && response.containsKey("email")) {
                    String email = response.get("email").toString();
                    log.info("✅ EMAIL OBTENIDO DEL CACHE: {} -> {}", clienteId, email);
                    return email;
                }
            } catch (Exception e) {
                log.warn("⚠️ No se pudo obtener email del cache de payment-service: {}", e.getMessage());
            }
            
            // 2. Si no está en cache, usar mapeo local o cache dinámico
            String email = UserRegisteredListener.obtenerEmailDinamico(clienteId);
            log.info("📧 EMAIL ASIGNADO DEL MAPEO LOCAL: {} -> {}", clienteId, email);
            return email;
            
        } catch (Exception e) {
            log.error("❌ Error obteniendo email para cliente {}: {}", clienteId, e.getMessage());
            return clienteId.toLowerCase() + "@toursadventure.com";
        }
    }

    // MÉTODOS AUXILIARES (se mantienen igual)
    private String obtenerValorSeguro(Map<String, Object> notification, String key, String defaultValue) {
        try {
            Object value = notification.get(key);
            return value != null ? value.toString() : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private Boolean obtenerValorSeguroBoolean(Map<String, Object> notification, String key) {
        try {
            Object value = notification.get(key);
            if (value instanceof Boolean) return (Boolean) value;
            if (value instanceof String) return Boolean.parseBoolean((String) value);
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private Double obtenerValorSeguroDouble(Map<String, Object> notification, String key, Double defaultValue) {
        try {
            Object value = notification.get(key);
            if (value instanceof Double) return (Double) value;
            if (value instanceof Number) return ((Number) value).doubleValue();
            if (value instanceof String) return Double.parseDouble((String) value);
            return defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private void enviarEmailConfirmacion(String email, String clienteId, Double total, 
                                       List<String> codigosPaquetes, String mensaje, String cedula, String direccion) {
        try {
            EmailDTO emailDTO = new EmailDTO();
            emailDTO.setDestinatario(email);
            emailDTO.setAsunto("✅ Confirmación de Compra - Tours Adventure");
            
            String paquetesStr = codigosPaquetes != null ? 
                String.join(", ", codigosPaquetes) : "No especificado";
            
            String mensajeHtml = "<h2>¡Compra Confirmada!</h2>" +
                "<p>Hola <strong>" + clienteId + "</strong>,</p>" +
                "<p>¡Tu compra ha sido confirmada exitosamente!</p>" +
                "<h3>📋 Detalles de la compra:</h3>" +
                "<ul>" +
                "<li><strong>Cédula:</strong> " + cedula + "</li>" +
                "<li><strong>Dirección:</strong> " + direccion + "</li>" +
                "<li><strong>Paquetes:</strong> " + paquetesStr + "</li>" +
                "<li><strong>Total pagado:</strong> $" + total + "</li>" +
                "<li><strong>Estado:</strong> " + mensaje + "</li>" +
                "</ul>" +
                "<p>¡Gracias por confiar en Tours Adventure!</p>" +
                "<br>" +
                "<p>Saludos,<br>Equipo de Tours Adventure</p>";
            
            emailDTO.setMensaje(mensajeHtml);
            
            emailService.sendMail(emailDTO);
            log.info("✅ EMAIL CONFIRMACIÓN ENVIADO a: {} para cliente: {}", email, clienteId);
            
        } catch (Exception e) {
            log.error("❌ Error enviando email de confirmación a {}: {}", email, e.getMessage());
        }
    }

    private void enviarEmailRechazo(String email, String clienteId, String mensaje) {
        try {
            EmailDTO emailDTO = new EmailDTO();
            emailDTO.setDestinatario(email);
            emailDTO.setAsunto("❌ Pago Rechazado - Tours Adventure");
            
            String mensajeHtml = "<h2>Pago Rechazado</h2>" +
                "<p>Hola,</p>" +
                "<p>Lamentamos informarte que tu pago ha sido rechazado.</p>" +
                "<h3>📋 Detalles:</h3>" +
                "<ul>" +
                "<li><strong>Email:</strong> " + email + "</li>" +
                "<li><strong>Razón:</strong> " + mensaje + "</li>" +
                "</ul>" +
                "<p>Por favor, verifica tus fondos e intenta nuevamente.</p>" +
                "<br>" +
                "<p>Saludos,<br>Equipo de Tours Adventure</p>";
            
            emailDTO.setMensaje(mensajeHtml);
            
            emailService.sendMail(emailDTO);
            log.info("✅ EMAIL RECHAZO ENVIADO a: {}", email);
            
        } catch (Exception e) {
            log.error("❌ Error enviando email de rechazo a {}: {}", email, e.getMessage());
        }
    }
}