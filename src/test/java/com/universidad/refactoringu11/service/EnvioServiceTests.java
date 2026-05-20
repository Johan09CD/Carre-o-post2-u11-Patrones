package com.universidad.refactoringu11.service;

import com.universidad.refactoringu11.domain.Pedido;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnvioServiceTests {

    private final EnvioService service = new EnvioService(
            Map.of("ESTANDAR", new EnvioEstandar())
    );

    @Test
    void calcularEnvio_estandar_conTotalAlto_debeSerGratis() {
        Pedido p = new Pedido();
        p.setTotal(60.0);
        assertEquals(0.0, service.calcularEnvio(p, "ESTANDAR"), 0.001);
    }
}
