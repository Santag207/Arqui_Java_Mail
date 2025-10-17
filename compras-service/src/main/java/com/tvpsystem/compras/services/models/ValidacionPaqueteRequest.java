package com.tvpsystem.compras.services.models;

import lombok.Data;
import java.util.List;

@Data
public class ValidacionPaqueteRequest {
    private List<String> codigosPaquetes;
    private String idSolicitud;
}