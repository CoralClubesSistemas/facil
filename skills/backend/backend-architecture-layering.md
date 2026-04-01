# Skill: Arquitectura y Capas del Backend (Spring Boot) - Coral Clubes

## Trigger (Cuándo usar esta skill)
Activa esta skill SIEMPRE que debas:
- Crear, modificar o analizar clases en el backend de Spring Boot (Java).
- Generar nuevos `Controllers`, `Services`, `Repositories` o `DTOs`.
- Estructurar un nuevo módulo o endpoint en la API REST.

## 1. Principio Fundamental: "El Backend es un Orquestador"
En Coral Clubes, **la lógica de negocio transaccional vive en SQL Server (Stored Procedures)**.
El backend en Spring Boot NO debe realizar cálculos complejos de negocio, validaciones de estado físico (ej. si una habitación está libre) ni transacciones multi-tabla manuales.
El rol del Backend es **orquestar**: recibir la petición, validar la forma del payload, extraer el usuario, llamar al SP, y disparar efectos secundarios (notificaciones, logs, almacenamiento de archivos).

## 2. Estructura de Capas Obligatoria

### A. Capa de Controladores (Controllers)
- **Anotaciones:** Usa `@RestController`, `@RequestMapping("/api/v1/modulo")` y `@RequiredArgsConstructor`.
- **Responsabilidad:** Solo enrutamiento HTTP, validación de entrada (`@Valid`) y control de acceso (`@PreAuthorize`).
- **Retorno:** DEBEN devolver siempre `ResponseEntity<ApiResponse<T>>`.
- **Regla estricta:** Un Controller NUNCA debe contener sentencias `if/else` para lógica, NUNCA debe atrapar excepciones (`try/catch`) y SOLO debe llamar métodos de la capa `Service`.

### B. Capa de Servicios (Services)
- **Anotaciones:** Usa `@Service`, `@Slf4j` (si requiere logs técnicos) y `@RequiredArgsConstructor`.
- **Responsabilidad:** 1. Extraer la identidad del usuario y desarrollo usando `UserContext`.
    2. Orquestar llamadas al `Repository`.
    3. Disparar integraciones (ej. `NotificationClient`, `StorageClient`).
    4. Registrar auditoría de negocio usando `BusinessLogger`.
- **Retorno:** DEBEN devolver directamente el objeto envuelto en la clase estándar `ApiResponse.success(...)` o `ApiResponse.error(...)`.

### C. Capa de Acceso a Datos (Repositories)
- **Anotaciones:** Usa `@Repository` y `@RequiredArgsConstructor`.
- **Responsabilidad:** Es el ÚNICO lugar que interactúa con la base de datos.
- **Implementación:** DEBE inyectar e invocar la librería propia `StoredProcedureExecutor`.
- **Mapeo:** Construye los parámetros usando `Map.of(...)` o `HashMap`. Mapea los resultados de SQL utilizando `RowMapper<T>` como constantes privadas o lambdas (no uses clases anónimas viejas).
- **Nomenclatura:** El método que llama al SP DEBE tener el mismo nombre que el SP (ej. `spCajaCobrar`).
- **Prohibición:** NO uses Spring Data JPA (`@Entity`, `JpaRepository`) para procesos transaccionales del negocio (Check-In, Pagos, Inventario). Todo va por SP.

### D. Capa de Transferencia de Datos (DTOs)
- **Tecnología:** Usa EXCLUSIVAMENTE **Java Records** (Java 21) para Request y Response (ej. `public record MiRequestDto(...) {}`). NO uses clases tradicionales con `@Data` o getters/setters.
- **Validación:** Coloca las anotaciones de Jakarta Validation (`@NotNull`, `@NotBlank`, `@Min`) directamente en los atributos del record.
- **Anidación:** Usa `@Builder` si el record es complejo o requiere ser ensamblado paso a paso en el Service.
- **Projección:** Si el SP devuelve un resultado complejo que no sera tal cual devuelto al como la respuesta (ej. Un resultado que devuelve un uuid que el backend debe canjear por la url real),
crea un record específico para mapear esa respuesta (ej. `public record ResvDetalleResponse(...) {}`) y úsalo en el Repository.
- **Directorio:** Los DTOs de respuesta (Response) deben ir en el paquete `dto.response`, los de solicitud (Request) en `dto.request` y los de mapeo interno (ej. resultados de SP) en `dto.projection` dentro de cada modulo.

## 3. Inyección de Dependencias
- PROHIBIDO usar `@Autowired` en atributos (Field Injection).
- ÚNICO MÉTODO PERMITIDO: Inyección por constructor utilizando atributos `private final` y la anotación `@RequiredArgsConstructor` de Lombok a nivel de clase.

## 4. Prohibiciones Absolutas (Anti-Patrones)
1. **Lógica Matemática/Negocio en Java:** Prohibido calcular impuestos, validaciones de stock o penalizaciones en Java. Llama a la función o SP de SQL Server para que te devuelva el valor.
2. **Romper el ApiResponse:** Prohibido devolver un `String`, un `List<T>` crudo o un `Map` directamente en el Controller. TODO sale envuelto en `ApiResponse<T>`.
3. **Cadenas quemadas (Hardcoding):** Prohibido usar roles quemados en `@PreAuthorize` si existe un Enum de permisos. Prohibido quemar URLs de otros servicios (usa `@Value`).
4. **Pasar el Request a la BD:** Prohibido pasar el objeto HTTP Request/Response o sesiones directamente al Repository. El Service extrae los datos puros y los manda.

## 5. Ejemplo de Orquestación
```java
// CONTROLLER
@RestController
@RequestMapping("/api/v1/ejemplo")
@RequiredArgsConstructor
public class EjemploController {
    private final EjemploService service;

    @PostMapping("/procesar")
    @PreAuthorize("hasAuthority('MOD_EJEMPLO')")
    public ResponseEntity<ApiResponse<Boolean>> procesar(@Valid @RequestBody EjemploRequest request) {
        return ResponseEntity.ok(service.procesarDatos(request));
    }
}

// SERVICE
@Service
@RequiredArgsConstructor
public class EjemploService {
    private final EjemploRepository repository;
    private final UserContext userContext;
    private final BusinessLogger businessLogger;

    public ApiResponse<Boolean> procesarDatos(EjemploRequest request) {
        String usuario = userContext.getUsername();
        repository.ejecutarProceso(request, usuario);
        businessLogger.info(usuario, "Proceso ejecutado: {}", request.id());
        return ApiResponse.success("Proceso exitoso", true);
    }
}
```