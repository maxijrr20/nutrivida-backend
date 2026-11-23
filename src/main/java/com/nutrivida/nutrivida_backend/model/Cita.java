package com.nutrivida.nutrivida_backend.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

// Tabla "cita": solicitud de hora de un paciente (usuario) para un servicio con un nutricionista.
@Entity
@Table(name = "cita")
public class Cita {

    // Estados posibles de una solicitud (los mismos que muestra el frontend)
    public static final String PENDIENTE = "Pendiente";
    public static final String CONFIRMADA = "Confirmada";
    public static final String REPROGRAMADA = "Reprogramada";
    public static final String CANCELADA = "Cancelada";
    public static final String RECHAZADA = "Rechazada";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Cuenta que hizo la solicitud: define quién puede verla y cancelarla
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Usuario paciente;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "servicio_id", nullable = false)
    private Servicio servicio;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "nutricionista_id", nullable = false)
    private Nutricionista nutricionista;

    // Datos de contacto escritos en el formulario
    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 12)
    private String rut;

    @Column(nullable = false, length = 120)
    private String email;

    @Column(length = 20)
    private String telefono;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime hora;

    @Column(length = 500)
    private String motivo;

    @Column(nullable = false, length = 20)
    private String estado = PENDIENTE;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @PrePersist
    void alCrear() {
        creadoEn = LocalDateTime.now();
    }

    // Una cita cancelada o rechazada libera el horario
    public boolean estaActiva() {
        return !CANCELADA.equals(estado) && !RECHAZADA.equals(estado);
    }

    public Integer getId() { return id; }
    public Usuario getPaciente() { return paciente; }
    public void setPaciente(Usuario paciente) { this.paciente = paciente; }
    public Servicio getServicio() { return servicio; }
    public void setServicio(Servicio servicio) { this.servicio = servicio; }
    public Nutricionista getNutricionista() { return nutricionista; }
    public void setNutricionista(Nutricionista nutricionista) { this.nutricionista = nutricionista; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getRut() { return rut; }
    public void setRut(String rut) { this.rut = rut; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
}
