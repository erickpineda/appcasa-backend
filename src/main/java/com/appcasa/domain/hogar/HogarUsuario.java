package com.appcasa.domain.hogar;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "TB_HOGAR_USUARIO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HogarUsuario {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "id_hogar", nullable = false)
  private UUID idHogar;

  @Column(name = "id_usuario", nullable = false)
  private UUID idUsuario;

  @Column(name = "id_rol", nullable = false)
  private Integer idRol;

  @Column(name = "es_principal", nullable = false)
  @Builder.Default
  private Boolean esPrincipal = false;

  @Column(name = "id_estado", nullable = false)
  @Builder.Default
  private Integer idEstado = 1;

  @Column(name = "created_at", insertable = false, updatable = false)
  private Instant createdAt;
}
