package pronosticos.unitarias;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import pronosticos.CalculadoraPronosticos;

class MseTest {
    private final CalculadoraPronosticos calculadora = new CalculadoraPronosticos();
    @Test
    @DisplayName("Calcula el MSE con los pronosticos redondeados de la tabla")
    void calculaElErrorCuadraticoMedio() {
        double resultado = calculadora.mse(List.of(141.0, 138.0, 150.0), List.of(126.67, 133.67, 135.67));
        assertEquals(143.15, resultado, 0.01);
    }

    @Test
    @DisplayName("El MSE es cero cuando cada pronostico es exacto")
    void pronosticoExacto() {
        assertEquals(0.0, calculadora.mse(List.of(10.0, 20.0), List.of(10.0, 20.0)), 0.000001);
    }

    @Test
    @DisplayName("Rechaza series de distinta longitud con el mensaje acordado")
    void rechazaLongitudesDistintas() {
        IllegalArgumentException error = org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class, () -> calculadora.mse(List.of(10.0, 20.0), List.of(10.0)));
        assertEquals("Las series deben tener el mismo número de periodos", error.getMessage());
    }
}
