package com.universidad.refactoringu11.service;

import com.universidad.refactoringu11.domain.Pedido;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class EnvioService {
    private final Map<String, EstrategiaEnvio> estrategias;

    public EnvioService(Map<String, EstrategiaEnvio> estrategias) {
        this.estrategias = estrategias;
    }

    public double calcularEnvio(Pedido pedido, String tipoEnvio) {
        return Optional.ofNullable(estrategias.get(tipoEnvio))
                .orElseThrow(() -> new IllegalArgumentException(tipoEnvio))
                .calcularCosto(pedido);
    }
}
