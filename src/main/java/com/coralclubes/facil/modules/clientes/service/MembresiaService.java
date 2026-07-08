package com.coralclubes.facil.modules.clientes.service;

import com.coralclubes.facil.modules.clientes.dto.response.BeneficiarioDto;
import com.coralclubes.facil.modules.clientes.dto.response.BeneficiarioPdfItemDto;
import com.coralclubes.facil.modules.clientes.dto.response.DatosReporteBeneficiariosDto;
import com.coralclubes.facil.modules.clientes.dto.response.InformacionSocio;
import com.coralclubes.facil.modules.clientes.dto.response.MembresiaDatosDto;
import com.coralclubes.facil.modules.clientes.repository.MembresiaRepository;
import com.coralclubes.facil.modules.cobranza.service.CobranzaGeneradorDocumentosService;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MembresiaService {

    private final MembresiaRepository repository;
    private final SociosService sociosService;
    private final CobranzaGeneradorDocumentosService generadorDocumentosService;

    public Optional<MembresiaDatosDto> obtenerDatosMembresia(String membresia, Integer plan) {
        return repository.spCobranzaOntenerDatosMembresia(membresia, plan);
    }

    public List<BeneficiarioDto> obtenerBeneficiariosMembresia(String membresia) {
        return repository.spClienteObtenerBeneficiariosMembresia(membresia);
    }

    public byte[] generarPdfReporteBeneficiarios(String membresia) {
        // 1. Consultar información del socio
        ApiResponse<InformacionSocio> apiResponseSocio = sociosService.obtenerSocios(membresia);
        InformacionSocio socio = apiResponseSocio != null ? apiResponseSocio.data() : null;
        if (socio == null) {
            throw new IllegalArgumentException("No se encontró información para el socio con membresía: " + membresia);
        }

        // 2. Consultar beneficiarios
        List<BeneficiarioDto> beneficiarios = repository.spClienteObtenerBeneficiariosMembresia(membresia);

        // 3. Formatear beneficiarios para el PDF
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        List<BeneficiarioPdfItemDto> pdfItems = new ArrayList<>();
        for (var b : beneficiarios) {
            String fNacimiento = b.fechaNacimiento() != null ? b.fechaNacimiento().format(dateFormatter) : "";
            pdfItems.add(BeneficiarioPdfItemDto.builder()
                    .id(b.numeroBeneficiario() != null ? b.numeroBeneficiario().toString() : "")
                    .nombre(b.nombreCompleto() != null ? b.nombreCompleto() : "")
                    .fechaNacimiento(fNacimiento)
                    .estadoCivil(b.estadoCivil() != null ? b.estadoCivil() : "")
                    .parentesco(b.parentesco() != null ? b.parentesco() : "")
                    .build());
        }

        // 4. Construir DTO consolidado
        LocalDate today = LocalDate.now();
        String fechaEmision = today.format(dateFormatter);

        DatosReporteBeneficiariosDto datos = DatosReporteBeneficiariosDto.builder()
                .razonSocial("CORAL CLUBES")
                .slogan("CREANDO MOMENTOS INOLVIDABLES")
                .fechaEmision(fechaEmision)
                .membresia(socio.membresia())
                .clasificacionMembresia(socio.clasificacionMembresia())
                .tipoMembresia(socio.tipoMembresia())
                .desarrollo(socio.desarrollo())
                .direccionMembresia(socio.direccion())
                .beneficiarios(pdfItems)
                .build();

        // 5. Generar PDF
        return generadorDocumentosService.generarPdfReporteBeneficiarios(datos);
    }
}
