package com.appcasa.infrastructure.api;

import com.appcasa.domain.hogar.Hogar;
import com.appcasa.domain.hogar.HogarRepository;
import com.appcasa.domain.hogar.HogarUsuario;
import com.appcasa.domain.hogar.HogarUsuarioRepository;
import com.appcasa.domain.miembro.MiembroHogar;
import com.appcasa.domain.miembro.MiembroHogarRepository;
import com.appcasa.domain.usuario.Usuario;
import com.appcasa.infrastructure.api.dto.hogar.HogarCreateRequest;
import com.appcasa.infrastructure.api.dto.hogar.HogarJoinRequest;
import com.appcasa.infrastructure.api.dto.hogar.HogarPublicResponse;
import com.appcasa.infrastructure.exception.RecursoNoEncontradoException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hogares")
@RequiredArgsConstructor
public class HogarController {

  private static final int ESTADO_ACTIVO_ID = 1;
  private static final int ROL_ADMIN_ID = 1;
  private static final int TIPO_MIEMBRO_PERSONA_ID = 1;

  private final HogarRepository hogarRepository;
  private final HogarUsuarioRepository hogarUsuarioRepository;
  private final MiembroHogarRepository miembroHogarRepository;

  @GetMapping
  public ResponseEntity<List<HogarPublicResponse>> listar(@AuthenticationPrincipal Usuario usuario) {
    List<UUID> idsHogar = hogarUsuarioRepository.findByIdUsuarioAndIdEstado(usuario.getId(), ESTADO_ACTIVO_ID).stream()
      .map(HogarUsuario::getIdHogar)
      .distinct()
      .toList();

    List<HogarPublicResponse> body = hogarRepository.findAllById(idsHogar).stream()
      .sorted(Comparator.comparing(Hogar::getNombre, String.CASE_INSENSITIVE_ORDER))
      .map(this::toResponse)
      .toList();

    return ResponseEntity.ok(body);
  }

  @PostMapping
  @Transactional
  public ResponseEntity<HogarPublicResponse> crear(
    @Valid @RequestBody HogarCreateRequest request,
    @AuthenticationPrincipal Usuario usuario
  ) {
    Hogar hogar = hogarRepository.save(Hogar.builder()
      .nombre(request.nombre().trim())
      .descripcion(request.descripcion())
      .codigo(generarCodigo())
      .idEstado(ESTADO_ACTIVO_ID)
      .build());

    asegurarRelacionUsuario(hogar.getId(), usuario.getId(), true);
    asegurarMiembroPersona(hogar.getId(), usuario);

    return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(hogar));
  }

  @PostMapping("/unirse")
  @Transactional
  public ResponseEntity<HogarPublicResponse> unirse(
    @Valid @RequestBody HogarJoinRequest request,
    @AuthenticationPrincipal Usuario usuario
  ) {
    String codigo = request.codigo().trim().toUpperCase();
    Hogar hogar = hogarRepository.findByCodigo(codigo)
      .orElseThrow(() -> new RecursoNoEncontradoException("Hogar no encontrado para codigo " + codigo));

    asegurarRelacionUsuario(hogar.getId(), usuario.getId(), false);
    asegurarMiembroPersona(hogar.getId(), usuario);

    return ResponseEntity.ok(toResponse(hogar));
  }

  private void asegurarRelacionUsuario(UUID idHogar, UUID idUsuario, boolean principal) {
    if (hogarUsuarioRepository.findByIdHogarAndIdUsuario(idHogar, idUsuario).isPresent()) {
      return;
    }

    hogarUsuarioRepository.save(HogarUsuario.builder()
      .idHogar(idHogar)
      .idUsuario(idUsuario)
      .idRol(ROL_ADMIN_ID)
      .esPrincipal(principal)
      .idEstado(ESTADO_ACTIVO_ID)
      .build());
  }

  private void asegurarMiembroPersona(UUID idHogar, Usuario usuario) {
    if (miembroHogarRepository.findByIdHogarAndIdUsuario(idHogar, usuario.getId()).isPresent()) {
      return;
    }

    miembroHogarRepository.save(MiembroHogar.builder()
      .idHogar(idHogar)
      .idTipoMiembro(TIPO_MIEMBRO_PERSONA_ID)
      .nombre(nombreVisibleUsuario(usuario))
      .idUsuario(usuario.getId())
      .idEstado(ESTADO_ACTIVO_ID)
      .build());
  }

  private HogarPublicResponse toResponse(Hogar hogar) {
    return new HogarPublicResponse(
      hogar.getId(),
      hogar.getNombre(),
      hogar.getDescripcion(),
      hogar.getCodigo()
    );
  }

  private String nombreVisibleUsuario(Usuario usuario) {
    String apellidos = usuario.getApellidos() == null ? "" : usuario.getApellidos().trim();
    String nombre = usuario.getNombre() == null ? "" : usuario.getNombre().trim();
    String visible = (nombre + " " + apellidos).trim();
    return visible.isBlank() ? usuario.getEmail() : visible;
  }

  private String generarCodigo() {
    String codigo;
    do {
      codigo = "CASA" + UUID.randomUUID().toString().replace("-", "").substring(0, 4).toUpperCase();
    } while (hogarRepository.existsByCodigo(codigo));
    return codigo;
  }
}
