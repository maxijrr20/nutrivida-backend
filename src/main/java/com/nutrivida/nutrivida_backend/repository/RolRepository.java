package com.nutrivida.nutrivida_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nutrivida.nutrivida_backend.model.Rol;

// Acceso a datos de la tabla "rol"
public interface RolRepository extends JpaRepository<Rol, Integer> {

    Optional<Rol> findByNombre(String nombre);
}
