package com.tours.compras.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "paquetes")
@Data
public class Paquete {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String codigo;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    @Column(nullable = false)
    private String destino;

    @Column(name = "duracion_dias", nullable = false)
    private Integer duracionDias;

    @Column(nullable = false)
    private BigDecimal precio;

    @Column(name = "cupos_disponibles", nullable = false)
    private Integer cuposDisponibles;

    @Column(nullable = false)
    private String estado; // DISPONIBLE, AGOTADO, CANCELADO

    // NUEVO CAMPO: Fecha de salida del tour
    @Column(name = "fecha_salida", nullable = false)
    private LocalDate fechaSalida;

    // NUEVO CAMPO: Fecha de regreso (calculada)
    @Column(name = "fecha_regreso", nullable = false)
    private LocalDate fechaRegreso;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Método para calcular fecha de regreso automáticamente
    @PrePersist
    @PreUpdate
    private void calcularFechas() {
        if (fechaSalida != null && duracionDias != null) {
            this.fechaRegreso = fechaSalida.plusDays(duracionDias);
        }
    }
}