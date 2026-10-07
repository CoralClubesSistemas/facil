package com.coralclubes.facil.modules.cobranza.service.extractor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Catálogo Enum oficial de instituciones financieras participantes en Banxico (SPEI)
 * con sus códigos de 3 dígitos de CLABE interbancaria.
 */
@Getter
@RequiredArgsConstructor
public enum BancoBanxico {

    BANAMEX("002", "BANAMEX"),
    BANCOMEXT("006", "BANCOMEXT"),
    BANOBRAS("009", "BANOBRAS"),
    BBVA("012", "BBVA"),
    SANTANDER("014", "SANTANDER"),
    BANJERCITO("019", "BANJERCITO"),
    HSBC("021", "HSBC"),
    BAJIO("030", "BAJIO"),
    IXE("032", "IXE"),
    INBURSA("036", "INBURSA"),
    INTERACCIONES("037", "INTERACCIONES"),
    MIFEL("042", "MIFEL"),
    SCOTIABANK("044", "SCOTIABANK"),
    BANREGIO("058", "BANREGIO"),
    INVEX("059", "INVEX"),
    BANSI("060", "BANSI"),
    AFIRME("062", "AFIRME"),
    BANORTE("072", "BANORTE"),
    ABN_AMRO("102", "ABN AMRO"),
    AMERICAN_EXPRESS("103", "AMERICAN EXPRESS"),
    BAMSA("106", "BAMSA"),
    TOKYO("108", "TOKYO"),
    JP_MORGAN("110", "JP MORGAN"),
    BMONEX("112", "BMONEX"),
    VE_POR_MAS("113", "VE POR MAS"),
    ING("116", "ING"),
    DEUTSCHE("124", "DEUTSCHE"),
    CREDIT_SUISSE("126", "CREDIT SUISSE"),
    AZTECA("127", "BANCO AZTECA"),
    AUTOFIN("128", "AUTOFIN"),
    BARCLAYS("129", "BARCLAYS"),
    COMPARTAMOS("130", "COMPARTAMOS"),
    BANCO_FAMSA("131", "BANCO FAMSA"),
    BMULTIVA("132", "BMULTIVA"),
    ACTINVER("133", "ACTINVER"),
    WAL_MART("134", "WAL-MART"),
    NAFIN("135", "NAFIN"),
    INTERBANCO("136", "INTERBANCO"),
    BANCOPPEL("137", "BANCOPPEL"),
    NU_MEXICO("138", "NU MEXICO"),
    CONSUBANCO("140", "CONSUBANCO"),
    VOLKSWAGEN("141", "VOLKSWAGEN"),
    CIBANCO("143", "CIBANCO"),
    BBASE("145", "BBASE"),
    BIENESTAR("166", "BIENESTAR"),
    HIPOTECARIA_FEDERAL("168", "HIPOTECARIA FEDERAL"),
    MONEXCB("600", "MONEXCB"),
    GBM("601", "GBM"),
    MASARI("602", "MASARI"),
    VALUE("605", "VALUE"),
    ESTRUCTURADORES("606", "ESTRUCTURADORES"),
    TIBER("607", "TIBER"),
    VECTOR("608", "VECTOR"),
    B_B("610", "B&B"),
    ACCIVAL("614", "ACCIVAL"),
    MERRILL_LYNCH("615", "MERRILL LYNCH"),
    FINAMEX("616", "FINAMEX"),
    VALMEX("617", "VALMEX"),
    UNICA("618", "UNICA"),
    MAPFRE("619", "MAPFRE"),
    PROFUTURO("620", "PROFUTURO"),
    CB_ACTINVER("621", "CB ACTINVER"),
    OACTIN("622", "OACTIN"),
    SKANDIA("623", "SKANDIA"),
    CBDEUTSCHE("626", "CBDEUTSCHE"),
    ZURICH("627", "ZURICH"),
    ZURICHVI("628", "ZURICHVI"),
    SU_CASITA("629", "SU CASITA"),
    CB_INTERCAM("630", "CB INTERCAM"),
    CI_BOLSA("631", "CI BOLSA"),
    BULLTICK_CB("632", "BULLTICK CB"),
    STERLING("633", "STERLING"),
    FINCOMUN("634", "FINCOMUN"),
    HDI_SEGUROS("636", "HDI SEGUROS"),
    ORDER("637", "ORDER"),
    AKALA("638", "AKALA"),
    CB_JPMORGAN("640", "CB JPMORGAN"),
    REFORMA("642", "REFORMA"),
    STP("646", "STP"),
    EVERCORE("648", "EVERCORE"),
    SEGMTY("651", "SEGMTY"),
    ASEA("652", "ASEA"),
    KUSPIT("653", "KUSPIT"),
    SOFIEXPRESS("655", "SOFIEXPRESS"),
    UNAGRA("656", "UNAGRA"),
    OPCIONES_EMPRESARIALES("659", "OPCIONES EMPRESARIALES DEL NOROESTE"),
    LIBERTAD("670", "LIBERTAD"),
    ALBO("680", "ALBO"),
    PAGATODO("683", "PAGATODO"),
    TRANSFER("684", "TRANSFER"),
    FONDEADORA("685", "FONDO (FONDEADORA)"),
    INNOVAPAY("686", "INNOVAPAY"),
    KLAR("688", "KLAR"),
    FINSUS("689", "FINANCIERA SUSTENTABLE (FINSUS)"),
    PILOT("690", "PILOT"),
    SANTANDER_CONSUMO("691", "SANTANDER CONSUMO"),
    SPIN_BY_OXXO("692", "SPIN BY OXXO"),
    STP_SECUNDARIO("846", "STP"),
    CLS("901", "CLS"),
    INDEVAL("902", "INDEVAL");

    private final String codigoClabe;
    private final String nombreComun;

    private static final Map<String, BancoBanxico> MAPA_POR_CODIGO;

    static {
        Map<String, BancoBanxico> mapa = new HashMap<>();
        for (BancoBanxico banco : values()) {
            mapa.put(banco.codigoClabe, banco);
        }
        MAPA_POR_CODIGO = Collections.unmodifiableMap(mapa);
    }

    /**
     * Resuelve el banco correspondiente a partir de una CLABE interbancaria (18 dígitos)
     * o directamente del prefijo de 3 dígitos.
     *
     * @param clabe CLABE o código numérico.
     * @return Optional con la constante BancoBanxico encontrada.
     */
    public static Optional<BancoBanxico> desdeClabe(String clabe) {
        if (clabe == null) {
            return Optional.empty();
        }
        String limpia = clabe.replaceAll("\\D", "");
        if (limpia.length() >= 3) {
            String codigo = limpia.substring(0, 3);
            return Optional.ofNullable(MAPA_POR_CODIGO.get(codigo));
        }
        return Optional.empty();
    }

    /**
     * Resuelve el nombre común del banco a partir de una CLABE interbancaria.
     */
    public static Optional<String> obtenerNombrePorClabe(String clabe) {
        return desdeClabe(clabe).map(BancoBanxico::getNombreComun);
    }
}
