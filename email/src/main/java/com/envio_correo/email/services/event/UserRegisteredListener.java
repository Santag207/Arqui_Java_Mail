package com.envio_correo.email.services.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserRegisteredListener {

    // Mapeo dinámico en memoria (en producción, usar base de datos)
    private static final ConcurrentHashMap<String, String> mapeoEmailsDinamico = new ConcurrentHashMap<>();

    static {
        // Precarga con datos de prueba
        mapeoEmailsDinamico.put("1", "castrosantiago476@gmail.com");
        mapeoEmailsDinamico.put("CLI-1001", "castrozsantiago@javeriana.edu.co");
        mapeoEmailsDinamico.put("CLI-2002", "castrosantiago476@gmail.com");
    }

    @RabbitListener(queues = "usuario.registrado.queue")
    public void handleUserRegistered(UserRegisteredEvent event) {
        try {
            log.info("📩 RECIBIENDO EVENTO: Usuario registrado - ID: {}, Email: {}", 
                    event.getUserId(), event.getEmail());

            // Guardar mapeo dinámico
            mapeoEmailsDinamico.put(String.valueOf(event.getUserId()), event.getEmail());
            
            log.info("✅ MAPEO ACTUALIZADO: {} -> {}", event.getUserId(), event.getEmail());
            log.info("📋 Mapeo actual: {}", mapeoEmailsDinamico);

        } catch (Exception e) {
            log.error("❌ Error procesando evento de usuario registrado: {}", e.getMessage(), e);
        }
    }

    /**
     * Mapear email de forma explícita (llamado desde AccountSyncService)
     */
    public static void mapearEmail(String clienteId, String email) {
        mapeoEmailsDinamico.put(clienteId, email);
        log.info("✅ EMAIL MAPEADO: {} -> {}", clienteId, email);
    }

    public static String obtenerEmailDinamico(String clienteId) {
        String email = mapeoEmailsDinamico.get(clienteId);
        
        if (email == null) {
            email = clienteId.toLowerCase() + "@toursadventure.com";
            log.warn("⚠️ Cliente no encontrado en mapeo dinámico: {}, usando default: {}", clienteId, email);
        } else {
            log.info("✅ Email encontrado en mapeo dinámico: {} -> {}", clienteId, email);
        }
        
        return email;
    }
}
