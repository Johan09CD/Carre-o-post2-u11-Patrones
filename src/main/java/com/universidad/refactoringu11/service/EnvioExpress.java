package com.universidad.refactoringu11.service;

import com.universidad.refactoringu11.domain.Pedido;
import org.springframework.stereotype.Component;

@Component("EXPRESS")
public class EnvioExpress implements EstrategiaEnvio {
    @Override
    public double calcularCosto(Pedido pedido) {
        return 12.99;
    }
}
