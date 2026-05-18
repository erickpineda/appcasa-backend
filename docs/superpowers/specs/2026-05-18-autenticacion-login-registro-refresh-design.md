# Diseno: autenticacion login, registro y refresh AppCasa

## Objetivo

Cerrar el vertical de autenticacion de `AppCasa` de extremo a extremo en backend y frontend, dejando operativos:

- login
- registro
- mantenimiento de sesion mediante refresh token
- cierre de sesion

El objetivo no es solo autenticar, sino establecer una base de sesion segura y extensible para el resto del producto.

## Contexto actual

El backend ya dispone de:

- `POST /api/v1/auth/login`
- `POST /api/v1/auth/registro`
- generacion de `access token` y `refreshToken`
- seguridad basada en JWT bearer para proteger el resto de endpoints

El frontend ya dispone de:

- pantalla unificada de `login` y `registro`
- `AuthService`
- `AuthGuard`
- `AuthInterceptor`

Actualmente el `refreshToken` solo existe como valor devuelto por backend, pero no hay flujo completo de renovacion, persistencia segura ni revocacion de sesion.

## Alcance

Incluye:

- completar `login` y `registro` en backend y frontend
- introducir refresh token persistido en backend
- enviar el refresh token en cookie `HttpOnly`
- implementar rotacion de refresh token
- implementar endpoint de `refresh`
- implementar `logout` con revocacion de sesion
- reintento automatico de peticiones tras renovacion de `access token`
- recuperacion silenciosa de sesion al arrancar el frontend
- pruebas automatizadas enfocadas en autenticacion

No incluye:

- login social
- verificacion de email
- recuperacion de contrasena
- gestion de dispositivos o listado de sesiones activas
- autenticacion multifactor

## Decisiones principales

### Modelo de sesion

Se usara un modelo hibrido:

- `access token` corto y stateless para autorizar llamadas API
- `refresh token` persistido y controlado por backend para continuidad de sesion

Esta combinacion permite mantener el modelo JWT ya existente para la API protegida, pero anade revocacion, rotacion y control de sesiones en el lado servidor.

### Transporte del refresh token

El `refresh token` no se expondra al frontend en el cuerpo de la respuesta ni se almacenara en `localStorage`.

Se enviara exclusivamente como cookie:

- `HttpOnly`
- con `SameSite` configurable por entorno
- con `Secure` desactivable en `local` y activado fuera de `local`

### Persistencia del refresh token

El backend persistira una sesion o refresh token asociado a usuario. La persistencia no guardara el valor plano del token, sino un hash estable para reducir impacto en caso de filtracion de base de datos.

Campos minimos recomendados:

- `id`
- `usuario`
- `tokenHash`
- `expiraEn`
- `creadoEn`
- `ultimoUsoEn`
- `revocadoEn`

### Politica de expiracion

- el `access token` seguira siendo de vida corta
- el `refresh token` tendra vida mas larga
- cuando expire el `access token`, el frontend intentara una renovacion automatica
- solo se forzara vuelta a login cuando el refresh token haya expirado, haya sido revocado o sea invalido

### Politica de rotacion

Cada llamada correcta a `POST /api/v1/auth/refresh` invalidara el refresh token anterior y emitira uno nuevo. Esto reduce el riesgo de reutilizacion y deja una base clara para endurecer seguridad mas adelante.

## Backend

### Endpoints

Se mantendran:

- `POST /api/v1/auth/login`
- `POST /api/v1/auth/registro`

Se anadiran:

- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`

### Contratos de respuesta

`login` y `registro` devolveran:

- `token`
- `usuario`

El `refresh token` se enviara por cookie, no en JSON.

`refresh` devolvera:

- nuevo `token`
- `usuario`

Se fija devolver tambien `usuario` en `refresh` para simplificar la recuperacion silenciosa de sesion y evitar depender de un endpoint adicional tipo `me`.

`logout` devolvera `204 No Content`. La prioridad es borrar cookie y revocar sesion.

### Modelo de dominio

Se anadira una entidad dedicada a sesion de autenticacion, por ejemplo `RefreshToken` o `SesionUsuario`.

Responsabilidades:

- crear sesion al autenticar
- buscar sesion por token recibido
- validar expiracion y revocacion
- rotar token en refresh
- revocar token en logout

La logica de autenticacion debe permanecer fuera del controlador y concentrarse en servicio de aplicacion.

### Flujo de login y registro

1. Backend valida credenciales o crea usuario
2. Genera `access token`
3. Genera `refresh token`
4. Calcula hash del refresh token
5. Persiste la sesion
6. Devuelve `access token` y usuario
7. Adjunta cookie `HttpOnly` con el refresh token

### Flujo de refresh

1. Backend lee la cookie de refresh
2. Calcula hash y busca sesion persistida
3. Verifica existencia, no revocacion y no expiracion
4. Revoca o marca como rotado el token anterior
5. Emite nuevo refresh token
6. Persiste la nueva sesion o actualiza la existente
7. Devuelve nuevo `access token`
8. Sobrescribe la cookie con el nuevo refresh token

### Flujo de logout

1. Backend lee la cookie actual
2. Revoca la sesion correspondiente si existe
3. Borra la cookie del cliente
4. Devuelve respuesta vacia

### Seguridad y errores

Respuestas esperadas:

- credenciales incorrectas: `401 Unauthorized`
- email ya registrado: `409 Conflict`
- refresh token invalido, revocado o expirado: `401 Unauthorized`
- payload invalido: `400 Bad Request`

Se evitara responder con diferencias que permitan enumerar usuarios. `login` debe mantener un mensaje de error generico.

## Frontend

### Estado de sesion

El frontend almacenara:

- `access token`
- usuario autenticado

No almacenara:

- `refresh token`

### AuthService

El servicio de autenticacion gestionara:

- `login`
- `registro`
- `refresh`
- `logout`
- recuperacion silenciosa de sesion al arrancar

Las llamadas `login`, `registro`, `refresh` y `logout` usaran `withCredentials: true` para permitir que la cookie de refresh viaje entre navegador y backend.

### Interceptor

El interceptor:

- anadira el `Authorization: Bearer <token>` cuando exista `access token`
- al recibir `401`, intentara una renovacion una sola vez
- reintentara la peticion original tras refrescar con exito
- limpiara la sesion y redirigira a `/auth` si el refresh falla

Debe evitar bucles infinitos. La llamada a `refresh` no debe disparar otro ciclo de refresh si tambien responde `401`.

### Bootstrap de sesion

Al arrancar la app:

1. si existe `access token`, se usa la sesion local
2. si no existe `access token`, el frontend intentara `refresh` silencioso con la cookie
3. si `refresh` funciona, recupera sesion sin mostrar login
4. si falla, deja al usuario en `/auth`

### Pantalla de autenticacion

La pantalla actual unificada de `login` y `registro` se conserva.

Cambios esperados:

- adaptar mensajes de error al nuevo contrato backend
- asegurar flujo de navegacion tras login y registro correctos
- eliminar cualquier dependencia de `refreshToken` en cliente si existiera

## Datos y almacenamiento

### Backend

Persistira una nueva tabla para sesiones de autenticacion. El cambio debe introducirse mediante migracion de base de datos.

### Frontend

Se mantendra almacenamiento local minimo para:

- `access token`
- usuario

Esto minimiza impacto sobre la app actual y simplifica integracion con guard e interceptor ya presentes.

## Testing

### Backend

Pruebas recomendadas:

- login correcto devuelve `access token` y cookie de refresh
- registro correcto crea usuario, crea sesion y devuelve cookie
- login incorrecto devuelve `401`
- registro con email duplicado devuelve `409`
- refresh correcto rota token y devuelve nueva cookie
- refresh revocado o expirado devuelve `401`
- logout revoca la sesion activa y limpia cookie

### Frontend

Pruebas recomendadas:

- login guarda sesion y navega al dashboard
- registro guarda sesion y navega al dashboard
- interceptor reintenta una peticion tras refresh correcto
- fallo en refresh limpia sesion y redirige a `/auth`
- bootstrap silencioso recupera sesion cuando existe cookie valida

## Riesgos y mitigaciones

### Complejidad extra por persistencia

Riesgo: la gestion de refresh tokens persistidos introduce mas piezas que el JWT puro.

Mitigacion: mantener la entidad de sesion pequena, con responsabilidades claras y pruebas centradas en los flujos criticos.

### Bucle de refresh en frontend

Riesgo: el interceptor puede entrar en reintentos infinitos.

Mitigacion: marcar peticiones ya reintentadas y excluir el endpoint de refresh del propio mecanismo de renovacion.

### Cookies en local

Riesgo: configuraciones de `SameSite`, `Secure` o CORS pueden impedir el flujo en desarrollo local.

Mitigacion: parametrizar cookie y CORS por entorno y verificar explicitamente el flujo local con `withCredentials`.

## Resultado esperado

Al finalizar esta iteracion, `AppCasa` tendra autenticacion funcional y coherente entre backend y frontend, con login y registro operativos, sesiones renovables, refresh token protegido en cookie `HttpOnly`, revocacion en logout y una base segura para empezar el resto de verticales del producto.
