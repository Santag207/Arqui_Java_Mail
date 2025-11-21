package com.tours.paymentservice.controllers;

import com.tours.paymentservice.account.entity.CreditCard;
import com.tours.paymentservice.services.CreditCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payment/cards")
@RequiredArgsConstructor
public class CreditCardController {

    private final CreditCardService creditCardService;

    @GetMapping("/{clienteId}")
    public ResponseEntity<List<CreditCard>> getCardsByCliente(@PathVariable String clienteId) {
        List<CreditCard> cards = creditCardService.getCardsByCliente(clienteId);
        return ResponseEntity.ok(cards);
    }

    @PostMapping("/{clienteId}")
    public ResponseEntity<CreditCard> addCard(@PathVariable String clienteId, @RequestBody CreditCard card) {
        card.setClienteId(clienteId);
        CreditCard savedCard = creditCardService.addCard(card);
        return ResponseEntity.ok(savedCard);
    }

    @PutMapping("/{clienteId}/default/{cardId}")
    public ResponseEntity<Map<String, String>> setDefaultCard(
            @PathVariable String clienteId,
            @PathVariable Long cardId) {
        creditCardService.setDefaultCard(clienteId, cardId);
        return ResponseEntity.ok(Map.of("message", "Tarjeta establecida como predeterminada"));
    }

    @DeleteMapping("/{clienteId}/{cardId}")
    public ResponseEntity<Map<String, String>> deleteCard(
            @PathVariable String clienteId,
            @PathVariable Long cardId) {
        creditCardService.deleteCard(clienteId, cardId);
        return ResponseEntity.ok(Map.of("message", "Tarjeta eliminada"));
    }
}