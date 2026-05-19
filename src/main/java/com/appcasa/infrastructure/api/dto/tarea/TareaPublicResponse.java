package com.appcasa.infrastructure.api.dto.tarea;

import com.appcasa.infrastructure.api.dto.catalogo.CodigoLabelDto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TareaPublicResponse(
  UUID id,
  String hogarCodigo,
  String titulo,
  String descripcion,
  CodigoLabelDto prioridad,
  String categoria,
  Instant fechaLimite,
  Instant fechaCompletada,
  CodigoLabelDto periodicidad,
  boolean esPersonal,
  CodigoLabelDto estado,
  List<TareaAsignacionPublicResponse> asignaciones
) {}
