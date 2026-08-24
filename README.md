# Franchises API

[![CI](https://github.com/musicstev/franchises-api/actions/workflows/ci.yml/badge.svg)](https://github.com/musicstev/franchises-api/actions/workflows/ci.yml)

API reactiva para la gestión de franquicias, sus sucursales y los productos ofertados en cada sucursal.

**Autor:** William Gomez

## Stack tecnológico

- **Java 21** + **Spring Boot 3.5** (WebFlux — programación reactiva con Project Reactor)
- **MongoDB** (driver reactivo de Spring Data)
- **Lombok** (modelos inmutables con `@Value`, `@Builder`, `@With`)
- **Arquitectura hexagonal** (puertos y adaptadores)
- **JaCoCo** con verificación de cobertura mínima del **95%** + pruebas de integración con **Testcontainers**
- **Docker** y **Docker Compose** para el empaquetado y despliegue local
- **GitHub Actions** ejecuta `./mvnw verify` en cada push/PR

## Arquitectura

```
src/main/java/com/franchises
├── domain                      # Núcleo del dominio (sin dependencias de frameworks)
│   ├── model                   # Franchise, Branch, Product, TopStockProduct (inmutables)
│   └── exception               # Excepciones de negocio
├── application
│   ├── port
│   │   ├── in                  # FranchiseUseCase (puerto de entrada)
│   │   └── out                 # FranchiseRepository (puerto de salida)
│   └── service                 # FranchiseService (casos de uso reactivos)
└── infrastructure
    └── adapter
        ├── in/web              # Router funcional, handler, DTOs, validación, errores
        └── out/mongodb         # Adaptador de persistencia + documentos + mapper
```

- El **dominio** es puro: modelos inmutables cuya lógica de negocio (agregar/renombrar/eliminar, producto con mayor stock) se expresa con funciones que devuelven nuevas instancias (`Stream`, `Optional`, `UnaryOperator`).
- La **aplicación** orquesta los casos de uso de forma reactiva (`Mono`/`Flux`) y solo conoce los puertos.
- La **infraestructura** contiene los adaptadores: endpoints funcionales de WebFlux (`RouterFunction`) y persistencia en MongoDB.

## Requisitos previos

Para ejecutar con Docker (recomendado) solo necesitas:

- [Docker](https://docs.docker.com/get-docker/) con Docker Compose

Para ejecutar en local sin Docker:

- Java 21 (JDK) — no hace falta instalar Maven: el proyecto incluye el wrapper (`./mvnw`)
- Una instancia de MongoDB accesible (por defecto `mongodb://localhost:27017/franchises`)

## Despliegue local

### Opción 1 — Docker Compose (recomendada)

Levanta MongoDB y el API (construyendo la imagen del proyecto) con un solo comando:

```bash
docker compose up --build
```

El API queda disponible en `http://localhost:8080` y MongoDB en `localhost:27017`.

Para detener y limpiar:

```bash
docker compose down
```

(agrega `-v` si además quieres borrar los datos de MongoDB).

> **Nota:** si tu instalación de Docker no incluye el plugin de Compose (p. ej. Colima), instálalo con `brew install docker-compose` o usa la alternativa manual:
>
> ```bash
> docker build -t franchises-api .
> docker network create franchises-net
> docker run -d --name mongo --network franchises-net -p 27017:27017 mongo:7
> docker run -d --name api --network franchises-net -p 8080:8080 -e MONGO_URI=mongodb://mongo:27017/franchises franchises-api
> ```

### Opción 2 — Maven en local

1. Levanta MongoDB (por ejemplo con Docker):

   ```bash
   docker run -d --name mongo -p 27017:27017 mongo:7
   ```

2. Ejecuta la aplicación:

   ```bash
   ./mvnw spring-boot:run
   ```

La URI de conexión puede sobrescribirse con la variable de entorno `MONGO_URI` y el puerto con `SERVER_PORT`.

### Opción 3 — Aprovisionar la persistencia con Terraform (IaC)

La base de datos también puede aprovisionarse de forma declarativa en lugar de con
`docker-compose`. El API solo depende de `MONGO_URI`, así que la infraestructura es
intercambiable:

```bash
cd infra/local
terraform init
terraform plan                              # muestra el diff, no aplica nada
terraform apply
export MONGO_URI=$(terraform output -raw mongo_uri)
./mvnw spring-boot:run
terraform destroy                           # limpieza total al terminar
```

`infra/atlas/` provisiona lo mismo sobre un clúster M0 (capa gratuita) de MongoDB
Atlas, para quien quiera demostrarlo contra una nube real. Detalles en
[`infra/README.md`](infra/README.md).

> Terraform y `docker compose` son **alternativas**, no se ejecutan a la vez sobre el
> mismo puerto 27017 (usa `-var mongo_port=27018` en `infra/local` si necesitas que
> convivan).

## Pruebas y cobertura

```bash
./mvnw verify
```

Esto ejecuta, en orden:

1. **Pruebas unitarias** (Surefire, fase `test`) — dominio, aplicación e infraestructura con mocks. Rápidas, no requieren Docker.
2. **Prueba de integración** (Failsafe, fase `verify`, sufijo `*IT`) — levanta un **MongoDB real** con [Testcontainers](https://testcontainers.com/) y valida contra él el mapeo del agregado y el bloqueo optimista (`@Version`). Requiere Docker en ejecución.
3. **Verificación de cobertura** (JaCoCo) — la build **falla** si la cobertura de instrucciones cae por debajo del **95%**.

El reporte de cobertura queda en `target/site/jacoco/index.html`.

Para correr solo las pruebas rápidas (sin Docker):

```bash
./mvnw test
```

Si no tienes Java instalado, puedes ejecutar todo dentro de Docker (monta el socket para que Testcontainers pueda levantar contenedores):

```bash
docker run --rm -v "$PWD":/workspace -v /var/run/docker.sock:/var/run/docker.sock \
  -w /workspace eclipse-temurin:21-jdk ./mvnw verify
```

> **Nota (Colima / Rancher Desktop / Podman):** si Testcontainers falla con *"Could not find a valid Docker environment"* o al arrancar el contenedor `ryuk`, es un problema conocido de negociación del API de Docker y de montaje del socket en daemons que no son Docker Desktop. Soluciones:
>
> ```bash
> echo "api.version=1.41" > ~/.docker-java.properties
> export TESTCONTAINERS_RYUK_DISABLED=true
> ```
>
> No hace falta en GitHub Actions (Docker nativo, sin este problema) ni en Docker Desktop.

## Documentación API (Swagger)

Con el API corriendo, la documentación interactiva (OpenAPI 3) está disponible en:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Especificación OpenAPI (JSON)**: http://localhost:8080/v3/api-docs

Desde Swagger UI puedes explorar cada endpoint (parámetros, esquemas de request/response, códigos de error) y ejecutar peticiones de prueba directamente contra el API en ejecución.

## Colección de Postman

El repositorio incluye [`postman-collection.json`](postman-collection.json) con los 9 endpoints listos para ejecutar en orden (crear franquicia → agregar sucursal → agregar producto → consultas y actualizaciones).

**Importarla:**

1. En Postman: `File → Import` y selecciona `postman-collection.json`.
2. Verifica que la variable de colección `baseUrl` apunte a tu API (por defecto `http://localhost:8080`).

**Cómo está armada:**

- Las peticiones **"Crear franquicia"**, **"Agregar sucursal"** y **"Agregar producto"** guardan automáticamente el `id` generado por el servidor en las variables de colección `franchiseId`, `branchId` y `productId` (vía scripts de test), así que el resto de peticiones los reutilizan sin copiar/pegar nada.
- Las variables `branchName` y `productName` vienen precargadas (`Sucursal Chapinero`, `Café Americano`) y se usan como el **nombre** enviado en el cuerpo de las peticiones de creación/renombrado — no como identificador de URL (ver "Decisiones de diseño" más abajo).
- Orden sugerido de ejecución: Crear franquicia → Agregar sucursal → Agregar producto → el resto de peticiones (actualizar nombres, modificar stock, eliminar producto, producto con más stock por sucursal) en cualquier orden.

## Endpoints

Base: `http://localhost:8080/api/franchises`

| Método | Ruta | Descripción |
|--------|------|-------------|
| `POST` | `/api/franchises` | Crear una franquicia |
| `PATCH` | `/api/franchises/{franchiseId}/name` | Actualizar el nombre de una franquicia |
| `POST` | `/api/franchises/{franchiseId}/branches` | Agregar una sucursal a la franquicia |
| `PATCH` | `/api/franchises/{franchiseId}/branches/{branchId}/name` | Actualizar el nombre de una sucursal |
| `POST` | `/api/franchises/{franchiseId}/branches/{branchId}/products` | Agregar un producto a la sucursal |
| `DELETE` | `/api/franchises/{franchiseId}/branches/{branchId}/products/{productId}` | Eliminar un producto de la sucursal |
| `PATCH` | `/api/franchises/{franchiseId}/branches/{branchId}/products/{productId}/stock` | Modificar el stock de un producto |
| `PATCH` | `/api/franchises/{franchiseId}/branches/{branchId}/products/{productId}/name` | Actualizar el nombre de un producto |
| `GET` | `/api/franchises/{franchiseId}/top-stock-products` | Producto con más stock por sucursal de la franquicia |

Sucursales y productos se identifican por un `id` generado por el servidor al crearlos (igual que la franquicia), no por su nombre — captura el `id` de la respuesta de creación y úsalo en las peticiones siguientes. Ver [Decisiones de diseño](#decisiones-de-diseño).

### Ejemplos con `curl`

Crear una franquicia (la respuesta incluye el `id` generado, úsalo en el resto de peticiones):

```bash
curl -s -X POST http://localhost:8080/api/franchises -H 'Content-Type: application/json' -d '{"name":"Mi Franquicia"}'
```

Agregar una sucursal (la respuesta incluye `branches[].id`, captúralo para las siguientes peticiones):

```bash
curl -s -X POST http://localhost:8080/api/franchises/{franchiseId}/branches -H 'Content-Type: application/json' -d '{"name":"Sucursal Centro"}'
```

Agregar un producto (la respuesta incluye `branches[].products[].id`):

```bash
curl -s -X POST http://localhost:8080/api/franchises/{franchiseId}/branches/{branchId}/products -H 'Content-Type: application/json' -d '{"name":"Café","stock":25}'
```

Modificar el stock:

```bash
curl -s -X PATCH http://localhost:8080/api/franchises/{franchiseId}/branches/{branchId}/products/{productId}/stock -H 'Content-Type: application/json' -d '{"stock":40}'
```

Eliminar un producto:

```bash
curl -s -X DELETE http://localhost:8080/api/franchises/{franchiseId}/branches/{branchId}/products/{productId}
```

Producto con más stock por sucursal:

```bash
curl -s http://localhost:8080/api/franchises/{franchiseId}/top-stock-products
```

Renombrar franquicia / sucursal / producto (la URL no cambia al renombrar, precisamente porque direcciona por `id`):

```bash
curl -s -X PATCH http://localhost:8080/api/franchises/{franchiseId}/name -H 'Content-Type: application/json' -d '{"name":"Nueva Marca"}'
```

```bash
curl -s -X PATCH http://localhost:8080/api/franchises/{franchiseId}/branches/{branchId}/name -H 'Content-Type: application/json' -d '{"name":"Centro Histórico"}'
```

```bash
curl -s -X PATCH http://localhost:8080/api/franchises/{franchiseId}/branches/{branchId}/products/{productId}/name -H 'Content-Type: application/json' -d '{"name":"Café Premium"}'
```

## Decisiones de diseño

### Identificación de sucursales y productos

El enunciado describe la sucursal y el producto únicamente con `nombre` (y su lista/stock), sin un identificador explícito. La primera versión de este API direccionaba ambos por nombre en la URL, consistente con esa descripción literal.

Al probar la API con casos límite (nombres con `/`, duplicados que difieren solo en mayúsculas o espacios) identifiqué que usar un atributo de negocio mutable como clave de dirección viola un principio básico de REST: la URI de un recurso debe ser estable e independiente de sus atributos. La franquicia ya seguía el patrón correcto (`id` generado por Mongo); sucursales y productos no.

**Decisión:** alinear ambos con el mismo estándar — `id` generado por el servidor (UUID) como identificador estable, `name` como atributo mutable normal. Ventajas: las URLs no cambian al renombrar, sin ambigüedad de mayúsculas/espacios, sin caracteres reservados de URL en el identificador. El nombre sigue siendo único dentro de su padre (409 en duplicados), pero como regla de negocio, no como mecanismo de direccionamiento.

## Manejo de errores

| Situación | Código | Cuerpo |
|-----------|--------|--------|
| Franquicia / sucursal / producto no encontrado | `404` | `{"status":404,"error":"Not Found","message":"..."}` |
| Nombre duplicado (sucursal o producto) | `409` | `{"status":409,"error":"Conflict","message":"..."}` |
| Cuerpo inválido (nombre en blanco, stock negativo, sin cuerpo) | `400` | `{"status":400,"error":"Bad Request","message":"..."}` |
