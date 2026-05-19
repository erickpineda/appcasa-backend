package com.appcasa.infrastructure.api.dto.miembro;

import java.time.LocalDate;
import java.util.UUID;

public record MiembroHogarPublicResponse(
  UUID id,
  UUID idHogar,
  Integer idTipoMiembro,
  String nombre,
  LocalDate fechaNacimiento,
  String avatarUrl,
  String notas,
  String raza,
  String color,
  String microchip,
  Boolean esterilizado,
  UUID idUsuario,
  Integer idEstado
) {}
