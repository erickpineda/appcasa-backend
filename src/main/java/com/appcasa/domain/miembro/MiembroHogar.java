package com.appcasa.domain.miembro;

import com.appcasa.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "TB_MIEMBRO_HOGAR")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MiembroHogar extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "id_hogar", nullable = false)
  private UUID idHogar;

  @Column(name = "id_tipo_miembro", nullable = false)
  private Integer idTipoMiembro;

  @Column(nullable = false, length = 100)
  private String nombre;

  @Column(name = "fecha_nacimiento")
  private LocalDate fechaNacimiento;

  @Column(name = "avatar_url", length = 500)
  private String avatarUrl;

  @Column(columnDefinition = "TEXT")
  private String notas;

  // Campos específicos de mascota
  @Column(length = 100)
  private String raza;

  @Column(length = 100)
  private String color;

  @Column(name = "peso_kg", precision = 5, scale = 2)
  private BigDecimal pesoKg;

  @Column(length = 50)
  private String microchip;

  @Column(name = "chip_num", length = 50)
  private String chipNum;

  private Boolean esterilizado;

  // Vínculo a usuario si es persona con cuenta
  @Column(name = "id_usuario")
  private UUID idUsuario;

  @Column(name = "id_estado", nullable = false)
  @Builder.Default
  private Integer idEstado = 1;
}
