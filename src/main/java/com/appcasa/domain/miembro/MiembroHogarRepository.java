package com.appcasa.domain.miembro;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MiembroHogarRepository extends JpaRepository<MiembroHogar, UUID> {

  List<MiembroHogar> findByIdHogarAndIdEstado(UUID idHogar, Integer idEstado);

  List<MiembroHogar> findByIdHogarAndIdTipoMiembro(UUID idHogar, Integer idTipoMiembro);

  Optional<MiembroHogar> findByIdHogarAndIdUsuario(UUID idHogar, UUID idUsuario);
}
