# AGENTS.md

## Panorama rápido
- Backend **Java 21 + Spring Boot 3.5** para **Coral Clubes / FACIL**.
- La app se organiza por módulos en `src/main/java/com/coralclubes/facil/modules/*` y por capacidades compartidas en `src/main/java/com/coralclubes/facil/shared/*`.
- Regla central: **la lógica transaccional vive en SQL Server (Stored Procedures)**; Spring Boot orquesta, valida forma y coordina efectos secundarios.

## Flujo de trabajo obligatorio (SDD)
1. Antes de proponer código, revisa memoria histórica con Engram (`mem_search` / `mem_read`) cuando aplique.
2. Lee **solo** las skills necesarias en `skills/` según la tarea.
3. Haz un plan paso a paso y pide confirmación antes de escribir la primera línea de código.
4. Implementa respetando las prohibiciones y convenciones de las skills cargadas.
5. Si resuelves un bug complejo o tomas una decisión arquitectónica nueva, registra el aprendizaje con `mem_save`.

## Lee primero
- `agent.md` para las reglas operativas originales del equipo.
- `pom.xml` para dependencias y plugins (MapStruct, Lombok, Spring Modulith, JJWT, OpenHTMLtoPDF, POI).
- `src/main/resources/application.yml` para perfiles y variables de entorno.
- `src/main/java/com/coralclubes/facil/shared/config/SecurityConfig.java`.
- `src/main/java/com/coralclubes/facil/shared/infrastructure/repository/StoredProcedureExecutor.java`.
- `src/main/java/com/coralclubes/facil/shared/infrastructure/security/service/UserContext.java`.
- `src/main/java/com/coralclubes/facil/shared/infrastructure/exceptions/GlobalExceptionHandler.java`.

## Router de skills
- SQL Server: `skills/backend/sql-server-core.md`.
- Reservaciones, Check-In/Check-Out o Lock-offs: `skills/domain/reservaciones-rules.md`.
- Controllers, Services, Repositories o DTOs: `skills/backend/backend-architecture-layering.md`.
- Acceso a datos / SPs / mapeo de `ResultSet`: `skills/backend/backend-data-access.md`.
- Seguridad, permisos o extracción de identidad: `skills/backend/backend-security-context.md`.
- Respuestas HTTP / excepciones: `skills/backend/backend-error-handling.md`.
- Comunicación entre microservicios: `skills/backend/backend-microservices-comm.md`.
- Notificaciones: `skills/integrations/coral-notificaciones-api.md`.
- Storage / PDF / archivos: `skills/integrations/coral-storage-api.md`.
- Git, ramas y PRs: `skills/workflow-git-standards.md`.

## Patrones que sí usa este repo
- `Controllers` devuelven `ResponseEntity<ApiResponse<T>>`; no llevan `try/catch` ni lógica de negocio.
- `Services` extraen identidad con `UserContext`, llaman a repositorios y disparan integraciones.
- `Repositories` usan **solo** `StoredProcedureExecutor`; el nombre del método suele coincidir con el SP.
- Los DTOs del dominio se modelan como **Java records**; separa `dto.request`, `dto.response` y `dto.projection`.
- Ejemplos claros: `modules/reservaciones/service/ReservacionesService.java` y `modules/reportes/controller/ReportesController.java`.
- `GatewayHeaderFilter` consume `X-Auth-*`; no leas JWT manualmente en controllers/services.
- Permisos con `@PreAuthorize`: prefijos `MOD_` y `AUTH_`.

## Integraciones clave
- `NotificationClient` llama a `/api/v1/notificaciones/enviar` con `X-API-KEY` y patrón *fire-and-forget*.
- `StorageClient` negocia `sign-upload` y luego hace carga directa a URL prefirmada (*Valet Key*).
- `RabbitMQ` se usa para eventos y procesos asíncronos; `Redis` y `SQL Server` forman parte del entorno esperado.

## Convenciones específicas
- Stored Procedures: prefijo `sp` + módulo + acción, por ejemplo `spResv...` o `spCaja...`.
- Respuestas de error: favorece `ApiResponse.error(...)` y el `GlobalExceptionHandler` antes que cuerpos crudos.
- No rompas la separación por módulo; cuando trabajes en una feature, revisa también la skill correspondiente en `skills/`.
- No uses JPA para la lógica transaccional del negocio; el estándar del repo es Stored Procedures.

## Validación y entorno
```powershell
.\mvnw.cmd clean compile
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```
- Usa `SPRING_PROFILES_ACTIVE=dev` o `qa` según el entorno.
- `compose.yaml` está vacío actualmente; para ejecutar localmente normalmente necesitas servicios externos o tu propia infraestructura de apoyo.

## Variables de entorno importantes
- `DB_URL`, `DB_USER`, `DB_PASS`
- `JWT_SECRET`
- `STORAGE_SERVICE_URL`, `STORAGE_API_KEY`
- `NOTIFICATIONS_SERVICE_URL`, `NOTIFICATIONS_API_KEY`
- `RABBIT_HOST`, `RABBIT_USER`, `RABBIT_PASS`
- `REDIS_HOST`, `REDIS_PORT`


