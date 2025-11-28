package com.tours.paymentservice.cache;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clase para almacenar datos del usuario en el cache de payment-service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserData {
    private String clienteId;
    private String email;
    private String cedula;
    private String direccion;
}
