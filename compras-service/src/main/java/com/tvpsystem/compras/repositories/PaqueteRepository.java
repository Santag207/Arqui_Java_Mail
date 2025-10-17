package com.tvpsystem.compras.repositories;

import com.tvpsystem.compras.entities.PaqueteTuristico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaqueteRepository extends JpaRepository<PaqueteTuristico, String> {
    List<PaqueteTuristico> findByDestino(String destino);
    List<PaqueteTuristico> findByEstado(PaqueteTuristico.EstadoPaquete estado);
    
    @Query("SELECT p FROM PaqueteTuristico p WHERE p.precio BETWEEN :precioMin AND :precioMax")
    List<PaqueteTuristico> findByPrecioBetween(Double precioMin, Double precioMax);
    
    @Query("SELECT p FROM PaqueteTuristico p WHERE p.cuposDisponibles > 0")
    List<PaqueteTuristico> findPaquetesDisponibles();
}