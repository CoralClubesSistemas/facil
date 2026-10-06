package com.coralclubes.facil.shared.infrastructure.integration.filesanalysis.service;

import com.coralclubes.facil.shared.infrastructure.integration.filesanalysis.dto.AnalisisArchivoSolicitud;
import com.coralclubes.facil.shared.infrastructure.integration.filesanalysis.dto.MotorAnalisis;
import com.coralclubes.facil.shared.infrastructure.integration.filesanalysis.dto.ResultadoAnalisis;
import com.coralclubes.facil.shared.infrastructure.integration.filesanalysis.extractor.BedrockVisionExtractor;
import com.coralclubes.facil.shared.infrastructure.integration.filesanalysis.extractor.DigitalPdfExtractor;
import com.coralclubes.facil.shared.infrastructure.integration.filesanalysis.extractor.PdfToImageConverter;
import com.coralclubes.facil.shared.infrastructure.integration.storage.StorageClient;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.InfoArchivoDto;
import com.coralclubes.logging.BusinessLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilesAnalysisServiceTest {

    @Mock
    private StorageClient storageClient;

    @Mock
    private DigitalPdfExtractor digitalPdfExtractor;

    @Mock
    private PdfToImageConverter pdfToImageConverter;

    @Mock
    private BedrockVisionExtractor bedrockVisionExtractor;

    @Mock
    private BusinessLogger businessLogger;

    @InjectMocks
    private FilesAnalysisService service;

    private UUID fileId;
    private byte[] sampleBytes;

    record TestDto(String banco, String folio) {}

    @BeforeEach
    void setUp() {
        fileId = UUID.randomUUID();
        sampleBytes = "dummy-content".getBytes();
    }

    @Test
    @DisplayName("Debe utilizar DIGITAL_PDF cuando la extracción directa es exitosa")
    void testProcesarPdfDigitalExitoso() {
        InfoArchivoDto info = new InfoArchivoDto(
                fileId, "comprobante.pdf", "pdf", "application/pdf", 1024L,
                "DISPONIBLE", false, "https://storage/download/url", null
        );

        when(storageClient.consultarArchivo(fileId)).thenReturn(info);
        when(storageClient.descargarArchivo(info.urlDescarga())).thenReturn(sampleBytes);
        when(digitalPdfExtractor.extraerTexto(sampleBytes)).thenReturn(Optional.of("Texto plano bancario"));

        TestDto expected = new TestDto("BBVA", "12345");
        AnalisisArchivoSolicitud<TestDto> solicitud = new AnalisisArchivoSolicitud<>(
                fileId,
                TestDto.class,
                "Instrucciones de prueba",
                texto -> Optional.of(expected)
        );

        ResultadoAnalisis<TestDto> resultado = service.analizar(solicitud);

        assertNotNull(resultado);
        assertEquals(MotorAnalisis.DIGITAL_PDF, resultado.motorUsado());
        assertEquals("BBVA", resultado.datos().banco());
        verify(bedrockVisionExtractor, never()).extraerDesdeImagen(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Debe activar fallback a Bedrock cuando la extracción digital falla")
    void testProcesarPdfFallbackBedrock() {
        InfoArchivoDto info = new InfoArchivoDto(
                fileId, "comprobante.pdf", "pdf", "application/pdf", 1024L,
                "DISPONIBLE", false, "https://storage/download/url", null
        );

        byte[] fakeImageBytes = "image-bytes".getBytes();
        TestDto aiExpected = new TestDto("SANTANDER", "67890");

        when(storageClient.consultarArchivo(fileId)).thenReturn(info);
        when(storageClient.descargarArchivo(info.urlDescarga())).thenReturn(sampleBytes);
        when(digitalPdfExtractor.extraerTexto(sampleBytes)).thenReturn(Optional.of("Texto insuficiente"));
        when(pdfToImageConverter.convertirPrimeraPaginaAImagen(sampleBytes)).thenReturn(Optional.of(fakeImageBytes));
        when(bedrockVisionExtractor.extraerDesdeImagen(eq(fakeImageBytes), eq("image/png"), eq(TestDto.class), any()))
                .thenReturn(aiExpected);

        AnalisisArchivoSolicitud<TestDto> solicitud = new AnalisisArchivoSolicitud<>(
                fileId,
                TestDto.class,
                "Instrucciones de prueba",
                texto -> Optional.empty() // Falla intencionalmente la extracción directa
        );

        ResultadoAnalisis<TestDto> resultado = service.analizar(solicitud);

        assertNotNull(resultado);
        assertEquals(MotorAnalisis.BEDROCK_AI, resultado.motorUsado());
        assertEquals("SANTANDER", resultado.datos().banco());
        verify(bedrockVisionExtractor, times(1)).extraerDesdeImagen(eq(fakeImageBytes), eq("image/png"), eq(TestDto.class), any());
    }

    @Test
    @DisplayName("Debe enviar directamente a Bedrock cuando el contentType es imagen")
    void testProcesarImagenDirectoABedrock() {
        InfoArchivoDto info = new InfoArchivoDto(
                fileId, "ticket.jpg", "jpg", "image/jpeg", 2048L,
                "DISPONIBLE", false, "https://storage/download/url", null
        );

        TestDto aiExpected = new TestDto("BANORTE", "99999");

        when(storageClient.consultarArchivo(fileId)).thenReturn(info);
        when(storageClient.descargarArchivo(info.urlDescarga())).thenReturn(sampleBytes);
        when(bedrockVisionExtractor.extraerDesdeImagen(eq(sampleBytes), eq("image/jpeg"), eq(TestDto.class), any()))
                .thenReturn(aiExpected);

        AnalisisArchivoSolicitud<TestDto> solicitud = new AnalisisArchivoSolicitud<>(
                fileId,
                TestDto.class,
                "Instrucciones de prueba"
        );

        ResultadoAnalisis<TestDto> resultado = service.analizar(solicitud);

        assertNotNull(resultado);
        assertEquals(MotorAnalisis.BEDROCK_AI, resultado.motorUsado());
        assertEquals("BANORTE", resultado.datos().banco());
        verify(digitalPdfExtractor, never()).extraerTexto(any());
        verify(bedrockVisionExtractor, times(1)).extraerDesdeImagen(eq(sampleBytes), eq("image/jpeg"), eq(TestDto.class), any());
    }
}
