package com.envio_correo.email.services.models;

import lombok.Data;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ValidacionPaqueteResponse {
    private boolean todosAprobados = false;
    private boolean puedeContinuar = false;
    private String mensaje = "";
    private Map<String, String> resultadosValidacion = new HashMap<>();
    private List<String> paquetesRechazados = new ArrayList<>();
    private List<String> paquetesEnProceso = new ArrayList<>();
}