package com.nutrivida.nutrivida_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nutrivida.nutrivida_backend.model.Servicio;

public interface ServicioRepository extends JpaRepository<Servicio, Integer> {

    List<Servicio> findByActivoTrueOrderByIdAsc();

    long countByCategoriaIdAndActivoTrue(Integer categoriaId);

    boolean existsByCategoriaId(Integer categoriaId);
}
