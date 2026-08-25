package com.coralclubes.facil.shared.infrastructure.integration.crm.zoho.adapter;

import com.coralclubes.facil.modules.prospectos.dto.request.ProspectoCrearRequest;
import com.coralclubes.facil.modules.prospectos.dto.request.ProspectoRegistrarCitaRequest;
import com.coralclubes.facil.modules.prospectos.service.ProspectosService;
import com.coralclubes.facil.shared.infrastructure.integration.crm.zoho.dto.ZohoWebhookPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Adaptador específico para transformar los eventos y datos entrantes de Zoho CRM
 * hacia los modelos y servicios de dominio del módulo de Prospectos.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ZohoProspectoAdapter {

    private final ProspectosService prospectosService;
    private final ObjectMapper objectMapper;

    private static final Set<String> CAMPOS_CONSUMIDOS = Set.of(
            "id", "entity_id", "nombre", "apellido_paterno", "email",
            "telefono", "cargo", "edad", "desarrollo_interes", "green_fees",
            "fecha_inicio", "lugar_cita", "nota"
    );

    /**
     * Procesa un webhook recibido de Zoho CRM creando/actualizando el prospecto y su cita de forma secuencial.
     */
    public void procesarWebhookZoho(ZohoWebhookPayload payload, Map<String, String> rawParams) {
        Map<String, Object> dataMap = consolidarDatos(payload, rawParams);

        String idExterno = getVal(dataMap, "id");
        if (idExterno == null || idExterno.isBlank()) {
            idExterno = getVal(dataMap, "entity_id");
        }
        if (idExterno == null || idExterno.isBlank()) {
            idExterno = "ZOHO_" + System.currentTimeMillis();
        }

        log.info("[ZOHO ADAPTER] Procesando webhook de Zoho para ID Externo: {}", idExterno);

        // PASO 1: Crear o Actualizar Prospecto
        ProspectoCrearRequest prospectoRequest = construirProspectoRequest(idExterno, dataMap);
        Integer prospectoId = prospectosService.crearOActualizarProspecto(prospectoRequest);

        log.info("[ZOHO ADAPTER] Prospecto procesado con éxito. ID Interno: {}", prospectoId);

        // PASO 2: Registrar Cita para el Prospecto (si viene fecha_inicio)
        String fechaInicioStr = getVal(dataMap, "fecha_inicio");
        if (prospectoId != null && fechaInicioStr != null) {
            ProspectoRegistrarCitaRequest citaRequest = construirCitaRequest(prospectoId, fechaInicioStr, dataMap);
            prospectosService.registrarCitaProspecto(citaRequest, "ZOHO");
            log.info("[ZOHO ADAPTER] Cita registrada secuencialmente para el Prospecto ID: {}", prospectoId);
        } else {
            log.info("[ZOHO ADAPTER] El webhook no contiene fecha_inicio para cita. Proceso completado.");
        }
    }

    private ProspectoCrearRequest construirProspectoRequest(String idExterno, Map<String, Object> dataMap) {
        String[] nombres = separarNombres(getVal(dataMap, "nombre"));
        String[] apellidos = separarNombres(getVal(dataMap, "apellido_paterno"));

        String desarrolloTexto = getVal(dataMap, "desarrollo_interes");
        String greenFees = getVal(dataMap, "green_fees");

        Integer desarrolloInteres = mapearDesarrolloInteres(desarrolloTexto);
        Integer lsvAreaInteres = mapearAreaInteres(desarrolloTexto, greenFees);

        Map<String, Object> adicionalesMap = obtenerSoloDataAdicional(dataMap);
        String dataAdicionalJson = convertirAJsonString(adicionalesMap);

        return ProspectoCrearRequest.builder()
                .idExterno(idExterno)
                .origen("ZOHO")
                .nombre(nombres[0])
                .segundoNombre(nombres[1])
                .apellidoPaterno(apellidos[0])
                .apellidoMaterno(apellidos[1])
                .email(getVal(dataMap, "email"))
                .telefono(getVal(dataMap, "telefono"))
                .cargo(getVal(dataMap, "cargo"))
                .edad(getValInteger(dataMap, "edad"))
                .desarrolloInteres(desarrolloInteres)
                .lsvAreaInteres(lsvAreaInteres)
                .dataAdicional(dataAdicionalJson)
                .estatus(null)
                .build();
    }

    private ProspectoRegistrarCitaRequest construirCitaRequest(Integer prospectoId, String fechaInicioStr, Map<String, Object> dataMap) {
        LocalDateTime fechaHora = parsearFechaHora(fechaInicioStr);
        LocalDate fechaInicio = fechaHora != null ? fechaHora.toLocalDate() : LocalDate.now();
        LocalTime horaInicio = fechaHora != null ? fechaHora.toLocalTime() : LocalTime.now();

        String lugarTexto = getVal(dataMap, "lugar_cita");
        Integer lugarCita = mapearDesarrolloInteres(lugarTexto);
        if (lugarCita == null) {
            lugarCita = 1;
        }

        return ProspectoRegistrarCitaRequest.builder()
                .prospectoId(prospectoId)
                .fechaInicio(fechaInicio)
                .horaInicio(horaInicio)
                .lugarCita(lugarCita)
                .nota(getVal(dataMap, "nota"))
                .build();
    }

    private Integer mapearDesarrolloInteres(String texto) {
        if (texto == null || texto.isBlank()) return null;
        String norm = normalizarTexto(texto);

        if (norm.contains("coral cuernavaca")) return 1;
        if (norm.contains("coral santa maria")) return 2;
        if (norm.contains("coral golf")) return 6;
        if (norm.contains("fitur")) return 907;
        if (norm.contains("eventos santa maria")) return 2;
        if (norm.contains("eventos cuernavaca")) return 1;
        if (norm.contains("eventos golf")) return 6;
        if (norm.contains("desarrollo de playas") || norm.contains("playas")) return 0;

        return null;
    }

    private Integer mapearAreaInteres(String desarrolloTexto, String greenFees) {
        if (desarrolloTexto == null || desarrolloTexto.isBlank()) return null;
        String norm = normalizarTexto(desarrolloTexto);

        if (norm.contains("coral golf") && greenFees != null && !greenFees.isBlank()) {
            return 3754;
        }
        if (norm.contains("eventos santa maria") || norm.contains("eventos cuernavaca") || norm.contains("eventos golf")) {
            return 3751;
        }
        if (norm.contains("coral cuernavaca") || norm.contains("coral santa maria") || norm.contains("coral golf")) {
            return 3750;
        }
        if (norm.contains("desarrollo de playas") || norm.contains("playas")) {
            return 3752;
        }
        if (norm.contains("fitur")) {
            return 3755;
        }

        return null;
    }

    private LocalDateTime parsearFechaHora(String str) {
        if (str == null || str.isBlank()) return null;
        String trimmed = str.trim();

        DateTimeFormatter[] formatters = new DateTimeFormatter[]{
                DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
                DateTimeFormatter.ISO_DATE_TIME
        };

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDateTime.parse(trimmed, formatter);
            } catch (Exception ignored) {}
        }

        DateTimeFormatter[] dateOnlyFormatters = new DateTimeFormatter[]{
                DateTimeFormatter.ofPattern("MM-dd-yyyy"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                DateTimeFormatter.ISO_LOCAL_DATE
        };

        for (DateTimeFormatter formatter : dateOnlyFormatters) {
            try {
                LocalDate d = LocalDate.parse(trimmed, formatter);
                return d.atStartOfDay();
            } catch (Exception ignored) {}
        }

        return null;
    }

    private Map<String, Object> obtenerSoloDataAdicional(Map<String, Object> dataMap) {
        Map<String, Object> adicionales = new HashMap<>();
        for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
            if (!CAMPOS_CONSUMIDOS.contains(entry.getKey().toLowerCase().trim())) {
                adicionales.put(entry.getKey(), entry.getValue());
            }
        }
        return adicionales;
    }

    private Map<String, Object> consolidarDatos(ZohoWebhookPayload payload, Map<String, String> rawParams) {
        Map<String, Object> map = new HashMap<>();
        if (rawParams != null) map.putAll(rawParams);
        if (payload != null) {
            if (payload.id() != null) map.put("id", payload.id());
            if (payload.entityId() != null) map.put("entity_id", payload.entityId());
            if (payload.data() != null) map.putAll(payload.data());
        }
        return map;
    }

    private String[] separarNombres(String texto) {
        if (texto == null || texto.isBlank()) return new String[]{"N/A", null};
        String[] partes = texto.trim().split("\\s+", 2);
        String primero = partes[0];
        String segundo = partes.length > 1 ? partes[1] : null;
        return new String[]{primero, segundo};
    }

    private String normalizarTexto(String texto) {
        String nfd = Normalizer.normalize(texto, Normalizer.Form.NFD);
        return nfd.replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toLowerCase().trim();
    }

    private String getVal(Map<String, Object> map, String key) {
        if (map.containsKey(key) && map.get(key) != null) {
            String val = String.valueOf(map.get(key)).trim();
            if (!val.isBlank() && !val.equalsIgnoreCase("null")) {
                return val;
            }
        }
        return null;
    }

    private Integer getValInteger(Map<String, Object> map, String key) {
        String str = getVal(map, key);
        if (str != null) {
            try {
                return Integer.parseInt(str.replaceAll("[^0-9]", ""));
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    private String convertirAJsonString(Map<String, Object> dataMap) {
        try {
            return objectMapper.writeValueAsString(dataMap);
        } catch (Exception e) {
            log.warn("[ZOHO ADAPTER] No se pudo serializar data_adicional a JSON: {}", e.getMessage());
            return dataMap.toString();
        }
    }
}
