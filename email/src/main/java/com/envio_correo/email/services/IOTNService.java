package com.envio_correo.email.services;

import com.envio_correo.email.exceptions.OTNServiceException;
import com.envio_correo.email.services.models.OTNResponse;
import com.envio_correo.email.services.models.ValidacionPaqueteRequest;
import com.envio_correo.email.services.models.ValidacionPaqueteResponse;

public interface IOTNService {
    OTNResponse validarPaquete(String codigoPaquete) throws OTNServiceException;
    ValidacionPaqueteResponse validarMultiplesPaquetes(ValidacionPaqueteRequest request);
    boolean isServiceAvailable();
}