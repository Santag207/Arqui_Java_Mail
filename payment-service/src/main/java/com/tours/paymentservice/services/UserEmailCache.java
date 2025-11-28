package com.tours.paymentservice.services;

import com.tours.paymentservice.cache.UserData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class UserEmailCache {
    
    private static final ConcurrentHashMap<String, UserData> userDataCache = new ConcurrentHashMap<>();

    public void registrarUsuario(String clienteId, String email, String cedula, String direccion) {
        UserData userData = new UserData(clienteId, email, cedula, direccion);
        userDataCache.put(clienteId, userData);
        log.info("✅ USUARIO REGISTRADO EN CACHE: {} -> Email: {}, Cédula: {}, Dirección: {}", 
            clienteId, email, cedula, direccion);
    }

    public UserData obtenerUsuario(String clienteId) {
        UserData userData = userDataCache.get(clienteId);
        if (userData == null) {
            userData = new UserData(clienteId, clienteId.toLowerCase() + "@toursadventure.com", "N/A", "N/A");
            log.warn("⚠️ Usuario no encontrado en cache para: {}, usando defaults", clienteId);
        }
        return userData;
    }

    public String obtenerEmail(String clienteId) {
        UserData userData = userDataCache.get(clienteId);
        if (userData == null) {
            String email = clienteId.toLowerCase() + "@toursadventure.com";
            log.warn("⚠️ Email no encontrado en cache para: {}, usando default: {}", clienteId, email);
            return email;
        }
        return userData.getEmail();
    }

    public void limpiar() {
        userDataCache.clear();
    }
}
