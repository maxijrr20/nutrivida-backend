# 🥗 NutriVida - Backend Service (Spring Boot)

Este repositorio contiene la API REST y los servicios backend desarrollados con **Spring Boot** para la plataforma de la **Clínica Nutricional NutriVida**.

---

## 📌 Checklist de Entrega / Control de Avance

- [x] Creamos un repositorio nuevo para el backend y conservamos separado el frontend.
- [x] El README identifica proyecto, caso, alcance e integrantes.
- [x] El README del backend enlaza al frontend y el del frontend enlaza al backend.
- [x] Justificamos la tecnología elegida (Spring Boot) y su ejecución en EC2 o Lambda, según corresponda.
- [ ] Definimos la base de datos y mantenemos su diagrama actualizado, sin diccionario de datos.
- [x] Los diagramas en Drive tienen enlaces directos y acceso de lectura para el docente.
- [x] Explicamos las responsabilidades del backend y de los integrantes.
- [x] Iniciamos la estructura del proyecto, la conexión a datos y los primeros endpoints; indicamos el estado real de cada avance.
- [x] Documentamos la API con OpenAPI/Swagger y explicamos cómo consultarla y probar las rutas disponibles.
- [ ] Relacionamos las operaciones de la API con las funcionalidades del frontend.
- [x] Investigamos y justificamos la protección de contraseñas sin incluir credenciales reales.
- [x] Diseñamos el flujo JWT y los permisos, y distinguimos qué controles están implementados y cuáles pendientes.
- [x] Incluimos el diagrama de secuencia propio y consideramos los casos de rechazo.
- [ ] Registramos pruebas reales de los avances y separamos los resultados de los casos aún pendientes.
- [x] El README explica cómo ejecutar o desplegar y comprobar lo que ya está construido.
- [ ] Organizamos el trabajo para dos semanas y dejamos claros los siguientes pasos.
- [ ] Todos pueden explicar las decisiones, el código inicial y el avance del equipo.

---

## ℹ️ Identificación del Proyecto y Equipo

* **Proyecto:** NutriVida
* **Equipo:** Grupo N°3
* **Integrantes:**
  * Maximiliano Andrés Valenzuela Barrera - `max.valenzuelab@duocuc.cl`
  * Génesis Cerda - `gene.cerda@duocuc.cl`
  * Yoximar Ruiz - `yox.ruiz@duocuc.cl`
* **Caso:** Clínica Nutricional NutriVida
* **Alcance:** Proveer una API REST en **Java / Spring Boot** para la gestión persistente de contenidos, citas nutricionales, catálogo de servicios y autenticación de usuarios/pacientes, conectada a una base de datos PostgreSQL en AWS RDS y desplegada sobre infraestructura AWS (EC2 / Lambda).

---

## 🔗 Enlace al Repositorio Frontend

* **Repositorio Frontend:** [NutriVidaProyecto (React + Vite)](https://github.com/maxijrr20/NutriVidaProyecto.git)
* **Documentación ERS en Google Drive:** [Enlace a carpeta ERS en Drive](https://drive.google.com/drive/folders/1ncapMCk5xU48v4hHfm7bWwrtKheoQHpF?usp=drive_link)

---

## 🛠️ Tecnologías y Arquitectura Backend

* **Lenguaje:** Java 21
* **Framework:** Spring Boot 4.1 (Spring Web MVC, Spring Data JPA, Spring Security, Validation, driver PostgreSQL)
* **Gestor de dependencias:** Maven (wrapper `mvnw`)
* **Base de Datos:** PostgreSQL (AWS RDS - Subred Privada, Puerto 5432 aislado)
* **Servicios AWS:** EC2

* **Migraciones de base de datos:** Flyway (`src/main/resources/db/migration`)
* **Seguridad:** Spring Security + OAuth2 Resource Server (JWT HS256) + BCrypt
* **Documentación de la API:** springdoc-openapi (Swagger UI)

---

## ✅ Estado del trabajo (avance lunes 16 de noviembre)

| Operación | Ruta | Estado | Diseñado | Implementado | Probado (Swagger/Postman) |
| :--- | :--- | :--- | :---: | :---: | :---: |
| Tabla de usuarios (y roles) | Migración `V1__crear_tablas_rol_y_usuario.sql` | **Listo** | ✔ | ✔ | ✔ |
| Registro (crear usuario) | `POST /api/v1/auth/register` | **Listo** | ✔ | ✔ | ✔ |
| Inicio de sesión con JWT | `POST /api/v1/auth/login` | **Listo** | ✔ | ✔ | ✔ |
| Usuario actual | `GET /api/v1/auth/me` | **Listo** | ✔ | ✔ | ✔ |
| Listar usuarios | `GET /api/v1/usuarios` | **En avance** | ✔ | ✔ | Pendiente |
| Ver un usuario | `GET /api/v1/usuarios/{id}` | **En avance** | ✔ | ✔ | Pendiente |
| Modificar usuario | `PUT /api/v1/usuarios/{id}` | **En avance** | ✔ | ✔ | Pendiente |
| Eliminar (desactivar) usuario | `DELETE /api/v1/usuarios/{id}` | **En avance** | ✔ | ✔ | Pendiente |
| Crear usuario con rol (ADMIN) | `POST /api/v1/usuarios` | **En avance** | ✔ | ✔ | Pendiente |
| Cambiar rol (ADMIN) | `PATCH /api/v1/usuarios/{id}/rol` | **En avance** | ✔ | ✔ | Pendiente |
| Catálogo de servicios (CRUD) | — | Pendiente (semana 16) | ✔ | — | — |
| Citas con regla de horario ocupado (409) | — | Pendiente (semana 16) | ✔ | — | — |
| Integración con el frontend React | — | Pendiente | — | — | — |

> **Listo:** se probó en Swagger o Postman y el resultado quedó guardado en la base de datos. **En avance:** la ruta y su lógica están implementadas y con commits, pero falta registrar sus pruebas.

---

## 🗂️ Organización del backend (capas)

```mermaid
flowchart LR
    R["React (NutriVidaProyecto)"] -- "HTTP + JSON<br/>Authorization: Bearer" --> C
    subgraph API["Spring Boot (EC2)"]
        F["Filtro JWT<br/>(Spring Security)"] --> C["Controladores<br/>AuthController · UsuarioController"]
        C --> S["Servicios<br/>AuthService · UsuarioService"]
        S --> D["Repositorios JPA<br/>UsuarioRepository · RolRepository"]
    end
    D -- "JDBC :5432" --> DB[("PostgreSQL<br/>AWS RDS")]
```

| Capa | Paquete | Qué hace |
| :--- | :--- | :--- |
| Controladores | `controller` | Reciben la solicitud HTTP, validan el cuerpo con `@Valid`, aplican `@PreAuthorize` y devuelven el estado HTTP. |
| Servicios | `service` | Reglas del negocio: correo y RUT únicos, rol asignado por el servidor, propietario o ADMIN, desactivación lógica. |
| Acceso a datos | `repository` | Consultas a PostgreSQL con Spring Data JPA. |
| Modelos y DTO | `model`, `dto` | Entidades de las tablas y datos de entrada y salida de la API (nunca se devuelve el hash). |
| Seguridad | `config/SecurityConfig`, `security` | BCrypt, emisión y verificación del JWT, CORS y respuestas 401/403. |
| Errores | `exception` | Convierte los errores en JSON con el estado correcto (400, 401, 403, 404, 409). |
| Validaciones | `validation` | `@RutValido`: formato y dígito verificador del RUT (módulo 11). |

---

## 🔌 Endpoints implementados

Todas las rutas usan el prefijo versionado `/api/v1`. El detalle (cuerpos, ejemplos y errores) está en Swagger UI.

| Método y ruta | Quién puede usarla | Entrada | Éxito | Errores | Pantalla del frontend |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `POST /api/v1/auth/register` | Público | `rut`, `nombre`, `email`, `telefono` (opcional), `password` | `201` usuario creado con rol `PACIENTE` | `400` datos inválidos · `409` correo o RUT ya registrados | `RegistroPage` |
| `POST /api/v1/auth/login` | Público | `email`, `password` | `200` `accessToken`, `tokenType: "Bearer"`, `expiresIn`, `usuario` | `400` campos vacíos · `401` "Correo o contraseña incorrectos." | `LoginPage` |
| `GET /api/v1/auth/me` | Con token | — | `200` datos del dueño del token | `401` | Navbar / sesión |
| `POST /api/v1/usuarios` | ADMIN | `rut`, `nombre`, `email`, `telefono` (opcional), `password`, `rol` | `201` usuario creado con el rol elegido | `400` · `401` · `403` · `409` correo o RUT ya registrados | `AdminUsuarios` |
| `GET /api/v1/usuarios` | ADMIN | — | `200` lista | `401` · `403` | `AdminDashboardPage` |
| `GET /api/v1/usuarios/{id}` | ADMIN o el mismo usuario | `id` | `200` | `401` · `403` cuenta ajena · `404` | Perfil / administración |
| `PUT /api/v1/usuarios/{id}` | ADMIN o el mismo usuario | `nombre`, `email`, `telefono`, `password` (opcional) | `200` | `400` · `401` · `403` · `404` · `409` correo de otra cuenta | Perfil / administración |
| `PATCH /api/v1/usuarios/{id}/rol` | ADMIN | `rol`: `ADMIN`, `NUTRICIONISTA` o `PACIENTE` | `200` usuario con el rol nuevo | `400` rol inválido · `401` · `403` · `404` · `409` el admin intenta quitarse su propio rol | `AdminUsuarios` |
| `DELETE /api/v1/usuarios/{id}` | ADMIN | `id` | `204` (desactivación lógica) | `401` · `403` · `404` · `409` el admin intenta desactivarse | `AdminDashboardPage` |

**Ejemplo de login (datos ficticios)**

```json
// Solicitud (cliente)
{ "email": "camila.rojas@correo.cl", "password": "Clave-segura-2026" }

// Respuesta 200 (servidor)
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "usuario": { "id": 2, "rut": "12345678-5", "nombre": "Camila Rojas", "email": "camila.rojas@correo.cl",
               "telefono": "+56912345678", "rol": "PACIENTE", "activo": true, "creadoEn": "2026-11-16T09:23:50" }
}
```

Campos que **genera el servidor**: `id`, `rol` (en el registro siempre `PACIENTE`), `activo`, `creadoEn`, `accessToken` y `expiresIn`. Si el cliente envía un `rol` en el registro, se ignora.

---

## 📘 Swagger UI (documentación viva)

* Local: `http://localhost:8080/swagger-ui.html`
* EC2: `http://<IP-PUBLICA-EC2>:8080/swagger-ui.html` ← **reemplazar por la IP real del equipo**
* Especificación OpenAPI en JSON: `/v3/api-docs`

Para probar una ruta protegida: ejecutar `POST /api/v1/auth/login`, copiar el `accessToken`, presionar **Authorize** y pegarlo (sin escribir "Bearer").

---

## 🔐 Seguridad

### Contraseñas: BCrypt

* Se guardan con **BCrypt** (`BCryptPasswordEncoder` de Spring Security) con **costo 12**. Nunca se guarda la contraseña en texto plano ni se devuelve el hash en la API.
* **Por qué BCrypt:** es un algoritmo hecho para contraseñas. Agrega una **sal aleatoria** a cada hash (dos usuarios con la misma clave tienen hashes distintos) y es **lento a propósito**: el costo 12 hace que probar millones de claves sea muy caro. Un hash rápido como SHA-256 no sirve para contraseñas. OWASP recomienda Argon2id como primera opción y acepta BCrypt con costo 10 o más cuando Argon2id no está disponible. Elegimos BCrypt porque viene incluido en Spring Security, sin dependencias extra.
* **Configuración:** costo 12 y contraseñas de 8 a 72 caracteres (BCrypt solo considera los primeros 72 bytes).
* **Fuentes:** [OWASP Password Storage Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html) · [Spring Security: Password Storage](https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html)

### JWT

| Decisión | Valor |
| :--- | :--- |
| Quién emite y verifica | El mismo backend (`JwtService` emite; `SecurityConfig.jwtDecoder` verifica). |
| Algoritmo | HS256 (HMAC-SHA256). El verificador **solo acepta HS256**. |
| Clave de firma | Variable de entorno `JWT_SECRET` (mínimo 32 caracteres). No está en el repositorio ni en React. |
| Claims | `sub` (id del usuario), `rol`, `iss` = `nutrivida-backend`, `aud` = `nutrivida-frontend`, `iat`, `exp`. Sin contraseñas ni datos sensibles. |
| Vigencia | 60 minutos (`JWT_DURACION_MINUTOS`). Alcanza para agendar una cita sin exponer un token por días. |
| Qué se comprueba | Firma, algoritmo, `exp` (con 60 s de tolerancia de reloj), `iss` y `aud`. Además, en cada solicitud se comprueba que la cuenta siga activa. |
| Cierre de sesión y renovación | Primera versión sin refresh token: al vencer el token se vuelve a iniciar sesión. Cerrar sesión borra el token en el frontend; un token copiado sigue sirviendo hasta su `exp`, **salvo que la cuenta se desactive** (desde ese momento responde 401). |
| Token en el frontend | Pendiente de implementar en React. Se evaluará `sessionStorage` o `localStorage` para mantener la sesión al recargar (IE3.3.2), con su riesgo ante XSS. |
| CORS | Solo `http://localhost:5173` y `https://maxijrr20.github.io` (`CORS_ORIGENES`). CORS no reemplaza la autorización. |

**Roles y permisos (usuarios):** el registro público siempre asigna `PACIENTE`; solo el `ADMIN` puede crear usuarios con otro rol (`POST /usuarios`) o cambiar el rol de una cuenta (`PATCH /usuarios/{id}/rol`). Listar y desactivar son solo para `ADMIN` (`@PreAuthorize`, 403 para otros roles). Ver y modificar una cuenta lo pueden hacer el `ADMIN` o el mismo usuario: el id se compara con el `sub` del token, así que cambiar el id en la URL responde 403. El primer administrador se crea al iniciar con `ADMIN_EMAIL` y `ADMIN_PASSWORD`.

### Diagrama de secuencia: login y ruta protegida

```mermaid
sequenceDiagram
    participant R as React
    participant F as Filtro JWT (Spring Security)
    participant C as AuthController / UsuarioController
    participant S as AuthService / UsuarioService
    participant DB as PostgreSQL (RDS)

    R->>C: POST /api/v1/auth/login (HTTPS: email y password)
    C->>S: login(datos)
    S->>DB: SELECT usuario por email
    DB-->>S: usuario + password_hash
    alt correo no existe, clave incorrecta o cuenta desactivada
        S-->>R: 401 "Correo o contraseña incorrectos." (mensaje genérico)
    else credenciales correctas
        S->>S: BCrypt.matches() y firma JWT HS256 (sub, rol, iss, aud, exp)
        S-->>R: 200 accessToken, tokenType Bearer, expiresIn
    end

    R->>F: GET /api/v1/usuarios (Authorization: Bearer token)
    alt sin token, firma alterada, vencido o iss/aud incorrectos
        F-->>R: 401
    else token válido
        F->>C: rol del token -> ROLE_ADMIN / ROLE_PACIENTE
        alt @PreAuthorize: el rol no es ADMIN
            C-->>R: 403
        else rol permitido
            C->>S: listar()
            S->>DB: SELECT usuarios
            DB-->>S: filas
            S-->>R: 200 lista (sin password_hash)
        end
    end
```

---

## 🧪 Pruebas registradas

| # | Caso | Resultado esperado | Resultado obtenido |
| :--- | :--- | :--- | :--- |
| 1 | Registro con datos válidos | 201, rol `PACIENTE`, fila nueva en `usuario` con hash BCrypt | |
| 2 | Registro con correo o RUT repetido | 409, sin usuario duplicado | |
| 3 | Registro con RUT inválido o contraseña corta | 400 con el error por campo | |
| 4 | Registro enviando `"rol": "ADMIN"` | 201 con rol `PACIENTE` (el servidor ignora el rol) | |
| 5 | Login correcto | 200 con `accessToken`, `tokenType` y `expiresIn` | |
| 6 | Login con clave incorrecta o correo inexistente | 401 con el mismo mensaje genérico | |
| 7 | `GET /auth/me` sin token / con token alterado | 401 | |
| 8 | `GET /usuarios` con token de PACIENTE | 403 | |
| 9 | PACIENTE consulta `GET /usuarios/{id}` de otra persona | 403 | |
| 10 | Reiniciar el backend y volver a iniciar sesión | Los usuarios siguen en la base de datos | |
| 11 | ADMIN cambia el rol de un paciente a `NUTRICIONISTA` | 200 y el rol queda guardado | |
| 12 | PACIENTE intenta `PATCH /usuarios/{id}/rol` | 403 | |
| 13 | ADMIN intenta quitarse su propio rol | 409 | |
| 14 | Frontend: registro e inicio de sesión llaman a la API (carga, éxito y error) | Mensajes visibles en pantalla | |

---

## ▶️ Cómo ejecutar

**Requisitos:** Java 21 y una base PostgreSQL (RDS o local). No hace falta instalar Maven: se usa `./mvnw`.

Variables de entorno (no se suben al repositorio):

| Variable | Ejemplo | Para qué |
| :--- | :--- | :--- |
| `DB_URL` | `jdbc:postgresql://<endpoint-rds>:5432/nutrivida` | Conexión a PostgreSQL |
| `DB_USER` / `DB_PASSWORD` | `postgres` / *(la del equipo)* | Credenciales de la base |
| `JWT_SECRET` | *(texto aleatorio de 32+ caracteres)* | Clave de firma del JWT |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | `admin@nutrivida.cl` / *(la del equipo)* | Crea el primer ADMIN si no existe |
| `JWT_DURACION_MINUTOS` | `60` | Vigencia del token (opcional) |
| `CORS_ORIGENES` | `http://localhost:5173,https://maxijrr20.github.io` | Orígenes permitidos (opcional) |

```bash
# Linux / EC2
export DB_URL="jdbc:postgresql://<endpoint-rds>:5432/nutrivida"
export DB_USER="postgres"
export DB_PASSWORD="<clave>"
export JWT_SECRET="$(openssl rand -base64 48)"
export ADMIN_EMAIL="admin@nutrivida.cl"
export ADMIN_PASSWORD="<clave-admin>"
chmod +x mvnw
./mvnw spring-boot:run
```

Al iniciar, Flyway crea las tablas `rol` y `usuario` (solo la primera vez) y carga los roles. Luego se abre Swagger UI en el puerto 8080.

> La base `nutrivida` debe existir antes de iniciar: `CREATE DATABASE nutrivida;`
