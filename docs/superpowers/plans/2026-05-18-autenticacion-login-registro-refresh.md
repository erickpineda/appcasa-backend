# Autenticacion Login Registro Refresh Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implementar autenticacion completa en `AppCasa` con login, registro, refresh token persistido y rotado, cookie `HttpOnly`, logout y recuperacion silenciosa de sesion en backend y frontend.

**Architecture:** El backend mantiene `JWT bearer` para autorizacion con un `token` corto y anade persistencia de refresh tokens para continuidad y revocacion de sesion. El frontend deja de manipular el refresh token, trabaja con `withCredentials`, renueva el `token` en segundo plano y recupera sesion al arrancar.

**Tech Stack:** Spring Boot 3.5, Spring Security, JPA, Flyway, MySQL/H2, Angular 21, Ionic 8, HttpClient, Karma/Jasmine

---

## File Map

**Backend**

- Create: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\domain\auth\RefreshTokenSession.java`
- Create: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\domain\auth\RefreshTokenSessionRepository.java`
- Create: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\application\auth\RefreshRequestContext.java`
- Create: `C:\dev\git\appcasa-backend\src\main\resources\db\migration\V3__auth_refresh_sessions.sql`
- Create: `C:\dev\git\appcasa-backend\src\test\java\com\appcasa\infrastructure\api\AuthControllerIntegrationTest.java`
- Modify: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\application\auth\AuthService.java`
- Modify: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\application\auth\AuthResponse.java`
- Modify: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\infrastructure\api\AuthController.java`
- Modify: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\infrastructure\exception\GlobalExceptionHandler.java`
- Modify: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\infrastructure\security\SecurityConfig.java`
- Modify: `C:\dev\git\appcasa-backend\src\main\resources\application.yml`

**Frontend**

- Create: `C:\dev\git\appcasa-frontend\src\app\core\services\auth.service.spec.ts`
- Create: `C:\dev\git\appcasa-frontend\src\app\core\interceptors\auth.interceptor.spec.ts`
- Modify: `C:\dev\git\appcasa-frontend\src\app\core\services\auth.service.ts`
- Modify: `C:\dev\git\appcasa-frontend\src\app\core\interceptors\auth.interceptor.ts`
- Modify: `C:\dev\git\appcasa-frontend\src\app\core\guards\auth.guard.ts`
- Modify: `C:\dev\git\appcasa-frontend\src\app\features\auth\auth.page.ts`
- Modify: `C:\dev\git\appcasa-frontend\src\app\app.component.ts`
- Modify: `C:\dev\git\appcasa-frontend\src\app\core\models\domain.models.ts`
- Modify: `C:\dev\git\appcasa-frontend\src\environments\environment.ts`
- Modify: `C:\dev\git\appcasa-frontend\src\environments\environment.prod.ts`

### Task 1: Persistencia de refresh token en backend

**Files:**
- Create: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\domain\auth\RefreshTokenSession.java`
- Create: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\domain\auth\RefreshTokenSessionRepository.java`
- Create: `C:\dev\git\appcasa-backend\src\main\resources\db\migration\V3__auth_refresh_sessions.sql`
- Test: `C:\dev\git\appcasa-backend\src\test\java\com\appcasa\infrastructure\api\AuthControllerIntegrationTest.java`

- [ ] **Step 1: Write the failing migration-backed auth test**

```java
@Test
void login_returnsRefreshCookie() throws Exception {
  String email = "refresh-" + UUID.randomUUID() + "@example.com";

  mockMvc.perform(post("/api/v1/auth/registro")
      .contentType(MediaType.APPLICATION_JSON)
      .content("""
        {"nombre":"Ana","apellidos":"Casa","email":"%s","password":"Password123"}
        """.formatted(email)))
    .andExpect(status().isCreated())
    .andExpect(cookie().exists("appcasa_refresh"));
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn "-Dtest=AuthControllerIntegrationTest#login_returnsRefreshCookie" test`
Expected: FAIL because no refresh cookie is written and no auth integration test exists yet.

- [ ] **Step 3: Write minimal persistence model and migration**

```java
@Entity
@Table(name = "TB_REFRESH_TOKEN_SESSION")
public class RefreshTokenSession {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "id_usuario", nullable = false)
  private Usuario usuario;

  @Column(name = "token_hash", nullable = false, unique = true, length = 64)
  private String tokenHash;

  @Column(name = "expira_en", nullable = false)
  private Instant expiraEn;

  @Column(name = "ultimo_uso_en")
  private Instant ultimoUsoEn;

  @Column(name = "revocado_en")
  private Instant revocadoEn;
}
```

```sql
create table TB_REFRESH_TOKEN_SESSION (
  id uuid primary key,
  id_usuario uuid not null,
  token_hash varchar(64) not null unique,
  expira_en timestamp not null,
  ultimo_uso_en timestamp null,
  revocado_en timestamp null,
  created_at timestamp not null default current_timestamp,
  updated_at timestamp not null default current_timestamp,
  version bigint not null default 0
);
```

- [ ] **Step 4: Run backend auth test to verify persistence wiring passes**

Run: `mvn "-Dtest=AuthControllerIntegrationTest#login_returnsRefreshCookie" test`
Expected: PASS once the migration applies and the entity/repository are visible to JPA.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/appcasa/domain/auth/RefreshTokenSession.java src/main/java/com/appcasa/domain/auth/RefreshTokenSessionRepository.java src/main/resources/db/migration/V3__auth_refresh_sessions.sql src/test/java/com/appcasa/infrastructure/api/AuthControllerIntegrationTest.java
git commit -m "feat: add refresh token session persistence"
```

### Task 2: Login y registro con cookie HttpOnly

**Files:**
- Modify: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\application\auth\AuthService.java`
- Modify: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\application\auth\AuthResponse.java`
- Modify: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\infrastructure\api\AuthController.java`
- Modify: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\infrastructure\exception\GlobalExceptionHandler.java`
- Modify: `C:\dev\git\appcasa-backend\src\main\resources\application.yml`
- Test: `C:\dev\git\appcasa-backend\src\test\java\com\appcasa\infrastructure\api\AuthControllerIntegrationTest.java`

- [ ] **Step 1: Extend the auth integration test with response contract assertions**

```java
@Test
void registro_returnsTokenUsuarioAndRefreshCookie() throws Exception {
  mockMvc.perform(post("/api/v1/auth/registro")
      .contentType(MediaType.APPLICATION_JSON)
      .content("""
        {"nombre":"Ana","apellidos":"Casa","email":"ana@example.com","password":"Password123"}
        """))
    .andExpect(status().isCreated())
    .andExpect(jsonPath("$.token").isString())
    .andExpect(jsonPath("$.usuario.email").value("ana@example.com"))
    .andExpect(cookie().httpOnly("appcasa_refresh", true));
}

@Test
void registro_duplicateEmail_returnsConflict() throws Exception {
  // create first user, then repeat same payload
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn "-Dtest=AuthControllerIntegrationTest#registro_returnsTokenUsuarioAndRefreshCookie+AuthControllerIntegrationTest#registro_duplicateEmail_returnsConflict" test`
Expected: FAIL because duplicate email currently throws `IllegalArgumentException` and no cookie attributes are configured.

- [ ] **Step 3: Implement cookie writing and consistent auth response**

```java
public class AuthResponse {

  private final String token;
  private final UsuarioDto usuario;

  public AuthResponse(String token, UsuarioDto usuario) {
    this.token = token;
    this.usuario = usuario;
  }
}
```

```java
@PostMapping("/registro")
public ResponseEntity<AuthResponse> registro(@Valid @RequestBody RegistroRequest request,
                                             HttpServletResponse response) {
  AuthResult result = authService.registro(request);
  response.addHeader(HttpHeaders.SET_COOKIE, result.refreshCookieHeader());
  return ResponseEntity.status(HttpStatus.CREATED).body(result.body());
}
```

```java
@ExceptionHandler(IllegalArgumentException.class)
public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
  ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.CONFLICT);
  detail.setDetail(ex.getMessage());
  return detail;
}
```

- [ ] **Step 4: Run the backend auth suite**

Run: `mvn "-Dtest=AuthControllerIntegrationTest" test`
Expected: PASS with JSON body `{ "token": "...", "usuario": { ... } }` and cookie `appcasa_refresh`.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/appcasa/application/auth/AuthService.java src/main/java/com/appcasa/application/auth/AuthResponse.java src/main/java/com/appcasa/infrastructure/api/AuthController.java src/main/java/com/appcasa/infrastructure/exception/GlobalExceptionHandler.java src/main/resources/application.yml src/test/java/com/appcasa/infrastructure/api/AuthControllerIntegrationTest.java
git commit -m "feat: issue auth refresh cookie on login and registration"
```

### Task 3: Refresh rotado y logout revocable

**Files:**
- Create: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\application\auth\RefreshRequestContext.java`
- Modify: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\application\auth\AuthService.java`
- Modify: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\infrastructure\api\AuthController.java`
- Test: `C:\dev\git\appcasa-backend\src\test\java\com\appcasa\infrastructure\api\AuthControllerIntegrationTest.java`

- [ ] **Step 1: Add failing tests for refresh rotation and logout**

```java
@Test
void refresh_rotatesCookieAndReturnsNewToken() throws Exception {
  MvcResult registro = registrarUsuario("refresh-ok@example.com");
  Cookie refreshCookie = registro.getResponse().getCookie("appcasa_refresh");

  mockMvc.perform(post("/api/v1/auth/refresh").cookie(refreshCookie))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.token").isString())
    .andExpect(jsonPath("$.usuario.email").value("refresh-ok@example.com"))
    .andExpect(cookie().exists("appcasa_refresh"));
}

@Test
void logout_revokesSessionAndRefreshFailsAfterwards() throws Exception {
  MvcResult registro = registrarUsuario("logout-ok@example.com");
  Cookie refreshCookie = registro.getResponse().getCookie("appcasa_refresh");

  mockMvc.perform(post("/api/v1/auth/logout").cookie(refreshCookie))
    .andExpect(status().isNoContent());

  mockMvc.perform(post("/api/v1/auth/refresh").cookie(refreshCookie))
    .andExpect(status().isUnauthorized());
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn "-Dtest=AuthControllerIntegrationTest#refresh_rotatesCookieAndReturnsNewToken+AuthControllerIntegrationTest#logout_revokesSessionAndRefreshFailsAfterwards" test`
Expected: FAIL because `/auth/refresh` and `/auth/logout` do not exist yet.

- [ ] **Step 3: Implement refresh rotation and logout**

```java
public AuthResult refresh(String rawRefreshToken) {
  RefreshTokenSession current = loadActiveSession(rawRefreshToken);
  current.setRevocadoEn(Instant.now());
  repository.save(current);

  return createSessionResult(current.getUsuario());
}

public void logout(String rawRefreshToken) {
  repository.findByTokenHash(hash(rawRefreshToken)).ifPresent(session -> {
    session.setRevocadoEn(Instant.now());
    repository.save(session);
  });
}
```

```java
@PostMapping("/refresh")
public ResponseEntity<AuthResponse> refresh(@CookieValue(name = "appcasa_refresh") String refreshToken,
                                            HttpServletResponse response) {
  AuthResult result = authService.refresh(refreshToken);
  response.addHeader(HttpHeaders.SET_COOKIE, result.refreshCookieHeader());
  return ResponseEntity.ok(result.body());
}

@PostMapping("/logout")
public ResponseEntity<Void> logout(@CookieValue(name = "appcasa_refresh", required = false) String refreshToken,
                                   HttpServletResponse response) {
  authService.logout(refreshToken);
  response.addHeader(HttpHeaders.SET_COOKIE, expiredRefreshCookie());
  return ResponseEntity.noContent().build();
}
```

- [ ] **Step 4: Run refresh/logout tests**

Run: `mvn "-Dtest=AuthControllerIntegrationTest" test`
Expected: PASS with rotation on refresh and `401` after logout using the previous cookie.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/appcasa/application/auth/RefreshRequestContext.java src/main/java/com/appcasa/application/auth/AuthService.java src/main/java/com/appcasa/infrastructure/api/AuthController.java src/test/java/com/appcasa/infrastructure/api/AuthControllerIntegrationTest.java
git commit -m "feat: add refresh rotation and logout revocation"
```

### Task 4: CORS, cookie config y regresion de seguridad

**Files:**
- Modify: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\infrastructure\security\SecurityConfig.java`
- Modify: `C:\dev\git\appcasa-backend\src\main\resources\application.yml`
- Test: `C:\dev\git\appcasa-backend\src\test\java\com\appcasa\infrastructure\security\ActuatorSecurityIntegrationTest.java`
- Test: `C:\dev\git\appcasa-backend\src\test\java\com\appcasa\infrastructure\api\AuthControllerIntegrationTest.java`

- [ ] **Step 1: Add a failing test for credentialed auth requests**

```java
@Test
void auth_refresh_allowsCredentialedRequestInLocalProfile() throws Exception {
  mockMvc.perform(options("/api/v1/auth/refresh")
      .header("Origin", "http://localhost:8100")
      .header("Access-Control-Request-Method", "POST"))
    .andExpect(status().isOk())
    .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn "-Dtest=AuthControllerIntegrationTest#auth_refresh_allowsCredentialedRequestInLocalProfile" test`
Expected: FAIL because allowed origins and credential settings are not environment-driven yet.

- [ ] **Step 3: Implement cookie and CORS properties**

```yaml
appcasa:
  security:
    cors:
      allowed-origins: http://localhost:8100,http://localhost:4200
    refresh-cookie:
      name: appcasa_refresh
      same-site: Lax
      secure: false
      path: /api/v1/auth
```

```java
cfg.setAllowedOrigins(List.of("http://localhost:8100", "http://localhost:4200"));
cfg.setAllowCredentials(true);
cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
```

- [ ] **Step 4: Run backend security regression suite**

Run: `mvn "-Dtest=AuthControllerIntegrationTest,ActuatorSecurityIntegrationTest" test`
Expected: PASS with auth CORS working and actuator rules unchanged.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/appcasa/infrastructure/security/SecurityConfig.java src/main/resources/application.yml src/test/java/com/appcasa/infrastructure/api/AuthControllerIntegrationTest.java src/test/java/com/appcasa/infrastructure/security/ActuatorSecurityIntegrationTest.java
git commit -m "feat: configure credentialed auth cors"
```

### Task 5: AuthService de frontend con refresh silencioso

**Files:**
- Create: `C:\dev\git\appcasa-frontend\src\app\core\services\auth.service.spec.ts`
- Modify: `C:\dev\git\appcasa-frontend\src\app\core\services\auth.service.ts`
- Modify: `C:\dev\git\appcasa-frontend\src\app\core\models\domain.models.ts`
- Modify: `C:\dev\git\appcasa-frontend\src\environments\environment.ts`
- Modify: `C:\dev\git\appcasa-frontend\src\environments\environment.prod.ts`

Run all commands in this task from: `C:\dev\git\appcasa-frontend`

- [ ] **Step 1: Write failing frontend service tests**

```ts
it('sends withCredentials on login and stores the token', () => {
  service.login({ email: 'ana@example.com', password: 'Password123' }).subscribe();

  const req = httpMock.expectOne('http://localhost:8080/api/v1/auth/login');
  expect(req.request.withCredentials).toBeTrue();

  req.flush({
    token: 'jwt-token',
    usuario: { id: '1', nombre: 'Ana', email: 'ana@example.com', tema: 'CLARO', locale: 'es-ES', idEstado: 1 }
  });

  expect(service.getToken()).toBe('jwt-token');
});

it('refreshes silently and updates the stored user', () => {
  service.refresh().subscribe();
  const req = httpMock.expectOne('http://localhost:8080/api/v1/auth/refresh');
  expect(req.request.withCredentials).toBeTrue();
});
```

- [ ] **Step 2: Run test to verify it fails**

Run: `npm test -- --watch=false --include src/app/core/services/auth.service.spec.ts`
Expected: FAIL because `refresh()` does not exist and `withCredentials` is not sent.

- [ ] **Step 3: Implement the auth client contract**

```ts
export interface AuthResponse {
  token: string;
  usuario: Usuario;
}

login(request: LoginRequest): Observable<AuthResponse> {
  return this.http.post<AuthResponse>(`${this.apiUrl}/login`, request, { withCredentials: true })
    .pipe(tap((res) => this.guardarSesion(res)));
}

refresh(): Observable<AuthResponse> {
  return this.http.post<AuthResponse>(`${this.apiUrl}/refresh`, {}, { withCredentials: true })
    .pipe(tap((res) => this.guardarSesion(res)));
}
```

- [ ] **Step 4: Run auth service tests**

Run: `npm test -- --watch=false --include src/app/core/services/auth.service.spec.ts`
Expected: PASS with service storing only `token` and `usuario`, never `refreshToken`.

- [ ] **Step 5: Commit**

```powershell
Set-Location C:\dev\git\appcasa-frontend
git add src/app/core/services/auth.service.ts src/app/core/services/auth.service.spec.ts src/app/core/models/domain.models.ts src/environments/environment.ts src/environments/environment.prod.ts
git commit -m "feat: add frontend auth refresh client"
```

### Task 6: Interceptor y guard sin bucles de refresh

**Files:**
- Create: `C:\dev\git\appcasa-frontend\src\app\core\interceptors\auth.interceptor.spec.ts`
- Modify: `C:\dev\git\appcasa-frontend\src\app\core\interceptors\auth.interceptor.ts`
- Modify: `C:\dev\git\appcasa-frontend\src\app\core\guards\auth.guard.ts`

Run all commands in this task from: `C:\dev\git\appcasa-frontend`

- [ ] **Step 1: Write failing interceptor tests**

```ts
it('retries the original request after a successful refresh', () => {
  httpClient.get('/api/secure').subscribe();

  const secure = httpMock.expectOne('/api/secure');
  secure.flush({}, { status: 401, statusText: 'Unauthorized' });

  const refresh = httpMock.expectOne('http://localhost:8080/api/v1/auth/refresh');
  refresh.flush({
    token: 'jwt-token-2',
    usuario: { id: '1', nombre: 'Ana', email: 'ana@example.com', tema: 'CLARO', locale: 'es-ES', idEstado: 1 }
  });

  const retried = httpMock.expectOne('/api/secure');
  expect(retried.request.headers.get('Authorization')).toBe('Bearer jwt-token-2');
});

it('logs out when refresh fails', () => {
  // first request 401 -> refresh 401 -> session cleared and router redirected to /auth
});
```

- [ ] **Step 2: Run test to verify it fails**

Run: `npm test -- --watch=false --include src/app/core/interceptors/auth.interceptor.spec.ts`
Expected: FAIL because the interceptor currently redirects immediately on `401`.

- [ ] **Step 3: Implement one-shot refresh and fallback logout**

```ts
if (error.status === 401 && !request.url.endsWith('/auth/refresh') && !request.headers.has('X-Refresh-Attempt')) {
  return this.authService.refresh().pipe(
    switchMap(() => next.handle(request.clone({
      setHeaders: { Authorization: `Bearer ${this.authService.getToken()}` },
      headers: request.headers.set('X-Refresh-Attempt', 'true'),
    }))),
    catchError((refreshError) => {
      this.authService.logout();
      this.router.navigate(['/auth']);
      return throwError(() => refreshError);
    })
  );
}
```

```ts
canActivate(): boolean {
  if (this.authService.isAuthenticated()) {
    return true;
  }
  this.router.navigate(['/auth']);
  return false;
}
```

- [ ] **Step 4: Run interceptor and guard tests**

Run: `npm test -- --watch=false --include src/app/core/interceptors/auth.interceptor.spec.ts`
Expected: PASS with a single refresh attempt and redirect only after refresh failure.

- [ ] **Step 5: Commit**

```powershell
Set-Location C:\dev\git\appcasa-frontend
git add src/app/core/interceptors/auth.interceptor.ts src/app/core/interceptors/auth.interceptor.spec.ts src/app/core/guards/auth.guard.ts
git commit -m "feat: refresh expired frontend sessions automatically"
```

### Task 7: Bootstrap silencioso y pantalla auth

**Files:**
- Modify: `C:\dev\git\appcasa-frontend\src\app\app.component.ts`
- Modify: `C:\dev\git\appcasa-frontend\src\app\features\auth\auth.page.ts`
- Test: `C:\dev\git\appcasa-frontend\src\app\core\services\auth.service.spec.ts`

Run all commands in this task from: `C:\dev\git\appcasa-frontend`

- [ ] **Step 1: Add a failing bootstrap test**

```ts
it('restores the session on app startup when refresh cookie is still valid', fakeAsync(() => {
  service.restoreSession().subscribe();

  const req = httpMock.expectOne('http://localhost:8080/api/v1/auth/refresh');
  req.flush({
    token: 'boot-token',
    usuario: { id: '1', nombre: 'Ana', email: 'ana@example.com', tema: 'CLARO', locale: 'es-ES', idEstado: 1 }
  });

  expect(service.isAuthenticated()).toBeTrue();
}));
```

- [ ] **Step 2: Run test to verify it fails**

Run: `npm test -- --watch=false --include src/app/core/services/auth.service.spec.ts`
Expected: FAIL because `restoreSession()` does not exist and startup does not attempt refresh.

- [ ] **Step 3: Implement startup restore and auth page cleanup**

```ts
restoreSession(): Observable<boolean> {
  if (this.getToken()) {
    return of(true);
  }

  return this.refresh().pipe(
    map(() => true),
    catchError(() => of(false))
  );
}
```

```ts
ngOnInit(): void {
  if (this.authService.isAuthenticated()) {
    this.router.navigate(['/dashboard'], { replaceUrl: true });
    return;
  }
  this.initForms();
}
```

```ts
ngOnInit(): void {
  this.authService.restoreSession().subscribe();
}
```

- [ ] **Step 4: Run the focused frontend auth suite**

Run: `npm test -- --watch=false --include src/app/core/services/auth.service.spec.ts --include src/app/core/interceptors/auth.interceptor.spec.ts`
Expected: PASS with silent restore and no refresh token stored in the browser.

- [ ] **Step 5: Commit**

```powershell
Set-Location C:\dev\git\appcasa-frontend
git add src/app/app.component.ts src/app/features/auth/auth.page.ts src/app/core/services/auth.service.ts src/app/core/services/auth.service.spec.ts src/app/core/interceptors/auth.interceptor.spec.ts
git commit -m "feat: restore auth session on app startup"
```

### Task 8: Verificacion final y documentacion operativa

**Files:**
- Modify: `C:\dev\git\appcasa-backend\README.md`
- Modify: `C:\dev\git\appcasa-frontend\README.md`

- [ ] **Step 1: Add a final manual verification checklist to both READMEs**

```md
## Flujo de autenticacion local

1. Arranca backend con perfil `local`
2. Arranca frontend con `npm run ionic:serve`
3. Registra un usuario nuevo desde `/auth`
4. Verifica que las llamadas a `/api/v1/auth/refresh` envian cookies
5. Verifica que `logout` invalida la sesion
```

- [ ] **Step 2: Run the full backend and frontend auth suites**

Run: `mvn test`
Expected: PASS including `AuthControllerIntegrationTest`, `ActuatorSecurityIntegrationTest` e `InvalidUuidIntegrationTest`.

Run from `C:\dev\git\appcasa-frontend`: `npm test -- --watch=false --include src/app/core/services/auth.service.spec.ts --include src/app/core/interceptors/auth.interceptor.spec.ts`
Expected: PASS in frontend auth suite.

- [ ] **Step 3: Smoke-test the full flow manually**

Run: `mvn spring-boot:run "-Dspring-boot.run.profiles=local"`
Expected: backend serving on `http://localhost:8080`.

Run from `C:\dev\git\appcasa-frontend`: `npm run ionic:serve`
Expected: frontend serving on `http://localhost:8100` or the Ionic local URL shown in console.

- [ ] **Step 4: Confirm the manual acceptance criteria**

```text
- Registro crea usuario y entra al dashboard
- Login devuelve al dashboard con token valido
- Refresh renueva sesion sin exponer refresh token al cliente
- Logout revoca la sesion y obliga a volver a /auth
```

- [ ] **Step 5: Commit**

```powershell
Set-Location C:\dev\git\appcasa-backend
git add README.md
git commit -m "docs: add backend auth verification steps"

Set-Location C:\dev\git\appcasa-frontend
git add README.md
git commit -m "docs: add frontend auth verification steps"
```

## Self-Review

- Spec coverage:
  - login y registro: Tasks 2, 5 y 7
  - refresh persistido y rotado: Tasks 1 y 3
  - cookie `HttpOnly` y CORS: Tasks 2 y 4
  - logout con revocacion: Task 3
  - refresh silencioso, interceptor y bootstrap: Tasks 5, 6 y 7
  - pruebas backend y frontend: Tasks 1 a 8
- Placeholder scan: no `TODO`, `TBD` ni referencias ambiguas; cada tarea incluye archivos, tests y comandos concretos.
- Type consistency:
  - respuesta backend/frontend fija `token` y `usuario`
  - cookie de refresh fija `appcasa_refresh`
  - endpoints fijos `/api/v1/auth/login`, `/registro`, `/refresh`, `/logout`
