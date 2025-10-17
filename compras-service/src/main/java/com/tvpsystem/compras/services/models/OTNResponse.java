package com.tvpsystem.compras.services.models;

import lombok.Data;

@Data
public class OTNResponse {
    private String estado; // APROBADO, RECHAZADO, ENPROCESO
    private String codigoPaquete;
    private String mensaje;
    private String timestamp;
}