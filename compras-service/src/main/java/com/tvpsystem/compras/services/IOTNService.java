package com.tvpsystem.compras.services;

import com.tvpsystem.compras.exceptions.OTNServiceException;
import com.tvpsystem.compras.services.models.OTNResponse;
import com.tvpsystem.compras.services.models.ValidacionPaqueteRequest;
import com.tvpsystem.compras.services.models.ValidacionPaqueteResponse;

public interface IOTNService {
    OTNResponse validarPaquete(String codigoPaquete) throws OTNServiceException;
    ValidacionPaqueteResponse validarMultiplesPaquetes(ValidacionPaqueteRequest request);
    boolean isServiceAvailable();
}