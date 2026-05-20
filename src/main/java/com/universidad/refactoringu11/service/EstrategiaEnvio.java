package com.universidad.refactoringu11.service;

import com.universidad.refactoringu11.domain.Pedido;

public interface EstrategiaEnvio {
    double calcularCosto(Pedido pedido);
}
