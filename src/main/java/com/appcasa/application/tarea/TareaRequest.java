// ============================================================
// TareaRequest.java
// ============================================================
package com.appcasa.application.tarea;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class TareaRequest {

  @NotNull
  private UUID idHogar;

  @NotBlank
  private String titulo;

  private String descripcion;

  private Integer idPrioridad;

  private String categoria;

  private Instant fechaLimite;

  private Boolean esPeriodica;

  private String periodicidad;

  private String reglaRecurrencia;

  private Boolean esPersonal;

  private List<UUID> idsMiembros;
}
