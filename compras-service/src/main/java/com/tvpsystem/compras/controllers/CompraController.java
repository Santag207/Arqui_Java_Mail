package com.tvpsystem.compras.controllers;

import com.tvpsystem.compras.services.ICompraService;
import com.tvpsystem.compras.services.models.CompraRequestDTO;
import com.tvpsystem.compras.services.models.CompraResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/compras")
public class CompraController {

    private final ICompraService compraService;
    private final RestTemplate restTemplate;

    @Value("${auth.service.url:http://localhost:8081}")
    private String authServiceUrl;

    public CompraController(ICompraService compraService, RestTemplate restTemplate) {
        this.compraService = compraService;
        this.restTemplate = restTemplate;
    }

    @PostMapping("/procesar")
    public ResponseEntity<CompraResponseDTO> procesarCompra(@RequestBody CompraRequestDTO compraRequest) {
        try {
            // Obtener el token del header
            String token = getTokenFromRequest();
            if (token != null) {
                // Obtener información del usuario desde el servicio de auth
                Map<String, Object> userInfo = getUserInfoFromAuthService(token);
                if (userInfo != null) {
                    String usuarioId = String.valueOf(userInfo.getOrDefault("usuarioId", ""));
                    String email = (String) userInfo.getOrDefault("email", "");
                    
                    if (!usuarioId.isEmpty() && !usuarioId.equals("null")) {
                        compraRequest.setIdCliente(usuarioId);
                    }
                    if (!email.isEmpty()) {
                        compraRequest.setEmailCliente(email);
                    }
                }
            }
            
            log.info("Recibida solicitud de compra para cliente: {}", compraRequest.getIdCliente());
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

    private String getTokenFromRequest() {
        try {
            // Obtener del contexto de seguridad
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                // El token está almacenado en credentials
                Object credentials = auth.getCredentials();
                if (credentials != null) {
                    return credentials.toString();
                }
            }
        } catch (Exception e) {
            log.debug("No se pudo obtener token del contexto de seguridad");
        }
        return null;
    }

    private Map<String, Object> getUserInfoFromAuthService(String token) {
        try {
            String url = authServiceUrl + "/auth/validate";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            var response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.debug("Error obteniendo información del usuario desde auth service: {}", e.getMessage());
        }
        return null;
    }
}