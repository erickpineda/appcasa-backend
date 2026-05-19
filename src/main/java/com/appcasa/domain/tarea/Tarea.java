package com.appcasa.domain.tarea;

import com.appcasa.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "TB_TAREA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tarea extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "id_hogar", nullable = false)
  private UUID idHogar;

  @Column(nullable = false, length = 200)
  private String titulo;

  @Column(columnDefinition = "TEXT")
  private String descripcion;

  @Column(name = "id_prioridad", nullable = false)
  @Builder.Default
  private Integer idPrioridad = 1;

  @Column(length = 100)
  private String categoria;

  @Column(name = "fecha_limite")
  private Instant fechaLimite;

  @Column(name = "fecha_completada")
  private Instant fechaCompletada;

  @Column(name = "es_periodica", nullable = false)
  @Builder.Default
  private Boolean esPeriodica = false;

  @Column(length = 30)
  private String periodicidad;

  @Column(name = "regla_recurrencia", columnDefinition = "TEXT")
  private String reglaRecurrencia;

  @Column(name = "es_personal", nullable = false)
  @Builder.Default
  private Boolean esPersonal = false;

  @Column(name = "id_creador")
  private UUID idCreador;

  @Column(name = "adjunto_url", length = 500)
  private String adjuntoUrl;

  @Column(name = "id_estado", nullable = false)
  @Builder.Default
  private Integer idEstado = 1;
}
