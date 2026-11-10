package pronosticos.bdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import java.util.List;

import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dada;
import io.cucumber.java.es.Entonces;

import pronosticos.CalculadoraPronosticos;

/**
 * Pasos BDD del pronóstico con promedio móvil.
 *
 * <p>Conecta cada frase de promedio_movil.feature con código. El escenario describe el
 * comportamiento con las palabras de Producción; aquí solo se traduce cada frase a una
 * llamada a la calculadora y a una verificación.
 *
 * <p>Cucumber crea una instancia nueva de esta clase para cada escenario: los campos
 * empiezan vacíos y ningún escenario depende del resultado de otro.
 */
public class PromedioMovilSteps {

    private static final double TOLERANCIA = 0.01;

    private final CalculadoraPronosticos calculadora = new CalculadoraPronosticos();
    private List<Double> serie;
    private Double pronostico;
    private IllegalArgumentException error;

    // Cucumber reconoce el paso por el texto: @Dada y @Dado son la misma palabra clave
    // (Given). Se usa una expresión regular (empieza con ^ y termina con $) porque la serie
    // es una lista separada por comas, que ningún parámetro como {int} captura completa.
    @Dada("^la serie de demanda ([0-9., ]+)$")
    public void laSerieDeDemanda(String texto) {
        serie = Arrays.stream(texto.split(","))
                .map(valor -> Double.valueOf(valor.trim()))
                .toList();
    }

    // {int} captura el número de la frase. En el esquema, <ventana> ya fue reemplazado por
    // el valor de la fila (7 o 0), así que este mismo paso sirve para todas las filas.
    // El paso "Cuando" solo ejecuta la acción y guarda lo que pasó: el resultado o la
    // excepción. Decidir si eso es correcto le corresponde al paso "Entonces".
    @Cuando("calculo el promedio móvil con una ventana de {int} periodos")
    public void calculoElPromedioMovil(int ventana) {
        try {
            pronostico = calculadora.promedioMovil(serie, ventana);
        } catch (IllegalArgumentException e) {
            error = e;
        }
    }

    // {double} lee los números con la convención del idioma del .feature. En español la
    // coma es el separador decimal y el punto el de miles: "143,5" es 143.5, pero "2.63"
    // se lee como 263. Por eso el escenario escribe el resultado sin decimales (143).
    @Entonces("el pronóstico del siguiente periodo es {double} unidades")
    public void elPronosticoDelSiguientePeriodoEs(double esperado) {
        assertNull(error, () -> "Hubo un error inesperado: " + error.getMessage());
        assertEquals(esperado, pronostico, TOLERANCIA);
    }

    // {string} captura el texto entre comillas de la frase y lo entrega sin las comillas.
    @Entonces("se informa el error {string}")
    public void seInformaElError(String mensaje) {
        assertNull(pronostico, "Se esperaba un error y se obtuvo un pronóstico");
        assertNotNull(error, "Se esperaba un error y no se lanzó ninguno");
        assertEquals(mensaje, error.getMessage());
    }
}
