------------------------------------------------------------------------------------------------------------------------
-- ============================================================
-- SP Modificado: spAmaCrearTareaLimpieza
-- Agrega validación de independencia para evitar tareas duplicadas
-- Verifica si la unidad ya tiene una tarea activa (PENDIENTE, ASIGNADA o EN_PROCESO)
-- Si existe, retorna silenciosamente sin error ni inserción
-- ============================================================

CREATE OR ALTER PROCEDURE dbo.spAmaCrearTareaLimpieza
    @IdUnidadFisica INT,
    @Usuario        VARCHAR(50),
    @OrigenAccion VARCHAR(50) -- "CREACION_INICIAL", "REPROGRAMACION", "REAPERTURA"
AS
BEGIN
    SET NOCOUNT ON;

    -- ==========================================================
    -- 0. Verificar si ya existe una tarea activa (no completada)
    --    para esta unidad. Si existe, salir silenciosamente.
    -- ==========================================================
    DECLARE @EstatusCompletada INT = (SELECT TOP 1 LSV_ID FROM dbo.LISTAS_VALORES WITH(NOLOCK) WHERE LSV_TABLA = 'ESTATUSTAREACAMARISTA' AND LSV_CLAVE = 'COMPLETADA');

    IF EXISTS (
        SELECT 1 FROM dbo.AMA_TAREAS_LIMPIEZA WITH(NOLOCK)
        WHERE ATL_RUN_ID = @IdUnidadFisica
          AND ATL_LSV_ESTATUSTAREA <> @EstatusCompletada
    )
        RETURN;  -- Ya hay tarea activa, no crear duplicado ni lanzar error

    -- Variables para la proxima reservacion
    DECLARE @ProxMembresia VARCHAR(15) = NULL;
    DECLARE @ProxConsecutivo INT = NULL;
    DECLARE @CantidadPersonas INT = 0;
    DECLARE @PeticionEspecial VARCHAR(250) = NULL;

    DECLARE @EstatusCancelada INT = (SELECT TOP 1 LSV_ID FROM dbo.LISTAS_VALORES WITH(NOLOCK) WHERE LSV_TABLA = 'ESTATUSRESERVACION' AND LSV_CLAVE = 'CANCELADA');
    DECLARE @EstatusCheckout INT = (SELECT TOP 1 LSV_ID FROM dbo.LISTAS_VALORES WITH(NOLOCK) WHERE LSV_TABLA = 'ESTATUSRESERVACION' AND LSV_CLAVE = 'CHECK-OUT');

    DECLARE @EstatusPendiente INT = (SELECT TOP 1 LSV_ID FROM dbo.LISTAS_VALORES WITH(NOLOCK) WHERE LSV_TABLA = 'ESTATUSTAREACAMARISTA' AND LSV_CLAVE = 'PENDIENTE');

    -- Buscamos la reservacion mas proxima para este cuarto (Hoy o en el futuro)
    SELECT TOP 1
        @ProxMembresia = RSV_MEM_MEMBRESIA,
        @ProxConsecutivo = RSV_CONSECUTIVO,
        @CantidadPersonas = RSV_NUMERO_SOCIOS + ISNULL(RSV_NUMERO_INVITADOS, 0),
        @PeticionEspecial = RSV_PETICION_ESPECIAL
    FROM dbo.RESERVACIONES_CLIENTES WITH(NOLOCK)
    WHERE RSV_RUN_ID = @IdUnidadFisica
      AND CAST(RSV_FECHA_ENTRADA AS DATE) >= CAST(GETDATE() AS DATE)
      AND RSV_LSV_ESTATUSRESERVACION NOT IN (@EstatusCancelada, @EstatusCheckout)
    ORDER BY RSV_FECHA_ENTRADA;

    -- Insertamos la tarea con estatus (PENDIENTE)
    INSERT INTO dbo.AMA_TAREAS_LIMPIEZA (
        ATL_RUN_ID, ATL_PROX_MEMBRESIA, ATL_PROX_CONSECUTIVO,
        ATL_CANTIDAD_PERSONAS, ATL_PETICION_ESPECIAL,
        ATL_FECHA_CREACION, ATL_LSV_ESTATUSTAREA, ATL_ORIGEN_ACCION, ATL_USR_MASTER
    )
    VALUES (
               @IdUnidadFisica, @ProxMembresia, @ProxConsecutivo,
               @CantidadPersonas, @PeticionEspecial,
               GETDATE(), @EstatusPendiente, @OrigenAccion, @Usuario
           );
END;
GO