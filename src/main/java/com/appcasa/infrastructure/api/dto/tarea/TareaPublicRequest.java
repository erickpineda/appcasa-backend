package com.appcasa.infrastructure.api.dto.tarea;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TareaPublicRequest(
  @NotBlank
  String hogarCodigo,
  @NotBlank
  String titulo,
  String descripcion,
  String prioridadCodigo,
  String categoria,
  Instant fechaLimite,
  Boolean esPeriodica,
  String periodicidadCodigo,
  Boolean esPersonal,
  List<UUID> miembroIds,
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @Null(message = "No usar idHogar en el contrato publico")
  UUID idHogar,
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @Null(message = "No usar idPrioridad en el contrato publico")
  Integer idPrioridad,
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @Null(message = "No usar idEstado en el contrato publico")
  Integer idEstado
) {}
