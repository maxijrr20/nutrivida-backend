package com.nutrivida.nutrivida_backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.nutrivida.nutrivida_backend.config.OpenApiConfig;
import com.nutrivida.nutrivida_backend.dto.CategoriaRequest;
import com.nutrivida.nutrivida_backend.dto.CategoriaResponse;
import com.nutrivida.nutrivida_backend.dto.NutricionistaResponse;
import com.nutrivida.nutrivida_backend.dto.ServicioRequest;
import com.nutrivida.nutrivida_backend.dto.ServicioResponse;
import com.nutrivida.nutrivida_backend.service.CatalogoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

// Catálogo: categorías, servicios y nutricionistas.
// GET es público (lo usan las páginas del sitio sin iniciar sesión).
// POST, PUT y DELETE exigen JWT (401) y rol ADMIN (403).
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Catálogo", description = "Categorías, servicios y nutricionistas")
public class CatalogoController {

    private final CatalogoService catalogo;

    public CatalogoController(CatalogoService catalogo) {
        this.catalogo = catalogo;
    }

    // ---------- Categorías ----------

    @GetMapping("/categorias")
    @Operation(summary = "Listar categorías", description = "Público. Incluye cuántos servicios activos tiene cada una.")
    public List<CategoriaResponse> listarCategorias() {
        return catalogo.listarCategorias();
    }

    @PostMapping("/categorias")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = OpenApiConfig.BEARER)
    @Operation(summary = "Crear categoría", description = "Solo ADMIN.")
    @ApiResponse(responseCode = "201", description = "Categoría creada")
    @ApiResponse(responseCode = "400", description = "Nombre vacío o muy largo")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @ApiResponse(responseCode = "403", description = "El rol no es ADMIN")
    @ApiResponse(responseCode = "409", description = "Ya existe una categoría con ese nombre")
    public CategoriaResponse crearCategoria(@Valid @RequestBody CategoriaRequest datos) {
        return catalogo.crearCategoria(datos);
    }

    @PutMapping("/categorias/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = OpenApiConfig.BEARER)
    @Operation(summary = "Renombrar categoría", description = "Solo ADMIN. Los servicios de la categoría quedan con el nombre nuevo.")
    @ApiResponse(responseCode = "200", description = "Categoría renombrada")
    @ApiResponse(responseCode = "403", description = "El rol no es ADMIN")
    @ApiResponse(responseCode = "404", description = "No existe la categoría")
    @ApiResponse(responseCode = "409", description = "Ya existe una categoría con ese nombre")
    public CategoriaResponse renombrarCategoria(@PathVariable Integer id, @Valid @RequestBody CategoriaRequest datos) {
        return catalogo.renombrarCategoria(id, datos);
    }

    @DeleteMapping("/categorias/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = OpenApiConfig.BEARER)
    @Operation(summary = "Eliminar categoría", description = "Solo ADMIN y solo si no tiene servicios.")
    @ApiResponse(responseCode = "204", description = "Categoría eliminada")
    @ApiResponse(responseCode = "403", description = "El rol no es ADMIN")
    @ApiResponse(responseCode = "404", description = "No existe la categoría")
    @ApiResponse(responseCode = "409", description = "La categoría tiene servicios")
    public void eliminarCategoria(@PathVariable Integer id) {
        catalogo.eliminarCategoria(id);
    }

    // ---------- Servicios ----------

    @GetMapping("/servicios")
    @Operation(summary = "Listar servicios", description = "Público. Filtro opcional: ?categoria=Consultas")
    public List<ServicioResponse> listarServicios(@RequestParam(required = false) String categoria) {
        return catalogo.listarServicios(categoria);
    }

    @GetMapping("/servicios/{id}")
    @Operation(summary = "Ver un servicio", description = "Público.")
    @ApiResponse(responseCode = "200", description = "Datos del servicio")
    @ApiResponse(responseCode = "404", description = "No existe el servicio")
    public ServicioResponse verServicio(@PathVariable Integer id) {
        return catalogo.verServicio(id);
    }

    @PostMapping("/servicios")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = OpenApiConfig.BEARER)
    @Operation(summary = "Crear servicio", description = "Solo ADMIN. El código (SV0xx) lo asigna el servidor.")
    @ApiResponse(responseCode = "201", description = "Servicio creado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o categoría inexistente")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @ApiResponse(responseCode = "403", description = "El rol no es ADMIN")
    public ServicioResponse crearServicio(@Valid @RequestBody ServicioRequest datos) {
        return catalogo.crearServicio(datos);
    }

    @PutMapping("/servicios/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = OpenApiConfig.BEARER)
    @Operation(summary = "Modificar servicio", description = "Solo ADMIN.")
    @ApiResponse(responseCode = "200", description = "Servicio actualizado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o categoría inexistente")
    @ApiResponse(responseCode = "403", description = "El rol no es ADMIN")
    @ApiResponse(responseCode = "404", description = "No existe el servicio")
    public ServicioResponse actualizarServicio(@PathVariable Integer id, @Valid @RequestBody ServicioRequest datos) {
        return catalogo.actualizarServicio(id, datos);
    }

    @DeleteMapping("/servicios/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = OpenApiConfig.BEARER)
    @Operation(summary = "Eliminar servicio", description = "Solo ADMIN. Borrado lógico: deja de aparecer en el catálogo, pero las citas antiguas lo conservan.")
    @ApiResponse(responseCode = "204", description = "Servicio eliminado del catálogo")
    @ApiResponse(responseCode = "403", description = "El rol no es ADMIN")
    @ApiResponse(responseCode = "404", description = "No existe el servicio")
    public void eliminarServicio(@PathVariable Integer id) {
        catalogo.eliminarServicio(id);
    }

    // ---------- Nutricionistas ----------

    @GetMapping("/nutricionistas")
    @Operation(summary = "Listar nutricionistas", description = "Público. Incluye días y jornada de atención.")
    public List<NutricionistaResponse> listarNutricionistas() {
        return catalogo.listarNutricionistas();
    }
}
