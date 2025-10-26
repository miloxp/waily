# Lista de Espera y Reservaciones

Una aplicación full-stack para restaurantes y negocios de servicios para gestionar listas de espera y reservaciones de mesas. Los clientes reciben actualizaciones por SMS cuando su mesa está lista o pueden consultar los tiempos de espera a través de enlaces públicos.

## Arquitectura

- **Backend**: Java 17 + Spring Boot 3 con Arquitectura Limpia
- **Frontend**: React.js con TypeScript, Vite y Tailwind CSS
- **Base de Datos**: PostgreSQL
- **Mensajería**: Twilio SMS (abstraído detrás de una interfaz)
- **Seguridad**: Spring Security con autenticación JWT

## Estructura del Proyecto

```
waitlist/
├── backend/                 # Aplicación Spring Boot
│   ├── src/main/java/
│   │   └── com/waitlist/
│   │       ├── domain/      # Entidades de dominio y lógica de negocio
│   │       ├── application/ # Casos de uso y servicios
│   │       ├── infrastructure/ # Preocupaciones externas (DB, SMS, etc.)
│   │       └── presentation/ # Controladores y DTOs
│   ├── src/test/java/       # Clases de prueba
│   └── src/main/resources/  # Archivos de configuración
├── frontend/                # Aplicación React
│   ├── src/
│   │   ├── components/      # Componentes React
│   │   ├── pages/          # Componentes de página
│   │   ├── services/       # Servicios API
│   │   └── types/          # Tipos TypeScript
│   └── public/             # Recursos estáticos
├── POSTGRESQL_SETUP.md     # Guía de configuración de PostgreSQL
└── POSTMAN_TEST_CASES.md   # Guía de pruebas de API
```

## Prerrequisitos

- Java 17+
- Node.js 18+
- PostgreSQL 12+

## Inicio Rápido

### 1. Configuración de Base de Datos

Primero, configura PostgreSQL localmente. Consulta [POSTGRESQL_SETUP.md](./POSTGRESQL_SETUP.md) para instrucciones detalladas.

Configuración rápida:
```bash
# Instalar PostgreSQL (macOS con Homebrew)
brew install postgresql@15
brew services start postgresql@15

# Crear base de datos y usuario
psql postgres
CREATE DATABASE waitlist_db;
CREATE USER waitlist_user WITH PASSWORD 'waitlist_password';
GRANT ALL PRIVILEGES ON DATABASE waitlist_db TO waitlist_user;
\q
```

### 2. Configuración del Backend

1. Navegar al directorio backend:
   ```bash
   cd backend
   ```

2. Copiar variables de entorno:
   ```bash
   cp env.example .env
   ```

3. Actualizar `.env` con tu configuración (ver sección Variables de Entorno)

4. Ejecutar la aplicación:
   ```bash
   ./mvnw spring-boot:run
   ```

El backend estará disponible en `http://localhost:8080`

### 3. Configuración del Frontend

1. Navegar al directorio frontend:
   ```bash
   cd frontend
   ```

2. Instalar dependencias:
   ```bash
   npm install
   ```

3. Copiar variables de entorno:
   ```bash
   cp env.example .env
   ```

4. Iniciar servidor de desarrollo:
   ```bash
   npm run dev
   ```

El frontend estará disponible en `http://localhost:5173`

## Endpoints de API

### Autenticación
- `POST /api/auth/register` - Registrar una nueva cuenta de negocio
- `POST /api/auth/login` - Autenticar y devolver JWT
- `GET /api/auth/profile` - Obtener perfil del negocio

### Gestión de Negocios
- `GET /api/business` - Listar negocios
- `GET /api/business/{id}` - Obtener detalles del negocio
- `PUT /api/business/{id}` - Actualizar información del negocio

### Gestión de Clientes
- `POST /api/customers` - Crear un cliente
- `GET /api/customers/{id}` - Obtener información del cliente

### Gestión de Lista de Espera
- `POST /api/waitlist` - Agregar cliente a la lista de espera
- `GET /api/waitlist` - Listar todas las entradas de lista de espera para el negocio autenticado
- `PATCH /api/waitlist/{id}/status` - Actualizar estado (esperando, llamado, sentado, cancelado)
- `DELETE /api/waitlist/{id}` - Remover de la lista de espera

### Gestión de Reservaciones
- `POST /api/reservations` - Crear una reservación
- `GET /api/reservations` - Listar todas las reservaciones
- `GET /api/reservations/{id}` - Obtener detalles de la reservación
- `PATCH /api/reservations/{id}` - Actualizar reservación (estado o hora)
- `DELETE /api/reservations/{id}` - Cancelar reservación

### Notificaciones
- `POST /api/notifications/sms` - Enviar SMS a un cliente usando Twilio
- `GET /api/notifications/status/{id}` - Verificar estado de entrega del SMS

### Endpoints Públicos (Sin Autenticación)
- `GET /public/waitlist/{businessId}` - Endpoint público mostrando tiempo de espera estimado

## Documentación de API

Una vez que el backend esté ejecutándose, visita:
- Swagger UI: http://localhost:8080/swagger-ui.html
- Documentos API: http://localhost:8080/v3/api-docs

## Características

- **Registro de Negocios**: Registrar nuevas cuentas de negocio con autenticación de usuario
- **Gestión de Usuarios**: Vincular usuarios a negocios con acceso basado en roles
- **Gestión de Lista de Espera**: Cola de clientes con seguimiento de posición y notificaciones SMS
- **Sistema de Reservaciones**: Manejar reservaciones de mesas con horarios
- **Notificaciones SMS**: Enviar actualizaciones a clientes vía Twilio (con simulación para desarrollo)
- **Información Pública de Lista de Espera**: Los clientes pueden consultar tiempos de espera vía enlaces SMS
- **Autenticación JWT**: Endpoints de API seguros con acceso limitado por negocio
- **Arquitectura Limpia**: Estructura de código mantenible y testeable
- **Interfaz en Español**: Interfaz completa del frontend en español

## Pruebas

### Pruebas del Backend
```bash
cd backend
./mvnw test
```

El proyecto incluye pruebas unitarias completas, incluyendo una suite de pruebas completa para el caso de uso "Agregar Cliente a Lista de Espera" que demuestra:
- Adición exitosa a la lista de espera
- Validación de negocio
- Validación de cliente
- Prevención de entradas duplicadas
- Cálculo de posición
- Manejo de notificaciones SMS
- Escenarios de error

### Pruebas del Frontend
```bash
cd frontend
npm test
```

### Pruebas de API

Consulta [POSTMAN_TEST_CASES.md](./POSTMAN_TEST_CASES.md) para instrucciones completas de pruebas de API y configuración de colección de Postman.

## Variables de Entorno

### Backend (.env)
```bash
# Configuración de Base de Datos
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/waitlist_db
SPRING_DATASOURCE_USERNAME=waitlist_user
SPRING_DATASOURCE_PASSWORD=waitlist_password

# Configuración JWT
JWT_SECRET=your-super-secret-jwt-key-change-this-in-production
JWT_EXPIRATION=86400000

# Configuración Twilio (opcional - usa simulación si no se proporciona)
TWILIO_ACCOUNT_SID=your-twilio-account-sid
TWILIO_AUTH_TOKEN=your-twilio-auth-token
TWILIO_PHONE_NUMBER=your-twilio-phone-number

# Configuración de Aplicación
SERVER_PORT=8080
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173

# Configuración SMS
SMS_MOCK_ENABLED=true
```

### Frontend (.env)
```bash
# Configuración API
VITE_API_BASE_URL=http://localhost:8080/api

# Configuración de Aplicación
VITE_APP_NAME=Lista de Espera y Reservaciones
VITE_APP_VERSION=1.0.0
```

## Credenciales de Demostración

Después de ejecutar la aplicación, puedes registrar una nueva cuenta de negocio o usar los datos de demostración. El sistema soporta:

- **Registro de Negocios**: Crear nuevas cuentas de negocio a través del endpoint de registro
- **Roles de Usuario**: BUSINESS_OWNER, BUSINESS_MANAGER, BUSINESS_STAFF
- **Acceso Limitado por Negocio**: Los usuarios solo pueden acceder a datos de su negocio asociado

Usuario administrador por defecto (creado automáticamente):
- **Nombre de usuario**: admin@waitlist.com
- **Contraseña**: admin123

## Integración SMS

El sistema incluye un sistema completo de notificaciones SMS:

- **Integración Twilio**: Envío real de SMS vía SDK de Twilio
- **Servicio Simulado**: Implementación simulada amigable para desarrollo
- **Plantillas de Mensajes**: Plantillas predefinidas para notificaciones de lista de espera y reservaciones
- **Seguimiento de Estado**: Monitoreo del estado de entrega de SMS (implementación simulada)

## Despliegue en Producción

Para despliegue en producción:

1. Actualizar variables de entorno con valores de producción
2. Configurar credenciales apropiadas de Twilio
3. Configurar base de datos de producción
4. Actualizar configuraciones CORS para dominios de producción
5. Usar secretos JWT apropiados
6. Configurar certificados SSL/TLS

## Contribuir

Este es un scaffold listo para producción que sigue principios de Arquitectura Limpia e incluye pruebas completas. El código está estructurado para fácil extensión y mantenimiento.