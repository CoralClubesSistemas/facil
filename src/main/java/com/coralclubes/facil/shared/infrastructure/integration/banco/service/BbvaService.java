package com.coralclubes.facil.shared.infrastructure.integration.banco.service;

import org.springframework.stereotype.Service;

@Service
public class BbvaService {

    /**
     * Algoritmo para cálculo de dígito verificador proporcionado por BBVA
     */
    public String calcularDigitoVerificador(String referencia) {
        int suma = 0;

        for (int i = 0; i < referencia.length(); i++) {
            // obtenemos los digitos en cada vuelta
            int digito = Character.getNumericValue(referencia.charAt(i));

            if (i % 2 == 0) {
                // Posiciones impares → sumar directamente
                suma += digito;
            } else {
                // Posiciones pares → multiplicar por 2
                int multiplicado = digito * 2;

                // sumar dígitos si >= 10 (equivalente a restar 9)
                suma += (multiplicado < 10) ? multiplicado : (multiplicado - 9);
            }
        }

        int residuo = suma % 10;

        if (residuo == 0) {
            return "0";
        }

        return String.valueOf(10 - residuo);
    }
}
