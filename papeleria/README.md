# Papelería Fundadores — Backend API REST

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.2-brightgreen?logo=springboot)
![Gradle](https://img.shields.io/badge/Build-Gradle-02303A?logo=gradle)
![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL-336791?logo=postgresql)
![JWT](https://img.shields.io/badge/Auth-JWT-black?logo=jsonwebtokens)
![License](https://img.shields.io/badge/License-<!-- LICENSE_PLACEHOLDER -->-blue)

API REST para el sistema integral de gestión y comercio electrónico de **Papelería Fundadores**. Centraliza todas las operaciones del negocio: control de inventario en tiempo real, registro de ventas y compras a proveedores, gestión de clientes y pedidos online, y autenticación segura basada en JWT con control de acceso por roles (RBAC).

---

## Tabla de contenidos

- [Características principales](#características-principales)
- [Stack tecnológico](#stack-tecnológico)
- [Requisitos previos](#requisitos-previos)
- [Instalación y ejecución local](#instalación-y-ejecución-local)
- [Configuración](#configuración)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Endpoints de la API](#endpoints-de-la-api)
- [Base de datos](#base-de-datos)
- [Testing](#testing)
- [Docker](#docker)
- [Contribución](#contribución)
- [Licencia](#licencia)
- [Autor](#autor)

---

## Características principales

- **Autenticación JWT stateless** — login, registro, recuperación de contraseña por email con token de un solo uso (15 min de validez).
- **Control de acceso por roles (RBAC)** — tres roles: `ADMIN`, `EMPLEADO`, `CLIENTE`. Cada endpoint está protegido con `@PreAuthorize` según el rol requerido.
- **Gestión de inventario en tiempo real** — el stock se descuenta automáticamente al confirmar una venta o pedido, y se incrementa al registrar una compra a proveedor.
- **E-commerce** — carrito de compras por cliente, creación de pedidos desde el carrito con cálculo automático de IVA (19%), seguimiento de estados del pedido.
- **Ventas presenciales** — registro de ventas con descuento configurable, cálculo de subtotal, impuesto y total.
- **Compras a proveedores** — registro de compras con actualización automática de stock y precio de costo histórico.
- **Gestión de clientes y proveedores** — CRUD completo con validación de documento y correo únicos.
- **Catálogo de productos** — búsqueda por nombre, código de barras y categoría; alerta de stock mínimo.
- **Manejo de errores uniforme** — todos los errores devuelven JSON con el mismo formato (`timestamp`, `message`, `details`, `errorCode`), incluyendo 401 y 403.
- **Swagger UI** — documentación interactiva disponible en perfil de desarrollo.
- **Variables de entorno con `.env`** — ningún secreto hardcodeado en el código fuente.

---

## Stack tecnológico

| Categoría | Tecnología | Versión |
|---|---|---|
| Lenguaje | Java | 21 (LTS) |
| Framework | Spring Boot | 3.4.2 |
| Seguridad | Spring Security + JWT (jjwt) | 3.4.2 / 0.12.5 |
| Persistencia | Spring Data JPA + Hibernate | 3.4.2 |
| Base de datos | PostgreSQL | — |
| Mapeo de DTOs | MapStruct | 1.5.5.Final |
| Reducción de boilerplate | Lombok | 1.18.30 |
| Validación | Jakarta Validation | — |
| Documentación API | SpringDoc OpenAPI (Swagger UI) | 2.8.4 |
| Email | Spring Mail (SMTP Gmail) | 3.4.2 |
| Variables de entorno | spring-dotenv | 4.0.0 |
| Build tool | Gradle | 9.x |
| Testing | JUnit 5 + Spring Security Test | — |

---

## Requisitos previos

- **JDK 21** — [Descargar](https://adoptium.net/)
- **PostgreSQL** — versión 13 o superior, con una base de datos creada llamada `papeleria_db`
- **Gradle** — el proyecto incluye Gradle Wrapper (`./gradlew`), no necesitas instalarlo globalmente
- **Cuenta Gmail** con una [Contraseña de Aplicación](https://myaccount.google.com/apppasswords) generada (para el envío de emails de recuperación de contraseña)

> No se requiere Docker. No hay `Dockerfile` ni `docker-compose.yml` en este proyecto.

---

## Instalación y ejecución local

### 1. Clonar el repositorio

```bash
git clone https://github.com/jorgeMontoya022/papeleria-backend.git
cd papeleria-backend/papeleria
```

### 2. Configurar las variables de entorno

Copia la plantilla y rellena tus valores:

```powershell
# Windows (PowerShell)
Copy-Item .env.example .env
```

```bash
# Linux / macOS
cp .env.example .env
```

Abre `.env` y completa cada variable (ver sección [Configuración](#configuración)).

### 3. Crear la base de datos en PostgreSQL

```sql
CREATE DATABASE papeleria_db;
```

> Hibernate creará las tablas automáticamente en el primer arranque (`ddl-auto=update`).

### 4. Compilar el proyecto

```bash
./gradlew build
```

En Windows:

```powershell
.\gradlew build
```

### 5. Ejecutar la aplicación

**Perfil por defecto** (Swagger deshabilitado, recomendado para producción):

```bash
./gradlew bootRun
```

**Perfil de desarrollo** (Swagger habilitado en `http://localhost:8080/swagger-ui/index.html`):

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

En Windows:

```powershell
.\gradlew bootRun --args='--spring.profiles.active=dev'
```

La API estará disponible en: `http://localhost:8080`

### 6. Ejecutar el JAR directamente

```bash
./gradlew bootJar
java -jar build/libs/papeleria-0.0.1-SNAPSHOT.jar
```

Con perfil dev:

```bash
java -jar build/libs/papeleria-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

---

## Configuración

Todas las propiedades sensibles se leen del archivo `.env` a través de `spring-dotenv`. **Nunca se suben a git** (el archivo `.env` está en `.gitignore`).

### Variables de entorno requeridas

Copia `.env.example` como `.env` y rellena:

```env
# --- BASE DE DATOS POSTGRESQL ---
DB_USERNAME=tu_usuario_postgres
DB_PASSWORD=tu_contraseña_postgres

# --- CORREO SMTP (Gmail) ---
# Usa una Contraseña de Aplicación, no tu contraseña de Google.
# Genera una en: https://myaccount.google.com/apppasswords
MAIL_USERNAME=tu_correo@gmail.com
MAIL_PASSWORD=tu_contraseña_de_aplicacion

# --- JWT ---
# Genera un secreto seguro con:  openssl rand -hex 32
# O en PowerShell:
#   $bytes = New-Object byte[] 32
#   $rng = [System.Security.Cryptography.RNGCryptoServiceProvider]::new()
#   $rng.GetBytes($bytes)
#   ($bytes | ForEach-Object { $_.ToString("x2") }) -join ""
JWT_SECRET=tu_secreto_hexadecimal_de_64_caracteres
JWT_EXPIRATION_MS=86400000

# --- URLS ---
RESET_PASSWORD_URL=http://localhost:8080/api/v1/auth/reset-password
```

### Propiedades de `application.properties`

| Propiedad | Variable de entorno | Valor por defecto | Descripción |
|---|---|---|---|
| `spring.datasource.url` | — | `jdbc:postgresql://localhost:5432/papeleria_db` | URL de conexión |
| `spring.datasource.username` | `DB_USERNAME` | `postgres` | Usuario de BD |
| `spring.datasource.password` | `DB_PASSWORD` | — | Contraseña de BD |
| `spring.mail.username` | `MAIL_USERNAME` | — | Cuenta Gmail |
| `spring.mail.password` | `MAIL_PASSWORD` | — | App password de Gmail |
| `jwt.secret` | `JWT_SECRET` | — | Clave HMAC-SHA256 (min. 32 bytes) |
| `jwt.expiration-ms` | `JWT_EXPIRATION_MS` | `86400000` (24 h) | Validez del token |
| `app.reset-password-url` | `RESET_PASSWORD_URL` | `http://localhost:8080/...` | URL en emails de recuperación |
| `spring.jpa.hibernate.ddl-auto` | — | `update` | Estrategia de esquema |

### Perfiles

| Perfil | Swagger | Log level | Activación |
|---|---|---|---|
| `default` | ❌ Deshabilitado | INFO | Sin `--spring.profiles.active` |
| `dev` | ✅ Habilitado | DEBUG | `--spring.profiles.active=dev` |

---

## Estructura del proyecto

```
papeleria/
└── src/
    ├── main/
    │   ├── java/com/papeleriafundadores/papeleria/
    │   │   ├── PapeleriaApplication.java       # Clase principal Spring Boot
    │   │   │
    │   │   ├── config/                         # Configuración de Spring Security
    │   │   │   └── SecurityConfig.java         # Filtros, RBAC, sesión stateless
    │   │   │
    │   │   ├── controller/                     # Capa de presentación (REST)
    │   │   │   ├── AuthController.java         # /api/v1/auth/**
    │   │   │   ├── UserController.java         # /api/v1/users/**
    │   │   │   ├── RoleController.java         # /api/v1/roles/**
    │   │   │   ├── ProductController.java      # /api/v1/products/**
    │   │   │   ├── CategoryController.java     # /api/v1/categories/**
    │   │   │   ├── CustomerController.java     # /api/v1/customers/**
    │   │   │   ├── SupplierController.java     # /api/v1/suppliers/**
    │   │   │   ├── PurchaseController.java     # /api/v1/purchases/**
    │   │   │   ├── SaleController.java         # /api/v1/sales/**
    │   │   │   ├── OrderController.java        # /api/v1/orders/**
    │   │   │   └── CartController.java         # /api/v1/carts/**
    │   │   │
    │   │   ├── dto/
    │   │   │   ├── request/                    # Objetos de entrada (validados con @Valid)
    │   │   │   └── response/                   # Objetos de salida (sin datos sensibles)
    │   │   │
    │   │   ├── entity/                         # Entidades JPA (tablas de la BD)
    │   │   │   └── enums/                      # Enumeraciones (UserStatus, OrderStatus…)
    │   │   │
    │   │   ├── exception/                      # Manejo centralizado de errores
    │   │   │   ├── GlobalExceptionHandler.java # @RestControllerAdvice
    │   │   │   ├── ErrorDetails.java           # Formato uniforme de error
    │   │   │   └── *Exception.java             # Excepciones de dominio
    │   │   │
    │   │   ├── mapper/                         # MapStruct: Entity ↔ DTO
    │   │   │
    │   │   ├── repository/                     # Spring Data JPA (consultas JPQL)
    │   │   │
    │   │   ├── security/                       # Capa de seguridad JWT
    │   │   │   ├── JwtService.java             # Generación y validación de tokens
    │   │   │   ├── JwtAuthFilter.java          # OncePerRequestFilter
    │   │   │   ├── CustomUserDetailsService.java # Carga usuario por email
    │   │   │   ├── SecurityEntryPoints.java    # JSON para 401 y 403
    │   │   │   └── SecurityUtils.java          # Helpers para @PreAuthorize (SpEL)
    │   │   │
    │   │   ├── service/                        # Interfaces del dominio
    │   │   │   └── impl/                       # Implementaciones de negocio
    │   │   │
    │   │   └── util/                           # Utilidades (EmailTemplateEngine)
    │   │
    │   └── resources/
    │       ├── application.properties          # Configuración base (usa ${VAR})
    │       └── application-dev.properties      # Overrides para perfil dev
    │
    └── test/
        └── java/.../security/                  # Tests de seguridad con @WebMvcTest
            ├── UserControllerSecurityTest.java
            ├── RoleControllerSecurityTest.java
            └── ProductControllerSecurityTest.java
```

---

## Endpoints de la API

> Todos los endpoints (excepto `/api/v1/auth/**`) requieren el header:
> `Authorization: Bearer <token>`
>
> Con el perfil `dev`, la documentación interactiva completa está en:
> **`http://localhost:8080/swagger-ui/index.html`**

### Autenticación — `/api/v1/auth` (público)

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/v1/auth/register` | Registrar un nuevo usuario |
| `POST` | `/api/v1/auth/login` | Iniciar sesión — devuelve JWT |
| `POST` | `/api/v1/auth/forgot-password` | Solicitar email de recuperación |
| `POST` | `/api/v1/auth/reset-password` | Restablecer contraseña con token |

### Usuarios — `/api/v1/users`

| Método | Ruta | Rol requerido | Descripción |
|---|---|---|---|
| `GET` | `/api/v1/users/me` | Cualquier autenticado | Ver mi perfil (sin IDOR) |
| `PUT` | `/api/v1/users/me` | Cualquier autenticado | Editar mi perfil |
| `GET` | `/api/v1/users` | `ADMIN` | Listar todos los usuarios |
| `GET` | `/api/v1/users/{id}` | `ADMIN` | Ver usuario por ID |
| `GET` | `/api/v1/users/email?email=` | `ADMIN` | Buscar usuario por email |
| `GET` | `/api/v1/users/state/{state}` | `ADMIN` | Filtrar por estado |
| `PUT` | `/api/v1/users/{id}` | `ADMIN` | Editar usuario por ID |
| `PATCH` | `/api/v1/users/{id}/status` | `ADMIN` | Cambiar estado de cuenta |

### Roles — `/api/v1/roles` (solo `ADMIN`)

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/v1/roles` | Crear rol (`?name=&description=`) |
| `GET` | `/api/v1/roles` | Listar todos los roles |
| `GET` | `/api/v1/roles/{id}` | Buscar rol por ID |
| `GET` | `/api/v1/roles/name/{name}` | Buscar rol por nombre |

### Productos — `/api/v1/products`

| Método | Ruta | Rol requerido | Descripción |
|---|---|---|---|
| `GET` | `/api/v1/products` | Cualquier autenticado | Listar todos |
| `GET` | `/api/v1/products/{id}` | Cualquier autenticado | Buscar por ID |
| `GET` | `/api/v1/products/barcode/{barcode}` | Cualquier autenticado | Buscar por código de barras |
| `GET` | `/api/v1/products/search?name=` | Cualquier autenticado | Buscar por nombre |
| `GET` | `/api/v1/products/category/{id}` | Cualquier autenticado | Filtrar por categoría |
| `GET` | `/api/v1/products/low-stock` | Cualquier autenticado | Productos bajo stock mínimo |
| `POST` | `/api/v1/products` | `ADMIN` | Crear producto |
| `PUT` | `/api/v1/products/{id}` | `ADMIN` | Editar producto |
| `PATCH` | `/api/v1/products/{id}/toggle` | `ADMIN` | Activar/desactivar |

### Categorías — `/api/v1/categories`

| Método | Ruta | Rol requerido | Descripción |
|---|---|---|---|
| `GET` | `/api/v1/categories` | Cualquier autenticado | Listar todas |
| `GET` | `/api/v1/categories/{id}` | Cualquier autenticado | Buscar por ID |
| `GET` | `/api/v1/categories/active` | Cualquier autenticado | Listar activas |
| `POST` | `/api/v1/categories` | `ADMIN` | Crear categoría |
| `PUT` | `/api/v1/categories/{id}` | `ADMIN` | Editar categoría |
| `PATCH` | `/api/v1/categories/{id}/toggle` | `ADMIN` | Activar/desactivar |

### Clientes — `/api/v1/customers`

| Método | Ruta | Rol requerido | Descripción |
|---|---|---|---|
| `GET` | `/api/v1/customers` | `ADMIN`, `EMPLEADO` | Listar clientes |
| `GET` | `/api/v1/customers/{id}` | `ADMIN`, `EMPLEADO` | Buscar por ID |
| `GET` | `/api/v1/customers/document/{doc}` | `ADMIN`, `EMPLEADO` | Buscar por documento |
| `POST` | `/api/v1/customers` | `ADMIN`, `EMPLEADO` | Registrar cliente |
| `PUT` | `/api/v1/customers/{id}` | `ADMIN`, `EMPLEADO` | Editar cliente |
| `PATCH` | `/api/v1/customers/{id}/status` | `ADMIN` | Cambiar estado |

### Proveedores — `/api/v1/suppliers`

| Método | Ruta | Rol requerido | Descripción |
|---|---|---|---|
| `GET` | `/api/v1/suppliers` | `ADMIN`, `EMPLEADO` | Listar proveedores |
| `GET` | `/api/v1/suppliers/{id}` | `ADMIN`, `EMPLEADO` | Buscar por ID |
| `GET` | `/api/v1/suppliers/nit/{nit}` | `ADMIN`, `EMPLEADO` | Buscar por NIT |
| `POST` | `/api/v1/suppliers` | `ADMIN` | Crear proveedor |
| `PUT` | `/api/v1/suppliers/{id}` | `ADMIN` | Editar proveedor |
| `PATCH` | `/api/v1/suppliers/{id}/toggle` | `ADMIN` | Activar/desactivar |

### Compras — `/api/v1/purchases`

| Método | Ruta | Rol requerido | Descripción |
|---|---|---|---|
| `POST` | `/api/v1/purchases` | `ADMIN` | Registrar compra a proveedor |
| `GET` | `/api/v1/purchases` | `ADMIN`, `EMPLEADO` | Listar compras |
| `GET` | `/api/v1/purchases/{id}` | `ADMIN`, `EMPLEADO` | Detalle de compra |
| `GET` | `/api/v1/purchases/supplier/{id}` | `ADMIN`, `EMPLEADO` | Compras por proveedor |

### Ventas — `/api/v1/sales`

| Método | Ruta | Rol requerido | Descripción |
|---|---|---|---|
| `POST` | `/api/v1/sales` | `ADMIN`, `EMPLEADO` | Registrar venta presencial |
| `GET` | `/api/v1/sales` | `ADMIN`, `EMPLEADO` | Listar ventas |
| `GET` | `/api/v1/sales/{id}` | `ADMIN`, `EMPLEADO` | Detalle de venta |
| `GET` | `/api/v1/sales/user/{userId}` | `ADMIN`, `EMPLEADO` | Ventas por empleado |

### Pedidos — `/api/v1/orders`

| Método | Ruta | Rol requerido | Descripción |
|---|---|---|---|
| `POST` | `/api/v1/orders` | Todos | Crear pedido desde carrito |
| `DELETE` | `/api/v1/orders/{id}/cancel` | Todos | Cancelar pedido pendiente |
| `GET` | `/api/v1/orders/{id}` | Todos | Detalle de pedido |
| `GET` | `/api/v1/orders/number/{num}` | Todos | Buscar por número de pedido |
| `GET` | `/api/v1/orders/customer/{id}` | Todos | Pedidos de un cliente |
| `GET` | `/api/v1/orders` | `ADMIN`, `EMPLEADO` | Listar todos los pedidos |
| `GET` | `/api/v1/orders/status/{status}` | `ADMIN`, `EMPLEADO` | Filtrar por estado |
| `PATCH` | `/api/v1/orders/{id}/status` | `ADMIN`, `EMPLEADO` | Actualizar estado del pedido |

**Estados de pedido:** `PENDING` → `PAID` → `PREPARING` → `SHIPPED` → `DELIVERED` / `CANCELED`

### Carrito — `/api/v1/carts`

| Método | Ruta | Rol requerido | Descripción |
|---|---|---|---|
| `GET` | `/api/v1/carts/customer/{id}` | Todos | Ver carrito del cliente |
| `POST` | `/api/v1/carts/customer/{id}/items` | Todos | Agregar producto al carrito |
| `PUT` | `/api/v1/carts/customer/{id}/items/{pid}` | Todos | Actualizar cantidad |
| `DELETE` | `/api/v1/carts/customer/{id}/items/{pid}` | Todos | Eliminar ítem |
| `DELETE` | `/api/v1/carts/customer/{id}/clear` | Todos | Vaciar carrito |

### Formato de errores

Todos los errores devuelven el mismo JSON:

```json
{
  "timestamp": "2026-08-25T10:30:00",
  "message": "Descripción del error",
  "details": "uri=/api/v1/...",
  "errorCode": "RESOURCE_NOT_FOUND"
}
```

| Código HTTP | `errorCode` | Causa |
|---|---|---|
| `400` | `BAD_REQUEST_ERROR` | Datos inválidos o credenciales incorrectas |
| `400` | `INSUFFICIENT_STOCK` | Stock insuficiente para la operación |
| `400` | `VALIDATION_FAILED` | Falló la validación de campos (`@Valid`) |
| `401` | `AUTHENTICATION_REQUIRED` | Sin token o token inválido/expirado |
| `403` | `ACCESS_DENIED` | Rol insuficiente para el endpoint |
| `403` | `USER_ACCOUNT_DISABLED` | Cuenta inactiva, bloqueada o suspendida |
| `404` | `RESOURCE_NOT_FOUND` | Recurso no encontrado |
| `409` | `RESOURCE_ALREADY_EXISTS` | Duplicado (email, documento, NIT, barcode) |
| `500` | `INTERNAL_SERVER_ERROR` | Error no controlado |

---

## Base de datos

La base de datos es **PostgreSQL**. El esquema se gestiona automáticamente con Hibernate (`ddl-auto=update`). No se usa Flyway ni Liquibase.

### Entidades principales

| Entidad | Tabla | Descripción |
|---|---|---|
| `User` | `usuarios` | Usuarios del sistema (ADMIN, EMPLEADO, CLIENTE) |
| `Role` | `roles` | Roles de seguridad |
| `Product` | `productos` | Catálogo con stock y precios |
| `Category` | `categorias` | Categorías de productos |
| `Supplier` | `proveedores` | Proveedores de mercancía |
| `Customer` | `clientes` | Compradores del e-commerce |
| `Cart` | `carritos` | Carrito de compras (1:1 con Cliente) |
| `CartItem` | `items_carrito` | Ítems dentro del carrito |
| `Order` | `pedidos` | Pedidos generados desde el carrito |
| `OrderDetail` | `detalles_pedido` | Líneas del pedido |
| `Purchase` | `compras` | Compras a proveedores |
| `PurchaseDetails` | `detalles_compra` | Líneas de la compra |
| `Sale` | `ventas` | Ventas presenciales |
| `SaleDetail` | `detalles_venta` | Líneas de la venta |

### Roles del sistema

Los roles deben crearse una vez mediante `POST /api/v1/roles` antes de registrar usuarios:

```json
{ "name": "ADMIN",    "description": "Acceso total al sistema" }
{ "name": "EMPLEADO", "description": "Acceso operativo (ventas, pedidos, clientes)" }
{ "name": "CLIENTE",  "description": "Acceso al e-commerce y perfil propio" }
```

> **Importante:** los nombres de rol son case-sensitive y deben coincidir exactamente con los valores anteriores para que los `@PreAuthorize` funcionen correctamente.

---

## Testing

### Ejecutar todos los tests

```bash
./gradlew test
```

En Windows:

```powershell
.\gradlew test
```

### Tipo de tests presentes

Los tests son de **seguridad de la capa web** usando `@WebMvcTest` + `@WithMockUser` de Spring Security Test. No necesitan base de datos.

| Archivo | Qué verifica |
|---|---|
| `UserControllerSecurityTest` | IDOR bloqueado en `/users/{id}`, endpoints `/me` accesibles a cualquier rol, `ADMIN` único con acceso a gestión, 401 sin token |
| `RoleControllerSecurityTest` | Solo `ADMIN` puede listar y crear roles; `EMPLEADO` y `CLIENTE` reciben 403 |
| `ProductControllerSecurityTest` | Lectura libre para todos los roles autenticados; escritura restringida a `ADMIN` |

### Reporte de resultados

Los resultados en HTML se generan en:

```
build/reports/tests/test/index.html
```

---

## Docker

> Este proyecto no incluye `Dockerfile` ni `docker-compose.yml`. Para ejecutarlo necesitas instalar Java 21 y PostgreSQL directamente en tu máquina o servidor.

Si deseas contenedorizarlo en el futuro, el punto de entrada sería:

```dockerfile
# Ejemplo básico (no incluido en el repositorio)
FROM eclipse-temurin:21-jre
COPY build/libs/papeleria-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

---

## Contribución

Este es un proyecto personal de práctica. Si deseas sugerir mejoras:

1. Haz un fork del repositorio.
2. Crea una rama: `git checkout -b feature/mi-mejora`
3. Realiza tus cambios y haz commit: `git commit -m 'feat: descripción breve'`
4. Abre un Pull Request describiendo los cambios.

---

## Licencia

<!-- LICENSE_PLACEHOLDER: Agrega aquí tu licencia. Ejemplo: MIT, Apache 2.0, etc. -->

Este proyecto no incluye un archivo de licencia explícito en el repositorio.

---

## Autor

**Jorge William Montoya Toro**
- GitHub: [@jorgeMontoya022](https://github.com/jorgeMontoya022)
- Repositorio: [papeleria-backend](https://github.com/jorgeMontoya022/papeleria-backend)

<!-- CONTACT_PLACEHOLDER: Agrega correo u otros medios de contacto si lo deseas -->
