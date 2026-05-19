package com.appcasa.application.auth;

import com.appcasa.domain.auth.RefreshTokenSession;
import com.appcasa.domain.auth.RefreshTokenSessionRepository;
import com.appcasa.domain.usuario.Usuario;
import com.appcasa.domain.usuario.UsuarioRepository;
import com.appcasa.infrastructure.exception.UnauthorizedException;
import com.appcasa.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

  private final UsuarioRepository usuarioRepository;
  private final RefreshTokenSessionRepository refreshTokenSessionRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public AuthResult registro(RegistroRequest request) {
    if (usuarioRepository.existsByEmail(request.getEmail())) {
      throw new IllegalArgumentException("El email ya esta registrado");
    }

    Usuario usuario = Usuario.builder()
      .nombre(request.getNombre())
      .apellidos(request.getApellidos())
      .email(request.getEmail())
      .passwordHash(passwordEncoder.encode(request.getPassword()))
      .build();

    Usuario guardado = usuarioRepository.save(usuario);
    log.info("Usuario registrado: {}", guardado.getEmail());

    actualizarUltimoAcceso(guardado);
    return crearSesion(guardado);
  }

  public AuthResult login(LoginRequest request) {
    Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
      .orElseThrow(() -> new UnauthorizedException("Credenciales incorrectas"));

    if (!passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash())) {
      throw new UnauthorizedException("Credenciales incorrectas");
    }

    actualizarUltimoAcceso(usuario);
    return crearSesion(usuario);
  }

  public AuthResult refresh(String refreshToken) {
    RefreshTokenSession sesionActual = obtenerSesionActiva(refreshToken);

    sesionActual.setUltimoUsoEn(Instant.now());
    sesionActual.setRevocadoEn(Instant.now());
    refreshTokenSessionRepository.save(sesionActual);

    actualizarUltimoAcceso(sesionActual.getUsuario());
    return crearSesion(sesionActual.getUsuario());
  }

  public void logout(String refreshToken) {
    if (refreshToken == null || refreshToken.isBlank()) {
      return;
    }

    refreshTokenSessionRepository.findByTokenHash(hash(refreshToken))
      .ifPresent(sesion -> {
        sesion.setRevocadoEn(Instant.now());
        sesion.setUltimoUsoEn(Instant.now());
        refreshTokenSessionRepository.save(sesion);
      });
  }

  private void actualizarUltimoAcceso(Usuario usuario) {
    usuario.setUltimoAcceso(Instant.now());
    usuarioRepository.save(usuario);
  }

  private AuthResult crearSesion(Usuario usuario) {
    String token = jwtService.generarToken(usuario.getId(), usuario.getEmail());
    String refreshToken = generarRefreshTokenSeguro();

    persistirRefreshToken(usuario, refreshToken);

    UsuarioDto dto = new UsuarioDto(
      usuario.getId().toString(),
      usuario.getNombre(),
      usuario.getApellidos(),
      usuario.getEmail(),
      usuario.getTema(),
      usuario.getLocale()
    );

    return new AuthResult(new AuthResponse(token, dto), refreshToken);
  }

  private void persistirRefreshToken(Usuario usuario, String refreshToken) {
    Instant ahora = Instant.now();
    RefreshTokenSession sesion = RefreshTokenSession.builder()
      .usuario(usuario)
      .tokenHash(hash(refreshToken))
      .expiraEn(ahora.plusMillis(jwtService.getRefreshExpirationMs()))
      .ultimoUsoEn(ahora)
      .build();

    refreshTokenSessionRepository.save(sesion);
  }

  private RefreshTokenSession obtenerSesionActiva(String refreshToken) {
    if (refreshToken == null || refreshToken.isBlank()) {
      throw new UnauthorizedException("Sesion no valida");
    }

    RefreshTokenSession sesion = refreshTokenSessionRepository.findByTokenHash(hash(refreshToken))
      .orElseThrow(() -> new UnauthorizedException("Sesion no valida"));

    if (sesion.getRevocadoEn() != null || sesion.getExpiraEn().isBefore(Instant.now())) {
      throw new UnauthorizedException("Sesion no valida");
    }

    return sesion;
  }

  private String generarRefreshTokenSeguro() {
    return UUID.randomUUID() + "-" + UUID.randomUUID();
  }

  private String hash(String valor) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(valor.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException("No se pudo generar el hash del refresh token", ex);
    }
  }
}
