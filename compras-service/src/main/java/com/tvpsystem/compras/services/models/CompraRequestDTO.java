package com.tvpsystem.compras.services.models;

import lombok.Data;
import java.util.List;

@Data
public class CompraRequestDTO {
    private String idCliente;
    private String nombreCliente;
    private String emailCliente;
    private String telefonoCliente;
    private List<String> codigosPaquetes;
    private String idSolicitud;
}