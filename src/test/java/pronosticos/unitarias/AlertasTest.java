package pronosticos.unitarias;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import pronosticos.CalculadoraPronosticos;

class AlertasTest {

    private final CalculadoraPronosticos calculadora = new CalculadoraPronosticos();

    @Test
    @DisplayName("Genera alerta si el error supera el umbral")
    void alertaAlSuperarUmbral() {
        assertTrue(calculadora.debeGenerarAlerta(12, 10));
    }

    @Test
    @DisplayName("Genera alerta si el error es igual al umbral")
    void alertaAlIgualarUmbral() {
        assertTrue(calculadora.debeGenerarAlerta(10, 10));
    }

    @Test
    @DisplayName("No genera alerta si el error es menor al umbral")
    void noAlertaDebajoDelUmbral() {
        assertFalse(calculadora.debeGenerarAlerta(7, 10));
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1, 150})
    @DisplayName("Rechaza umbrales fuera del rango porcentual")
    void rechazaUmbralInvalido(double umbral) {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> calculadora.debeGenerarAlerta(12, umbral));
        assertEquals("El umbral debe estar entre 0 y 100", error.getMessage());
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, 100})
    @DisplayName("Acepta ambos extremos del rango de umbral")
    void aceptaUmbralesLimite(double umbral) {
        assertTrue(calculadora.debeGenerarAlerta(umbral, umbral));
    }
}
