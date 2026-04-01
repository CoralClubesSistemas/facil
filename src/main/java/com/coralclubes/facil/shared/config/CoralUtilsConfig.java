package com.coralclubes.facil.shared.config;

import com.coralclubes.logging.BusinessLogger;
import com.coralclubes.logging.SqlLogger;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * Configuracion de utilidades de la libreria Coral Utils.
 */
@Configuration
public class CoralUtilsConfig {

    /**
     * el Bean de BusinessLoger sirve para mostrar logs de negocio en los servicios.
     * El logger se crea con el nombre de la clase donde se inyecta.
     */
    @Bean
    @Scope("prototype")
    public BusinessLogger businessLogger(InjectionPoint injectionPoint) {
        Class<?> clazz = injectionPoint.getMember().getDeclaringClass();

        return new BusinessLogger(clazz);
    }

    /**
     * El Bean de SqlLoger sirve para mostrar logs de consultas SQL en los repositorios.
     * El logger se crea con el nombre de la clase donde se inyecta.
     */
    @Bean
    @Scope("prototype")
    public SqlLogger sqlLogger(InjectionPoint injectionPoint) {
        Class<?> clazz = injectionPoint.getMember().getDeclaringClass();

        return new SqlLogger(clazz);
    }
}

