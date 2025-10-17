package com.tvpsystem.compras.services;

import com.tvpsystem.compras.services.models.CompraRequestDTO;
import com.tvpsystem.compras.services.models.CompraResponseDTO;

import java.util.List;

public interface ICompraService {
    CompraResponseDTO procesarCompra(CompraRequestDTO compraRequest);
    CompraResponseDTO obtenerCompraPorId(Long id);
    CompraResponseDTO obtenerCompraPorCodigo(String codigoCompra);
    List<CompraResponseDTO> obtenerComprasPorCliente(String idCliente);
    List<CompraResponseDTO> obtenerTodasLasCompras();
    CompraResponseDTO actualizarEstadoCompra(Long id, String nuevoEstado, String mensaje);
}