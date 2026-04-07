-- =========================================================================
-- MÓDULO DE MANUALES DE USUARIO
-- Estándares: Coral Clubes (SDD)
-- =========================================================================

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'MANUALES')
BEGIN
    CREATE TABLE MANUALES (
        MNL_ID INT PRIMARY KEY IDENTITY(1,1),
        MNL_NOMBRE VARCHAR(255) NOT NULL,
        MNL_DESCRIPCION VARCHAR(500) NULL,
        MNL_MDL_ID INT NOT NULL,
        MNL_ACTIVO BIT NOT NULL DEFAULT 1,
        MNL_FECHA_REGISTRO DATETIME NOT NULL DEFAULT GETDATE(),
        MNL_FECHA_MODIFICACION DATETIME NULL,
        FOREIGN KEY (MNL_MDL_ID) REFERENCES MODULOS(MDL_ID)
    );
END
GO

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'MANUALES_VERSIONES')
BEGIN
    CREATE TABLE MANUALES_VERSIONES (
        MNV_ID INT PRIMARY KEY IDENTITY(1,1),
        MNV_MNL_ID INT NOT NULL,
        MNV_VERSION INT NOT NULL,
        MNV_DESCRIPCION_CAMBIO VARCHAR(500) NULL,
        MNV_UUID_ARCHIVO UNIQUEIDENTIFIER NOT NULL,
        MNV_NOMBRE_ARCHIVO VARCHAR(255) NOT NULL,
        MNV_TIPO VARCHAR(50) NOT NULL, -- PDF, VIDEO, LINK, IMAGE, DOC
        MNV_URL VARCHAR(500) NULL,
        MNV_ES_ACTUAL BIT NOT NULL DEFAULT 1,
        MNV_FECHA_REGISTRO DATETIME NOT NULL DEFAULT GETDATE(),
        CONSTRAINT FK_MNV_MANUAL FOREIGN KEY (MNV_MNL_ID) REFERENCES MANUALES(MNL_ID)
    );
END
GO

-- =========================================================================
-- SP: Obtener manuales por módulo o generales
-- =========================================================================
CREATE OR ALTER PROCEDURE dbo.spMnlObtenerManuales
    @MdlId INT = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SELECT 
        m.MNL_ID AS Id,
        m.MNL_NOMBRE AS Nombre,
        m.MNL_DESCRIPCION AS Descripcion,
        m.MNL_MDL_ID AS ModuloId,
        mod.NOMBRE AS ModuloNombre,
        v.MNV_ID AS VersionId,
        v.MNV_VERSION AS Version,
        v.MNV_UUID_ARCHIVO AS ArchivoUuid,
        v.MNV_NOMBRE_ARCHIVO AS NombreArchivo,
        v.MNV_TIPO AS Tipo,
        v.MNV_URL AS Url
    FROM MANUALES m WITH(NOLOCK)
    JOIN MODULOS mod WITH(NOLOCK) ON m.MNL_MDL_ID = mod.MDL_ID
    LEFT JOIN MANUALES_VERSIONES v WITH(NOLOCK) ON m.MNL_ID = v.MNV_MNL_ID AND v.MNV_ES_ACTUAL = 1
    WHERE m.MNL_ACTIVO = 1
      AND (@MdlId IS NULL OR m.MNL_MDL_ID = @MdlId);
END;
GO

-- =========================================================================
-- SP: Guardar manual (Cabezal)
-- =========================================================================
CREATE OR ALTER PROCEDURE dbo.spMnlGuardarManual
    @Id INT,
    @Nombre VARCHAR(255),
    @Descripcion VARCHAR(500),
    @ModuloId INT,
    @Usuario VARCHAR(50)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRY
        BEGIN TRANSACTION;
            IF @Id = 0
            BEGIN
                INSERT INTO MANUALES (MNL_NOMBRE, MNL_DESCRIPCION, MNL_MDL_ID)
                VALUES (@Nombre, @Descripcion, @ModuloId);
                SELECT SCOPE_IDENTITY() AS Id;
            END
            ELSE
            BEGIN
                UPDATE MANUALES SET 
                    MNL_NOMBRE = @Nombre,
                    MNL_DESCRIPCION = @Descripcion,
                    MNL_MDL_ID = @ModuloId,
                    MNL_FECHA_MODIFICACION = GETDATE()
                WHERE MNL_ID = @Id;
                SELECT @Id AS Id;
            END
        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- =========================================================================
-- SP: Publicar nueva versión
-- =========================================================================
CREATE OR ALTER PROCEDURE dbo.spMnlPublicarVersion
    @ManualId INT,
    @Version INT,
    @Cambios VARCHAR(500),
    @Uuid UNIQUEIDENTIFIER,
    @NombreArchivo VARCHAR(255),
    @Tipo VARCHAR(50),
    @Url VARCHAR(500)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRY
        BEGIN TRANSACTION;
            -- Desactivar versión anterior
            UPDATE MANUALES_VERSIONES SET MNV_ES_ACTUAL = 0 
            WHERE MNV_MNL_ID = @ManualId AND MNV_ES_ACTUAL = 1;

            -- Insertar nueva versión
            INSERT INTO MANUALES_VERSIONES (
                MNV_MNL_ID, MNV_VERSION, MNV_DESCRIPCION_CAMBIO, 
                MNV_UUID_ARCHIVO, MNV_NOMBRE_ARCHIVO, MNV_TIPO, MNV_URL, MNV_ES_ACTUAL
            )
            VALUES (
                @ManualId, @Version, @Cambios, 
                @Uuid, @NombreArchivo, @Tipo, @Url, 1
            );

            SELECT SCOPE_IDENTITY() AS Id;
        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- =========================================================================
-- SP: Eliminar Manual (Lógico) y Retornar UUIDs para Limpieza S3
-- =========================================================================
CREATE OR ALTER PROCEDURE dbo.spMnlEliminarManual
    @Id INT
AS
BEGIN
    SET NOCOUNT ON;
    -- Retornamos los UUIDs antes de desactivar el manual para que el Service los limpie
    SELECT MNV_UUID_ARCHIVO 
    FROM MANUALES_VERSIONES WITH(NOLOCK) 
    WHERE MNV_MNL_ID = @Id;

    UPDATE MANUALES SET MNL_ACTIVO = 0 WHERE MNL_ID = @Id;
END;
GO

-- =========================================================================
-- SP: Obtener versiones de un manual
-- =========================================================================
CREATE OR ALTER PROCEDURE dbo.spMnlObtenerVersiones
    @ManualId INT
AS
BEGIN
    SET NOCOUNT ON;
    SELECT 
        MNV_ID AS Id,
        MNV_VERSION AS Version,
        MNV_DESCRIPCION_CAMBIO AS Cambios,
        MNV_UUID_ARCHIVO AS ArchivoUuid,
        MNV_NOMBRE_ARCHIVO AS NombreArchivo,
        MNV_TIPO AS Tipo,
        MNV_URL AS Url,
        MNV_ES_ACTUAL AS EsActual,
        MNV_FECHA_REGISTRO AS Fecha
    FROM MANUALES_VERSIONES WITH(NOLOCK)
    WHERE MNV_MNL_ID = @ManualId
    ORDER BY MNV_VERSION DESC;
END;
GO
