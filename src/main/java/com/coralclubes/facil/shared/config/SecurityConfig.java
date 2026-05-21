package com.coralclubes.facil.shared.config;

import com.coralclubes.facil.shared.infrastructure.gateway.filter.GatewayHeaderFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración de seguridad simplificada.
 *
 * En modo gateway:
 * - El gateway valida JWT y envía X-Auth-* headers.
 * - GatewayHeaderFilter lee esos headers y establece el SecurityContext.
 *
 * En modo legacy (compatibilidad transición):
 * - Los filtros de JWT fueron eliminados.
 * - Solo se mantiene GatewayHeaderFilter + @PreAuthorize.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final GatewayHeaderFilter gatewayHeaderFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // Autenticación (legacy, se eliminará cuando el gateway asuma 100%)
                        .requestMatchers("/api/auth/login", "/api/auth/login/simple").permitAll()
                        .requestMatchers("/api/auth/password-reset/**").permitAll()
                        .requestMatchers("/api/auth/refresh-token").permitAll()

                        // Documentación (Swagger/OpenAPI)
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // Configuración pública y utilidades
                        .requestMatchers("/api/config/**", "/api/util/**").permitAll()

                        // Endpoints de prueba
                        .requestMatchers("/api/test/**").permitAll()

                        // Endpoint interno del gateway
                        .requestMatchers("/internal/**").permitAll()

                        // Endpoints públicos (portales web)
                        .requestMatchers("/api/v1/public/**").permitAll()

                        // TODO LO DEMÁS requiere estar autenticado
                        .anyRequest().authenticated()
                )

                // GatewayHeaderFilter: lee X-Auth-* headers del gateway
                .addFilterBefore(gatewayHeaderFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
