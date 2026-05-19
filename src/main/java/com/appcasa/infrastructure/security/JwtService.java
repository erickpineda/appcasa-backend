package com.appcasa.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

  private final JwtProperties props;

  // ─── Generación ──────────────────────────────────────────

  public String generarToken(UUID idUsuario, String email) {
    return buildToken(idUsuario, email, props.getExpirationMs());
  }

  public String generarRefreshToken(UUID idUsuario, String email) {
    return buildToken(idUsuario, email, props.getRefreshExpirationMs());
  }

  public long getRefreshExpirationMs() {
    return props.getRefreshExpirationMs();
  }

  private String buildToken(UUID idUsuario, String email, long expirationMs) {
    Date ahora = new Date();
    Date expira = new Date(ahora.getTime() + expirationMs);

    return Jwts.builder()
      .subject(idUsuario.toString())
      .claim("email", email)
      .issuedAt(ahora)
      .expiration(expira)
      .signWith(clave())
      .compact();
  }

  // ─── Validación ──────────────────────────────────────────

  public boolean esValido(String token) {
    try {
      parsear(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      log.debug("Token inválido: {}", e.getMessage());
      return false;
    }
  }

  public UUID extraerIdUsuario(String token) {
    String subject = parsear(token).getSubject();
    return UUID.fromString(subject);
  }

  public String extraerEmail(String token) {
    return (String) parsear(token).get("email");
  }

  // ─── Interno ─────────────────────────────────────────────

  private Claims parsear(String token) {
    return Jwts.parser()
      .verifyWith(clave())
      .build()
      .parseSignedClaims(token)
      .getPayload();
  }

  private SecretKey clave() {
    return Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
  }
}
