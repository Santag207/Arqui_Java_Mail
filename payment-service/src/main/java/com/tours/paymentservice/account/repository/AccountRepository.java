package com.tours.paymentservice.account.repository;

import com.tours.paymentservice.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByClienteId(String clienteId);
}
