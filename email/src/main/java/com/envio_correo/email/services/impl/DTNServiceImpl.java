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
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

@Slf4j
@Service
@Profile("prod")
public class DTNServiceImpl implements IOTNService {

    @Value("${otn.service.url}")
    private String otnServiceUrl;

    @Value("${otn.service.timeout:5000}")
    private int timeout;

    private final RestTemplate restTemplate;

    public DTNServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public OTNResponse validarPaquete(String codigoPaquete) throws OTNServiceException {
        log.info("Enviando validación de paquete a OTN: {}/paquete/{}", otnServiceUrl, codigoPaquete);
        
        try {
            // Implementación real de llamada al servicio OTN
            ResponseEntity<OTNResponse> response = restTemplate.getForEntity(
                otnServiceUrl + "/paquete/" + codigoPaquete, 
                OTNResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new OTNServiceException("Error en respuesta del servicio OTN: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("Error en validación de paquete con OTN: {}", e.getMessage());
            throw new OTNServiceException("Error en servicio OTN: " + e.getMessage(), e);
        }
    }

    @Override
    public ValidacionPaqueteResponse validarMultiplesPaquetes(ValidacionPaqueteRequest request) {
        log.info("Enviando validación de múltiples paquetes a OTN: {}/paquetes", otnServiceUrl);
        
        ValidacionPaqueteResponse response = new ValidacionPaqueteResponse();
        
        try {
            // Llamada real al servicio OTN para múltiples paquetes
            ResponseEntity<ValidacionPaqueteResponse> otnResponse = restTemplate.postForEntity(
                otnServiceUrl + "/paquetes",
                request,
                ValidacionPaqueteResponse.class
            );
            
            if (otnResponse.getStatusCode().is2xxSuccessful() && otnResponse.getBody() != null) {
                return otnResponse.getBody();
            } else {
                response.setPuedeContinuar(false);
                response.setMensaje("Error en respuesta del servicio OTN: " + otnResponse.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("Error en validación de múltiples paquetes con OTN: {}", e.getMessage());
            response.setPuedeContinuar(false);
            response.setMensaje("Error en servicio OTN: " + e.getMessage());
        }
        
        return response;
    }

    @Override
    public boolean isServiceAvailable() {
        try {
            // Health check al servicio OTN
            ResponseEntity<String> healthResponse = restTemplate.getForEntity(
                otnServiceUrl + "/health", 
                String.class
            );
            return healthResponse.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("Servicio OTN no disponible: {}", e.getMessage());
            return false;
        }
    }
}