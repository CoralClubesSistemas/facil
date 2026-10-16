package com.coralclubes.facil.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;

/**
 * Configuración global de Web MVC para registro de convertidores personalizados.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new FlexibleStringToLocalDateConverter());
    }

    /**
     * Convertidor flexible para Strings a LocalDate que soporta:
     * - yyyy-MM-dd (ISO estándar)
     * - yyyyMMdd (formato compacto enviado frecuentemente por frontend)
     * - dd/MM/yyyy
     * - dd-MM-yyyy
     * - yyyy/MM/dd
     */
    public static class FlexibleStringToLocalDateConverter implements Converter<String, LocalDate> {

        private static final DateTimeFormatter FLEXIBLE_FORMATTER = new DateTimeFormatterBuilder()
                .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                .appendOptional(DateTimeFormatter.ofPattern("yyyyMMdd"))
                .appendOptional(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                .appendOptional(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                .appendOptional(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
                .toFormatter();

        @Override
        public LocalDate convert(String source) {
            if (source == null || source.isBlank()) {
                return null;
            }
            try {
                return LocalDate.parse(source.trim(), FLEXIBLE_FORMATTER);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException(String.format("Formato de fecha inválido: '%s'. Se esperaba yyyy-MM-dd, yyyyMMdd o dd/MM/yyyy", source), e);
            }
        }
    }
}
