package com.appcasa.application.auth;

public class UsuarioDto {

  private final String id;
  private final String nombre;
  private final String apellidos;
  private final String email;
  private final String tema;
  private final String locale;

  public UsuarioDto(String id, String nombre, String apellidos, String email, String tema, String locale) {
    this.id = id;
    this.nombre = nombre;
    this.apellidos = apellidos;
    this.email = email;
    this.tema = tema;
    this.locale = locale;
  }

  public String getId() {
    return id;
  }

  public String getNombre() {
    return nombre;
  }

  public String getApellidos() {
    return apellidos;
  }

  public String getEmail() {
    return email;
  }

  public String getTema() {
    return tema;
  }

  public String getLocale() {
    return locale;
  }
}
