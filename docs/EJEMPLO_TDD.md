# Ejemplo de TDD: así se habría construido `mad`

El método `mad` (error absoluto medio) ya existe en `CalculadoraPronosticos.java`. Este documento reconstruye, paso a paso, cómo se habría escrito con TDD: primero la prueba, después el código mínimo para que pase y al final la mejora del diseño. Es el mismo ciclo que van a seguir en la actividad, aplicado a un método que ya conocen.

**De dónde salen las salidas.** Cada salida de consola de este documento se obtuvo de verdad, en una copia del proyecto (`/tmp/pronosticos-copia/pronosticos-java`) de la que se quitaron `mad` y sus pruebas. De la salida de Maven se muestra el fragmento que importa: desde la sección de pruebas (o desde el error de compilación) hasta la línea `BUILD`. En su equipo solo cambian la ruta de la carpeta y los tiempos. El código del proyecto no se modificó para escribir este documento.

**Una aclaración sobre el controlador.** En el proyecto, `PronosticoController` ya usa `mad`. Si se quita el método, lo primero que deja de compilar es el controlador, no la prueba. En una construcción guiada por pruebas la capa HTTP llega **después** de la regla de negocio, así que para esta reconstrucción el controlador se retiró de la copia y se devolvió al final.

## El requisito

Antes de escribir una sola línea hay que saber qué se espera. Producción lo describió así:

| Regla | Ejemplo |
|---|---|
| El MAD es el promedio de las diferencias absolutas entre la demanda real y el pronóstico, en unidades | real `[141, 138, 150]`, pronóstico `[126.67, 133.67, 135.67]` → (14.33 + 4.33 + 14.33) / 3 = 11.00 |
| Si el pronóstico es exacto, el MAD es 0 | real `[100, 100]`, pronóstico `[100, 100]` → 0 |
| Las dos series deben tener el mismo número de periodos | `[100, 110]` y `[100]` → `IllegalArgumentException` "Las series deben tener el mismo número de periodos" |
| Se necesita al menos un periodo | `[]` y `[]` → `IllegalArgumentException` "Se necesita al menos un periodo" |

Cada fila de la tabla se convierte en un ciclo rojo-verde-refactor.

---

## Ciclo 1. El cálculo con el ejemplo de Producción

### Rojo

La prueba describe el primer comportamiento con el ejemplo calculado a mano. Se crea `src/test/java/pronosticos/unitarias/MadTest.java`:

```java
package pronosticos.unitarias;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import pronosticos.CalculadoraPronosticos;

/** Pruebas unitarias del error absoluto medio (MAD). */
class MadTest {

    private static final double TOLERANCIA = 0.01;

    private CalculadoraPronosticos calculadora;

    @BeforeEach
    void prepararCalculadora() {
        calculadora = new CalculadoraPronosticos();
    }

    @Test
    @DisplayName("Calcula el error absoluto medio")
    void calculaElErrorAbsolutoMedio() {
        double resultado = calculadora.mad(List.of(141.0, 138.0, 150.0), List.of(126.67, 133.67, 135.67));

        // (14.33 + 4.33 + 14.33) / 3 = 11.00, calculado de forma independiente.
        assertEquals(11.00, resultado, TOLERANCIA);
    }
}
```

```bash
mvn test -Dtest='**/unitarias/*Test'
```

```
[ERROR] COMPILATION ERROR :
[INFO] -------------------------------------------------------------
[ERROR] /private/tmp/pronosticos-copia/pronosticos-java/src/test/java/pronosticos/unitarias/MadTest.java:[28,39] cannot find symbol
  symbol:   method mad(java.util.List<java.lang.Double>,java.util.List<java.lang.Double>)
  location: variable calculadora of type pronosticos.CalculadoraPronosticos
[INFO] 1 error
[INFO] -------------------------------------------------------------
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
```

En Java el rojo suele ser un error de compilación: la prueba usa un método que todavía no existe, y el compilador lo detecta antes de ejecutar nada. `[28,39]` es la línea y la columna de la llamada a `mad`.

### Verde

El código **mínimo** para que esa prueba pase es devolver el valor esperado:

```java
    public double mad(List<Double> real, List<Double> pronostico) {
        return 11.0;
    }
```

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running pronosticos.unitarias.MadTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.027 s -- in pronosticos.unitarias.MadTest
[INFO] Running pronosticos.unitarias.PromedioMovilTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.028 s -- in pronosticos.unitarias.PromedioMovilTest
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO]
[INFO] --- jacoco:0.8.14:report (reporte) @ pronosticos-java ---
[INFO] Loading execution data file /private/tmp/pronosticos-copia/pronosticos-java/target/jacoco.exec
[INFO] Analyzed bundle 'pronosticos-java' with 1 classes
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
```

### Refactor

Todavía no hay nada que mejorar: el método tiene una sola línea y no repite nada.

### Qué se aprende

- Ver el rojo primero demuestra que la prueba es capaz de fallar. Una prueba que nunca se vio fallar puede estar pasando por accidente (por ejemplo, porque no se ejecuta).
- Devolver una constante parece hacer trampa, pero es deliberado: obliga a que **otra prueba** exija el cálculo real. Si con una sola prueba se escribe la fórmula completa, nada garantiza que cada parte de la fórmula esté verificada.

---

## Ciclo 2. Un segundo caso obliga a calcular de verdad

### Rojo

```java
    @Test
    @DisplayName("Es cero cuando el pronóstico es exacto")
    void esCeroCuandoElPronosticoEsExacto() {
        assertEquals(0.0, calculadora.mad(List.of(100.0, 100.0), List.of(100.0, 100.0)), TOLERANCIA);
    }
```

```bash
mvn test -Dtest='**/unitarias/*Test'
```

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running pronosticos.unitarias.MadTest
[ERROR] Tests run: 2, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 0.034 s <<< FAILURE! -- in pronosticos.unitarias.MadTest
[ERROR] pronosticos.unitarias.MadTest.esCeroCuandoElPronosticoEsExacto -- Time elapsed: 0.024 s <<< FAILURE!
org.opentest4j.AssertionFailedError: expected: <0.0> but was: <11.0>
	at org.junit.jupiter.api.AssertionFailureBuilder.build(AssertionFailureBuilder.java:158)
	at org.junit.jupiter.api.AssertionFailureBuilder.buildAndThrow(AssertionFailureBuilder.java:139)
	at org.junit.jupiter.api.AssertEquals.failNotEqual(AssertEquals.java:201)
	at org.junit.jupiter.api.AssertEquals.assertEquals(AssertEquals.java:88)
	at org.junit.jupiter.api.AssertEquals.assertEquals(AssertEquals.java:83)
	at org.junit.jupiter.api.Assertions.assertEquals(Assertions.java:1052)
	at pronosticos.unitarias.MadTest.esCeroCuandoElPronosticoEsExacto(MadTest.java:37)

[INFO] Running pronosticos.unitarias.PromedioMovilTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.031 s -- in pronosticos.unitarias.PromedioMovilTest
[INFO]
[INFO] Results:
[INFO]
[ERROR] Failures:
[ERROR]   MadTest.esCeroCuandoElPronosticoEsExacto:37 expected: <0.0> but was: <11.0>
[INFO]
[ERROR] Tests run: 8, Failures: 1, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
```

Ahora sí compila, la prueba se ejecuta y falla: la constante 11.0 no sirve para este caso.

### Verde

Con dos ejemplos distintos, el único código que pasa los dos es el cálculo real:

```java
    public double mad(List<Double> real, List<Double> pronostico) {
        double sumaErrores = 0;
        for (int i = 0; i < real.size(); i++) {
            sumaErrores += Math.abs(real.get(i) - pronostico.get(i));
        }
        return sumaErrores / real.size();
    }
```

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running pronosticos.unitarias.MadTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.025 s -- in pronosticos.unitarias.MadTest
[INFO] Running pronosticos.unitarias.PromedioMovilTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.028 s -- in pronosticos.unitarias.PromedioMovilTest
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO]
[INFO] --- jacoco:0.8.14:report (reporte) @ pronosticos-java ---
[INFO] Loading execution data file /private/tmp/pronosticos-copia/pronosticos-java/target/jacoco.exec
[INFO] Analyzed bundle 'pronosticos-java' with 1 classes
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
```

### Refactor

El código es corto y claro. Se deja como está hasta tener las validaciones.

### Qué se aprende

- Esta técnica se llama **triangulación**: cada ejemplo nuevo descarta una implementación falsa. Dos ejemplos que no pueden resolverse con la misma constante obligan a escribir la fórmula.
- JUnit muestra el valor esperado (`0.0`) y el obtenido (`11.0`), y Surefire repite el resumen al final con la línea de la prueba (`MadTest.java:37`). Por eso conviene que cada prueba verifique un solo comportamiento: el mensaje dice exactamente qué falló.

---

## Ciclo 3. Series de distinta longitud

### Rojo

```java
    @Test
    @DisplayName("Rechaza series de distinta longitud")
    void rechazaSeriesDeDistintaLongitud() {
        assertThrows(IllegalArgumentException.class,
                () -> calculadora.mad(List.of(100.0, 110.0), List.of(100.0)));
    }
```

(con `import static org.junit.jupiter.api.Assertions.assertThrows;`)

```bash
mvn test -Dtest='**/unitarias/*Test'
```

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running pronosticos.unitarias.MadTest
[ERROR] Tests run: 3, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 0.031 s <<< FAILURE! -- in pronosticos.unitarias.MadTest
[ERROR] pronosticos.unitarias.MadTest.rechazaSeriesDeDistintaLongitud -- Time elapsed: 0.003 s <<< FAILURE!
org.opentest4j.AssertionFailedError: Unexpected exception type thrown, expected: <java.lang.IllegalArgumentException> but was: <java.lang.IndexOutOfBoundsException>
	at org.junit.jupiter.api.AssertionFailureBuilder.build(AssertionFailureBuilder.java:158)
	at org.junit.jupiter.api.AssertThrows.assertThrows(AssertThrows.java:68)
	at org.junit.jupiter.api.AssertThrows.assertThrows(AssertThrows.java:35)
	at org.junit.jupiter.api.Assertions.assertThrows(Assertions.java:3223)
	at pronosticos.unitarias.MadTest.rechazaSeriesDeDistintaLongitud(MadTest.java:44)
Caused by: java.lang.IndexOutOfBoundsException: Index: 1 Size: 1
	at java.base/java.util.ImmutableCollections$AbstractImmutableList.outOfBounds(ImmutableCollections.java:359)
	at java.base/java.util.ImmutableCollections$List12.get(ImmutableCollections.java:612)
	at pronosticos.CalculadoraPronosticos.mad(CalculadoraPronosticos.java:34)
	at pronosticos.unitarias.MadTest.lambda$rechazaSeriesDeDistintaLongitud$0(MadTest.java:45)
	at org.junit.jupiter.api.AssertThrows.assertThrows(AssertThrows.java:54)
	... 3 more

[INFO] Running pronosticos.unitarias.PromedioMovilTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.030 s -- in pronosticos.unitarias.PromedioMovilTest
[INFO]
[INFO] Results:
[INFO]
[ERROR] Failures:
[ERROR]   MadTest.rechazaSeriesDeDistintaLongitud:44 Unexpected exception type thrown, expected: <java.lang.IllegalArgumentException> but was: <java.lang.IndexOutOfBoundsException>
[INFO]
[ERROR] Tests run: 9, Failures: 1, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
```

Hubo una excepción, pero no la acordada: el ciclo recorre `real` y pide `pronostico.get(1)`, que no existe, y Java lanza `IndexOutOfBoundsException`. Para quien llama al método, ese error no explica qué hizo mal.

### Verde

```java
    public double mad(List<Double> real, List<Double> pronostico) {
        if (real.size() != pronostico.size()) {
            throw new IllegalArgumentException("Las series deben tener el mismo número de periodos");
        }
        double sumaErrores = 0;
        for (int i = 0; i < real.size(); i++) {
            sumaErrores += Math.abs(real.get(i) - pronostico.get(i));
        }
        return sumaErrores / real.size();
    }
```

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running pronosticos.unitarias.MadTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.028 s -- in pronosticos.unitarias.MadTest
[INFO] Running pronosticos.unitarias.PromedioMovilTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.030 s -- in pronosticos.unitarias.PromedioMovilTest
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO]
[INFO] --- jacoco:0.8.14:report (reporte) @ pronosticos-java ---
[INFO] Loading execution data file /private/tmp/pronosticos-copia/pronosticos-java/target/jacoco.exec
[INFO] Analyzed bundle 'pronosticos-java' with 1 classes
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
```

### Refactor

Aún nada: hay una sola validación.

### Qué se aprende

- Las pruebas de los casos inválidos no son un trámite: definen qué error recibe quien usa el método.
- `assertThrows` comprueba el **tipo** de la excepción. Si llega otra, la prueba falla y dice cuál llegó.

---

## Ciclo 4. Series vacías

### Rojo

```java
    @Test
    @DisplayName("Rechaza series vacías")
    void rechazaSeriesVacias() {
        assertThrows(IllegalArgumentException.class, () -> calculadora.mad(List.of(), List.of()));
    }
```

```bash
mvn test -Dtest='**/unitarias/*Test'
```

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running pronosticos.unitarias.MadTest
[ERROR] Tests run: 4, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 0.034 s <<< FAILURE! -- in pronosticos.unitarias.MadTest
[ERROR] pronosticos.unitarias.MadTest.rechazaSeriesVacias -- Time elapsed: 0.003 s <<< FAILURE!
org.opentest4j.AssertionFailedError: Expected java.lang.IllegalArgumentException to be thrown, but nothing was thrown.
	at org.junit.jupiter.api.AssertionFailureBuilder.build(AssertionFailureBuilder.java:159)
	at org.junit.jupiter.api.AssertThrows.assertThrows(AssertThrows.java:74)
	at org.junit.jupiter.api.AssertThrows.assertThrows(AssertThrows.java:35)
	at org.junit.jupiter.api.Assertions.assertThrows(Assertions.java:3223)
	at pronosticos.unitarias.MadTest.rechazaSeriesVacias(MadTest.java:51)

[INFO] Running pronosticos.unitarias.PromedioMovilTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.027 s -- in pronosticos.unitarias.PromedioMovilTest
[INFO]
[INFO] Results:
[INFO]
[ERROR] Failures:
[ERROR]   MadTest.rechazaSeriesVacias:51 Expected java.lang.IllegalArgumentException to be thrown, but nothing was thrown.
[INFO]
[ERROR] Tests run: 10, Failures: 1, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
```

Esta vez no hubo ninguna excepción. Con dos listas vacías el método calcula `0.0 / 0`, y en Java dividir un `double` entre cero no es un error. Un programa de una sola clase muestra qué devuelve:

```java
import java.util.List;
public class Demo { public static void main(String[] a) { System.out.println(new pronosticos.CalculadoraPronosticos().mad(List.of(), List.of())); } }
```

```bash
java -cp target/classes Demo.java
```

```
NaN
```

`NaN` (*not a number*) llegaría hasta la pantalla de Producción sin que nadie supiera por qué.

### Verde

```java
    public double mad(List<Double> real, List<Double> pronostico) {
        if (real.size() != pronostico.size()) {
            throw new IllegalArgumentException("Las series deben tener el mismo número de periodos");
        }
        if (real.isEmpty()) {
            throw new IllegalArgumentException("Se necesita al menos un periodo");
        }
        double sumaErrores = 0;
        for (int i = 0; i < real.size(); i++) {
            sumaErrores += Math.abs(real.get(i) - pronostico.get(i));
        }
        return sumaErrores / real.size();
    }
```

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running pronosticos.unitarias.MadTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.027 s -- in pronosticos.unitarias.MadTest
[INFO] Running pronosticos.unitarias.PromedioMovilTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.026 s -- in pronosticos.unitarias.PromedioMovilTest
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO]
[INFO] --- jacoco:0.8.14:report (reporte) @ pronosticos-java ---
[INFO] Loading execution data file /private/tmp/pronosticos-copia/pronosticos-java/target/jacoco.exec
[INFO] Analyzed bundle 'pronosticos-java' with 1 classes
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
```

### Qué se aprende

- La prueba no solo exige que haya un error: exige **el** error acordado. Eso es parte del contrato del método.
- Con este ciclo la tabla del requisito queda cubierta. Ahora se puede mejorar el diseño con la tranquilidad de que cuatro pruebas vigilan el comportamiento.

---

## Refactor final

Con todas las pruebas en verde se mejora el código **sin cambiar lo que hace**:

1. **Quitar la duplicación.** Las dos validaciones son idénticas a las que necesita `mape`, que ya las tiene en `validarSeries`. Se reutiliza ese método en lugar de mantener dos copias de la misma regla.
2. **Documentación.** Un Javadoc explica qué es el MAD.

Antes:

```java
    public double mad(List<Double> real, List<Double> pronostico) {
        if (real.size() != pronostico.size()) {
            throw new IllegalArgumentException("Las series deben tener el mismo número de periodos");
        }
        if (real.isEmpty()) {
            throw new IllegalArgumentException("Se necesita al menos un periodo");
        }
        double sumaErrores = 0;
        for (int i = 0; i < real.size(); i++) {
            sumaErrores += Math.abs(real.get(i) - pronostico.get(i));
        }
        return sumaErrores / real.size();
    }
```

Después (es exactamente el código que hoy tiene el proyecto):

```java
    /** Error absoluto medio (MAD): promedio de las diferencias absolutas, en unidades. */
    public double mad(List<Double> real, List<Double> pronostico) {
        validarSeries(real, pronostico);

        double sumaErrores = 0;
        for (int i = 0; i < real.size(); i++) {
            sumaErrores += Math.abs(real.get(i) - pronostico.get(i));
        }
        return sumaErrores / real.size();
    }
```

Con el método terminado se devuelve `PronosticoController` a su lugar y se corren todas las pruebas unitarias, porque `validarSeries` también la usa `mape`:

```bash
mvn test -Dtest='**/unitarias/*Test'
```

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running pronosticos.unitarias.MadTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.028 s -- in pronosticos.unitarias.MadTest
[INFO] Running pronosticos.unitarias.PromedioMovilTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.027 s -- in pronosticos.unitarias.PromedioMovilTest
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO]
[INFO] --- jacoco:0.8.14:report (reporte) @ pronosticos-java ---
[INFO] Loading execution data file /private/tmp/pronosticos-copia/pronosticos-java/target/jacoco.exec
[INFO] Analyzed bundle 'pronosticos-java' with 1 classes
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
```

Y el análisis estático, para confirmar que el refactor no introdujo problemas:

```bash
mvn checkstyle:check
```

```
[INFO] --- checkstyle:3.6.0:check (default-cli) @ pronosticos-java ---
[INFO] Comenzando auditoría...
Auditoría concluida.
[INFO] You have 0 Checkstyle violations.
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
```

### Qué se aprende

- El refactor es seguro **porque** hay pruebas. Sin ellas, cambiar las validaciones por `validarSeries` sería una apuesta.
- Refactorizar no es agregar funciones nuevas. Si durante el refactor aparece un comportamiento nuevo, se vuelve al rojo con una prueba para ese comportamiento.

---

## El historial que deja el ciclo

En la actividad se pide que el historial de commits muestre las tres fases. Para `mad`, un historial razonable sería:

| Commit | Fase | Estado de las pruebas |
|---|---|---|
| `test: prueba en rojo para mad` | Rojo del ciclo 1 | no compila |
| `feat: mad devuelve el resultado del ejemplo` | Verde del ciclo 1 | todas pasan |
| `test: mad es cero con un pronóstico exacto` | Rojo del ciclo 2 | 1 falla |
| `feat: mad calcula el promedio de los errores absolutos` | Verde del ciclo 2 | todas pasan |
| `test: mad rechaza series de distinta longitud` | Rojo del ciclo 3 | 1 falla |
| `feat: mad valida la longitud de las series` | Verde del ciclo 3 | todas pasan |
| `test: mad rechaza series vacías` | Rojo del ciclo 4 | 1 falla |
| `feat: mad exige al menos un periodo` | Verde del ciclo 4 | todas pasan |
| `refactor: mad reutiliza validarSeries` | Refactor | todas pasan |

No es obligatorio un commit por fase de cada ciclo; lo importante es que el historial permita ver que la prueba existió antes que el código.

## Resumen

| Fase | Qué se hace | Cómo se sabe que terminó |
|---|---|---|
| Rojo | Se escribe una prueba para un comportamiento que todavía no existe | La prueba falla (o no compila), y falla por la razón esperada |
| Verde | Se escribe el código mínimo para que pase | Todas las pruebas pasan |
| Refactor | Se mejora el diseño sin cambiar el comportamiento | Todas las pruebas siguen pasando y el análisis estático no reporta nada |
