package com.tours.paymentservice.services.dto;

import lombok.Data;
import java.util.List;

@Data
public class PaymentRequestDTO {
    private String clienteId;
    private Double total;
    private List<String> codigosPaquetes;
}
