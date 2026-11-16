package com.nutrivida.nutrivida_backend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nutrivida.nutrivida_backend.dto.LoginRequest;
import com.nutrivida.nutrivida_backend.dto.LoginResponse;
import com.nutrivida.nutrivida_backend.dto.RegistroRequest;
import com.nutrivida.nutrivida_backend.dto.UsuarioResponse;
import com.nutrivida.nutrivida_backend.exception.ApiException;
import com.nutrivida.nutrivida_backend.model.Rol;
import com.nutrivida.nutrivida_backend.model.Usuario;
import com.nutrivida.nutrivida_backend.repository.RolRepository;
import com.nutrivida.nutrivida_backend.repository.UsuarioRepository;
import com.nutrivida.nutrivida_backend.security.JwtService;
import com.nutrivida.nutrivida_backend.validation.Rut;

// Reglas de negocio de la autenticación
@Service
public class AuthService {

    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // Mensaje genérico: no revela si el correo existe o si la contraseña es la incorrecta
    private static final String CREDENCIALES_INVALIDAS = "Correo o contraseña incorrectos.";

    // Hash de relleno: si el correo no existe se compara igual contra un hash,
    // para que la respuesta tarde lo mismo y no delate qué correos están registrados.
    private final String hashDeRelleno;

    public AuthService(UsuarioRepository usuarios, RolRepository roles, PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.hashDeRelleno = passwordEncoder.encode("relleno-sin-uso");
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

    // Login: busca al usuario por correo, compara la contraseña con el hash BCrypt
    // y, si todo calza y la cuenta está activa, emite el JWT.
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest datos) {
        String email = datos.email().trim().toLowerCase();
        Usuario usuario = usuarios.findByEmail(email).orElse(null);

        if (usuario == null) {
            passwordEncoder.matches(datos.password(), hashDeRelleno);
            throw ApiException.noAutorizado(CREDENCIALES_INVALIDAS);
        }
        if (!passwordEncoder.matches(datos.password(), usuario.getPasswordHash()) || !usuario.isActivo()) {
            throw ApiException.noAutorizado(CREDENCIALES_INVALIDAS);
        }

        String token = jwtService.generarToken(usuario);
        return new LoginResponse(token, "Bearer", jwtService.segundosDeVigencia(), UsuarioResponse.de(usuario));
    }

    // Usuario dueño del token. El id sale del claim "sub" del JWT ya verificado,
    // nunca de un id que mande el cliente. Si la cuenta se desactivó, el token deja de servir (401).
    @Transactional(readOnly = true)
    public Usuario usuarioActual(Jwt jwt) {
        Integer id = Integer.valueOf(jwt.getSubject());
        return usuarios.findById(id)
                .filter(Usuario::isActivo)
                .orElseThrow(() -> ApiException.noAutorizado("La cuenta no existe o está desactivada."));
    }

    private Rol buscarRol(String nombre) {
        return roles.findByNombre(nombre)
                .orElseThrow(() -> new IllegalStateException("Falta el rol " + nombre + " en la base de datos."));
    }
}
