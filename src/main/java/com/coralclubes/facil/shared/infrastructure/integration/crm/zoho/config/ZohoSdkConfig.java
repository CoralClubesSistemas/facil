package com.coralclubes.facil.shared.infrastructure.integration.crm.zoho.config;

import com.zoho.api.authenticator.OAuthToken;
import com.zoho.api.authenticator.Token;
import com.zoho.api.authenticator.store.FileStore;
import com.zoho.api.authenticator.store.TokenStore;
import com.zoho.crm.api.Initializer;
import com.zoho.crm.api.SDKConfig;
import com.zoho.crm.api.UserSignature;
import com.zoho.crm.api.dc.DataCenter.Environment;
import com.zoho.crm.api.dc.USDataCenter;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.File;

/**
 * Configuración e inicialización del SDK oficial de Zoho CRM v8.0 bajo el módulo agnóstico app.crm.
 */
@Configuration
@Slf4j
public class ZohoSdkConfig {

    @Value("${app.crm.zoho.user-email:}")
    private String userEmail;

    @Value("${app.crm.zoho.client-id:}")
    private String clientId;

    @Value("${app.crm.zoho.client-secret:}")
    private String clientSecret;

    @Value("${app.crm.zoho.refresh-token:}")
    private String refreshToken;

    @Value("${app.crm.zoho.environment:PRODUCTION}")
    private String environmentStr;

    @PostConstruct
    public void initializeZohoSdk() {
        if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank()) {
            log.info("[ZOHO SDK CONFIG] Credenciales de Zoho CRM no configuradas. Omitiendo inicialización del SDK.");
            return;
        }

        try {
            log.info("[ZOHO SDK CONFIG] Inicializando Zoho CRM Java SDK 8.0 para usuario: {}", userEmail);

            Environment environment = parseEnvironment(environmentStr);
            UserSignature user = new UserSignature(userEmail);

            String resourcePath = System.getProperty("java.io.tmpdir") + File.separator + "zoho-sdk";
            File resourceDir = new File(resourcePath);
            if (!resourceDir.exists()) {
                resourceDir.mkdirs();
            }

            TokenStore tokenStore = new FileStore(resourcePath + File.separator + "zoho_tokens.txt");

            Token token = new OAuthToken.Builder()
                    .clientID(clientId)
                    .clientSecret(clientSecret)
                    .refreshToken(refreshToken)
                    .build();

            SDKConfig sdkConfig = new SDKConfig.Builder()
                    .autoRefreshFields(false)
                    .pickListValidation(true)
                    .build();

            com.zoho.api.logger.Logger logger = new com.zoho.api.logger.Logger.Builder()
                    .level(com.zoho.api.logger.Logger.Levels.INFO)
                    .filePath(resourcePath + File.separator + "zoho_sdk.log")
                    .build();

            new Initializer.Builder()
                    .environment(environment)
                    .token(token)
                    .store(tokenStore)
                    .SDKConfig(sdkConfig)
                    .resourcePath(resourcePath)
                    .logger(logger)
                    .initialize();

            log.info("[ZOHO SDK CONFIG] Zoho CRM Java SDK 8.0 inicializado exitosamente en entorno: {}", environmentStr);
        } catch (Exception e) {
            log.error("[ZOHO SDK CONFIG] Error al inicializar el SDK de Zoho CRM: {}", e.getMessage(), e);
        }
    }

    private Environment parseEnvironment(String env) {
        if (env == null) return USDataCenter.PRODUCTION;
        return switch (env.trim().toUpperCase()) {
            case "SANDBOX" -> USDataCenter.SANDBOX;
            case "DEVELOPER" -> USDataCenter.DEVELOPER;
            default -> USDataCenter.PRODUCTION;
        };
    }
}
