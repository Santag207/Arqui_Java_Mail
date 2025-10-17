package com.tvpsystem.compras.services.models;

import com.tvpsystem.compras.entities.Compra;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CompraResponseDTO {
    private Long id;
    private String codigoCompra;
    private String idCliente;
    private String nombreCliente;
    private String emailCliente;
    private String telefonoCliente;
    private List<String> codigosPaquetes;
    private Double total;
    private Compra.EstadoCompra estado;
    private String mensaje;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private boolean exitosa;
    private String detalleResultado;
}