# Skill: Estándares Core de SQL Server - Coral Clubes

## Trigger (Cuándo usar esta skill)
Activa esta skill SIEMPRE que debas interactuar con la base de datos:
- Crear, modificar o analizar Stored Procedures (SPs), Funciones o Vistas.
- Diseñar nuevas tablas o esquemas.
- Escribir consultas SQL complejas, sin importar el módulo del sistema.

## 1. Nomenclatura y Convenciones (Obligatorio)
- **Tablas:** Siempre en MAYÚSCULAS y en plural (ej. `RESERVACIONES_CLIENTES`, `INV_ARTICULOS`).
- **Columnas:** Prefijo de 3 o 4 letras que identifique a la tabla, seguido de guion bajo y nombre descriptivo (ej. Para `INV_ARTICULOS`, usar `ART_ID`, `ART_NOMBRE`).
- **Stored Procedures (SPs):** Prefijo `sp` + Módulo (CamelCase) + Acción (ej. `spInvAjustarStock`, `spCajaCobrar`).
- **Funciones:** Prefijo `fn` + Módulo + Acción (ej. `fnObtenerTipoCambio`).
- **Vistas:** Prefijo `vw` + Módulo + Nombre (ej. `vwResumenVentas`).

## 2. El Patrón "Listas Valores" (Catálogo Universal)
NUNCA crees tablas de catálogo pequeñas (ej. `CatEstatus`, `CatTipos`). Todo catálogo en Coral Clubes vive en la tabla maestra `LISTAS_VALORES`.
Para buscar un estatus o tipo, siempre debes hacer subconsultas o JOINs usando `LSV_TABLA` y `LSV_CLAVE`.
*Ejemplo correcto:*
```sql
DECLARE @EstatusActivo INT = (SELECT TOP 1 LSV_ID FROM dbo.LISTAS_VALORES WITH(NOLOCK) WHERE LSV_TABLA = 'ESTATUSGENERAL' AND LSV_CLAVE = 'ACTIVO');
```

## 3. Estructura Obligatoria de un Stored Procedure
Todo SP que modifique datos (INSERT, UPDATE, DELETE) DEBE seguir esta plantilla transaccional para evitar bloqueos:

```sql
CREATE OR ALTER PROCEDURE dbo.spModuloAccion
    @Parametro1 INT,
    @Usuario VARCHAR(50)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON; -- Obligatorio para rollback automático ante errores severos

    DECLARE @FechaHoy DATETIME = GETDATE();
    
    BEGIN TRY
        -- Validaciones previas (Usar UPLOCK si se va a modificar la fila)
        IF NOT EXISTS (SELECT 1 FROM Tabla WITH(UPDLOCK, ROWLOCK) WHERE ID = @Parametro1)
            THROW 50001, 'Mensaje de error claro para el usuario.', 1;

        BEGIN TRANSACTION;
            -- Operaciones DML (Updates, Inserts)
            -- Inserción de Auditoría (AUDITORIA_MOVIMIENTOS)
        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW; -- Propaga el error a la capa de Spring Boot
    END CATCH
END;
GO
```

## 4. Prohibiciones (Qué NO hacer)

- NO usar cursores \(`CURSOR`\) a menos que sea matemáticamente imposible resolverlo con operaciones basadas en conjuntos \(`Sets`/`JOINs`\)\.
- NO devolver mensajes de éxito como `SELECT 'OK'`\. El éxito se asume si no entra al `CATCH`\.
- NO poner lógica de presentación \(ej\. formatear fechas como `DD/MM/YYYY`\)\. Retorna los tipos nativos \(`DATETIME`, `DECIMAL`\)\.