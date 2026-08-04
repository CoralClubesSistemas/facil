package com.coralclubes.facil.modules.clientes.service;

import com.coralclubes.facil.modules.clientes.dto.request.AdicionarCuponesMembresiaRequest;
import com.coralclubes.facil.modules.clientes.dto.request.AsignarCuponesMembresiaRequest;
import com.coralclubes.facil.modules.clientes.dto.request.SintetizarCorreoCuponesRequest;
import com.coralclubes.facil.modules.clientes.dto.response.CuponDisponibleAsignacionResponse;
import com.coralclubes.facil.modules.clientes.dto.response.CuponFormatoInfoResponse;
import com.coralclubes.facil.modules.clientes.dto.response.CuponMembresiaDetalleResponse;
import com.coralclubes.facil.modules.clientes.dto.response.CuponMembresiaResumenResponse;
import com.coralclubes.facil.modules.clientes.repository.CuponesMembresiasRepository;
import com.coralclubes.facil.modules.cobranza.dto.response.CuerpoCorreoResponse;
import com.coralclubes.facil.modules.sistema.service.PlantillasCuerpoCorreoService;
import com.coralclubes.logging.BusinessLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CuponesMembresiasService {

    private final CuponesMembresiasRepository repository;
    private final PlantillasCuerpoCorreoService plantillasService;
    private final SociosService sociosService;
    private final BusinessLogger logger;

    public List<CuponMembresiaResumenResponse> obtenerCuponesMembresia(String membresia, Integer year) {
        return repository.spMembresiaObtenerCupones(membresia, year);
    }

    public List<CuponMembresiaDetalleResponse> obtenerDetalleCuponMembresia(Integer cuponId) {
        return repository.spMembresiaObtenerDetalleCupon(cuponId);
    }

    public List<CuponDisponibleAsignacionResponse> obtenerDisponiblesParaAsignacion(
            String membresia,
            String origen,
            Integer anio
    ) {
        return repository.spCuponesObtenerDisponiblesParaAsignacion(membresia, origen, anio);
    }

    public void asignarCuponesAMembresia(AsignarCuponesMembresiaRequest request, String usuario) {
        logger.info(usuario, "Asignando cupones a membresía: {} con origen: {}", request.membresia(), request.origen());
        repository.spCuponesAsignarAMembresia(request, usuario);
    }

    public void bloquearCuponMembresia(Integer cuponId, Integer folio, String usuario) {
        logger.info(usuario, "Bloqueando folio: {} del cupón ID: {}", folio, cuponId);
        repository.spMembresiaBloquearCupon(cuponId, folio, usuario);
    }

    public void adicionarCuponesMembresia(AdicionarCuponesMembresiaRequest request, String usuario) {
        logger.info(usuario, "Adicionando {} cupones al PQAC_ID: {}", request.cantidad(), request.id());
        repository.spMembresiaAdicionarCupones(request, usuario);
    }

    public List<CuponFormatoInfoResponse> obtenerInfoFormatosCupones(Integer id) {
        return repository.spMembresiaObtenerInfoFormatosCupones(id);
    }

    public CuerpoCorreoResponse sintetizarCuerpoCorreoEnvioCupones(SintetizarCorreoCuponesRequest request) {
        // 1. Obtener la lista completa de cupones asignados a la membresía
        List<CuponMembresiaResumenResponse> cuponesMembresia = obtenerCuponesMembresia(request.membresia(), request.anio());

        // 2. Filtrar únicamente por los IDs recibidos en el request
        List<CuponMembresiaResumenResponse> cuponesFiltrados = cuponesMembresia.stream()
                .filter(c -> c.id() != null && request.ids().contains(c.id()))
                .toList();

        if (cuponesFiltrados.isEmpty()) {
            throw new RuntimeException("No se encontraron cupones coincidentes con los IDs proporcionados para la membresía: " + request.membresia());
        }

        // 3. Consultar datos del socio para la plantilla
        var apiResponseSocio = sociosService.obtenerSocios(request.membresia());
        var socio = apiResponseSocio != null ? apiResponseSocio.data() : null;

        CuponMembresiaResumenResponse primerCupon = cuponesFiltrados.getFirst();

        Map<String, Object> variables = new HashMap<>();
        boolean tieneNombreSocio = socio != null && socio.nombreCompleto() != null && !socio.nombreCompleto().isBlank();
        variables.put("socio", tieneNombreSocio);
        variables.put("nombreSocio", tieneNombreSocio ? socio.nombreCompleto() : "");
        variables.put("membresia", request.membresia());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        variables.put("fechaEnvio", LocalDateTime.now().format(formatter));

        // 4. Mapear los campos nombreCupon y nomenclatura requeridos por la plantilla de correo
        List<Map<String, Object>> cuponesMapList = cuponesFiltrados.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("nombre", c.nombreCupon());
            map.put("nomenclatura", c.nomenclatura());
            return map;
        }).toList();

        variables.put("cupones", cuponesMapList);

        // 5. Sintetizar asunto y cuerpo con Pebble
        String asunto = plantillasService.renderizarAsunto("ENVIO_CUPONES_PDF", variables);
        String cuerpo = plantillasService.renderizarCuerpo("ENVIO_CUPONES_PDF", variables);

        return CuerpoCorreoResponse.builder()
                .asunto(asunto)
                .cuerpo(cuerpo)
                .build();
    }
}
