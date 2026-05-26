# Skill: Integracion con Microservicio Coral Notificaciones

## Trigger
Activa esta skill SIEMPRE que el requerimiento implique:
- Enviar correos electronicos, SMS o mensajes de WhatsApp.
- Notificar a clientes o empleados hacia el exterior del sistema.

## 1. Principio Arquitectonico: "Fire and Forget"
PROHIBIDO implementar logica de envio de correos (ej. `JavaMailSender`) en el sistema actual.
Toda comunicacion externa se delega al microservicio centralizado mediante una peticion HTTP asincrona o por mensajeria asincrona.

## 2. Autenticacion
Todas las peticiones a la API de Coral Notificaciones DEBEN incluir el header `X-API-KEY`. A nivel de colas, se debe inyectar la cabecera `x-api-key` en las propiedades del mensaje.

## 3. Endpoints REST principales
### 3.1 POST /api/v1/notificaciones/enviar
Recibe un payload JSON. Privilegia siempre el "Envio por Plantilla".

**Estructura (Envio por Plantilla):**
```json
{
  "aliasConfig": "string (ej. COBRANZA_EMAIL)",
  "destinatarios": [
    "email@dominio.com"
  ],
  "codigoPlantilla": "string",
  "variables": {
    "llave": "valor"
  },
  "prioridad": 5,
  "adjuntos": ["uuid-archivo-1"]
}
```
**Estructura (Mensaje Directo sin plantilla):**
```json
{
  "aliasConfig": "string (ej. ALERTA_SMS)",
  "destinatarios": ["+52..."],
  "cuerpo": "Mensaje de texto",
  "adjuntos": []
}
```

### 3.2 POST /api/v1/notificaciones/enviar-con-adjuntos (Multipart)
Utiliza `multipart/form-data` para enviar archivos binarios en memoria y adjuntarlos de forma directa. Requiere dos partes:
- `solicitud` (application/json): `SolicitudNotificacionDto` estándar.
- `archivos` (multipart/form-data): Uno o varios archivos binarios.

## 4. Consumo Asíncrono por Eventos (RabbitMQ)
Para evitar peticiones HTTP síncronas en hilos críticos, publica la solicitud en la cola `INBOX` (`coral-notificaciones-inbox`).
Para auditar o conciliar el estado de los envíos físicos, suscríbete a la cola `READY` (`coral-notificaciones-ready`).

## 5. Manejo de Respuestas
- En REST, el microservicio devuelve HTTP 202 Accepted.
- Extrae y guarda el `trackingId` del response (`data.trackingId`) si necesitas auditar la entrega en el futuro.
- Si el response es 4xx o 5xx (o falla la conexión), el backend debe loggear el error técnico pero **NO** debe detener el proceso de negocio principal.


