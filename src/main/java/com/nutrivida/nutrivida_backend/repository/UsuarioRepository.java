package com.nutrivida.nutrivida_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nutrivida.nutrivida_backend.model.Usuario;

// Acceso a datos de la tabla "usuario".
// Spring Data JPA arma las consultas SQL a partir del nombre de cada método.
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByRut(String rut);

    boolean existsByEmailAndIdNot(String email, Integer id);
}
