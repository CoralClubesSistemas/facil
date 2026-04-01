-- ============================================================
-- TABLA DE MAPEO DE PARÁMETROS POR SP DE REPORTE
-- Normaliza la relación entre posición, nombre Java y nombre real en el SP
--
-- REGLA CRÍTICA: RPM_NOMBRE_JAVA debe coincidir EXACTAMENTE con
-- LSV_DESCRIPCION del catálogo que el frontend envía como key.
--
-- El frontend obtiene los nombres desde spRepoObtenerParametrosReporte
-- que devuelve: L2.LSV_DESCRIPCION AS NombreFiltroUI
-- El frontend usa ese NombreFiltroUI como key del Map de parámetros.
-- ============================================================

-- 1. CREAR TABLA
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'REPORTE_PARAMETROS_MAPEO')
BEGIN
    CREATE TABLE dbo.REPORTE_PARAMETROS_MAPEO (
        RPM_ID              INT IDENTITY(1,1) NOT NULL,
        RPM_PDBRPT_ID       INT NOT NULL,           -- FK a PROCEDIMIENTOS_DB_REPORTES
        RPM_POSICION        INT NOT NULL,            -- Posición en el SP (1,2,3...)
        RPM_ROL             VARCHAR(30) NOT NULL,    -- 'FECHA_INICIO', 'FECHA_FIN', 'USUARIO', 'CATALOGO'
        RPM_NOMBRE_JAVA     VARCHAR(50) NOT NULL,    -- Nombre que usa el frontend como key del Map (debe ser LSV_DESCRIPCION)
        RPM_NOMBRE_SP       VARCHAR(100) NOT NULL,   -- Nombre real del parámetro en el SP (con @)
        RPM_TIPO_DATO       VARCHAR(30) NOT NULL,    -- 'datetime', 'varchar', 'int'
        RPM_LONGITUD        INT NULL,                -- Longitud del varchar (NULL si no aplica)
        RPM_ES_OBLIGATORIO  BIT NOT NULL CONSTRAINT DF_RPM_OBLIG DEFAULT (1),
        RPM_FECHA_REGISTRO  DATETIME NOT NULL CONSTRAINT DF_RPM_FECHA DEFAULT (GETDATE()),
        CONSTRAINT PK_REPORTE_PARAMETROS_MAPEO PRIMARY KEY (RPM_ID),
        CONSTRAINT UQ_RPM_POSICION UNIQUE (RPM_PDBRPT_ID, RPM_POSICION),
        CONSTRAINT FK_RPM_PDBRPT FOREIGN KEY (RPM_PDBRPT_ID)
            REFERENCES dbo.PROCEDIMIENTOS_DB_REPORTES(PDBRPT_ID)
    );
    PRINT 'Tabla REPORTE_PARAMETROS_MAPEO creada.';
END
GO

-- Limpiar datos previos si existen
DELETE FROM dbo.REPORTE_PARAMETROS_MAPEO;
GO


-- ============================================================
-- SP 1 (PDBRPT_ID=1): spObtenerMovimientosPagadosMasParametrosExcelTest
-- Parámetros reales: @FechaInicialPago(datetime), @FechaFinalPago(datetime), @Desarrollo(varchar), @TipoMovimiento(varchar), @Usuario_Generador(varchar)
-- Frontend envía:    { DESARROLLOS: 1, TIPOSMOVIMIENTOS: 4 }
-- ============================================================
INSERT INTO dbo.REPORTE_PARAMETROS_MAPEO (RPM_PDBRPT_ID, RPM_POSICION, RPM_ROL, RPM_NOMBRE_JAVA, RPM_NOMBRE_SP, RPM_TIPO_DATO, RPM_LONGITUD, RPM_ES_OBLIGATORIO) VALUES
(1, 1, 'FECHA_INICIO',   'FechaInicio',        '@FechaInicialPago',   'datetime', NULL,  1),
(1, 2, 'FECHA_FIN',      'FechaFin',           '@FechaFinalPago',     'datetime', NULL,  1),
(1, 3, 'CATALOGO',       'DESARROLLOS',        '@Desarrollo',         'varchar',  25,    1),
(1, 4, 'CATALOGO',       'TIPOSMOVIMIENTOS',   '@TipoMovimiento',     'varchar',  -1,    1),
(1, 5, 'USUARIO',        'UsuarioGenerador',   '@Usuario_Generador',  'varchar',  15,    1);


-- ============================================================
-- SP 2 (PDBRPT_ID=2): spObtenerMovimientosSaldosPendientesMasParametrosExcelTest
-- Parámetros reales: @FechaInicial(datetime), @FechaFinal(datetime), @IdDesarrollos(varchar), @IdCarterasCobranza(varchar), @IdUsuarioGenerador(varchar)
-- Frontend envía:    { DESARROLLOS: 1, CARTERAS: 3 }
-- ============================================================
INSERT INTO dbo.REPORTE_PARAMETROS_MAPEO (RPM_PDBRPT_ID, RPM_POSICION, RPM_ROL, RPM_NOMBRE_JAVA, RPM_NOMBRE_SP, RPM_TIPO_DATO, RPM_LONGITUD, RPM_ES_OBLIGATORIO) VALUES
(2, 1, 'FECHA_INICIO',   'FechaInicio',        '@FechaInicial',       'datetime', NULL,  1),
(2, 2, 'FECHA_FIN',      'FechaFin',           '@FechaFinal',         'datetime', NULL,  1),
(2, 3, 'CATALOGO',       'DESARROLLOS',        '@IdDesarrollos',      'varchar',  100,   1),
(2, 4, 'CATALOGO',       'CARTERAS',           '@IdCarterasCobranza', 'varchar',  -1,    1),
(2, 5, 'USUARIO',        'UsuarioGenerador',   '@IdUsuarioGenerador', 'varchar',  15,    1);


-- ============================================================
-- SP 3 (PDBRPT_ID=3): spObtenerMovimientosPagadosPendientesMasParametrosExcelTest
-- Parámetros reales: @FechaInicialPago(datetime), @FechaFinalPago(datetime), @Desarrollo(varchar), @TipoMovimiento(varchar), @Usuario_Generador(varchar)
-- Frontend envía:    { DESARROLLOS: 1, TIPOSMOVIMIENTOS: 4 }
-- ============================================================
INSERT INTO dbo.REPORTE_PARAMETROS_MAPEO (RPM_PDBRPT_ID, RPM_POSICION, RPM_ROL, RPM_NOMBRE_JAVA, RPM_NOMBRE_SP, RPM_TIPO_DATO, RPM_LONGITUD, RPM_ES_OBLIGATORIO) VALUES
(3, 1, 'FECHA_INICIO',   'FechaInicio',        '@FechaInicialPago',   'datetime', NULL,  1),
(3, 2, 'FECHA_FIN',      'FechaFin',           '@FechaFinalPago',     'datetime', NULL,  1),
(3, 3, 'CATALOGO',       'DESARROLLOS',        '@Desarrollo',         'varchar',  25,    1),
(3, 4, 'CATALOGO',       'TIPOSMOVIMIENTOS',   '@TipoMovimiento',     'varchar',  -1,    1),
(3, 5, 'USUARIO',        'UsuarioGenerador',   '@Usuario_Generador',  'varchar',  15,    1);


-- ============================================================
-- SP 4 (PDBRPT_ID=4): spReporteConsecutivoDeRecibos
-- Parámetros reales: @FechaGeneracionDe(datetime), @FechaGeneracionA(datetime), @IdCveDesarrollo(varchar), @IdUsuarioGenerador(varchar)
-- Frontend envía:    { DESARROLLOS: 1 }
-- ============================================================
INSERT INTO dbo.REPORTE_PARAMETROS_MAPEO (RPM_PDBRPT_ID, RPM_POSICION, RPM_ROL, RPM_NOMBRE_JAVA, RPM_NOMBRE_SP, RPM_TIPO_DATO, RPM_LONGITUD, RPM_ES_OBLIGATORIO) VALUES
(4, 1, 'FECHA_INICIO',   'FechaInicio',        '@FechaGeneracionDe',  'datetime', NULL,  1),
(4, 2, 'FECHA_FIN',      'FechaFin',           '@FechaGeneracionA',   'datetime', NULL,  1),
(4, 3, 'CATALOGO',       'DESARROLLOS',        '@IdCveDesarrollo',    'varchar',  100,   1),
(4, 4, 'USUARIO',        'UsuarioGenerador',   '@IdUsuarioGenerador', 'varchar',  15,    1);


-- ============================================================
-- SP 5 (PDBRPT_ID=5): spReporteCorteCajaCobranzaExcel
-- Parámetros reales: @FechaGeneracionDe(datetime), @FechaGeneracionA(datetime), @IdCveDesarrollo(varchar), @IdSeriesRecibos(varchar), @IdUsuario(varchar), @IdUsuarioGenerador(varchar)
-- Frontend envía:    { DESARROLLOS: 1, SERIESRECIBOS: 2, USUARIOS: 'admin' }
-- ============================================================
INSERT INTO dbo.REPORTE_PARAMETROS_MAPEO (RPM_PDBRPT_ID, RPM_POSICION, RPM_ROL, RPM_NOMBRE_JAVA, RPM_NOMBRE_SP, RPM_TIPO_DATO, RPM_LONGITUD, RPM_ES_OBLIGATORIO) VALUES
(5, 1, 'FECHA_INICIO',   'FechaInicio',        '@FechaGeneracionDe',  'datetime', NULL,  1),
(5, 2, 'FECHA_FIN',      'FechaFin',           '@FechaGeneracionA',   'datetime', NULL,  1),
(5, 3, 'CATALOGO',       'DESARROLLOS',        '@IdCveDesarrollo',    'varchar',  100,   1),
(5, 4, 'CATALOGO',       'SERIESRECIBOS',      '@IdSeriesRecibos',    'varchar',  -1,    1),
(5, 5, 'CATALOGO',       'USUARIOS',           '@IdUsuario',          'varchar',  -1,    0),
(5, 6, 'USUARIO',        'UsuarioGenerador',   '@IdUsuarioGenerador', 'varchar',  -1,    1);


-- ============================================================
-- SP 6 (PDBRPT_ID=6): spObtieneAuditoriaMovimientos
-- Parámetros reales: @FechaInicialPago(datetime), @FechaFinalPago(datetime), @Desarrollo(varchar), @TipoMovimiento(varchar), @Usuario_Generador(varchar)
-- Frontend envía:    { DESARROLLOS: 1, TIPOSMOVIMIENTOS: 4 }
-- ============================================================
INSERT INTO dbo.REPORTE_PARAMETROS_MAPEO (RPM_PDBRPT_ID, RPM_POSICION, RPM_ROL, RPM_NOMBRE_JAVA, RPM_NOMBRE_SP, RPM_TIPO_DATO, RPM_LONGITUD, RPM_ES_OBLIGATORIO) VALUES
(6, 1, 'FECHA_INICIO',   'FechaInicio',        '@FechaInicialPago',   'datetime', NULL,  1),
(6, 2, 'FECHA_FIN',      'FechaFin',           '@FechaFinalPago',     'datetime', NULL,  1),
(6, 3, 'CATALOGO',       'DESARROLLOS',        '@Desarrollo',         'varchar',  25,    1),
(6, 4, 'CATALOGO',       'TIPOSMOVIMIENTOS',   '@TipoMovimiento',     'varchar',  -1,    1),
(6, 5, 'USUARIO',        'UsuarioGenerador',   '@Usuario_Generador',  'varchar',  15,    1);


-- ============================================================
-- SP 7 (PDBRPT_ID=7): spReporteVentasCompletasUnidadesNegocioTodosPorPeriodoExcel
-- Parámetros reales: @FechaVentaInicial(varchar), @FechaVentaFinal(varchar), @IDesarrollo(int), @IdUsuarioGenerador(varchar)
-- Frontend envía:    { DESARROLLOS: 2 }
-- ============================================================
INSERT INTO dbo.REPORTE_PARAMETROS_MAPEO (RPM_PDBRPT_ID, RPM_POSICION, RPM_ROL, RPM_NOMBRE_JAVA, RPM_NOMBRE_SP, RPM_TIPO_DATO, RPM_LONGITUD, RPM_ES_OBLIGATORIO) VALUES
(7, 1, 'FECHA_INICIO',   'FechaInicio',        '@FechaVentaInicial',  'varchar',  10,    1),
(7, 2, 'FECHA_FIN',      'FechaFin',           '@FechaVentaFinal',    'varchar',  10,    1),
(7, 3, 'CATALOGO',       'DESARROLLOS',        '@IDesarrollo',        'int',      NULL,  1),
(7, 4, 'USUARIO',        'UsuarioGenerador',   '@IdUsuarioGenerador', 'varchar',  -1,    1);


-- ============================================================
-- SP 8 (PDBRPT_ID=8): spReporteCorteCajaCobranzaExcelResumen
-- Parámetros reales: @FechaGeneracionDe(datetime), @FechaGeneracionA(datetime), @IdCveDesarrollo(varchar), @IdSeriesRecibos(varchar), @IdUsuario(varchar), @IdUsuarioGenerador(varchar)
-- Frontend envía:    { DESARROLLOS: 1, SERIESRECIBOS: 2, USUARIOS: 'admin' }
-- ============================================================
INSERT INTO dbo.REPORTE_PARAMETROS_MAPEO (RPM_PDBRPT_ID, RPM_POSICION, RPM_ROL, RPM_NOMBRE_JAVA, RPM_NOMBRE_SP, RPM_TIPO_DATO, RPM_LONGITUD, RPM_ES_OBLIGATORIO) VALUES
(8, 1, 'FECHA_INICIO',   'FechaInicio',        '@FechaGeneracionDe',  'datetime', NULL,  1),
(8, 2, 'FECHA_FIN',      'FechaFin',           '@FechaGeneracionA',   'datetime', NULL,  1),
(8, 3, 'CATALOGO',       'DESARROLLOS',        '@IdCveDesarrollo',    'varchar',  100,   1),
(8, 4, 'CATALOGO',       'SERIESRECIBOS',      '@IdSeriesRecibos',    'varchar',  -1,    1),
(8, 5, 'CATALOGO',       'USUARIOS',           '@IdUsuario',          'varchar',  -1,    0),
(8, 6, 'USUARIO',        'UsuarioGenerador',   '@IdUsuarioGenerador', 'varchar',  15,    1);


-- ============================================================
-- SP 9 (PDBRPT_ID=9): spReporteVentasCompletasPorPeriodoFormatoExcel
-- Parámetros reales: @FechaVentaInicial(varchar), @FechaVentaFinal(varchar), @IdDesarrollo(varchar), @IdUsuarioGenerador(varchar)
-- Frontend envía:    { DESARROLLOS: 1 }
-- ============================================================
INSERT INTO dbo.REPORTE_PARAMETROS_MAPEO (RPM_PDBRPT_ID, RPM_POSICION, RPM_ROL, RPM_NOMBRE_JAVA, RPM_NOMBRE_SP, RPM_TIPO_DATO, RPM_LONGITUD, RPM_ES_OBLIGATORIO) VALUES
(9, 1, 'FECHA_INICIO',   'FechaInicio',        '@FechaVentaInicial',  'varchar',  10,    1),
(9, 2, 'FECHA_FIN',      'FechaFin',           '@FechaVentaFinal',    'varchar',  10,    1),
(9, 3, 'CATALOGO',       'DESARROLLOS',        '@IdDesarrollo',       'varchar',  50,    1),
(9, 4, 'USUARIO',        'UsuarioGenerador',   '@IdUsuarioGenerador', 'varchar',  15,    1);


-- ============================================================
-- SP 10 (PDBRPT_ID=10): spReporteBuscaAccesosDesarrollosSociosFormatoExcel
-- Parámetros reales: @FechaAccesoInicial(varchar), @FechaAccesoFinal(varchar), @CveDesarrollo(varchar), @IdUsuarioGenerador(varchar)
-- Frontend envía:    { DESARROLLOS: 1 }
-- ============================================================
INSERT INTO dbo.REPORTE_PARAMETROS_MAPEO (RPM_PDBRPT_ID, RPM_POSICION, RPM_ROL, RPM_NOMBRE_JAVA, RPM_NOMBRE_SP, RPM_TIPO_DATO, RPM_LONGITUD, RPM_ES_OBLIGATORIO) VALUES
(10, 1, 'FECHA_INICIO',   'FechaInicio',        '@FechaAccesoInicial', 'varchar',  10,    1),
(10, 2, 'FECHA_FIN',      'FechaFin',           '@FechaAccesoFinal',   'varchar',  10,    1),
(10, 3, 'CATALOGO',       'DESARROLLOS',        '@CveDesarrollo',      'varchar',  100,   1),
(10, 4, 'USUARIO',        'UsuarioGenerador',   '@IdUsuarioGenerador', 'varchar',  15,    1);


-- ============================================================
-- SP 11 (PDBRPT_ID=11): spObtieneMovimientosGeneradosPagBonifMktd
-- Parámetros reales: @param_FechaInicial(datetime), @param_FechaFinal(datetime), @IdCveDesarrollo(varchar), @usr_usuario_genera(varchar)
-- Frontend envía:    { DESARROLLOS: 1 }
-- ============================================================
INSERT INTO dbo.REPORTE_PARAMETROS_MAPEO (RPM_PDBRPT_ID, RPM_POSICION, RPM_ROL, RPM_NOMBRE_JAVA, RPM_NOMBRE_SP, RPM_TIPO_DATO, RPM_LONGITUD, RPM_ES_OBLIGATORIO) VALUES
(11, 1, 'FECHA_INICIO',   'FechaInicio',        '@param_FechaInicial', 'datetime', NULL,  1),
(11, 2, 'FECHA_FIN',      'FechaFin',           '@param_FechaFinal',   'datetime', NULL,  1),
(11, 3, 'CATALOGO',       'DESARROLLOS',        '@IdCveDesarrollo',    'varchar',  100,   1),
(11, 4, 'USUARIO',        'UsuarioGenerador',   '@usr_usuario_genera', 'varchar',  15,    1);


-- ============================================================
-- SP 12 (PDBRPT_ID=12): spObtenerMovimientosSaldosPendientesMasParametrosExcelPQANoPagadoTest
-- Parámetros reales: @FechaInicial(datetime), @FechaFinal(datetime), @IdDesarrollos(varchar), @IdCarterasCobranza(varchar), @IdUsuarioGenerador(varchar)
-- Frontend envía:    { DESARROLLOS: 1, CARTERAS: 3 }
-- ============================================================
INSERT INTO dbo.REPORTE_PARAMETROS_MAPEO (RPM_PDBRPT_ID, RPM_POSICION, RPM_ROL, RPM_NOMBRE_JAVA, RPM_NOMBRE_SP, RPM_TIPO_DATO, RPM_LONGITUD, RPM_ES_OBLIGATORIO) VALUES
(12, 1, 'FECHA_INICIO',   'FechaInicio',        '@FechaInicial',         'datetime', NULL,  1),
(12, 2, 'FECHA_FIN',      'FechaFin',           '@FechaFinal',           'datetime', NULL,  1),
(12, 3, 'CATALOGO',       'DESARROLLOS',        '@IdDesarrollos',        'varchar',  100,   1),
(12, 4, 'CATALOGO',       'CARTERAS',           '@IdCarterasCobranza',   'varchar',  -1,    1),
(12, 5, 'USUARIO',        'UsuarioGenerador',   '@IdUsuarioGenerador',   'varchar',  15,    1);


-- ============================================================
-- SP 13 (PDBRPT_ID=111): spReporteVentasCompletasUnidadesNegocioTodosPorPeriodoExcel
-- Parámetros reales: @FechaVentaInicial(varchar), @FechaVentaFinal(varchar), @IDesarrollo(int), @IdUsuarioGenerador(varchar)
-- Frontend envía:    { DESARROLLOS: 2 }
-- ============================================================
INSERT INTO dbo.REPORTE_PARAMETROS_MAPEO (RPM_PDBRPT_ID, RPM_POSICION, RPM_ROL, RPM_NOMBRE_JAVA, RPM_NOMBRE_SP, RPM_TIPO_DATO, RPM_LONGITUD, RPM_ES_OBLIGATORIO) VALUES
(111, 1, 'FECHA_INICIO',   'FechaInicio',        '@FechaVentaInicial',  'varchar',  10,    1),
(111, 2, 'FECHA_FIN',      'FechaFin',           '@FechaVentaFinal',    'varchar',  10,    1),
(111, 3, 'CATALOGO',       'DESARROLLOS',        '@IDesarrollo',        'int',      NULL,  1),
(111, 4, 'USUARIO',        'UsuarioGenerador',   '@IdUsuarioGenerador', 'varchar',  -1,    1);


-- ============================================================
-- SP AUXILIAR: spReporteObtenerParametrosMapeo
-- Devuelve el mapeo de parámetros para un reporte específico
-- ============================================================
CREATE OR ALTER PROCEDURE dbo.spReporteObtenerParametrosMapeo
    @IdTipoReporte INT
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        M.RPM_POSICION       AS Posicion,
        M.RPM_ROL            AS Rol,
        M.RPM_NOMBRE_JAVA    AS NombreJava,
        M.RPM_NOMBRE_SP      AS NombreSP,
        M.RPM_TIPO_DATO      AS TipoDato,
        M.RPM_LONGITUD       AS Longitud,
        M.RPM_ES_OBLIGATORIO AS EsObligatorio
    FROM dbo.REPORTE_PARAMETROS_MAPEO M WITH(NOLOCK)
    INNER JOIN dbo.PROCEDIMIENTOS_DB_REPORTES P WITH(NOLOCK)
        ON M.RPM_PDBRPT_ID = P.PDBRPT_ID
    WHERE P.PDBRPT_LSV_TIPOSREPORTES = @IdTipoReporte
    ORDER BY M.RPM_POSICION;
END;
GO

PRINT 'Script de mapeo de parámetros ejecutado exitosamente.';
GO
