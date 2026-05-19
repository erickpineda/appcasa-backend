package com.appcasa.infrastructure.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(RecursoNoEncontradoException.class)
  public ProblemDetail handleRecursoNoEncontrado(RecursoNoEncontradoException ex) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    pd.setType(URI.create("/errors/not-found"));
    return pd;
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    pd.setType(URI.create("/errors/conflict"));
    return pd;
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ProblemDetail handleUnauthorized(UnauthorizedException ex) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
    pd.setType(URI.create("/errors/unauthorized"));
    return pd;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleValidacion(MethodArgumentNotValidException ex) {
    String errores = ex.getBindingResult().getFieldErrors().stream()
      .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
      .collect(Collectors.joining("; "));
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errores);
    pd.setType(URI.create("/errors/validation"));
    return pd;
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
    String parametro = ex.getName();
    String detalle = (parametro == null || parametro.isBlank())
      ? "Parametro invalido"
      : "Parametro " + parametro + " invalido";

    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detalle);
    pd.setType(URI.create("/errors/invalid-parameter"));
    return pd;
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleGeneral(Exception ex) {
    log.error("Error no controlado", ex);
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(
      HttpStatus.INTERNAL_SERVER_ERROR,
      "Error interno del servidor"
    );
    pd.setType(URI.create("/errors/internal"));
    return pd;
  }
}
