package com.tvpsystem.compras.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "paquetes_turisticos")
public class PaqueteTuristico {

    @Id
    private String codigo;

    @Column(nullable = false)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    @Column(nullable = false)
    private String destino;

    @Column(nullable = false)
    private Integer duracionDias;

    @Column(nullable = false)
    private Double precio;

    private Integer cuposDisponibles;

    @Enumerated(EnumType.STRING)
    private EstadoPaquete estado;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
        if (estado == null) {
            estado = EstadoPaquete.DISPONIBLE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    public enum EstadoPaquete {
        DISPONIBLE,
        NO_DISPONIBLE,
        AGOTADO,
        EN_VERIFICACION
    }
}