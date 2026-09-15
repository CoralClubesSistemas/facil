package com.coralclubes.facil.shared.config;

import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        // Serializador de Spring Data Redis compatible nativamente con Java Records
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();
        serializer.configure(mapper -> {
            mapper.registerModule(new JavaTimeModule());
            mapper.registerModule(new Jdk8Module());
        });

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(2))
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(serializer)
                );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }

    /**
     * Limpia en el arranque las cachés cuyos registros viejos se guardaron sin metadatos de tipo.
     */
    @Bean
    public CommandLineRunner clearCacheOnStartup(CacheManager cacheManager) {
        return args -> {
            try {
                List<String> cachesToClear = List.of(
                        "storage_urls",
                        "tipo_unidad_detalles",
                        "tipos_unidad_cards",
                        "tipo_unidad_imagenes",
                        "tipo_unidad_caracteristicas",
                        "temporadas_reservaciones",
                        "temporadas_fecha",
                        "tarifas_reservaciones",
                        "hoteles_cards",
                        "hotel_imagenes",
                        "hotel_caracteristicas",
                        "plantillas_pdf"
                );
                for (String cacheName : cachesToClear) {
                    Cache cache = cacheManager.getCache(cacheName);
                    if (cache != null) {
                        cache.clear();
                    }
                }
            } catch (Exception e) {
                // Silencioso en caso de entornos sin Redis activo
            }
        };
    }
}