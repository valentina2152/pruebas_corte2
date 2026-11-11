package pronosticos.unitarias;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import pronosticos.CalculadoraPronosticos;

/** Casos diseñados previamente en docs/CASOS_PROPIOS.md. */
class CasosPropiosTest {
    private final CalculadoraPronosticos calculadora = new CalculadoraPronosticos();

    @Test
    @DisplayName("El promedio conserva las fracciones de la demanda")
    void conservaDecimales() {
        assertEquals(3.0, calculadora.promedioMovil(List.of(2.25, 3.75), 2), 0.000001);
    }

    @Test
    @DisplayName("Una demanda antigua extrema no afecta la ventana reciente")
    void ignoraDatosFueraDeLaVentana() {
        assertEquals(3.0, calculadora.promedioMovil(List.of(9000.0, 2.0, 4.0), 2), 0.000001);
    }

    @Test
    @DisplayName("Los errores de signos opuestos no se cancelan en MAD")
    void noCancelaErroresOpuestos() {
        assertEquals(4.0, calculadora.mad(List.of(10.0, 20.0), List.of(14.0, 16.0)), 0.000001);
    }
}
