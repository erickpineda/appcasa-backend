package com.appcasa.infrastructure.api.mapper;

import com.appcasa.domain.hogar.Hogar;
import com.appcasa.domain.hogar.HogarRepository;
import com.appcasa.domain.miembro.MiembroHogar;
import com.appcasa.domain.miembro.MiembroHogarRepository;
import com.appcasa.domain.tarea.Tarea;
import com.appcasa.domain.tarea.TareaAsignacion;
import com.appcasa.domain.tarea.TareaAsignacionRepository;
import com.appcasa.infrastructure.api.dto.catalogo.CodigoLabelDto;
import com.appcasa.infrastructure.api.dto.tarea.TareaAsignacionPublicResponse;
import com.appcasa.infrastructure.api.dto.tarea.TareaPublicResponse;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

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
  private final TareaAsignacionRepository tareaAsignacionRepository;
  private final MiembroHogarRepository miembroHogarRepository;

  public TareaPublicMapper(
    HogarRepository hogarRepository,
    TareaAsignacionRepository tareaAsignacionRepository,
    MiembroHogarRepository miembroHogarRepository
  ) {
    this.hogarRepository = hogarRepository;
    this.tareaAsignacionRepository = tareaAsignacionRepository;
    this.miembroHogarRepository = miembroHogarRepository;
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
      asignacionesDto(tarea.getId())
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

  private List<TareaAsignacionPublicResponse> asignacionesDto(UUID idTarea) {
    List<TareaAsignacion> asignaciones = tareaAsignacionRepository.findByIdTarea(idTarea);
    if (asignaciones.isEmpty()) {
      return List.of();
    }

    Map<UUID, MiembroHogar> miembros = miembroHogarRepository.findAllById(
        asignaciones.stream().map(TareaAsignacion::getIdMiembro).toList()
      ).stream()
      .collect(Collectors.toMap(MiembroHogar::getId, Function.identity()));

    return asignaciones.stream()
      .map(asignacion -> new TareaAsignacionPublicResponse(
        asignacion.getIdMiembro(),
        miembros.containsKey(asignacion.getIdMiembro()) ? miembros.get(asignacion.getIdMiembro()).getNombre() : null
      ))
      .sorted(Comparator
        .comparing((TareaAsignacionPublicResponse item) -> item.nombreMiembro() == null ? "" : item.nombreMiembro())
        .thenComparing(TareaAsignacionPublicResponse::miembroId))
      .toList();
  }
}
