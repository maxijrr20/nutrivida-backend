-- Tablas de usuarios y roles (según el diagrama entidad-relación del equipo).
-- Flyway ejecuta este archivo una sola vez; los datos se conservan al reiniciar el backend.

CREATE TABLE rol (
    id     SERIAL PRIMARY KEY,
    nombre VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE usuario (
    id            SERIAL PRIMARY KEY,
    rut           VARCHAR(12)  NOT NULL UNIQUE,
    nombre        VARCHAR(100) NOT NULL,
    email         VARCHAR(120) NOT NULL UNIQUE,
    telefono      VARCHAR(20),
    password_hash VARCHAR(100) NOT NULL,          -- hash BCrypt, nunca la contraseña en texto plano
    rol_id        INT          NOT NULL REFERENCES rol (id) ON DELETE RESTRICT,
    activo        BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en     TIMESTAMP    NOT NULL DEFAULT now()
);

-- Roles del caso NutriVida
INSERT INTO rol (nombre) VALUES ('ADMIN'), ('NUTRICIONISTA'), ('PACIENTE');
