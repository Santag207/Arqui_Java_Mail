package com.envio_correo.email.services.impl;

import com.envio_correo.email.exceptions.OTNServiceException;
import com.envio_correo.email.services.IOTNService;
import com.envio_correo.email.services.models.OTNResponse;
import com.envio_correo.email.services.models.ValidacionPaqueteRequest;
import com.envio_correo.email.services.models.ValidacionPaqueteResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@Profile("!prod")
public class MockOTNService implements IOTNService {

    @Value("${otn.mock.enabled:true}")
    private boolean mockEnabled;

    private final Random random = new Random();

    // Estados posibles para simulación
    private final List<String> estados = Arrays.asList("APROBADO", "RECHAZADO", "EN_PROCESO");

    @Override
    public OTNResponse validarPaquete(String codigoPaquete) throws OTNServiceException {
        log.info("Validando paquete único con código: {}", codigoPaquete);

        // Simular un error aleatorio (5% de las veces)
        if (random.nextInt(100) < 5) {
            throw new OTNServiceException("Error simulado en la validación del paquete");
        }

        OTNResponse response = new OTNResponse();
        response.setCodigoPaquete(codigoPaquete);

        // Asignar estado aleatorio
        String estado = estados.get(random.nextInt(estados.size()));
        response.setEstado(estado);
        response.setAprobado("APROBADO".equals(estado));
        response.setTimestamp(LocalDateTime.now()); // CORREGIDO: Usar LocalDateTime

        // Mensaje según el estado
        if ("APROBADO".equals(estado)) {
            response.setMensaje("Paquete aprobado exitosamente");
        } else if ("RECHAZADO".equals(estado)) {
            response.setMensaje("Paquete rechazado por políticas de seguridad");
        } else {
            response.setMensaje("Paquete en proceso de validación");
        }

        // Simular retardo de red
        try {
            Thread.sleep(100 + random.nextInt(400));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OTNServiceException("Validación interrumpida", e);
        }

        return response;
    }

    @Override
    public ValidacionPaqueteResponse validarMultiplesPaquetes(ValidacionPaqueteRequest request) {
        log.info("Validando {} paquetes. ID Solicitud: {}", 
                 request.getCodigosPaquetes().size(), request.getIdSolicitud());

        ValidacionPaqueteResponse response = new ValidacionPaqueteResponse();

        int aprobados = 0;
        int rechazados = 0;
        int enProceso = 0;

        for (String codigo : request.getCodigosPaquetes()) {
            try {
                OTNResponse otnResponse = validarPaquete(codigo);
                response.getResultadosValidacion().put(codigo, otnResponse.getEstado());

                if (otnResponse.isAprobado()) {
                    aprobados++;
                } else if ("RECHAZADO".equals(otnResponse.getEstado())) {
                    rechazados++;
                    response.getPaquetesRechazados().add(codigo);
                } else {
                    enProceso++;
                    response.getPaquetesEnProceso().add(codigo);
                }

            } catch (OTNServiceException e) {
                log.error("Error validando paquete {}: {}", codigo, e.getMessage());
                response.getResultadosValidacion().put(codigo, "ERROR");
                rechazados++;
                response.getPaquetesRechazados().add(codigo);
            }
        }

        // Determinar si todos están aprobados
        response.setTodosAprobados(rechazados == 0 && enProceso == 0);
        // Puede continuar si no hay rechazados (los en proceso están permitidos)
        response.setPuedeContinuar(rechazados == 0);

        // Construir mensaje
        if (response.isTodosAprobados()) {
            response.setMensaje("Todos los paquetes fueron aprobados. Puede continuar con la compra.");
        } else if (response.isPuedeContinuar()) {
            response.setMensaje(String.format(
                "Validación parcialmente exitosa. %d aprobados, %d en proceso. Puede continuar con la compra.",
                aprobados, enProceso));
        } else {
            response.setMensaje(String.format(
                "Validación fallida. %d aprobados, %d rechazados, %d en proceso. No puede continuar con la compra.",
                aprobados, rechazados, enProceso));
        }

        return response;
    }

    @Override
    public boolean isServiceAvailable() {
        return mockEnabled;
    }
}