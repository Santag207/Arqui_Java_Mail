package com.tvpsystem.compras.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "compras")
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigoCompra;

    @Column(nullable = false)
    private String idCliente;

    @Column(nullable = false)
    private String nombreCliente;

    @Column(nullable = false)
    private String emailCliente;

    @Column(nullable = false)
    private String telefonoCliente;

    @ElementCollection
    @CollectionTable(name = "compra_paquetes", joinColumns = @JoinColumn(name = "compra_id"))
    @Column(name = "codigo_paquete")
    private List<String> codigosPaquetes = new ArrayList<>();

    @Column(nullable = false)
    private Double total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCompra estado = EstadoCompra.PENDIENTE;

    @Column(length = 1000)
    private String mensaje;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
        // Generar código único para la compra
        if (codigoCompra == null) {
            codigoCompra = "COMP-" + System.currentTimeMillis();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    public enum EstadoCompra {
        PENDIENTE,
        VALIDANDO,
        APROBADA,
        RECHAZADA,
        COMPLETADA,
        CANCELADA
    }
}