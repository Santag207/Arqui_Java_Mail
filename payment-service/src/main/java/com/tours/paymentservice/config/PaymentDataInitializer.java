package com.tours.paymentservice.config;

import com.tours.paymentservice.account.entity.Account;
import com.tours.paymentservice.account.repository.AccountRepository;
import com.tours.paymentservice.inventory.entity.InventorySlot;
import com.tours.paymentservice.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class PaymentDataInitializer {

    @Bean
    public CommandLineRunner initData(InventoryRepository inventoryRepository, AccountRepository accountRepository) {
        return args -> {
            if (inventoryRepository.count() == 0) {
                InventorySlot s1 = new InventorySlot();
                s1.setCodigoPaquete("PAQ-PLAYA-01");
                s1.setCuposDisponibles(5);
                inventoryRepository.save(s1);

                InventorySlot s2 = new InventorySlot();
                s2.setCodigoPaquete("PAQ-MONTAÑA-02");
                s2.setCuposDisponibles(2);
                inventoryRepository.save(s2);
            }

            if (accountRepository.count() == 0) {
                Account a1 = new Account();
                a1.setClienteId("CLI-1001");
                a1.setBalance(2000.0);
                accountRepository.save(a1);

                Account a2 = new Account();
                a2.setClienteId("CLI-2002");
                a2.setBalance(100.0);
                accountRepository.save(a2);
            }
        };
    }
}
