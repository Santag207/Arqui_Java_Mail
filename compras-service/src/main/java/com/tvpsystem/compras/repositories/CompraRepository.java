package com.tvpsystem.compras.repositories;

import com.tvpsystem.compras.entities.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {
    Optional<Compra> findByCodigoCompra(String codigoCompra);
    List<Compra> findByIdCliente(String idCliente);
    List<Compra> findByEstado(Compra.EstadoCompra estado);
    
    @Query("SELECT c FROM Compra c WHERE c.emailCliente = :email ORDER BY c.fechaCreacion DESC")
    List<Compra> findByEmailCliente(String email);
    
    @Query("SELECT COUNT(c) FROM Compra c WHERE c.estado = :estado")
    Long countByEstado(Compra.EstadoCompra estado);
}