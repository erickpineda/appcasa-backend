package com.appcasa.infrastructure.api.mapper;

import com.appcasa.domain.hogar.Hogar;
import com.appcasa.domain.hogar.HogarRepository;
import com.appcasa.domain.tarea.Tarea;
import com.appcasa.infrastructure.api.dto.catalogo.CodigoLabelDto;
import com.appcasa.infrastructure.api.dto.tarea.TareaPublicResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class TareaPublicMapper {

  private static final Map<Integer, CodigoLabelDto> PRIORIDADES = Map.of(
    1, new CodigoLabelDto("BAJA", "Baja"),
    2, new CodigoLabelDto("MEDIA", "Media"),
    3, new CodigoLabelDto("ALTA", "Alta"),
    4, new CodigoLabelDto("URGENTE", "Urgente")
  );

  private static final Map<Integer, CodigoLabelDto> ESTADOS = Map.of(
    1, new CodigoLabelDto("ACTIVA", "Activa"),
    2, new CodigoLabelDto("COMPLETADA", "Completada"),
    3, new CodigoLabelDto("ELIMINADA", "Eliminada")
  );

  private static final Map<String, CodigoLabelDto> PERIODICIDADES = Map.of(
    "DIARIA", new CodigoLabelDto("DIARIA", "Diaria"),
    "SEMANAL", new CodigoLabelDto("SEMANAL", "Semanal"),
    "MENSUAL", new CodigoLabelDto("MENSUAL", "Mensual"),
    "ANUAL", new CodigoLabelDto("ANUAL", "Anual")
  );

  private final HogarRepository hogarRepository;

  public TareaPublicMapper(HogarRepository hogarRepository) {
    this.hogarRepository = hogarRepository;
  }

  public TareaPublicResponse toResponse(Tarea tarea) {
    return new TareaPublicResponse(
      tarea.getId(),
      resolverHogarCodigo(tarea.getIdHogar()),
      tarea.getTitulo(),
      tarea.getDescripcion(),
      prioridadDto(tarea.getIdPrioridad()),
      tarea.getCategoria(),
      tarea.getFechaLimite(),
      tarea.getFechaCompletada(),
      periodicidadDto(tarea.getPeriodicidad()),
      Boolean.TRUE.equals(tarea.getEsPersonal()),
      estadoDto(tarea.getIdEstado()),
      List.of()
    );
  }

  public UUID resolveHogarId(String hogarCodigo) {
    return hogarRepository.findByCodigo(hogarCodigo)
      .map(Hogar::getId)
      .orElseThrow(() -> new IllegalArgumentException("Hogar no encontrado para codigo " + hogarCodigo));
  }

  public Integer resolvePrioridadId(String prioridadCodigo) {
    if (prioridadCodigo == null || prioridadCodigo.isBlank()) {
      return 1;
    }

    return switch (prioridadCodigo) {
      case "BAJA" -> 1;
      case "MEDIA" -> 2;
      case "ALTA" -> 3;
      case "URGENTE" -> 4;
      default -> throw new IllegalArgumentException("Prioridad no valida: " + prioridadCodigo);
    };
  }

  public String resolvePeriodicidad(String periodicidadCodigo) {
    if (periodicidadCodigo == null || periodicidadCodigo.isBlank()) {
      return null;
    }

    if (!PERIODICIDADES.containsKey(periodicidadCodigo)) {
      throw new IllegalArgumentException("Periodicidad no valida: " + periodicidadCodigo);
    }

    return periodicidadCodigo;
  }

  private String resolverHogarCodigo(UUID idHogar) {
    return hogarRepository.findById(idHogar)
      .map(Hogar::getCodigo)
      .orElse(null);
  }

  private CodigoLabelDto prioridadDto(Integer idPrioridad) {
    return PRIORIDADES.getOrDefault(idPrioridad, PRIORIDADES.get(1));
  }

  private CodigoLabelDto estadoDto(Integer idEstado) {
    return ESTADOS.getOrDefault(idEstado, ESTADOS.get(1));
  }

  private CodigoLabelDto periodicidadDto(String periodicidad) {
    if (periodicidad == null || periodicidad.isBlank()) {
      return null;
    }

    return PERIODICIDADES.getOrDefault(periodicidad, new CodigoLabelDto(periodicidad, periodicidad));
  }
}
