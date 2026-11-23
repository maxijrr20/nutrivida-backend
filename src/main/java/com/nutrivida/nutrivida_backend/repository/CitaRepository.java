package com.nutrivida.nutrivida_backend.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nutrivida.nutrivida_backend.model.Cita;

public interface CitaRepository extends JpaRepository<Cita, Integer> {

    List<Cita> findAllByOrderByFechaAscHoraAsc();

    List<Cita> findByPacienteIdOrderByFechaAscHoraAsc(Integer pacienteId);

    // ¿Hay otra cita activa del mismo nutricionista en esa fecha y hora? (excluye la cita "idExcluir")
    @Query("""
            select count(c) > 0 from Cita c
            where c.nutricionista.id = :nutricionistaId and c.fecha = :fecha and c.hora = :hora
              and c.estado not in ('Cancelada', 'Rechazada') and c.id <> :idExcluir
            """)
    boolean horarioOcupado(@Param("nutricionistaId") Integer nutricionistaId, @Param("fecha") LocalDate fecha,
            @Param("hora") LocalTime hora, @Param("idExcluir") Integer idExcluir);
}
