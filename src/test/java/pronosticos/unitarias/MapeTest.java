package pronosticos.unitarias;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import pronosticos.CalculadoraPronosticos;

class MapeTest {

    private final CalculadoraPronosticos calculadora = new CalculadoraPronosticos();

    @Test
    @DisplayName("Calcula el error porcentual absoluto medio")
    void calculaElErrorPorcentual() {
        double resultado = calculadora.mape(List.of(141.0, 138.0, 150.0), List.of(126.67, 133.67, 135.67));
        assertEquals(7.62, resultado, 0.01);
    }

    @Test
    @DisplayName("Excluye del divisor los periodos con demanda cero")
    void excluyeDemandaCero() {
        // (0 / 80 * 100 + 5 / 95 * 100) / 2 = 2.631578...
        double resultado = calculadora.mape(List.of(80.0, 0.0, 95.0), List.of(80.0, 0.0, 100.0));
        assertEquals(2.63, resultado, 0.01);
    }

    @Test
    @DisplayName("Rechaza una serie sin periodos válidos para MAPE")
    void rechazaTodasLasDemandasCero() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> calculadora.mape(List.of(0.0, 0.0), List.of(10.0, 20.0)));
        assertEquals("El MAPE no está definido: todos los periodos tienen demanda 0", error.getMessage());
    }

    @Test
    @DisplayName("El MAPE de un pronóstico exacto es cero")
    void pronosticoExacto() {
        assertEquals(0, calculadora.mape(List.of(80.0), List.of(80.0)), 0.01);
    }

    @Test
    @DisplayName("Rechaza series de distinta longitud en MAPE")
    void rechazaLongitudesDistintas() {
        assertThrows(IllegalArgumentException.class,
                () -> calculadora.mape(List.of(80.0, 95.0), List.of(80.0)));
    }

    @Test
    @DisplayName("Rechaza series vacías en MAPE")
    void rechazaSeriesVacias() {
        assertThrows(IllegalArgumentException.class, () -> calculadora.mape(List.of(), List.of()));
    }
}
