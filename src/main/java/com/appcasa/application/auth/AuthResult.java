package com.appcasa.application.auth;

public record AuthResult(
  AuthResponse body,
  String refreshToken
) {
}
