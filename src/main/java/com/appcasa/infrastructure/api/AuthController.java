package com.appcasa.infrastructure.api;

import com.appcasa.application.auth.AuthResponse;
import com.appcasa.application.auth.AuthService;
import com.appcasa.application.auth.LoginRequest;
import com.appcasa.application.auth.RegistroRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/registro")
  public ResponseEntity<AuthResponse> registro(
    @Valid @RequestBody RegistroRequest request
  ) {
    return ResponseEntity.status(HttpStatus.CREATED).body(authService.registro(request));
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(
    @Valid @RequestBody LoginRequest request
  ) {
    return ResponseEntity.ok(authService.login(request));
  }
}
