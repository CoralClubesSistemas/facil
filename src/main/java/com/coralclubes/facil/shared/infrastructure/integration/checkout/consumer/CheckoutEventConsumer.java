package com.coralclubes.facil.shared.infrastructure.integration.checkout.consumer;

import com.coralclubes.facil.shared.events.dto.CheckoutPaymentStatusChangedEvent;
import com.coralclubes.facil.shared.infrastructure.integration.checkout.dto.CheckoutPaymentEventDto;
import com.coralclubes.logging.BusinessLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.messaging.provider", havingValue = "rabbitmq", matchIfMissing = true)
public class CheckoutEventConsumer {

    private final BusinessLogger logger;
    private final ApplicationEventPublisher eventPublisher;

    @RabbitListener(queues = "payment.status.queue")
    @Transactional
    public void recibirEventoPago(CheckoutPaymentEventDto eventDto) {
        logger.info("CHECKOUT_CONSUMER", "Evento de RabbitMQ recibido para transacción UUID: {} (Estatus: {})",
                eventDto.transactionUuid(), eventDto.status());

        try {
            CheckoutPaymentStatusChangedEvent internalEvent = CheckoutPaymentStatusChangedEvent.builder()
                    .eventType(eventDto.eventType())
                    .transactionUuid(eventDto.transactionUuid())
                    .externalReference(eventDto.externalReference())
                    .status(eventDto.status())
                    .amount(eventDto.amount())
                    .paymentMethod(eventDto.paymentMethod())
                    .authorizationCode(eventDto.authorizationCode())
                    .eventDate(eventDto.eventDate())
                    .metadata(eventDto.metadata())
                    .build();

            eventPublisher.publishEvent(internalEvent);
            logger.info("CHECKOUT_CONSUMER", "Evento interno publicado con éxito para transacción UUID: {}", eventDto.transactionUuid());
        } catch (Exception e) {
            logger.error("CHECKOUT_CONSUMER", "Error al procesar y publicar el evento interno para UUID: " + eventDto.transactionUuid(), e);
        }
    }
}
