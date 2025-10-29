package com.tvpsystem.compras.services.models;

import lombok.Data;
import java.io.Serializable;
import java.util.UUID;

@Data
public class EmailMessageDTO implements Serializable {
    private String destinatario;
    private String asunto;
    private String mensaje;
    private String tipo;
    private String idTransaccion;

    public EmailMessageDTO(String destinatario, String asunto, String mensaje) {
        this.destinatario = destinatario;
        this.asunto = asunto;
        this.mensaje = mensaje;
        this.tipo = "compra";
        this.idTransaccion = UUID.randomUUID().toString();
    }
}