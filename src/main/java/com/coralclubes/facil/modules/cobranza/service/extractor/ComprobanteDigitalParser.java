package com.coralclubes.facil.modules.cobranza.service.extractor;

import com.coralclubes.facil.modules.cobranza.dto.response.ComprobantePagoAnalizadoDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class ComprobanteDigitalParser {

    private static final Pattern PATTERN_MONTO = Pattern.compile(
            "(?i)(?:importe|monto|total|cantidad)[\t :$]*([0-9]{1,3}(?:,[0-9]{3})*(?:\\.[0-9]{2}))"
    );

    private static final Pattern PATTERN_CLAVE_RASTREO = Pattern.compile(
            "(?i)(?:clave\\s+de\\s+rastreo|rastreo)[\t :]*([A-Za-z0-9]{10,40})"
    );

    private static final Pattern PATTERN_REFERENCIA = Pattern.compile(
            "(?i)(?:referencia(?:\\s+num[eé]rica)?|folio|ref\\.?)[\t :]*([0-9]{4,20})"
    );

    private static final Pattern PATTERN_FECHA = Pattern.compile(
            "(?i)(?:fecha(?:\\s+de\\s+operaci[oó]n|\\s+de\\s+aplicaci[oó]n|\\s+de\\s+pago)?[\t :]*)(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}|\\d{4}[/-]\\d{1,2}[/-]\\d{1,2})"
    );

    private static final Pattern PATTERN_HORA = Pattern.compile(
            "(?i)(?:hora)[\t :]*(\\d{1,2}:\\d{2}(?::\\d{2})?)"
    );

    private static final Pattern PATTERN_CUENTA_ORDENANTE = Pattern.compile(
            "(?i)(?:cuenta\\s+ordenante|cuenta\\s+emisora|desde\\s+la\\s+cuenta)[\t :]*([0-9]{10,18})"
    );

    private static final Pattern PATTERN_CUENTA_BENEFICIARIA = Pattern.compile(
            "(?i)(?:cuenta\\s+beneficiaria|cuenta\\s+receptora|cuenta\\s+destino|hacia\\s+la\\s+cuenta)[\t :]*([0-9]{10,18})"
    );

    private static final Pattern PATTERN_CLABE = Pattern.compile(
            "\\b(\\d{18})\\b"
    );

    private static final String[] BANCOS_CONOCIDOS = {
            "BBVA", "BANCOMER", "CITIBANAMEX", "BANAMEX", "SANTANDER", "BANORTE",
            "HSBC", "SCOTIABANK", "INBURSA", "BANCO AZTECA", "STP", "SISTEMA DE TRANSFERENCIAS Y PAGOS",
            "BANCOPPEL", "AFIRME", "BANREGIO", "BAJIO", "MIFEL", "ACTINVER", "NU MEXICO", "SPIN BY OXXO"
    };

    /**
     * Intenta extraer de forma directa los datos estructurados a partir del texto de un PDF digital.
     *
     * @param texto Texto plano extraído del PDF.
     * @return Optional con el DTO si se lograron extraer campos esenciales (monto y rastreo/referencia),
     *         o Optional.empty() para indicar que se requiere análisis multimodal por IA.
     */
    public Optional<ComprobantePagoAnalizadoDto> parsear(String texto) {
        if (texto == null || texto.isBlank()) {
            return Optional.empty();
        }

        BigDecimal monto = extraerMonto(texto);
        String claveRastreo = extraerRegex(PATTERN_CLAVE_RASTREO, texto);
        String referencia = extraerRegex(PATTERN_REFERENCIA, texto);
        String fecha = extraerRegex(PATTERN_FECHA, texto);
        String hora = extraerRegex(PATTERN_HORA, texto);
        String cuentaOrdenante = extraerRegex(PATTERN_CUENTA_ORDENANTE, texto);
        String cuentaBeneficiaria = extraerRegex(PATTERN_CUENTA_BENEFICIARIA, texto);

        if (cuentaBeneficiaria == null) {
            cuentaBeneficiaria = extraerRegex(PATTERN_CLABE, texto);
        }

        String banco = detectarBanco(texto);

        // Para considerar la extracción digital directa exitosa requerimos al menos monto y (clave de rastreo o referencia)
        if (monto == null || (claveRastreo == null && referencia == null)) {
            log.debug("Texto digital no cumple con los campos mínimos requeridos (monto y rastreo/referencia). Requiere IA.");
            return Optional.empty();
        }

        ComprobantePagoAnalizadoDto dto = new ComprobantePagoAnalizadoDto(
                banco,
                null,
                monto,
                fecha,
                hora,
                claveRastreo,
                referencia,
                cuentaOrdenante,
                cuentaBeneficiaria,
                null,
                null,
                "Transferencia identificada por texto digital",
                "TRANSFERENCIA"
        );

        return Optional.of(dto);
    }

    private BigDecimal extraerMonto(String texto) {
        Matcher matcher = PATTERN_MONTO.matcher(texto);
        if (matcher.find()) {
            try {
                String raw = matcher.group(1).replace(",", "");
                return new BigDecimal(raw);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private String extraerRegex(Pattern pattern, String texto) {
        Matcher matcher = pattern.matcher(texto);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    private String detectarBanco(String texto) {
        String upper = texto.toUpperCase();
        for (String b : BANCOS_CONOCIDOS) {
            if (upper.contains(b)) {
                return b;
            }
        }
        return null;
    }
}
