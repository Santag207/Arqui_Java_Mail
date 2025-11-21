package com.tours.paymentservice.services.impl;

// imports...

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final InventoryRepository inventoryRepository;
    private final AccountRepository accountRepository;
    private final RabbitTemplate rabbitTemplate;
    private final CreditCardService creditCardService; // NUEVO

    @Override
    // @Transactional("accountTransactionManager") comentado temporalmente
    public PaymentResponseDTO validarYReservar(PaymentRequestDTO request) {
        log.info("Iniciando validación de pago para cliente {} - Total: {}", request.getClienteId(), request.getTotal());

        // NUEVA VALIDACIÓN: Verificar que tiene tarjeta registrada o proporcionada
        if (!validarTarjetaPago(request)) {
            PaymentResponseDTO resp = new PaymentResponseDTO();
            resp.setAprobado(false);
            resp.setMensaje("Información de tarjeta inválida o no proporcionada");
            resp.setPaquetesFallidos(new ArrayList<>());
            enviarNotificacion(request, resp);
            return resp;
        }

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

            // NUEVO: Guardar tarjeta si se solicitó
            if (request.getTarjeta() != null && request.getTarjeta().getSaveCard()) {
                guardarTarjetaCliente(request);
            }

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

    // NUEVO MÉTODO: Validar información de tarjeta
    private boolean validarTarjetaPago(PaymentRequestDTO request) {
        if (request.getTarjeta() == null) {
            log.warn("No se proporcionó información de tarjeta");
            return false;
        }

        // Si se proporciona cardId, verificar que existe y pertenece al cliente
        if (request.getTarjeta().getCardId() != null) {
            try {
                Long cardId = Long.parseLong(request.getTarjeta().getCardId());
                Optional<CreditCard> card = creditCardService.getCardsByCliente(request.getClienteId())
                        .stream()
                        .filter(c -> c.getId().equals(cardId))
                        .findFirst();
                return card.isPresent();
            } catch (NumberFormatException e) {
                log.error("Formato de cardId inválido: {}", request.getTarjeta().getCardId());
                return false;
            }
        }

        // Si se proporciona nueva tarjeta, validar datos básicos
        if (request.getTarjeta().getCardNumber() != null &&
                request.getTarjeta().getCardHolder() != null &&
                request.getTarjeta().getExpiryMonth() != null &&
                request.getTarjeta().getExpiryYear() != null &&
                request.getTarjeta().getCvv() != null) {

            // Validaciones básicas de tarjeta
            return validarDatosTarjeta(
                    request.getTarjeta().getCardNumber(),
                    request.getTarjeta().getExpiryMonth(),
                    request.getTarjeta().getExpiryYear()
            );
        }

        return false;
    }

    // NUEVO MÉTODO: Validar datos básicos de tarjeta
    private boolean validarDatosTarjeta(String cardNumber, Integer expiryMonth, Integer expiryYear) {
        // Validar número de tarjeta (debe tener al menos 13 dígitos)
        if (cardNumber == null || cardNumber.replaceAll("\\s", "").length() < 13) {
            return false;
        }

        // Validar fecha de expiración
        if (expiryMonth == null || expiryYear == null) {
            return false;
        }

        if (expiryMonth < 1 || expiryMonth > 12) {
            return false;
        }

        int currentYear = java.time.Year.now().getValue();
        if (expiryYear < currentYear) {
            return false;
        }

        return true;
    }

    // NUEVO MÉTODO: Guardar tarjeta del cliente
    private void guardarTarjetaCliente(PaymentRequestDTO request) {
        try {
            CreditCard newCard = new CreditCard();
            newCard.setClienteId(request.getClienteId());
            newCard.setCardNumber(request.getTarjeta().getCardNumber());
            newCard.setCardHolder(request.getTarjeta().getCardHolder());
            newCard.setExpiryMonth(request.getTarjeta().getExpiryMonth());
            newCard.setExpiryYear(request.getTarjeta().getExpiryYear());
            newCard.setCvv(request.getTarjeta().getCvv());
            newCard.setIsDefault(false); // No hacerla default automáticamente

            creditCardService.addCard(newCard);
            log.info("Tarjeta guardada para cliente: {}", request.getClienteId());
        } catch (Exception e) {
            log.error("Error guardando tarjeta para cliente {}: {}", request.getClienteId(), e.getMessage());
        }
    }

    // Resto de métodos existentes se mantienen igual...
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
                    response.getPaquetesFallidos(),
                    obtenerEmailDelCliente(request.getClienteId())
            );

            rabbitTemplate.convertAndSend("compra.exchange", "pago.routingkey", notification);
            log.info("Notificación enviada a RabbitMQ: {}", notification);

        } catch (Exception e) {
            log.error("Error enviando notificación a RabbitMQ: ", e);
        }
    }

    private String obtenerEmailDelCliente(String clienteId) {
        Map<String, String> clientesEmails = Map.of(
                "CLI-1001", "castrozsantiago@javeriana.edu.co",
                "CLI-2002", "castrosantiago476@gmail.com",
                "CLI-3003", "castrosantiago3@gmail.com"
        );
        return clientesEmails.getOrDefault(clienteId, "castrozsantiago@javeriana.edu.co");
    }

    // Clase interna para la notificación (se mantiene igual)
    public static class PaymentNotification {
        // ... mismo código anterior
    }
}