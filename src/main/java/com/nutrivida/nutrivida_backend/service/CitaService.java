package com.nutrivida.nutrivida_backend.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nutrivida.nutrivida_backend.dto.CambiarEstadoCitaRequest;
import com.nutrivida.nutrivida_backend.dto.CitaRequest;
import com.nutrivida.nutrivida_backend.dto.CitaResponse;
import com.nutrivida.nutrivida_backend.dto.ReprogramarCitaRequest;
import com.nutrivida.nutrivida_backend.exception.ApiException;
import com.nutrivida.nutrivida_backend.model.Cita;
import com.nutrivida.nutrivida_backend.model.Nutricionista;
import com.nutrivida.nutrivida_backend.model.Rol;
import com.nutrivida.nutrivida_backend.model.Usuario;
import com.nutrivida.nutrivida_backend.repository.CitaRepository;
import com.nutrivida.nutrivida_backend.repository.NutricionistaRepository;
import com.nutrivida.nutrivida_backend.validation.Rut;

// Reglas de las solicitudes de cita:
// - Cualquier usuario con sesión puede pedir una hora; queda como dueño de la solicitud.
// - El PACIENTE ve, cancela y reprograma solo sus solicitudes (403 si intenta con otra).
// - El ADMIN y el NUTRICIONISTA ven todas; el ADMIN cambia el estado.
// - Un nutricionista no puede tener dos citas activas en la misma fecha y hora (409).
// - La hora debe estar dentro de la jornada del nutricionista, en bloques de 30 minutos (400).
@Service
public class CitaService {

    private static final Pattern RANGO_HORARIO = Pattern.compile("(\\d{2}:\\d{2}).*?(\\d{2}:\\d{2})");

    private final CitaRepository citas;
    private final NutricionistaRepository nutricionistas;
    private final CatalogoService catalogo;

    public CitaService(CitaRepository citas, NutricionistaRepository nutricionistas, CatalogoService catalogo) {
        this.citas = citas;
        this.nutricionistas = nutricionistas;
        this.catalogo = catalogo;
    }

    @Transactional
    public CitaResponse crear(CitaRequest datos, Usuario actual) {
        Nutricionista nutricionista = nutricionistas.findByCodigo(datos.nutricionistaCodigo().trim())
                .orElseThrow(() -> ApiException.solicitudInvalida("No existe el nutricionista " + datos.nutricionistaCodigo() + "."));
        LocalDate fecha = leerFecha(datos.fecha());
        LocalTime hora = leerHora(datos.hora());
        validarHorario(nutricionista, fecha, hora, 0);

        Cita cita = new Cita();
        cita.setPaciente(actual);
        cita.setServicio(catalogo.buscarServicioActivo(datos.servicioId()));
        cita.setNutricionista(nutricionista);
        cita.setNombre(datos.nombre().trim());
        cita.setRut(Rut.normalizar(datos.rut()));
        cita.setEmail(datos.email().trim().toLowerCase());
        cita.setTelefono(datos.telefono() == null || datos.telefono().isBlank() ? null : datos.telefono().trim());
        cita.setFecha(fecha);
        cita.setHora(hora);
        cita.setMotivo(datos.motivo() == null || datos.motivo().isBlank() ? null : datos.motivo().trim());
        return CitaResponse.de(citas.save(cita));
    }

    @Transactional(readOnly = true)
    public List<CitaResponse> listar(Usuario actual) {
        List<Cita> lista = veTodas(actual)
                ? citas.findAllByOrderByFechaAscHoraAsc()
                : citas.findByPacienteIdOrderByFechaAscHoraAsc(actual.getId());
        return lista.stream().map(CitaResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public CitaResponse ver(Integer id, Usuario actual) {
        Cita cita = buscar(id);
        if (!veTodas(actual)) {
            exigirDueno(cita, actual, "No puedes ver la solicitud de otra persona.");
        }
        return CitaResponse.de(cita);
    }

    // Solo ADMIN (lo exige el controlador). Si una cita cancelada vuelve a quedar activa, se revisa el horario.
    @Transactional
    public CitaResponse cambiarEstado(Integer id, CambiarEstadoCitaRequest datos) {
        Cita cita = buscar(id);
        String nuevo = datos.estado();
        boolean seReactiva = !cita.estaActiva()
                && !Cita.CANCELADA.equals(nuevo) && !Cita.RECHAZADA.equals(nuevo);
        if (seReactiva && citas.horarioOcupado(cita.getNutricionista().getId(), cita.getFecha(), cita.getHora(), cita.getId())) {
            throw ApiException.conflicto("Ese horario ya fue tomado por otra cita: reprográmala en vez de reactivarla.");
        }
        cita.setEstado(nuevo);
        return CitaResponse.de(cita);
    }

    @Transactional
    public CitaResponse cancelar(Integer id, Usuario actual) {
        Cita cita = buscarParaModificar(id, actual, "No puedes cancelar la solicitud de otra persona.");
        cita.setEstado(Cita.CANCELADA);
        return CitaResponse.de(cita);
    }

    @Transactional
    public CitaResponse reprogramar(Integer id, ReprogramarCitaRequest datos, Usuario actual) {
        Cita cita = buscarParaModificar(id, actual, "No puedes reprogramar la solicitud de otra persona.");
        LocalDate fecha = leerFecha(datos.fecha());
        LocalTime hora = leerHora(datos.hora());
        validarHorario(cita.getNutricionista(), fecha, hora, cita.getId());
        cita.setFecha(fecha);
        cita.setHora(hora);
        cita.setEstado(Cita.REPROGRAMADA);
        return CitaResponse.de(cita);
    }

    // ---------- Apoyo ----------

    private Cita buscarParaModificar(Integer id, Usuario actual, String mensajeAjena) {
        Cita cita = buscar(id);
        if (!esAdmin(actual)) {
            exigirDueno(cita, actual, mensajeAjena);
        }
        if (!cita.estaActiva()) {
            throw ApiException.conflicto("La solicitud ya está " + cita.getEstado().toLowerCase() + ".");
        }
        return cita;
    }

    private void validarHorario(Nutricionista nutricionista, LocalDate fecha, LocalTime hora, Integer idExcluir) {
        if (fecha.isBefore(LocalDate.now())) {
            throw ApiException.solicitudInvalida("La fecha debe ser hoy o una fecha futura.");
        }
        if (hora.getMinute() % 30 != 0) {
            throw ApiException.solicitudInvalida("Las horas se toman en bloques de 30 minutos (ej: 10:00 o 10:30).");
        }
        Matcher rango = RANGO_HORARIO.matcher(nutricionista.getHorario() == null ? "" : nutricionista.getHorario());
        if (rango.find()) {
            LocalTime inicio = LocalTime.parse(rango.group(1));
            LocalTime fin = LocalTime.parse(rango.group(2));
            if (hora.isBefore(inicio) || !hora.isBefore(fin)) {
                throw ApiException.solicitudInvalida("La hora está fuera de la jornada de " + nutricionista.getNombre()
                        + " (" + nutricionista.getHorario() + ").");
            }
        }
        if (citas.horarioOcupado(nutricionista.getId(), fecha, hora, idExcluir)) {
            throw ApiException.conflicto("Ese horario ya está tomado con " + nutricionista.getNombre()
                    + ". Elige otra hora.");
        }
    }

    private static LocalDate leerFecha(String texto) {
        try {
            return LocalDate.parse(texto);
        } catch (DateTimeParseException e) {
            throw ApiException.solicitudInvalida("La fecha " + texto + " no existe.");
        }
    }

    private static LocalTime leerHora(String texto) {
        try {
            return LocalTime.parse(texto);
        } catch (DateTimeParseException e) {
            throw ApiException.solicitudInvalida("La hora " + texto + " no es válida.");
        }
    }

    private static void exigirDueno(Cita cita, Usuario actual, String mensaje) {
        if (!cita.getPaciente().getId().equals(actual.getId())) {
            throw ApiException.prohibido(mensaje);
        }
    }

    private static boolean esAdmin(Usuario u) {
        return Rol.ADMIN.equals(u.getRol().getNombre());
    }

    private static boolean veTodas(Usuario u) {
        return esAdmin(u) || Rol.NUTRICIONISTA.equals(u.getRol().getNombre());
    }

    private Cita buscar(Integer id) {
        return citas.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("No existe la solicitud " + id + "."));
    }
}
