package com.coralclubes.facil.modules.sistema.service;

import com.coralclubes.facil.modules.sistema.dto.response.PlantillaCuerpoCorreo;
import com.coralclubes.facil.modules.sistema.repository.PlantillasCuerpoCorreoRepository;
import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlantillasCuerpoCorreoService {

    private final PlantillasCuerpoCorreoRepository repository;
    private final PebbleEngine pebbleEngine;

    public String renderizarCuerpo(String codigo, Map<String, Object> variables) {
        PlantillaCuerpoCorreo plantilla = repository.obtenerPorCodigo(codigo)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la plantilla de correo activa con código: " + codigo));

        try {
            Writer writer = new StringWriter();
            PebbleTemplate template = pebbleEngine.getTemplate(plantilla.cuerpo());
            template.evaluate(writer, variables);
            return writer.toString();
        } catch (Exception e) {
            log.error("Error:{}", String.valueOf(e));
            throw new RuntimeException("Error al renderizar la plantilla de correo con Pebble: " + codigo, e);
        }
    }

    public String renderizarAsunto(String codigo, Map<String, Object> variables) {
        PlantillaCuerpoCorreo plantilla = repository.obtenerPorCodigo(codigo)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la plantilla de correo activa con código: " + codigo));

        try {
            Writer writer = new StringWriter();
            PebbleTemplate template = pebbleEngine.getTemplate(plantilla.asunto());
            template.evaluate(writer, variables);
            return writer.toString();
        } catch (Exception e) {
            return plantilla.asunto();
        }
    }
}
