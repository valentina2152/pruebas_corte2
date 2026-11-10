package pronosticos.bdd;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

/**
 * Ejecutor de los escenarios BDD.
 *
 * <p>Suite agrupa pruebas e IncludeEngines("cucumber") hace que se ejecuten con Cucumber
 * en lugar de con JUnit Jupiter. SelectPackages("features") indica dónde están los .feature:
 * Cucumber trata la carpeta src/test/resources/features como un paquete del classpath.
 * GLUE_PROPERTY_NAME indica en qué paquete están los pasos.
 *
 * <p>Esta clase ejecuta todos los .feature de la carpeta con todos los pasos del paquete
 * pronosticos.bdd. Una característica nueva no necesita otro ejecutor: basta con agregar
 * su .feature y su clase de pasos en este mismo paquete.
 */
@Suite
@IncludeEngines("cucumber")
@SelectPackages("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "pronosticos.bdd")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty,html:target/cucumber.html")
public class RunCucumberTest {
}
