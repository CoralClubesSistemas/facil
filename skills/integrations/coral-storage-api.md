# Skill: Integracion con Microservicio Coral Almacenamiento

## Trigger
Activa esta skill SIEMPRE que el requerimiento implique:
- Subir, descargar o gestionar archivos, imagenes o documentos.
- Interactuar con MinIO o AWS S3 dentro del ecosistema Coral.

## 1. Principio Arquitectonico: Patron "Valet Key"
PROHIBIDO procesar archivos binarios (`MultipartFile`) en el backend de los sistemas satelite (ej. FACIL).
El flujo obligatorio de 3 pasos es:
1. **Negociacion (Backend):** El backend solicita permiso a Coral Almacenamiento via POST a `/api/v1/storage/sign-upload`.
2. **Carga Directa (Frontend):** El backend devuelve la `uploadUrl` al Frontend. El Frontend hace un PUT directo del binario a esa URL.
3. **Confirmacion (Backend):** El backend escucha el evento RabbitMQ (`storage.files.ready`) para saber que el archivo se subio exitosamente.

## 2. Autenticacion
Todas las peticiones a la API de Coral Almacenamiento DEBEN incluir el header `X-API-KEY` inyectado a traves del cliente HTTP (Feign/RestTemplate) tomando el valor de las variables de entorno (`${app.integrations.storage.api-key}`).

## 3. Estructura de Peticiones
### POST /api/v1/storage/sign-upload
**Request:**
```json
{
  "nombreArchivo": "string",
  "contentType": "string",
  "tamanoBytes": 0,
  "esPublico": boolean,
  "aliasConfiguracion": "string",
  "rutaLogica": "string",
  "metadatos": {}
}
```

**Response:** (Extraer fileId y uploadUrl):
```json
{ "code": "STORAGE_200", "result": { "fileId": "uuid", "uploadUrl": "url", "metodo": "PUT" } }
```

### GET /api/v1/storage/files/{uuid}
Para reaccionar a cargas exitosas, debes crear un @RabbitListener suscrito al exchange coral.topic con la routing key storage.files.ready. El payload tendra la propiedad status: "READY" y el fileId.