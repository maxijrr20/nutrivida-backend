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
- [ ] Iniciamos la estructura del proyecto, la conexión a datos y los primeros endpoints; indicamos el estado real de cada avance.
- [ ] Documentamos la API con OpenAPI/Swagger y explicamos cómo consultarla y probar las rutas disponibles.
- [ ] Relacionamos las operaciones de la API con las funcionalidades del frontend.
- [ ] Investigamos y justificamos la protección de contraseñas sin incluir credenciales reales.
- [ ] Diseñamos el flujo JWT y los permisos, y distinguimos qué controles están implementados y cuáles pendientes.
- [ ] Incluimos el diagrama de secuencia propio y consideramos los casos de rechazo.
- [ ] Registramos pruebas reales de los avances y separamos los resultados de los casos aún pendientes.
- [ ] El README explica cómo ejecutar o desplegar y comprobar lo que ya está construido.
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

* **Lenguaje:** Java 17+
* **Framework:** Spring Boot 3.x (Spring Web, Spring Data JPA, Spring Security, validation, PostgreSQL DRIVER)
* **Gestor de dependencias:** Maven / Gradle
* **Base de Datos:** PostgreSQL (AWS RDS - Subred Privada, Puerto 5432 aislado)
* **Servicios AWS:** EC2 
