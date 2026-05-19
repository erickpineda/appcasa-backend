package com.appcasa.application.auth;

public class AuthResponse {

  private final String token;
  private final UsuarioDto usuario;

  public AuthResponse(String token, UsuarioDto usuario) {
    this.token = token;
    this.usuario = usuario;
  }

  public String getToken() {
    return token;
  }

  public UsuarioDto getUsuario() {
    return usuario;
  }
}
