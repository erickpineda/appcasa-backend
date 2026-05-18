package com.appcasa.domain.hogar;

import com.appcasa.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "TB_HOGAR")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hogar extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, length = 100)
  private String nombre;

  @Column(columnDefinition = "TEXT")
  private String descripcion;

  @Column(nullable = false, unique = true, length = 10)
  private String codigo;

  @Column(name = "id_estado", nullable = false)
  private Integer idEstado;
}
