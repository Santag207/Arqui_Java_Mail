package com.envio_correo.email.services.models;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OTNResponse {
    private String codigoPaquete;
    private boolean aprobado;
    private String estado;
    private String mensaje;
    private LocalDateTime timestamp;
}