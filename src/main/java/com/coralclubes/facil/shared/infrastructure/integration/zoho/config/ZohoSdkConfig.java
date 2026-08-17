package com.coralclubes.facil.shared.infrastructure.integration.zoho.config;

import com.zoho.api.authenticator.OAuthToken;
import com.zoho.api.authenticator.Token;
import com.zoho.api.authenticator.store.TokenStore;
import com.zoho.api.logger.Logger;
import com.zoho.crm.api.Initializer;
import com.zoho.crm.api.SDKConfig;
import com.zoho.crm.api.dc.USDataCenter;
import com.zoho.crm.api.dc.DataCenter.Environment;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuración e inicialización del SDK oficial de Zoho CRM (zohocrm-java-sdk-8-0:5.0.0).
 * Utiliza un TokenStore en memoria sin requerir tablas en la base de datos SQL Server.
 */
@Configuration
@Slf4j
public class ZohoSdkConfig {

    @Value("${app.zoho.user-email}")
    private String userEmail;

    @Value("${app.zoho.client-id}")
    private String clientId;

    @Value("${app.zoho.client-secret}")
    private String clientSecret;

    @Value("${app.zoho.refresh-token}")
    private String refreshToken;

    @Value("${app.zoho.environment}")
    private String environmentType;

    @PostConstruct
    public void initZohoSdk() {
        try {
            Environment environment = switch (environmentType.toUpperCase()) {
                case "SANDBOX" -> USDataCenter.SANDBOX;
                case "DEVELOPER" -> USDataCenter.DEVELOPER;
                default -> USDataCenter.PRODUCTION;
            };

            Token token = new OAuthToken.Builder()
                    .clientID(clientId)
                    .clientSecret(clientSecret)
                    .refreshToken(refreshToken)
                    .build();

            TokenStore tokenStore = new InMemoryTokenStore(token);

            SDKConfig sdkConfig = new SDKConfig.Builder()
                    .autoRefreshFields(true)
                    .pickListValidation(false)
                    .build();

            Logger logger = new Logger.Builder()
                    .level(Logger.Levels.INFO)
                    .build();

            String resourcePath = System.getProperty("java.io.tmpdir") + File.separator + "zoho-sdk";
            File sdkDir = new File(resourcePath);
            if (!sdkDir.exists()) {
                boolean created = sdkDir.mkdirs();
                log.debug("[ZOHO SDK CONFIG] Directorio para SDK de Zoho creado: {} ({})", resourcePath, created);
            }

            new Initializer.Builder()
                    .environment(environment)
                    .token(token)
                    .store(tokenStore)
                    .SDKConfig(sdkConfig)
                    .resourcePath(resourcePath)
                    .logger(logger)
                    .initialize();

            log.info("[ZOHO SDK CONFIG] Zoho CRM Java SDK 8.0 inicializado exitosamente en memoria para el usuario: {}", userEmail);
        } catch (Exception e) {
            log.error("[ZOHO SDK CONFIG] Error inicializando el SDK de Zoho CRM: {}", e.getMessage(), e);
        }
    }

    private static class InMemoryTokenStore implements TokenStore {
        private Token currentToken;

        public InMemoryTokenStore(Token initialToken) {
            this.currentToken = initialToken;
        }

        @Override
        public Token findToken(Token token) {
            return this.currentToken != null ? this.currentToken : token;
        }

        @Override
        public void saveToken(Token token) {
            this.currentToken = token;
        }

        @Override
        public void deleteToken(String id) {
            this.currentToken = null;
        }

        @Override
        public List<Token> getTokens() {
            List<Token> tokens = new ArrayList<>();
            if (this.currentToken != null) {
                tokens.add(this.currentToken);
            }
            return tokens;
        }

        @Override
        public void deleteTokens() {
            this.currentToken = null;
        }

        @Override
        public Token findTokenById(String id) {
            return this.currentToken;
        }
    }
}
