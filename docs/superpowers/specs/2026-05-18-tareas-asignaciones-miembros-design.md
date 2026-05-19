# Diseno: tareas con asignaciones reales de miembros AppCasa

## Objetivo
Completar la siguiente iteracion funcional de `tareas` para que una tarea pueda asignarse realmente a miembros del hogar, persistiendo esas asignaciones en backend, exponiendolas en el contrato publico y permitiendo gestionarlas desde el formulario de crear/editar en frontend.

## Contexto actual
- Ya existe `TB_TAREA_ASIGNACION` en base de datos como tabla puente entre tarea y miembro.
- El backend ya ha migrado `tareas` a contrato publico por `codigo`, pero `asignaciones` sigue saliendo vacio en `TareaPublicResponse`.
- El frontend ya usa `hogarCodigo`, `prioridadCodigo` y `periodicidadCodigo`, pero el formulario de tarea no permite todavia seleccionar miembros.
- `MiembroHogarRepository` ya permite listar miembros por hogar, por lo que existe una base suficiente para cerrar el flujo sin redefinir el modulo de miembros.

## Alcance aprobado
- Seleccionar miembros al crear y editar tareas.
- Mostrar nombres visibles de miembros asignados en la lista/tarjeta de tareas.
- Regla de negocio:
  - si el usuario selecciona miembros, la tarea deja de ser `personal` automaticamente
  - si el usuario activa `Solo para mi`, se limpian las asignaciones seleccionadas

## Fuera de alcance
- No se implementa en esta iteracion el flujo de `aceptada` de `TB_TAREA_ASIGNACION`.
- No se introducen notificaciones, invitaciones ni estados de aceptacion por miembro.
- No se rediseña el modulo completo de `miembros` a contrato publico por `codigo`; solo se consume lo necesario para esta iteracion.

## Decision de diseno
- Se mantiene `miembroIds` como campo de entrada del contrato publico de `tareas`.
- Se persiste la relacion real en `TB_TAREA_ASIGNACION`.
- Se devuelve `asignaciones` resuelto con datos minimos de miembro (`miembroId`, `nombreMiembro`) para evitar llamadas extra por cada tarea.
- La actualizacion de asignaciones sigue una estrategia de reemplazo completo:
  - el backend elimina las asignaciones vigentes de la tarea
  - luego inserta exactamente el conjunto nuevo recibido

## Contrato publico

### Request de tarea
- Se mantiene:
  - `hogarCodigo`
  - `titulo`
  - `descripcion`
  - `prioridadCodigo`
  - `categoria`
  - `fechaLimite`
  - `periodicidadCodigo`
  - `esPersonal`
  - `miembroIds`

### Response de tarea
- `asignaciones` deja de ser una lista vacia y pasa a contener:
  - `miembroId`
  - `nombreMiembro`

### Regla de coherencia
- Si `miembroIds` contiene uno o mas elementos:
  - backend fuerza `esPersonal = false`
- Si `miembroIds` es vacio o ausente:
  - la tarea puede seguir siendo personal o no personal segun el campo `esPersonal`

## Diseno backend

### Persistencia
- Crear entidad JPA `TareaAsignacion` asociada a `TB_TAREA_ASIGNACION`.
- Crear `TareaAsignacionRepository` con consultas minimas:
  - listar por `idTarea`
  - borrar por `idTarea`
  - guardar lote de asignaciones

### Validacion
- Antes de persistir asignaciones, validar que cada `miembroId`:
  - exista
  - pertenezca al `hogar` de la tarea
  - no este repetido en el request final
- Si algun miembro no pertenece al hogar o no existe, responder `400 Bad Request`.

### Creacion y actualizacion
- `crear`:
  - crea la tarea base
  - persiste asignaciones si `miembroIds` viene informado
  - fuerza `esPersonal = false` cuando haya miembros
- `actualizar`:
  - actualiza campos base de la tarea
  - reemplaza las asignaciones por el conjunto nuevo recibido
  - si el conjunto nuevo tiene miembros, fuerza `esPersonal = false`
  - si el conjunto nuevo queda vacio, respeta el valor final de `esPersonal`

### Lectura
- El mapper publico de `tareas` deja de devolver `List.of()`.
- Para cada tarea, resuelve sus asignaciones y mapea:
  - `miembroId`
  - `nombreMiembro`

## Diseno frontend

### Formulario de tarea
- Cargar los miembros del hogar actual al abrir crear/editar.
- Mostrar selector multiple de miembros debajo de la seccion de prioridad/organizacion.
- El formulario envia `miembroIds`.
- Si el usuario selecciona uno o mas miembros:
  - `esPersonal` se pone automaticamente en `false`
- Si el usuario marca `Solo para mi`:
  - se vacia la seleccion de miembros

### Edicion
- En modo edicion, precargar `miembroIds` desde `tarea.asignaciones`.
- Mantener el formulario consistente si la tarea ya viene con asignaciones.

### Lista / tarjeta
- Mostrar los nombres de miembros asignados de forma visible.
- Para esta iteracion basta con texto o chips simples bajo el titulo o descripcion.
- Si no hay asignaciones, no se muestra bloque adicional.

## Manejo de errores
- Backend devuelve `400` para miembros inexistentes o ajenos al hogar.
- Frontend muestra mensaje generico de error de guardado si backend rechaza la asignacion.
- No se anade UI especial por miembro invalido en esta iteracion.

## Pruebas requeridas

### Backend
- crear tarea con `miembroIds` validos persiste asignaciones
- actualizar tarea reemplaza asignaciones previas
- crear o actualizar con miembro fuera del hogar devuelve `400`
- crear o actualizar con asignaciones fuerza `esPersonal = false`
- respuesta publica incluye `asignaciones` con `miembroId` y `nombreMiembro`

### Frontend
- el formulario envia `miembroIds`
- seleccionar miembros fuerza `esPersonal = false`
- marcar `Solo para mi` limpia `miembroIds`
- la edicion precarga asignaciones existentes
- la tarjeta/lista muestra nombres asignados

## Riesgos y mitigacion
- Riesgo: consultas adicionales por tarea al resolver asignaciones.
  - Mitigacion: aceptar coste inicial en esta iteracion y optimizar despues si hiciera falta.
- Riesgo: inconsistencia entre `esPersonal` y `miembroIds`.
  - Mitigacion: aplicar la misma regla en frontend y backend, con backend como fuente de verdad.
- Riesgo: modulo `miembros` aun no migrado del todo al nuevo contrato publico.
  - Mitigacion: limitar el alcance a consumo interno/controlado solo para selector de asignaciones.

## Criterios de aceptacion
- Se pueden crear tareas con miembros asignados desde la UI.
- Se pueden editar tareas y reemplazar sus asignaciones.
- La API devuelve asignaciones reales en el contrato publico de `tareas`.
- La lista de tareas muestra nombres de miembros asignados.
- La regla `miembros seleccionados => no personal` se cumple en frontend y backend.
