# Diseno: tareas con fecha y hora real de vencimiento

## Objetivo
Corregir la UX del formulario de `tareas` para que el vencimiento se gestione como una fecha y hora real, coherente entre frontend y backend, y aprovechar el cambio para cerrar dos problemas detectados en la creacion de tareas:
- falta de contexto visible cuando no hay hogar activo
- categoria en texto libre cuando el usuario espera un selector cerrado

## Contexto actual
- El formulario de tarea usa `fechaLimite` como campo visual de `Fecha limite`, pero hoy se captura solo fecha.
- El backend persiste `fechaLimite` como `LocalDate`, por lo que la hora no existe en el contrato ni en la base de datos.
- El frontend envia `fechaLimite` como `string`, sin una semantica clara de fecha frente a fecha y hora.
- El guardado depende de `hogarService.hogarActual`; si no existe hogar activo, el usuario solo descubre el problema al pulsar guardar.
- `categoria` es actualmente un `ion-input`, pero el comportamiento esperado es un desplegable fijo.

## Alcance aprobado
- Mantener el nombre publico del campo `fechaLimite`.
- Cambiar su significado para que represente una fecha y hora real de vencimiento.
- Enviar y devolver `fechaLimite` como datetime ISO-8601 completo.
- Actualizar modelo, API, persistencia, formulario y tests para soportar el cambio de extremo a extremo.
- Cambiar `categoria` a un selector fijo.
- Mostrar un estado visible en el formulario cuando no haya hogar activo, en lugar de fallar solo al guardar.

## Fuera de alcance
- No se introduce un campo nuevo paralelo como `fechaHoraLimite`.
- No se anaden zonas horarias configurables por usuario.
- No se redisenan listados, filtros o recordatorios mas alla de lo necesario para reflejar el nuevo `fechaLimite`.
- No se aborda en esta iteracion la publicacion pendiente de commits al remoto.

## Alternativas consideradas

### Opcion elegida: mantener `fechaLimite` y cambiarlo a datetime real
Ventajas:
- minimiza el impacto en nombres de API y modelos frontend
- evita duplicar campos con semanticas parecidas
- alinea el formulario con la persistencia real

Riesgos:
- cambia el contrato para clientes que hoy manden solo fecha
- requiere migracion de persistencia si la columna actual es de tipo fecha

### Opcion descartada: crear `fechaHoraLimite` y deprecar `fechaLimite`
Ventajas:
- compatibilidad mas explicita

Desventajas:
- duplica la semantica en contrato y UI
- introduce complejidad temporal innecesaria para un unico consumidor conocido

### Opcion descartada: dejar backend en fecha y mostrar hora solo en frontend
Desventajas:
- rompe la expectativa del usuario
- genera perdida silenciosa de informacion

## Decision de diseno
- Se mantiene el nombre `fechaLimite` en request y response.
- `fechaLimite` deja de representar un `LocalDate` y pasa a representar un instante completo.
- El formato de intercambio sera ISO-8601 completo.
- El backend sera la fuente de verdad y persistira la fecha y hora real.
- El frontend mostrara un control claro de fecha y hora, no un selector ambiguo de solo fecha.

## Contrato publico

### Request de tarea
- `fechaLimite` pasa a aceptar datetime ISO-8601 completo.
- Se mantiene el resto del contrato publico actual:
  - `hogarCodigo`
  - `titulo`
  - `descripcion`
  - `prioridadCodigo`
  - `categoria`
  - `esPeriodica`
  - `periodicidadCodigo`
  - `esPersonal`
  - `miembroIds`

### Response de tarea
- `fechaLimite` pasa a devolverse como datetime ISO-8601 completo.
- El nombre del campo no cambia.

### Compatibilidad
- La API se tratara como contrato actualizado, no como contrato dual.
- Si existiera algun dato historico guardado solo con fecha, se normalizara durante la migracion a una hora fija controlada.
- No se anade en esta iteracion una capa de compatibilidad para aceptar simultaneamente `YYYY-MM-DD` y datetime completo, salvo que aparezca un consumidor externo real que lo exija.

## Diseno backend

### Dominio y aplicacion
- `Tarea.fechaLimite` cambia de `LocalDate` a `Instant`.
- `TareaRequest` cambia de `LocalDate` a `Instant`.
- `TareaPublicRequest` cambia de `LocalDate` a `Instant`.
- `TareaPublicResponse` cambia de `LocalDate` a `Instant`.
- `TareaServiceImpl` mantiene la logica actual de negocio, pero deja de truncar a fecha.

### Persistencia
- La columna `fecha_limite` debe soportar timestamp real.
- Si hoy la base de datos la almacena como tipo fecha, se requiere migracion de esquema a timestamp.
- Los datos historicos con solo fecha se normalizan a una hora fija para no perder el dia original.
- La hora elegida para normalizacion inicial sera `00:00:00Z`, salvo que al implementar se detecte una restriccion tecnica del motor de base de datos que exija una variante equivalente.

### Mapper y serializacion
- Los mappers publicos deben exponer y consumir el mismo `Instant` sin transformaciones parciales.
- No se introduciran campos duplicados ni wrappers adicionales.

### Validacion y errores
- Si `fechaLimite` viene vacio, el campo sigue siendo opcional.
- Si `fechaLimite` viene con formato invalido, backend responde `400 Bad Request`.
- Si el usuario no tiene hogar activo en frontend, no se enviara un request incompleto: el bloqueo ocurre en la UI con un estado visible.

## Diseno frontend

### Formulario de tarea
- La etiqueta de la seccion debe dejar claro que es una fecha y hora de vencimiento.
- El control pasa de seleccion de solo fecha a seleccion de fecha y hora.
- El valor del formulario se conserva como string ISO para evitar conversiones innecesarias en el servicio HTTP.
- En modo edicion, la tarea precarga el datetime exacto devuelto por la API.

### Categoria
- `categoria` pasa de texto libre a desplegable fijo.
- La lista debe ser corta y entendible. Propuesta inicial:
  - `LIMPIEZA`
  - `COMPRAS`
  - `COCINA`
  - `MASCOTAS`
  - `NINOS`
  - `MANTENIMIENTO`
  - `OTROS`
- En frontend se mostraran etiquetas legibles; el valor enviado puede mantenerse como texto simple estable.

### Hogar activo
- Si hay hogar activo, el formulario funciona sin pasos extra.
- Si no hay hogar activo, se muestra un bloque visible dentro de la pantalla indicando que hay que seleccionar o activar un hogar antes de crear la tarea.
- El usuario no debe descubrir este problema solo al pulsar guardar.

### Lista y detalle
- Donde ya se muestre `fechaLimite`, se debe aceptar que ahora incluye hora.
- Si alguna vista resume la fecha, podra formatearla amigablemente, pero sin perder la hora al volver a editar.

## Flujo de datos
- El usuario elige fecha y hora en el formulario.
- El formulario guarda `fechaLimite` como ISO completo.
- `TareaService` envia ese valor sin truncarlo.
- Backend lo deserializa como `Instant`, lo persiste y lo devuelve igual.
- La UI lo reutiliza para editar y para renderizado.

## Pruebas requeridas

### Backend
- crear tarea con `fechaLimite` datetime la persiste correctamente
- obtener tarea devuelve `fechaLimite` con hora real
- actualizar tarea conserva el nuevo datetime
- request con `fechaLimite` invalido devuelve `400`
- la respuesta publica mantiene `asignaciones` y resto del contrato sin regresiones

### Frontend
- `TareaService` envia `fechaLimite` como ISO completo
- el formulario precarga `fechaLimite` en edicion
- el control de fecha y hora actualiza el valor del formulario
- `categoria` se envia desde un selector fijo
- sin hogar activo se muestra el estado visible y no se intenta guardar silenciosamente

## Riesgos y mitigacion
- Riesgo: ruptura de contrato si otro cliente manda solo fecha.
  - Mitigacion: validar si existe algun consumidor externo antes de desplegar; si no existe, avanzar con contrato unico.
- Riesgo: problemas de zona horaria al serializar en frontend.
  - Mitigacion: trabajar de forma consistente con ISO completo y verificar round-trip crear -> leer -> editar.
- Riesgo: migracion de columna segun el motor de base de datos.
  - Mitigacion: revisar el tipo real en entorno de implementacion y preparar la migracion especifica del motor.
- Riesgo: categorias demasiado cerradas para casos reales.
  - Mitigacion: empezar con un set pequeno y revisar tras uso real; `OTROS` cubre el escape inicial.

## Criterios de aceptacion
- El usuario puede elegir una fecha y una hora reales de vencimiento al crear y editar tareas.
- La API guarda y devuelve `fechaLimite` con hora real.
- No se pierde la hora al recargar, listar o editar una tarea.
- `categoria` aparece como desplegable fijo.
- Cuando no hay hogar activo, el formulario lo comunica de forma visible antes del guardado.
