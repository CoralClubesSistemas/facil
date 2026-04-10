package com.coralclubes.facil.modules.cobranza.model.pagos.engine;

import com.coralclubes.facil.modules.cobranza.model.pagos.interfaces.PaymentStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PaymentStrategyFactory {

    private final Map<String, PaymentStrategy> strategies;

    // Spring Boot inyecta todas las implementaciones
    public PaymentStrategyFactory(List<PaymentStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(PaymentStrategy::getGatewayType, Function.identity()));
    }

    public PaymentStrategy getStrategy(String clave) {
        PaymentStrategy strategy = strategies.get(clave);

        if (strategy == null) {
            throw new UnsupportedOperationException("No hay procesador configurado para: " + clave);
        }

        return strategy;
    }
}