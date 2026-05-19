# Diseno: identificadores publicos de API AppCasa

## Objetivo
Definir una regla transversal y estable para los identificadores expuestos por la API de AppCasa, evitando acoplar el contrato publico a IDs tecnicos internos (especialmente IDs numericos de catalogos y estados).

## Problema actual
- Las entidades principales usan `UUID` (no hay IDs secuenciales en agregados principales).
- La API devuelve entidades JPA directas en varios endpoints.
- El frontend consume campos tecnicos como `idPrioridad`, `idTipoMiembro`, `idTipoRecordatorio` e `idEstado`.
- Resultado: el contrato publico refleja detalles internos de base de datos y dificulta evolucionar catalogos o normalizacion.

## Decision de diseno
- Se aplica ruptura limpia de contrato (sin capa temporal de compatibilidad).
- Se separa estrictamente:
  - `identificador tecnico interno` (persistencia)
  - `identificador publico` (contrato API)
- En contrato publico:
  - `id` se reserva para identidad del propio recurso.
  - `codigo` se usa para claves de negocio y catalogos.
  - `...Id` solo para referencias a otros agregados principales expuestos.
  - `idXxx` de catalogos/estados queda prohibido.

## Reglas de identificadores

### 1) Agregados principales
- `Usuario`, `Miembro`, `Tarea`, `Recordatorio`, `Evento`, `Lista`: exponen `id` opaco estable (UUID serializado).
- `Hogar`: expone `codigo` como clave publica principal para navegacion funcional.

### 2) Catalogos y estados
- `Prioridad`, `TipoMiembro`, `TipoRecordatorio`, `TipoEvento`, `Estado`: exponen `codigo` semantico y opcionalmente `label`.
- Nunca se exponen IDs numericos internos de catalogo/estado.

### 3) Relaciones
- Relaciones a agregados principales: `...Id` (o `...Codigo` cuando el recurso principal sea por codigo, como `hogar`).
- Relaciones a catalogos/estados: `...Codigo`.

### 4) Serializacion y nombres
- DTOs publicos en camelCase.
- No se devuelven entidades JPA directas desde controladores.
- El nombre de campos no refleja columnas SQL ni claves foraneas internas.

## Contrato esperado por recurso (resumen)

### Tarea
- Request:
  - `hogarCodigo`
  - `titulo`, `descripcion`
  - `prioridadCodigo`
  - `categoria`
  - `fechaLimite`
  - `periodicidadCodigo`
  - `esPersonal`
  - `miembroIds` (opcional)
- Response:
  - `id`
  - `hogarCodigo`
  - `titulo`, `descripcion`
  - `prioridad` (`codigo`, `label`)
  - `categoria`, `fechaLimite`, `fechaCompletada`
  - `periodicidad` (`codigo`, `label`) si aplica
  - `estado` (`codigo`, `label`)
  - `asignaciones` con `miembroId` y datos minimos del miembro

### Miembro
- Request:
  - `hogarCodigo`
  - `tipoCodigo`
  - datos de perfil
- Response:
  - `id`
  - `hogarCodigo`
  - `tipo` (`codigo`, `label`)
  - datos de perfil
  - `estado` (`codigo`, `label`)

### Recordatorio
- Request:
  - `hogarCodigo`
  - `tipoCodigo`
  - `fechaHora`, `anticipacionMinutos`
  - referencias opcionales a agregados (`tareaId`, `miembroId`, `eventoId`)
- Response:
  - `id`
  - `hogarCodigo`
  - `tipo` (`codigo`, `label`)
  - datos funcionales
  - `estado` (`codigo`, `label`)

## Impacto tecnico

### Backend
- Introducir DTOs de request/response por recurso.
- Introducir mapeadores entidad -> DTO y request -> comando de aplicacion.
- Ajustar servicios para resolver catalogos por `codigo`.
- Cambiar rutas funcionales por `hogarCodigo` donde aplique.
- Mantener IDs internos de BD sin exponerlos en payload.

### Frontend
- Actualizar `domain.models.ts` para eliminar IDs numericos de catalogo/estado.
- Cambiar servicios para enviar `...Codigo`.
- Adaptar componentes que dependen de IDs numericos (ejemplo prioridad).

## Fuera de alcance
- No se redefine el modelo relacional interno completo.
- No se implementa versionado de API en esta iteracion.
- No se incluye compatibilidad dual de payloads.

## Criterios de aceptacion
- Ningun endpoint publico devuelve `idPrioridad`, `idTipoMiembro`, `idTipoRecordatorio` o `idEstado`.
- Ningun request publico requiere IDs numericos de catalogo/estado.
- Controladores no devuelven entidades JPA directamente.
- Frontend compila y funciona con contratos por `codigo`/DTO.
- Pruebas de contrato validan ausencia de campos tecnicos prohibidos.

## Riesgos y mitigacion
- Riesgo: ruptura amplia en frontend al quitar IDs numericos de golpe.
  - Mitigacion: migrar por modulos con tests de contrato y tipos estrictos.
- Riesgo: inconsistencia de codigos entre catalogos.
  - Mitigacion: definir enums/catalagos canonicos en backend y testearlos.

## Orden recomendado de aplicacion
1. Definir DTOs y convenciones compartidas (base comun).
2. Migrar `tareas` extremo a extremo (backend + frontend).
3. Migrar `miembros`.
4. Migrar `recordatorios`.
5. Extender la regla al resto de modulos.
