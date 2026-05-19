package com.appcasa.infrastructure.api;

import com.appcasa.domain.hogar.HogarUsuarioRepository;
import com.appcasa.domain.miembro.MiembroHogar;
import com.appcasa.domain.miembro.MiembroHogarRepository;
import com.appcasa.domain.usuario.Usuario;
import com.appcasa.infrastructure.api.dto.miembro.MiembroHogarPublicResponse;
import com.appcasa.infrastructure.exception.RecursoNoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/miembros")
@RequiredArgsConstructor
public class MiembroController {

  private static final int ESTADO_ACTIVO_ID = 1;

  private final MiembroHogarRepository miembroHogarRepository;
  private final HogarUsuarioRepository hogarUsuarioRepository;

  @GetMapping("/hogar/{idHogar}")
  public ResponseEntity<List<MiembroHogarPublicResponse>> listarPorHogar(
    @PathVariable UUID idHogar,
    @AuthenticationPrincipal Usuario usuario
  ) {
    if (hogarUsuarioRepository.findByIdHogarAndIdUsuario(idHogar, usuario.getId()).isEmpty()) {
      throw new RecursoNoEncontradoException("Hogar no encontrado: " + idHogar);
    }

    List<MiembroHogarPublicResponse> body = miembroHogarRepository.findByIdHogarAndIdEstado(idHogar, ESTADO_ACTIVO_ID).stream()
      .sorted(Comparator.comparing(MiembroHogar::getNombre, String.CASE_INSENSITIVE_ORDER))
      .map(this::toResponse)
      .toList();

    return ResponseEntity.ok(body);
  }

  private MiembroHogarPublicResponse toResponse(MiembroHogar miembro) {
    return new MiembroHogarPublicResponse(
      miembro.getId(),
      miembro.getIdHogar(),
      miembro.getIdTipoMiembro(),
      miembro.getNombre(),
      miembro.getFechaNacimiento(),
      miembro.getAvatarUrl(),
      miembro.getNotas(),
      miembro.getRaza(),
      miembro.getColor(),
      miembro.getMicrochip() != null ? miembro.getMicrochip() : miembro.getChipNum(),
      miembro.getEsterilizado(),
      miembro.getIdUsuario(),
      miembro.getIdEstado()
    );
  }
}
