# Carreño-Post2-U11

**Patrones de Diseño de Software — Unidad 11: Refactorización Avanzada y Clean Code Profundo**  
Universidad de Santander (UDES) · Ingeniería de Sistemas · 2026

---

## Objetivo

Refactorizar condicionales complejos de alta complejidad ciclomática (CC) aplicando **Replace Conditional with Polymorphism** y **Guard Clauses**, verificando con SonarQube que la complejidad disminuye y que el Quality Gate del proyecto se mantiene en estado **Passed**, documentando el proceso con evidencia comparativa.

---

## Estructura del proyecto

```
src/
├── main/
│   └── java/com/universidad/refactoringu11/
│       ├── domain/
│       │   ├── Cliente.java
│       │   ├── CodigoDescuento.java
│       │   ├── DatosCliente.java
│       │   ├── Direccion.java
│       │   ├── LineaPedido.java
│       │   ├── Pedido.java
│       │   └── Producto.java
│       ├── repository/
│       │   └── PedidoRepository.java
│       └── service/
│           ├── CreditoService.java
│           ├── EnvioEstandar.java
│           ├── EnvioExpress.java
│           ├── EnvioGratis.java
│           ├── EnvioMismoDia.java
│           ├── EnvioService.java
│           ├── EstrategiaEnvio.java
│           ├── NotificacionService.java
│           └── PedidoService.java
└── test/
    └── java/com/universidad/refactoringu11/
        └── service/
            ├── CreditoServiceTests.java
            └── EnvioServiceTests.java
```

---

## Code Smells identificados (código original)

| # | Smell | Ubicación | Descripción |
|---|-------|-----------|-------------|
| 1 | **Switch Statement** | `EnvioService.calcularEnvio()` | Switch con 5 ramas para tipos de envío, CC = 5. Viola Open/Closed: agregar un tipo nuevo implica modificar el método |
| 2 | **Arrow Code** | `CreditoService.aprobarCredito()` | 5 niveles de `if` anidados, CC = 6. Dificulta la lectura y el mantenimiento |

---

## Técnicas de refactorización aplicadas

### 1. Replace Conditional with Polymorphism — `calcularEnvio()`

Se creó la interfaz `EstrategiaEnvio` y cuatro implementaciones, una por tipo de envío. Spring inyecta automáticamente todas las implementaciones en un `Map<String, EstrategiaEnvio>` usando el nombre del `@Component` como clave.

```java
public interface EstrategiaEnvio {
    double calcularCosto(Pedido pedido);
}

@Component("ESTANDAR")
public class EnvioEstandar implements EstrategiaEnvio {
    public double calcularCosto(Pedido pedido) {
        return pedido.getTotal() > 50 ? 0.0 : 5.99;
    }
}

@Component("EXPRESS")
public class EnvioExpress implements EstrategiaEnvio {
    public double calcularCosto(Pedido pedido) { return 12.99; }
}

@Component("MISMO_DIA")
public class EnvioMismoDia implements EstrategiaEnvio {
    public double calcularCosto(Pedido pedido) { return 24.99; }
}

@Component("GRATIS")
public class EnvioGratis implements EstrategiaEnvio {
    public double calcularCosto(Pedido pedido) { return 0.0; }
}
```

`EnvioService` queda con CC = 1, sin ningún condicional:

```java
@Service
public class EnvioService {
    private final Map<String, EstrategiaEnvio> estrategias;

    public EnvioService(Map<String, EstrategiaEnvio> estrategias) {
        this.estrategias = estrategias;
    }

    public double calcularEnvio(Pedido pedido, String tipo) {
        return Optional.ofNullable(estrategias.get(tipo))
                .orElseThrow(() -> new IllegalArgumentException(tipo))
                .calcularCosto(pedido);
    }
}
```

**Beneficio:** Agregar un nuevo tipo de envío solo requiere crear una nueva clase con `@Component`. `EnvioService` nunca se modifica — cumple el principio Open/Closed.

---

### 2. Guard Clauses — `aprobarCredito()`

El arrow code de 5 niveles se reemplazó por cláusulas de guarda que retornan anticipadamente, dejando el camino feliz al final:

```java
// Antes — Arrow code, CC = 6
public String aprobarCredito(Cliente c, double monto) {
    if (c != null) {
        if (c.isActivo()) {
            if (c.getScore() >= 600) {
                if (monto > 0) {
                    if (monto <= c.getLimiteCredito()) {
                        return "APROBADO";
                    }
                }
            }
        }
    }
    return "RECHAZADO";
}

// Después — Guard Clauses, CC = 2
public String aprobarCredito(Cliente c, double monto) {
    if (c == null)                        return "RECHAZADO";
    if (!c.isActivo())                    return "RECHAZADO";
    if (c.getScore() < 600)               return "RECHAZADO";
    if (monto <= 0)                       return "RECHAZADO";
    if (monto > c.getLimiteCredito())     return "RECHAZADO";
    return "APROBADO";
}
```

**Beneficio:** El método quedó con exactamente 6 líneas (5 guards + 1 return). La CC bajó de 6 a 2 y la intención es inmediatamente legible.

---

## Métricas SonarQube — Antes vs. Después

| Método | CC Antes | CC Después | Reducción |
|--------|----------|------------|-----------|
| `calcularEnvio()` | 5 | 1 | ↓ 80 % |
| `aprobarCredito()` | 6 | 2 | ↓ 67 % |
| Code Smells totales | _ver captura_ | _ver captura_ | ↓ notable |
| Quality Gate | — | **Passed** | ✅ |

> 📸 **Capturas del dashboard de SonarQube:**

**Antes de la refactorización:**

![SonarQube Antes](img/captura1.png)

**Después de la refactorización — Quality Gate Passed:**

![SonarQube Después](img/captura2.png)

---

## Reflexión — Patrón Strategy y el principio Open/Closed

El patrón Strategy aplicado en `EnvioService` convierte cada tipo de envío en una unidad de comportamiento independiente. Gracias a esto, cuando el negocio requiera un nuevo tipo —por ejemplo, `EnvioInternacional`— basta con crear una nueva clase que implemente `EstrategiaEnvio` y anotarla con `@Component("INTERNACIONAL")`; Spring la registra automáticamente en el mapa de estrategias sin que `EnvioService` sea tocado. Esto es exactamente lo que propone el principio Open/Closed: el servicio está **abierto a extensión** (nuevas estrategias) pero **cerrado a modificación** (su código no cambia). El resultado es un sistema más fácil de mantener, probar de forma aislada y evolucionar sin riesgo de introducir regresiones en los tipos de envío ya existentes.

---

## Prerrequisitos para ejecutar el proyecto

```bash
# SonarQube debe estar corriendo (mismo del Post-Contenido 1)
docker start sonarqube

# Ejecutar pruebas y análisis
mvn verify sonar:sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=TU_TOKEN \
  -Dsonar.projectKey=refactoring-u11
```

- Java 17+
- Maven 3.9+
- Docker Desktop