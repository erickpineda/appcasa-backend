# Perfil Ajustes Logout Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Anadir una pantalla de perfil en el frontend con datos del usuario, ajustes basicos visibles y un flujo claro de cierre de sesion accesible desde el dashboard.

**Architecture:** La solucion se implementa solo en frontend como una nueva feature lazy `perfil` protegida por la infraestructura de autenticacion ya existente. La pagina lee el usuario desde `AuthService`, muestra placeholders de `tema` e `idioma`, y usa `logout()` para limpiar sesion y volver a `/auth`, incluso si falla la revocacion remota.

**Tech Stack:** Angular 21, Ionic 8, Angular Router, HttpClient, RxJS, Karma, Jasmine

---

## File Map

**Frontend**

- Create: `C:\dev\git\appcasa-frontend\src\app\features\perfil\perfil.page.ts`
- Create: `C:\dev\git\appcasa-frontend\src\app\features\perfil\perfil.page.html`
- Create: `C:\dev\git\appcasa-frontend\src\app\features\perfil\perfil.module.ts`
- Create: `C:\dev\git\appcasa-frontend\src\app\features\perfil\perfil.page.spec.ts`
- Modify: `C:\dev\git\appcasa-frontend\src\app\app-routing.module.ts`
- Modify: `C:\dev\git\appcasa-frontend\src\app\features\dashboard\dashboard.page.ts`
- Modify: `C:\dev\git\appcasa-frontend\src\app\features\dashboard\dashboard.page.html`
- Modify: `C:\dev\git\appcasa-frontend\src\app\core\services\auth.service.spec.ts`

Run all commands for this plan from: `C:\dev\git\appcasa-frontend`

### Task 1: Crear la feature lazy `perfil`

**Files:**
- Create: `C:\dev\git\appcasa-frontend\src\app\features\perfil\perfil.page.ts`
- Create: `C:\dev\git\appcasa-frontend\src\app\features\perfil\perfil.page.html`
- Create: `C:\dev\git\appcasa-frontend\src\app\features\perfil\perfil.module.ts`
- Modify: `C:\dev\git\appcasa-frontend\src\app\app-routing.module.ts`
- Test: `C:\dev\git\appcasa-frontend\src\app\features\perfil\perfil.page.spec.ts`

- [ ] **Step 1: Write the failing profile page spec**

```ts
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { IonicModule } from '@ionic/angular';
import { of } from 'rxjs';
import { PerfilPage } from './perfil.page';
import { AuthService } from '../../core/services/auth.service';

describe('PerfilPage', () => {
  let component: PerfilPage;
  let fixture: ComponentFixture<PerfilPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [IonicModule.forRoot(), RouterTestingModule],
      declarations: [PerfilPage],
      providers: [
        {
          provide: AuthService,
          useValue: {
            usuario: {
              id: '1',
              nombre: 'Ana',
              apellidos: 'Casa',
              email: 'ana@example.com',
              tema: 'CLARO',
              locale: 'es-ES',
              idEstado: 1,
            },
            logout: () => of(void 0),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PerfilPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('shows authenticated user data', () => {
    expect(component.usuario?.email).toBe('ana@example.com');
  });
});
```

- [ ] **Step 2: Run test to verify it fails**

Run: `npm test -- --watch=false --include src/app/features/perfil/perfil.page.spec.ts`
Expected: FAIL because `PerfilPage` and its module do not exist yet.

- [ ] **Step 3: Implement the lazy profile feature**

```ts
// src/app/features/perfil/perfil.page.ts
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Usuario } from '../../core/models/domain.models';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-perfil',
  standalone: false,
  templateUrl: './perfil.page.html',
})
export class PerfilPage implements OnInit {
  usuario: Usuario | null = null;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.usuario = this.authService.usuario;

    if (!this.usuario) {
      this.router.navigate(['/auth'], { replaceUrl: true });
    }
  }
}
```

```ts
// src/app/features/perfil/perfil.module.ts
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SharedModule } from '../../shared/shared.module';
import { PerfilPage } from './perfil.page';

const routes: Routes = [
  { path: '', component: PerfilPage },
];

@NgModule({
  imports: [SharedModule, RouterModule.forChild(routes)],
  declarations: [PerfilPage],
})
export class PerfilModule {}
```

```ts
// src/app/app-routing.module.ts
{
  path: 'perfil',
  loadChildren: () =>
    import('./features/perfil/perfil.module').then((m) => m.PerfilModule),
},
```

- [ ] **Step 4: Run test to verify it passes**

Run: `npm test -- --watch=false --include src/app/features/perfil/perfil.page.spec.ts`
Expected: PASS with the protected lazy feature compiled and rendering the authenticated user.

- [ ] **Step 5: Commit**

```powershell
Set-Location C:\dev\git\appcasa-frontend
git add src/app/features/perfil/perfil.page.ts src/app/features/perfil/perfil.page.html src/app/features/perfil/perfil.module.ts src/app/features/perfil/perfil.page.spec.ts src/app/app-routing.module.ts
git commit -m "feat: add profile feature"
```

### Task 2: Enlazar el dashboard con `perfil`

**Files:**
- Modify: `C:\dev\git\appcasa-frontend\src\app\features\dashboard\dashboard.page.html`
- Modify: `C:\dev\git\appcasa-frontend\src\app\features\dashboard\dashboard.page.ts`
- Test: `C:\dev\git\appcasa-frontend\src\app\features\perfil\perfil.page.spec.ts`

- [ ] **Step 1: Extend the profile spec with dashboard navigation coverage**

```ts
it('renders a link to /perfil from the dashboard header', () => {
  // create DashboardPage fixture and assert the profile button points to /perfil
});
```

- [ ] **Step 2: Run test to verify it fails**

Run: `npm test -- --watch=false --include src/app/features/perfil/perfil.page.spec.ts`
Expected: FAIL because the dashboard header still only shows the existing `familia` action.

- [ ] **Step 3: Add the profile icon to the dashboard header**

```html
<ion-buttons slot="end">
  <ion-button routerLink="/perfil" aria-label="Abrir perfil">
    <ion-icon name="person-circle-outline" slot="icon-only"></ion-icon>
  </ion-button>
  <ion-button routerLink="/familia" aria-label="Ir a familia">
    <ion-icon name="people-outline" slot="icon-only"></ion-icon>
  </ion-button>
</ion-buttons>
```

```ts
// dashboard.page.ts stays minimal; no extra service wiring is required for this task
```

- [ ] **Step 4: Run test to verify it passes**

Run: `npm test -- --watch=false --include src/app/features/perfil/perfil.page.spec.ts`
Expected: PASS with the dashboard exposing a visible profile entry point.

- [ ] **Step 5: Commit**

```powershell
Set-Location C:\dev\git\appcasa-frontend
git add src/app/features/dashboard/dashboard.page.html src/app/features/dashboard/dashboard.page.ts src/app/features/perfil/perfil.page.spec.ts
git commit -m "feat: link dashboard header to profile"
```

### Task 3: Mostrar datos de usuario y ajustes basicos

**Files:**
- Modify: `C:\dev\git\appcasa-frontend\src\app\features\perfil\perfil.page.html`
- Modify: `C:\dev\git\appcasa-frontend\src\app\features\perfil\perfil.page.ts`
- Test: `C:\dev\git\appcasa-frontend\src\app\features\perfil\perfil.page.spec.ts`

- [ ] **Step 1: Add failing UI expectations to the profile spec**

```ts
it('renders the user card and basic settings placeholders', () => {
  const compiled = fixture.nativeElement as HTMLElement;
  expect(compiled.textContent).toContain('Ana Casa');
  expect(compiled.textContent).toContain('ana@example.com');
  expect(compiled.textContent).toContain('Tema');
  expect(compiled.textContent).toContain('Idioma');
  expect(compiled.textContent).toContain('Proximamente');
});
```

- [ ] **Step 2: Run test to verify it fails**

Run: `npm test -- --watch=false --include src/app/features/perfil/perfil.page.spec.ts`
Expected: FAIL because the profile template still does not show the required card or settings labels.

- [ ] **Step 3: Implement the profile layout**

```html
<ion-header>
  <ion-toolbar color="primary">
    <ion-buttons slot="start">
      <ion-back-button defaultHref="/dashboard"></ion-back-button>
    </ion-buttons>
    <ion-title>Perfil</ion-title>
  </ion-toolbar>
</ion-header>

<ion-content class="ion-padding">
  @if (usuario) {
    <ion-card>
      <ion-card-header>
        <ion-card-title>{{ usuario.nombre }} {{ usuario.apellidos || '' }}</ion-card-title>
        <ion-card-subtitle>{{ usuario.email }}</ion-card-subtitle>
      </ion-card-header>
    </ion-card>

    <ion-list inset="true">
      <ion-list-header>
        <ion-label>Ajustes</ion-label>
      </ion-list-header>
      <ion-item>
        <ion-label>
          <h2>Tema</h2>
          <p>Proximamente</p>
        </ion-label>
        <ion-note slot="end">{{ usuario.tema }}</ion-note>
      </ion-item>
      <ion-item>
        <ion-label>
          <h2>Idioma</h2>
          <p>Proximamente</p>
        </ion-label>
        <ion-note slot="end">{{ usuario.locale }}</ion-note>
      </ion-item>
    </ion-list>
  }
</ion-content>
```

- [ ] **Step 4: Run test to verify it passes**

Run: `npm test -- --watch=false --include src/app/features/perfil/perfil.page.spec.ts`
Expected: PASS with the user card and both placeholder settings rendered.

- [ ] **Step 5: Commit**

```powershell
Set-Location C:\dev\git\appcasa-frontend
git add src/app/features/perfil/perfil.page.ts src/app/features/perfil/perfil.page.html src/app/features/perfil/perfil.page.spec.ts
git commit -m "feat: show profile data and basic settings placeholders"
```

### Task 4: Implementar `logout` visible y fiable

**Files:**
- Modify: `C:\dev\git\appcasa-frontend\src\app\features\perfil\perfil.page.ts`
- Modify: `C:\dev\git\appcasa-frontend\src\app\features\perfil\perfil.page.html`
- Test: `C:\dev\git\appcasa-frontend\src\app\features\perfil\perfil.page.spec.ts`
- Test: `C:\dev\git\appcasa-frontend\src\app\core\services\auth.service.spec.ts`

- [ ] **Step 1: Add failing logout tests**

```ts
it('logs out and redirects to /auth', () => {
  const authService = TestBed.inject(AuthService);
  spyOn(authService, 'logout').and.returnValue(of(void 0));
  const router = TestBed.inject(Router);
  spyOn(router, 'navigate');

  component.cerrarSesion();

  expect(authService.logout).toHaveBeenCalled();
  expect(router.navigate).toHaveBeenCalledWith(['/auth'], { replaceUrl: true });
});

it('redirects to /auth even if logout fails', () => {
  spyOn(authService, 'logout').and.returnValue(throwError(() => new Error('network')));
  spyOn(authService, 'clearSession');
  component.cerrarSesion();
  expect(authService.clearSession).toHaveBeenCalled();
});
```

- [ ] **Step 2: Run test to verify it fails**

Run: `npm test -- --watch=false --include src/app/features/perfil/perfil.page.spec.ts --include src/app/core/services/auth.service.spec.ts`
Expected: FAIL because the profile page still has no logout action and the fallback path is not covered.

- [ ] **Step 3: Implement the logout flow in the page**

```ts
cerrarSesion(): void {
  this.authService.logout().subscribe({
    next: () => {
      this.router.navigate(['/auth'], { replaceUrl: true });
    },
    error: () => {
      this.authService.clearSession();
      this.router.navigate(['/auth'], { replaceUrl: true });
    },
  });
}
```

```html
<ion-button expand="block" color="danger" fill="solid" (click)="cerrarSesion()">
  Cerrar sesion
</ion-button>
```

- [ ] **Step 4: Run test to verify it passes**

Run: `npm test -- --watch=false --include src/app/features/perfil/perfil.page.spec.ts --include src/app/core/services/auth.service.spec.ts`
Expected: PASS with both success and fallback logout behavior verified.

- [ ] **Step 5: Commit**

```powershell
Set-Location C:\dev\git\appcasa-frontend
git add src/app/features/perfil/perfil.page.ts src/app/features/perfil/perfil.page.html src/app/features/perfil/perfil.page.spec.ts src/app/core/services/auth.service.spec.ts
git commit -m "feat: add visible logout flow in profile"
```

### Task 5: Verificacion final de navegacion y regresion

**Files:**
- Modify: `C:\dev\git\appcasa-frontend\src\app\app.component.spec.ts`
- Test: `C:\dev\git\appcasa-frontend\src\app\features\perfil\perfil.page.spec.ts`
- Test: `C:\dev\git\appcasa-frontend\src\app\core\interceptors\auth.interceptor.spec.ts`
- Test: `C:\dev\git\appcasa-frontend\src\app\core\services\auth.service.spec.ts`

- [ ] **Step 1: Add any missing provider wiring for the root test**

```ts
providers: [
  {
    provide: AuthService,
    useValue: {
      restoreSession: () => of(true),
      usuario: null,
    },
  },
],
```

- [ ] **Step 2: Run the focused frontend test suite**

Run: `npm test -- --watch=false --include src/app/app.component.spec.ts --include src/app/features/perfil/perfil.page.spec.ts --include src/app/core/services/auth.service.spec.ts --include src/app/core/interceptors/auth.interceptor.spec.ts`
Expected: PASS with the new profile flow and the existing auth flow both green.

- [ ] **Step 3: Run the production build**

Run: `npm run build`
Expected: PASS with `/perfil` compiled as a lazy feature and no template or routing errors.

- [ ] **Step 4: Confirm manual acceptance criteria**

```text
- El dashboard muestra acceso visible a /perfil
- /perfil enseña nombre, email, tema e idioma
- El boton Cerrar sesion saca al usuario de la app
- Si logout remoto falla, la app igualmente vuelve a /auth
```

- [ ] **Step 5: Commit**

```powershell
Set-Location C:\dev\git\appcasa-frontend
git add src/app/app.component.spec.ts src/app/features/perfil/perfil.page.spec.ts src/app/core/services/auth.service.spec.ts src/app/core/interceptors/auth.interceptor.spec.ts
git commit -m "test: cover profile logout flow"
```

## Self-Review

- Spec coverage:
  - nueva pantalla `perfil`: Tasks 1 y 3
  - ruta protegida `/perfil`: Task 1
  - acceso desde cabecera del dashboard: Task 2
  - datos de usuario y placeholders de ajustes: Task 3
  - logout fiable con fallback local: Task 4
  - pruebas y regresion: Tasks 4 y 5
- Placeholder scan: no `TODO`, `TBD` ni referencias circulares; cada tarea incluye archivos exactos, tests, comandos y commits.
- Type consistency:
  - la pagina usa `PerfilPage`, ruta `/perfil` y metodo `cerrarSesion()`
  - se reutiliza `AuthService.logout()` y `AuthService.clearSession()`
  - los ajustes visibles usan `tema` y `locale`, que ya existen en `Usuario`
