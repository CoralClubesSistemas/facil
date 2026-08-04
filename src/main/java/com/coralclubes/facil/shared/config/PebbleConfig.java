package com.coralclubes.facil.shared.config;

import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.loader.StringLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración para el motor de plantillas Pebble.
 * <p>
 * Se configura una instancia de PebbleEngine utilizando {@link StringLoader}.
 * Esto permite que Pebble compile y renderice plantillas HTML pasadas directamente como cadenas
 * de texto (obtenidas dinámicamente de la base de datos), en lugar de leerlas desde archivos locales en disco.
 * </p>
 * <p>
 * Funcionamiento del caché con StringLoader:
 * Pebble cachea internamente las plantillas compiladas utilizando el propio contenido HTML de la plantilla
 * como clave de caché. Dado que las plantillas en base de datos son estáticas para cada renderización
 * (solo varían los datos/variables inyectados durante la evaluación), las subsecuentes solicitudes de
 * renderización de la misma plantilla tendrán un excelente rendimiento al recuperar la plantilla ya compilada
 * desde el caché.
 * Si una plantilla es modificada en la base de datos, Pebble la tratará como una clave de caché nueva
 * al renderizarse, compilándola y cacheándola automáticamente sin requerir reinicios.
 * </p>
 */
@Configuration
public class PebbleConfig {

    @Bean
    public PebbleEngine pebbleEngine() {
        return new PebbleEngine.Builder()
                .loader(new StringLoader())
                .strictVariables(false)
                .build();
    }
}
