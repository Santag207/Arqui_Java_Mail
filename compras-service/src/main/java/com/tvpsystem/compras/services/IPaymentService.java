package com.tvpsystem.compras.services;

import com.tvpsystem.compras.services.models.PaymentRequestDTO;
import com.tvpsystem.compras.services.models.PaymentResponseDTO;

public interface IPaymentService {
    PaymentResponseDTO validarPago(PaymentRequestDTO request);
}
