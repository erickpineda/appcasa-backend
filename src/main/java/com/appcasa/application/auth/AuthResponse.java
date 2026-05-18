package com.appcasa.application.auth;

public class AuthResponse {

  private final String token;
  private final String refreshToken;
  private final UsuarioDto usuario;

  public AuthResponse(String token, String refreshToken, UsuarioDto usuario) {
    this.token = token;
    this.refreshToken = refreshToken;
    this.usuario = usuario;
  }

  public String getToken() {
    return token;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public UsuarioDto getUsuario() {
    return usuario;
  }
}
