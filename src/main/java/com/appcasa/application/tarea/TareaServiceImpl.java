package com.appcasa.application.tarea;

import com.appcasa.domain.tarea.Tarea;
import com.appcasa.domain.tarea.TareaRepository;
import com.appcasa.infrastructure.exception.RecursoNoEncontradoException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TareaServiceImpl implements TareaService {

  private final TareaRepository tareaRepository;

  @Override
  public Tarea crear(TareaRequest request, UUID idCreador) {
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
      .esPersonal(Boolean.TRUE.equals(request.getEsPersonal()))
      .idCreador(idCreador)
      .build();

    Tarea guardada = tareaRepository.save(tarea);
    log.info("Tarea creada: {} en hogar {}", guardada.getId(), guardada.getIdHogar());
    return guardada;
  }

  @Override
  public Tarea actualizar(UUID id, TareaRequest request) {
    Tarea tarea = obtener(id);
    tarea.setTitulo(request.getTitulo());
    tarea.setDescripcion(request.getDescripcion());
    tarea.setIdPrioridad(request.getIdPrioridad());
    tarea.setCategoria(request.getCategoria());
    tarea.setFechaLimite(request.getFechaLimite());
    tarea.setEsPeriodica(Boolean.TRUE.equals(request.getEsPeriodica()));
    tarea.setPeriodicidad(request.getPeriodicidad());
    tarea.setReglaRecurrencia(request.getReglaRecurrencia());
    return tareaRepository.save(tarea);
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
}
