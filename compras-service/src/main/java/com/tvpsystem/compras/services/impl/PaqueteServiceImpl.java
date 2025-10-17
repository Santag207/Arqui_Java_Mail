package com.tvpsystem.compras.services.impl;

import com.tvpsystem.compras.entities.PaqueteTuristico;
import com.tvpsystem.compras.repositories.PaqueteRepository;
import com.tvpsystem.compras.services.IPaqueteService;
import com.tvpsystem.compras.services.models.PaqueteDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PaqueteServiceImpl implements IPaqueteService {

    private final PaqueteRepository paqueteRepository;

    public PaqueteServiceImpl(PaqueteRepository paqueteRepository) {
        this.paqueteRepository = paqueteRepository;
    }

    @Override
    public PaqueteDTO crearPaquete(PaqueteDTO paqueteDTO) {
        log.info("Creando nuevo paquete turístico: {}", paqueteDTO.getCodigo());

        PaqueteTuristico paquete = new PaqueteTuristico();
        paquete.setCodigo(paqueteDTO.getCodigo());
        paquete.setNombre(paqueteDTO.getNombre());
        paquete.setDescripcion(paqueteDTO.getDescripcion());
        paquete.setDestino(paqueteDTO.getDestino());
        paquete.setDuracionDias(paqueteDTO.getDuracionDias());
        paquete.setPrecio(paqueteDTO.getPrecio());
        paquete.setCuposDisponibles(paqueteDTO.getCuposDisponibles());
        paquete.setEstado(paqueteDTO.getEstado());
        paquete.setFechaInicio(paqueteDTO.getFechaInicio());
        paquete.setFechaFin(paqueteDTO.getFechaFin());

        paquete = paqueteRepository.save(paquete);
        log.info("Paquete creado exitosamente: {}", paquete.getCodigo());

        return convertirADTO(paquete);
    }

    @Override
    public PaqueteDTO actualizarPaquete(String codigo, PaqueteDTO paqueteDTO) {
        log.info("Actualizando paquete: {}", codigo);

        return paqueteRepository.findById(codigo)
                .map(paquete -> {
                    paquete.setNombre(paqueteDTO.getNombre());
                    paquete.setDescripcion(paqueteDTO.getDescripcion());
                    paquete.setDestino(paqueteDTO.getDestino());
                    paquete.setDuracionDias(paqueteDTO.getDuracionDias());
                    paquete.setPrecio(paqueteDTO.getPrecio());
                    paquete.setCuposDisponibles(paqueteDTO.getCuposDisponibles());
                    paquete.setEstado(paqueteDTO.getEstado());
                    paquete.setFechaInicio(paqueteDTO.getFechaInicio());
                    paquete.setFechaFin(paqueteDTO.getFechaFin());

                    paquete = paqueteRepository.save(paquete);
                    log.info("Paquete actualizado exitosamente: {}", codigo);

                    return convertirADTO(paquete);
                })
                .orElseThrow(() -> new RuntimeException("Paquete no encontrado: " + codigo));
    }

    @Override
    public void eliminarPaquete(String codigo) {
        log.info("Eliminando paquete: {}", codigo);
        if (paqueteRepository.existsById(codigo)) {
            paqueteRepository.deleteById(codigo);
            log.info("Paquete eliminado: {}", codigo);
        } else {
            throw new RuntimeException("Paquete no encontrado: " + codigo);
        }
    }

    @Override
    public PaqueteDTO obtenerPaquetePorCodigo(String codigo) {
        return paqueteRepository.findById(codigo)
                .map(this::convertirADTO)
                .orElse(null);
    }

    @Override
    public List<PaqueteDTO> obtenerTodosLosPaquetes() {
        return paqueteRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaqueteDTO> obtenerPaquetesDisponibles() {
        return paqueteRepository.findPaquetesDisponibles()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaqueteDTO> buscarPaquetesPorDestino(String destino) {
        return paqueteRepository.findByDestino(destino)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean validarDisponibilidadPaquete(String codigoPaquete) {
        return paqueteRepository.findById(codigoPaquete)
                .map(paquete -> 
                    paquete.getEstado() == PaqueteTuristico.EstadoPaquete.DISPONIBLE &&
                    (paquete.getCuposDisponibles() == null || paquete.getCuposDisponibles() > 0)
                )
                .orElse(false);
    }

    @Override
    public Double calcularTotalCompra(List<String> codigosPaquetes) {
        return codigosPaquetes.stream()
                .map(codigo -> paqueteRepository.findById(codigo)
                        .map(PaqueteTuristico::getPrecio)
                        .orElse(0.0))
                .reduce(0.0, Double::sum);
    }

    private PaqueteDTO convertirADTO(PaqueteTuristico paquete) {
        PaqueteDTO dto = new PaqueteDTO();
        dto.setCodigo(paquete.getCodigo());
        dto.setNombre(paquete.getNombre());
        dto.setDescripcion(paquete.getDescripcion());
        dto.setDestino(paquete.getDestino());
        dto.setDuracionDias(paquete.getDuracionDias());
        dto.setPrecio(paquete.getPrecio());
        dto.setCuposDisponibles(paquete.getCuposDisponibles());
        dto.setEstado(paquete.getEstado());
        dto.setFechaInicio(paquete.getFechaInicio());
        dto.setFechaFin(paquete.getFechaFin());
        dto.setFechaCreacion(paquete.getFechaCreacion());
        return dto;
    }
}