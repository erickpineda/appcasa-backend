# Diseno: selector visible de hogar con acciones crear y unirse

## Objetivo
Hacer visible y usable la seleccion de `hogar` en la app para que el usuario siempre sepa en que contexto esta trabajando y pueda cambiarlo facilmente al crear tareas y navegar por la aplicacion. El cambio debe resolver el problema completo, no solo el sintoma en `tareas`, por lo que incluye la API publica de `hogares` que hoy falta en backend.

## Contexto actual
- El frontend ya tiene `HogarService` con estado local en `localStorage` mediante `appcasa_hogar`.
- `dashboard` y `tareas` dependen de `hogarService.hogarActual`, pero no muestran una UI clara para elegir o cambiar hogar.
- El formulario de tarea se queda bloqueado si no hay hogar activo, aunque ya se mejoro el mensaje visual.
- El frontend ya asume endpoints de `hogares` para `crear()` y `unirse()`, pero en backend no existe aun un controlador publico equivalente.
- En backend existe `Hogar`, `HogarRepository`, `MiembroHogar` y `MiembroHogarRepository`, por lo que hay una base suficiente para derivar los hogares del usuario desde `TB_MIEMBRO_HOGAR.id_usuario`.

## Alcance aprobado
- Anadir selector visible de hogar a nivel global.
- Anadir contexto visible y cambio rapido de hogar dentro del modulo de `tareas`.
- Permitir crear hogar y unirse por codigo desde la UI cuando no haya uno activo o el usuario quiera cambiar.
- Crear la API publica de `hogares` necesaria para listar hogares del usuario autenticado, crear hogar y unirse por codigo.
- Mantener la seleccion activa en frontend mediante `localStorage`, sin persistirla en backend.

## Fuera de alcance
- No se implementa en esta iteracion una pantalla completa de administracion avanzada de hogares.
- No se anaden roles complejos ni permisos por hogar.
- No se modela un concepto de "hogar favorito" en backend.
- No se redisenan todos los modulos dependientes de hogar mas alla de exponer y consumir el hogar activo de forma visible.

## Alternativas consideradas

### Opcion elegida: selector global mas contexto visible en tareas
Ventajas:
- hace descubrible el concepto de hogar en toda la app
- evita que el usuario tenga que salir de `tareas` para resolver el bloqueo
- mantiene una unica fuente de verdad para el hogar activo y la refleja localmente donde hace falta

Riesgos:
- requiere tocar backend y frontend
- obliga a definir reglas claras de auto-seleccion y estado vacio

### Opcion descartada: selector solo en tareas
Ventajas:
- menor esfuerzo inicial

Desventajas:
- deja `dashboard` y otras pantallas con el mismo problema conceptual
- el usuario no entiende el contexto global de la app

### Opcion descartada: solo mensaje con enlace a futura pantalla
Ventajas:
- implementacion minima

Desventajas:
- no resuelve el problema real ahora
- anade friccion innecesaria en el flujo principal

## Decision de diseno
- Habra un selector visible de hogar en una zona global descubierta de la app.
- `Tareas` y `Nueva tarea` mostraran tambien el hogar activo y permitiran cambiarlo sin salir del flujo.
- Si el usuario no tiene hogares asociados, la UI mostrara acciones directas:
  - `Crear hogar`
  - `Unirme con codigo`
- El backend expondra una API publica de `hogares`.
- El frontend seguira guardando el hogar activo en `localStorage`.

## Contrato publico de hogares

### GET `/api/v1/hogares`
- Devuelve la lista de hogares asociados al usuario autenticado.
- El listado se resuelve a partir de `MiembroHogar.idUsuario = usuario autenticado`.
- Cada item devolvera al menos:
  - `id`
  - `nombre`
  - `descripcion`
  - `codigo`

### POST `/api/v1/hogares`
- Crea un hogar nuevo.
- El usuario creador queda asociado a ese hogar como miembro persona vinculado a su `idUsuario`.
- La respuesta devuelve el hogar creado con el mismo DTO publico.

### POST `/api/v1/hogares/unirse`
- Recibe `codigo`.
- Si el hogar existe y el usuario no pertenece ya a el, crea la asociacion en `TB_MIEMBRO_HOGAR`.
- Si ya pertenece, devuelve el hogar igualmente como resultado idempotente del flujo de seleccion.
- La respuesta devuelve el hogar objetivo con el DTO publico.

## Diseno backend

### Dominio y repositorios
- Reutilizar `HogarRepository`.
- Extender `MiembroHogarRepository` con una consulta por `idUsuario` para obtener membresias del usuario autenticado.
- Reutilizar `MiembroHogar` para materializar la relacion usuario-hogar cuando se crea o se une a uno.

### API
- Crear `HogarController` con las tres operaciones publicas:
  - listar
  - crear
  - unirse
- Crear DTOs publicos minimos para request/response de hogares.
- Mantener el estilo del contrato publico ya usado en `tareas`.

### Reglas de negocio
- Crear hogar:
  - genera el hogar
  - genera el `MiembroHogar` asociado al usuario actual con `idTipoMiembro` de persona
  - devuelve el hogar creado
- Unirse por codigo:
  - valida que el codigo exista
  - si el usuario ya pertenece, no duplica membresia
  - si no pertenece, crea `MiembroHogar`
- Listar hogares:
  - devuelve solo hogares en los que el usuario esta asociado
  - orden preferente por `nombre`

### Errores
- `codigo` inexistente al unirse devuelve `404` o `400` segun el estilo actual de errores del proyecto; en esta iteracion se recomienda `404` por ser recurso no encontrado.
- Duplicados de membresia se resuelven de forma idempotente, no como error visible al usuario.
- Requests invalidos siguen el formato de validacion actual del backend.

## Diseno frontend

### Servicio de hogares
- `HogarService` se amplia con un metodo `listarMisHogares()`.
- Se mantiene `hogarActual` como fuente de verdad local.
- Se anade una utilidad para validar si el hogar guardado sigue existiendo dentro del listado recibido.

### Selector global
- Se muestra en un punto visible de la app, preferiblemente `dashboard` y/o cabecera principal.
- Comportamiento al cargar:
  - si hay un hogar guardado y sigue existiendo, se conserva
  - si no hay hogar guardado y solo hay uno disponible, se selecciona automaticamente
  - si hay varios y no hay hogar valido activo, se pide elegir
  - si no hay ninguno, se muestran acciones de crear o unirse

### Modulo de tareas
- `Tareas` muestra el hogar activo arriba de la lista.
- `Nueva tarea` muestra el hogar activo arriba del formulario.
- En ambas vistas se puede abrir el selector rapido de hogar.
- Si no hay hogar activo:
  - se muestra el estado visible
  - se ofrece selector si hay hogares
  - si no hay hogares, se muestran acciones `Crear hogar` y `Unirme con codigo`

### Flujos crear y unirse
- `Crear hogar` abre un flujo simple con nombre y descripcion opcional.
- `Unirme con codigo` abre un flujo simple con campo de codigo.
- Tras crear o unirse:
  - el hogar recibido se marca como activo
  - se actualiza el listado local de hogares
  - la pantalla actual se refresca y queda desbloqueada

## Flujo de datos
- La UI pide `GET /api/v1/hogares` al entrar en una vista que necesite contexto de hogar.
- El frontend valida si el `hogarActual` guardado sigue dentro del listado.
- Si el usuario cambia de hogar, `HogarService.seleccionar()` actualiza `localStorage` y el estado en memoria.
- `dashboard`, `tareas` y `tarea-form` consumen el mismo hogar activo.
- Crear hogar o unirse por codigo producen tambien una seleccion automatica del hogar devuelto.

## Pruebas requeridas

### Backend
- listar hogares del usuario autenticado devuelve solo sus hogares
- crear hogar crea el hogar y la membresia del usuario
- unirse por codigo crea la membresia si no existe
- unirse por codigo es idempotente si la membresia ya existe
- unirse con codigo invalido devuelve error controlado

### Frontend
- `HogarService` carga el listado de hogares del usuario
- el selector global auto-selecciona cuando solo hay un hogar
- `tareas` muestra el hogar activo visible
- `tarea-form` muestra el hogar activo visible
- sin hogares disponibles se muestran acciones `Crear hogar` y `Unirme con codigo`
- crear o unirse selecciona automaticamente el hogar devuelto

## Riesgos y mitigacion
- Riesgo: no existe hoy API publica de hogares, por lo que el cambio puede parecer mas grande de lo esperado.
  - Mitigacion: mantener el contrato de hogares minimo y centrado solo en seleccion/creacion/unirse.
- Riesgo: no conocer con certeza el `idTipoMiembro` correcto para persona.
  - Mitigacion: confirmar el valor maestro existente durante implementacion y cubrirlo con test.
- Riesgo: divergencia entre hogar guardado en `localStorage` y hogares reales del usuario.
  - Mitigacion: validar el hogar activo contra `GET /api/v1/hogares` al cargar.
- Riesgo: exceso de UI duplicada entre selector global y tareas.
  - Mitigacion: reutilizar un componente o patron visual comun para selector y acciones rapidas.

## Criterios de aceptacion
- El usuario ve de forma clara el hogar activo en la app.
- El usuario puede cambiar de hogar sin salir del flujo de `tareas`.
- El usuario puede crear hogar o unirse por codigo desde la UI visible cuando no tiene contexto de hogar.
- La API publica de `hogares` soporta listar, crear y unirse.
- `dashboard`, `tareas` y `nueva tarea` reaccionan correctamente al cambiar el hogar activo.
