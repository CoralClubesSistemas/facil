vamos a empezar a trabajar en el modulo de cobranza del sistema facil

la idea de este modulo es poder gestionar los movimientos de los usuarios (clientes)
ejecutar los cobros correspondientes mediante el flujo: crear orden de seleccionar movimientos a pagar->generar orden de cobranza
->seleccioanr formas de pago->pagar

en su primer estapa esta sera la funcionalidad a realizar, para ello centrate en las siguientes tablas:

-- auto-generated definition
create table dbo.MOVIMIENTOS_CLIENTES
(
MVT_MEM_MEMBRESIA          varchar(15)  not null
constraint FK_MOVIMIENTOS_CLIENTES_CLIENTES
references dbo.CLIENTES
constraint FK_MOVIMIENTOS_CLIENTES_MEMBRESIAS
references dbo.MEMBRESIAS,
MVT_ID                     int          not null,
MVT_ORIGINAL_ID            int          not null,
MVT_PADRE_ID               int          not null,
MVT_FAMILIA_ID             int          not null,
MVT_PLV_NUMEROPLAN         int          not null,
MVT_TMV_TIPOMOVIMIENTO     int          not null,
MVT_PP                     int          not null,
MVT_SEQ                    int          not null,
MVT_FECHAGENERACION        datetime     not null,
MVT_FECHAVENCIMIENTO       datetime     not null,
MVT_IMPORTECARGO           money        not null,
MVT_IMPORTEABONO           money        not null,
MVT_IMPORTEPENDIENTE       money        not null,
MVT_USR_USUARIO            varchar(15)  not null,
MVT_LSV_ESTATUSMOVIMIENTO  int          not null,
MVT_LSV_VARIANTEMOVIMIENTO int          not null,
MVT_LSV_DESARROLLOCONSUMO  int          not null,
MVT_NUMERO_BENEFICIARIOS   int          not null,
MVT_USR_USUARIO_AUTORIZA   varchar(15)  not null,
MVT_CONCEPTO_DESCRIPCION   varchar(100) not null,
MVT_MOVIMIENTO_MIGRADO     int          not null,
MVT_DESCRIPCION_MOVIMIENTO varchar(300),
constraint PK_MOVIMIENTOS_CLIENTES_1
primary key (MVT_MEM_MEMBRESIA, MVT_ID),
constraint FK_MOVIMIENTOS_CLIENTES_PLANES_VENTAS
foreign key (MVT_MEM_MEMBRESIA, MVT_PLV_NUMEROPLAN) references dbo.PLANES_VENTAS
)
go

ES LA TABLA PRINCIPAL QUE GESTIONA EL REGISTRO DE MOVIMIENTOS BAJO LA LOGICA: UN MOVIMIENTO DE CARGO PUEDE TENER
MOVIMIENTOS HIJOS DE: ABONO, BONIFICACION, DESCUENTO

-- auto-generated definition
create table dbo.ORDEN_COBRANZA
(
ORC_MEM_MEMBRESIA             varchar(15)
constraint DF_ORDEN_COBRANZA_ORC_CLI_ID default 0 not null,
ORC_NUM_ORDENCOB              int                     not null,
ORC_LSV_DESARROLLO            int                     not null
constraint FK_ORDEN_COBRANZA_LISTAS_VALORES
references dbo.LISTAS_VALORES,
ORC_LSV_LUGARCOBRO            int,
ORC_LSV_HORARIOMAÑANA_INICIAL int,
ORC_LSV_HORARIOMAÑANA_FINAL   int,
ORC_LSV_HORARIOTARDE_INICIAL  int,
ORC_LSV_HORARIOTARDE_FINAL    int,
ORC_FECHA_GENERACION          datetime                not null,
ORC_FECHA_COBRO               datetime,
ORC_USR_USUARIO               varchar(15)             not null,
ORC_EMP_ID_MENSAJERO          int,
ORC_DCL_ID                    int,
ORC_LSV_ESTATUSORDENCOBRANZA  int                     not null,
ORC_RCB_NUMERO_RECIBO         int,
ORC_RCB_LSV_SERIERECIBO       int,
ORC_FECHA_REGISTRO            datetime                not null,
constraint PK_ORDEN_COBRANZA
primary key (ORC_NUM_ORDENCOB, ORC_LSV_DESARROLLO, ORC_MEM_MEMBRESIA)
)
go

create table dbo.ORDEN_COBRANZA_DETALLE
(
OCD_MVT_MEM_MEMBRESIA  varchar(15) not null,
OCD_MVT_ID             int         not null,
OCD_ORC_NUM_ORDENCOB   int         not null,
OCD_ORC_LSV_DESARROLLO int         not null
constraint FK_ORDEN_COBRANZA_DETALLE_LISTAS_VALORES
references dbo.LISTAS_VALORES,
OCD_FECHA_REGISTRO     datetime    not null,
constraint PK_ORDEN_COBRANZA_DETALLE
primary key (OCD_ORC_NUM_ORDENCOB, OCD_ORC_LSV_DESARROLLO, OCD_MVT_MEM_MEMBRESIA, OCD_MVT_ID)
)
go

create index IX_ORDEN_COBRANZA_DETALLE
on dbo.ORDEN_COBRANZA_DETALLE (OCD_MVT_ID, OCD_MVT_MEM_MEMBRESIA, OCD_ORC_NUM_ORDENCOB)
go

-- auto-generated definition
create table dbo.LISTAS_VALORES
(
LSV_ID             int         not null
constraint PK_LISTAS_VALORES
primary key,
LSV_DESCRIPCION    varchar(75) not null,
LSV_TABLA          varchar(50) not null,
LSV_CLAVE          varchar(15) not null,
LSV_FECHA_REGISTRO datetime    not null
)
go

create index _dta_index_LISTAS_VALORES_7_1772585403__K1_2
on dbo.LISTAS_VALORES (LSV_ID) include (LSV_DESCRIPCION)
go

create index _dta_index_LISTAS_VALORES_7_1772585403__K3_K4_K1_2
on dbo.LISTAS_VALORES (LSV_TABLA, LSV_CLAVE, LSV_ID) include (LSV_DESCRIPCION)
go

CON TABLAS REELEVANTES:

567,MEMBRESIA,CLASIFICACIONMOVIMIENTOS,M,2008-05-15 00:00:00.000
568,SERVICIO,CLASIFICACIONMOVIMIENTOS,S,2008-05-15 00:00:00.000
593,SPA,CLASIFICACIONMOVIMIENTOS,SPA,2008-05-15 00:00:00.000
601,GENERADO,ESTATUSMOVIMIENTOS,1,2008-05-15 00:00:00.000
602,ORDEN DE COBRANZA,ESTATUSMOVIMIENTOS,2,2008-05-15 00:00:00.000
603,PAGADO,ESTATUSMOVIMIENTOS,3,2008-05-15 00:00:00.000
604,PAGO PARCIAL,ESTATUSMOVIMIENTOS,4,2008-05-15 00:00:00.000
605,NO GENERADO,ESTATUSMOVIMIENTOS,5,2008-05-15 00:00:00.000
606,GENERADO MANUAL,ESTATUSMOVIMIENTOS,6,2008-05-15 00:00:00.000
607,CANCELADO,ESTATUSMOVIMIENTOS,7,2008-05-15 00:00:00.000
608,BONIFICADO,ESTATUSMOVIMIENTOS,8,2008-05-15 00:00:00.000
609,BONIFICACION PARCIAL,ESTATUSMOVIMIENTOS,9,2008-05-15 00:00:00.000
610,RECORRIMIENTO,ESTATUSMOVIMIENTOS,10,2008-05-15 00:00:00.000
634,EVENTO,CLASIFICACIONMOVIMIENTOS,EVE,2012-03-14 10:13:39.747
684,GENERADO,ESTATUSRECIBOS,1,2008-05-15 00:00:00.000
685,ASIGNADO,ESTATUSRECIBOS,2,2008-05-15 00:00:00.000
686,PAGADO,ESTATUSRECIBOS,3,2008-05-15 00:00:00.000
687,CANCELADO,ESTATUSRECIBOS,4,2008-05-15 00:00:00.000
688,CANCELADO SIN PAGO,ESTATUSRECIBOS,5,2008-05-15 00:00:00.000
694,GENERADA,ESTATUSORDENCOBRANZA,1,2008-05-15 00:00:00.000
695,CANCELADA,ESTATUSORDENCOBRANZA,2,2008-05-15 00:00:00.000
696,CANCELADA SIN PAGAR,ESTATUSORDENCOBRANZA,3,2008-05-15 00:00:00.000
697,PAGADA,ESTATUSORDENCOBRANZA,4,2008-05-15 00:00:00.000
824,EN PROCESO DE CARGOS AUTOMATICOS,ESTATUSMOVIMIENTOS,11,2009-02-05 14:10:48.683
911,MANUAL RECIBOS,TIPOSERIERECIBOS,1,2009-10-14 11:05:59.047
912,AUTOMATICA,TIPOSERIERECIBOS,2,2009-10-14 11:05:59.093
934,MANUAL FACTURAS,TIPOSERIERECIBOS,4,2009-11-19 19:55:34.487
1120,RECIBO COBRANZA INTERNA,TIPORECIBOCOBRANZA,1,2010-12-03 08:28:00.873
1121,RECIBO COBRANZA GESTORIA,TIPORECIBOCOBRANZA,2,2010-12-03 08:28:01.123
1122,RECIBO COBRANZA CARGOS AUTOMATICOS,TIPORECIBOCOBRANZA,3,2010-12-03 08:28:01.170
1123,RECIBO COBRANZA INTERNET,TIPORECIBOCOBRANZA,4,2010-12-03 08:28:01.280
1346,EN PROCESO DE CARGOS POR INTERNET,ESTATUSMOVIMIENTOS,12,2012-05-07 00:00:00.000
1624,GENERADO DENTRO DE UN PAQUETE,ESTATUSMOVIMIENTOS,13,2014-07-09 13:21:19.950
9866,CARGO A HABITACION,CLASIFICACIONMOVIMIENTOS,CARGO_HAB,2025-12-09 16:13:22.647

-- auto-generated definition
create table dbo.RECIBOS_COBRANZA
(
RCB_MEM_MEMBRESIA          varchar(15) not null,
RCB_NUMERO_RECIBO          int         not null,
RCB_LSV_SERIERECIBO        int         not null
constraint FK_RECIBOS_COBRANZA_LISTAS_VALORES1
references dbo.LISTAS_VALORES,
RCB_FECHA_GENERACION       datetime    not null,
RCB_FECHA_PAGO             datetime,
RCB_ORC_NUM_ORDENCOB       int         not null,
RCB_IMPORTE_RECIBO         money       not null,
RCB_USR_USUARIO            varchar(15) not null,
RCB_LSV_ESTATUSRECIBOS     int         not null
constraint FK_RECIBOS_COBRANZA_LISTAS_VALORES
references dbo.LISTAS_VALORES,
RCB_LSV_DESARROLLO         int         not null
constraint FK_RECIBOS_COBRANZA_LISTAS_VALORES2
references dbo.LISTAS_VALORES,
RCB_USR_USUARIO_PAGADOR    varchar(15) not null,
RCB_LSV_TIPORECIBOCOBRANZA int         not null,
constraint PK_RECIBOS_COBRANZA_1
primary key (RCB_MEM_MEMBRESIA, RCB_NUMERO_RECIBO, RCB_LSV_SERIERECIBO),
constraint UQ_RECIBOS_COBRANZA
unique (RCB_NUMERO_RECIBO, RCB_LSV_SERIERECIBO, RCB_LSV_DESARROLLO)
)
go

create index _dta_index_RECIBOS_COBRANZA_16_1840829720__K11_K6_K10_K3_2
on dbo.RECIBOS_COBRANZA (RCB_LSV_DESARROLLO, RCB_ORC_NUM_ORDENCOB, RCB_MEM_MEMBRESIA,
RCB_LSV_SERIERECIBO) include (RCB_NUMERO_RECIBO)
go

-- auto-generated definition
create table dbo.COMPAÑIAS_BANCOS
(
CIAB_ID             int          not null,
CIAB_NOMBRE_CORTO   varchar(50)  not null,
CIAB_NOMBRE_EMPRESA varchar(165) not null,
CIAB_LSV_BANCO      int          not null,
CIAB_CUENTA         varchar(15)  not null,
CIAB_CLABE          varchar(18)  not null,
CIAB_USR_USUARIO    varchar(15)  not null,
CIAB_FECHA_REGISTRO datetime     not null,
CIAB_DIRECCION      nvarchar(300),
CIAB_CP             nvarchar(20),
CIAB_COLONIA        nvarchar(100),
CIAB_MUNICIPIO      nvarchar(100),
CIAB_ESTADO         nvarchar(100),
CIAB_TELEFONO       nvarchar(50),
CIAB_EMAIL          nvarchar(100),
CIAB_RFC            nvarchar(20),
constraint PK_COMPAÑIAS_BANCOS
primary key ()
)
go

create index []
on dbo.COMPAÑIAS_BANCOS ()
go

-- auto-generated definition
  create table dbo.DESARROLLOS_SERIESRECIBOS
  (
  DSR_ID                         unknown,
  DSR_LSV_DESARROLLOS            unknown,
  DSR_LSV_SERIESRECIBOS          unknown,
  DSR_LSV_TIPOSERIERECIBOS       unknown,
  DSR_CIAB_ID                    unknown,
  DSR_ACTIVA                     unknown,
  DSR_LSV_CLASIFICACIONMEMBRESIA unknown,
  DSR_LSV_DESARROLLO_MEMBRESIA   unknown,
  DSR_COMENTARIO_ADICIONAL       unknown,
  DSR_SERIE_SOLO_EFECTIVO        unknown,
  DSR_FECHA_REGISTRO             unknown
  )
  go

-- auto-generated definition
create table dbo.ROLES_SERIES_RECIBOS
(
RSR_ROL_ID         unknown,
RSR_DSR_ID         unknown,
RSR_FECHA_REGISTRO unknown
)
go

paso 1: generar los sps que devuelven los movimientos de adeudo de un cliente: en la version legacy este era el sp



