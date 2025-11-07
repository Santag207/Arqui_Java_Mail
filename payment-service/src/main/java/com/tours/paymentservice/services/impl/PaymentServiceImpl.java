package com.tours.paymentservice.services.impl;

import com.tours.paymentservice.account.entity.Account;
import com.tours.paymentservice.account.repository.AccountRepository;
import com.tours.paymentservice.inventory.entity.InventorySlot;
import com.tours.paymentservice.inventory.repository.InventoryRepository;
import com.tours.paymentservice.services.PaymentService;
import com.tours.paymentservice.services.dto.PaymentRequestDTO;
import com.tours.paymentservice.services.dto.PaymentResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final InventoryRepository inventoryRepository;
    private final AccountRepository accountRepository;
    private final RabbitTemplate rabbitTemplate;

    // Usa el transaction manager de accounts (o el que prefieras)
    @Override
    //@Transactional("accountTransactionManager")
    public PaymentResponseDTO validarYReservar(PaymentRequestDTO request) {
        log.info("Iniciando validación de pago para cliente {} - Total: {}", request.getClienteId(), request.getTotal());

        List<String> paquetesFallidos = new ArrayList<>();

        // 1) Verificar disponibilidad de cupos para cada paquete
        for (String codigo : request.getCodigosPaquetes()) {
            InventorySlot slot = inventoryRepository.findByCodigoPaquete(codigo).orElse(null);
            if (slot == null) {
                paquetesFallidos.add(codigo + " (no encontrado)");
                log.warn("Paquete no encontrado en inventario: {}", codigo);
                continue;
            }
            if (slot.getCuposDisponibles() == null || slot.getCuposDisponibles() <= 0) {
                paquetesFallidos.add(codigo + " (sin cupos)");
                log.warn("Sin cupos disponibles para: {}", codigo);
                continue;
            }
        }

        // 2) Verificar fondos del cliente
        Account account = accountRepository.findByClienteId(request.getClienteId()).orElse(null);
        if (account == null) {
            PaymentResponseDTO resp = createResponse(false, "Cuenta no encontrada para cliente " + request.getClienteId(), paquetesFallidos);
            enviarNotificacion(request, resp);
            return resp;
        }

        if (account.getBalance() < request.getTotal()) {
            PaymentResponseDTO resp = createResponse(false, 
                String.format("Fondos insuficientes. Balance: %.2f, Total requerido: %.2f", 
                    account.getBalance(), request.getTotal()), 
                paquetesFallidos);
            enviarNotificacion(request, resp);
            return resp;
        }

        // Si hay paquetes fallidos, devolvemos error
        if (!paquetesFallidos.isEmpty()) {
            PaymentResponseDTO resp = createResponse(false, "Algunos paquetes no están disponibles", paquetesFallidos);
            enviarNotificacion(request, resp);
            return resp;
        }

        // 3) Reservar cupos y descontar fondos
        try {
            for (String codigo : request.getCodigosPaquetes()) {
                InventorySlot slot = inventoryRepository.findByCodigoPaquete(codigo).get();
                slot.setCuposDisponibles(slot.getCuposDisponibles() - 1);
                inventoryRepository.save(slot);
                log.info("Cupo reservado para paquete: {}", codigo);
            }

            account.setBalance(account.getBalance() - request.getTotal());
            accountRepository.save(account);
            log.info("Fondos descontados. Nuevo balance: {}", account.getBalance());

            PaymentResponseDTO resp = createResponse(true, 
                String.format("Pago y reserva exitosos. Nuevo balance: %.2f", account.getBalance()), 
                new ArrayList<>());
            
            enviarNotificacion(request, resp);
            return resp;

        } catch (Exception e) {
            log.error("Error durante la reserva: ", e);
            PaymentResponseDTO resp = createResponse(false, "Error durante la reserva: " + e.getMessage(), paquetesFallidos);
            enviarNotificacion(request, resp);
            return resp;
        }
    }

    private PaymentResponseDTO createResponse(boolean aprobado, String mensaje, List<String> paquetesFallidos) {
        PaymentResponseDTO resp = new PaymentResponseDTO();
        resp.setAprobado(aprobado);
        resp.setMensaje(mensaje);
        resp.setPaquetesFallidos(paquetesFallidos);
        return resp;
    }

    private void enviarNotificacion(PaymentRequestDTO request, PaymentResponseDTO response) {
        try {
            // Crear objeto de notificación
            PaymentNotification notification = new PaymentNotification(
                request.getClienteId(),
                request.getTotal(),
                request.getCodigosPaquetes(),
                response.isAprobado(),
                response.getMensaje(),
                response.getPaquetesFallidos()
            );
            
            rabbitTemplate.convertAndSend("compra.exchange", "pago.routingkey", notification);
            log.info("Notificación enviada a RabbitMQ: {}", notification);
            
        } catch (Exception e) {
            log.error("Error enviando notificación a RabbitMQ: ", e);
        }
    }

    // Clase interna para la notificación
    public static class PaymentNotification {
        private String clienteId;
        private Double total;
        private List<String> codigosPaquetes;
        private boolean aprobado;
        private String mensaje;
        private List<String> paquetesFallidos;

        public PaymentNotification(String clienteId, Double total, List<String> codigosPaquetes, 
                                 boolean aprobado, String mensaje, List<String> paquetesFallidos) {
            this.clienteId = clienteId;
            this.total = total;
            this.codigosPaquetes = codigosPaquetes;
            this.aprobado = aprobado;
            this.mensaje = mensaje;
            this.paquetesFallidos = paquetesFallidos;
        }

        // Getters y setters
        public String getClienteId() { return clienteId; }
        public void setClienteId(String clienteId) { this.clienteId = clienteId; }
        public Double getTotal() { return total; }
        public void setTotal(Double total) { this.total = total; }
        public List<String> getCodigosPaquetes() { return codigosPaquetes; }
        public void setCodigosPaquetes(List<String> codigosPaquetes) { this.codigosPaquetes = codigosPaquetes; }
        public boolean isAprobado() { return aprobado; }
        public void setAprobado(boolean aprobado) { this.aprobado = aprobado; }
        public String getMensaje() { return mensaje; }
        public void setMensaje(String mensaje) { this.mensaje = mensaje; }
        public List<String> getPaquetesFallidos() { return paquetesFallidos; }
        public void setPaquetesFallidos(List<String> paquetesFallidos) { this.paquetesFallidos = paquetesFallidos; }
    }
}