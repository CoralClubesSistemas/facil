------------------------------------------------------------------------------------------------------------------------
-- ============================================================
-- Check-in Anticipado / Check-out Posterior con Cargo
-- Módulo: Reservaciones - Recepción
-- Tipos de movimiento existentes: 1900 (EARLY CHECK-IN), 1901 (LATE CHECK-OUT)
-- ============================================================

-- 1. TABLA DE CONFIGURACIÓN POR DESARROLLO
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'RESERVACIONES_PARAMETROS_CHECKINOUT')
    BEGIN
        CREATE TABLE dbo.RESERVACIONES_PARAMETROS_CHECKINOUT (
                                                                 RPCI_ID                             INT IDENTITY(1,1) NOT NULL,
                                                                 RPCI_LSV_DESARROLLO                 INT NOT NULL,
                                                                 RPCI_MINUTOS_MAX_CHECKIN_ANTICIPADO INT NOT NULL,
                                                                 RPCI_MINUTOS_MAX_CHECKOUT_POSTERIOR    INT NOT NULL,
                                                                 RPCI_TMV_ID_CARGO_CHECKIN           INT NOT NULL CONSTRAINT DF_RPCI_TMV_CHECKIN DEFAULT (1900),
                                                                 RPCI_TMV_ID_CARGO_CHECKOUT          INT NOT NULL CONSTRAINT DF_RPCI_TMV_CHECKOUT DEFAULT (1901),
                                                                 RPCI_ACTIVO                         BIT NOT NULL CONSTRAINT DF_RPCI_ACTIVO DEFAULT (1),
                                                                 RPCI_USR_USUARIO                    VARCHAR(50) NOT NULL,
                                                                 RPCI_FECHA_REGISTRO                 DATETIME NOT NULL CONSTRAINT DF_RPCI_FECHA_REGISTRO DEFAULT (GETDATE()),
                                                                 CONSTRAINT PK_RESERVACIONES_PARAMETROS_CHECKINOUT PRIMARY KEY (RPCI_ID),
                                                                 CONSTRAINT UQ_RPCI_DESARROLLO UNIQUE (RPCI_LSV_DESARROLLO)
        );

        INSERT INTO dbo.RESERVACIONES_PARAMETROS_CHECKINOUT (RPCI_LSV_DESARROLLO, RPCI_MINUTOS_MAX_CHECKIN_ANTICIPADO,
                                                             RPCI_MINUTOS_MAX_CHECKOUT_POSTERIOR, RPCI_TMV_ID_CARGO_CHECKIN,
                                                             RPCI_TMV_ID_CARGO_CHECKOUT, RPCI_ACTIVO, RPCI_USR_USUARIO)
        VALUES (1, 60, 60, 1900, 1901, 1, 'admin'),
               (2, 60, 60, 1900, 1901, 1, 'admin'),
               (3, 60, 60, 1900, 1901, 1, 'admin'),
               (4, 60, 60, 1900, 1901, 1, 'admin'),
               (5, 60, 60, 1900, 1901, 1, 'admin'),
               (6, 60, 60, 1900, 1901, 1, 'admin');
    END
GO
------------------------------------------------------------------------------------------------------------------------
CREATE OR ALTER PROCEDURE dbo.spResvRegistrarMovimientoCheckInOutEspecial
    @Membresia      VARCHAR(15),
    @Consecutivo    INT,
    @TipoOperacion  VARCHAR(10),    -- 'CHECKIN' o 'CHECKOUT'
    @Usuario        VARCHAR(50)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    -- Variables de trabajo
    DECLARE @IdDesarrollo               INT,
        @FechaEntrada               DATETIME,
        @FechaSalida                DATETIME,
        @RunId                      INT,
        @MinutosMaxCheckin          INT,
        @MinutosMaxCheckout         INT,
        @TmvIdCargoCheckin          INT,
        @TmvIdCargoCheckout         INT,
        @TipoMovimientoACargo       INT,
        @EstatusReservacion         INT,
        @MinutosDiferencia          INT;

    -- Constantes de estatus (LISTAS_VALORES)
    DECLARE @EstatusConfirmada INT = (SELECT TOP 1 LSV_ID FROM dbo.LISTAS_VALORES WITH(NOLOCK) WHERE LSV_TABLA = 'ESTATUSRESERVACIONES' AND LSV_CLAVE = 'CONFIRMADA');
    DECLARE @EstatusEnCasa     INT = (SELECT TOP 1 LSV_ID FROM dbo.LISTAS_VALORES WITH(NOLOCK) WHERE LSV_TABLA = 'ESTATUSRESERVACIONES' AND LSV_CLAVE = 'CHECK-IN');
    DECLARE @EstatusLibre      INT = (SELECT TOP 1 LSV_ID FROM dbo.LISTAS_VALORES WITH(NOLOCK) WHERE LSV_TABLA = 'ESTATUSUNIDADES' AND LSV_CLAVE = '1');

    BEGIN TRY
        -- ==========================================================
        -- 1. Obtener datos de la reservación
        -- ==========================================================
        SELECT
            @IdDesarrollo       = RHD.RHDT_LSV_DESARROLLOS,
            @FechaEntrada       = RSV_FECHA_ENTRADA,
            @FechaSalida        = RSV_FECHA_SALIDA,
            @EstatusReservacion = RSV_LSV_ESTATUSRESERVACION
        FROM dbo.RESERVACIONES_CLIENTES RC WITH(NOLOCK)
                 INNER JOIN RESERVACIONES_HABITACIONES_DETALLES RHD WITH(NOLOCK) ON
            RC.RSV_RHDT_ID = RHD.RHDT_ID
        WHERE RSV_MEM_MEMBRESIA = @Membresia
          AND RSV_CONSECUTIVO   = @Consecutivo;

        IF @FechaEntrada IS NULL
            THROW 50001, 'No se encontró la reservación especificada.', 1;

        IF @EstatusReservacion NOT IN (@EstatusConfirmada, @EstatusEnCasa)
            THROW 50002, 'La reservación no tiene un estatus válido para esta operación.', 1;

        -- ==========================================================
        -- 2. Obtener RUN_ID de la unidad asignada
        -- ==========================================================
        SELECT TOP 1 @RunId = RUES_RUN_ID
        FROM dbo.RESERVACIONES_UNIDADES_ESTATUS WITH(NOLOCK)
        WHERE RUES_RSV_MEM_MEMBRESIA = @Membresia
          AND RUES_RSV_CONSECUTIVO   = @Consecutivo;

        -- ==========================================================
        -- 3. Cargar configuración del desarrollo (Nuevas columnas de MINUTOS)
        -- ==========================================================
        SELECT
            @MinutosMaxCheckin  = RPCI_MINUTOS_MAX_CHECKIN_ANTICIPADO,
            @MinutosMaxCheckout = RPCI_MINUTOS_MAX_CHECKOUT_POSTERIOR,
            @TmvIdCargoCheckin  = RPCI_TMV_ID_CARGO_CHECKIN,
            @TmvIdCargoCheckout = RPCI_TMV_ID_CARGO_CHECKOUT
        FROM dbo.RESERVACIONES_PARAMETROS_CHECKINOUT WITH(NOLOCK)
        WHERE RPCI_LSV_DESARROLLO = @IdDesarrollo
          AND RPCI_ACTIVO = 1;

        IF @MinutosMaxCheckin IS NULL
            THROW 50003, 'No existe configuración de Check-in/Check-out especial para este desarrollo.', 1;

        -- ==========================================================
        -- 4. Validaciones específicas por tipo de operación
        -- ==========================================================
        IF @TipoOperacion = 'CHECKIN'
            BEGIN
                SET @TipoMovimientoACargo = @TmvIdCargoCheckin;
                SET @MinutosDiferencia = DATEDIFF(MINUTE, GETDATE(), @FechaEntrada);

                -- No puede hacer check-in anticipado si ya pasó la hora de entrada
                IF GETDATE() >= @FechaEntrada
                    THROW 50004, 'La hora actual es posterior o igual a la fecha de entrada. Use el Check-In estándar.', 1;

                -- REGLA DE NEGOCIO: Solo se cobra si EXCEDE la tolerancia de minutos
                IF @MinutosDiferencia <= @MinutosMaxCheckin
                    THROW 50005, 'El socio está dentro de la tolerancia gratuita de minutos. No aplica cargo especial, use el Check-In estándar.', 1;

                -- Verificar que la unidad esté libre
                IF @RunId IS NOT NULL
                    BEGIN
                        DECLARE @EstatusUnidadCI INT;
                        SELECT @EstatusUnidadCI = RUN_LSV_ESTATUS
                        FROM dbo.RESERVACIONES_UNIDADES WITH(NOLOCK)
                        WHERE RUN_ID = @RunId;

                        IF @EstatusUnidadCI <> @EstatusLibre
                            THROW 50006, 'La unidad asignada no se encuentra libre para realizar el Check-in anticipado.', 1;
                    END
            END
        ELSE IF @TipoOperacion = 'CHECKOUT'
            BEGIN
                SET @TipoMovimientoACargo = @TmvIdCargoCheckout;
                SET @MinutosDiferencia = DATEDIFF(MINUTE, @FechaSalida, GETDATE());

                -- No puede hacer check-out posterior si aún no ha llegado la hora de salida
                IF GETDATE() <= @FechaSalida
                    THROW 50007, 'La hora actual es anterior o igual a la fecha de salida. Use el Check-Out estándar.', 1;

                -- REGLA DE NEGOCIO: Solo se cobra si EXCEDE la tolerancia de minutos
                IF @MinutosDiferencia <= @MinutosMaxCheckout
                    THROW 50008, 'El socio está dentro de la tolerancia gratuita de minutos. No aplica cargo por Check-out tardío.', 1;
            END
        ELSE
            BEGIN
                THROW 50009, 'Tipo de operación no válido. Use CHECKIN o CHECKOUT.', 1;
            END

        -- ==========================================================
        -- 5. Verificar que no exista un cargo previo por el mismo concepto
        -- ==========================================================
        DECLARE @CargoExistente INT = 0;

        SELECT @CargoExistente = COUNT(1)
        FROM dbo.MOVIMIENTOS_CARGOS_HABITACIONES MCH WITH(NOLOCK)
        INNER JOIN dbo.MOVIMIENTOS_CLIENTES MC WITH(NOLOCK)
            ON MCH.MCH_MVT_ID = MC.MVT_ID
            AND MCH.MCH_RSV_MEM_MEMBRESIA = MC.MVT_MEM_MEMBRESIA
        WHERE MCH.MCH_RSV_MEM_MEMBRESIA = @Membresia
          AND MCH.MCH_RSV_CONSECUTIVO   = @Consecutivo
          AND MC.MVT_TMV_TIPOMOVIMIENTO = @TipoMovimientoACargo
          AND MC.MVT_IMPORTEPENDIENTE > 0;

        IF @CargoExistente > 0
            THROW 50010, 'Ya existe un cargo previo por este concepto para esta reservación.', 1;

        -- ==========================================================
        -- 6. Generar el cargo usando SP existente del ecosistema
        -- ==========================================================
        DECLARE @Concepto VARCHAR(100);
        SET @Concepto = CASE
                            WHEN @TipoOperacion = 'CHECKIN' THEN 'EARLY CHECK-IN (' + CAST(@MinutosDiferencia AS VARCHAR) + ' mins antes)'
                            ELSE 'LATE CHECK-OUT (' + CAST(@MinutosDiferencia AS VARCHAR) + ' mins después)'
            END;

        BEGIN TRANSACTION;

        EXEC dbo.spResvGenerarCargoHabitacion
             @Membresia      = @Membresia,
             @Consecutivo    = @Consecutivo,
             @TipoMovimiento = @TipoMovimientoACargo,
             @Usuario        = @Usuario,
             @Importe        = NULL,         -- El SP buscará la cuota en MOVIMIENTOS_CUOTAS
             @Referencia     = @Concepto,
             @Observaciones  = @Concepto;

        COMMIT TRANSACTION;

    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO
-------------------------------------------------------------------------------------------------------------------------
-- 3. STORED PROCEDURE: spResvCotizarCheckInOutEspecial
--    Devuelve un resultset con la cotización (si aplica cargo y cuánto costaría)
-- ============================================================
-- Exec spResvCotizarCheckInOutEspecial @Membresia = '0-166-2025', @Consecutivo = 3;
CREATE OR ALTER PROCEDURE dbo.spResvCotizarCheckInOutEspecial
    @Membresia      VARCHAR(15),
    @Consecutivo    INT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    DECLARE @IdDesarrollo       INT,
        @FechaEntrada       DATETIME,
        @FechaSalida        DATETIME,
        @TpmId              INT,
        @MinutosMaxCheckin  INT,
        @MinutosMaxCheckout INT,
        @TmvIdCargoCheckin  INT,
        @TmvIdCargoCheckout INT,
        @MinutosAntesCheckin  INT = 0,
        @MinutosDespuesCheckout INT = 0,
        @CargoCheckin       MONEY = 0,
        @CargoCheckout      MONEY = 0,
        @FechaActual        DATETIME = GETDATE();

    BEGIN TRY
        -- ==========================================================
        -- 1. Obtener datos de la reservación y desarrollo
        -- ==========================================================
        SELECT
            @IdDesarrollo   = RHD.RHDT_LSV_DESARROLLOS,
            @FechaEntrada   = RC.RSV_FECHA_ENTRADA,
            @FechaSalida    = RC.RSV_FECHA_SALIDA
        FROM dbo.RESERVACIONES_CLIENTES RC WITH(NOLOCK)
                 INNER JOIN dbo.RESERVACIONES_HABITACIONES_DETALLES RHD WITH(NOLOCK)
                            ON RC.RSV_RHDT_ID = RHD.RHDT_ID
        WHERE RC.RSV_MEM_MEMBRESIA = @Membresia
          AND RC.RSV_CONSECUTIVO   = @Consecutivo;

        IF @FechaEntrada IS NULL
            BEGIN
                SELECT CAST(0 AS BIT) AS AplicaCheckinAnticipado, 0 AS MinutosAntesCheckin, CAST(0 AS MONEY) AS CargoCheckin,
                       CAST(0 AS BIT) AS AplicaCheckoutPosterior, 0 AS MinutosDespuesCheckout, CAST(0 AS MONEY) AS CargoCheckout,
                       NULL AS FechaEntrada, NULL AS FechaSalida, 0 AS MinutosMaxCheckin, 0 AS MinutosMaxCheckout;
                RETURN;
            END

        -- ==========================================================
        -- 2. Obtener tipo de membresía del socio
        -- ==========================================================
        SELECT TOP 1 @TpmId = MEM_TPM_ID
        FROM dbo.MEMBRESIAS WITH(NOLOCK)
        WHERE MEM_MEMBRESIA = @Membresia;

        -- ==========================================================
        -- 3. Cargar configuración dinámica (AHORA EN MINUTOS)
        -- ==========================================================
        SELECT
            @MinutosMaxCheckin  = ISNULL(RPCI_MINUTOS_MAX_CHECKIN_ANTICIPADO, 0),
            @MinutosMaxCheckout = ISNULL(RPCI_MINUTOS_MAX_CHECKOUT_POSTERIOR, 0),
            @TmvIdCargoCheckin  = RPCI_TMV_ID_CARGO_CHECKIN,
            @TmvIdCargoCheckout = RPCI_TMV_ID_CARGO_CHECKOUT
        FROM dbo.RESERVACIONES_PARAMETROS_CHECKINOUT WITH(NOLOCK)
        WHERE RPCI_LSV_DESARROLLO = @IdDesarrollo
          AND RPCI_ACTIVO = 1;

        -- ==========================================================
        -- 4. Evaluar si ya existe un cargo previo por check-in/checkout especial
        -- ==========================================================
        DECLARE @YaCargoCheckin  BIT = 0,
                @YaCargoCheckout BIT = 0;

        SELECT
            @YaCargoCheckin  = CASE WHEN EXISTS (
                SELECT 1 FROM dbo.MOVIMIENTOS_CARGOS_HABITACIONES MCH WITH(NOLOCK)
                INNER JOIN dbo.MOVIMIENTOS_CLIENTES MC WITH(NOLOCK)
                    ON MCH.MCH_MVT_ID = MC.MVT_ID
                    AND MCH.MCH_RSV_MEM_MEMBRESIA = MC.MVT_MEM_MEMBRESIA
                WHERE MCH.MCH_RSV_MEM_MEMBRESIA = @Membresia
                  AND MCH.MCH_RSV_CONSECUTIVO   = @Consecutivo
                  AND MC.MVT_TMV_TIPOMOVIMIENTO = @TmvIdCargoCheckin
                  AND MC.MVT_IMPORTEPENDIENTE > 0
            ) THEN 1 ELSE 0 END,
            @YaCargoCheckout = CASE WHEN EXISTS (
                SELECT 1 FROM dbo.MOVIMIENTOS_CARGOS_HABITACIONES MCH WITH(NOLOCK)
                INNER JOIN dbo.MOVIMIENTOS_CLIENTES MC WITH(NOLOCK)
                    ON MCH.MCH_MVT_ID = MC.MVT_ID
                    AND MCH.MCH_RSV_MEM_MEMBRESIA = MC.MVT_MEM_MEMBRESIA
                WHERE MCH.MCH_RSV_MEM_MEMBRESIA = @Membresia
                  AND MCH.MCH_RSV_CONSECUTIVO   = @Consecutivo
                  AND MC.MVT_TMV_TIPOMOVIMIENTO = @TmvIdCargoCheckout
                  AND MC.MVT_IMPORTEPENDIENTE > 0
            ) THEN 1 ELSE 0 END;

        -- ==========================================================
        -- 5. Evaluar Check-in Anticipado
        -- ==========================================================
        -- ¿Cuántos minutos faltan para su hora oficial de entrada?
        SET @MinutosAntesCheckin = DATEDIFF(MINUTE, @FechaActual, @FechaEntrada);

        -- Si llega MUY ANTES, excede la tolerancia Y no tiene cargo previo
        IF @MinutosAntesCheckin > @MinutosMaxCheckin AND @YaCargoCheckin = 0
            BEGIN
                SELECT TOP 1 @CargoCheckin = ISNULL(MCU_CUOTA, 0)
                FROM dbo.MOVIMIENTOS_CUOTAS WITH(NOLOCK)
                WHERE MCU_TMV_TIPOMOVIMIENTO = @TmvIdCargoCheckin
                  AND MCU_TPM_ID = ISNULL(@TpmId, MCU_TPM_ID)
                  AND MCU_CUOTA > 0
                ORDER BY MCU_TPM_ID;
            END

        -- ==========================================================
        -- 5. Evaluar Check-out Posterior
        -- ==========================================================
        -- ¿Cuántos minutos han pasado desde su hora oficial de salida?
        SET @MinutosDespuesCheckout = DATEDIFF(MINUTE, @FechaSalida, @FechaActual);

        -- Si sale MUY TARDE, excede la tolerancia Y no tiene cargo previo
        IF @MinutosDespuesCheckout > @MinutosMaxCheckout AND @YaCargoCheckout = 0
            BEGIN
                SELECT TOP 1 @CargoCheckout = ISNULL(MCU_CUOTA, 0)
                FROM dbo.MOVIMIENTOS_CUOTAS WITH(NOLOCK)
                WHERE MCU_TMV_TIPOMOVIMIENTO = @TmvIdCargoCheckout
                  AND MCU_TPM_ID = ISNULL(@TpmId, MCU_TPM_ID)
                  AND MCU_CUOTA > 0
                ORDER BY MCU_TPM_ID;
            END

        -- ==========================================================
        -- 8. Devolver resultado (Nombres de columnas ajustados a minutos)
        -- ==========================================================
        SELECT
            IIF(@MinutosAntesCheckin > @MinutosMaxCheckin AND @YaCargoCheckin = 0, CAST(1 AS BIT), CAST(0 AS BIT)) AS AplicaCheckinAnticipado,
            IIF(@MinutosAntesCheckin > 0, @MinutosAntesCheckin, 0)                         AS MinutosAntesCheckin,
            @CargoCheckin                                                                  AS CargoCheckin,
            @YaCargoCheckin                                                                AS YaTieneCargoCheckin,

            IIF(@MinutosDespuesCheckout > @MinutosMaxCheckout AND @YaCargoCheckout = 0, CAST(1 AS BIT), CAST(0 AS BIT)) AS AplicaCheckoutPosterior,
            IIF(@MinutosDespuesCheckout > 0, @MinutosDespuesCheckout, 0)                       AS MinutosDespuesCheckout,
            @CargoCheckout                                                                     AS CargoCheckout,
            @YaCargoCheckout                                                                   AS YaTieneCargoCheckout,

            @FechaEntrada        AS FechaEntrada,
            @FechaSalida         AS FechaSalida,
            @MinutosMaxCheckin   AS MinutosMaxCheckin,
            @MinutosMaxCheckout  AS MinutosMaxCheckout;

    END TRY
    BEGIN CATCH
        THROW;
    END CATCH
END;
GO