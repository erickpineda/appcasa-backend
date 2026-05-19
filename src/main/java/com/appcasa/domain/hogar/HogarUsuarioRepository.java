package com.appcasa.domain.hogar;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HogarUsuarioRepository extends JpaRepository<HogarUsuario, UUID> {

  List<HogarUsuario> findByIdUsuarioAndIdEstado(UUID idUsuario, Integer idEstado);

  Optional<HogarUsuario> findByIdHogarAndIdUsuario(UUID idHogar, UUID idUsuario);
}
