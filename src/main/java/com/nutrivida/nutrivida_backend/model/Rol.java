package com.nutrivida.nutrivida_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Tabla "rol": ADMIN, NUTRICIONISTA o PACIENTE (se cargan en la migración V1).
@Entity
@Table(name = "rol")
public class Rol {

    public static final String ADMIN = "ADMIN";
    public static final String NUTRICIONISTA = "NUTRICIONISTA";
    public static final String PACIENTE = "PACIENTE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 30)
    private String nombre;

    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
}
