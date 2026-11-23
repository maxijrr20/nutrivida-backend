# Resultados de pruebas: catálogo y citas

Fecha: 2026-11-23 20:31 - API: http://localhost:8080/api/v1

| # | Caso | Resultado esperado | Resultado obtenido | |
| :--- | :--- | :--- | :--- | :---: |
| C1 | GET /servicios sin iniciar sesión (público) | 200 con los servicios | 200, 18 servicios | OK |
| C2 | GET /categorias (público) | 200 con total de servicios por categoría | 200, 4 categorías | OK |
| C3 | GET /nutricionistas (público) | 200 con días y jornada | 200, 4 nutricionistas | OK |
| C4 | POST /servicios sin token | 401 | 401 - Debes iniciar sesión: el token falta, no es válido o está vencido. | OK |
| C5 | POST /servicios con token de PACIENTE | 403 | 403 | OK |
| C6 | POST /servicios con token de ADMIN | 201 con código SV0xx | 201, código SV019 | OK |
| C7 | POST /servicios con precio 0 | 400 con error por campo | 400 | OK |
| C8 | PUT /servicios/{id} (ADMIN cambia el precio) | 200 con precio 25000 | 200 | OK |
| C9 | DELETE /servicios/{id} (ADMIN) | 204 | 204 | OK |
| C10 | GET del servicio eliminado | 404 (ya no está en el catálogo) | 404 | OK |
| C11 | POST /categorias con nombre repetido | 409 | 409 | OK |
| C12 | POST /categorias nueva (ADMIN) | 201 | 201 | OK |
| C13 | PUT /categorias/{id} renombrar (ADMIN) | 200 | 200 | OK |
| C14 | DELETE de una categoría con servicios | 409 | 409 | OK |
| C15 | DELETE de una categoría vacía | 204 | 204 | OK |
| A1 | POST /citas sin token | 401 | 401 - Debes iniciar sesión: el token falta, no es válido o está vencido. | OK |
| A2 | PACIENTE 1 solicita una cita | 201 en estado Pendiente | 201, cita #1 2028-01-30 12:30 | OK |
| A3 | PACIENTE 2 pide el MISMO horario | 409 horario tomado | 409 | OK |
| A4 | Cita con fecha pasada | 400 | 400 | OK |
| A5 | Hora fuera de la jornada del nutricionista | 400 | 400 | OK |
| A6 | Hora que no es bloque de 30 min (09:15) | 400 | 400 | OK |
| A7 | PACIENTE 2 lista sus citas | 200 sin la cita del paciente 1 | 200, 0 citas | OK |
| A8 | ADMIN lista todas las citas | 200 incluye la cita del paciente 1 | 200, 1 citas | OK |
| A9 | PACIENTE 2 abre /citas/{id} del paciente 1 | 403 | 403 | OK |
| A10 | PACIENTE 2 intenta cancelar la cita ajena | 403 | 403 | OK |
| A11 | PACIENTE 1 reprograma su cita | 200 en estado Reprogramada | 200, nueva hora 11:00 | OK |
| A12 | PACIENTE intenta cambiar el estado | 403 | 403 | OK |
| A13 | ADMIN confirma la cita | 200 en estado Confirmada | 200 | OK |
| A14 | PACIENTE 1 cancela su cita | 200 en estado Cancelada | 200 | OK |
| A15 | Cancelar una cita ya cancelada | 409 | 409 | OK |
| A16 | PACIENTE 2 toma el horario que se liberó | 201 | 201 | OK |
