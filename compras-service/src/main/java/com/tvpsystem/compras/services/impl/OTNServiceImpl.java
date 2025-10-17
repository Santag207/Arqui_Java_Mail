package com.tvpsystem.compras.services.impl;

import com.tvpsystem.compras.exceptions.OTNServiceException;
import com.tvpsystem.compras.services.IOTNService;
import com.tvpsystem.compras.services.models.OTNResponse;
import com.tvpsystem.compras.services.models.ValidacionPaqueteRequest;
import com.tvpsystem.compras.services.models.ValidacionPaqueteResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class OTNServiceImpl implements IOTNService {

    private final WebClient webClient;
    
    @Value("${otn.service.timeout:5000}")
    private int timeout;
    
    @Value("${otn.service.max-retries:3}")
    private int maxRetries;

    public OTNServiceImpl(WebClient otnWebClient) {
        this.webClient = otnWebClient;
    }

    @Override
    @Retryable(
        value = {OTNServiceException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public OTNResponse validarPaquete(String codigoPaquete) throws OTNServiceException {
        try {
            log.info("Validando paquete con OTN: {}", codigoPaquete);
            
            return webClient.get()
                    .uri("/validar?codigo={codigo}", codigoPaquete)
                    .retrieve()
                    .bodyToMono(OTNResponse.class)
                    .timeout(Duration.ofMillis(timeout))
                    .onErrorMap(WebClientRequestException.class, 
                         ex -> new OTNServiceException("Error de conexión con OTN: " + ex.getMessage(), ex))
                    .onErrorMap(WebClientResponseException.class,
                         ex -> new OTNServiceException("Error en respuesta de OTN: " + ex.getStatusCode(), ex))
                    .block();
                    
        } catch (Exception e) {
            log.error("Error al validar paquete {} con OTN: {}", codigoPaquete, e.getMessage());
            throw new OTNServiceException("No se pudo validar el paquete con OTN: " + e.getMessage(), e);
        }
    }

    @Override
    public ValidacionPaqueteResponse validarMultiplesPaquetes(ValidacionPaqueteRequest request) {
        log.info("Iniciando validación de {} paquetes", request.getCodigosPaquetes().size());
        
        Map<String, String> resultados = new HashMap<>();
        List<String> rechazados = new ArrayList<>();
        List<String> enProceso = new ArrayList<>();
        boolean todosAprobados = true;
        boolean puedeContinuar = true;

        // Verificar disponibilidad del servicio primero
        if (!isServiceAvailable()) {
            log.warn("Servicio OTN no disponible");
            return createResponseError("Servicio OTN no disponible en este momento");
        }

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
                
            } catch (OTNServiceException e) {
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
            response.setMensaje("Todos los paquetes han sido aprobados. Puede continuar con la compra.");
        } else if (!rechazados.isEmpty()) {
            response.setMensaje("No se puede continuar: " + rechazados.size() + " paquete(s) rechazado(s)");
        } else if (!enProceso.isEmpty()) {
            response.setMensaje("No se puede continuar: " + enProceso.size() + " paquete(s) en proceso de validación");
        } else {
            response.setMensaje("Error en la validación de paquetes");
        }

        return response;
    }

    @Override
    public boolean isServiceAvailable() {
        try {
            // Intentar una conexión simple para verificar disponibilidad
            webClient.get()
                    .uri("/health")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(3000))
                    .block();
            return true;
        } catch (Exception e) {
            log.warn("Servicio OTN no disponible: {}", e.getMessage());
            return false;
        }
    }

    private ValidacionPaqueteResponse createResponseError(String mensaje) {
        ValidacionPaqueteResponse response = new ValidacionPaqueteResponse();
        response.setTodosAprobados(false);
        response.setPuedeContinuar(false);
        response.setMensaje(mensaje);
        response.setResultadosValidacion(new HashMap<>());
        response.setPaquetesRechazados(new ArrayList<>());
        response.setPaquetesEnProceso(new ArrayList<>());
        return response;
    }
}