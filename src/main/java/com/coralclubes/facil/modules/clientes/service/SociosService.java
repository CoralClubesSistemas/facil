package com.coralclubes.facil.modules.clientes.service;

import com.coralclubes.facil.modules.clientes.dto.projection.InformacionSocioDb;
import com.coralclubes.facil.modules.clientes.dto.response.DatosSocioResponse;
import com.coralclubes.facil.modules.clientes.dto.response.InformacionSocio;
import com.coralclubes.facil.modules.clientes.dto.response.InformacionSocioBusqueda;
import com.coralclubes.facil.modules.clientes.dto.response.InformacionSocioPortales;
import com.coralclubes.facil.modules.clientes.dto.response.InformacionSocioTabla;
import com.coralclubes.facil.modules.clientes.repository.SociosRepository;
import com.coralclubes.facil.shared.infrastructure.integration.banco.service.BbvaService;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SociosService {
    private final SociosRepository repo;
    private final BbvaService bbvaService;

    @Value("${app.banco.cie.bbva}")
    private String bankNumberCie;

    public DatosSocioResponse obtenerDatosSocio(String membresia) {
        DatosSocioResponse socio = repo.spMembresiaObtenerDatosSocio(membresia)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró información para la membresía: " + membresia));
        return socio;
    }

    public ApiResponse<InformacionSocio> obtenerSocios(String membresia) {
        InformacionSocioDb socio = repo.spClientesObtenerDatosSocio(membresia)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró información para la membresía: " + membresia));

        InformacionSocio socioFinal = InformacionSocio.builder()
                .membresia(socio.membresia())
                .nombreCompleto(socio.nombreCompleto())
                .nombre(socio.nombre())
                .segundoNombre(socio.segundoNombre())
                .apellidoPaterno(socio.apellidoPaterno())
                .apellidoMaterno(socio.apellidoMaterno())
                .correo(socio.correo())
                .correoAlternativo(socio.correoAlternativo())
                .telefono(socio.telefono())
                .telefonoAlternativo(socio.telefonoAlternativo())
                .fechaNacimiento(socio.fechaNacimiento())
                .tipoMembresiaId(socio.tipoMembresiaId())
                .tipoMembresia(socio.tipoMembresia())
                .clasificacionMembresiaId(socio.clasificacionMembresiaId())
                .clasificacionMembresia(socio.clasificacionMembresia())
                .desarrolloId(socio.desarrolloId())
                .desarrollo(socio.desarrollo())
                .estatusMembresiaId(socio.estatusMembresiaId())
                .estatusMembresia(socio.estatusMembresia())
                .carteraCobranzaId(socio.carteraCobranzaId())
                .carteraCobranza(socio.carteraCobranza())
                .vigenciaOriginal(socio.vigenciaOriginal())
                .vigenciaRestante(socio.vigenciaRestante())
                .alertaConsultaActiva(socio.alertaConsultaActiva())
                .alertaConsultaConsecutivo(socio.alertaConsultaConsecutivo())
                .alertaConsultaNota(socio.alertaConsultaNota())
                .alertaConsultaFechaRegistro(socio.alertaConsultaFechaRegistro())
                .convenioCie(obtenerConvenioCIECadena(socio.membresia()))
                .direccion(socio.direccion())
                .build();

        return ApiResponse.success("Socio obtenido exitosamente", socioFinal);
    }

    public ApiResponse<InformacionSocioPortales> obtenerSocioPortales(String membresia) {
        InformacionSocioDb socio = repo.spClientesObtenerDatosSocio(membresia)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró información para la membresía: " + membresia));

        InformacionSocioPortales socioFinal = InformacionSocioPortales.builder()
                .membresia(socio.membresia())
                .nombreCompleto(socio.nombreCompleto())
                .nombre(socio.nombre())
                .segundoNombre(socio.segundoNombre())
                .apellidoPaterno(socio.apellidoPaterno())
                .apellidoMaterno(socio.apellidoMaterno())
                .correo(socio.correo())
                .correoAlternativo(socio.correoAlternativo())
                .telefono(socio.telefono())
                .telefonoAlternativo(socio.telefonoAlternativo())
                .fechaNacimiento(socio.fechaNacimiento())
                .build();

        return ApiResponse.success("Socio obtenido exitosamente", socioFinal);
    }

    public ApiResponse<List<InformacionSocioBusqueda>> obtenerSociosBusquedaRapida(String busqueda) {
        return ApiResponse.success("Socios obtenidos exitosamente", repo.spClientesBusquedaInteligente(busqueda));
    }

    public ApiResponse<List<InformacionSocioTabla>> obtenerSociosPorFiltros(
            String membresia,
            String nombre,
            String nombreBeneficiario,
            Integer desarrolloId,
            Integer tipoMembresiaId,
            Integer clasificacionMembresiaId,
            Integer carteraCobranzaId,
            Integer estatusMembresiaId,
            String email,
            String telefono,
            Integer pagina,
            Integer tamanioPagina
    ) {
        List<InformacionSocioTabla> socios = repo.spFacilBusquedaPorFiltros(
                membresia,
                nombre,
                nombreBeneficiario,
                desarrolloId,
                tipoMembresiaId,
                clasificacionMembresiaId,
                carteraCobranzaId,
                estatusMembresiaId,
                email,
                telefono,
                pagina,
                tamanioPagina
        );

        return ApiResponse.success("Socios obtenidos por filtros exitosamente", socios);
    }

    public String calcularCIE(String membresia) {
        if (membresia == null || membresia.isBlank()) {
            throw new IllegalArgumentException("La membresía no puede ser nula o vacía");
        }

        String[] partes = membresia.split("-");
        if (partes.length != 3) {
            throw new IllegalArgumentException("Formato inválido. Se espera: puntoVenta-membresia-desarrollo");
        }

        String puntoVenta = partes[0];
        String membresiaId = partes[1];
        String desarrollo = partes[2];

        if (!esNumerico(puntoVenta) || !esNumerico(membresiaId) || !esNumerico(desarrollo)) {
            throw new IllegalArgumentException("Todos los componentes deben ser numéricos");
        }

        String parcial = construirReferenciaParcial(puntoVenta, membresiaId, desarrollo);

        if (parcial.length() != 10 && parcial.length() != 12) {
            throw new IllegalStateException("Longitud inesperada para referencia CIE: " + parcial.length());
        }

        String digitoVerificador = bbvaService.calcularDigitoVerificador(parcial);

        return parcial + digitoVerificador;
    }

    public String getBankNumberCie() {
        return bankNumberCie;
    }

    public String obtenerConvenioCIECadena(String membresia) {
        String cie = calcularCIE(membresia);
        return bankNumberCie + "  REF " + cie;
    }

    private String construirReferenciaParcial(String puntoVenta, String membresiaId, String desarrollo) {
        String desarrolloFmt = String.format("%02d", Integer.parseInt(desarrollo));
        String membresiaFmt = String.format("%06d", Integer.parseInt(membresiaId));
        String puntoVentaFmt = String.format("%02d", Integer.parseInt(puntoVenta));

        return desarrolloFmt + membresiaFmt + puntoVentaFmt;
    }

    private boolean esNumerico(String valor) {
        return valor != null && valor.matches("\\d+");
    }
}
