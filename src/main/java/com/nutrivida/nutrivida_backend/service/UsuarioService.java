package com.nutrivida.nutrivida_backend.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nutrivida.nutrivida_backend.dto.ActualizarUsuarioRequest;
import com.nutrivida.nutrivida_backend.dto.CambiarRolRequest;
import com.nutrivida.nutrivida_backend.dto.CrearUsuarioRequest;
import com.nutrivida.nutrivida_backend.dto.UsuarioResponse;
import com.nutrivida.nutrivida_backend.exception.ApiException;
import com.nutrivida.nutrivida_backend.model.Rol;
import com.nutrivida.nutrivida_backend.model.Usuario;
import com.nutrivida.nutrivida_backend.repository.RolRepository;
import com.nutrivida.nutrivida_backend.repository.UsuarioRepository;
import com.nutrivida.nutrivida_backend.validation.Rut;

// Reglas de negocio de usuarios: listar, ver, modificar y desactivar.
// "actual" es siempre el usuario dueño del token (lo entrega AuthService.usuarioActual).
@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarios, RolRepository roles, PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.passwordEncoder = passwordEncoder;
    }

    // CREAR (solo ADMIN): igual que el registro, pero el ADMIN elige el rol.
    @Transactional
    public UsuarioResponse crear(CrearUsuarioRequest datos) {
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
        usuario.setRol(buscarRol(datos.rol()));
        return UsuarioResponse.de(usuarios.save(usuario));
    }

    // CAMBIAR ROL (solo ADMIN). El ADMIN no puede quitarse su propio rol,
    // así el sistema nunca se queda sin administrador por error (409).
    @Transactional
    public UsuarioResponse cambiarRol(Integer id, CambiarRolRequest datos, Usuario actual) {
        Usuario usuario = buscar(id);
        if (usuario.getId().equals(actual.getId()) && !Rol.ADMIN.equals(datos.rol())) {
            throw ApiException.conflicto("No puedes quitarte el rol de administrador a ti mismo.");
        }
        usuario.setRol(buscarRol(datos.rol()));
        return UsuarioResponse.de(usuario);
    }

    // LISTAR (solo ADMIN, lo controla @PreAuthorize en el controlador)
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarios.findAll().stream()
                .sorted((a, b) -> a.getId().compareTo(b.getId()))
                .map(UsuarioResponse::de)
                .toList();
    }

    // VER UNO: el ADMIN ve cualquiera; los demás solo su propia cuenta.
    // Cambiar el id en la URL no sirve para ver datos ajenos (403).
    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Integer id, Usuario actual) {
        validarPropietarioOAdmin(id, actual);
        return UsuarioResponse.de(buscar(id));
    }

    // MODIFICAR: el ADMIN o el mismo usuario. El correo nuevo no puede ser de otra cuenta (409).
    @Transactional
    public UsuarioResponse actualizar(Integer id, ActualizarUsuarioRequest datos, Usuario actual) {
        validarPropietarioOAdmin(id, actual);
        Usuario usuario = buscar(id);
        String email = datos.email().trim().toLowerCase();
        if (usuarios.existsByEmailAndIdNot(email, id)) {
            throw ApiException.conflicto("Ya existe otra cuenta con ese correo.");
        }
        usuario.setNombre(datos.nombre().trim());
        usuario.setEmail(email);
        usuario.setTelefono(datos.telefono() == null || datos.telefono().isBlank() ? null : datos.telefono().trim());
        if (datos.password() != null && !datos.password().isBlank()) {
            usuario.setPasswordHash(passwordEncoder.encode(datos.password()));
        }
        return UsuarioResponse.de(usuario);
    }

    // ELIMINAR (solo ADMIN): desactivación lógica (activo = false).
    // No se borra la fila porque más adelante tendrá citas e historial asociados.
    @Transactional
    public void desactivar(Integer id, Usuario actual) {
        Usuario usuario = buscar(id);
        if (usuario.getId().equals(actual.getId())) {
            throw ApiException.conflicto("No puedes desactivar tu propia cuenta.");
        }
        usuario.setActivo(false);
    }

    private void validarPropietarioOAdmin(Integer id, Usuario actual) {
        boolean esAdmin = Rol.ADMIN.equals(actual.getRol().getNombre());
        if (!esAdmin && !actual.getId().equals(id)) {
            throw ApiException.prohibido("Solo puedes consultar o modificar tu propia cuenta.");
        }
    }

    private Rol buscarRol(String nombre) {
        return roles.findByNombre(nombre)
                .orElseThrow(() -> ApiException.solicitudInvalida("El rol " + nombre + " no existe."));
    }

    private Usuario buscar(Integer id) {
        return usuarios.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("No existe un usuario con id " + id + "."));
    }
}
