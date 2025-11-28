package com.tvpsystem.compras.controllers;

import com.tvpsystem.compras.services.ICompraService;
import com.tvpsystem.compras.services.models.CompraRequestDTO;
import com.tvpsystem.compras.services.models.CompraResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/compras")
public class CompraController {

    private final ICompraService compraService;

    public CompraController(ICompraService compraService) {
        this.compraService = compraService;
    }

    @PostMapping("/procesar")
    public ResponseEntity<CompraResponseDTO> procesarCompra(@RequestBody CompraRequestDTO compraRequest) {
        log.info("Recibida solicitud de compra para cliente: {}", compraRequest.getIdCliente());
        
        try {
            CompraResponseDTO response = compraService.procesarCompra(compraRequest);
            
            if (response.isExitosa()) {
                log.info("Compra procesada exitosamente: {}", response.getCodigoCompra());
                return ResponseEntity.ok(response);
            } else {
                log.warn("Compra rechazada: {}", response.getDetalleResultado());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            log.error("Error procesando compra: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompraResponseDTO> obtenerCompra(@PathVariable Long id) {
        CompraResponseDTO compra = compraService.obtenerCompraPorId(id);
        if (compra != null) {
            return ResponseEntity.ok(compra);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/codigo/{codigoCompra}")
    public ResponseEntity<CompraResponseDTO> obtenerCompraPorCodigo(@PathVariable String codigoCompra) {
        CompraResponseDTO compra = compraService.obtenerCompraPorCodigo(codigoCompra);
        if (compra != null) {
            return ResponseEntity.ok(compra);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<List<CompraResponseDTO>> obtenerComprasPorCliente(@PathVariable String idCliente) {
        List<CompraResponseDTO> compras = compraService.obtenerComprasPorCliente(idCliente);
        return ResponseEntity.ok(compras);
    }

    @GetMapping
    public ResponseEntity<List<CompraResponseDTO>> obtenerTodasLasCompras() {
        List<CompraResponseDTO> compras = compraService.obtenerTodasLasCompras();
        return ResponseEntity.ok(compras);
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<CompraResponseDTO> actualizarEstadoCompra(
            @PathVariable Long id,
            @RequestParam String estado,
            @RequestParam(required = false) String mensaje) {
        
        CompraResponseDTO response = compraService.actualizarEstadoCompra(id, estado, mensaje);
        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}