package com.tvpsystem.compras.services.models;

import com.tvpsystem.compras.entities.PaqueteTuristico;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PaqueteDTO {
    private String codigo;
    private String nombre;
    private String descripcion;
    private String destino;
    private Integer duracionDias;
    private Double precio;
    private Integer cuposDisponibles;
    private PaqueteTuristico.EstadoPaquete estado;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private LocalDateTime fechaCreacion;
}