package com.nutrivida.nutrivida_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Tabla "nutricionista": profesionales que atienden las citas, con sus días y su jornada.
@Entity
@Table(name = "nutricionista")
public class Nutricionista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 10)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 150)
    private String especialidad;

    @Column(length = 60)
    private String dias;

    @Column(length = 30)
    private String horario;

    public Integer getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getEspecialidad() { return especialidad; }
    public String getDias() { return dias; }
    public String getHorario() { return horario; }
}
