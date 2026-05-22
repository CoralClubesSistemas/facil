package com.coralclubes.facil.shared.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de infraestructura para RabbitMQ en el backend de Facil.
 * Declara las colas, exchanges y bindings necesarios para consumir eventos de almacenamiento.
 * Activo únicamente si el proveedor de mensajería configurado es 'rabbitmq'.
 */
@Configuration
@ConditionalOnProperty(name = "app.messaging.provider", havingValue = "rabbitmq")
public class RabbitConfig {

    @Value("${app.rabbitmq.exchange:coral.storage.exchange}")
    private String exchangeName;

    /**
     * Nombre de la cola de eventos de almacenamiento para el sistema Facil.
     */
    public static final String STORAGE_FACIL_QUEUE = "coral.storage.files.facil";

    /**
     * Routing key para filtrar eventos del sistema Facil.
     */
    public static final String STORAGE_FACIL_ROUTING_KEY = "storage.event.facil";

    /**
     * Declara la cola duradera para los eventos de Facil.
     */
    @Bean
    public Queue storageFacilQueue() {
        return new Queue(STORAGE_FACIL_QUEUE, true);
    }

    /**
     * Declara el TopicExchange del servicio de almacenamiento.
     */
    @Bean
    public TopicExchange storageExchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    /**
     * Une la cola al exchange filtrando por la routing key específica de Facil.
     */
    @Bean
    public Binding bindingStorageFacilQueue(Queue storageFacilQueue, TopicExchange storageExchange) {
        return BindingBuilder.bind(storageFacilQueue)
                .to(storageExchange)
                .with(STORAGE_FACIL_ROUTING_KEY);
    }

    /**
     * Configura el serializador/deserializador JSON de Spring AMQP.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
