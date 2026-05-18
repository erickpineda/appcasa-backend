package com.appcasa.domain.recordatorio;

import com.appcasa.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "TB_RECORDATORIO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recordatorio extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "id_hogar", nullable = false)
  private UUID idHogar;

  @Column(nullable = false, length = 200)
  private String titulo;

  @Column(columnDefinition = "TEXT")
  private String descripcion;

  @Column(name = "id_tipo_recordatorio", nullable = false)
  private Integer idTipoRecordatorio;

  @Column(name = "fecha_hora", nullable = false)
  private Instant fechaHora;

  @Column(name = "regla_recurrencia", columnDefinition = "TEXT")
  private String reglaRecurrencia;

  @Column(name = "anticipacion_minutos", nullable = false)
  @Builder.Default
  private Integer anticipacionMinutos = 30;

  @Column(nullable = false)
  @Builder.Default
  private Boolean activo = true;

  // Referencias opcionales
  @Column(name = "id_tarea")
  private UUID idTarea;

  @Column(name = "id_miembro")
  private UUID idMiembro;

  @Column(name = "id_evento")
  private UUID idEvento;

  @Column(name = "id_estado", nullable = false)
  @Builder.Default
  private Integer idEstado = 1;
}
