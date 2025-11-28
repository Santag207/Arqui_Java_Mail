package com.tours.paymentservice.services.event;

import com.tours.paymentservice.account.entity.Account;
import com.tours.paymentservice.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserRegisteredListener {

    private final AccountRepository accountRepository;

    @RabbitListener(queues = "usuario.registrado.queue")
    public void handleUserRegistered(UserRegisteredEvent event) {
        try {
            log.info("📩 RECIBIENDO EVENTO: Usuario registrado - ID: {}, Email: {}", 
                    event.getUserId(), event.getEmail());

            // Verificar si la cuenta ya existe
            if (accountRepository.findByClienteId(String.valueOf(event.getUserId())).isPresent()) {
                log.warn("⚠️ Cuenta ya existe para usuario ID: {}", event.getUserId());
                return;
            }

            // Crear nueva cuenta con fondos iniciales
            Account account = new Account();
            account.setClienteId(String.valueOf(event.getUserId()));
            account.setBalance(5000.0); // Fondos iniciales para pruebas

            Account savedAccount = accountRepository.save(account);
            
            log.info("✅ CUENTA CREADA AUTOMÁTICAMENTE: ID: {}, ClienteId: {}, Balance: ${}", 
                    savedAccount.getId(), savedAccount.getClienteId(), savedAccount.getBalance());

        } catch (Exception e) {
            log.error("❌ Error procesando evento de usuario registrado: {}", e.getMessage(), e);
        }
    }
}
