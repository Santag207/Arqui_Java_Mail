package com.tvpsystem.compras.controllers;

import com.tvpsystem.compras.services.IPaqueteService;
import com.tvpsystem.compras.services.models.PaqueteDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/paquetes")
public class PaqueteController {

    private final IPaqueteService paqueteService;

    public PaqueteController(IPaqueteService paqueteService) {
        this.paqueteService = paqueteService;
    }

    @PostMapping
    public ResponseEntity<PaqueteDTO> crearPaquete(@RequestBody PaqueteDTO paqueteDTO) {
        try {
            PaqueteDTO nuevoPaquete = paqueteService.crearPaquete(paqueteDTO);
            return ResponseEntity.ok(nuevoPaquete);
        } catch (Exception e) {
            log.error("Error creando paquete: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<PaqueteDTO> actualizarPaquete(
            @PathVariable String codigo, 
            @RequestBody PaqueteDTO paqueteDTO) {
        try {
            PaqueteDTO paqueteActualizado = paqueteService.actualizarPaquete(codigo, paqueteDTO);
            return ResponseEntity.ok(paqueteActualizado);
        } catch (Exception e) {
            log.error("Error actualizando paquete: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> eliminarPaquete(@PathVariable String codigo) {
        try {
            paqueteService.eliminarPaquete(codigo);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error eliminando paquete: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<PaqueteDTO> obtenerPaquete(@PathVariable String codigo) {
        PaqueteDTO paquete = paqueteService.obtenerPaquetePorCodigo(codigo);
        if (paquete != null) {
            return ResponseEntity.ok(paquete);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<PaqueteDTO>> obtenerTodosLosPaquetes() {
        List<PaqueteDTO> paquetes = paqueteService.obtenerTodosLosPaquetes();
        return ResponseEntity.ok(paquetes);
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<PaqueteDTO>> obtenerPaquetesDisponibles() {
        List<PaqueteDTO> paquetes = paqueteService.obtenerPaquetesDisponibles();
        return ResponseEntity.ok(paquetes);
    }

    @GetMapping("/destino/{destino}")
    public ResponseEntity<List<PaqueteDTO>> buscarPaquetesPorDestino(@PathVariable String destino) {
        List<PaqueteDTO> paquetes = paqueteService.buscarPaquetesPorDestino(destino);
        return ResponseEntity.ok(paquetes);
    }
}