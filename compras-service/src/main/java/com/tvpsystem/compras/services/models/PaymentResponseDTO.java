package com.tvpsystem.compras.services.models;

import lombok.Data;
import java.util.List;

@Data
public class PaymentResponseDTO {
    private boolean aprobado;
    private String mensaje;
    private List<String> paquetesFallidos;
}
