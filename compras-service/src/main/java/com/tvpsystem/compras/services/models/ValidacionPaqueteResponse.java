package com.tvpsystem.compras.services.models;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ValidacionPaqueteResponse {
    private boolean todosAprobados;
    private boolean puedeContinuar;
    private String mensaje;
    private Map<String, String> resultadosValidacion;
    private List<String> paquetesRechazados;
    private List<String> paquetesEnProceso;
}