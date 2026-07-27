package com.coralclubes.facil.modules.cobranza.service;

import com.coralclubes.dto.SelectGenerico;
import com.coralclubes.facil.modules.cobranza.dto.request.GuardarCuponRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.CuponDetalleResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.CuponListadoResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.CuponesCatalogoElementoResponse;
import com.coralclubes.facil.modules.cobranza.repository.CuponesRepository;
import com.coralclubes.facil.modules.reservaciones.dto.response.TipoUnidadDetalleDto;
import com.coralclubes.facil.shared.domain.dto.ArchivoDescarga;
import com.coralclubes.facil.shared.infrastructure.exceptions.custom.ServiceUnavailableException;
import com.coralclubes.facil.shared.infrastructure.integration.storage.StorageClient;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.RespuestaCargaDto;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.SolicitarUrlRequest;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.SolicitudCargaDto;
import com.coralclubes.logging.BusinessLogger;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CuponesService {

    private final CuponesRepository repository;
    private final StorageClient storageClient;
    private final BusinessLogger logger;

    @Value("${app.clients.storage.aliases.default}")
    private String aliasStorageDefault;

    public List<CuponesCatalogoElementoResponse> obtenerCatalogoCondiciones() {
        return repository.spCuponesCatalogoCondiciones();
    }

    public List<CuponesCatalogoElementoResponse> obtenerCatalogoBeneficios() {
        return repository.spCuponesCatalogoBeneficios();
    }

    public List<SelectGenerico<Integer>> obtenerCatalogoOrigenes() {
        return repository.spCuponesCatalogoOrigenes();
    }

    public List<SelectGenerico<Integer>> obtenerCatalogoDestinos() {
        return repository.spCuponesCatalogoDestinos();
    }

    public Integer guardarCupon(GuardarCuponRequest request, String usuario) {
        logger.info(usuario,"Guardando cupón con nombre: {} por usuario: {}", request.nombre(), usuario);

        return repository.spCuponesGuardarCupon(request, usuario)
                .orElseThrow(() -> new RuntimeException("No se pudo guardar el cupón"));
    }

    public List<CuponListadoResponse> obtenerListadoCupones(
            Integer year,
            Integer desarrollo,
            Integer origen,
            Integer destino
    ) {
        return repository.spCuponesObtenerListadoCupones(year, desarrollo, origen, destino);
    }

    public RespuestaCargaDto solicitarUrlCarga(SolicitarUrlRequest request, String usuario) {
        String year = String.valueOf(LocalDateTime.now().getYear());

        // 2. Construir la ruta lógica
        String rutaLogica = "cobranza/cupones/" + year;

        // 3. Crear payload para el StorageClient
        SolicitudCargaDto solicitudStorage = SolicitudCargaDto.builder()
                .nombreArchivo(request.nombreArchivo())
                .contentType(request.contentType())
                .tamanoBytes(request.tamanoBytes())
                .aliasConfiguracion(aliasStorageDefault)
                .esPublico(false)
                .rutaLogica(rutaLogica)
                .metadatos(Map.of(
                        "modulo", "CUPONES",
                        "idCupon", String.valueOf(request.id()),
                        "subidoPor", usuario
                ))
                .build();

        // 4. Obtener URL del microservicio
        return storageClient.solicitarUrlCarga(solicitudStorage);
    }

    public void guardarImagenCupon(Integer idCupon, String imagen) {
        repository.spCuponesGuardarImagenCupon(idCupon, imagen);
    }

    public ArchivoDescarga obtenerFormatoCupon (UUID uuid) {
        return storageClient.obtenerUrlDescargaYNombre(uuid, "inline");
    }

    public void eliminarCupon(Integer idCupon, String usuario) {
        logger.info(usuario,"Eliminando cupón con ID: {} por usuario: {}", idCupon, usuario);

        repository.spCuponesModificarEstatusCupon(idCupon, false);
    }

    public void reactivarCupon(Integer idCupon, String usuario) {
        logger.info(usuario,"Reactivando cupón con ID: {} por usuario: {}", idCupon, usuario);

        repository.spCuponesModificarEstatusCupon(idCupon, true);
    }

    public CuponDetalleResponse obtenerDetalleCupon(Integer id) {
        return repository.spCuponesObtenerDetalle(id)
                .orElseThrow(() -> new RuntimeException("Cupón no encontrado"));
    }

    public List<CuponListadoResponse> obtenerCuponesDesactivados(Integer year, Integer desarrollo) {
        return repository.spCuponesObtenerCuponesDesactivados(year, desarrollo);
    }
}
