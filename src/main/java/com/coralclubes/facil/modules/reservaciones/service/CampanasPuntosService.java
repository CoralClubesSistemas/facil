    package com.coralclubes.facil.modules.reservaciones.service;

    import com.coralclubes.facil.modules.reservaciones.dto.request.CampanaPuntosRequest;
    import com.coralclubes.facil.modules.reservaciones.dto.response.CampanaPuntosResponse;
    import com.coralclubes.facil.modules.reservaciones.dto.response.OpcionPagoPuntosDto;
    import com.coralclubes.facil.modules.reservaciones.repository.CampanasPuntosRepository;
    import com.coralclubes.facil.shared.infrastructure.integration.storage.StorageClient;
    import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.RespuestaCargaDto;
    import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.SolicitarUrlImagenRequest;
    import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.SolicitudCargaDto;
    import com.coralclubes.facil.shared.infrastructure.security.service.UserContext;
    import com.coralclubes.responses.ApiResponse;
    import lombok.RequiredArgsConstructor;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.stereotype.Service;

    import java.util.List;
    import java.util.Map;
    import java.util.UUID;

    @Service
    @RequiredArgsConstructor
    public class CampanasPuntosService {

        private final CampanasPuntosRepository repository;
        private final UserContext userContext;
        private final StorageClient storageClient;

        @Value("${app.clients.storage.aliases.default}")
        private String aliasConfiguracion;

        private static final String STORAGE_FOLDER = "reservaciones/promociones";

        public ApiResponse<List<CampanaPuntosResponse>> obtenerCampanas() {
            List<CampanaPuntosResponse> campanas = repository.obtenerCampanasPuntos();

            // Resolución del UUID a URL Pública/Firmada
            campanas.forEach(campana -> {
                if (campana.getImagenUuid() != null && !campana.getImagenUuid().isBlank()) {
                    try {
                        String url = storageClient.obtenerUrlDescarga(UUID.fromString(campana.getImagenUuid()));
                        campana.setImagenUrl(url);
                    } catch (IllegalArgumentException e) {
                        campana.setImagenUrl(null);
                    }
                }
            });

            return ApiResponse.success("Campañas de Puntos obtenidas correctamente.", campanas);
        }

        public ApiResponse<Integer> guardarCampana(CampanaPuntosRequest request) {
            String usuario = userContext.getUsername();
            Integer idGenerado = repository.guardarCampanaPuntos(request, usuario)
                    .orElseThrow(() -> new RuntimeException("Error al guardar la campaña de puntos."));

            return ApiResponse.success("Campaña de Puntos guardada exitosamente.", idGenerado);
        }

        public ApiResponse<Boolean> eliminarCampana(Integer idPromocion) {
            String usuario = userContext.getUsername();
            repository.eliminarCampanaPuntos(idPromocion, usuario);
            return ApiResponse.success("Campaña dada de baja exitosamente.", true);
        }

        /**
         * Negocia con el microservicio de Storage una URL prefirmada para que
         * el Frontend suba la imagen de la promoción directamente al Bucket (S3/MinIO).
         */
        public ApiResponse<RespuestaCargaDto> solicitarUrlCarga(SolicitarUrlImagenRequest request) {
            String usuario = userContext.getUsername();

            Map<String, String> metadata = Map.of("modulo", "RESERVACIONES - PROMOCIONES - PUNTOS", "subidoPor", usuario);

            SolicitudCargaDto solicitud = SolicitudCargaDto.builder()
                    .nombreArchivo(request.nombreArchivo())
                    .contentType(request.contentType())
                    .tamanoBytes(request.tamanoBytes())
                    .aliasConfiguracion(aliasConfiguracion)
                    .esPublico(true)
                    .rutaLogica(STORAGE_FOLDER)
                    .metadatos(metadata)
                    .build();

            RespuestaCargaDto respuesta = storageClient.solicitarUrlCarga(solicitud);
            return ApiResponse.success("URL de carga generada exitosamente", respuesta);
        }


        public List<OpcionPagoPuntosDto> evaluarPromocionesCarrito(UUID groupId) {
            return repository.evaluarPromocionesPuntosCarrito(groupId);
        }
    }