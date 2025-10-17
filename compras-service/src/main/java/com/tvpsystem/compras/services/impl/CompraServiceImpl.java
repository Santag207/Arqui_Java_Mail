package com.tvpsystem.compras.services.impl;

import com.tvpsystem.compras.entities.Compra;
import com.tvpsystem.compras.repositories.CompraRepository;
import com.tvpsystem.compras.services.ICompraService;
import com.tvpsystem.compras.services.IPaqueteService;
import com.tvpsystem.compras.services.IOTNService;
import com.tvpsystem.compras.services.models.CompraRequestDTO;
import com.tvpsystem.compras.services.models.CompraResponseDTO;
import com.tvpsystem.compras.services.models.ValidacionPaqueteRequest;
import com.tvpsystem.compras.services.models.ValidacionPaqueteResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CompraServiceImpl implements ICompraService {

    private final CompraRepository compraRepository;
    private final IPaqueteService paqueteService;
    private final IOTNService otnService;

    public CompraServiceImpl(CompraRepository compraRepository, 
                           IPaqueteService paqueteService, 
                           IOTNService otnService) {
        this.compraRepository = compraRepository;
        this.paqueteService = paqueteService;
        this.otnService = otnService;
    }

    @Override
    @Transactional
    public CompraResponseDTO procesarCompra(CompraRequestDTO compraRequest) {
        log.info("🛒 INICIANDO PROCESAMIENTO DE COMPRA para cliente: {}", compraRequest.getIdCliente());
        log.info("📦 Paquetes solicitados: {}", compraRequest.getCodigosPaquetes());

        try {
            // 1. Validar disponibilidad de paquetes
            log.info("🔍 Validando disponibilidad de paquetes...");
            if (!validarDisponibilidadPaquetes(compraRequest.getCodigosPaquetes())) {
                log.error("❌ VALIDACIÓN FALLIDA: Algunos paquetes no están disponibles");
                return crearRespuestaError("❌ No se puede procesar la compra: Algunos paquetes no están disponibles");
            }
            log.info("✅ Validación de disponibilidad exitosa");

            // 2. Calcular total
            Double total = paqueteService.calcularTotalCompra(compraRequest.getCodigosPaquetes());
            log.info("💰 Total calculado: ${}", total);
            
            // 3. Crear entidad Compra
            Compra compra = new Compra();
            compra.setIdCliente(compraRequest.getIdCliente());
            compra.setNombreCliente(compraRequest.getNombreCliente());
            compra.setEmailCliente(compraRequest.getEmailCliente());
            compra.setTelefonoCliente(compraRequest.getTelefonoCliente());
            compra.setCodigosPaquetes(compraRequest.getCodigosPaquetes());
            compra.setTotal(total);
            compra.setEstado(Compra.EstadoCompra.VALIDANDO);
            compra.setMensaje("🔄 Compra en proceso de validación con OTN");

            // 4. Guardar compra inicial
            compra = compraRepository.save(compra);
            log.info("💾 Compra guardada temporalmente - ID: {}, Código: {}", compra.getId(), compra.getCodigoCompra());

            // 5. Validar con OTN
            log.info("🌐 Enviando validación a OTN...");
            ValidacionPaqueteRequest otnRequest = new ValidacionPaqueteRequest();
            otnRequest.setCodigosPaquetes(compraRequest.getCodigosPaquetes());
            otnRequest.setIdSolicitud(compraRequest.getIdSolicitud());

            ValidacionPaqueteResponse otnResponse = otnService.validarMultiplesPaquetes(otnRequest);

            // 6. Actualizar estado basado en respuesta OTN
            String mensajeFinal;
            if (otnResponse.isPuedeContinuar()) {
                compra.setEstado(Compra.EstadoCompra.APROBADA);
                mensajeFinal = "🎉 ¡COMPRA EXITOSA! " + otnResponse.getMensaje();
                compra.setMensaje(mensajeFinal);
                log.info("✅ COMPRA APROBADA - Código: {}", compra.getCodigoCompra());
                log.info("✨ Todos los paquetes han sido validados exitosamente por OTN");
            } else {
                compra.setEstado(Compra.EstadoCompra.RECHAZADA);
                mensajeFinal = "😞 COMPRA RECHAZADA - " + otnResponse.getMensaje();
                compra.setMensaje(mensajeFinal);
                log.warn("❌ COMPRA RECHAZADA - Código: {}", compra.getCodigoCompra());
                if (!otnResponse.getPaquetesRechazados().isEmpty()) {
                    log.warn("📦 Paquetes rechazados: {}", otnResponse.getPaquetesRechazados());
                }
                if (!otnResponse.getPaquetesEnProceso().isEmpty()) {
                    log.warn("⏳ Paquetes en proceso: {}", otnResponse.getPaquetesEnProceso());
                }
            }

            // 7. Guardar compra final
            compra = compraRepository.save(compra);

            // 8. Convertir a DTO de respuesta
            CompraResponseDTO response = convertirADTO(compra);
            response.setExitosa(compra.getEstado() == Compra.EstadoCompra.APROBADA);
            response.setDetalleResultado(mensajeFinal);

            // 9. Log final según el resultado
            if (response.isExitosa()) {
                log.info("🎊 PROCESAMIENTO COMPLETADO - COMPRA EXITOSA");
                log.info("📋 Resumen compra:");
                log.info("   👤 Cliente: {}", compra.getNombreCliente());
                log.info("   📧 Email: {}", compra.getEmailCliente());
                log.info("   📦 Paquetes: {}", compra.getCodigosPaquetes());
                log.info("   💰 Total: ${}", compra.getTotal());
                log.info("   🆔 Código compra: {}", compra.getCodigoCompra());
                log.info("   🏁 Estado: {}", compra.getEstado());
                log.info("🎯 La compra ha sido procesada y aprobada exitosamente");
            } else {
                log.info("📋 Resumen compra rechazada:");
                log.info("   👤 Cliente: {}", compra.getNombreCliente());
                log.info("   🆔 Código compra: {}", compra.getCodigoCompra());
                log.info("   🏁 Estado: {}", compra.getEstado());
                log.info("   📝 Motivo: {}", compra.getMensaje());
            }

            return response;

        } catch (Exception e) {
            log.error("💥 ERROR CRÍTICO procesando compra: {}", e.getMessage(), e);
            log.error("🔧 Detalles del error:", e);
            return crearRespuestaError("💥 Error interno del sistema: " + e.getMessage());
        }
    }

    private boolean validarDisponibilidadPaquetes(List<String> codigosPaquetes) {
        log.debug("🔎 Verificando disponibilidad de {} paquetes", codigosPaquetes.size());
        
        for (String codigo : codigosPaquetes) {
            boolean disponible = paqueteService.validarDisponibilidadPaquete(codigo);
            if (!disponible) {
                log.warn("🚫 Paquete NO disponible: {}", codigo);
                return false;
            } else {
                log.debug("✅ Paquete disponible: {}", codigo);
            }
        }
        return true;
    }

    private CompraResponseDTO convertirADTO(Compra compra) {
        CompraResponseDTO dto = new CompraResponseDTO();
        dto.setId(compra.getId());
        dto.setCodigoCompra(compra.getCodigoCompra());
        dto.setIdCliente(compra.getIdCliente());
        dto.setNombreCliente(compra.getNombreCliente());
        dto.setEmailCliente(compra.getEmailCliente());
        dto.setTelefonoCliente(compra.getTelefonoCliente());
        dto.setCodigosPaquetes(compra.getCodigosPaquetes());
        dto.setTotal(compra.getTotal());
        dto.setEstado(compra.getEstado());
        dto.setMensaje(compra.getMensaje());
        dto.setFechaCreacion(compra.getFechaCreacion());
        dto.setFechaActualizacion(compra.getFechaActualizacion());
        return dto;
    }

    private CompraResponseDTO crearRespuestaError(String mensaje) {
        CompraResponseDTO response = new CompraResponseDTO();
        response.setExitosa(false);
        response.setDetalleResultado(mensaje);
        response.setEstado(Compra.EstadoCompra.RECHAZADA);
        
        log.error("🚨 Respuesta de error creada: {}", mensaje);
        return response;
    }

    @Override
    public CompraResponseDTO obtenerCompraPorId(Long id) {
        log.info("🔍 Buscando compra por ID: {}", id);
        return compraRepository.findById(id)
                .map(compra -> {
                    log.info("✅ Compra encontrada - ID: {}, Estado: {}", id, compra.getEstado());
                    return convertirADTO(compra);
                })
                .orElseGet(() -> {
                    log.warn("⚠️ Compra no encontrada - ID: {}", id);
                    return null;
                });
    }

    @Override
    public CompraResponseDTO obtenerCompraPorCodigo(String codigoCompra) {
        log.info("🔍 Buscando compra por código: {}", codigoCompra);
        return compraRepository.findByCodigoCompra(codigoCompra)
                .map(compra -> {
                    log.info("✅ Compra encontrada - Código: {}, Estado: {}", codigoCompra, compra.getEstado());
                    return convertirADTO(compra);
                })
                .orElseGet(() -> {
                    log.warn("⚠️ Compra no encontrada - Código: {}", codigoCompra);
                    return null;
                });
    }

    @Override
    public List<CompraResponseDTO> obtenerComprasPorCliente(String idCliente) {
        log.info("👤 Buscando compras del cliente: {}", idCliente);
        List<CompraResponseDTO> compras = compraRepository.findByIdCliente(idCliente)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        
        log.info("📊 Compras encontradas: {} para cliente: {}", compras.size(), idCliente);
        return compras;
    }

    @Override
    public List<CompraResponseDTO> obtenerTodasLasCompras() {
        log.info("📋 Obteniendo todas las compras del sistema");
        List<CompraResponseDTO> compras = compraRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        
        log.info("🏢 Total de compras en sistema: {}", compras.size());
        return compras;
    }

    @Override
    @Transactional
    public CompraResponseDTO actualizarEstadoCompra(Long id, String nuevoEstado, String mensaje) {
        log.info("🔄 Actualizando estado de compra ID: {} a {}", id, nuevoEstado);
        
        return compraRepository.findById(id)
                .map(compra -> {
                    try {
                        Compra.EstadoCompra estado = Compra.EstadoCompra.valueOf(nuevoEstado.toUpperCase());
                        String estadoAnterior = compra.getEstado().toString();
                        compra.setEstado(estado);
                        compra.setMensaje(mensaje != null ? mensaje : "Estado actualizado a: " + nuevoEstado);
                        compra = compraRepository.save(compra);
                        
                        log.info("✅ Estado actualizado - Compra ID: {} - De {} a {}", 
                                id, estadoAnterior, nuevoEstado);
                        log.info("📝 Mensaje actualizado: {}", compra.getMensaje());
                        
                        return convertirADTO(compra);
                    } catch (IllegalArgumentException e) {
                        log.error("🚨 Estado de compra inválido: {}", nuevoEstado);
                        return crearRespuestaError("Estado de compra inválido: " + nuevoEstado);
                    }
                })
                .orElseGet(() -> {
                    log.error("🚨 Compra no encontrada para actualizar - ID: {}", id);
                    return crearRespuestaError("Compra no encontrada con ID: " + id);
                });
    }
}