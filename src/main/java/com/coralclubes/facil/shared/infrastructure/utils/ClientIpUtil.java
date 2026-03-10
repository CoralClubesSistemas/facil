package com.coralclubes.facil.shared.infrastructure.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * Componente utilitario que obtiene la dirección IP real del cliente desde la solicitud HTTP.
 * Evalúa múltiples headers para traspasar proxies y balanceadores de carga.
 */
@Component
public class ClientIpUtil {

    public String getClientIpAddress(HttpServletRequest request) {
        if (request == null) return "UNKNOWN";

        String[] headersToCheck = {
                "X-Forwarded-For",           // Proxy estándar
                "Proxy-Client-IP",           // Apache
                "WL-Proxy-Client-IP",        // WebLogic
                "HTTP_X_FORWARDED_FOR",      // Variantes comunes
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",  // Load balancers
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR"
        };

        for (String header : headersToCheck) {
            String ipList = request.getHeader(header);
            // Reemplazamos length() != 0 por el método moderno isEmpty()
            if (ipList != null && !ipList.isEmpty() && !"unknown".equalsIgnoreCase(ipList)) {
                // A veces el header contiene varias IPs separadas por coma (IP Cliente, IP Proxy1, IP Proxy2)
                return ipList.split(",")[0].trim();
            }
        }

        // Si ningún header válido, tomar el remoto directo
        return request.getRemoteAddr();
    }
}