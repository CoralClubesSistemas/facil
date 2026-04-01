-- ============================================================
-- SP Corregido: spRepoGuardarColumnasUsuarioMasivo
-- Recibe las columnas como JSON y las inserta en un solo DELETE + INSERT masivo.
-- Evita el bug donde solo se guardaba la última columna por el DELETE en cada iteración.
-- ============================================================

CREATE OR ALTER PROCEDURE dbo.spRepoGuardarColumnasUsuarioMasivo
    @IdTipoReporte INT,
    @Usuario       VARCHAR(15),
    @ColumnasJSON  NVARCHAR(MAX)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        -- Eliminar TODAS las columnas previas del usuario para este reporte (UNA sola vez)
        DELETE FROM dbo.MEMORIA_TECNICA_COLUMNAS_REPORTES
        WHERE MTCR_LSV_TIPO_REPORTE = @IdTipoReporte
          AND MTCR_USR_USUARIO = @Usuario;

        -- Insertar todas las columnas nuevas desde el JSON
        INSERT INTO dbo.MEMORIA_TECNICA_COLUMNAS_REPORTES (
            MTCR_USR_USUARIO,
            MTCR_LSV_TIPO_REPORTE,
            MTCR_NOMBRE_COLUMNA,
            MTCR_ORDEN,
            MTCR_FECHA_REGISTRO
        )
        SELECT
            @Usuario,
            @IdTipoReporte,
            JSON_VALUE(value, '$.nombreColumna'),
            CAST(JSON_VALUE(value, '$.orden') AS INT),
            GETDATE()
        FROM OPENJSON(@ColumnasJSON);

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

PRINT 'SP spRepoGuardarColumnasUsuarioMasivo creado exitosamente.';
GO
