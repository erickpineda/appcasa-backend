package com.appcasa.domain.usuario;

import com.appcasa.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "TB_USUARIO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, length = 100)
  private String nombre;

  @Column(length = 150)
  private String apellidos;

  @Column(nullable = false, unique = true, length = 200)
  private String email;

  @Column(name = "password_hash", nullable = false, length = 255)
  private String passwordHash;

  @Column(name = "avatar_url", length = 500)
  private String avatarUrl;

  @Column(nullable = false, length = 10)
  @Builder.Default
  private String tema = "CLARO";

  @Column(nullable = false, length = 10)
  @Builder.Default
  private String locale = "es-ES";

  @Column(name = "id_estado", nullable = false)
  @Builder.Default
  private Integer idEstado = 1;

  @Column(name = "ultimo_acceso")
  private Instant ultimoAcceso;
}
