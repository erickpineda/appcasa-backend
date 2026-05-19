package com.appcasa.infrastructure.api;

import com.appcasa.application.auth.AuthResponse;
import com.appcasa.application.auth.AuthResult;
import com.appcasa.application.auth.AuthService;
import com.appcasa.application.auth.LoginRequest;
import com.appcasa.application.auth.RegistroRequest;
import com.appcasa.infrastructure.security.JwtService;
import com.appcasa.infrastructure.security.RefreshCookieProperties;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final RefreshCookieProperties refreshCookieProperties;
  private final JwtService jwtService;

  @PostMapping("/registro")
  public ResponseEntity<AuthResponse> registro(
    @Valid @RequestBody RegistroRequest request
  ) {
    AuthResult result = authService.registro(request);
    return ResponseEntity.status(HttpStatus.CREATED)
      .header("Set-Cookie", buildRefreshCookie(result.refreshToken()).toString())
      .body(result.body());
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(
    @Valid @RequestBody LoginRequest request
  ) {
    AuthResult result = authService.login(request);
    return ResponseEntity.ok()
      .header("Set-Cookie", buildRefreshCookie(result.refreshToken()).toString())
      .body(result.body());
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthResponse> refresh(
    @CookieValue(name = "appcasa_refresh", required = false) String refreshToken
  ) {
    AuthResult result = authService.refresh(refreshToken);
    return ResponseEntity.ok()
      .header("Set-Cookie", buildRefreshCookie(result.refreshToken()).toString())
      .body(result.body());
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(
    @CookieValue(name = "appcasa_refresh", required = false) String refreshToken
  ) {
    authService.logout(refreshToken);
    return ResponseEntity.noContent()
      .header("Set-Cookie", buildExpiredRefreshCookie().toString())
      .build();
  }

  private ResponseCookie buildRefreshCookie(String refreshToken) {
    return ResponseCookie.from(refreshCookieProperties.getName(), refreshToken)
      .httpOnly(true)
      .secure(refreshCookieProperties.isSecure())
      .sameSite(refreshCookieProperties.getSameSite())
      .path(refreshCookieProperties.getPath())
      .maxAge(Duration.ofMillis(jwtService.getRefreshExpirationMs()))
      .build();
  }

  private ResponseCookie buildExpiredRefreshCookie() {
    return ResponseCookie.from(refreshCookieProperties.getName(), "")
      .httpOnly(true)
      .secure(refreshCookieProperties.isSecure())
      .sameSite(refreshCookieProperties.getSameSite())
      .path(refreshCookieProperties.getPath())
      .maxAge(Duration.ZERO)
      .build();
  }
}
