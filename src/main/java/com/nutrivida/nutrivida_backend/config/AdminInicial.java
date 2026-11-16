package com.nutrivida.nutrivida_backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nutrivida.nutrivida_backend.model.Rol;
import com.nutrivida.nutrivida_backend.model.Usuario;
import com.nutrivida.nutrivida_backend.repository.RolRepository;
import com.nutrivida.nutrivida_backend.repository.UsuarioRepository;
import com.nutrivida.nutrivida_backend.validation.Rut;

// Crea el primer administrador al iniciar, solo si no existe y si se entregaron
// ADMIN_EMAIL y ADMIN_PASSWORD como variables de entorno.
// Así no queda ninguna contraseña real escrita en el repositorio.
@Component
public class AdminInicial implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInicial.class);

    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:}")
    private String email;

    @Value("${app.admin.password:}")
    private String password;

    @Value("${app.admin.rut:11.111.111-1}")
    private String rut;

    public AdminInicial(UsuarioRepository usuarios, RolRepository roles, PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (email.isBlank() || password.isBlank()) {
            log.info("ADMIN_EMAIL / ADMIN_PASSWORD no definidos: no se crea administrador inicial.");
            return;
        }
        String correo = email.trim().toLowerCase();
        if (usuarios.existsByEmail(correo)) {
            return;
        }
        Usuario admin = new Usuario();
        admin.setRut(Rut.normalizar(rut));
        admin.setNombre("Administrador NutriVida");
        admin.setEmail(correo);
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setRol(roles.findByNombre(Rol.ADMIN).orElseThrow());
        usuarios.save(admin);
        log.info("Administrador inicial creado: {}", correo);
    }
}
