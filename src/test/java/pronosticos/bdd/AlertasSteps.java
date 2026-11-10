package pronosticos.bdd;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;

import pronosticos.CalculadoraPronosticos;

/** Cucumber crea una instancia independiente para cada escenario. */
public class AlertasSteps {

    private final CalculadoraPronosticos calculadora = new CalculadoraPronosticos();
    private double umbral;
    private boolean alerta;

    @Dado("un umbral de alerta de {int} %")
    public void unUmbralDeAlertaDe(int valor) {
        umbral = valor;
    }

    @Cuando("el error del pronóstico es {int} %")
    public void elErrorDelPronosticoEs(int error) {
        alerta = calculadora.debeGenerarAlerta(error, umbral);
    }

    @Entonces("se genera una alerta")
    public void seGeneraUnaAlerta() {
        assertTrue(alerta);
    }

    @Entonces("no se genera una alerta")
    public void noSeGeneraUnaAlerta() {
        assertFalse(alerta);
    }
}
