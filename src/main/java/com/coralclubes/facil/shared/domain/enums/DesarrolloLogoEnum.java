package com.coralclubes.facil.shared.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Base64;

@Slf4j
@Getter
@RequiredArgsConstructor
public enum DesarrolloLogoEnum {

    CUERNAVACA(
            1,
            "CUERNAVACA",
            "static/img/logos_desarrollos/cuernavaca/logo-cc.jpg",
            "image/jpeg",
            "#0f3a6f"
    );

    private final Integer desarrolloId;
    private final String nombre;
    private final String rutaClasspath;
    private final String mimeType;
    private final String bgColor;

    /**
     * Obtiene los bytes de la imagen del logo desde el classpath.
     */
    public byte[] getLogoBytes() {
        try (InputStream is = new ClassPathResource(rutaClasspath).getInputStream()) {
            return is.readAllBytes();
        } catch (Exception e) {
            log.error("No se pudo cargar el logo desde classpath para {}: {}", name(), e.getMessage());
            return new byte[0];
        }
    }

    /**
     * Obtiene la imagen codificada como Data URI en Base64 para incrustar directamente en HTML/Pebble/Gotenberg.
     */
    public String getLogoBase64DataUri() {
        byte[] bytes = getLogoBytes();
        if (bytes.length == 0) {
            return "";
        }
        return "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * Busca el enum correspondiente por ID de desarrollo.
     */
    public static DesarrolloLogoEnum fromDesarrolloId(Integer desarrolloId) {
        if (desarrolloId == null) {
            return CUERNAVACA;
        }
        return Arrays.stream(values())
                .filter(d -> d.desarrolloId.equals(desarrolloId))
                .findFirst()
                .orElse(CUERNAVACA);
    }

    /**
     * Busca el enum correspondiente por nombre del desarrollo.
     */
    public static DesarrolloLogoEnum fromNombre(String nombreDesarrollo) {
        if (nombreDesarrollo == null || nombreDesarrollo.isBlank()) {
            return CUERNAVACA;
        }
        String normalizado = nombreDesarrollo.toUpperCase().trim();
        return Arrays.stream(values())
                .filter(d -> normalizado.contains(d.nombre) || d.nombre.contains(normalizado))
                .findFirst()
                .orElse(CUERNAVACA);
    }
}
