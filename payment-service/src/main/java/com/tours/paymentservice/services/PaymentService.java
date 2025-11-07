package com.tours.paymentservice.services;

import com.tours.paymentservice.services.dto.PaymentRequestDTO;
import com.tours.paymentservice.services.dto.PaymentResponseDTO;

public interface PaymentService {
    PaymentResponseDTO validarYReservar(PaymentRequestDTO request);
}
