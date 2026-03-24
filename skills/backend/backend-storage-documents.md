# Skill: Almacenamiento y Generacion de Documentos (Spring Boot) - Coral Clubes

## Trigger (Cuándo usar esta skill)

Activa esta skill SIEMPRE que debas:

- Subir, descargar o eliminar archivos adjuntos, imágenes o comprobantes.
- Generar documentos dinámicos (PDFs, Cartas de Ocupación, Contratos, Recibos).
- Interactuar con proveedores de almacenamiento en la nube o S3.

## 1. Principio Fundamental: "Backend Stateless (Sin Estado)"

El servidor de Spring Boot no tiene disco duro para el negocio. Todo archivo generado o subido por el usuario fluye
temporalmente por la memoria RAM y debe depositarse inmediatamente en el Object Storage (MinIO/S3 compatible).

## 2. Gestion de Archivos (Object Storage)

- **Herramienta Obligatoria:** Utiliza el bean inyectable `StorageClient` para interactuar con el almacenamiento.
- **Regla de Base de Datos:** En las tablas de SQL Server (ej. `RESERVACIONES_UNIDADES_IMAGENES`) NUNCA se guardan
  bytes (`VARBINARY`) ni rutas de carpetas locales (`C:\archivos\...`). Solo se guarda el identificador único del
  archivo (UUID) devuelto por el `StorageClient`.
- **URLs Temporales:** Para mostrar una imagen o descargar un archivo en Angular, el Backend debe convertir ese UUID en
  una URL prefirmada (Pre-Signed URL) usando `storageClient.obtenerUrlDescarga(uuid)`. No expongas los buckets
  directamente.

## 3. Generacion de Documentos Dinamicos (PDF)

- **Motor de Plantillas:** Utiliza Thymeleaf EXCLUSIVAMENTE para procesar plantillas HTML que luego se convertirán a
  PDF. Thymeleaf no se usa para vistas web del usuario.
- **Flujo de Generación:**
    1. Recopilar los datos del negocio (DTO).
    2. Procesar el HTML inyectando el DTO mediante `TemplateEngine` de Thymeleaf.
    3. Convertir el String HTML a un arreglo de bytes (`byte[]`) de PDF.
    4. Subir el `byte[]` inmediatamente al `StorageClient` y obtener el UUID.
    5. Retornar la URL de descarga al usuario o enviarla como adjunto al Microservicio de Notificaciones.

## 4. Prohibiciones Absolutas (Anti-Patrones)

1. **PROHIBIDO Uso de java.io.File:** No crees ni manipules archivos temporales en el disco del servidor. Usa
   `InputStream`, `ByteArrayOutputStream` o `MultipartFile` para procesar todo en memoria.
2. **PROHIBIDO Retornar Bytes en Endpoints:** No devuelvas un `byte[]` directamente en la respuesta HTTP (
   `ResponseEntity<byte[]>`). Sube el archivo al S3 y devuelve el UUID o la URL de descarga dentro del
   `ApiResponse<String>`.
3. **PROHIBIDO Archivos Huérfanos:** Si una transacción de base de datos falla y se hace Rollback, asegúrate de instruir
   al `StorageClient` para que elimine el archivo recién subido en S3, evitando basura digital.