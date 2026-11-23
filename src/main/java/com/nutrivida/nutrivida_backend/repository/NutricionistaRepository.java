package com.nutrivida.nutrivida_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nutrivida.nutrivida_backend.model.Nutricionista;

public interface NutricionistaRepository extends JpaRepository<Nutricionista, Integer> {

    Optional<Nutricionista> findByCodigo(String codigo);
}
