# Diseno: repositorios iniciales AppCasa

## Objetivo

Preparar el arranque formal de `AppCasa` con dos repositorios Git separados y publicos en GitHub, partiendo del codigo ya existente en las carpetas locales:

- `C:\dev\git\appcasa-backend`
- `C:\dev\git\appcasa-frontend`

El objetivo es dejar ambos proyectos listos para empezar desarrollo real con historial inicial, configuracion minima de repositorio y una estructura publica entendible.

## Contexto

La documentacion funcional, tecnica y de modelo de datos describe `AppCasa` como una plataforma modular para la gestion del hogar. La separacion backend/frontend ya existe fisicamente y es consistente con la arquitectura documentada:

- Backend: `Spring Boot`
- Frontend: `Ionic + Angular + Capacitor`

Ambas carpetas ya contienen codigo inicial y deben conservarse como origen del primer commit.

## Alcance

Incluye:

- Crear dos repositorios publicos en GitHub en la cuenta personal del usuario
- Usar los nombres `appcasa-backend` y `appcasa-frontend`
- Inicializar git en ambas carpetas locales si aun no existe
- Revisar y ajustar `.gitignore` en cada proyecto
- Crear o completar un `README.md` minimo por repositorio
- Realizar un commit inicial por proyecto
- Configurar el remoto `origin` correspondiente para cada carpeta

No incluye:

- Implementar nuevas funcionalidades de negocio
- Reestructurar la arquitectura actual
- Crear un monorepo
- Introducir automatizaciones CI/CD en esta fase

## Decisiones

### Topologia de repositorios

Se usaran dos repositorios independientes:

- `appcasa-backend`
- `appcasa-frontend`

No habra repositorio contenedor adicional ni monorepo. La coordinacion entre ambos se resolvera mediante documentacion cruzada.

### Visibilidad

Los dos repositorios se crearan como publicos en GitHub. Esto facilita compartir el progreso y empezar a construir desde una base visible, con la opcion de cambiar la visibilidad mas adelante si el proyecto lo requiere.

### Fuente del primer commit

El codigo actual de cada carpeta local se considerara la base del primer commit. No se generara un esqueleto nuevo desde GitHub ni se sustituira el contenido existente.

### Higiene minima antes de publicar

Antes del primer push se revisara el contenido versionado para evitar subir artefactos locales o generados.

Backend:

- excluir `target/`
- excluir repos Maven locales como `.mvnrepo*`
- excluir caches y ficheros temporales
- excluir artefactos de IDE no compartibles

Frontend:

- excluir `node_modules/`
- excluir `.angular/`
- excluir salidas de build
- excluir caches y artefactos locales de IDE no compartibles

### Documentacion minima

Cada repositorio tendra un `README.md` breve con:

- descripcion del proyecto
- stack tecnologico
- prerequisitos
- comando(s) basicos de arranque
- referencia al repositorio hermano

La documentacion extensa existente permanecera en el backend por ahora, salvo que luego se decida redistribuirla.

## Flujo de ejecucion previsto

1. Revisar el estado local de ambas carpetas
2. Preparar `.gitignore` y `README.md`
3. Inicializar git localmente en cada proyecto si hace falta
4. Crear los repositorios publicos en GitHub
5. Conectar cada carpeta con su remoto `origin`
6. Registrar un commit inicial claro
7. Hacer el primer push

## Riesgos y mitigaciones

### Basura en el primer commit

Riesgo: subir caches, builds o artefactos locales.

Mitigacion: revisar `.gitignore` y el contenido staged antes del commit inicial.

### Estado local heterogeneo

Riesgo: que backend y frontend no tengan el mismo nivel de preparacion documental o de limpieza.

Mitigacion: aplicar una base minima comun de higiene y README, sin bloquear por perfeccionismo.

### Autenticacion GitHub

Riesgo: que la creacion o el push fallen por falta de autenticacion.

Mitigacion: usar la integracion disponible con GitHub o pedir confirmacion puntual solo si falta algun dato operativo.

## Resultado esperado

Al finalizar esta tarea existiran dos repositorios publicos en GitHub, ambos enlazados a sus carpetas locales, con commit inicial, `.gitignore` correcto y `README.md` basico. Ese sera el punto de partida para empezar el desarrollo funcional del producto.
