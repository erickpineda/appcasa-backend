package com.appcasa.infrastructure.api;

import com.appcasa.application.tarea.TareaRequest;
import com.appcasa.application.tarea.TareaService;
import com.appcasa.domain.usuario.Usuario;
import com.appcasa.infrastructure.api.dto.tarea.TareaPublicRequest;
import com.appcasa.infrastructure.api.dto.tarea.TareaPublicResponse;
import com.appcasa.infrastructure.api.mapper.TareaPublicMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tareas")
@RequiredArgsConstructor
public class TareaController {

  private final TareaService tareaService;
  private final TareaPublicMapper tareaPublicMapper;

  @GetMapping("/hogar/{hogarCodigo}/pendientes")
  public ResponseEntity<List<TareaPublicResponse>> listarPendientes(@PathVariable String hogarCodigo) {
    UUID idHogar = tareaPublicMapper.resolveHogarId(hogarCodigo);
    List<TareaPublicResponse> body = tareaService.listarPendientes(idHogar).stream()
      .map(tareaPublicMapper::toResponse)
      .toList();
    return ResponseEntity.ok(body);
  }

  @GetMapping("/{id}")
  public ResponseEntity<TareaPublicResponse> obtener(@PathVariable UUID id) {
    return ResponseEntity.ok(tareaPublicMapper.toResponse(tareaService.obtener(id)));
  }

  @PostMapping
  public ResponseEntity<TareaPublicResponse> crear(
    @Valid @RequestBody TareaPublicRequest request,
    @AuthenticationPrincipal Usuario usuario
  ) {
    TareaRequest internalRequest = toInternalRequest(request);
    return ResponseEntity.status(HttpStatus.CREATED)
      .body(tareaPublicMapper.toResponse(tareaService.crear(internalRequest, usuario.getId())));
  }

  @PutMapping("/{id}")
  public ResponseEntity<TareaPublicResponse> actualizar(
    @PathVariable UUID id,
    @Valid @RequestBody TareaPublicRequest request
  ) {
    TareaRequest internalRequest = toInternalRequest(request);
    return ResponseEntity.ok(tareaPublicMapper.toResponse(tareaService.actualizar(id, internalRequest)));
  }

  @PatchMapping("/{id}/completar")
  public ResponseEntity<TareaPublicResponse> completar(@PathVariable UUID id) {
    return ResponseEntity.ok(tareaPublicMapper.toResponse(tareaService.completar(id)));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
    tareaService.eliminar(id);
    return ResponseEntity.noContent().build();
  }

  private TareaRequest toInternalRequest(TareaPublicRequest request) {
    TareaRequest internal = new TareaRequest();
    internal.setIdHogar(tareaPublicMapper.resolveHogarId(request.hogarCodigo()));
    internal.setTitulo(request.titulo());
    internal.setDescripcion(request.descripcion());
    internal.setIdPrioridad(tareaPublicMapper.resolvePrioridadId(request.prioridadCodigo()));
    internal.setCategoria(request.categoria());
    internal.setFechaLimite(request.fechaLimite());
    internal.setEsPeriodica(request.esPeriodica());
    internal.setPeriodicidad(tareaPublicMapper.resolvePeriodicidad(request.periodicidadCodigo()));
    internal.setEsPersonal(request.esPersonal());
    internal.setIdsMiembros(request.miembroIds());
    return internal;
  }
}
