# Identificadores Publicos API Migracion Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Aplicar una ruptura limpia del contrato para eliminar IDs tecnicos internos de catalogos/estados, introducir DTOs publicos y migrar primero el modulo de `tareas` extremo a extremo.

**Architecture:** Se mantiene el modelo interno actual (JPA + UUID + IDs de catalogo) pero se desacopla del contrato API mediante DTOs y mapeadores. El primer vertical de migracion sera `tareas`, incluyendo backend y frontend, para validar la regla antes de extenderla a `miembros` y `recordatorios`.

**Tech Stack:** Spring Boot 3.5, Spring MVC, Spring Security, JPA, MySQL/H2, Angular 21, Ionic 8, HttpClient, JUnit 5, MockMvc, Jasmine/Karma

---

## File Map

**Backend**
- Create: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\infrastructure\api\dto\catalogo\CodigoLabelDto.java`
- Create: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\infrastructure\api\dto\tarea\TareaPublicRequest.java`
- Create: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\infrastructure\api\dto\tarea\TareaPublicResponse.java`
- Create: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\infrastructure\api\dto\tarea\TareaAsignacionPublicResponse.java`
- Create: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\infrastructure\api\mapper\TareaPublicMapper.java`
- Create: `C:\dev\git\appcasa-backend\src\test\java\com\appcasa\infrastructure\api\TareaControllerContractTest.java`
- Modify: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\infrastructure\api\TareaController.java`
- Modify: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\application\tarea\TareaRequest.java`
- Modify: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\application\tarea\TareaServiceImpl.java`
- Modify: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\infrastructure\exception\GlobalExceptionHandler.java`

**Frontend**
- Modify: `C:\dev\git\appcasa-frontend\src\app\core\models\domain.models.ts`
- Modify: `C:\dev\git\appcasa-frontend\src\app\core\services\tarea.service.ts`
- Modify: `C:\dev\git\appcasa-frontend\src\app\features\tareas\tarea-form\tarea-form.page.ts`
- Modify: `C:\dev\git\appcasa-frontend\src\app\shared\components\badge-prioridad\badge-prioridad.component.ts`
- Create: `C:\dev\git\appcasa-frontend\src\app\core\services\tarea.service.spec.ts`
- Create: `C:\dev\git\appcasa-frontend\src\app\shared\components\badge-prioridad\badge-prioridad.component.spec.ts`

### Task 1: Definir DTOs publicos y test de contrato para tareas

**Files:**
- Create: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\infrastructure\api\dto\catalogo\CodigoLabelDto.java`
- Create: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\infrastructure\api\dto\tarea\TareaPublicResponse.java`
- Create: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\infrastructure\api\dto\tarea\TareaAsignacionPublicResponse.java`
- Create: `C:\dev\git\appcasa-backend\src\test\java\com\appcasa\infrastructure\api\TareaControllerContractTest.java`

- [ ] **Step 1: Write the failing contract test**

```java
@Test
void listarPendientes_doesNotExposeTechnicalIds() throws Exception {
  UUID idHogar = UUID.randomUUID();

  mockMvc.perform(get("/api/v1/tareas/hogar/{idHogar}/pendientes", idHogar)
      .header("Authorization", "Bearer " + tokenValido()))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$[0].id").isString())
    .andExpect(jsonPath("$[0].prioridad.codigo").isString())
    .andExpect(jsonPath("$[0].estado.codigo").isString())
    .andExpect(jsonPath("$[0].idPrioridad").doesNotExist())
    .andExpect(jsonPath("$[0].idEstado").doesNotExist())
    .andExpect(jsonPath("$[0].idHogar").doesNotExist());
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn "-Dtest=TareaControllerContractTest#listarPendientes_doesNotExposeTechnicalIds" test`  
Expected: FAIL because controller currently returns `Tarea` entity fields (`idPrioridad`, `idEstado`, `idHogar`).

- [ ] **Step 3: Create minimal public DTO classes**

```java
public record CodigoLabelDto(String codigo, String label) {}
```

```java
public record TareaPublicResponse(
  UUID id,
  String hogarCodigo,
  String titulo,
  String descripcion,
  CodigoLabelDto prioridad,
  String categoria,
  LocalDate fechaLimite,
  Instant fechaCompletada,
  CodigoLabelDto periodicidad,
  CodigoLabelDto estado,
  List<TareaAsignacionPublicResponse> asignaciones
) {}
```

- [ ] **Step 4: Run test to verify contract wiring compiles**

Run: `mvn "-Dtest=TareaControllerContractTest#listarPendientes_doesNotExposeTechnicalIds" test`  
Expected: still FAIL on assertions (controller not migrated yet), but compilation should pass with new DTOs.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/appcasa/infrastructure/api/dto/catalogo/CodigoLabelDto.java src/main/java/com/appcasa/infrastructure/api/dto/tarea/TareaPublicResponse.java src/main/java/com/appcasa/infrastructure/api/dto/tarea/TareaAsignacionPublicResponse.java src/test/java/com/appcasa/infrastructure/api/TareaControllerContractTest.java
git commit -m "test: add tarea public contract test and dto scaffolding"
```

### Task 2: Migrar controller de tareas a contrato publico

**Files:**
- Create: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\infrastructure\api\mapper\TareaPublicMapper.java`
- Modify: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\infrastructure\api\TareaController.java`
- Modify: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\application\tarea\TareaServiceImpl.java`
- Test: `C:\dev\git\appcasa-backend\src\test\java\com\appcasa\infrastructure\api\TareaControllerContractTest.java`

- [ ] **Step 1: Add failing test for all read endpoints**

```java
@Test
void obtener_usesPublicResponseShape() throws Exception {
  UUID id = UUID.randomUUID();
  mockMvc.perform(get("/api/v1/tareas/{id}", id).header("Authorization", "Bearer " + tokenValido()))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.prioridad.codigo").isString())
    .andExpect(jsonPath("$.idPrioridad").doesNotExist())
    .andExpect(jsonPath("$.idEstado").doesNotExist());
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `mvn "-Dtest=TareaControllerContractTest" test`  
Expected: FAIL on missing `prioridad.codigo` and leaked technical fields.

- [ ] **Step 3: Implement mapper and controller response migration**

```java
@Component
public class TareaPublicMapper {

  public TareaPublicResponse toResponse(Tarea tarea) {
    return new TareaPublicResponse(
      tarea.getId(),
      resolverHogarCodigo(tarea.getIdHogar()),
      tarea.getTitulo(),
      tarea.getDescripcion(),
      prioridadDto(tarea.getIdPrioridad()),
      tarea.getCategoria(),
      tarea.getFechaLimite(),
      tarea.getFechaCompletada(),
      periodicidadDto(tarea.getPeriodicidad()),
      estadoDto(tarea.getIdEstado()),
      List.of()
    );
  }
}
```

```java
@GetMapping("/hogar/{hogarCodigo}/pendientes")
public ResponseEntity<List<TareaPublicResponse>> listarPendientes(@PathVariable String hogarCodigo) {
  UUID idHogar = resolverIdHogarPorCodigo(hogarCodigo);
  return ResponseEntity.ok(tareaService.listarPendientes(idHogar).stream().map(tareaMapper::toResponse).toList());
}
```

- [ ] **Step 4: Run backend contract tests**

Run: `mvn "-Dtest=TareaControllerContractTest" test`  
Expected: PASS with no `idPrioridad`, `idEstado`, `idHogar` in payloads.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/appcasa/infrastructure/api/mapper/TareaPublicMapper.java src/main/java/com/appcasa/infrastructure/api/TareaController.java src/main/java/com/appcasa/application/tarea/TareaServiceImpl.java src/test/java/com/appcasa/infrastructure/api/TareaControllerContractTest.java
git commit -m "feat: expose tareas through public response dto"
```

### Task 3: Migrar request de tareas a codigos publicos

**Files:**
- Create: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\infrastructure\api\dto\tarea\TareaPublicRequest.java`
- Modify: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\application\tarea\TareaRequest.java`
- Modify: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\application\tarea\TareaServiceImpl.java`
- Modify: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\infrastructure\api\TareaController.java`
- Test: `C:\dev\git\appcasa-backend\src\test\java\com\appcasa\infrastructure\api\TareaControllerContractTest.java`

- [ ] **Step 1: Write failing create/update request contract test**

```java
@Test
void crear_usesCodigosInsteadOfTechnicalIds() throws Exception {
  mockMvc.perform(post("/api/v1/tareas")
      .header("Authorization", "Bearer " + tokenValido())
      .contentType(MediaType.APPLICATION_JSON)
      .content("""
        {"hogarCodigo":"CASA1234","titulo":"Comprar pienso","prioridadCodigo":"ALTA","esPersonal":false}
      """))
    .andExpect(status().isCreated())
    .andExpect(jsonPath("$.prioridad.codigo").value("ALTA"));
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn "-Dtest=TareaControllerContractTest#crear_usesCodigosInsteadOfTechnicalIds" test`  
Expected: FAIL because request currently expects `idHogar` and `idPrioridad`.

- [ ] **Step 3: Implement public request mapping**

```java
public record TareaPublicRequest(
  String hogarCodigo,
  String titulo,
  String descripcion,
  String prioridadCodigo,
  String categoria,
  LocalDate fechaLimite,
  Boolean esPeriodica,
  String periodicidadCodigo,
  Boolean esPersonal,
  List<UUID> miembroIds
) {}
```

```java
TareaRequest internal = TareaRequest.builder()
  .idHogar(resolverIdHogar(publicRequest.hogarCodigo()))
  .idPrioridad(resolverIdPrioridad(publicRequest.prioridadCodigo()))
  .periodicidad(resolverPeriodicidad(publicRequest.periodicidadCodigo()))
  .build();
```

- [ ] **Step 4: Run contract tests**

Run: `mvn "-Dtest=TareaControllerContractTest" test`  
Expected: PASS for create/update payloads with `...Codigo`.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/appcasa/infrastructure/api/dto/tarea/TareaPublicRequest.java src/main/java/com/appcasa/application/tarea/TareaRequest.java src/main/java/com/appcasa/application/tarea/TareaServiceImpl.java src/main/java/com/appcasa/infrastructure/api/TareaController.java src/test/java/com/appcasa/infrastructure/api/TareaControllerContractTest.java
git commit -m "feat: accept public codigo-based tarea requests"
```

### Task 4: Estandarizar errores de contrato para IDs tecnicos prohibidos

**Files:**
- Modify: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\infrastructure\exception\GlobalExceptionHandler.java`
- Test: `C:\dev\git\appcasa-backend\src\test\java\com\appcasa\infrastructure\api\TareaControllerContractTest.java`

- [ ] **Step 1: Add failing validation/error test**

```java
@Test
void crear_rejectsLegacyTechnicalFields() throws Exception {
  mockMvc.perform(post("/api/v1/tareas")
      .header("Authorization", "Bearer " + tokenValido())
      .contentType(MediaType.APPLICATION_JSON)
      .content("""
        {"idHogar":"...","titulo":"X","idPrioridad":3}
      """))
    .andExpect(status().isBadRequest())
    .andExpect(jsonPath("$.detail").value("El contrato publico no admite campos tecnicos legacy"));
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn "-Dtest=TareaControllerContractTest#crear_rejectsLegacyTechnicalFields" test`  
Expected: FAIL because legacy fields are currently ignored or mapped implicitly.

- [ ] **Step 3: Implement strict request validation**

```java
objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
```

```java
@ExceptionHandler(HttpMessageNotReadableException.class)
public ProblemDetail handleUnreadable(HttpMessageNotReadableException ex) {
  ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
  pd.setDetail("El contrato publico no admite campos tecnicos legacy");
  return pd;
}
```

- [ ] **Step 4: Run backend contract suite**

Run: `mvn "-Dtest=TareaControllerContractTest" test`  
Expected: PASS including explicit 400 on legacy fields.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/appcasa/infrastructure/exception/GlobalExceptionHandler.java src/test/java/com/appcasa/infrastructure/api/TareaControllerContractTest.java
git commit -m "fix: reject legacy technical fields in public contract"
```

### Task 5: Migrar modelos y servicio de tareas en frontend a codigos

**Files:**
- Modify: `C:\dev\git\appcasa-frontend\src\app\core\models\domain.models.ts`
- Modify: `C:\dev\git\appcasa-frontend\src\app\core\services\tarea.service.ts`
- Create: `C:\dev\git\appcasa-frontend\src\app\core\services\tarea.service.spec.ts`

Run all commands in this task from: `C:\dev\git\appcasa-frontend`

- [ ] **Step 1: Write failing service serialization test**

```ts
it('sends tarea request using codigos and no technical ids', () => {
  service.crear({
    hogarCodigo: 'CASA1234',
    titulo: 'Comprar pienso',
    prioridadCodigo: 'ALTA',
    esPersonal: false
  }).subscribe();

  const req = httpMock.expectOne('http://localhost:8080/api/v1/tareas');
  expect(req.request.body.prioridadCodigo).toBe('ALTA');
  expect(req.request.body.idPrioridad).toBeUndefined();
  expect(req.request.body.idHogar).toBeUndefined();
});
```

- [ ] **Step 2: Run test to verify it fails**

Run: `npm test -- --watch=false --include src/app/core/services/tarea.service.spec.ts`  
Expected: FAIL because interface still uses `idHogar` and `idPrioridad`.

- [ ] **Step 3: Implement model and service contract migration**

```ts
export interface TareaRequest {
  hogarCodigo: string;
  titulo: string;
  descripcion?: string;
  prioridadCodigo?: 'BAJA' | 'MEDIA' | 'ALTA' | 'URGENTE';
  categoria?: string;
  fechaLimite?: string;
  periodicidadCodigo?: 'DIARIA' | 'SEMANAL' | 'MENSUAL' | 'ANUAL';
  esPersonal?: boolean;
  miembroIds?: string[];
}
```

```ts
listarPendientes(hogarCodigo: string): Observable<Tarea[]> {
  return this.http.get<Tarea[]>(`${this.apiUrl}/hogar/${hogarCodigo}/pendientes`);
}
```

- [ ] **Step 4: Run frontend tarea service tests**

Run: `npm test -- --watch=false --include src/app/core/services/tarea.service.spec.ts`  
Expected: PASS using only codigo-based request fields.

- [ ] **Step 5: Commit**

```powershell
Set-Location C:\dev\git\appcasa-frontend
git add src/app/core/models/domain.models.ts src/app/core/services/tarea.service.ts src/app/core/services/tarea.service.spec.ts
git commit -m "feat: migrate frontend tarea service to public codigo contract"
```

### Task 6: Adaptar UI de tareas (formulario y prioridad) a codigos

**Files:**
- Modify: `C:\dev\git\appcasa-frontend\src\app\features\tareas\tarea-form\tarea-form.page.ts`
- Modify: `C:\dev\git\appcasa-frontend\src\app\shared\components\badge-prioridad\badge-prioridad.component.ts`
- Create: `C:\dev\git\appcasa-frontend\src\app\shared\components\badge-prioridad\badge-prioridad.component.spec.ts`

Run all commands in this task from: `C:\dev\git\appcasa-frontend`

- [ ] **Step 1: Write failing badge/component test**

```ts
it('renders label and color by prioridad codigo', () => {
  component.prioridadCodigo = 'ALTA';
  fixture.detectChanges();
  expect(fixture.nativeElement.textContent).toContain('Alta');
});
```

- [ ] **Step 2: Run test to verify it fails**

Run: `npm test -- --watch=false --include src/app/shared/components/badge-prioridad/badge-prioridad.component.spec.ts`  
Expected: FAIL because component still expects `idPrioridad` number.

- [ ] **Step 3: Implement codigo-based UI mapping**

```ts
const CONFIG: Record<string, { label: string; color: string }> = {
  BAJA: { label: 'Baja', color: 'success' },
  MEDIA: { label: 'Media', color: 'warning' },
  ALTA: { label: 'Alta', color: 'danger' },
  URGENTE: { label: 'Urgente', color: 'danger' },
};

@Input() prioridadCodigo: string = 'BAJA';
```

```ts
readonly prioridades = [
  { codigo: 'BAJA', label: 'Baja' },
  { codigo: 'MEDIA', label: 'Media' },
  { codigo: 'ALTA', label: 'Alta' },
  { codigo: 'URGENTE', label: 'Urgente' },
];
```

- [ ] **Step 4: Run focused UI tests**

Run: `npm test -- --watch=false --include src/app/shared/components/badge-prioridad/badge-prioridad.component.spec.ts --include src/app/core/services/tarea.service.spec.ts`  
Expected: PASS with UI fully detached from `idPrioridad`.

- [ ] **Step 5: Commit**

```powershell
Set-Location C:\dev\git\appcasa-frontend
git add src/app/features/tareas/tarea-form/tarea-form.page.ts src/app/shared/components/badge-prioridad/badge-prioridad.component.ts src/app/shared/components/badge-prioridad/badge-prioridad.component.spec.ts
git commit -m "refactor: switch tarea priority ui to codigo semantics"
```

### Task 7: Verificacion final de ruptura limpia

**Files:**
- Modify: `C:\dev\git\appcasa-backend\README.md`
- Modify: `C:\dev\git\appcasa-frontend\README.md`

- [ ] **Step 1: Add contract notes in READMEs**

```md
## Contrato publico de identificadores

- No usar `idPrioridad`, `idTipoMiembro`, `idTipoRecordatorio`, `idEstado` en requests/responses.
- Usar `codigo` para catalogos y `id` para agregados principales.
```

- [ ] **Step 2: Run full backend tests**

Run: `mvn test`  
Expected: PASS with `TareaControllerContractTest`, auth tests y regresiones previas.

- [ ] **Step 3: Run frontend tests and build**

Run from `C:\dev\git\appcasa-frontend`: `npm test -- --watch=false`  
Expected: PASS in frontend suite.

Run from `C:\dev\git\appcasa-frontend`: `npm run build`  
Expected: PASS and generated chunks without TypeScript errors.

- [ ] **Step 4: Execute manual smoke test**

```text
1. Crear tarea enviando prioridadCodigo y hogarCodigo.
2. Listar pendientes y verificar ausencia de idPrioridad/idEstado/idHogar.
3. Completar tarea y verificar estado por codigo en respuesta.
```

- [ ] **Step 5: Commit**

```powershell
Set-Location C:\dev\git\appcasa-backend
git add README.md
git commit -m "docs: document public identifier contract"

Set-Location C:\dev\git\appcasa-frontend
git add README.md
git commit -m "docs: document frontend public identifier contract"
```

## Self-Review
- Spec coverage:
  - regla transversal de identificadores publicos: Tasks 1, 2, 3 y 4
  - eliminacion de IDs tecnicos en contrato: Tasks 1, 2 y 4
  - migracion inicial por modulo (`tareas`): Tasks 2, 3, 5 y 6
  - validacion backend/frontend: Task 7
- Placeholder scan: no hay `TODO` ni pasos ambiguos; cada tarea define archivos, tests, comandos y commit.
- Type consistency:
  - `...Codigo` para catalogos en backend y frontend
  - `id`/`...Id` solo para agregados principales
  - endpoints de tareas alineados con `hogarCodigo` en ruta y request
