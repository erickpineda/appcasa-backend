# Repositorios Iniciales AppCasa Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Dejar `appcasa-backend` y `appcasa-frontend` listos como dos repositorios Git y GitHub publicos, partiendo del codigo local existente y con una base minima de documentacion e higiene.

**Architecture:** Se mantiene la separacion fisica actual entre backend Spring Boot y frontend Ionic/Angular. Cada carpeta se prepara de forma independiente con su propio `.gitignore`, `README.md`, historial Git y remoto `origin`, y solo despues se crea el repositorio GitHub y se publica el primer commit.

**Tech Stack:** Git, GitHub CLI, Maven, Spring Boot 3.5, Java 25, Ionic 8, Angular 21, Capacitor 8, PowerShell

---

### Task 1: Backend Repository Hygiene

**Files:**
- Create: `C:\dev\git\appcasa-backend\.gitignore`
- Create: `C:\dev\git\appcasa-backend\README.md`
- Reference: `C:\dev\git\appcasa-backend\pom.xml`
- Reference: `C:\dev\git\appcasa-backend\src\main\resources\application.yml`

- [ ] **Step 1: Write the backend `.gitignore`**

```gitignore
target/
.mvnrepo*/
*.log
*.tmp
*.bak
*.swp
.idea/
.vscode/
.classpath
.project
.settings/
.factorypath
Thumbs.db
.DS_Store
```

- [ ] **Step 2: Write the backend `README.md`**

````md
# AppCasa Backend

Backend REST de AppCasa para la gestion del hogar.

## Stack

- Java 25
- Maven
- Spring Boot 3.5
- Spring Security + JWT
- Spring Data JPA
- Flyway
- MySQL / H2

## Prerequisitos

- JDK 25
- Maven 3.9+
- MySQL 8 para perfiles `dev` o `prod`

## Arranque rapido

### Perfil local

```bash
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

### Compilacion

```bash
mvn clean test
```

## Repositorio hermano

El frontend de AppCasa vive en el repositorio publico `appcasa-frontend`.
````

- [ ] **Step 3: Review the generated backend hygiene files**

Run in `C:\dev\git\appcasa-backend`:

```powershell
Get-Content .gitignore
Get-Content README.md
```

Expected: both files exist and match the planned content.

- [ ] **Step 4: Commit the file-only preparation checkpoint**

```bash
git add .gitignore README.md
git diff --cached -- .gitignore README.md
```

Expected: only the two new files appear in the staged diff.

### Task 2: Frontend Repository Hygiene

**Files:**
- Create: `C:\dev\git\appcasa-frontend\.gitignore`
- Create: `C:\dev\git\appcasa-frontend\README.md`
- Reference: `C:\dev\git\appcasa-frontend\package.json`

- [ ] **Step 1: Write the frontend `.gitignore`**

```gitignore
node_modules/
.angular/
www/
dist/
coverage/
tmp/
*.log
*.tmp
*.bak
.idea/
.vscode/
Thumbs.db
.DS_Store
```

- [ ] **Step 2: Write the frontend `README.md`**

````md
# AppCasa Frontend

Frontend movil y web de AppCasa construido con Ionic, Angular y Capacitor.

## Stack

- Node.js 20+
- npm
- Ionic 8
- Angular 21
- Capacitor 8

## Prerequisitos

- Node.js 20 o superior
- npm 10 o superior
- Ionic CLI opcional para flujos de desarrollo con `ionic serve`

## Arranque rapido

### Instalar dependencias

```bash
npm install
```

### Desarrollo web

```bash
npm run ionic:serve
```

### Build

```bash
npm run build
```

## Repositorio hermano

La API y logica de servidor viven en el repositorio publico `appcasa-backend`.
````

- [ ] **Step 3: Review the generated frontend hygiene files**

Run in `C:\dev\git\appcasa-frontend`:

```powershell
Get-Content .gitignore
Get-Content README.md
```

Expected: both files exist and match the planned content.

- [ ] **Step 4: Validate that build outputs are ignored by policy**

Run in `C:\dev\git\appcasa-frontend`:

```powershell
Get-ChildItem -Name
```

Expected: the repository root may still contain `www`, but the new `.gitignore` explicitly excludes it from version control.

### Task 3: Initialize Local Git for Backend

**Files:**
- Modify: `C:\dev\git\appcasa-backend\.gitignore`
- Modify: `C:\dev\git\appcasa-backend\README.md`
- Create: `C:\dev\git\appcasa-backend\.git\`

- [ ] **Step 1: Initialize Git with `main` as the default branch**

Run in `C:\dev\git\appcasa-backend`:

```bash
git init -b main
```

Expected: Git reports an empty repository initialized on branch `main`.

- [ ] **Step 2: Inspect the status before staging**

Run in `C:\dev\git\appcasa-backend`:

```bash
git status --short --ignored
```

Expected: source files plus `README.md` and `.gitignore` appear as untracked, while entries such as `target/` and `.mvnrepo*` appear as ignored when present.

- [ ] **Step 3: Stage the backend repository contents**

Run in `C:\dev\git\appcasa-backend`:

```bash
git add .
git status --short --ignored
```

Expected: tracked project files are staged and ignored build artifacts remain excluded.

- [ ] **Step 4: Create the backend initial commit**

Run in `C:\dev\git\appcasa-backend`:

```bash
git commit -m "chore: bootstrap backend repository"
git log --oneline -1
```

Expected: the latest commit message is `chore: bootstrap backend repository`.

### Task 4: Initialize Local Git for Frontend

**Files:**
- Modify: `C:\dev\git\appcasa-frontend\.gitignore`
- Modify: `C:\dev\git\appcasa-frontend\README.md`
- Create: `C:\dev\git\appcasa-frontend\.git\`

- [ ] **Step 1: Initialize Git with `main` as the default branch**

Run in `C:\dev\git\appcasa-frontend`:

```bash
git init -b main
```

Expected: Git reports an empty repository initialized on branch `main`.

- [ ] **Step 2: Inspect the status before staging**

Run in `C:\dev\git\appcasa-frontend`:

```bash
git status --short --ignored
```

Expected: application sources plus `README.md` and `.gitignore` appear as untracked, while `node_modules/`, `.angular/`, `www/`, and `dist/` appear as ignored when present.

- [ ] **Step 3: Stage the frontend repository contents**

Run in `C:\dev\git\appcasa-frontend`:

```bash
git add .
git status --short --ignored
```

Expected: tracked project files are staged and ignored generated folders remain excluded.

- [ ] **Step 4: Create the frontend initial commit**

Run in `C:\dev\git\appcasa-frontend`:

```bash
git commit -m "chore: bootstrap frontend repository"
git log --oneline -1
```

Expected: the latest commit message is `chore: bootstrap frontend repository`.

### Task 5: Create GitHub Repositories and Push the First Commit

**Files:**
- Modify: `C:\dev\git\appcasa-backend\.git\config`
- Modify: `C:\dev\git\appcasa-frontend\.git\config`

- [ ] **Step 1: Verify GitHub CLI authentication once**

Run in any terminal:

```bash
gh auth status
```

Expected: the current personal GitHub account appears as authenticated.

- [ ] **Step 2: Create the backend GitHub repository and attach `origin`**

Run in `C:\dev\git\appcasa-backend`:

```bash
gh repo create appcasa-backend --public --source . --remote origin --description "Backend REST de AppCasa"
git remote -v
```

Expected: the repository is created in the authenticated personal account and `origin` points to `appcasa-backend`.

- [ ] **Step 3: Create the frontend GitHub repository and attach `origin`**

Run in `C:\dev\git\appcasa-frontend`:

```bash
gh repo create appcasa-frontend --public --source . --remote origin --description "Frontend Ionic de AppCasa"
git remote -v
```

Expected: the repository is created in the authenticated personal account and `origin` points to `appcasa-frontend`.

- [ ] **Step 4: Push the backend initial commit**

Run in `C:\dev\git\appcasa-backend`:

```bash
git push -u origin main
git status --short
```

Expected: branch `main` is published and the working tree is clean.

- [ ] **Step 5: Push the frontend initial commit**

Run in `C:\dev\git\appcasa-frontend`:

```bash
git push -u origin main
git status --short
```

Expected: branch `main` is published and the working tree is clean.

- [ ] **Step 6: Capture the public repository URLs for handoff**

Run in each repository:

```bash
git remote get-url origin
```

Expected: one final backend URL and one final frontend URL ready to share with the user.
