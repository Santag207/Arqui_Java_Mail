package com.tvpsystem.compras.services.impl;

import com.tvpsystem.compras.services.IOTNService;
import com.tvpsystem.compras.services.models.OTNResponse;
import com.tvpsystem.compras.services.models.ValidacionPaqueteRequest;
import com.tvpsystem.compras.services.models.ValidacionPaqueteResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Slf4j
@Primary
@Service
@ConditionalOnProperty(name = "otn.mock.enabled", havingValue = "true", matchIfMissing = true)
public class MockOTNService implements IOTNService {

    private final Random random = new Random();
    
    @Override
    public OTNResponse validarPaquete(String codigoPaquete) {
        log.info("Mock OTN: Validando paquete {}", codigoPaquete);
        
        // Simular un pequeño retardo de red
        try {
            Thread.sleep(100 + random.nextInt(200));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        OTNResponse response = new OTNResponse();
        response.setCodigoPaquete(codigoPaquete);
        
        // Lógica de mock basada en el código del paquete
        if (codigoPaquete.toUpperCase().contains("RECHAZADO")) {
            response.setEstado("RECHAZADO");
            response.setMensaje("Paquete rechazado - No cumple con las políticas turísticas nacionales");
        } else if (codigoPaquete.toUpperCase().contains("ENPROCESO") || 
                   codigoPaquete.toUpperCase().contains("PENDIENTE")) {
            response.setEstado("ENPROCESO");
            response.setMensaje("Paquete en proceso de validación - Espere confirmación");
        } else if (codigoPaquete.toUpperCase().contains("ERROR")) {
            throw new RuntimeException("Error simulado en la validación");
        } else {
            // Para códigos normales, decidir aleatoriamente
            int decision = random.nextInt(100);
            if (decision < 70) { // 70% de probabilidad de aprobar
                response.setEstado("APROBADO");
                response.setMensaje("Paquete aprobado para la venta");
            } else if (decision < 85) { // 15% de probabilidad de rechazar
                response.setEstado("RECHAZADO");
                response.setMensaje("Paquete rechazado - Requiere documentación adicional");
            } else { // 15% de probabilidad de en proceso
                response.setEstado("ENPROCESO");
                response.setMensaje("Paquete en revisión por el comité de turismo");
            }
        }
        
        response.setTimestamp(java.time.LocalDateTime.now().toString());
        log.info("Mock OTN: Paquete {} -> {}", codigoPaquete, response.getEstado());
        return response;
    }

    @Override
    public ValidacionPaqueteResponse validarMultiplesPaquetes(ValidacionPaqueteRequest request) {
        log.info("Mock OTN: Validando {} paquetes - ID: {}", 
                 request.getCodigosPaquetes().size(), request.getIdSolicitud());
        
        Map<String, String> resultados = new HashMap<>();
        List<String> rechazados = new ArrayList<>();
        List<String> enProceso = new ArrayList<>();
        boolean todosAprobados = true;
        boolean puedeContinuar = true;

        for (String codigo : request.getCodigosPaquetes()) {
            try {
                OTNResponse respuesta = validarPaquete(codigo);
                resultados.put(codigo, respuesta.getEstado());
                
                switch (respuesta.getEstado()) {
                    case "RECHAZADO":
                        rechazados.add(codigo);
                        todosAprobados = false;
                        puedeContinuar = false;
                        break;
                    case "ENPROCESO":
                        enProceso.add(codigo);
                        todosAprobados = false;
                        puedeContinuar = false;
                        break;
                    case "APROBADO":
                        // Continúa sin problemas
                        break;
                    default:
                        log.warn("Estado desconocido para paquete {}: {}", codigo, respuesta.getEstado());
                        puedeContinuar = false;
                }
                
            } catch (Exception e) {
                log.error("Error validando paquete {}: {}", codigo, e.getMessage());
                resultados.put(codigo, "ERROR");
                puedeContinuar = false;
                todosAprobados = false;
            }
        }

        ValidacionPaqueteResponse response = new ValidacionPaqueteResponse();
        response.setTodosAprobados(todosAprobados);
        response.setPuedeContinuar(puedeContinuar);
        response.setResultadosValidacion(resultados);
        response.setPaquetesRechazados(rechazados);
        response.setPaquetesEnProceso(enProceso);
        
        if (puedeContinuar) {
            response.setMensaje("✅ Todos los paquetes han sido APROBADOS. Puede continuar con la compra.");
        } else if (!rechazados.isEmpty()) {
            response.setMensaje("❌ No se puede continuar: " + rechazados.size() + " paquete(s) RECHAZADO(S)");
        } else if (!enProceso.isEmpty()) {
            response.setMensaje("⏳ No se puede continuar: " + enProceso.size() + " paquete(s) EN PROCESO de validación");
        } else {
            response.setMensaje("⚠️ Error en la validación de paquetes");
        }

        log.info("Mock OTN: Resultado validación - {}", response.getMensaje());
        return response;
    }

    @Override
    public boolean isServiceAvailable() {
        return true; // Mock siempre disponible
    }
}