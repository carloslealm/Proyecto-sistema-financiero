# 🏦 LoanManager — Sistema de Gestión de Préstamos

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen?logo=spring)
![Angular](https://img.shields.io/badge/Angular-19-red?logo=angular)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?logo=mysql)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker)
![JWT](https://img.shields.io/badge/Auth-JWT-orange)
![License](https://img.shields.io/badge/License-MIT-yellow)

Sistema web completo para la gestión de préstamos financieros, desarrollado como proyecto de portafolio profesional. Implementa el ciclo de vida completo de un crédito: solicitud, aprobación, desembolso, plan de pagos con amortización francesa y seguimiento de mora.

---

## 📋 Tabla de Contenidos

- [Características](#-características)
- [Tecnologías](#-tecnologías)
- [Arquitectura](#-arquitectura)
- [Instalación rápida con Docker](#-instalación-rápida-con-docker)
- [Instalación manual](#-instalación-manual)
- [Credenciales de prueba](#-credenciales-de-prueba)
- [Estructura del proyecto](#-estructura-del-proyecto)
- [API REST](#-api-rest)
- [Autor](#-autor)

---

## ✨ Características

- **Autenticación JWT** con roles (ADMIN, ASESOR)
- **Gestión de clientes** con búsqueda y paginación
- **Ciclo de vida de préstamos**: solicitud → aprobación → desembolso
- **Calculadora de amortización** Sistema Francés, Alemán y Americano
- **Plan de pagos automático** con desglose de capital e interés
- **Detección de mora** automática con job programado
- **Dashboard financiero** con KPIs en tiempo real
- **API REST documentada** con Swagger/OpenAPI
- **Diseño responsive** compatible con móvil y escritorio
- **Docker Compose** para despliegue con un solo comando

---

## 🛠 Tecnologías

### Backend
| Tecnología | Versión | Uso |
|---|---|---|
| Java | 17 | Lenguaje principal |
| Spring Boot | 3.3.5 | Framework web |
| Spring Security | 6.x | Autenticación y autorización |
| Spring Data JPA | 3.x | Persistencia de datos |
| Hibernate | 6.5 | ORM |
| MySQL | 8.0 | Base de datos |
| JWT (JJWT) | 0.12.3 | Tokens de autenticación |
| Lombok | Latest | Reducción de boilerplate |
| Swagger/OpenAPI | 2.3.0 | Documentación de API |
| Maven | 3.9 | Gestión de dependencias |

### Frontend
| Tecnología | Versión | Uso |
|---|---|---|
| Angular | 19 | Framework SPA |
| TypeScript | 5.x | Lenguaje principal |
| Bootstrap | 5.3 | Estilos y componentes |
| RxJS | 7.x | Programación reactiva |

### DevOps
| Tecnología | Uso |
|---|---|
| Docker | Contenedores |
| Docker Compose | Orquestación |
| Nginx | Servidor web para Angular |

---

## 🏗 Arquitectura

```
┌─────────────────────────────────────────────────┐
│                  Cliente (Browser)               │
│              Angular SPA — puerto 80             │
└─────────────────────┬───────────────────────────┘
                      │ HTTP/REST
┌─────────────────────▼───────────────────────────┐
│            Spring Boot API REST                  │
│                  puerto 8080                     │
│                                                  │
│  Controller → Service → Repository → Entity      │
│                    ↕ JWT Auth                    │
└─────────────────────┬───────────────────────────┘
                      │ JPA/Hibernate
┌─────────────────────▼───────────────────────────┐
│              MySQL 8.0 — puerto 3306             │
└─────────────────────────────────────────────────┘
```

### Arquitectura por capas (Backend)
```
src/main/java/com/loanmanager/backend/
├── config/        # Seguridad, CORS, Swagger
├── controller/    # Endpoints REST
├── dto/           # Objetos de transferencia
├── entity/        # Entidades JPA
├── exception/     # Manejo global de errores
├── repository/    # Acceso a datos
├── security/      # JWT Filter, UserDetails
├── service/       # Lógica de negocio
└── util/          # Calculadora de amortización, JWT
```

---

## 🚀 Instalación rápida con Docker

### Prerrequisitos
- Docker Desktop instalado
- Git

### Pasos

```bash
# 1. Clonar el repositorio
git clone https://github.com/tu-usuario/loanmanager.git
cd loanmanager

# 2. Levantar todos los servicios
docker compose up -d

# 3. Verificar que los contenedores estén corriendo
docker ps

# 4. Ver logs del backend
docker logs loanmanager-backend -f
```

### Acceder a la aplicación

| Servicio | URL |
|---|---|
| Frontend Angular | http://localhost |
| Backend API | http://localhost:8080/api/v1 |
| Swagger UI | http://localhost:8080/api/v1/swagger-ui |
| MySQL | localhost:3307 |

### Detener los servicios

```bash
docker compose down          # Detener sin borrar datos
docker compose down -v       # Detener y borrar datos
```

---

## 💻 Instalación manual

### Prerrequisitos
- Java 17+
- Maven 3.9+
- Node.js 20+
- Angular CLI 19+
- MySQL 8.0+

### Base de datos

```bash
mysql -u root -p < database/loanmanager_database_v2.sql
```

### Backend

```bash
cd backend/backend/backend
mvn clean spring-boot:run
```

### Frontend

```bash
cd frontend/frontend
npm install
ng serve
```

---

## 🔑 Credenciales de prueba

| Usuario | Email | Contraseña | Rol |
|---|---|---|---|
| Carlos Mendoza | admin@loanmanager.com | Admin1234! | ADMIN + ASESOR |
| Daniela Ríos | daniela.rios@loanmanager.com | Admin1234! | ASESOR |
| Andrés Castillo | andres.castillo@loanmanager.com | Admin1234! | ASESOR |

---

## 📁 Estructura del proyecto

```
loanmanager/
├── backend/
│   └── backend/backend/
│       ├── src/
│       │   ├── main/java/com/loanmanager/backend/
│       │   └── main/resources/
│       ├── Dockerfile
│       └── pom.xml
├── frontend/
│   └── frontend/
│       ├── src/app/
│       │   ├── guards/
│       │   ├── interceptors/
│       │   ├── models/
│       │   ├── modules/
│       │   │   ├── auth/
│       │   │   ├── clientes/
│       │   │   ├── dashboard/
│       │   │   └── prestamos/
│       │   └── services/
│       ├── Dockerfile
│       └── nginx.conf
├── database/
│   └── loanmanager_database_v2.sql
├── docker-compose.yml
├── .gitignore
└── README.md
```

---

## 📡 API REST

La documentación interactiva está disponible en Swagger UI:
```
http://localhost:8080/api/v1/swagger-ui
```

### Endpoints principales

| Método | Endpoint | Descripción | Auth |
|---|---|---|---|
| POST | /auth/login | Iniciar sesión | No |
| POST | /auth/registro | Registrar usuario | No |
| GET | /clientes | Listar clientes | Sí |
| POST | /clientes | Crear cliente | Sí |
| PUT | /clientes/{id} | Actualizar cliente | Sí |
| GET | /prestamos | Listar préstamos | Sí |
| POST | /prestamos | Solicitar préstamo | Sí |
| PATCH | /prestamos/{id}/aprobar | Aprobar préstamo | Sí |
| PATCH | /prestamos/{id}/desembolsar | Desembolsar | Sí (ADMIN) |
| GET | /prestamos/{id}/plan-pagos | Plan de pagos | Sí |
| GET | /dashboard | KPIs del sistema | Sí |

### Autenticación

```bash
# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@loanmanager.com","password":"Admin1234!"}'

# Usar el token en requests protegidos
curl -X GET http://localhost:8080/api/v1/clientes \
  -H "Authorization: Bearer {token}"
```

---

## 👤 Autor

**[Tu Nombre]**
- GitHub: [@tu-usuario](https://github.com/tu-usuario)
- LinkedIn: [tu-perfil](https://linkedin.com/in/tu-perfil)
- Email: tu@email.com

Desarrollado como proyecto de portafolio para demostrar competencias en
desarrollo Full Stack con Java Spring Boot y Angular.

---

## 📄 Licencia

Este proyecto está bajo la Licencia MIT.
Ver el archivo [LICENSE](LICENSE) para más detalles.
