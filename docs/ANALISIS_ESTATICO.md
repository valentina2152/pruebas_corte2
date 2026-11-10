# Análisis estático con Checkstyle

Guía de referencia para entender, ejecutar y modificar el análisis estático de este proyecto. Todas las salidas de consola que aparecen aquí se obtuvieron ejecutando los comandos en una copia del proyecto (`/tmp/pronosticos-copia/pronosticos-java`), con Checkstyle 13.3.0 y maven-checkstyle-plugin 3.6.0. Los archivos de ejemplo (en `src/main/java/ejemplos/`) se crearon solo para provocar cada hallazgo y no forman parte del repositorio.

**Sobre el idioma de los mensajes.** Checkstyle escribe los mensajes en el idioma del sistema operativo. En el equipo donde se preparó esta guía salen en español; en el runner de GitHub Actions salen en inglés. Por eso, para cada regla, se muestran los dos mensajes (el inglés se obtuvo con `MAVEN_OPTS="-Duser.language=en -Duser.country=US"`). El nombre de la regla, entre corchetes al final, es el mismo en cualquier idioma.

## 1. Qué es el análisis estático

El análisis estático revisa el código **sin ejecutarlo**. Lee el texto del programa y lo compara con un conjunto de reglas: importaciones y variables que sobran, bloques vacíos, nombres que no siguen las convenciones, métodos demasiado complejos.

Las pruebas (análisis dinámico) ejecutan el código y comparan el resultado con lo esperado. Por eso solo encuentran problemas en las líneas que recorren. El análisis estático encuentra lo que las pruebas no ven:

| Lo encuentra el análisis estático | Por qué las pruebas no lo ven |
|---|---|
| Un `if` vacío donde faltó escribir la validación | El código compila y las pruebas que no tocan ese caso pasan |
| Una variable calculada que nadie usa | El resultado es correcto; el problema es que sobra código |
| Un método con demasiadas decisiones | Funciona, pero es difícil de entender, de probar y de cambiar |
| Nombres que no siguen las convenciones de Java | No cambia el comportamiento, cambia la mantenibilidad |

El temario ISTQB CTFL v4.0 (sección 3.1.3) lo resume así: el análisis estático encuentra defectos directamente, puede detectar defectos en caminos que se ejecutan rara vez y puede medir características que no dependen de ejecutar el código, como la mantenibilidad. Las dos técnicas se complementan; ninguna reemplaza a la otra.

En Java, el compilador ya hace parte de este trabajo (un método que no existe no compila). Checkstyle revisa lo que el compilador acepta pero el equipo no quiere: código sobrante, estructuras frágiles y convenciones.

## 2. La herramienta y su configuración

[Checkstyle](https://checkstyle.org/) es un analizador estático de código Java. Se ejecuta con Maven a través del plugin `maven-checkstyle-plugin`. La configuración está en dos archivos.

**`pom.xml`**, en el plugin `maven-checkstyle-plugin`, dice cómo se ejecuta:

```xml
<configuration>
    <configLocation>config/checkstyle.xml</configLocation>
    <includeTestSourceDirectory>true</includeTestSourceDirectory>
    <consoleOutput>true</consoleOutput>
    <failOnViolation>true</failOnViolation>
    <violationSeverity>warning</violationSeverity>
</configuration>
```

| Opción | Qué hace |
|---|---|
| `configLocation` | Archivo con las reglas. Sin esta opción el plugin usa `sun_checks.xml`, un conjunto mucho más amplio |
| `includeTestSourceDirectory` | Revisa también `src/test/java` (por defecto no lo hace) |
| `consoleOutput` | Imprime cada hallazgo en la consola |
| `failOnViolation` | Hace fallar el build si hay hallazgos (es el valor por defecto) |
| `violationSeverity` | Desde qué severidad un hallazgo cuenta como violación. Con `warning`, cuentan los de severidad `warning` y `error` |

**`config/checkstyle.xml`** dice qué se revisa. Es un árbol de módulos: `Checker` es la raíz y revisa archivos completos; `TreeWalker` recorre la estructura de cada clase y contiene casi todas las reglas.

```xml
<module name="Checker">
    <property name="charset" value="UTF-8"/>
    <property name="severity" value="error"/>

    <module name="FileTabCharacter"/>

    <module name="TreeWalker">
        <module name="UnusedImports"/>
        <module name="RedundantImport"/>
        <module name="UnusedLocalVariable"/>
        <module name="EmptyBlock"/>
        <module name="AvoidStarImport"/>
        <module name="NeedBraces"/>
        <module name="MultipleVariableDeclarations"/>
        <module name="MethodName"/>
        <module name="LocalVariableName"/>
        <module name="ParameterName"/>
        <module name="ConstantName"/>
        <module name="TypeName"/>
        <module name="CyclomaticComplexity">
            <property name="max" value="5"/>
        </module>
    </module>
</module>
```

`severity="error"` en `Checker` hace que todas las reglas reporten con severidad de error. Son 14 reglas, todas con sus valores por defecto salvo `CyclomaticComplexity`.

## 3. Las reglas configuradas

Cada ejemplo se creó en `src/main/java/ejemplos/` y se confirmó que `mvn checkstyle:check` lo reporta. También se comprobó que, con las correcciones, la misma ejecución termina con `You have 0 Checkstyle violations.`

### 3.1 FileTabCharacter: sin tabulaciones

Detecta caracteres de tabulación en el archivo. Una tabulación se ve con un ancho distinto en cada editor y descuadra la indentación. Está bajo `Checker` porque revisa el archivo como texto. Por defecto reporta solo la primera tabulación de cada archivo.

```java
public class Tabulaciones {

→   public double doble(double valor) {
→   →   return valor * 2;
→   }
}
```

(`→` representa una tabulación.)

```
Tabulaciones.java:5:1: Archivo contiene caracteres de tabulación (este es el primer ejemplo). [FileTabCharacter]
Tabulaciones.java:5:1: File contains tab characters (this is the first instance). [FileTabCharacter]
```

Corrección: indentar con cuatro espacios. La mayoría de los editores lo hacen solos si se configura "insertar espacios".

### 3.2 UnusedImports: importaciones sin usar

```java
import java.util.ArrayList;
import java.util.List;

public class ImportacionSinUsar {

    public int periodos(List<Double> serie) {
        return serie.size();
    }
}
```

```
ImportacionSinUsar.java:3:8: Importación no usada - java.util.ArrayList. [UnusedImports]
ImportacionSinUsar.java:3:8: Unused import - java.util.ArrayList. [UnusedImports]
```

Corrección: borrar `import java.util.ArrayList;`.

### 3.3 RedundantImport: importaciones innecesarias

Detecta importaciones repetidas, del mismo paquete de la clase o de `java.lang`, que Java importa siempre.

```java
import java.lang.Math;

public class ImportacionRedundante {

    public double errorAbsoluto(double real, double pronostico) {
        return Math.abs(real - pronostico);
    }
}
```

```
ImportacionRedundante.java:3:1: Importación redundante del paquete java.lang - java.lang.Math. [RedundantImport]
ImportacionRedundante.java:3:8: Importación no usada - java.lang.Math. [UnusedImports]
```

```
ImportacionRedundante.java:3:1: Redundant import from the java.lang package - java.lang.Math. [RedundantImport]
ImportacionRedundante.java:3:8: Unused import - java.lang.Math. [UnusedImports]
```

Una línea dispara dos reglas: para `UnusedImports`, una importación de `java.lang` tampoco se usa, porque la clase ya estaba disponible sin ella. Corrección: borrar la importación.

### 3.4 UnusedLocalVariable: variables locales sin usar

```java
    public double promedio(List<Double> serie) {
        int totalPeriodos = serie.size();
        double suma = 0;
        for (double valor : serie) {
            suma += valor;
        }
        return suma / 3;
    }
```

```
VariableSinUsar.java:8:9: Variable local con nombre no utilizada 'totalPeriodos'. [UnusedLocalVariable]
VariableSinUsar.java:8:9: Unused named local variable 'totalPeriodos'. [UnusedLocalVariable]
```

Aquí el hallazgo revela un defecto real: el método divide entre 3 en lugar de entre el número de periodos. Corrección: `return suma / totalPeriodos;`.

### 3.5 EmptyBlock: bloques vacíos

Detecta bloques `{}` vacíos en `if`, `else`, `for`, `while`, `do`, `try`, `finally`, `switch`, `synchronized` y bloques de inicialización. Con la opción por defecto (`statement`) el bloque debe tener al menos una sentencia; un comentario no basta. Los `catch` vacíos no los revisa esta regla (existe otra, `EmptyCatchBlock`, que no está activa).

```java
    public double ventanaValida(int ventana) {
        if (ventana <= 0) {
        }
        return ventana;
    }
```

```
BloqueVacio.java:6:27: Debe tener al menos una sentencia. [EmptyBlock]
BloqueVacio.java:6:27: Must have at least one statement. [EmptyBlock]
```

Un `if` vacío casi siempre es una validación que se quedó sin escribir. Corrección:

```java
        if (ventana <= 0) {
            throw new IllegalArgumentException("La ventana debe ser un entero positivo");
        }
```

### 3.6 AvoidStarImport: importaciones con asterisco

```java
import java.util.*;
```

```
ImportacionConAsterisco.java:3:17: Usar la importación con '.*' debería evitarse - java.util.*. [AvoidStarImport]
ImportacionConAsterisco.java:3:17: Using the '.*' form of import should be avoided - java.util.*. [AvoidStarImport]
```

Con `*` no se ve de dónde viene cada clase, y dos paquetes con una clase del mismo nombre producen conflictos. Corrección: importar cada clase, `import java.util.List;`.

### 3.7 NeedBraces: llaves obligatorias

Exige llaves en `if`, `else`, `for`, `while` y `do`, aunque el bloque tenga una sola línea.

```java
        if (error >= umbral)
            return true;
        return false;
```

```
SinLlaves.java:6:9: La construcción 'if' debe usar '{}' (llaves). [NeedBraces]
SinLlaves.java:6:9: 'if' construct must use '{}'s. [NeedBraces]
```

Sin llaves, una segunda línea agregada después parece estar dentro del `if` y no lo está. Corrección:

```java
        if (error >= umbral) {
            return true;
        }
        return false;
```

### 3.8 MultipleVariableDeclarations: una variable por declaración

```java
        double inicio = minimo, fin = maximo;
```

```
VariasDeclaraciones.java:6:9: Cada declaración de variable debe estar en su línea. [MultipleVariableDeclarations]
VariasDeclaraciones.java:6:9: Each variable declaration must be in its own statement. [MultipleVariableDeclarations]
```

Corrección:

```java
        double inicio = minimo;
        double fin = maximo;
```

### 3.9 MethodName, LocalVariableName, ParameterName, ConstantName y TypeName: convenciones de nombres

Las cinco reglas comparan cada nombre con una expresión regular. Los patrones son los valores por defecto de Checkstyle y corresponden a las convenciones de Java:

| Regla | Qué nombres revisa | Patrón | Ejemplo válido |
|---|---|---|---|
| MethodName | Métodos | `^[a-z][a-zA-Z0-9]*$` | `calcularPromedio` |
| LocalVariableName | Variables locales | `^([a-z][a-zA-Z0-9]*\|_)$` | `sumaTotal` |
| ParameterName | Parámetros | `^[a-z][a-zA-Z0-9]*$` | `ventanaMaxima` |
| ConstantName | Constantes (`static final`) | `^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$` | `TOLERANCIA` |
| TypeName | Clases, interfaces, enums, records | `^[A-Z][a-zA-Z0-9]*$` | `CalculadoraAuxiliar` |

Un ejemplo de cada una:

```java
    public double Calcular_Promedio(double a, double b) {        // MethodName
        double Suma_Total = a + b;                              // LocalVariableName
    public double ultimo(double[] serie, int Ventana_Maxima) {  // ParameterName
    private static final double tolerancia = 0.01;              // ConstantName
public class calculadora_auxiliar {                             // TypeName
```

```
NombreDeMetodo.java:5:19: El nombre 'Calcular_Promedio' debe coincidir con el patrón '^[a-z][a-zA-Z0-9]*$'. [MethodName]
NombreDeVariable.java:6:16: El nombre 'Suma_Total' debe coincidir con el patrón '^([a-z][a-zA-Z0-9]*|_)$'. [LocalVariableName]
NombreDeParametro.java:5:46: El nombre 'Ventana_Maxima' debe coincidir con el patrón '^[a-z][a-zA-Z0-9]*$'. [ParameterName]
NombreDeConstante.java:5:33: El nombre 'tolerancia' debe coincidir con el patrón '^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$'. [ConstantName]
calculadora_auxiliar.java:3:14: El nombre 'calculadora_auxiliar' debe coincidir con el patrón '^[A-Z][a-zA-Z0-9]*$'. [TypeName]
```

```
NombreDeMetodo.java:5:19: Name 'Calcular_Promedio' must match pattern '^[a-z][a-zA-Z0-9]*$'. [MethodName]
NombreDeVariable.java:6:16: Name 'Suma_Total' must match pattern '^([a-z][a-zA-Z0-9]*|_)$'. [LocalVariableName]
NombreDeParametro.java:5:46: Name 'Ventana_Maxima' must match pattern '^[a-z][a-zA-Z0-9]*$'. [ParameterName]
NombreDeConstante.java:5:33: Name 'tolerancia' must match pattern '^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$'. [ConstantName]
calculadora_auxiliar.java:3:14: Name 'calculadora_auxiliar' must match pattern '^[A-Z][a-zA-Z0-9]*$'. [TypeName]
```

Corrección: `calcularPromedio`, `sumaTotal`, `ventanaMaxima`, `TOLERANCIA` y `CalculadoraAuxiliar` (en Java el archivo debe llamarse igual que la clase pública, así que también se renombra el archivo).

### 3.10 CyclomaticComplexity con máximo 5: complejidad ciclomática

Cuenta los caminos independientes de un método: 1 más cada decisión (`if`, `while`, `do`, `for`, `?:`, `catch`, `switch`, `case`, y cada `&&` y `||`). Con más de 5 caminos un método necesita más pruebas para cubrirse y cuesta más entenderlo. El máximo por defecto de Checkstyle es 10; el proyecto lo bajó a 5.

```java
    public String clasificarError(double error, double umbral) {
        if (error < 0) {
            return "inválido";
        }
        if (umbral <= 0) {
            return "sin umbral";
        }
        if (error >= umbral * 2) {
            return "crítico";
        }
        if (error >= umbral) {
            return "alerta";
        }
        if (error >= umbral / 2) {
            return "vigilar";
        }
        return "normal";
    }
```

```
Complejidad.java:5:5: La complejidad ciclomática es 6 (máxima permitida es 5). [CyclomaticComplexity]
Complejidad.java:5:5: Cyclomatic Complexity is 6 (max allowed is 5). [CyclomaticComplexity]
```

Cinco `if` más uno: complejidad 6. Corrección: convertir la cadena de decisiones en datos. El comportamiento no cambia (se comprobó con 27 combinaciones de error y umbral), pero agregar un nivel ya no agrega una rama:

```java
public class Complejidad {

    /**
     * Cada nivel es un dato y no una rama de código: agregar un nivel no aumenta la
     * complejidad del método. values() recorre los niveles en el orden en que se declaran.
     */
    private enum Nivel {
        CRITICO(2, "crítico"),
        ALERTA(1, "alerta"),
        VIGILAR(0.5, "vigilar");

        private final double proporcion;
        private final String nombre;

        Nivel(double proporcion, String nombre) {
            this.proporcion = proporcion;
            this.nombre = nombre;
        }
    }

    public String clasificarError(double error, double umbral) {
        if (error < 0) {
            return "inválido";
        }
        if (umbral <= 0) {
            return "sin umbral";
        }
        for (Nivel nivel : Nivel.values()) {
            if (error >= umbral * nivel.proporcion) {
                return nivel.nombre;
            }
        }
        return "normal";
    }
}
```

## 4. Cómo se ejecuta y cómo se lee un hallazgo

```bash
mvn checkstyle:check
```

Checkstyle no necesita compilar: lee los archivos fuente de `src/main/java` y, en este proyecto, también los de `src/test/java`. Con los 14 ejemplos de la sección 3 juntos, la salida fue (fragmento):

```
[ERROR] /private/tmp/pronosticos-copia/pronosticos-java/src/main/java/ejemplos/ImportacionSinUsar.java:3:8: Importación no usada - java.util.ArrayList. [UnusedImports]
...
Auditoría concluida.
[INFO] There are 15 errors reported by Checkstyle 13.3.0 with config/checkstyle.xml ruleset.
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
...
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-checkstyle-plugin:3.6.0:check (default-cli) on project pronosticos-java: You have 15 Checkstyle violations. -> [Help 1]
```

| Parte | En el ejemplo | Qué significa |
|---|---|---|
| Archivo | `…/ejemplos/ImportacionSinUsar.java` | Ruta completa del archivo |
| Ubicación | `3:8` | Línea y columna |
| Mensaje | `Importación no usada - java.util.ArrayList.` | Qué encontró, con el nombre concreto |
| Regla | `[UnusedImports]` | Nombre del módulo de `config/checkstyle.xml`. Se busca en https://checkstyle.org/checks.html |
| Resumen | `There are 15 errors…` y `You have 15 Checkstyle violations.` | Total de hallazgos |

Después de la lista, el plugin repite cada hallazgo en otro formato, con el grupo de la regla entre paréntesis: `src/main/java/ejemplos/ImportacionSinUsar.java:[3,8] (imports) UnusedImports: Importación no usada - java.util.ArrayList.` Es la misma información.

Sin hallazgos, la salida termina así:

```
[INFO] You have 0 Checkstyle violations.
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
```

### El código de salida

El pipeline no lee los mensajes: decide con el **código de salida** del comando. Maven termina con 0 cuando el build es `BUILD SUCCESS` y con 1 cuando es `BUILD FAILURE`; con `failOnViolation` en `true`, cualquier hallazgo produce `BUILD FAILURE`.

En GitHub Actions, un paso cuyo comando termina con un código distinto de 0 falla, el job queda en rojo y los jobs que dependen de él (`needs`) no se ejecutan.

## 5. Cómo corregir

Checkstyle **no corrige nada solo**: solo reporta. Todas las correcciones de la sección 3 se hicieron a mano y se verificaron corriendo de nuevo `mvn checkstyle:check` (y las pruebas, cuando el cambio tocaba la lógica).

Algunas correcciones mecánicas las hace el editor: la mayoría de los IDE de Java ordenan y limpian las importaciones (lo que resuelve `UnusedImports`, `RedundantImport` y `AvoidStarImport`), convierten tabulaciones en espacios y renombran una variable o un método en todos los lugares donde se usa. Las demás (bloques vacíos, complejidad, variables sin usar) exigen una decisión: qué validación faltaba, cómo dividir el método, para qué era la variable.

## 6. Cómo cambiar la configuración

Todos los cambios de esta sección se probaron en una copia del proyecto y después se revirtieron.

### Agregar una regla

Se agrega un módulo dentro de `TreeWalker`. Por ejemplo, `MagicNumber` reporta números escritos directamente en el código en lugar de una constante con nombre (por defecto permite -1, 0, 1 y 2):

```xml
        <!-- Regla agregada: números sin nombre en el código (se permiten -1, 0, 1 y 2). -->
        <module name="MagicNumber"/>
```

```bash
mvn checkstyle:check
```

```
[ERROR] /private/tmp/pronosticos-copia/pronosticos-java/src/main/java/pronosticos/CalculadoraPronosticos.java:59:82: '100' es un número mágico. [MagicNumber]
[ERROR] /private/tmp/pronosticos-copia/pronosticos-java/src/main/java/pronosticos/CalculadoraPronosticos.java:76:36: '100' es un número mágico. [MagicNumber]
[ERROR] /private/tmp/pronosticos-copia/pronosticos-java/src/test/java/pronosticos/unitarias/PromedioMovilTest.java:39:61: '3' es un número mágico. [MagicNumber]
[ERROR] /private/tmp/pronosticos-copia/pronosticos-java/src/test/java/pronosticos/unitarias/PromedioMovilTest.java:42:22: '143.00' es un número mágico. [MagicNumber]
...
[INFO] There are 27 errors reported by Checkstyle 13.3.0 with config/checkstyle.xml ruleset.
```

La regla nueva encontró 27 hallazgos en el código que ya existía: 2 en `CalculadoraPronosticos` y 25 en las pruebas. En el código de producción, el `100` que convierte una proporción en porcentaje podría ser una constante. En las pruebas, en cambio, los números son los datos del ejemplo (`143.00`, `126.67`) y escribirlos tal cual es lo más legible. Esto pasa siempre que se agrega una regla: hay que decidir **dónde** se aplica y qué hacer con el código existente antes de subir el cambio, o el pipeline quedará en rojo.

### Subir o bajar un umbral

Los umbrales son propiedades del módulo. Bajar la complejidad máxima de 5 a 2:

```xml
        <module name="CyclomaticComplexity">
            <property name="max" value="2"/>
        </module>
```

```
[ERROR] /private/tmp/pronosticos-copia/pronosticos-java/src/main/java/pronosticos/CalculadoraPronosticos.java:19:5: La complejidad ciclomática es 3 (máxima permitida es 2). [CyclomaticComplexity]
[ERROR] /private/tmp/pronosticos-copia/pronosticos-java/src/main/java/pronosticos/CalculadoraPronosticos.java:49:5: La complejidad ciclomática es 4 (máxima permitida es 2). [CyclomaticComplexity]
[ERROR] /private/tmp/pronosticos-copia/pronosticos-java/src/main/java/pronosticos/CalculadoraPronosticos.java:75:5: La complejidad ciclomática es 3 (máxima permitida es 2). [CyclomaticComplexity]
[ERROR] /private/tmp/pronosticos-copia/pronosticos-java/src/main/java/pronosticos/CalculadoraPronosticos.java:82:5: La complejidad ciclomática es 3 (máxima permitida es 2). [CyclomaticComplexity]
[INFO] There are 4 errors reported by Checkstyle 13.3.0 with config/checkstyle.xml ruleset.
```

Las líneas 19, 49, 75 y 82 son `promedioMovil`, `mape`, `debeGenerarAlerta` y `validarSeries`. `mape` tiene complejidad 4: el `for`, el `if` dentro del ciclo y el `if` final. Un umbral más bajo obliga a métodos más pequeños; uno más alto (el valor por defecto de Checkstyle es 10) tolera métodos más complejos. El umbral es una decisión del equipo y conviene anotarla en el README.

### Desactivar una regla en una línea concreta

Checkstyle no trae activa ninguna forma de suprimir una regla desde el código: hay que agregar un **filtro**. El más preciso es `SuppressWithNearbyCommentFilter`, que va dentro de `TreeWalker`:

```xml
    <module name="TreeWalker">
        <!--
          Permite suprimir una regla en una línea con un comentario en esa misma línea:
          // SUPPRESS CHECKSTYLE NombreDeLaRegla: justificación
        -->
        <module name="SuppressWithNearbyCommentFilter"/>
```

Con sus valores por defecto, un comentario que contiene `SUPPRESS CHECKSTYLE NombreDeLaRegla` suprime esa regla **solo en la línea del comentario**. El resto del comentario queda para la justificación:

```java
    // Los cinco niveles son la tabla que acordó Producción y se leen mejor juntos.
    public String clasificarError(double error, double umbral) { // SUPPRESS CHECKSTYLE CyclomaticComplexity: se revisa en el sprint 4
        if (error < 0) {
            return "inválido";
        }
        // … los mismos cinco if del ejemplo de la sección 3.10
    }
```

Sin el filtro, el comentario no tiene efecto y Checkstyle reporta `La complejidad ciclomática es 6 (máxima permitida es 5). [CyclomaticComplexity]` en la línea 6. Con el filtro:

```
[INFO] You have 0 Checkstyle violations.
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
```

Reglas para usarlo:

- Siempre con el nombre de la regla; nunca suprimir todas.
- Siempre con la justificación en el mismo comentario.
- La línea del comentario debe ser la del hallazgo (en el caso de la complejidad, la de la firma del método).

Hay otras dos formas, más amplias, que la documentación de Checkstyle describe: `SuppressionCommentFilter`, que desactiva reglas entre un comentario `// CHECKSTYLE:OFF` y otro `// CHECKSTYLE:ON`, y la combinación `SuppressWarningsHolder` + `SuppressWarningsFilter`, que permite usar `@SuppressWarnings("checkstyle:cyclomaticcomplexity")` sobre un método o una clase. Afectan más de una línea, así que conviene preferir la primera.

## 7. Integración en el editor y en Git (referencia)

Esta sección es solo de referencia: el proyecto no la necesita para funcionar y la actividad no la exige.

**Editor.** El sitio de Checkstyle lista las extensiones para cada entorno: **CheckStyle-IDEA** para IntelliJ IDEA, **Checkstyle for Java** (`shengchen.vscode-checkstyle`) para Visual Studio Code y **Eclipse Checkstyle Plugin** (eclipse-cs) para Eclipse. Las tres se pueden apuntar a `config/checkstyle.xml` para marcar los hallazgos mientras se escribe, con las mismas reglas que el pipeline.

**Hook de Git.** Git puede ejecutar un programa antes de cada commit: el archivo `.git/hooks/pre-commit`. Si el programa termina con un código distinto de 0, el commit se cancela. Un hook mínimo para este proyecto:

```sh
#!/bin/sh
# .git/hooks/pre-commit — se ejecuta antes de cada commit.
mvn -B -q checkstyle:check
```

Hay que darle permiso de ejecución (`chmod +x .git/hooks/pre-commit`). La carpeta `.git/hooks` no se sube al repositorio, así que cada persona instala su hook. El hook da retroalimentación antes de subir el cambio, pero no reemplaza al pipeline: se puede saltar con `git commit --no-verify` y el pipeline no.

## 8. Documentación oficial

- Checkstyle: https://checkstyle.org/
- Lista de reglas (*checks*): https://checkstyle.org/checks.html
- Configuración (`Checker`, `TreeWalker`, propiedades): https://checkstyle.org/config.html
- Filtro `SuppressWithNearbyCommentFilter`: https://checkstyle.org/filters/suppresswithnearbycommentfilter.html
- Filtro `SuppressionCommentFilter`: https://checkstyle.org/filters/suppressioncommentfilter.html
- `SuppressWarningsHolder`: https://checkstyle.org/checks/annotation/suppresswarningsholder.html
- Regla `CyclomaticComplexity`: https://checkstyle.org/checks/metrics/cyclomaticcomplexity.html
- Regla `MagicNumber`: https://checkstyle.org/checks/coding/magicnumber.html
- maven-checkstyle-plugin, meta `check`: https://maven.apache.org/plugins/maven-checkstyle-plugin/check-mojo.html
- Herramientas relacionadas (extensiones para editores): https://checkstyle.org/index.html#Related_Tools
- Hooks de Git: https://git-scm.com/docs/githooks
