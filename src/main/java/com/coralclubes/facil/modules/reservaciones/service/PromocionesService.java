package com.coralclubes.facil.modules.reservaciones.service;

import com.coralclubes.facil.modules.reservaciones.dto.projection.PromocionListProjection;
import com.coralclubes.facil.modules.reservaciones.dto.request.ConsumoOfertaRequest;
import com.coralclubes.facil.modules.reservaciones.dto.request.EnlazarImagenRequest;
import com.coralclubes.facil.modules.reservaciones.dto.request.PromocionIntegralRequest;
import com.coralclubes.facil.modules.reservaciones.dto.response.Promocion;
import com.coralclubes.facil.modules.reservaciones.dto.response.PromocionListResponse;
import com.coralclubes.facil.modules.reservaciones.repository.PromocionesRepository;
import com.coralclubes.facil.shared.infrastructure.exceptions.custom.ServiceUnavailableException;
import com.coralclubes.facil.shared.infrastructure.integration.storage.StorageClient;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.RespuestaCargaDto;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.SolicitarUrlImagenRequest;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.SolicitudCargaDto;
import com.coralclubes.facil.shared.infrastructure.security.service.UserContext;
import com.coralclubes.responses.ApiResponse;
import com.coralclubes.responses.codes.GeneralResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromocionesService {

    private final PromocionesRepository repository;
    private final UserContext userContext;
    private final StorageClient storageClient;

    private static final String STORAGE_FOLDER = "reservaciones/promociones";

    @Value("${app.clients.storage.aliases.default}")
    private String aliasConfiguracion;

    // =========================================================================
    // MÉTODOS ADMINISTRATIVOS
    // =========================================================================

    /**
     * Obtiene el listado completo y ligero de promociones vigentes.
     * Exclusivo para el DataGrid del panel de administración.
     */
    public ApiResponse<List<PromocionListResponse>> obtenerPromociones() {
        List<PromocionListProjection> promociones = repository.spResvObtenerPromociones();

        List<PromocionListResponse> promocionesResponse = promociones.stream()
                .map(p -> PromocionListResponse.builder()
                        .idPromocion(p.idPromocion())
                        .nombrePromocion(p.nombrePromocion())
                        .codigoPromocion(p.codigoPromocion())
                        .descripcionPromocion(p.descripcionPromocion())
                        .fechaInicio(p.fechaInicio())
                        .fechaFin(p.fechaFin())
                        .stockTotal(p.stockTotal())
                        .stockDisponible(p.stockDisponible())
                        .esPrivada(p.esPrivada())
                        .esGlobal(p.esGlobal())
                        .fechaVisible(p.fechaVisible())
                        // Resolvemos la URL de la imagen si existe un UUID
                        .urlImagen(p.uuidImagen() != null ? storageClient.obtenerUrlDescarga(p.uuidImagen()) : null)
                        .build())
                .toList();

        return ApiResponse.success("Promociones obtenidas correctamente", promocionesResponse);
    }

    public ApiResponse<Integer> guardarPromocion(PromocionIntegralRequest request) {
        String usuario = userContext.getUsername();

        Integer nuevoId = repository.spResvGuardarPromocionIntegral(request, usuario)
                .orElseThrow(() -> new ServiceUnavailableException("No se pudo guardar la promoción. Revise que el código no esté duplicado."));

        return ApiResponse.success("Promoción guardada exitosamente.", nuevoId);
    }

    public ApiResponse<Boolean> eliminarPromocion(Integer idPromocion) {
        String usuario = userContext.getUsername();

        // eliminamos la imagen asociada a la promoción si existe
        UUID uuidImagen = repository.spResvObtenerUuidImagenPromocion(idPromocion)
                .orElse(null);

        if (uuidImagen != null) {
            try {
                storageClient.eliminarArchivo(uuidImagen, true); // true = eliminación fisica definitiva
            } catch (Exception e) {
                log.error("Error al eliminar la imagen de la promoción (UUID: {}): {}", uuidImagen, e.getMessage());
            }
        }

        boolean success = repository.spResvEliminarPromocion(idPromocion, usuario);

        if (!success) {
            throw new ServiceUnavailableException("No se pudo eliminar la promoción.");
        }
        return ApiResponse.success("Promoción eliminada.", true);
    }

    /**
     * Negocia con el microservicio de Storage una URL prefirmada para que
     * el Frontend suba la imagen de la promoción directamente al Bucket (S3/MinIO).
     */
    public ApiResponse<RespuestaCargaDto> solicitarUrlCarga(SolicitarUrlImagenRequest request) {
        String usuario = userContext.getUsername();

        Map<String, String> metadata = Map.of(
                "modulo", "RESERVACIONES - PROMOCIONES",
                "promocion", String.valueOf(request.id()),
                "subidoPor", usuario
        );

        SolicitudCargaDto solicitud = SolicitudCargaDto.builder()
                .nombreArchivo(request.nombreArchivo())
                .contentType(request.contentType())
                .tamanoBytes(request.tamanoBytes())
                .aliasConfiguracion(aliasConfiguracion)
                .esPublico(true) // Las imágenes de promociones son públicas para que el portal las pueda mostrar sin complicaciones
                .rutaLogica(STORAGE_FOLDER)
                .metadatos(metadata)
                .build();

        RespuestaCargaDto respuesta = storageClient.solicitarUrlCarga(solicitud);
        return ApiResponse.success("URL de carga generada exitosamente", respuesta);
    }

    // =========================================================================
    // MÉTODOS PÚBLICOS (PORTALES WEB)
    // =========================================================================

    public ApiResponse<Promocion> validarPromocionPorCodigo(String codigo) {
        Promocion promo = repository.spResvObtenerPromocionPorCodigo(codigo)
                .orElse(null);

        if (promo == null) {
            return ApiResponse.error(GeneralResponseCode.BAD_REQUEST, "El código ingresado no existe, está inactivo o agotado.");
        }

        // Si la promoción tiene imagen, generamos su URL para que el portal público la pueda renderizar en el carrito
        if (promo.uuidImagen() != null) {
            String urlDescarga = storageClient.obtenerUrlDescarga(promo.uuidImagen());
            // Creamos una copia inmutable del record inyectando la URL resuelta
            promo = Promocion.builder()
                    .idPromocion(promo.idPromocion())
                    .nombrePromocion(promo.nombrePromocion())
                    .descripcionPromocion(promo.descripcionPromocion())
                    .codigoPromocion(promo.codigoPromocion())
                    .stockDisponible(promo.stockDisponible())
                    .stockTotal(promo.stockTotal())
                    .fechaInicio(promo.fechaInicio())
                    .fechaFin(promo.fechaFin())
                    .fechaVisible(promo.fechaVisible())
                    .esGlobal(promo.esGlobal())
                    .esPrivada(promo.esPrivada())
                    .uuidImagen(promo.uuidImagen())
                    .urlImagen(urlDescarga) // URL resuelta para el portal
                    .beneficios(promo.beneficios())
                    .reglas(promo.reglas())
                    .build();
        }

        return ApiResponse.success("Código promocional válido.", promo);
    }

    public ApiResponse<Integer> aplicarConsumoOferta(ConsumoOfertaRequest request) {
        String usuarioAplicacion = userContext.getUsername() != null ? userContext.getUsername() : "PORTAL_WEB";

        Integer idAplicacion = repository.spResvDetallarConsumoOferta(request, usuarioAplicacion)
                .orElseThrow(() -> new ServiceUnavailableException("Error al aplicar la promoción. Posible falta de stock de último segundo."));

        return ApiResponse.success("Promoción aplicada exitosamente.", idAplicacion);
    }

    public ApiResponse<Boolean> enlazarImagenPromocion(EnlazarImagenRequest request) {
        String usuario = userContext.getUsername();
        boolean exito = repository.spResvEnlazarImagenPromocion(request.idPromocion(), request.uuidImagen(), usuario);

        if (!exito) {
            throw new ServiceUnavailableException("No se pudo enlazar la imagen a la promoción.");
        }
        return ApiResponse.success("Imagen enlazada correctamente.", true);
    }
}