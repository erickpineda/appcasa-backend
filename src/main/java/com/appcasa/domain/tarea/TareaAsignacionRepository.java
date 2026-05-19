package com.appcasa.domain.tarea;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TareaAsignacionRepository extends JpaRepository<TareaAsignacion, UUID> {

  List<TareaAsignacion> findByIdTarea(UUID idTarea);

  void deleteByIdTarea(UUID idTarea);
}
