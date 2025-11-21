package com.tours.paymentservice.config;

import com.tours.paymentservice.account.entity.Account;
import com.tours.paymentservice.account.entity.CreditCard;
import com.tours.paymentservice.account.repository.AccountRepository;
import com.tours.paymentservice.account.repository.CreditCardRepository;
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
    public CommandLineRunner initData(InventoryRepository inventoryRepository,
                                      AccountRepository accountRepository,
                                      CreditCardRepository creditCardRepository) {
        return args -> {
            // 1. Inicializar slots de inventario
            if (inventoryRepository.count() == 0) {
                InventorySlot s1 = new InventorySlot();
                s1.setCodigoPaquete("PAQ-PLAYA-01");
                s1.setCuposDisponibles(5);
                inventoryRepository.save(s1);

                InventorySlot s2 = new InventorySlot();
                s2.setCodigoPaquete("PAQ-MONTAÑA-02");
                s2.setCuposDisponibles(2);
                inventoryRepository.save(s2);

                InventorySlot s3 = new InventorySlot();
                s3.setCodigoPaquete("PAQ-CIUDAD-03");
                s3.setCuposDisponibles(8);
                inventoryRepository.save(s3);

                InventorySlot s4 = new InventorySlot();
                s4.setCodigoPaquete("PAQ-AVENTURA-04");
                s4.setCuposDisponibles(3);
                inventoryRepository.save(s4);

                System.out.println("✅ Slots de inventario inicializados");
            }

            // 2. Inicializar cuentas de clientes
            if (accountRepository.count() == 0) {
                Account a1 = new Account();
                a1.setClienteId("CLI-1001");
                a1.setBalance(2000.0);
                accountRepository.save(a1);

                Account a2 = new Account();
                a2.setClienteId("CLI-2002");
                a2.setBalance(100.0);
                accountRepository.save(a2);

                Account a3 = new Account();
                a3.setClienteId("CLI-3003");
                a3.setBalance(1500.0);
                accountRepository.save(a3);

                Account a4 = new Account();
                a4.setClienteId("CLI-4004");
                a4.setBalance(500.0);
                accountRepository.save(a4);

                System.out.println("✅ Cuentas de clientes inicializadas");
            }

            // 3. Inicializar tarjetas de crédito
            if (creditCardRepository.count() == 0) {
                // Tarjetas para CLI-1001
                CreditCard card1 = new CreditCard();
                card1.setClienteId("CLI-1001");
                card1.setCardNumber("****1234");
                card1.setCardHolder("SANTIAGO CASTRO");
                card1.setExpiryMonth(12);
                card1.setExpiryYear(2026);
                card1.setCvv("ENC_123");
                card1.setIsDefault(true);
                creditCardRepository.save(card1);

                CreditCard card2 = new CreditCard();
                card2.setClienteId("CLI-1001");
                card2.setCardNumber("****5678");
                card2.setCardHolder("SANTIAGO CASTRO");
                card2.setExpiryMonth(6);
                card2.setExpiryYear(2025);
                card2.setCvv("ENC_456");
                card2.setIsDefault(false);
                creditCardRepository.save(card2);

                // Tarjetas para CLI-2002
                CreditCard card3 = new CreditCard();
                card3.setClienteId("CLI-2002");
                card3.setCardNumber("****9012");
                card3.setCardHolder("MARIA GOMEZ");
                card3.setExpiryMonth(3);
                card3.setExpiryYear(2027);
                card3.setCvv("ENC_789");
                card3.setIsDefault(true);
                creditCardRepository.save(card3);

                // Tarjetas para CLI-3003
                CreditCard card4 = new CreditCard();
                card4.setClienteId("CLI-3003");
                card4.setCardNumber("****3456");
                card4.setCardHolder("CARLOS LOPEZ");
                card4.setExpiryMonth(9);
                card4.setExpiryYear(2026);
                card4.setCvv("ENC_012");
                card4.setIsDefault(true);
                creditCardRepository.save(card4);

                // Tarjetas para CLI-4004
                CreditCard card5 = new CreditCard();
                card5.setClienteId("CLI-4004");
                card5.setCardNumber("****7890");
                card5.setCardHolder("ANA MARTINEZ");
                card5.setExpiryMonth(11);
                card5.setExpiryYear(2025);
                card5.setCvv("ENC_345");
                card5.setIsDefault(true);
                creditCardRepository.save(card5);

                System.out.println("✅ Tarjetas de crédito inicializadas");
            }

            System.out.println("🎯 Payment Service - Datos de prueba cargados exitosamente");
        };
    }
}