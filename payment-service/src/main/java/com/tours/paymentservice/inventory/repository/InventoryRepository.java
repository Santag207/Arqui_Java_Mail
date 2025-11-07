package com.tours.paymentservice.inventory.repository;

import com.tours.paymentservice.inventory.entity.InventorySlot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<InventorySlot, Long> {
    Optional<InventorySlot> findByCodigoPaquete(String codigoPaquete);
}
