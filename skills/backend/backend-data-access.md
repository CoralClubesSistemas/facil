# Skill: Acceso a Datos y Repositorios (Spring Boot) - Coral Clubes

## Trigger (Cuándo usar esta skill)

Activa esta skill SIEMPRE que debas:

- Crear o modificar clases en la capa `Repository` de Java.
- Ejecutar Stored Procedures (SPs) o Funciones de SQL Server desde Spring Boot.
- Mapear resultados de la base de datos (`ResultSet`) hacia DTOs (Java Records).

## 1. Principio Fundamental: "Cero Lógica de Datos en Java"

El acceso a datos en Coral Clubes está estrictamente confinado a la ejecución de Procedimientos Almacenados (SPs).
Los Repositorios en Java NO construyen consultas SQL, NO hacen `JOINs` en memoria y NO usan ORMs transaccionales. Son
meros "traductores" entre los tipos de datos de Java y los parámetros del SP.

## 2. El `StoredProcedureExecutor` (Obligatorio)

- **Prohibido:** No inyectes `JdbcTemplate` o `NamedParameterJdbcTemplate` directamente a menos que sea una consulta
  escalar a una función (`SELECT dbo.fn...`).
- **Obligatorio:** Inyecta siempre nuestra librería interna `StoredProcedureExecutor` usando `private final` y
  `@RequiredArgsConstructor`.
- **Métodos disponibles:**
    - `execute(spName, params)` -> Para SPs que no devuelven datos (ej. Inserciones simples, Updates).
    - `executeLog(spName, params)` -> Igual que execute, pero retorna `boolean` indicando éxito y loguea la acción.
    - `querySingle(spName, params, rowMapper)` -> Devuelve un `Optional<T>`. Usar cuando esperas 0 o 1 registro (ej.
      Obtener Detalle de Reserva).
    - `queryList(spName, params, rowMapper)` -> Devuelve `List<T>`. Usar cuando esperas múltiples registros (ej.
      Catálogos, Dashboards).

## 3. Construcción de Parámetros

- **Map.of():** Úsalo si los parámetros del SP son fijos y no superan los 10 (límite de Java). Es inmutable y limpio.
- **HashMap:** Úsalo si necesitas agregar parámetros condicionalmente o si superas los 10 parámetros.
- NUNCA pases objetos completos (`RequestDto`) al `StoredProcedureExecutor`. Extrae y mapea llave-valor exactamente con
  el nombre de las variables del SP en SQL Server (sin la `@`).

## 4. Mapeo de Resultados (RowMapper)

- **Definición:** Declara los `RowMapper<T>` como constantes `private final` dentro de la clase `Repository` utilizando
  lambdas. NO uses clases anónimas.
- **Java Records:** El RowMapper siempre debe instanciar un Java Record o usar su `Builder`.
- **Campos JSON:** Si el SP devuelve una columna con JSON (ej. `CargosJson`), extráela como `String` y parsea usando
  nuestra utilidad interna `JsonUtils.fromJson(jsonStr, new TypeReference<>() {})` dentro del mismo mapper.
- **Manejo de Nulos:** Valida nulos al extraer de BD, especialmente fechas (
  `rs.getDate("Campo") != null ? rs.getDate("Campo").toLocalDate() : null`) y campos JSON (
  `jsonStr != null && !jsonStr.isBlank()`).

## 5. Prohibiciones Absolutas (Anti-Patrones)

1. **PROHIBIDO Spring Data JPA:** No uses `@Entity`, `@Table`, ni interfaces que extiendan de `JpaRepository` para la
   operativa del negocio.
2. **PROHIBIDO SQL Dinámico:** No construyas `String sql = "SELECT * FROM..."` dentro de Java. Si necesitas un query
   dinámico, hazlo dentro del SP en SQL Server.
3. **PROHIBIDO Lógica en el Mapper:** El `RowMapper` es solo para traducir tipos de datos. No calcules totales ni hagas
   validaciones condicionales dentro del mapeo. (Esos cálculos se hacen en el SP).
4. **PROHIBIDO Control de Transacciones en Java:** No uses `@Transactional` en los Repositorios ni en los Services para
   controlar operaciones DML, ya que el control transaccional (COMMIT/ROLLBACK) vive dentro del SP con
   `SET XACT_ABORT ON`. Solo se permite si coordinas múltiples SPs.

## 6. Ejemplo de Acceso a Datos Perfecto (Golden Path)

```java
package com.coralclubes.facil.modules.ejemplo.repository;

import com.coralclubes.facil.modules.ejemplo.dto.response.EjemploDto;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class EjemploRepository {

    private final StoredProcedureExecutor spExecutor;

    // RowMapper inmutable usando Lambdas y Java Records
    private final RowMapper<EjemploDto> ejemploMapper = (rs, rowNum) -> EjemploDto.builder()
            .id(rs.getInt("IdEjemplo"))
            .descripcion(rs.getString("Descripcion"))
            .fechaRegistro(rs.getTimestamp("FechaRegistro") != null ? rs.getTimestamp("FechaRegistro").toLocalDateTime() : null)
            .build();

    /**
     * Ejecuta el SP para obtener el listado.
     */
    public List<EjemploDto> obtenerListaEjemplo(Integer desarrolloId, String estatus) {
        Map<String, Object> params = Map.of(
                "DesarrolloId", desarrolloId,
                "Estatus", estatus
        );

        return spExecutor.queryList(
                "spEjemploObtenerLista",
                params,
                ejemploMapper
        );
    }

    /**
     * Ejecuta un SP de escritura (Insert/Update).
     */
    public void registrarAccion(Integer id, String usuario) {
        Map<String, Object> params = Map.of(
                "Id", id,
                "Usuario", usuario
        );

        spExecutor.execute("spEjemploRegistrarAccion", params);
    }
}
```