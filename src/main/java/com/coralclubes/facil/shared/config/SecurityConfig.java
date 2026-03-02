package com.coralclubes.facil.shared.config;

import com.coralclubes.facil.shared.infrastructure.exceptions.CustomAuthenticationEntryPoint;
import com.coralclubes.facil.shared.infrastructure.exceptions.FilterExceptionHandler;
import com.coralclubes.facil.shared.infrastructure.security.filter.JwtAuthenticationFilter;
import com.coralclubes.facil.shared.infrastructure.security.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuración principal de seguridad para la aplicación.
 * Define reglas de acceso, CORS, autenticación Stateless y filtros JWT.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Habilita @PreAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter; // filtro para procesar el token JWT en cada solicitud
    private final FilterExceptionHandler filterExceptionHandler; // filtro para atrapar errores de JWT
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint; // manejador de errores dentro de los filtros de seguridad (401 Unauthorized)

    @Value("${app.url.frontend}")
    private String frontendUrl;

    /**
     * Bean del AuthenticationManager.
     * Simplemente delega a la configuración de autenticación de Spring Security, que usará el DaoAuthenticationProvider que definimos.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Bean para encriptar contraseñas (BCrypt).
     * Por defecto usa un strength de 10
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura el proveedor de autenticación con el UserDetailsServiceImpl y el PasswordEncoder.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setHideUserNotFoundExceptions(false);
        return authProvider;
    }

    /**
     * Cadena de filtros de seguridad
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Deshabilitamos CSRF porque usamos JWT (Stateless)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. Manejo de Excepciones de Seguridad (401 Unauthorized)
                // Se dispara cuando un usuario ANÓNIMO intenta entrar a una ruta protegida.
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                )

                // 4. Autorización de Rutas
                .authorizeHttpRequests(auth -> auth
                        // Autenticación
                        .requestMatchers("/api/auth/login", "/api/auth/login/simple").permitAll()
                        .requestMatchers("/api/auth/password-reset/**").permitAll()
                        .requestMatchers("/api/auth/refresh-token").permitAll()

                        // Documentación (Swagger/OpenAPI)
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // Configuración pública y utilidades
                        .requestMatchers("/api/config/**", "/api/util/**").permitAll()

                        // Endpoints de prueba
                        .requestMatchers("/api/test/**").permitAll()

                        // endpoints especificos dque consumen los portales web y que no requieren autenticación
                        .requestMatchers("/api/v1/public/**").permitAll()

                        // endpoints de WebSocket
                        .requestMatchers("/ws-api/**").permitAll()

                        // TODO LO DEMÁS requiere estar autenticado
                        .anyRequest().authenticated()
                )

                // 5. Inserción de Filtros
                // El orden es CRÍTICO:
                // a) Primero el FilterExceptionHandler para que envuelva a los demás y atrape sus errores.
                .addFilterBefore(filterExceptionHandler, UsernamePasswordAuthenticationFilter.class)
                // b) Luego el JwtAuthenticationFilter para procesar el token.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // Asignamos el proveedor de autenticación
        http.authenticationProvider(authenticationProvider());

        return http.build();
    }
}