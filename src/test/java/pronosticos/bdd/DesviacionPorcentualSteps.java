package pronosticos.bdd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dadas;
import io.cucumber.java.es.Entonces;

import pronosticos.CalculadoraPronosticos;

public class DesviacionPorcentualSteps {
    private List<Double> demandas;
    private List<Double> cantidadesPrevistas;
    private double desviacion;

    @Dadas("las demandas observadas de {int}, {int} y {int} unidades")
    public void demandasObservadas(int primera, int segunda, int tercera) {
        demandas = List.of((double) primera, (double) segunda, (double) tercera);
    }

    @Dadas("las cantidades previstas de {int}, {int} y {int} unidades")
    public void cantidadesPrevistas(int primera, int segunda, int tercera) {
        cantidadesPrevistas = List.of((double) primera, (double) segunda, (double) tercera);
    }

    @Cuando("reviso la desviación porcentual de la planificación")
    public void revisoDesviacion() {
        desviacion = new CalculadoraPronosticos().mape(demandas, cantidadesPrevistas);
    }

    @Entonces("la desviación media de los periodos con demanda es {double} %")
    public void desviacionEsperada(double esperada) {
        assertEquals(esperada, desviacion, 0.01);
    }
}
