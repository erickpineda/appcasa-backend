# Diseno: perfil, ajustes basicos y logout AppCasa

## Objetivo

Anadir una primera pantalla de `perfil` en el frontend de `AppCasa` para cubrir dos necesidades inmediatas:

- ofrecer un punto claro y estable para `cerrar sesion`
- preparar la ubicacion natural de futuros ajustes de usuario

En esta iteracion se busca resolver la ausencia actual de `logout` en la interfaz sin sobredisenar una gestion completa de preferencias.

## Contexto actual

El frontend ya dispone de:

- autenticacion funcional con `login`, `registro`, `refresh` y `logout` en `AuthService`
- `AuthGuard` para proteger rutas autenticadas
- `dashboard` como primera pantalla tras autenticacion
- cabecera en el `dashboard` con acciones rapidas

Actualmente el `logout` existe a nivel de servicio pero no hay una accion visible en la UI que permita al usuario cerrar sesion de forma natural.

## Alcance

Incluye:

- crear una nueva pantalla `perfil`
- anadir ruta protegida `/perfil`
- anadir acceso a `/perfil` mediante icono en la cabecera del `dashboard`
- mostrar datos basicos del usuario autenticado
- mostrar una seccion de ajustes visibles pero no editables aun
- anadir boton `Cerrar sesion`
- cerrar sesion incluso si la revocacion remota falla
- pruebas frontend enfocadas en navegacion y logout

No incluye:

- persistencia real de cambios de `tema` o `locale`
- edicion de perfil
- subida de avatar
- endpoint backend adicional para perfil
- nueva arquitectura de navegacion global

## Decisiones principales

### Ruta y acceso

Se anadira una ruta protegida `perfil` dentro del bloque ya protegido por `AuthGuard`.

El acceso a esta pantalla se hara mediante un icono de perfil en la cabecera del `dashboard`. Esta decision aprovecha una zona de accion ya existente y evita sobrecargar la barra inferior con una seccion nueva en esta fase.

### Fuente de datos

La pantalla `perfil` leerá el usuario directamente desde `AuthService`.

No se anadira en esta iteracion una llamada adicional al backend para obtener perfil, porque los datos minimos ya estan disponibles en sesion local y son suficientes para el alcance actual.

### Ajustes basicos

La pantalla mostrara al menos:

- `Tema`
- `Idioma`

Ambos apareceran como ajustes visibles con copy de `Proximamente`, sin controles interactivos persistentes. Esto permite preparar la estructura del producto sin fingir funcionalidad incompleta.

### Logout

El boton `Cerrar sesion` usara `AuthService.logout()`.

Comportamiento esperado:

1. intentar revocar la sesion en backend
2. limpiar la sesion local del frontend
3. redirigir a `/auth` con `replaceUrl`

Si la llamada remota falla, igualmente se limpiara la sesion local y se redirigira a `/auth`. Para el usuario, salir debe ser una accion confiable y predecible.

## Arquitectura frontend

### Nueva feature de perfil

Se creara una nueva feature `perfil` siguiendo el patron actual de modulos y paginas del frontend.

Piezas minimas:

- `perfil.module.ts`
- `perfil.page.ts`
- `perfil.page.html`

Opcionalmente se podra anadir un `scss` si hace falta, pero no es obligatorio para esta primera version.

### Routing

La ruta `/perfil` se incorporara al `AppRoutingModule` dentro del bloque autenticado existente. No debe quedar accesible desde fuera de sesion.

### Dashboard

La cabecera del `dashboard` anadira un icono de perfil en el slot derecho. Ese icono navegara a `/perfil`.

Debe convivir con el acceso ya existente a `familia` sin desplazarlo de forma confusa.

### Pantalla de perfil

La pagina tendra tres zonas:

1. cabecera con titulo y opcion de volver
2. tarjeta o bloque principal con datos del usuario autenticado
3. seccion de ajustes y accion de `Cerrar sesion`

Datos minimos mostrados:

- nombre
- apellidos, si existen
- email

La seccion de ajustes mostrara items como:

- `Tema`: valor actual o texto `Proximamente`
- `Idioma`: valor actual o texto `Proximamente`

El boton `Cerrar sesion` ira separado visualmente del resto, con tratamiento de accion destructiva.

## Estados y errores

### Usuario no disponible

Si se navega a `/perfil` sin usuario cargado pero la sesion aun puede restaurarse, el comportamiento del arranque silencioso ya deberia haber resuelto ese caso antes.

Si aun asi la pagina se renderiza sin usuario valido, se redirigira a `/auth` con `replaceUrl`. No se anadira en esta iteracion un estado vacio intermedio para evitar complejidad innecesaria.

### Fallo en logout

Si el backend devuelve error en `logout`, el frontend no debe bloquear al usuario en sesion.

La prioridad funcional es:

- limpiar tokens y usuario local
- navegar a `/auth`

El error puede ignorarse a nivel de UX en esta iteracion o mostrarse solo si no empeora la experiencia.

## Testing

Pruebas recomendadas:

- la ruta `/perfil` queda protegida por `AuthGuard`
- la pagina muestra los datos del usuario desde `AuthService`
- el icono de cabecera del `dashboard` navega a `/perfil`
- pulsar `Cerrar sesion` invoca `logout()` y navega a `/auth`
- si `logout()` falla, igualmente se limpia sesion y se navega a `/auth`

No hace falta ampliar backend para esta iteracion salvo regresiones puntuales del endpoint ya existente.

## Riesgos y mitigaciones

### Pantalla demasiado vacia

Riesgo: que `perfil` parezca una pagina hueca si solo tiene logout.

Mitigacion: mostrar datos reales del usuario y una seccion de ajustes visibles con copy claro de `Proximamente`.

### Navegacion inconsistente

Riesgo: que el acceso a `perfil` quede escondido o compita con otras acciones.

Mitigacion: usar un icono claro en la cabecera del `dashboard`, manteniendo el resto de acciones ya conocidas.

### Logout incompleto

Riesgo: depender demasiado de la respuesta del backend para salir de la app.

Mitigacion: tratar el logout como exito local aunque falle la revocacion remota.

## Resultado esperado

Al finalizar esta iteracion, el usuario autenticado podra entrar en una pantalla de `perfil`, ver sus datos basicos, identificar el espacio donde iran `tema` e `idioma`, y cerrar sesion desde una accion visible, clara y fiable dentro del frontend.
