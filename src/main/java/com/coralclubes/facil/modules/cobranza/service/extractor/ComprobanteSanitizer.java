package com.coralclubes.facil.modules.cobranza.service.extractor;

import com.coralclubes.facil.modules.cobranza.dto.response.ComprobantePagoAnalizadoDto;
import com.coralclubes.facil.modules.cobranza.enums.BancoBanxico;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Sanitiza y normaliza los datos extraídos de un comprobante de pago bancario,
 * aplicando reglas determinísticas financieras, validación CLABE Banxico,
 * descarte de leyendas de pie de página y consistencia de roles ordenante/beneficiario.
 */
@Slf4j
@Component
public class ComprobanteSanitizer {

    private static final List<String> BLACKLIST_PIE_PAGINA = List.of(
            "CONDUSEF", "HOJA", "PAGINA", "PÁGINA", "VERSION", "VERSIÓN",
            "CONSULTA", "LINEA", "LÍNEA", "ACLARACION", "ACLARACIÓN",
            "TELEFONO", "TELÉFONO", "DERECHOS RESERVADOS", "WWW.", "HTTP",
            "IMPUESTO", "COMISION", "COMISIÓN", "IVA", "FOLIO DE IMPRESION"
    );

    private static final Pattern PATTERN_SOLO_DIGITOS = Pattern.compile("^\\d+$");
    private static final Pattern PATTERN_CLABE_18 = Pattern.compile("^\\d{18}$");

    /**
     * Aplica reglas de sanitización al DTO devuelto por IA o extracción digital.
     */
    public ComprobantePagoAnalizadoDto sanitizar(ComprobantePagoAnalizadoDto raw) {
        if (raw == null) {
            return null;
        }

        String bancoEmisor = normalizarTexto(raw.bancoEmisor());
        String bancoReceptor = normalizarTexto(raw.bancoReceptor());
        String cuentaOrdenante = limpiarNumeroCuenta(raw.cuentaOrdenante());
        String cuentaBeneficiaria = limpiarNumeroCuenta(raw.cuentaBeneficiaria());
        String ordenante = normalizarTexto(raw.ordenante());
        String beneficiario = normalizarTexto(raw.beneficiario());
        String claveRastreo = sanitizarClaveRastreo(raw.claveRastreo());
        String referencia = sanitizarReferencia(raw.referencia());
        BigDecimal monto = sanitizarMonto(raw.monto());
        String fecha = normalizarFecha(raw.fechaOperacion());
        String hora = normalizarTexto(raw.horaOperacion());
        String concepto = normalizarTexto(raw.concepto());
        String tipoOperacion = normalizarTipoOperacion(raw.tipoOperacion(), claveRastreo);

        // 1. Corrección infalible de Banco Emisor si cuentaOrdenante es CLABE de 18 dígitos
        if (cuentaOrdenante != null && PATTERN_CLABE_18.matcher(cuentaOrdenante).matches()) {
            BancoBanxico.obtenerNombrePorClabe(cuentaOrdenante)
                    .ifPresent(banco -> log.debug("Banco emisor deducido por CLABE: {}", banco));
            bancoEmisor = BancoBanxico.obtenerNombrePorClabe(cuentaOrdenante).orElse(bancoEmisor);
        }

        // 2. Corrección infalible de Banco Receptor si cuentaBeneficiaria es CLABE de 18 dígitos
        if (cuentaBeneficiaria != null && PATTERN_CLABE_18.matcher(cuentaBeneficiaria).matches()) {
            bancoReceptor = BancoBanxico.obtenerNombrePorClabe(cuentaBeneficiaria).orElse(bancoReceptor);
        }

        // 3. Inversión de roles si están cruzados (en Cobranza el receptor/beneficiario siempre es Coral Clubes)
        if (esCoralClubes(ordenante) && !esCoralClubes(beneficiario)) {
            log.info("Detectada inversión de roles (Coral Clubes figuraba como ordenante). Ajustando a beneficiario.");
            String tempNombre = ordenante;
            ordenante = beneficiario;
            beneficiario = tempNombre;

            String tempCuenta = cuentaOrdenante;
            cuentaOrdenante = cuentaBeneficiaria;
            cuentaBeneficiaria = tempCuenta;

            String tempBanco = bancoEmisor;
            bancoEmisor = bancoReceptor;
            bancoReceptor = tempBanco;
        }

        return new ComprobantePagoAnalizadoDto(
                bancoEmisor,
                bancoReceptor,
                monto,
                fecha,
                hora,
                claveRastreo,
                referencia,
                cuentaOrdenante,
                cuentaBeneficiaria,
                beneficiario,
                ordenante,
                concepto,
                tipoOperacion
        );
    }

    private String sanitizarReferencia(String rawRef) {
        if (rawRef == null || rawRef.isBlank()) {
            return null;
        }
        String ref = rawRef.trim();

        // Si la referencia contiene palabras de pie de página / leyendas legales, descartarla
        String upper = ref.toUpperCase();
        for (String terminoProhibido : BLACKLIST_PIE_PAGINA) {
            if (upper.contains(terminoProhibido)) {
                log.debug("Referencia descartada por contener término de pie de página/legal: '{}' (término: {})", ref, terminoProhibido);
                return null;
            }
        }

        // Descartar si excede una longitud razonable de referencia bancaria (ej. más de 25 caracteres no es una referencia estándar)
        if (ref.length() > 25) {
            log.debug("Referencia descartada por exceder longitud bancaria máxima: '{}'", ref);
            return null;
        }

        // Descartar si contiene muchas palabras con espacios (indica leyenda o texto descriptivo)
        if (ref.contains(" ") && ref.split("\\s+").length > 2) {
            log.debug("Referencia descartada por ser texto descriptivo: '{}'", ref);
            return null;
        }

        return ref;
    }

    private String sanitizarClaveRastreo(String rawClave) {
        if (rawClave == null || rawClave.isBlank()) {
            return null;
        }
        String clave = rawClave.trim();
        String upper = clave.toUpperCase();
        for (String terminoProhibido : BLACKLIST_PIE_PAGINA) {
            if (upper.contains(terminoProhibido)) {
                log.debug("Clave de rastreo descartada por contener término sospechoso: '{}'", clave);
                return null;
            }
        }
        if (clave.length() < 7 || clave.length() > 50) {
            return null;
        }
        return clave;
    }

    private BigDecimal sanitizarMonto(BigDecimal monto) {
        if (monto == null) {
            return null;
        }
        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        return monto.setScale(2, RoundingMode.HALF_UP);
    }

    private String limpiarNumeroCuenta(String rawCuenta) {
        if (rawCuenta == null || rawCuenta.isBlank()) {
            return null;
        }
        String limpia = rawCuenta.replaceAll("[^0-9X*]", "");
        return limpia.isBlank() ? null : limpia;
    }

    private String normalizarFecha(String rawFecha) {
        if (rawFecha == null || rawFecha.isBlank()) {
            return null;
        }
        String fecha = rawFecha.trim();
        // Convertir DD/MM/YYYY o DD-MM-YYYY a YYYY-MM-DD
        if (fecha.matches("^\\d{1,2}[/-]\\d{1,2}[/-]\\d{4}$")) {
            String[] parts = fecha.split("[/-]");
            String dia = String.format("%02d", Integer.parseInt(parts[0]));
            String mes = String.format("%02d", Integer.parseInt(parts[1]));
            String anio = parts[2];
            return anio + "-" + mes + "-" + dia;
        }
        return fecha;
    }

    private String normalizarTipoOperacion(String tipo, String claveRastreo) {
        if (claveRastreo != null && !claveRastreo.isBlank()) {
            return "SPEI";
        }
        if (tipo != null && !tipo.isBlank()) {
            return tipo.toUpperCase().trim();
        }
        return "TRANSFERENCIA";
    }

    private boolean esCoralClubes(String nombre) {
        if (nombre == null) {
            return false;
        }
        String upper = nombre.toUpperCase();
        return upper.contains("CORAL") || upper.contains("FACIL") || upper.contains("CLUBES");
    }

    private String normalizarTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim();
    }
}
