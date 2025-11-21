package com.tours.paymentservice.account.repository;

import com.tours.paymentservice.account.entity.CreditCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CreditCardRepository extends JpaRepository<CreditCard, Long> {
    List<CreditCard> findByClienteId(String clienteId);
    List<CreditCard> findByClienteIdAndIsActiveTrue(String clienteId);
    Optional<CreditCard> findByClienteIdAndIsDefaultTrue(String clienteId);
    Optional<CreditCard> findByClienteIdAndCardNumber(String clienteId, String cardNumber);
    Boolean existsByClienteIdAndCardNumber(String clienteId, String cardNumber);
}