package com.nutrivida.nutrivida_backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nutrivida.nutrivida_backend.dto.CategoriaRequest;
import com.nutrivida.nutrivida_backend.dto.CategoriaResponse;
import com.nutrivida.nutrivida_backend.dto.NutricionistaResponse;
import com.nutrivida.nutrivida_backend.dto.ServicioRequest;
import com.nutrivida.nutrivida_backend.dto.ServicioResponse;
import com.nutrivida.nutrivida_backend.exception.ApiException;
import com.nutrivida.nutrivida_backend.model.Categoria;
import com.nutrivida.nutrivida_backend.model.Servicio;
import com.nutrivida.nutrivida_backend.repository.CategoriaRepository;
import com.nutrivida.nutrivida_backend.repository.NutricionistaRepository;
import com.nutrivida.nutrivida_backend.repository.ServicioRepository;

// Reglas del catálogo: categorías, servicios y nutricionistas.
// La lectura es pública (la usan las páginas del sitio); crear, modificar y eliminar es solo del ADMIN
// (lo controla @PreAuthorize en el controlador).
@Service
public class CatalogoService {

    private final CategoriaRepository categorias;
    private final ServicioRepository servicios;
    private final NutricionistaRepository nutricionistas;

    public CatalogoService(CategoriaRepository categorias, ServicioRepository servicios,
            NutricionistaRepository nutricionistas) {
        this.categorias = categorias;
        this.servicios = servicios;
        this.nutricionistas = nutricionistas;
    }

    // ---------- Categorías ----------

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarCategorias() {
        return categorias.findAll().stream()
                .sorted((a, b) -> a.getId().compareTo(b.getId()))
                .map(c -> CategoriaResponse.de(c, servicios.countByCategoriaIdAndActivoTrue(c.getId())))
                .toList();
    }

    @Transactional
    public CategoriaResponse crearCategoria(CategoriaRequest datos) {
        String nombre = datos.nombre().trim();
        if (categorias.existsByNombreIgnoreCase(nombre)) {
            throw ApiException.conflicto("Ya existe una categoría con ese nombre.");
        }
        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        return CategoriaResponse.de(categorias.save(categoria), 0);
    }

    // Renombrar: como los servicios apuntan a la categoría por su id, todos quedan actualizados.
    @Transactional
    public CategoriaResponse renombrarCategoria(Integer id, CategoriaRequest datos) {
        Categoria categoria = buscarCategoria(id);
        String nombre = datos.nombre().trim();
        if (categorias.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw ApiException.conflicto("Ya existe una categoría con ese nombre.");
        }
        categoria.setNombre(nombre);
        return CategoriaResponse.de(categoria, servicios.countByCategoriaIdAndActivoTrue(id));
    }

    // Solo se puede eliminar una categoría que nunca tuvo servicios (409 si tiene).
    @Transactional
    public void eliminarCategoria(Integer id) {
        Categoria categoria = buscarCategoria(id);
        if (servicios.existsByCategoriaId(id)) {
            throw ApiException.conflicto("La categoría tiene servicios asociados: muévelos o elimínalos primero.");
        }
        categorias.delete(categoria);
    }

    // ---------- Servicios ----------

    @Transactional(readOnly = true)
    public List<ServicioResponse> listarServicios(String categoria) {
        return servicios.findByActivoTrueOrderByIdAsc().stream()
                .filter(s -> categoria == null || categoria.isBlank()
                        || s.getCategoria().getNombre().equalsIgnoreCase(categoria.trim()))
                .map(ServicioResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public ServicioResponse verServicio(Integer id) {
        return ServicioResponse.de(buscarServicioActivo(id));
    }

    @Transactional
    public ServicioResponse crearServicio(ServicioRequest datos) {
        Servicio servicio = new Servicio();
        aplicar(servicio, datos);
        // Código provisorio único; después de guardar se reemplaza por "SV" + id (ej: SV019)
        servicio.setCodigo("T" + UUID.randomUUID().toString().substring(0, 8));
        servicios.saveAndFlush(servicio);
        servicio.setCodigo(String.format("SV%03d", servicio.getId()));
        return ServicioResponse.de(servicio);
    }

    @Transactional
    public ServicioResponse actualizarServicio(Integer id, ServicioRequest datos) {
        Servicio servicio = buscarServicioActivo(id);
        aplicar(servicio, datos);
        return ServicioResponse.de(servicio);
    }

    // Borrado lógico: el servicio deja de aparecer en el catálogo, pero las citas antiguas lo conservan.
    @Transactional
    public void eliminarServicio(Integer id) {
        buscarServicioActivo(id).setActivo(false);
    }

    // ---------- Nutricionistas ----------

    @Transactional(readOnly = true)
    public List<NutricionistaResponse> listarNutricionistas() {
        return nutricionistas.findAll().stream()
                .sorted((a, b) -> a.getId().compareTo(b.getId()))
                .map(NutricionistaResponse::de)
                .toList();
    }

    // ---------- Apoyo ----------

    private void aplicar(Servicio servicio, ServicioRequest datos) {
        Categoria categoria = categorias.findByNombreIgnoreCase(datos.categoria().trim())
                .orElseThrow(() -> ApiException.solicitudInvalida("La categoría " + datos.categoria() + " no existe."));
        servicio.setNombre(datos.nombre().trim());
        servicio.setCategoria(categoria);
        servicio.setTipo(textoOpcional(datos.tipo()));
        servicio.setDuracion(textoOpcional(datos.duracion()));
        servicio.setModalidad(textoOpcional(datos.modalidad()));
        servicio.setPrecio(datos.precio());
        servicio.setDescripcion(textoOpcional(datos.descripcion()));
    }

    private static String textoOpcional(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }

    private Categoria buscarCategoria(Integer id) {
        return categorias.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("No existe la categoría " + id + "."));
    }

    public Servicio buscarServicioActivo(Integer id) {
        return servicios.findById(id)
                .filter(Servicio::isActivo)
                .orElseThrow(() -> ApiException.noEncontrado("No existe el servicio " + id + "."));
    }
}
