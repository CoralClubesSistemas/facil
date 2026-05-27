package com.coralclubes.facil.shared.infrastructure.integration.storage.consumer;

import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.StorageEventDto;
import com.coralclubes.facil.shared.events.dto.StorageFileProcessedEvent;
import com.coralclubes.logging.BusinessLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Consumidor de eventos de almacenamiento desde RabbitMQ.
 * Recibe el evento físico, lo transforma en un evento de Spring interno y lo difunde a los demás módulos de negocio.
 * Activo únicamente si el proveedor de mensajería configurado es 'rabbitmq'.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.messaging.provider", havingValue = "rabbitmq")
public class StorageEventConsumer {

    private final BusinessLogger logger;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Escucha la cola de RabbitMQ para los eventos de almacenamiento destinados al sistema Facil.
     *
     * @param eventDto El evento de almacenamiento recibido y deserializado a DTO.
     */
    @RabbitListener(queues = "${app.rabbitmq.queues.storage}")
    public void recibirEventoStorage(StorageEventDto eventDto) {
        logger.info("STORAGE_CONSUMER", "Evento de RabbitMQ recibido para archivo UUID: {} (Estatus: {})",
                eventDto.fileId(), eventDto.status());

        try {
            // Mapeamos el evento de RabbitMQ a un evento interno de Spring
            StorageFileProcessedEvent internalEvent = new StorageFileProcessedEvent(
                    eventDto.fileId(),
                    eventDto.bucket(),
                    eventDto.path(),
                    eventDto.status(),
                    eventDto.system(),
                    eventDto.message(),
                    eventDto.metadatos()
            );

            // Publicamos el evento a nivel del contexto de Spring
            eventPublisher.publishEvent(internalEvent);

            logger.info("STORAGE_CONSUMER", "Evento interno publicado con éxito para archivo UUID: {}", eventDto.fileId());

        } catch (Exception e) {
            logger.error("STORAGE_CONSUMER", "Error al procesar y publicar el evento interno para UUID: " + eventDto.fileId(), e);
        }
    }
}
