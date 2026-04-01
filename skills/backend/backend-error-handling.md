# Skill: Manejo de Errores y Respuestas (Spring Boot) - Coral Clubes

## Trigger (Cuándo usar esta skill)

Activa esta skill SIEMPRE que debas:

- Diseñar el retorno de un Controller o Service.
- Lanzar excepciones de negocio o validación.
- Manejar flujos donde una operación (como un SP) falla.

## 1. Principio Fundamental: "Respuestas Predecibles"

El Frontend (Angular) espera que TODOS los endpoints devuelvan exactamente la misma estructura JSON (`ApiResponse<T>`).
Una ruptura en este contrato causará pantallas blancas o errores de parseo en el cliente.

## 2. Estructura del ApiResponse

- **Éxito:** `ApiResponse.success("Mensaje para el usuario", dataObjeto);`
- **Error Controlado:** `ApiResponse.error(GeneralResponseCode.CONFLICT, "Mensaje amigable del error");`
- NUNCA devuelvas objetos crudos como `ResponseEntity.ok(miListaDto);`. DEBE ser
  `ResponseEntity.ok(ApiResponse.success(..., miListaDto));`.

## 3. Lanzamiento de Excepciones (Services)

- Si un parámetro es inválido lógicamente o un SP no devuelve lo esperado, lanza una excepción nativa:
    - `throw new IllegalArgumentException("La fecha de salida debe ser mayor a la entrada.");`
    - `throw new RuntimeException("Error crítico: La BD no devolvió folios.");`
- **NOTA:** El sistema cuenta con un `@RestControllerAdvice` global que interceptará estas excepciones y las envolverá
  automáticamente en un `ApiResponse.error(...)` con HTTP 400/500, asegurando que el cliente nunca vea un StackTrace de
  Java.

## 4. Prohibiciones Absolutas (Anti-Patrones)

1. **PROHIBIDO Bloques `try/catch` en Controllers:** Los Controllers nunca atrapan excepciones.
2. **PROHIBIDO Silenciar Errores:** No hagas `catch (Exception e) { return null; }`. Si atrapas un error, debes
   re-lanzarlo o devolver un `ApiResponse.error`.
3. **PROHIBIDO Filtrar StackTraces al Cliente:** Nunca pongas `e.getMessage()` directamente en el `ApiResponse.error` si
   es un `SQLException` o error de infraestructura. Los mensajes de error deben ser entendibles por un humano (ej. "La
   base de datos no está disponible en este momento").
4. **PROHIBIDO Modificar Código HTTP Manualmente:** No uses `ResponseEntity.status(404).body(...)` a menos que sea
   estrictamente necesario en integraciones externas. Para la API interna, confía en el Global Exception Handler y
   `ApiResponse`.