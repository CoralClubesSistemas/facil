package com.coralclubes.facil.modules.reservaciones.service;

import com.coralclubes.facil.modules.reservaciones.dto.request.CambiarEstatusTareaRequest;
import com.coralclubes.facil.modules.reservaciones.dto.request.ConsumoRealDto;
import com.coralclubes.facil.modules.reservaciones.dto.request.FinalizarTareaRequest;
import com.coralclubes.facil.modules.reservaciones.dto.request.GuardarCamaristaRequest;
import com.coralclubes.facil.modules.reservaciones.dto.response.CamaristaDto;
import com.coralclubes.facil.modules.reservaciones.dto.response.InventarioBodegaDto;
import com.coralclubes.facil.modules.reservaciones.dto.response.SugerenciaAmenidadDto;
import com.coralclubes.facil.modules.reservaciones.dto.response.TareaDashboardDto;
import com.coralclubes.facil.modules.reservaciones.repository.AmaDeLlavesRepository;
import com.coralclubes.facil.shared.infrastructure.notificaciones.application.dto.PeticionNotificacionDto;
import com.coralclubes.facil.shared.infrastructure.notificaciones.application.service.NotificacionEmisorService;
import com.coralclubes.facil.shared.infrastructure.security.enums.ClavesModulos;
import com.coralclubes.facil.shared.infrastructure.security.service.SeguridadService;
import com.coralclubes.facil.shared.infrastructure.security.service.UserContext;
import com.coralclubes.responses.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AmaDeLlavesService {

    private final AmaDeLlavesRepository repository;
    private final UserContext userContext;
    private final ObjectMapper objectMapper;

    private final NotificacionEmisorService notificacionEmisor;
    private final SeguridadService seguridadService;

    public ApiResponse<List<CamaristaDto>> obtenerCamaristas() {
        Integer idDesarrollo = userContext.getIdDesarrollo();

        List<CamaristaDto> lista = repository.obtenerCamaristas(idDesarrollo);
        return ApiResponse.success("Lista de personal obtenida", lista);
    }

    public ApiResponse<Boolean> guardarCamarista(GuardarCamaristaRequest request) {
        String usuario = userContext.getUsername();
        Integer idDesarrollo = userContext.getIdDesarrollo();

        repository.guardarCamarista(request.idCamarista(), request.nombre(), idDesarrollo, usuario);
        return ApiResponse.success("Personal registrado correctamente", true);
    }

    public ApiResponse<Boolean> desactivarCamarista(Integer idCamarista) {
        String usuario = userContext.getUsername();
        repository.desactivarCamarista(idCamarista, usuario);
        return ApiResponse.success("Personal dado de baja correctamente", true);
    }

    public ApiResponse<List<TareaDashboardDto>> obtenerTareasDashboard() {
        Integer idDesarrollo = userContext.getIdDesarrollo();

        List<TareaDashboardDto> tareas = repository.obtenerTareasDashboard(idDesarrollo);
        return ApiResponse.success("Dashboard cargado", tareas);
    }

    public ApiResponse<Boolean> cambiarEstatusTarea(Integer idTarea, CambiarEstatusTareaRequest request) {
        String usuario = userContext.getUsername();
        repository.cambiarEstatusTarea(idTarea, request.nuevoEstatus(), request.idCamarista(), usuario);
        return ApiResponse.success("Estatus actualizado", true);
    }

    public ApiResponse<List<SugerenciaAmenidadDto>> obtenerSugerencias(Integer idTarea) {
        List<SugerenciaAmenidadDto> sugerencias = repository.obtenerSugerenciaAmenidades(idTarea);
        return ApiResponse.success("Sugerencias generadas", sugerencias);
    }

    public ApiResponse<List<InventarioBodegaDto>> obtenerInventarioBodega() {
        Integer idDesarrollo = userContext.getIdDesarrollo();

        List<InventarioBodegaDto> inventario = repository.obtenerInventarioBodega(idDesarrollo);
        return ApiResponse.success("Inventario consultado", inventario);
    }

    public ApiResponse<Boolean> finalizarTarea(Integer idTarea, FinalizarTareaRequest request) {
            String usuario = userContext.getUsername();
            // Convertimos la lista de consumos a JSON String para SQL Server
            String jsonConsumos = convertirConsumosAString(request.consumos());

            repository.finalizarTarea(idTarea, request.idAlmacenOrigen(), jsonConsumos, usuario);

            return ApiResponse.success("Tarea finalizada, inventario actualizado y cuarto liberado", true);
    }

    private String convertirConsumosAString(List<ConsumoRealDto> consumos) {
        try {
        return objectMapper.writeValueAsString(consumos);
        } catch (JsonProcessingException e) {
            log.error("Error al convertir consumos a JSON: {}", e.getMessage());
            return "[]";
        }
    }

    /**
     * Crea la tarea de limpieza en BD y dispara la notificación a las Master en
     * turno.
     */
    public void crearTareaYNotificar(Integer idUnidadFisica, String numeroHabitacion, Integer idDesarrollo,
            String usuarioQueLibera, String origenAccion) {
        try {
            // 1. Creamos la orden de trabajo en Base de Datos (Estatus: Pendiente)
            repository.crearTareaLimpieza(idUnidadFisica, usuarioQueLibera, origenAccion);

            // 2. Buscamos a quién avisarle.
            // Obtenemos los usernames de todos los usuarios con permiso de Ama de Llaves en
            // ese desarrollo
            List<String> usernamesMasters = seguridadService
                    .obtenerUsernamesPorPermisoYDesarrollo(ClavesModulos.AMADELLAVES.getClave(), idDesarrollo);

            if (usernamesMasters != null && !usernamesMasters.isEmpty()) {

                // 3. Armamos el Payload Dinámico (Metadata) para que Angular sepa qué hacer al
                // hacer clic
                Map<String, Object> metadata = Map.of(
                        "accion", "NUEVA_TAREA_LIMPIEZA",
                        "idUnidadFisica", idUnidadFisica,
                        "origen", origenAccion,
                        "urlDestino", "/app/reservaciones/ama-de-llaves" // Para que Angular navegue ahí al hacer clic
                );

                // 4. Construimos el DTO
                String titulo = "Habitación " + (numeroHabitacion != null ? numeroHabitacion : "Liberada");
                String mensaje = "Se ha liberado por " + origenAccion + " y requiere asignación de limpieza.";

                PeticionNotificacionDto peticion = new PeticionNotificacionDto(
                        "ALERTA_HOUSEKEEPING",
                        3,
                        titulo,
                        mensaje,
                        metadata);

                // 5. Disparamos la notificación masiva persistente
                // 'SISTEMA' será el remitente
                notificacionEmisor.enviarAMultiples("SISTEMA", usernamesMasters, peticion);
            } else {
                log.warn(
                        "Se liberó la unidad {}, pero no hay Camaristas Master activas para notificar en el desarrollo {}.",
                        idUnidadFisica, idDesarrollo);
            }

        } catch (Exception e) {
            log.error("Error al crear tarea de limpieza o notificar para la unidad {}: {}", idUnidadFisica,
                    e.getMessage());
        }
    }
}