package com.nutrivida.nutrivida_backend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nutrivida.nutrivida_backend.dto.RegistroRequest;
import com.nutrivida.nutrivida_backend.dto.UsuarioResponse;
import com.nutrivida.nutrivida_backend.exception.ApiException;
import com.nutrivida.nutrivida_backend.model.Rol;
import com.nutrivida.nutrivida_backend.model.Usuario;
import com.nutrivida.nutrivida_backend.repository.RolRepository;
import com.nutrivida.nutrivida_backend.repository.UsuarioRepository;
import com.nutrivida.nutrivida_backend.validation.Rut;

// Reglas de negocio de la autenticación
@Service
public class AuthService {

    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarios, RolRepository roles, PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.passwordEncoder = passwordEncoder;
    }

    // Registro de pacientes: el correo y el RUT no se pueden repetir (409)
    // y el rol lo asigna el servidor (PACIENTE).
    @Transactional
    public UsuarioResponse registrar(RegistroRequest datos) {
        String email = datos.email().trim().toLowerCase();
        String rut = Rut.normalizar(datos.rut());

        if (usuarios.existsByEmail(email)) {
            throw ApiException.conflicto("Ya existe una cuenta con ese correo.");
        }
        if (usuarios.existsByRut(rut)) {
            throw ApiException.conflicto("Ya existe una cuenta con ese RUT.");
        }

        Usuario usuario = new Usuario();
        usuario.setRut(rut);
        usuario.setNombre(datos.nombre().trim());
        usuario.setEmail(email);
        usuario.setTelefono(datos.telefono() == null || datos.telefono().isBlank() ? null : datos.telefono().trim());
        usuario.setPasswordHash(passwordEncoder.encode(datos.password()));
        usuario.setRol(buscarRol(Rol.PACIENTE));
        return UsuarioResponse.de(usuarios.save(usuario));
    }

    private Rol buscarRol(String nombre) {
        return roles.findByNombre(nombre)
                .orElseThrow(() -> new IllegalStateException("Falta el rol " + nombre + " en la base de datos."));
    }
}
