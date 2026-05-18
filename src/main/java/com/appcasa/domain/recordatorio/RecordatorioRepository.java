package com.appcasa.domain.recordatorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface RecordatorioRepository extends JpaRepository<Recordatorio, UUID> {

  List<Recordatorio> findByIdHogarAndActivoTrue(UUID idHogar);

  @Query("""
    SELECT r FROM Recordatorio r
    WHERE r.activo = true
      AND r.fechaHora BETWEEN :desde AND :hasta
    ORDER BY r.fechaHora ASC
  """)
  List<Recordatorio> findProximos(
    @Param("desde") Instant desde,
    @Param("hasta") Instant hasta
  );
}
