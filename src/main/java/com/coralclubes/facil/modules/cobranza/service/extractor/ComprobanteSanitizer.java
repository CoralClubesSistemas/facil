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
 * normalización de cadenas, descarte de leyendas de pie de página y corrección de la IA.
 */
@Slf4j
@Component
public class ComprobanteSanitizer {

    // Se agregan términos detectados en los logs (ACUDE, SUCURSAL, LLAMA, 800)
    private static final List<String> BLACKLIST_PIE_PAGINA = List.of(
            "CONDUSEF", "HOJA", "PAGINA", "PÁGINA", "VERSION", "VERSIÓN",
            "CONSULTA", "LINEA", "LÍNEA", "ACLARACION", "ACLARACIÓN",
            "TELEFONO", "TELÉFONO", "DERECHOS RESERVADOS", "WWW.", "HTTP",
            "IMPUESTO", "COMISION", "COMISIÓN", "IVA", "FOLIO DE IMPRESION",
            "SUCURSAL", "ACUDE", "LLAMA", "800 "
    );

    private static final Pattern PATTERN_CLABE_18 = Pattern.compile("^\\d{18}$");

    /**
     * Aplica reglas de sanitización al DTO devuelto por IA o extracción digital.
     */
    public ComprobantePagoAnalizadoDto sanitizar(ComprobantePagoAnalizadoDto raw) {
        if (raw == null) {
            return null;
        }

        String bancoEmisor = normalizarBanco(raw.bancoEmisor());
        String bancoReceptor = normalizarBanco(raw.bancoReceptor());
        String cuentaOrdenante = limpiarNumeroCuenta(raw.cuentaOrdenante());
        String cuentaBeneficiaria = limpiarNumeroCuenta(raw.cuentaBeneficiaria());
        String ordenante = normalizarTexto(raw.ordenante());
        String beneficiario = normalizarTexto(raw.beneficiario());
        String claveRastreo = sanitizarClaveRastreo(raw.claveRastreo());
        String referencia = sanitizarReferencia(raw.referencia());
        BigDecimal monto = sanitizarMonto(raw.monto());
        String fecha = normalizarFecha(raw.fechaOperacion());
        String hora = normalizarTexto(raw.horaOperacion());
        String concepto = sanitizarConcepto(raw.concepto());
        String tipoOperacion = normalizarTipoOperacion(raw.tipoOperacion());

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

        // 3. Inversión de roles si están cruzados (en Cobranza el receptor siempre es Coral Clubes)
        if (esCoralClubes(ordenante) && !esCoralClubes(beneficiario)) {
            log.info("Detectada inversión de roles. Ajustando a beneficiario.");
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
                bancoEmisor, bancoReceptor, monto, fecha, hora, claveRastreo, referencia,
                cuentaOrdenante, cuentaBeneficiaria, beneficiario, ordenante, concepto, tipoOperacion
        );
    }

    /**
     * Normaliza los nombres de los bancos y corrige errores de la IA detectados en los logs.
     */
    private String normalizarBanco(String rawBanco) {
        if (rawBanco == null || rawBanco.isBlank()) {
            return null;
        }
        String upper = rawBanco.toUpperCase().trim();

        if (upper.contains("BBVA")) return "BBVA";
        if (upper.contains("AZTECA") || upper.contains("SALINAS") || upper.contains("GUARDADITO"))
            return "BANCO AZTECA";
        if (upper.contains("SANTANDER")) return "SANTANDER";
        if (upper.contains("CITI") || upper.contains("BANAMEX")) return "BANAMEX";
        if (upper.contains("SPIN")) return "SPIN BY OXXO";
        if (upper.contains("MERCADO PAGO")) return "MERCADO PAGO";
        if (upper.contains("STP")) return "STP";

        return rawBanco.trim();
    }

    /**
     * Filtra el concepto para eliminar pies de página extraídos por error ("CUALQUIER ACLARACIÓN...")
     */
    private String sanitizarConcepto(String rawConcepto) {
        if (rawConcepto == null || rawConcepto.isBlank()) {
            return null;
        }
        String concepto = rawConcepto.trim();
        String upper = concepto.toUpperCase();

        for (String terminoProhibido : BLACKLIST_PIE_PAGINA) {
            if (upper.contains(terminoProhibido)) {
                log.debug("Concepto descartado por contener texto legal/pie de página: '{}'", concepto);
                return null;
            }
        }
        return concepto;
    }

    private String sanitizarReferencia(String rawRef) {
        if (rawRef == null || rawRef.isBlank()) {
            return null;
        }
        String ref = rawRef.trim();
        String upper = ref.toUpperCase();

        for (String terminoProhibido : BLACKLIST_PIE_PAGINA) {
            if (upper.contains(terminoProhibido)) {
                return null;
            }
        }
        if (ref.length() > 25 || (ref.contains(" ") && ref.split("\\s+").length > 2)) {
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
                return null;
            }
        }
        if (clave.length() < 7 || clave.length() > 50) {
            return null;
        }
        return clave;
    }

    private BigDecimal sanitizarMonto(BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        return monto.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Limpia completamente la cuenta, eliminando "CTA", "CLABE", asteriscos y espacios.
     * EJ: "CTA**1144" -> "1144". "***8585" -> "8585"
     */
    private String limpiarNumeroCuenta(String rawCuenta) {
        if (rawCuenta == null || rawCuenta.isBlank()) {
            return null;
        }
        // Expresión regular corregida: elimina TODO lo que NO sea un número del 0 al 9.
        String limpia = rawCuenta.replaceAll("[^0-9]", "");
        return limpia.isBlank() ? null : limpia;
    }

    private String normalizarFecha(String rawFecha) {
        if (rawFecha == null || rawFecha.isBlank()) {
            return null;
        }
        String fecha = rawFecha.trim();
        if (fecha.matches("^\\d{1,2}[/-]\\d{1,2}[/-]\\d{4}$")) {
            String[] parts = fecha.split("[/-]");
            String dia = String.format("%02d", Integer.parseInt(parts[0]));
            String mes = String.format("%02d", Integer.parseInt(parts[1]));
            String anio = parts[2];
            return anio + "-" + mes + "-" + dia;
        }
        return fecha;
    }

    /**
     * Respeta la decisión de la IA ("TRANSFERENCIA" o "DEPÓSITO"), pero maneja los caracteres Unicode y valores vacíos.
     */
    private String normalizarTipoOperacion(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            return "TRANSFERENCIA";
        }
        String upper = tipo.toUpperCase().trim();
        // Jackson ya deserializa Unicode (\u00d3), pero por seguridad normalizamos si la IA regresa variaciones.
        if (upper.contains("DEP") || upper.contains("DEPOSITO") || upper.contains("DEPÓSITO")) {
            return "DEPÓSITO";
        }
        return "TRANSFERENCIA";
    }

    private boolean esCoralClubes(String nombre) {
        if (nombre == null) {
            return false;
        }
        String upper = nombre.toUpperCase();
        return upper.contains("CORAL") || upper.contains("FACIL") || upper.contains("CLUBES") || upper.contains("FIMEX");
    }

    private String normalizarTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim();
    }
}