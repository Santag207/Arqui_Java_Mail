package com.envio_correo.email.services.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailMessageDTO implements Serializable {
    private String destinatario;
    private String asunto;
    private String mensaje;
    private String tipo;
    private String idTransaccion;
    
    // Constructor simplificado
    public EmailMessageDTO(String destinatario, String asunto, String mensaje) {
        this.destinatario = destinatario;
        this.asunto = asunto;
        this.mensaje = mensaje;
        this.tipo = "notificacion";
        this.idTransaccion = UUID.randomUUID().toString();
    }
}