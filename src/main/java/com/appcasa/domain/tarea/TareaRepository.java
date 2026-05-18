package com.appcasa.domain.tarea;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TareaRepository extends JpaRepository<Tarea, UUID> {

  Page<Tarea> findByIdHogarAndIdEstado(UUID idHogar, Integer idEstado, Pageable pageable);

  List<Tarea> findByIdHogarAndFechaLimiteBetween(
    UUID idHogar, LocalDate desde, LocalDate hasta
  );

  @Query("""
    SELECT t FROM Tarea t
    WHERE t.idHogar = :idHogar
      AND t.fechaCompletada IS NULL
      AND t.idEstado = 1
    ORDER BY t.fechaLimite ASC NULLS LAST
  """)
  List<Tarea> findPendientesByHogar(@Param("idHogar") UUID idHogar);
}
