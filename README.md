# 🛡️ SafeZone

SafeZone es una aplicación móvil desarrollada en **Android Studio** que permite a los ciudadanos reportar incidencias sociales de manera rápida, organizada y geolocalizada.

La aplicación permite a los ciudadanos **reportar incidencias sociales** como basura, vandalismo, alumbrado público, noticias comunitarias, entre otras, utilizando **geolocalización**, **evidencias multimedia** y un **sistema de seguimiento** del estado del reporte.

---

## 👥 Equipo de Desarrollo

* Wilkins Figuereo Jiménez
* Yeraldo Novas Eusebio

---

## 🧠 Descripción General

**SafeZone** es una aplicación móvil orientada a la participación ciudadana y la gestión de incidencias comunitarias. Permite reportar situaciones como problemas ambientales, vandalismo, seguridad, alumbrado público y noticias locales, incorporando evidencias multimedia y ubicación en tiempo real.

La plataforma facilita la comunicación entre ciudadanos, moderadores, administradores y entidades responsables, promoviendo una respuesta más eficiente y transparente ante los problemas sociales.

El backend de la aplicación está implementado con **Supabase**, encargado de la autenticación, persistencia de datos y almacenamiento de archivos.

---

## 🛠️ Tecnologías Utilizadas

### 📱 Frontend (Android)

* Kotlin
* Jetpack Compose
* Material 3
* Navigation Compose
* Google Maps Compose
* Coil (carga de imágenes)

### 🌐 Backend

* Supabase

  * Autenticación (email, teléfono, redes sociales)
  * Base de datos (PostgreSQL)
  * Storage (imágenes y videos)

### ☁️ APIs Externas

* **Google Cloud Platform (GCP)**

  * Google Maps API
  * Google Location Services
  * Geocodificación y visualización de mapas

### ⚙️ Otras Librerías

* Ktor Client
* Kotlinx Serialization
* Coroutines
* WorkManager
* Accompanist

---

ación de mapas

### ⚙️ Otras Librerías

* Ktor Client
* Kotlinx Serialization
* Coroutines
* WorkManager
* Accompanist

---

## 👤 Roles del Sistema

* **Ciudadano**: crea reportes y da seguimiento.
* **Moderador**: revisa y modera contenido.
* **Administrador**: gestiona usuarios, reportes y estadísticas.
* **Entidad Gubernamental**: recibe y gestiona incidencias asignadas.

---

## ✅ Requerimientos Funcionales Implementados

1. Gestión de usuarios y roles
2. Registro y autenticación de usuarios
3. Gestión de perfiles de usuario
4. Creación de reportes de incidencias
5. Geolocalización mediante GPS y mapas
6. Clasificación de reportes por categoría
7. Adjuntar evidencias (imágenes y videos)
8. Seguimiento del estado del reporte
9. Historial de reportes por usuario
10. Sistema de notificaciones
11. Gestión de comentarios
12. Valoración de reportes (likes/votos)
13. Gestión de noticias comunitarias
14. Filtros y búsqueda avanzada
15. Mapa de incidencias en tiempo real
16. Asignación de reportes a entidades
17. Reportes anónimos
18. Moderación de contenido
19. Dashboard administrativo
20. Exportación de datos
21. Encuestas y retroalimentación
22. Integración con canales externos

---

## 📍 Estados de lo### Requisitos Previos

* Android Studio (versión reciente)

* JDK 11

* Dispositivo Android o emulador

* Cuenta en Supabase

* **Google Cloud Platform (GCP)**

  * Proyecto creado en Google Cloud
  * Google Maps API habilitada
  * API Key configurada
    revios

* Android Studio (versión reciente)

* JDK 11

* Dispositivo Android o emulador

* Cuenta en Supabase

* **Google Cloud Platform (GCP)**

  * Proyecto creado en Goo

### Pasos

1. Clonar el repositorio:

   ```bash
   git clone git@github.com:WilkinsFiguereo/SafeZone.git
   ```

2. Abrir el proyecto en **Android Studio**.

3. Configurar las credenciales de **Supabase**:

   * URL del proyecto
   * API Key

4. Configurar la **Google Maps API Key** en el proyecto.

5. Sincronizar dependencias con Gradle.

6. Ejecutar la aplicación en un emulador o dispositivo físico.

---

## 🗺️ Permisos Utilizados

* Acceso a ubicación (GPS)
* Acceso a internet
* Acceso a almacenamiento

---

## 🎯 Objetivo de SafeZone

El objetivo de **SafeZone** es ofrecer una solución tecnológica confiable para el reporte, seguimiento y gestión de incidencias sociales, fomentando la colaboración ciudadana y facilitando la toma de decisiones por parte de las autoridades y entidades responsables.

---

## 📌 Notas Finales

SafeZone aplica tecnologías modernas de desarrollo Android para resolver problemas reales de la comunidad mediante una plataforma móvil escalable y segura.

---

⭐ Si te gustó el proyecto, no olvides darle una estrella en GitHub.
