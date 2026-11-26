# Resultados de pruebas de la API

Fecha: 2026-11-21 00:43 - API: http://localhost:8080/api/v1

| # | Caso | Resultado esperado | Resultado obtenido | |
| :--- | :--- | :--- | :--- | :---: |
| 1 | Registro con datos válidos | 201, rol PACIENTE | 201, rol PACIENTE | OK |
| 2a | Registro con correo repetido | 409 | 409 | OK |
| 2b | Registro con RUT repetido | 409 | 409 | OK |
| 3 | Registro con RUT inválido y contraseña corta | 400 con error por campo | 400 | OK |
| 4 | Registro enviando rol ADMIN | 201 con rol PACIENTE | 201, rol PACIENTE | OK |
| 5 | Login correcto | 200 con accessToken y expiresIn | 200, expiresIn 3600 | OK |
| 6 | Login con clave incorrecta / correo inexistente | 401 con el mismo mensaje | 401 | OK |
| 7a | GET /api/v1/auth/me sin token | 401 | 401 - Debes iniciar sesión: el token falta, no es válido o está vencido. | OK |
| 7b | GET /api/v1/auth/me con token alterado | 401 | 401 - Debes iniciar sesión: el token falta, no es válido o está vencido. | OK |
| 7c | GET /api/v1/auth/me con token válido | 200 | 200, paciente1.91852@correo.cl | OK |
| 8 | GET /api/v1/usuarios con token de PACIENTE | 403 | 403 | OK |
| 8b | GET /api/v1/usuarios con token de ADMIN | 200 con la lista | 200, 3 usuarios | OK |
| 9 | PACIENTE consulta /usuarios/{id} de otra persona | 403 | 403 | OK |
| 9b | PACIENTE modifica la cuenta de otra persona | 403 | 403 | OK |
| 9c | PACIENTE modifica su propia cuenta | 200 | 200, nombre: Paciente Prueba Uno Editado | OK |
| 10 | Login del ADMIN guardado en PostgreSQL (repetir tras reiniciar) | 200 | 200 | OK |
| 11 | ADMIN cambia el rol de un paciente a NUTRICIONISTA | 200 con el rol nuevo | 200, rol NUTRICIONISTA | OK |
| 12 | PACIENTE intenta cambiar un rol | 403 | 403 | OK |
| 13 | ADMIN intenta quitarse su propio rol | 409 | 409 | OK |
| 15 | ADMIN crea un usuario NUTRICIONISTA | 201 con rol NUTRICIONISTA | 201 | OK |
| 16 | ADMIN desactiva un usuario | 204 | 204 | OK |
| 17 | Usuario desactivado intenta iniciar sesión | 401 | 401 | OK |
