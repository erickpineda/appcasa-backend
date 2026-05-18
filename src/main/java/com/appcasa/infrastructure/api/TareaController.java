package com.appcasa.infrastructure.api;

import com.appcasa.application.tarea.TareaRequest;
import com.appcasa.application.tarea.TareaService;
import com.appcasa.domain.tarea.Tarea;
import com.appcasa.domain.usuario.Usuario;
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

  @GetMapping("/hogar/{idHogar}/pendientes")
  public ResponseEntity<List<Tarea>> listarPendientes(@PathVariable UUID idHogar) {
    return ResponseEntity.ok(tareaService.listarPendientes(idHogar));
  }

  @GetMapping("/{id}")
  public ResponseEntity<Tarea> obtener(@PathVariable UUID id) {
    return ResponseEntity.ok(tareaService.obtener(id));
  }

  @PostMapping
  public ResponseEntity<Tarea> crear(
    @Valid @RequestBody TareaRequest request,
    @AuthenticationPrincipal Usuario usuario
  ) {
    Tarea tarea = tareaService.crear(request, usuario.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(tarea);
  }

  @PutMapping("/{id}")
  public ResponseEntity<Tarea> actualizar(
    @PathVariable UUID id,
    @Valid @RequestBody TareaRequest request
  ) {
    return ResponseEntity.ok(tareaService.actualizar(id, request));
  }

  @PatchMapping("/{id}/completar")
  public ResponseEntity<Tarea> completar(@PathVariable UUID id) {
    return ResponseEntity.ok(tareaService.completar(id));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
    tareaService.eliminar(id);
    return ResponseEntity.noContent().build();
  }
}
