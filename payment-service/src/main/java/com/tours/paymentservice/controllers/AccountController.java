package com.tours.paymentservice.controllers;

import com.tours.paymentservice.account.entity.Account;
import com.tours.paymentservice.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountRepository accountRepository;

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createAccount(@RequestBody Map<String, Object> request) {
        try {
            String clienteId = (String) request.get("clienteId");
            Double balance = ((Number) request.get("balance")).doubleValue();

            // Verificar si ya existe
            if (accountRepository.findByClienteId(clienteId).isPresent()) {
                log.warn("⚠️ Cuenta ya existe para clienteId: {}", clienteId);
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cuenta ya existía"
                ));
            }

            // Crear nueva cuenta
            Account account = new Account();
            account.setClienteId(clienteId);
            account.setBalance(balance);
            Account saved = accountRepository.save(account);

            log.info("✅ CUENTA CREADA: ClienteId={}, Balance={}", clienteId, balance);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Cuenta creada exitosamente",
                "clienteId", saved.getClienteId(),
                "balance", saved.getBalance()
            ));

        } catch (Exception e) {
            log.error("❌ Error creando cuenta: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error creando cuenta: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/{clienteId}")
    public ResponseEntity<?> getAccount(@PathVariable String clienteId) {
        return accountRepository.findByClienteId(clienteId)
                .map(account -> ResponseEntity.ok((Object) Map.of(
                    "clienteId", account.getClienteId(),
                    "balance", account.getBalance()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/add-balance")
    public ResponseEntity<Map<String, Object>> addBalance(@RequestBody Map<String, Object> request) {
        try {
            String clienteId = (String) request.get("clienteId");
            Double amount = ((Number) request.get("amount")).doubleValue();

            var account = accountRepository.findByClienteId(clienteId);
            if (account.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "Cuenta no encontrada para clienteId: " + clienteId
                ));
            }

            Account acc = account.get();
            acc.setBalance(acc.getBalance() + amount);
            Account updated = accountRepository.save(acc);

            log.info("✅ FONDOS AGREGADOS: ClienteId={}, Monto agregado={}, Nuevo balance={}", 
                clienteId, amount, updated.getBalance());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Fondos agregados exitosamente",
                "clienteId", updated.getClienteId(),
                "amountAdded", amount,
                "newBalance", updated.getBalance()
            ));

        } catch (Exception e) {
            log.error("❌ Error agregando fondos: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error agregando fondos: " + e.getMessage()
            ));
        }
    }
}
