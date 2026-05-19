package com.appcasa.infrastructure.api.dto.hogar;

import jakarta.validation.constraints.NotBlank;

public record HogarCreateRequest(
  @NotBlank
  String nombre,
  String descripcion
) {}
