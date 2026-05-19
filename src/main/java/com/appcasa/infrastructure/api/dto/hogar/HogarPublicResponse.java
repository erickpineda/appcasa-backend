package com.appcasa.infrastructure.api.dto.hogar;

import java.util.UUID;

public record HogarPublicResponse(
  UUID id,
  String nombre,
  String descripcion,
  String codigo
) {}
