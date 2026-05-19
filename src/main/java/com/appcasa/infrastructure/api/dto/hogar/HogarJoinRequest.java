package com.appcasa.infrastructure.api.dto.hogar;

import jakarta.validation.constraints.NotBlank;

public record HogarJoinRequest(
  @NotBlank
  String codigo
) {}
