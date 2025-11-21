package com.tours.paymentservice.services.dto;

import lombok.Data;
import java.util.List;

@Data
public class PaymentRequestDTO {
    private String clienteId;
    private Double total;
    private List<String> codigosPaquetes;

    private CreditCardInfo tarjeta;

    @Data
    public static class CreditCardInfo {
        private String cardId; // ID de tarjeta guardada o null para nueva tarjeta
        private String cardNumber; // Para nueva tarjeta
        private String cardHolder;
        private Integer expiryMonth;
        private Integer expiryYear;
        private String cvv;
        private Boolean saveCard = false; // Si quiere guardar la tarjeta
    }
}