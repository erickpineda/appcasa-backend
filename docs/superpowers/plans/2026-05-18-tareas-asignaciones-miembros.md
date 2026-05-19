# Tareas Asignaciones Miembros Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Permitir asignar miembros reales a las tareas, persistir esas asignaciones en backend, devolverlas en el contrato publico y gestionarlas desde crear/editar en frontend.

**Architecture:** La tarea sigue siendo el agregado principal y las asignaciones se gestionan mediante una tabla puente `TB_TAREA_ASIGNACION` mapeada con una entidad JPA dedicada. El backend reemplaza siempre el conjunto completo de asignaciones recibido y el frontend aplica la misma regla de negocio que el servidor: si hay miembros seleccionados, la tarea deja de ser personal; si el usuario marca `Solo para mi`, se limpian las asignaciones.

**Tech Stack:** Spring Boot 3.5, Spring MVC, Spring Data JPA, H2/MySQL, Angular 21, Ionic 8, HttpClient, JUnit 5, MockMvc, Jasmine/Karma

---

## File Map

**Backend**
- Create: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\domain\tarea\TareaAsignacion.java`
- Create: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\domain\tarea\TareaAsignacionRepository.java`
- Modify: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\application\tarea\TareaServiceImpl.java`
- Modify: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\infrastructure\api\mapper\TareaPublicMapper.java`
- Modify: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\domain\miembro\MiembroHogarRepository.java`
- Modify: `C:\dev\git\appcasa-backend\src\test\java\com\appcasa\infrastructure\api\TareaControllerContractTest.java`

**Frontend**
- Modify: `C:\dev\git\appcasa-frontend\src\app\core\services\tarea.service.ts`
- Modify: `C:\dev\git\appcasa-frontend\src\app\features\tareas\tarea-form\tarea-form.page.ts`
- Modify: `C:\dev\git\appcasa-frontend\src\app\features\tareas\tarea-form\tarea-form.page.html`
- Modify: `C:\dev\git\appcasa-frontend\src\app\shared\components\tarjeta-tarea\tarjeta-tarea.component.ts`
- Modify: `C:\dev\git\appcasa-frontend\src\app\core\services\miembro.service.ts`
- Create: `C:\dev\git\appcasa-frontend\src\app\features\tareas\tarea-form\tarea-form.page.spec.ts`
- Modify: `C:\dev\git\appcasa-frontend\src\app\shared\components\tarjeta-tarea\tarjeta-tarea.component.spec.ts`

### Task 1: Persistencia de asignaciones en backend

**Files:**
- Create: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\domain\tarea\TareaAsignacion.java`
- Create: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\domain\tarea\TareaAsignacionRepository.java`
- Modify: `C:\dev\git\appcasa-backend\src\test\java\com\appcasa\infrastructure\api\TareaControllerContractTest.java`

- [ ] **Step 1: Write the failing backend contract test for create with miembroIds**

```java
@Test
void crear_conMiembros_validos_persisteAsignaciones() throws Exception {
  MiembroHogar miembro = miembroRepository.save(MiembroHogar.builder()
    .idHogar(hogar.getId())
    .idTipoMiembro(1)
    .nombre("Lucia")
    .build());

  mockMvc.perform(post("/api/v1/tareas")
      .header("Authorization", "Bearer " + token)
      .contentType(MediaType.APPLICATION_JSON)
      .content("""
        {
          "hogarCodigo":"%s",
          "titulo":"Sacar al perro",
          "prioridadCodigo":"MEDIA",
          "esPersonal":true,
          "miembroIds":["%s"]
        }
        """.formatted(hogar.getCodigo(), miembro.getId())))
    .andExpect(status().isCreated())
    .andExpect(jsonPath("$.asignaciones[0].miembroId").value(miembro.getId().toString()))
    .andExpect(jsonPath("$.asignaciones[0].nombreMiembro").value("Lucia"))
    .andExpect(jsonPath("$.esPersonal").value(false));
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn "-Dtest=TareaControllerContractTest#crear_conMiembros_validos_persisteAsignaciones" test`  
Expected: FAIL porque no existe entidad/repo de asignaciones y el mapper sigue devolviendo `List.of()`.

- [ ] **Step 3: Add the JPA entity and repository**

```java
@Entity
@Table(name = "TB_TAREA_ASIGNACION")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TareaAsignacion {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "id_tarea", nullable = false)
  private UUID idTarea;

  @Column(name = "id_miembro", nullable = false)
  private UUID idMiembro;

  private Boolean aceptada;
}
```

```java
@Repository
public interface TareaAsignacionRepository extends JpaRepository<TareaAsignacion, UUID> {
  List<TareaAsignacion> findByIdTarea(UUID idTarea);
  void deleteByIdTarea(UUID idTarea);
}
```

- [ ] **Step 4: Run test to verify it still fails for service behavior only**

Run: `mvn "-Dtest=TareaControllerContractTest#crear_conMiembros_validos_persisteAsignaciones" test`  
Expected: FAIL on response assertions only; compilation should now pass.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/appcasa/domain/tarea/TareaAsignacion.java src/main/java/com/appcasa/domain/tarea/TareaAsignacionRepository.java src/test/java/com/appcasa/infrastructure/api/TareaControllerContractTest.java
git commit -m "feat: add tarea assignment persistence primitives"
```

### Task 2: Servicio de tareas con validacion y reemplazo de asignaciones

**Files:**
- Modify: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\application\tarea\TareaServiceImpl.java`
- Modify: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\domain\miembro\MiembroHogarRepository.java`
- Modify: `C:\dev\git\appcasa-backend\src\test\java\com\appcasa\infrastructure\api\TareaControllerContractTest.java`

- [ ] **Step 1: Write failing tests for replace and invalid miembro**

```java
@Test
void actualizar_reemplazaAsignacionesPrevias() throws Exception {
  MiembroHogar miembro1 = crearMiembro(hogar.getId(), "Lucia");
  MiembroHogar miembro2 = crearMiembro(hogar.getId(), "Rafa");
  Tarea creada = tareaRepository.save(Tarea.builder().idHogar(hogar.getId()).titulo("Planchar").idPrioridad(1).build());
  asignacionRepository.save(TareaAsignacion.builder().idTarea(creada.getId()).idMiembro(miembro1.getId()).build());

  mockMvc.perform(put("/api/v1/tareas/{id}", creada.getId())
      .header("Authorization", "Bearer " + token)
      .contentType(MediaType.APPLICATION_JSON)
      .content("""
        {"hogarCodigo":"%s","titulo":"Planchar","prioridadCodigo":"BAJA","miembroIds":["%s"]}
        """.formatted(hogar.getCodigo(), miembro2.getId())))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.asignaciones.length()").value(1))
    .andExpect(jsonPath("$.asignaciones[0].nombreMiembro").value("Rafa"));
}

@Test
void crear_conMiembroDeOtroHogar_devuelveBadRequest() throws Exception {
  Hogar otroHogar = hogarRepository.save(Hogar.builder().nombre("Otro").codigo("OTRO1234").idEstado(1).build());
  MiembroHogar externo = crearMiembro(otroHogar.getId(), "Ajeno");

  mockMvc.perform(post("/api/v1/tareas")
      .header("Authorization", "Bearer " + token)
      .contentType(MediaType.APPLICATION_JSON)
      .content("""
        {"hogarCodigo":"%s","titulo":"X","prioridadCodigo":"MEDIA","miembroIds":["%s"]}
        """.formatted(hogar.getCodigo(), externo.getId())))
    .andExpect(status().isBadRequest());
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `mvn "-Dtest=TareaControllerContractTest#actualizar_reemplazaAsignacionesPrevias+crear_conMiembroDeOtroHogar_devuelveBadRequest" test`  
Expected: FAIL because `TareaServiceImpl` neither valida hogar de miembros ni reemplaza asignaciones.

- [ ] **Step 3: Implement assignment validation and replacement**

```java
public interface MiembroHogarRepository extends JpaRepository<MiembroHogar, UUID> {
  List<MiembroHogar> findByIdHogarAndIdEstado(UUID idHogar, Integer idEstado);
  List<MiembroHogar> findByIdIn(List<UUID> ids);
}
```

```java
private void reemplazarAsignaciones(Tarea tarea, List<UUID> miembroIds) {
  tareaAsignacionRepository.deleteByIdTarea(tarea.getId());

  if (miembroIds == null || miembroIds.isEmpty()) {
    return;
  }

  List<UUID> idsUnicos = miembroIds.stream().distinct().toList();
  List<MiembroHogar> miembros = miembroRepository.findByIdIn(idsUnicos);

  if (miembros.size() != idsUnicos.size() || miembros.stream().anyMatch(m -> !m.getIdHogar().equals(tarea.getIdHogar()))) {
    throw new IllegalArgumentException("Los miembros asignados no pertenecen al hogar de la tarea");
  }

  tareaAsignacionRepository.saveAll(idsUnicos.stream()
    .map(idMiembro -> TareaAsignacion.builder().idTarea(tarea.getId()).idMiembro(idMiembro).build())
    .toList());
}
```

```java
boolean tieneMiembros = request.getIdsMiembros() != null && !request.getIdsMiembros().isEmpty();
tarea.setEsPersonal(tieneMiembros ? false : Boolean.TRUE.equals(request.getEsPersonal()));
```

- [ ] **Step 4: Run focused backend contract tests**

Run: `mvn "-Dtest=TareaControllerContractTest" test`  
Expected: PASS para crear/actualizar con asignaciones, validacion de hogar y regla `esPersonal = false`.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/appcasa/application/tarea/TareaServiceImpl.java src/main/java/com/appcasa/domain/miembro/MiembroHogarRepository.java src/test/java/com/appcasa/infrastructure/api/TareaControllerContractTest.java
git commit -m "feat: validate and replace tarea member assignments"
```

### Task 3: Exponer asignaciones reales en el DTO publico

**Files:**
- Modify: `C:\dev\git\appcasa-backend\src\main\java\com\appcasa\infrastructure\api\mapper\TareaPublicMapper.java`
- Modify: `C:\dev\git\appcasa-backend\src\test\java\com\appcasa\infrastructure\api\TareaControllerContractTest.java`

- [ ] **Step 1: Add a failing read contract test**

```java
@Test
void listarPendientes_incluyeNombresDeMiembrosAsignados() throws Exception {
  MiembroHogar miembro = crearMiembro(hogar.getId(), "Lucia");
  asignacionRepository.save(TareaAsignacion.builder().idTarea(tarea.getId()).idMiembro(miembro.getId()).build());

  mockMvc.perform(get("/api/v1/tareas/hogar/{hogarCodigo}/pendientes", hogar.getCodigo())
      .header("Authorization", "Bearer " + token))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$[0].asignaciones[0].miembroId").value(miembro.getId().toString()))
    .andExpect(jsonPath("$[0].asignaciones[0].nombreMiembro").value("Lucia"));
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn "-Dtest=TareaControllerContractTest#listarPendientes_incluyeNombresDeMiembrosAsignados" test`  
Expected: FAIL because the public mapper still returns an empty list.

- [ ] **Step 3: Load and map assignments in the public mapper**

```java
public class TareaPublicMapper {
  private final HogarRepository hogarRepository;
  private final TareaAsignacionRepository tareaAsignacionRepository;
  private final MiembroHogarRepository miembroRepository;

  public TareaPublicResponse toResponse(Tarea tarea) {
    List<TareaAsignacionPublicResponse> asignaciones = tareaAsignacionRepository.findByIdTarea(tarea.getId()).stream()
      .map(asignacion -> miembroRepository.findById(asignacion.getIdMiembro())
        .map(miembro -> new TareaAsignacionPublicResponse(miembro.getId(), miembro.getNombre()))
        .orElse(null))
      .filter(Objects::nonNull)
      .toList();

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
      Boolean.TRUE.equals(tarea.getEsPersonal()),
      estadoDto(tarea.getIdEstado()),
      asignaciones
    );
  }
}
```

- [ ] **Step 4: Run backend contract tests**

Run: `mvn "-Dtest=TareaControllerContractTest" test`  
Expected: PASS with populated `asignaciones` in create, get and list responses.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/appcasa/infrastructure/api/mapper/TareaPublicMapper.java src/test/java/com/appcasa/infrastructure/api/TareaControllerContractTest.java
git commit -m "feat: expose tarea assignments in public responses"
```

### Task 4: Cargar miembros y aplicar reglas de negocio en el formulario

**Files:**
- Modify: `C:\dev\git\appcasa-frontend\src\app\core\services\miembro.service.ts`
- Modify: `C:\dev\git\appcasa-frontend\src\app\features\tareas\tarea-form\tarea-form.page.ts`
- Modify: `C:\dev\git\appcasa-frontend\src\app\features\tareas\tarea-form\tarea-form.page.html`
- Create: `C:\dev\git\appcasa-frontend\src\app\features\tareas\tarea-form\tarea-form.page.spec.ts`

Run all commands in this task from: `C:\dev\git\appcasa-frontend`

- [ ] **Step 1: Write the failing form behavior test**

```ts
it('clears esPersonal when members are selected and clears members when esPersonal is enabled', fakeAsync(() => {
  component.form.patchValue({ esPersonal: true, miembroIds: [] });
  component.onMiembrosChange(['m1']);
  expect(component.form.value.esPersonal).toBeFalse();

  component.form.patchValue({ miembroIds: ['m1'] });
  component.onEsPersonalChange(true);
  expect(component.form.value.miembroIds).toEqual([]);
}));
```

- [ ] **Step 2: Run test to verify it fails**

Run: `npm test -- --watch=false --include src/app/features/tareas/tarea-form/tarea-form.page.spec.ts`  
Expected: FAIL because the form currently has no `miembroIds` control and no rule handlers.

- [ ] **Step 3: Implement member loading and form rules**

```ts
export interface MiembroOpcion {
  id: string;
  nombre: string;
}
```

```ts
this.form = this.fb.group({
  titulo: ['',[Validators.required, Validators.maxLength(200)]],
  descripcion: [''],
  prioridadCodigo: ['BAJA'],
  categoria: [''],
  fechaLimite: [''],
  esPeriodica: [false],
  periodicidadCodigo: [null],
  esPersonal: [false],
  miembroIds: [[]],
});
```

```ts
onMiembrosChange(ids: string[]): void {
  this.form.patchValue({
    miembroIds: ids,
    esPersonal: ids.length > 0 ? false : this.form.value.esPersonal,
  }, { emitEvent: false });
}

onEsPersonalChange(enabled: boolean): void {
  if (enabled) {
    this.form.patchValue({ esPersonal: true, miembroIds: [] }, { emitEvent: false });
  }
}
```

```ts
private cargarMiembros(): void {
  const hogarCodigo = this.hogarService.hogarActual?.codigo ?? '';
  if (!hogarCodigo) { return; }

  this.miembroService.listarPorHogar(hogarCodigo).subscribe({
    next: (miembros) => { this.miembros = miembros; },
  });
}
```

- [ ] **Step 4: Run focused frontend form tests**

Run: `npm test -- --watch=false --include src/app/features/tareas/tarea-form/tarea-form.page.spec.ts`  
Expected: PASS for rule syncing and payload preparation with `miembroIds`.

- [ ] **Step 5: Commit**

```powershell
Set-Location C:\dev\git\appcasa-frontend
git add src/app/core/services/miembro.service.ts src/app/features/tareas/tarea-form/tarea-form.page.ts src/app/features/tareas/tarea-form/tarea-form.page.html src/app/features/tareas/tarea-form/tarea-form.page.spec.ts
git commit -m "feat: add member assignment selector to tarea form"
```

### Task 5: Precarga de asignaciones en edicion y render visible en tarjeta

**Files:**
- Modify: `C:\dev\git\appcasa-frontend\src\app\features\tareas\tarea-form\tarea-form.page.ts`
- Modify: `C:\dev\git\appcasa-frontend\src\app\shared\components\tarjeta-tarea\tarjeta-tarea.component.ts`
- Modify: `C:\dev\git\appcasa-frontend\src\app\shared\components\tarjeta-tarea\tarjeta-tarea.component.spec.ts`

Run all commands in this task from: `C:\dev\git\appcasa-frontend`

- [ ] **Step 1: Write failing render and edit preload tests**

```ts
it('preloads miembroIds from tarea.asignaciones on edit', () => {
  tareaService.obtener.and.returnValue(of({
    id: 't1',
    hogarCodigo: 'CASA1234',
    titulo: 'Comprar pienso',
    prioridad: { codigo: 'ALTA', label: 'Alta' },
    estado: { codigo: 'ACTIVA', label: 'Activa' },
    esPersonal: false,
    asignaciones: [{ miembroId: 'm1', nombreMiembro: 'Lucia' }]
  } as Tarea));

  component['cargarTarea']('t1');
  expect(component.form.value.miembroIds).toEqual(['m1']);
});

it('renders assigned member names', () => {
  component.tarea = {
    id: 't1',
    hogarCodigo: 'CASA1234',
    titulo: 'Comprar pienso',
    prioridad: { codigo: 'ALTA', label: 'Alta' },
    estado: { codigo: 'ACTIVA', label: 'Activa' },
    esPeriodica: false,
    esPersonal: false,
    asignaciones: [{ miembroId: 'm1', nombreMiembro: 'Lucia' }]
  } as Tarea;
  fixture.detectChanges();
  expect(fixture.nativeElement.textContent).toContain('Lucia');
});
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `npm test -- --watch=false --include src/app/features/tareas/tarea-form/tarea-form.page.spec.ts --include src/app/shared/components/tarjeta-tarea/tarjeta-tarea.component.spec.ts`  
Expected: FAIL because edit mode does not patch `miembroIds` and the card does not render names.

- [ ] **Step 3: Implement preload and visible assigned names**

```ts
this.form.patchValue({
  titulo: tarea.titulo,
  descripcion: tarea.descripcion,
  prioridadCodigo: tarea.prioridad?.codigo ?? 'BAJA',
  categoria: tarea.categoria,
  fechaLimite: tarea.fechaLimite,
  esPeriodica: tarea.esPeriodica,
  periodicidadCodigo: tarea.periodicidad?.codigo ?? null,
  esPersonal: tarea.esPersonal,
  miembroIds: tarea.asignaciones?.map(a => a.miembroId) ?? [],
});
```

```ts
template: `
  <ion-item ...>
    <ion-label>
      <h3 ...>{{ tarea.titulo }}</h3>
      @if (tarea.asignaciones?.length) {
        <p class="asignados">
          <ion-icon name="people-outline"></ion-icon>
          {{ tarea.asignaciones.map(a => a.nombreMiembro).join(', ') }}
        </p>
      }
    </ion-label>
    <app-badge-prioridad ...></app-badge-prioridad>
  </ion-item>
`
```

- [ ] **Step 4: Run focused UI tests**

Run: `npm test -- --watch=false --include src/app/features/tareas/tarea-form/tarea-form.page.spec.ts --include src/app/shared/components/tarjeta-tarea/tarjeta-tarea.component.spec.ts`  
Expected: PASS with edit preload and names visible in the card.

- [ ] **Step 5: Commit**

```powershell
Set-Location C:\dev\git\appcasa-frontend
git add src/app/features/tareas/tarea-form/tarea-form.page.ts src/app/shared/components/tarjeta-tarea/tarjeta-tarea.component.ts src/app/shared/components/tarjeta-tarea/tarjeta-tarea.component.spec.ts
git commit -m "feat: show assigned member names in tareas ui"
```

### Task 6: Verificacion final de backend y frontend

**Files:**
- Modify: `C:\dev\git\appcasa-backend\README.md`
- Modify: `C:\dev\git\appcasa-frontend\README.md`

- [ ] **Step 1: Document assignment behavior in READMEs**

```md
## Tareas y asignaciones

- Las tareas aceptan `miembroIds` en el contrato publico.
- Si hay miembros asignados, `esPersonal` pasa a `false`.
- Las respuestas de tareas incluyen `asignaciones` con `miembroId` y `nombreMiembro`.
```

- [ ] **Step 2: Run full backend test suite**

Run: `mvn test`  
Expected: PASS incluyendo `TareaControllerContractTest`, `InvalidUuidIntegrationTest` y regresiones previas.

- [ ] **Step 3: Run full frontend suite and build**

Run from `C:\dev\git\appcasa-frontend`: `npm test -- --watch=false`  
Expected: PASS incluyendo specs nuevas de `tarea-form` y `tarjeta-tarea`.

Run from `C:\dev\git\appcasa-frontend`: `npm run build`  
Expected: PASS sin errores de tipos ni plantilla.

- [ ] **Step 4: Execute manual smoke test**

```text
1. Abrir nueva tarea y cargar miembros del hogar.
2. Seleccionar uno o varios miembros y comprobar que "Solo para mi" se desactiva.
3. Guardar tarea y verificar que la lista muestra nombres visibles de asignados.
4. Editar la misma tarea, cambiar asignaciones y comprobar que se reemplazan.
5. Activar "Solo para mi" y verificar que la seleccion de miembros se limpia.
```

- [ ] **Step 5: Commit**

```powershell
Set-Location C:\dev\git\appcasa-backend
git add README.md
git commit -m "docs: describe tarea member assignment behavior"

Set-Location C:\dev\git\appcasa-frontend
git add README.md
git commit -m "docs: describe tarea assignment ui behavior"
```

## Self-Review
- Spec coverage:
  - persistencia real en `TB_TAREA_ASIGNACION`: Tasks 1 y 2
  - validacion de miembros por hogar: Task 2
  - respuesta publica con `asignaciones`: Task 3
  - selector en crear/editar con reglas de negocio: Tasks 4 y 5
  - nombres visibles en lista/tarjeta: Task 5
  - verificacion backend/frontend: Task 6
- Placeholder scan: no hay `TODO`, `TBD` ni pasos sin comando o sin codigo orientativo.
- Type consistency:
  - `miembroIds` se usa en request frontend/backend
  - `miembroId` y `nombreMiembro` se usan en response publico y UI
  - regla `miembros => esPersonal false` aparece igual en backend y frontend
