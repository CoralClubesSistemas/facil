# Skill: Integracion con Microservicio Coral Notificaciones

## Trigger
Activa esta skill SIEMPRE que el requerimiento implique:
- Enviar correos electronicos, SMS o mensajes de WhatsApp.
- Notificar a clientes o empleados hacia el exterior del sistema.

## 1. Principio Arquitectonico: "Fire and Forget"
PROHIBIDO implementar logica de envio de correos (ej. `JavaMailSender`) en el sistema actual.
Toda comunicacion externa se delega al microservicio centralizado mediante una peticion HTTP asincrona o sin bloqueo de hilo critico.

## 2. Autenticacion
Todas las peticiones a la API de Coral Notificaciones DEBEN incluir el header `X-API-KEY`.

## 3. Endpoint Principal
### POST /api/v1/notificaciones/enviar
Debes construir un payload JSON dependiendo del caso de uso. Privilegia siempre el "Envio por Plantilla".

**Estructura Obligatoria (Envio por Plantilla):**
```json
{
  "codigoSistema": "FACIL_CORE",
  "aliasConfig": "string (ej. COBRANZA_EMAIL)",
  "destinatarios": [
    "email@dominio.com"
  ],
  "codigoPlantilla": "string",
  "variables": {
    "llave": "valor"
  },
  "prioridad": 5
}
```
**Estructura Obligatoria (Mensaje Directo sin plantilla):**
```json
{
  "codigoSistema": "FACIL_CORE",
  "aliasConfig": "string (ej. ALERTA_SMS)",
  "destinatarios": ["+52..."],
  "cuerpo": "Mensaje de texto"
}
```
## 4. Manejo de Respuestas
- El microservicio devuelve HTTP 202 Accepted si encolo el mensaje.
- Extrae y guarda el trackingId del response (data.trackingId) si el proceso de negocio requiere auditar la entrega en el futuro.
- Si el response es 4xx (ej. NOTIF_101 Template Not Found) o 5xx, el backend debe loggear el error tecnico pero NO debe detener el proceso de negocio principal (ej. Si falla el correo de bienvenida, la reserva aun asi debe guardarse exitosamente).

