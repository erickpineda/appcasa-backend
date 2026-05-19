package com.appcasa.application.tarea;

import com.appcasa.domain.miembro.MiembroHogar;
import com.appcasa.domain.miembro.MiembroHogarRepository;
import com.appcasa.domain.tarea.Tarea;
import com.appcasa.domain.tarea.TareaAsignacion;
import com.appcasa.domain.tarea.TareaAsignacionRepository;
import com.appcasa.domain.tarea.TareaRepository;
import com.appcasa.infrastructure.exception.RecursoNoEncontradoException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TareaServiceImpl implements TareaService {

  private final TareaRepository tareaRepository;
  private final TareaAsignacionRepository tareaAsignacionRepository;
  private final MiembroHogarRepository miembroHogarRepository;

  @Override
  public Tarea crear(TareaRequest request, UUID idCreador) {
    List<UUID> idsMiembros = normalizarIdsMiembros(request.getIdsMiembros());
    validarMiembrosDelHogar(request.getIdHogar(), idsMiembros);

    Tarea tarea = Tarea.builder()
      .idHogar(request.getIdHogar())
      .titulo(request.getTitulo())
      .descripcion(request.getDescripcion())
      .idPrioridad(request.getIdPrioridad() != null ? request.getIdPrioridad() : 1)
      .categoria(request.getCategoria())
      .fechaLimite(request.getFechaLimite())
      .esPeriodica(Boolean.TRUE.equals(request.getEsPeriodica()))
      .periodicidad(request.getPeriodicidad())
      .reglaRecurrencia(request.getReglaRecurrencia())
      .esPersonal(determinarEsPersonal(request.getEsPersonal(), idsMiembros))
      .idCreador(idCreador)
      .build();

    Tarea guardada = tareaRepository.save(tarea);
    reemplazarAsignaciones(guardada, idsMiembros);
    log.info("Tarea creada: {} en hogar {}", guardada.getId(), guardada.getIdHogar());
    return guardada;
  }

  @Override
  public Tarea actualizar(UUID id, TareaRequest request) {
    Tarea tarea = obtener(id);
    List<UUID> idsMiembros = normalizarIdsMiembros(request.getIdsMiembros());
    validarMiembrosDelHogar(tarea.getIdHogar(), idsMiembros);

    tarea.setTitulo(request.getTitulo());
    tarea.setDescripcion(request.getDescripcion());
    tarea.setIdPrioridad(request.getIdPrioridad());
    tarea.setCategoria(request.getCategoria());
    tarea.setFechaLimite(request.getFechaLimite());
    tarea.setEsPeriodica(Boolean.TRUE.equals(request.getEsPeriodica()));
    tarea.setPeriodicidad(request.getPeriodicidad());
    tarea.setReglaRecurrencia(request.getReglaRecurrencia());
    tarea.setEsPersonal(determinarEsPersonal(request.getEsPersonal(), idsMiembros));

    Tarea actualizada = tareaRepository.save(tarea);
    reemplazarAsignaciones(actualizada, idsMiembros);
    return actualizada;
  }

  @Override
  public Tarea completar(UUID id) {
    Tarea tarea = obtener(id);
    tarea.setFechaCompletada(Instant.now());
    return tareaRepository.save(tarea);
  }

  @Override
  public void eliminar(UUID id) {
    Tarea tarea = obtener(id);
    tarea.setIdEstado(3); // ELIMINADO
    tareaRepository.save(tarea);
  }

  @Override
  @Transactional(readOnly = true)
  public Tarea obtener(UUID id) {
    return tareaRepository.findById(id)
      .orElseThrow(() -> new RecursoNoEncontradoException("Tarea", id));
  }

  @Override
  @Transactional(readOnly = true)
  public List<Tarea> listarPendientes(UUID idHogar) {
    return tareaRepository.findPendientesByHogar(idHogar);
  }

  private Boolean determinarEsPersonal(Boolean esPersonalSolicitado, List<UUID> idsMiembros) {
    if (!idsMiembros.isEmpty()) {
      return false;
    }
    return Boolean.TRUE.equals(esPersonalSolicitado);
  }

  private List<UUID> normalizarIdsMiembros(List<UUID> idsMiembros) {
    if (idsMiembros == null || idsMiembros.isEmpty()) {
      return List.of();
    }

    Set<UUID> idsUnicos = new LinkedHashSet<>();
    idsMiembros.stream()
      .filter(Objects::nonNull)
      .forEach(idsUnicos::add);

    return List.copyOf(idsUnicos);
  }

  private void validarMiembrosDelHogar(UUID idHogar, List<UUID> idsMiembros) {
    if (idsMiembros.isEmpty()) {
      return;
    }

    List<MiembroHogar> miembros = miembroHogarRepository.findAllById(idsMiembros);
    Set<UUID> idsValidos = miembros.stream()
      .filter(miembro -> idHogar.equals(miembro.getIdHogar()))
      .map(MiembroHogar::getId)
      .collect(java.util.stream.Collectors.toSet());

    if (idsValidos.size() != idsMiembros.size()) {
      throw new IllegalArgumentException("Los miembros seleccionados no pertenecen al hogar de la tarea");
    }
  }

  private void reemplazarAsignaciones(Tarea tarea, List<UUID> idsMiembros) {
    tareaAsignacionRepository.deleteByIdTarea(tarea.getId());

    if (idsMiembros.isEmpty()) {
      return;
    }

    List<TareaAsignacion> asignaciones = idsMiembros.stream()
      .map(idMiembro -> TareaAsignacion.builder()
        .idTarea(tarea.getId())
        .idMiembro(idMiembro)
        .build())
      .toList();

    tareaAsignacionRepository.saveAll(asignaciones);
  }
}
