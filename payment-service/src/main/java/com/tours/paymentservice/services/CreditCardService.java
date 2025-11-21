package com.tours.paymentservice.services;

import com.tours.paymentservice.account.entity.CreditCard;
import com.tours.paymentservice.account.repository.CreditCardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreditCardService {

    private final CreditCardRepository creditCardRepository;

    public List<CreditCard> getCardsByCliente(String clienteId) {
        return creditCardRepository.findByClienteIdAndIsActiveTrue(clienteId);
    }

    public Optional<CreditCard> getDefaultCard(String clienteId) {
        return creditCardRepository.findByClienteIdAndIsDefaultTrue(clienteId);
    }

    @Transactional
    public CreditCard addCard(CreditCard card) {
        // Verificar si ya existe la tarjeta
        if (creditCardRepository.existsByClienteIdAndCardNumber(card.getClienteId(), card.getCardNumber())) {
            throw new RuntimeException("La tarjeta ya está registrada para este cliente");
        }

        // Si es la primera tarjeta, hacerla por defecto
        List<CreditCard> existingCards = creditCardRepository.findByClienteId(card.getClienteId());
        if (existingCards.isEmpty()) {
            card.setIsDefault(true);
        }

        // Enmascarar número de tarjeta (guardar solo últimos 4 dígitos)
        String fullCardNumber = card.getCardNumber();
        String maskedNumber = "****" + fullCardNumber.substring(fullCardNumber.length() - 4);
        card.setCardNumber(maskedNumber);

        // Encriptar CVV (en una implementación real usaríamos encryption)
        card.setCvv("ENC_" + card.getCvv()); // Placeholder para encryption

        return creditCardRepository.save(card);
    }

    @Transactional
    public void setDefaultCard(String clienteId, Long cardId) {
        // Quitar default de todas las tarjetas del cliente
        List<CreditCard> allCards = creditCardRepository.findByClienteId(clienteId);
        allCards.forEach(card -> card.setIsDefault(false));
        creditCardRepository.saveAll(allCards);

        // Establecer nueva tarjeta como default
        CreditCard newDefault = creditCardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada"));
        newDefault.setIsDefault(true);
        creditCardRepository.save(newDefault);
    }

    @Transactional
    public void deleteCard(String clienteId, Long cardId) {
        CreditCard card = creditCardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada"));

        if (!card.getClienteId().equals(clienteId)) {
            throw new RuntimeException("No autorizado para eliminar esta tarjeta");
        }

        // Si es la tarjeta por defecto, asignar otra como default
        if (card.getIsDefault()) {
            List<CreditCard> otherCards = creditCardRepository.findByClienteIdAndIsActiveTrue(clienteId);
            otherCards.remove(card);
            if (!otherCards.isEmpty()) {
                otherCards.get(0).setIsDefault(true);
                creditCardRepository.save(otherCards.get(0));
            }
        }

        card.setIsActive(false);
        creditCardRepository.save(card);
    }

    public boolean validateCard(String clienteId, String cardLastFour) {
        return creditCardRepository.findByClienteId(clienteId).stream()
                .anyMatch(card -> card.getCardNumber().endsWith(cardLastFour) && card.getIsActive());
    }
}