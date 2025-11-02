# Demo Users - Credenciales de Demostración

Este documento lista todas las credenciales de demostración creadas automáticamente en el sistema.

## 🔑 Credenciales de Usuario

### PLATFORM_ADMIN (Administrador de Plataforma)
**Para ti - Propietario de la Plataforma SaaS**

- **Username:** `platform@waitlist.com`
- **Password:** `platform123`
- **Rol:** `PLATFORM_ADMIN`
- **Acceso:** Panel completo de administración de plataforma
- **Capacidades:**
  - Crear cuentas de negocios para nuevos clientes
  - Ver todos los negocios y sus datos
  - Gestionar suscripciones (próximamente)
  - Ver reportes de todas las empresas (próximamente)
  - Acceso completo a todas las funcionalidades

---

### BUSINESS_OWNER (Propietario de Negocio)
**Cliente Demo - Propietario del Restaurante Demo**

- **Username:** `demo-owner@restaurant.com`
- **Password:** `owner123`
- **Rol:** `BUSINESS_OWNER`
- **Negocio:** Demo Restaurant
- **Capacidades:**
  - Gestionar su propio negocio
  - Ver todas las reservaciones de su negocio
  - Confirmar/Completar reservaciones
  - Notificar/Sentar clientes en lista de espera
  - Gestionar clientes y personal

**Otro Negocio Demo (Café):**
- **Username:** `demo2-owner@cafe.com`
- **Password:** `owner123`
- **Rol:** `BUSINESS_OWNER`
- **Negocio:** Demo Café

---

### BUSINESS_MANAGER (Gerente)
**Cliente Demo - Gerente del Restaurante Demo**

- **Username:** `demo-manager@restaurant.com`
- **Password:** `manager123`
- **Rol:** `BUSINESS_MANAGER`
- **Negocio:** Demo Restaurant
- **Capacidades:**
  - Ver reservaciones de su negocio
  - Crear y cancelar reservaciones
  - Agregar clientes a lista de espera
  - Gestionar clientes
  - ❌ No puede confirmar/completar reservaciones
  - ❌ No puede notificar/sentar clientes

---

### BUSINESS_STAFF (Personal)
**Cliente Demo - Personal del Restaurante Demo**

- **Username:** `demo-staff@restaurant.com`
- **Password:** `staff123`
- **Rol:** `BUSINESS_STAFF`
- **Negocio:** Demo Restaurant
- **Capacidades:**
  - Ver reservaciones (solo lectura)
  - Crear y cancelar reservaciones
  - Agregar clientes a lista de espera
  - Gestionar información de clientes
  - ❌ No puede confirmar/completar reservaciones
  - ❌ No puede notificar/sentar clientes
  - ❌ Acceso limitado a funciones administrativas

---

## 📋 Vista Rápida

| Rol | Username | Password | Vista Principal |
|-----|----------|----------|-----------------|
| PLATFORM_ADMIN | `platform@waitlist.com` | `platform123` | Panel de Plataforma |
| BUSINESS_OWNER | `demo-owner@restaurant.com` | `owner123` | Dashboard de Negocio |
| BUSINESS_MANAGER | `demo-manager@restaurant.com` | `manager123` | Dashboard de Negocio |
| BUSINESS_STAFF | `demo-staff@restaurant.com` | `staff123` | Dashboard de Negocio |

---

## 🎯 Uso para Demostración

### Para mostrar a nuevos negocios que quieren adquirir una suscripción:

1. **Inicia sesión como PLATFORM_ADMIN**
   - Muestra el panel de administración
   - Crea un nuevo negocio para el cliente potencial
   - Demuestra las capacidades de gestión

2. **Cambia a BUSINESS_OWNER**
   - Muestra cómo se ve desde la perspectiva del cliente
   - Demuestra las funciones operativas
   - Muestra la interfaz de gestión de reservaciones y lista de espera

3. **Cambia a BUSINESS_MANAGER o BUSINESS_STAFF**
   - Demuestra los diferentes niveles de acceso
   - Muestra cómo pueden trabajar varios miembros del equipo
   - Demuestra la separación de responsabilidades

---

## 🔄 Notas Importantes

- Estos usuarios se crean automáticamente cuando el backend inicia por primera vez
- Si los usuarios ya existen, no se recrean
- Las contraseñas están hasheadas con BCrypt
- Todos los usuarios están asociados a negocios de demostración
- El PLATFORM_ADMIN está asociado a un "Platform Business" especial

---

## 🚀 Inicio Rápido

1. Inicia el backend (los usuarios se crearán automáticamente)
2. Inicia el frontend
3. Visita la página de login
4. Usa cualquiera de las credenciales arriba
5. Explora las diferentes vistas según el rol

¡Listo para demostrar! 🎉

