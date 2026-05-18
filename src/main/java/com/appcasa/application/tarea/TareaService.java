package com.appcasa.application.tarea;

import com.appcasa.domain.tarea.Tarea;

import java.util.List;
import java.util.UUID;

public interface TareaService {

  Tarea crear(TareaRequest request, UUID idCreador);

  Tarea actualizar(UUID id, TareaRequest request);

  Tarea completar(UUID id);

  void eliminar(UUID id);

  Tarea obtener(UUID id);

  List<Tarea> listarPendientes(UUID idHogar);
}
