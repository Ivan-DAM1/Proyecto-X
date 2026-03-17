# Spring Security (configuración básica)

## Objetivo
Se ha añadido Spring Security con **HTTP Basic** para proteger las peticiones **POST**, **PUT** y **DELETE**.

- `GET` (y `OPTIONS` de CORS) queda público.
- `POST`, `PUT` y `DELETE` requieren usuario/contraseña.

Esto es una configuración mínima pensada para desarrollo.

## Cambios realizados

- Dependencia añadida: `spring-boot-starter-security`.
- Configuración en `SecurityConfig`:
  - CORS habilitado (manteniendo la configuración existente).
  - CSRF desactivado (API stateless).
  - Sesiones en modo `STATELESS`.
  - Reglas:
    - `OPTIONS /**` → permitido
    - `GET /**` → permitido
    - `POST /**` → autenticado
    - `PUT /**` → autenticado
    - `DELETE /**` → autenticado

- Usuario en memoria (`InMemoryUserDetailsManager`) configurado por propiedades.

## Credenciales
En [src/main/resources/application.properties](src/main/resources/application.properties) se definen:

- `app.security.basic.username` (por defecto `admin`)
- `app.security.basic.password` (por defecto `admin`)

> Nota: la contraseña se cifra en memoria con BCrypt al arrancar.

## Ejemplos

### GET (sin auth)
```bash
curl -i http://localhost:8081/api/productos
```

### POST (con auth)
```bash
curl -i -u admin:admin \
  -H "Content-Type: application/json" \
  -X POST http://localhost:8081/api/productos \
  -d '{"nombre":"Producto","precio":10.0,"stock":5}'
```

### PUT (con auth)
```bash
curl -i -u admin:admin \
  -H "Content-Type: application/json" \
  -X PUT http://localhost:8081/api/productos/1 \
  -d '{"nombre":"Producto actualizado","precio":12.0,"stock":3}'
```

## Notas / siguientes pasos
- `DELETE` también está protegido.
- Para producción, conviene mover credenciales a variables de entorno/secret manager y usar HTTPS.
