package com.tours.paymentservice.inventory.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "inventory_slots")
@Data
public class InventorySlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String codigoPaquete;

    @Column(nullable = false)
    private Integer cuposDisponibles;
}
