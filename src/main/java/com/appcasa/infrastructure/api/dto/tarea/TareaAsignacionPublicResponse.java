package com.appcasa.infrastructure.api.dto.tarea;

import java.util.UUID;

public record TareaAsignacionPublicResponse(
  UUID miembroId,
  String nombreMiembro
) {}
