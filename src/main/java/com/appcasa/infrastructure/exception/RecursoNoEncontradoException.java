// ============================================================
// RecursoNoEncontradoException.java
// ============================================================
package com.appcasa.infrastructure.exception;

import java.util.UUID;

public class RecursoNoEncontradoException extends RuntimeException {

  public RecursoNoEncontradoException(String recurso, UUID id) {
    super(recurso + " no encontrado: " + id);
  }

  public RecursoNoEncontradoException(String mensaje) {
    super(mensaje);
  }
}
