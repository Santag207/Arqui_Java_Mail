package com.envio_correo.email.services.models;

import lombok.Data;

@Data
public class OTNResponse {
    private String estado; // APROBADO, RECHAZADO, ENPROCESO
    private String codigoPaquete;
    private String mensaje;
    private String timestamp;
}