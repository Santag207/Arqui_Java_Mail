package com.tours.paymentservice.config;

import com.tours.paymentservice.account.entity.Account;
import com.tours.paymentservice.account.repository.AccountRepository;
import com.tours.paymentservice.inventory.entity.InventorySlot;
import com.tours.paymentservice.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class PaymentDataInitializer {

    @Bean
    public CommandLineRunner initData(InventoryRepository inventoryRepository, AccountRepository accountRepository) {
        return args -> {
            if (inventoryRepository.count() == 0) {
                InventorySlot s1 = new InventorySlot();
                s1.setCodigoPaquete("PKG-001");
                s1.setCuposDisponibles(10);
                inventoryRepository.save(s1);

                InventorySlot s2 = new InventorySlot();
                s2.setCodigoPaquete("PKG-002");
                s2.setCuposDisponibles(10);
                inventoryRepository.save(s2);

                InventorySlot s3 = new InventorySlot();
                s3.setCodigoPaquete("PAQ-PLAYA-01");
                s3.setCuposDisponibles(5);
                inventoryRepository.save(s3);

                InventorySlot s4 = new InventorySlot();
                s4.setCodigoPaquete("PAQ-MONTAÑA-02");
                s4.setCuposDisponibles(2);
                inventoryRepository.save(s4);
                
                log.info("✅ Paquetes inicializados en inventario");
            }

            // Cuentas se crean dinámicamente cuando se registran usuarios
            // Los datos iniciales se mantienen solo si es necesario para pruebas
            if (accountRepository.count() == 0) {
                log.info("✅ Sin cuentas iniciales - Se crearán dinámicamente al registrar usuarios");
            }
        };
    }
}
