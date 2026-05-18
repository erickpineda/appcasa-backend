package com.appcasa.application.auth;

import com.appcasa.domain.usuario.Usuario;
import com.appcasa.domain.usuario.UsuarioRepository;
import com.appcasa.infrastructure.exception.RecursoNoEncontradoException;
import com.appcasa.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

  private final UsuarioRepository usuarioRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public AuthResponse registro(RegistroRequest request) {
    if (usuarioRepository.existsByEmail(request.getEmail())) {
      throw new IllegalArgumentException("El email ya está registrado");
    }

    Usuario usuario = Usuario.builder()
      .nombre(request.getNombre())
      .apellidos(request.getApellidos())
      .email(request.getEmail())
      .passwordHash(passwordEncoder.encode(request.getPassword()))
      .build();

    Usuario guardado = usuarioRepository.save(usuario);
    log.info("Usuario registrado: {}", guardado.getEmail());

    return buildResponse(guardado);
  }

  public AuthResponse login(LoginRequest request) {
    Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
      .orElseThrow(() -> new RecursoNoEncontradoException("Credenciales incorrectas"));

    if (!passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash())) {
      throw new RecursoNoEncontradoException("Credenciales incorrectas");
    }

    usuario.setUltimoAcceso(Instant.now());
    usuarioRepository.save(usuario);

    return buildResponse(usuario);
  }

  private AuthResponse buildResponse(Usuario usuario) {
    String token        = jwtService.generarToken(usuario.getId(), usuario.getEmail());
    String refreshToken = jwtService.generarRefreshToken(usuario.getId(), usuario.getEmail());

    UsuarioDto dto = new UsuarioDto(
      usuario.getId().toString(),
      usuario.getNombre(),
      usuario.getApellidos(),
      usuario.getEmail(),
      usuario.getTema(),
      usuario.getLocale()
    );

    return new AuthResponse(token, refreshToken, dto);
  }
}
