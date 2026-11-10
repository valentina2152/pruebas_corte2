package pronosticos.unitarias;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import pronosticos.CalculadoraPronosticos;

class SuavizacionExponencialTest {

    private static final List<Double> SERIE = List.of(120.0, 132.0, 128.0, 141.0, 138.0, 150.0);

    @Test
    @DisplayName("Pronostica el siguiente periodo con alfa 0.3")
    void pronosticaElSiguientePeriodoConAlfa03() {
        double resultado = new CalculadoraPronosticos().suavizacionExponencial(SERIE, 0.3);

        assertEquals(137.5546, resultado, 0.01);
    }

    @ParameterizedTest
    @CsvSource({"0, 120", "1, 150"})
    @DisplayName("Alfa cero conserva el primer valor y alfa uno usa el último")
    void respetaLosExtremosDeAlfa(double alfa, double esperado) {
        assertEquals(esperado, new CalculadoraPronosticos().suavizacionExponencial(SERIE, alfa), 0.01);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.1, 1.5, Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY})
    @DisplayName("Rechaza alfa fuera del intervalo cerrado entre cero y uno")
    void rechazaAlfaInvalido(double alfa) {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new CalculadoraPronosticos().suavizacionExponencial(SERIE, alfa));
        assertEquals("Alfa debe estar entre 0 y 1", error.getMessage());
    }

    @Test
    @DisplayName("Una sola demanda es el pronóstico inicial")
    void pronosticaConUnSoloPeriodo() {
        assertEquals(80.0, new CalculadoraPronosticos().suavizacionExponencial(List.of(80.0), 0.3), 0.01);
    }

    @Test
    @DisplayName("Rechaza la serie vacía porque no existe un pronóstico inicial")
    void rechazaSerieVacia() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new CalculadoraPronosticos().suavizacionExponencial(List.of(), 0.3));
        assertEquals("Se necesita al menos un periodo", error.getMessage());
    }
}
