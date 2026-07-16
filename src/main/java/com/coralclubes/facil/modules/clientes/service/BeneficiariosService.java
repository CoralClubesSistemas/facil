package com.coralclubes.facil.modules.clientes.service;

import com.coralclubes.facil.shared.domain.dto.ArchivoDescarga;
import com.coralclubes.facil.shared.infrastructure.integration.storage.StorageClient;
import com.coralclubes.facil.modules.clientes.dto.response.BeneficiarioDto;
import com.coralclubes.facil.modules.clientes.dto.response.BeneficiarioPdfItemDto;
import com.coralclubes.facil.modules.clientes.dto.response.DatosReporteBeneficiariosDto;
import com.coralclubes.facil.modules.clientes.dto.response.InformacionSocio;
import com.coralclubes.facil.modules.clientes.repository.BeneficiariosRepository;
import com.coralclubes.facil.modules.cobranza.service.CobranzaGeneradorDocumentosService;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BeneficiariosService {

    private final BeneficiariosRepository repository;
    private final SociosService sociosService;
    private final CobranzaGeneradorDocumentosService generadorDocumentosService;
    private final StorageClient storageClient;

    public List<BeneficiarioDto> obtenerBeneficiariosMembresia(String membresia) {
        List<BeneficiarioDto> list = repository.spClienteObtenerBeneficiariosMembresia(membresia);
        return list.stream()
                .map(b -> {
                    String url = null;
                    if (b.uuidCredencial() != null && !b.uuidCredencial().isBlank()) {
                        try {
                            ArchivoDescarga responseDescarga = storageClient.obtenerUrlDescarga(java.util.UUID.fromString(b.uuidCredencial()));
                            if (responseDescarga != null) {
                                url = responseDescarga.urlDescarga();
                            }
                        } catch (Exception e) {
                            // En caso de error, la URL se mantiene null
                        }
                    }
                    return BeneficiarioDto.builder()
                            .numeroBeneficiario(b.numeroBeneficiario())
                            .nombreCompleto(b.nombreCompleto())
                            .fechaNacimiento(b.fechaNacimiento())
                            .edad(b.edad())
                            .fechaRegistro(b.fechaRegistro())
                            .correoElectronico(b.correoElectronico())
                            .genero(b.genero())
                            .parentesco(b.parentesco())
                            .tipoCliente(b.tipoCliente())
                            .estatusCliente(b.estatusCliente())
                            .estadoCivil(b.estadoCivil())
                            .numeroCredencial(b.numeroCredencial())
                            .uuidCredencial(b.uuidCredencial())
                            .anioVigencia(b.anioVigencia())
                            .mesVigencia(b.mesVigencia())
                            .mesVigenciaTexto(b.mesVigencia() != null ? monthToText(b.mesVigencia()) : "")
                            .ultimoMovimiento(b.ultimoMovimiento())
                            .urlImagen(url)
                            .build();
                })
                .toList();
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

    private String monthToText(int month) {
        return switch (month) {
            case 1 -> "Enero";
            case 2 -> "Febrero";
            case 3 -> "Marzo";
            case 4 -> "Abril";
            case 5 -> "Mayo";
            case 6 -> "Junio";
            case 7 -> "Julio";
            case 8 -> "Agosto";
            case 9 -> "Septiembre";
            case 10 -> "Octubre";
            case 11 -> "Noviembre";
            case 12 -> "Diciembre";
            default -> "";
        };
    }
}
