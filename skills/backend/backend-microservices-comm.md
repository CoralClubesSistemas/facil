# Skill: Comunicacion entre Microservicios (Spring Boot) - Coral Clubes

## Trigger (Cuándo usar esta skill)

Activa esta skill SIEMPRE que debas:

- Hacer que un microservicio de Coral Clubes consuma datos o envíe comandos a otro microservicio interno.
- Configurar clientes HTTP (Feign, RestTemplate, WebClient).
- Establecer comunicación asíncrona mediante colas de mensajes.

## 1. Principio Fundamental: "Resiliencia y Desacoplamiento"

Los microservicios de Coral Clubes deben diseñarse asumiendo que la red fallará y que otros servicios pueden estar
caídos. Se prefiere la comunicación asíncrona (Fire-and-Forget) vía RabbitMQ siempre que el proceso de negocio lo
permita. La comunicación síncrona (HTTP) se reserva solo para consultas de lectura estrictamente necesarias o bloqueos
de flujo críticos.

## 2. Comunicacion Sincrona (HTTP/REST)

- **Herramienta:** Utiliza Spring Cloud OpenFeign para la comunicación síncrona.
- **Propagación de Identidad:** Es OBLIGATORIO configurar un `RequestInterceptor` en Feign para extraer el token JWT del
  `UserContext` o `SecurityContext` actual e inyectarlo en la cabecera `Authorization: Bearer` de la petición saliente.
- **Descubrimiento y Enrutamiento:** Las URLs base de los microservicios destino NUNCA se queman en el código. Deben
  inyectarse desde el `application.yml` o el Config Server usando
  `@FeignClient(name = "servicio-destino", url = "${app.clients.servicio-destino.url}")`.

## 3. Comunicacion Asincrona (Event Driven)

- **Herramienta:** Utiliza RabbitMQ (Spring AMQP) para notificar cambios de estado que otros microservicios deban saber.
- **Formato del Mensaje:** Los payloads enviados a las colas/exchanges deben ser objetos Java serializados a JSON (
  Records).
- **Idempotencia:** Los consumidores de los mensajes (`@RabbitListener`) deben diseñarse para ser idempotentes (procesar
  el mismo mensaje dos veces no debe corromper los datos).

## 4. Prohibiciones Absolutas (Anti-Patrones)

1. **PROHIBIDO Acoplamiento de Base de Datos:** Un microservicio NUNCA debe conectarse directamente a la base de datos
   de otro microservicio. Si necesita datos, debe pedirlos por API o reaccionar a eventos.
2. **PROHIBIDO Ignorar Timeouts:** Nunca configures un cliente Feign o HTTP sin `connectTimeout` y `readTimeout`. El
   bloqueo indefinido de un hilo tumbará el servicio origen.
3. **PROHIBIDO Retornar Errores Crudos:** Si una llamada Feign falla con 4xx o 5xx, el servicio origen debe capturar la
   `FeignException` y traducirla a un `ApiResponse.error()` estándar del sistema, evitando exponer stacktraces al
   cliente final.