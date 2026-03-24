# Skill: Contexto de Seguridad e Identidad (Spring Boot) - Coral Clubes

## Trigger (Cuándo usar esta skill)
Activa esta skill SIEMPRE que debas:
- Proteger un nuevo endpoint o controlador (`Controllers`).
- Extraer el nombre de usuario, roles o identificadores (ej. `idDesarrollo`) del usuario autenticado.
- Configurar reglas de acceso o permisos (`Authorities`).

## 1. Principio Fundamental: "Identidad Centralizada"
En Coral Clubes, la extracción del token JWT y la sesión del usuario está abstraída. NUNCA debes leer las cabeceras HTTP (`HttpServletRequest`) manualmente para extraer el `Authorization: Bearer`. Toda la identidad fluye a través del bean inyectable `UserContext`.

## 2. Extracción de Identidad (Capa Service)
- **Herramienta Obligatoria:** Inyecta `private final UserContext userContext;` usando `@RequiredArgsConstructor`.
- **Uso:** - Para obtener el usuario que realiza la acción: `String usuario = userContext.getUsername();`
    - Para obtener el hotel/sucursal actual: `Integer idDesarrollo = userContext.getIdDesarrollo();`
- **Ubicación:** Esta extracción se hace EXCLUSIVAMENTE en la capa `Service`. El Controller no debe saber quién es el usuario.

## 3. Protección de Endpoints (Capa Controller)
- **Herramienta Obligatoria:** Usa la anotación `@PreAuthorize` a nivel de método en los `Controllers`.
- **Nomenclatura de Permisos:** Los permisos en Coral Clubes utilizan el prefijo `MOD_` seguido del nombre del módulo en mayúsculas (ej. `MOD_SMNURECEPCION`, `MOD_SMNUHOUSEKEEPING`).
- O el prefijo AUTH_ seguido de la clave de la autorizacion especial
- *Ejemplo:* `@PreAuthorize("hasAuthority('MOD_SMNUCAJA')")`
- *Ejemplo:* `@PreAuthorize("hasAuthority('AUTH_ESPECIAL')")`

## 4. Prohibiciones Absolutas (Anti-Patrones)
1. **PROHIBIDO Hardcodear IDs:** Nunca quemes el `idDesarrollo` o `username` en el código (ej. `String user = "ADMIN";`). Siempre extráelo de `UserContext`.
2. **PROHIBIDO `SecurityContextHolder` Directo:** No llames a `SecurityContextHolder.getContext().getAuthentication()` manualmente. Usa `UserContext`.
3. **PROHIBIDA la Lógica de Roles en Java:** No uses sentencias `if (usuario.hasRole("ADMIN"))` en los `Services` para bifurcar lógica. Si un usuario no tiene permiso, el `@PreAuthorize` lo debe bloquear antes de que llegue al Service.

## 5. Golden Path
```java
// En el Controller:
@PostMapping("/ejecutar")
@PreAuthorize("hasAuthority('MOD_SMNUCAJA')")
public ResponseEntity<ApiResponse<Boolean>> ejecutarAccion(@RequestBody AccionRequest request) {
    return ResponseEntity.ok(service.ejecutar(request));
}

// En el Service:
public ApiResponse<Boolean> ejecutar(AccionRequest request) {
    String usuario = userContext.getUsername(); // Extrae la identidad limpiamente
    Integer idDesarrollo = userContext.getIdDesarrollo();
    repository.guardar(request, usuario, idDesarrollo);
    return ApiResponse.success("Acción ejecutada", true);
}
```