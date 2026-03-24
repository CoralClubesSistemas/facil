# Skill: Motor de Notificaciones y Mensajeria (Spring Boot) - Coral Clubes

## Trigger (Cuándo usar esta skill)

Activa esta skill SIEMPRE que debas:

- Alertar a los usuarios conectados en la plataforma web (Campanita, Modales, WebSockets).
- Enviar correos electrónicos, mensajes SMS o WhatsApp a clientes o empleados.
- Diseñar flujos de comunicación hacia el exterior o el interior del PMS.

## 1. Principio Fundamental: "Separación de Canales (Interno vs Externo)"

En Coral Clubes existen dos motores de comunicación con responsabilidades y arquitecturas completamente distintas. NUNCA
deben mezclarse ni confundirse sus DTOs.

## 2. Canal Interno (Alertas UI en Tiempo Real)

Este canal notifica a los usuarios que tienen abierto el sistema web en sus navegadores (ej. Ama de Llaves, Recepción).

- **Tecnología:** STOMP sobre WebSockets con RabbitMQ Broker Relay.
- **DTO:** Utiliza `PeticionNotificacionDto`.
- **Proceso Obligatorio:**
    1. Persistir la alerta en la Base de Datos (para que quede en el historial si el usuario está desconectado).
    2. Llamar a `notificacionEmisor.enviarAUsuario(...)` o `enviarAMultiples(...)`.
    3. El payload DEBE incluir el campo `metadata` con la clave `urlDestino` para que Angular sepa a dónde navegar al
       hacer clic.
- **Niveles de Prioridad:** 1 = Info, 2 = Alerta Normal, 3 = Crítica (Detona modal bloqueante en Angular).

## 3. Canal Externo (Microservicio de Mensajería Omnicanal)

Este canal envía comunicados al mundo real (Emails con PDFs, SMS para confirmaciones, WhatsApps).

- **Tecnología:** Colas de RabbitMQ dirigidas a un Microservicio de Notificaciones aislado.
- **DTO:** Utiliza `SolicitudNotificacionDto`.
- **Proceso Obligatorio (Fire and Forget):**
    1. Construir el payload especificando `codigoSistema`, `codigoPlantilla` (Template), y el mapa de `variables`.
    2. Si requiere adjuntos, pasar las URLs de descarga de MinIO en la lista `adjuntos`.
    3. Llamar a `notificationClient.enviarNotificacion(solicitud)`. El hilo principal NO debe esperar a que el correo se
       envíe.

## 4. Prohibiciones Absolutas (Anti-Patrones)

1. **PROHIBIDO el Envío Síncrono de Correos:** NUNCA inyectes `JavaMailSender` en los servicios core del negocio (ej.
   Reservaciones). Todo correo se delega al microservicio de notificaciones vía cola asíncrona.
2. **PROHIBIDO Enviar Mensajes Anónimos STOMP:** No uses `convertAndSend("/topic/...")` a menos que sea un broadcast
   global. Usa siempre `convertAndSendToUser` apoyándote en la identidad interceptada del WebSocket.
3. **PROHIBIDO Hardcodear Cuerpos de Correo:** Los textos de los correos (HTML) no viven en el código Java. Utiliza
   siempre el `codigoPlantilla` para que el Microservicio de Notificaciones resuelva el diseño final.