# AM_MueblAR

AM_MueblAR es una aplicación móvil innovadora para Android, desarrollada en Kotlin, que permite a los usuarios visualizar muebles en realidad aumentada (RA) dentro de su entorno real. La plataforma incorpora un sistema de autenticación robusto que gestiona tres roles de usuario con funcionalidades específicas.

[<image-card alt="Repositorio GitHub" src="https://img.shields.io/badge/GitHub-Repositorio-blue?logo=github" ></image-card>](https://github.com/AxelCruzado/AM_MueblAR)

## Descripción del Proyecto

AM_MueblAR utiliza realidad aumentada para permitir a los usuarios previsualizar muebles en sus espacios físicos. La aplicación cuenta con un sistema de autenticación seguro y control de acceso basado en roles, garantizando una experiencia fluida para administradores, empresas y clientes.

## Tecnologías Principales

- **Lenguaje de Programación**: Kotlin
- **Autenticación**: Firebase Authentication
- **Base de Datos**: Firebase Firestore
- **Realidad Aumentada**: ARCore
- **Geolocalización**: Google Maps SDK
- **Arquitectura**: Modelo-Vista-Modelo de Vista (MVVM)

## Funcionalidades Principales

### Sistema de Autenticación

- Registro diferenciado para empresas y clientes
- Validación de cuentas de empresa por administradores
- Recuperación de contraseña mediante correo electrónico
- Protección de rutas basada en roles

### Roles de Usuario

| Rol | Descripción | Funcionalidades |
|-----|-------------|-----------------|
| **Administrador** <br> <image-card alt="Admin" src="https://img.shields.io/badge/Rol-Administrador-red" ></image-card> | Gestiona la plataforma globalmente | - Aprobar/rechazar empresas <br> - Visualizar todas las empresas registradas <br> - Moderar contenido |
| **Empresa** <br> <image-card alt="Empresa" src="https://img.shields.io/badge/Rol-Empresa-orange" ></image-card> | Proveedores de muebles | - Gestionar catálogo de productos <br> - Configurar perfil <br> - Visualizar estadísticas |
| **Cliente** <br> <image-card alt="Cliente" src="https://img.shields.io/badge/Rol-Cliente-green" ></image-card> | Usuarios finales | - Explorar catálogo de productos <br> - Visualización en realidad aumentada <br> - Ubicar empresas |

## Configuración del Entorno

### Requisitos Previos

- Android Studio (versión estable más reciente)
- SDK de Android configurado
- Dispositivo compatible con ARCore o emulador
- Cuenta de Firebase configurada

### Archivo `.env`

El proyecto requiere un archivo `.env` con las siguientes variables de configuración:

```plaintext
# Configuración de Firebase
FIREBASE_API_KEY=your_api_key
FIREBASE_AUTH_DOMAIN=your_project.firebaseapp.com
FIREBASE_DATABASE_URL=https://your_project.firebaseio.com
FIREBASE_PROJECT_ID=your_project_id
FIREBASE_STORAGE_BUCKET=your_bucket.appspot.com
FIREBASE_MESSAGING_SENDER_ID=your_sender_id
FIREBASE_APP_ID=your_app_id

# API de Google Maps
MAPS_API_KEY=your_maps_key

# Configuración de ARCore
ARCORE_API_KEY=your_arcore_key
```

## Usuarios de Prueba

Para facilitar las pruebas, se han creado las siguientes cuentas:

| Rol | Email | Contraseña |
|-----|-------|------------|
| **Administrador** <br> ![Admin](https://img.shields.io/badge/Rol-Administrador-red) | admin@mueblar.com | admin2025 |
| **Cliente** <br> ![Cliente](https://img.shields.io/badge/Rol-Cliente-green) | aacruzadoa@gmail.com | Axel2025 |
| **Empresa** <br> ![Empresa](https://img.shields.io/badge/Rol-Empresa-orange) | aacruzadoa2@gmail.com | Axel2025 |

## Instalación y Ejecución

### Clonar el Repositorio

```bash
git clone https://github.com/AxelCruzado/AM_MueblAR.git
cd AM_MueblAR
```
### Configurar el Proyecto

1. Abrir el proyecto en Android Studio.
2. Crear el archivo `local.properties` si no existe.
3. Agregar las credenciales necesarias en el archivo `.env`.
4. Sincronizar el proyecto con los archivos Gradle.

### Ejecutar la Aplicación

1. Conectar un dispositivo físico compatible o configurar un emulador.
2. Seleccionar el dispositivo de destino.
3. Ejecutar la aplicación (Shift + F10).

## Estructura del Proyecto

```plaintext
AM_MueblAR/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/am_mueblar/
│   │   │   │   ├── data/          # Modelos de datos
│   │   │   │   ├── ui/            # Logica de las Vistas de Interface
│   │   │   │   ├MainActivity.kt   # Actividad Principal
│   │   │   ├── res/               # Recursos Android
│   │   │   └── AndroidManifest.xml
│   ├── build.gradle               # Configuración del módulo
├── .env                           # Variables de entorno
├── build.gradle                   # Configuración del proyecto
└── settings.gradle                # Configuración de módulos
```
