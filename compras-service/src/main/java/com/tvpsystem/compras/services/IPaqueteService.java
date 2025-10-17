package com.tvpsystem.compras.services;

import com.tvpsystem.compras.services.models.PaqueteDTO;

import java.util.List;

public interface IPaqueteService {
    PaqueteDTO crearPaquete(PaqueteDTO paqueteDTO);
    PaqueteDTO actualizarPaquete(String codigo, PaqueteDTO paqueteDTO);
    void eliminarPaquete(String codigo);
    PaqueteDTO obtenerPaquetePorCodigo(String codigo);
    List<PaqueteDTO> obtenerTodosLosPaquetes();
    List<PaqueteDTO> obtenerPaquetesDisponibles();
    List<PaqueteDTO> buscarPaquetesPorDestino(String destino);
    boolean validarDisponibilidadPaquete(String codigoPaquete);
    Double calcularTotalCompra(List<String> codigosPaquetes);
}