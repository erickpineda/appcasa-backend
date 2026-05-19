package com.appcasa.domain.tarea;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "TB_TAREA_ASIGNACION")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TareaAsignacion {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "id_tarea", nullable = false)
  private UUID idTarea;

  @Column(name = "id_miembro", nullable = false)
  private UUID idMiembro;

  @Column(name = "aceptada")
  private Boolean aceptada;

  @Column(name = "created_at", insertable = false, updatable = false)
  private Instant createdAt;
}
