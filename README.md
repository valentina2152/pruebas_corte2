# Pronósticos de demanda · Java con Spring Boot

Proyecto base del curso de Pruebas de Software. Calcula pronósticos de demanda y mide qué tan lejos quedaron de la demanda real, con las mismas reglas del proyecto nuclear "Sistema web para la predicción de la demanda en producción industrial". Expone una API HTTP con Spring Boot.

No usa bases de datos, contenedores ni servicios externos: solo Java y Maven. Spring Boot trae su propio servidor (Tomcat embebido), así que no hay que instalar ni configurar un servidor de aplicaciones.

La actividad que deben desarrollar está en [ACTIVIDAD.md](ACTIVIDAD.md) (en GitHub: https://github.com/josealfredore2604/pronosticos-java/blob/main/ACTIVIDAD.md).

## Solución del Corte 2

Se implementaron `suavizacionExponencial(serie, alfa)` y `mse(real, pronostico)` mediante TDD. Las pruebas
cubren MAPE, alertas y validaciones, y Cucumber ejecuta los tres escenarios de
alerta, los tres ejemplos de promedio móvil y un escenario propio de MAPE con demanda cero. El historial conserva los estados rojo, verde y
refactor; las salidas están en `docs/evidencias/`.

Con Java 21 y Maven, la verificación completa se ejecuta con:

```bash
mvn clean verify
```

Este comando revisa Checkstyle, ejecuta todas las pruebas y exige como mínimo
90 % de cobertura de líneas y ramas. Los reportes quedan en
`target/site/jacoco/index.html`, `target/cucumber.html` y `target/surefire-reports/`.
Las exclusiones de la capa HTTP se mantienen según la actividad.

El workflow ejecuta análisis estático, pruebas unitarias, BDD y cobertura en
cuatro jobs. El despliegue simulado requiere que los cuatro pasen y que el evento
sea un push a `main`. El job de cobertura conserva los reportes como artefactos.

Para reproducir los fallos controlados de Checkstyle, alertas y cobertura en
Windows, ejecutar `./scripts/comprobar-bloqueos.ps1` desde PowerShell con Java 21
en `JAVA_HOME`. El script restaura el código tras cada mutación y termina con
una verificación limpia. No equivale a ejecutar GitHub Actions.

La entrega remota y sus capturas se completan siguiendo [docs/ENTREGA.md](docs/ENTREGA.md).
El informe está en [INFORME_Valentina_Barreto.md](INFORME_Valentina_Barreto.md).

La actualización del profesor `59db024` agrega una práctica propia en el Paso 8
de `ACTIVIDAD.md`. Su desarrollo se documenta en [PRACTICA_PROPIA.md](docs/PRACTICA_PROPIA.md),
[CASOS_PROPIOS.md](docs/CASOS_PROPIOS.md) y [REVISION_ESTATICA.md](docs/REVISION_ESTATICA.md).
La regla adicional `EmptyStatement` se comprueba con `./scripts/comprobar-regla-propia.ps1`.

---

## Qué hace

| Método | Qué calcula | Ejemplo con la serie 120, 132, 128, 141, 138, 150 |
|---|---|---|
| `promedioMovil(serie, ventana)` | Pronóstico del siguiente periodo con el promedio de los últimos `ventana` valores | Ventana 3: (141 + 138 + 150) / 3 = 143,00 |
| `mad(real, pronostico)` | Error absoluto medio, en unidades | 11,00 para los periodos 4 a 6 |
| `mse(real, pronostico)` | Error cuadrático medio, en unidades al cuadrado | 143,15 usando los pronósticos redondeados a dos decimales |
| `mape(real, pronostico)` | Error porcentual absoluto medio; excluye los periodos con demanda 0 | 7,62 % para los periodos 4 a 6 |
| `debeGenerarAlerta(error, umbral)` | Si un error debe disparar una alerta; alerta cuando el error es mayor **o igual** al umbral | Error 10 % con umbral 10 %: sí alerta |

| Clase | Responsabilidad |
|---|---|
| `CalculadoraPronosticos` | Reglas de negocio. Java puro, sin dependencias de Spring |
| `PronosticoController` | Traduce peticiones HTTP a llamadas de la calculadora |
| `PronosticosApplication` | Arranca la aplicación y registra la calculadora como bean |

| Método y ruta | Qué hace |
|---|---|
| `GET /api/salud` | Responde `{"estado":"ok"}` si la aplicación está arriba |
| `POST /api/pronosticos/mad` | Recibe `{"real": [...], "pronostico": [...]}` y responde el MAD |

## Requisitos

- Java 21 (por ejemplo [Eclipse Temurin](https://adoptium.net/) o cualquier distribución de OpenJDK).
- [Maven](https://maven.apache.org/download.cgi) 3.9 o superior.

```bash
java -version
mvn -version
```

La primera vez que corran Maven va a descargar las dependencias desde Maven Central; tarda unos minutos. Las siguientes veces las toma de la caché local (`~/.m2`).

## Cómo levantarlo

```bash
git clone https://github.com/josealfredore2604/pronosticos-java
cd pronosticos-java

mvn spring-boot:run
```

La API queda en http://localhost:8080. En otra terminal:

```bash
curl http://localhost:8080/api/salud

curl -X POST http://localhost:8080/api/pronosticos/mad \
  -H "Content-Type: application/json" \
  -d '{"real": [141, 138, 150], "pronostico": [126.67, 133.67, 135.67]}'
```

La segunda petición responde un MAD de aproximadamente 11.00. Para detener la API, `Ctrl + C`.

## Cómo correr las pruebas

```bash
mvn test -Dtest='**/unitarias/*Test'
```

Al iniciar el proyecto hay 10 pruebas unitarias y 3 escenarios BDD (el ejemplo de promedio móvil), y todos pasan.

Comandos que van a usar en la actividad:

| Comando | Qué hace |
|---|---|
| `mvn test -Dtest='**/unitarias/*Test'` | Pruebas unitarias (paquete `unitarias`) |
| `mvn test -Dtest=RunCucumberTest` | Escenarios BDD con Cucumber: el ejemplo de promedio móvil y los escenarios que ustedes agreguen |
| `mvn checkstyle:check` | Análisis estático con Checkstyle |
| `mvn verify` | Todas las pruebas, reporte de cobertura y, cuando lo configuren, el mínimo de cobertura |

Las comillas de `-Dtest='...'` evitan que la terminal interprete los asteriscos.

El reporte de cobertura queda en `target/site/jacoco/index.html`; ábranlo en el navegador.

## Estructura

```
pronosticos-java/
├── .github/workflows/ci-cd.yml                 Pipeline de CI/CD de GitHub Actions
├── config/checkstyle.xml                       Reglas de análisis estático
├── src/main/java/pronosticos/
│   ├── CalculadoraPronosticos.java             Reglas de negocio
│   ├── PronosticoController.java               API HTTP
│   └── PronosticosApplication.java             Arranque de Spring Boot
├── src/main/resources/application.properties   Configuración de la aplicación
├── src/test/java/pronosticos/
│   ├── unitarias/                              Pruebas unitarias
│   └── bdd/
│       ├── RunCucumberTest.java                Ejecutor de los escenarios BDD
│       └── PromedioMovilSteps.java             Pasos del ejemplo de promedio móvil
├── src/test/resources/features/
│   └── promedio_movil.feature                  Escenarios BDD del ejemplo de promedio móvil
├── docs/
│   ├── EJEMPLO_TDD.md                          Ciclo rojo-verde-refactor de mad, con salidas reales
│   └── ANALISIS_ESTATICO.md                    Guía de Checkstyle: reglas, ejecución y configuración
├── pom.xml                                     Dependencias y plugins de Maven
├── README.md
└── ACTIVIDAD.md
```

## Herramientas

| Herramienta | Para qué | Dónde se configura |
|---|---|---|
| [Maven](https://maven.apache.org/guides/) | Compila, gestiona dependencias y ejecuta las pruebas | `pom.xml` |
| [Spring Boot](https://docs.spring.io/spring-boot/) 4.1 | Aplicación web con servidor embebido | `pom.xml`, `application.properties` |
| [JUnit 6](https://docs.junit.org/) | Pruebas unitarias | Anotaciones en las clases de prueba |
| [Cucumber](https://cucumber.io/docs/cucumber/) | Ejecuta escenarios escritos en Gherkin | `src/test/resources/features/` y los pasos |
| [Checkstyle](https://checkstyle.org/) | Analizador estático | `config/checkstyle.xml` |
| [JaCoCo](https://www.jacoco.org/jacoco/trunk/doc/) | Cobertura de código | Plugin `jacoco-maven-plugin` en `pom.xml` |

### Por qué Spring Boot 4

Spring Boot 4 dividió sus módulos en piezas más pequeñas. Por eso el proyecto usa `spring-boot-starter-webmvc` (antes `spring-boot-starter-web`) y su par de pruebas `spring-boot-starter-webmvc-test`. Si encuentran tutoriales con los nombres anteriores, son de Spring Boot 3.

### El ciclo de vida de Maven

Maven ejecuta fases en orden: `validate` → `compile` → `test` → `package` → `verify` → `install`. Pedir una fase ejecuta también todas las anteriores. `mvn test` compila y prueba; `mvn verify` además empaqueta y corre las verificaciones finales, como el mínimo de cobertura.

---

## Conceptos

### Prueba unitaria

Verifica una unidad pequeña de código (un método) de forma aislada, sin base de datos, red, archivos ni contexto de Spring. Por eso es rápida y su resultado no depende del entorno. Las pruebas de este proyecto crean la calculadora con `new`, sin levantar la aplicación.

Siguen el patrón **Arrange-Act-Assert**: preparar los datos, ejecutar el método y verificar el resultado, con un solo comportamiento por prueba. En JUnit, `@Test` marca una prueba, `@BeforeEach` se ejecuta antes de cada una, `@DisplayName` le da un nombre legible y `@ParameterizedTest` repite la misma prueba con varios valores.

Una buena prueba unitaria cumple **FIRST**: rápida (*Fast*), independiente de las demás (*Independent*), repetible en cualquier máquina (*Repeatable*), que decide sola si pasa o falla (*Self-validating*) y escrita a tiempo, junto con el código (*Timely*).

### Tolerancia numérica

Los cálculos con `double` no dan resultados exactos: `0.1 + 0.2` es `0.30000000000000004`. Por eso los resultados numéricos se comparan con tolerancia:

```java
assertEquals(11.00, resultado, 0.01);
```

El tercer argumento es la diferencia máxima aceptada. El valor esperado siempre sale de un cálculo independiente, nunca del mismo código que se está probando.

### TDD (desarrollo guiado por pruebas)

La prueba se escribe antes que el código, en ciclos cortos:

1. **Rojo**: se escribe una prueba para un comportamiento que todavía no existe. Falla (en Java, normalmente ni siquiera compila).
2. **Verde**: se escribe el código mínimo para que pase.
3. **Refactor**: se mejora el código sin cambiar su comportamiento. Las pruebas siguen en verde.

El ciclo completo, reconstruido paso a paso con `mad` y con las salidas reales de la consola, está en [docs/EJEMPLO_TDD.md](docs/EJEMPLO_TDD.md).

### BDD (desarrollo guiado por comportamiento)

El comportamiento esperado se describe en lenguaje natural con el formato **Dado / Cuando / Entonces** (Gherkin), antes de programarlo. Una persona del negocio puede leer el escenario y confirmar si es lo que necesita. En Cucumber, cada línea del escenario se conecta con un método anotado con `@Dado`, `@Cuando` o `@Entonces` (paquete `io.cucumber.java.es`). La clase `RunCucumberTest` le indica a JUnit que ejecute con Cucumber todos los `.feature` de `src/test/resources/features`, con los pasos de todas las clases del paquete `pronosticos.bdd`.

La primera línea del archivo `.feature` debe ser `# language: es` para usar las palabras clave en español. Si esa línea no está de primera, el archivo se lee en inglés y "Característica", "Dado" o "Cuando" no se reconocen.

El proyecto trae un ejemplo funcionando: `src/test/resources/features/promedio_movil.feature` describe el pronóstico con promedio móvil y `src/test/java/pronosticos/bdd/PromedioMovilSteps.java` conecta cada frase con el código. Tiene tres casos: el cálculo con la serie 120, 132, 128, 141, 138, 150 y ventana 3 (resultado 143), una ventana mayor que la serie y una ventana de 0, estos dos con error. Se ejecuta con `mvn test -Dtest=RunCucumberTest`.

Los dos casos de error están escritos como un **Esquema del escenario**: un solo escenario con marcadores (`<ventana>`, `<mensaje>`) y una tabla de **Ejemplos**. Gherkin lo ejecuta una vez por cada fila de la tabla, con los valores de esa fila. Sirve cuando varios casos siguen los mismos pasos y solo cambian los datos. Cucumber ejecuta cada fila como un escenario aparte; por eso `mvn test -Dtest=RunCucumberTest` informa `Tests run: 3` para dos escenarios.

Un detalle de Cucumber para Java: los parámetros numéricos como `{double}` se leen con la convención del idioma del `.feature`. En un archivo con `# language: es`, la coma es el separador decimal y el punto el de miles, así que hay que escribir `2,63`; un `2.63` se lee como 263. Se comprobó así en este proyecto, también con la JVM configurada en inglés.

### Pruebas funcionales

Una prueba funcional verifica **qué** hace el sistema: compara su comportamiento con el requisito, sin mirar cómo está programado. Es una prueba de **caja negra**. Quien la diseña parte del requisito ("con la serie 120, 132, 128, 141, 138, 150 y una ventana de 3 periodos, el pronóstico del periodo 7 es 143"), elige las entradas y los resultados esperados, y no necesita leer el código.

Los escenarios BDD de este proyecto son pruebas funcionales: cada escenario es un requisito de Producción, escrito en Gherkin y ejecutado contra el sistema. El ejemplo está en `src/test/resources/features/promedio_movil.feature`.

| | Prueba unitaria | Prueba funcional (escenario BDD) |
|---|---|---|
| Pregunta que responde | ¿Esta función hace bien su trabajo? | ¿El sistema cumple el requisito? |
| Se diseña a partir de | El código: quien la escribe conoce la función, sus ramas y sus validaciones | El requisito: no hace falta mirar el código |
| Quién la puede leer | El equipo de desarrollo | Desarrollo, pruebas y Producción |
| Lenguaje | Java | Gherkin, en español |
| Si falla, indica | Qué función y qué línea fallaron | Qué regla del negocio dejó de cumplirse |

Las dos pueden verificar la misma regla. En `PromedioMovilTest.java`, `conVentanaDeUnPeriodoRepiteElUltimoValor` comprueba un caso límite que sale de conocer cómo está escrito el método (el `subList` con ventana 1). El escenario de promedio móvil, en cambio, verifica la regla tal como la dijo Producción y seguiría siendo válido aunque `promedioMovil` se reescribiera por completo.

En este proyecto los escenarios llaman directamente a las funciones de cálculo. Verificar la misma regla a través de la API HTTP (`POST /api/pronosticos/mad`) también es una prueba funcional, pero además comprueba que las piezas del sistema funcionan juntas: es una **prueba de integración**, y las pruebas de integración se ven en la Unidad 3. Por eso este proyecto no incluye pruebas de la API HTTP.

### Análisis estático

Revisa el código sin ejecutarlo. La configuración de Checkstyle de este proyecto es corta a propósito: detecta importaciones y variables sin usar, bloques vacíos, `if` sin llaves, nombres que no siguen las convenciones de Java y métodos con **complejidad ciclomática** (número de decisiones más uno) mayor a 5.

Qué reglas están activas, qué detecta cada una, cómo se lee un hallazgo y cómo se cambia la configuración: [docs/ANALISIS_ESTATICO.md](docs/ANALISIS_ESTATICO.md).

### Cobertura de código

Indica qué porcentaje del código ejecutaron las pruebas. La **cobertura de líneas** cuenta líneas ejecutadas; la **cobertura de ramas** comprueba que cada decisión se recorrió por el lado verdadero y por el falso. Una cobertura alta no garantiza que las pruebas sean buenas, pero una cobertura baja sí señala código que nadie está probando.

`PronosticosApplication` y `PronosticoController` están excluidos de la medición (`excludes` de JaCoCo en `pom.xml`): son la capa HTTP y se prueban con pruebas de integración, que se ven en la siguiente unidad.

### CI/CD y pipeline

- **Integración continua (CI)**: cada cambio se verifica automáticamente al llegar al repositorio.
- **Despliegue continuo (CD)**: si la verificación pasa, el cambio se publica sin intervención manual. Aquí el despliegue es simulado: imprime `Desplegado` en el runner.
- **Pipeline**: la secuencia de pasos automáticos que recorre un cambio. En GitHub Actions se llama **workflow** y se define en `.github/workflows/`.

| Término | Qué es |
|---|---|
| Workflow | El archivo YAML completo |
| Evento (`on`) | Lo que dispara el workflow: un push, un pull request |
| Job | Un grupo de pasos que corre en su propia máquina virtual |
| Runner | La máquina virtual donde corre un job (`ubuntu-latest`, que ya trae Maven) |
| Step | Un paso dentro de un job: una acción (`uses`) o un comando (`run`) |
| `needs` | Hace que un job espere a que otros terminen bien |

## El pipeline al iniciar

`.github/workflows/ci-cd.yml` tiene tres jobs:

1. **Pruebas unitarias**: instala Java 21 y corre las pruebas del paquete `unitarias`.
2. **Pruebas BDD**: instala Java 21 y corre `mvn -B test -Dtest=RunCucumberTest`. Ejecuta el ejemplo de promedio móvil y, cuando los agreguen, sus escenarios de alertas.
3. **Despliegue (simulado)**: corre solo si los dos jobs anteriores pasaron y solo en un push a `main`. Imprime `Desplegado`.

Los dos jobs de pruebas corren en paralelo, cada uno en su propia máquina. En la actividad agregan análisis estático y cobertura, y hacen que el despliegue dependa de los cuatro jobs de verificación.

## Clean Code

*Clean Code* (código limpio) es código que otra persona puede leer, entender y cambiar sin miedo. No es una herramienta: es un conjunto de principios que el análisis estático ayuda a vigilar y que las revisiones estáticas comprueban.

| Principio | Qué significa | Dónde se ve en este proyecto |
|---|---|---|
| Nombres que explican | El nombre dice qué es o qué hace, sin necesidad de un comentario | `sumaErrores` dice qué acumula la variable; `debeGenerarAlerta` se lee como una pregunta y devuelve `true` o `false` |
| Una sola responsabilidad | Cada función hace una cosa; si hace dos, se divide | `CalculadoraPronosticos` solo calcula, `PronosticoController` solo traduce peticiones HTTP y `PronosticosApplication` solo arranca la aplicación (ver la tabla de clases al inicio). La calculadora no conoce Spring |
| Sin duplicación | Cada regla está escrita en un solo lugar | `mad` y `mape` necesitan las mismas validaciones y las toman de `validarSeries`; la suma de la ventana está en `sumar` |
| Sin números mágicos | Un número que significa algo para el negocio lleva nombre | las pruebas no escriben `0.01` en cada `assertEquals`: usan la constante `TOLERANCIA`. El `100` de `mape` se deja escrito porque es la definición de porcentaje, no un parámetro del negocio |
| Comentarios que explican el porqué | El código ya dice qué hace; el comentario dice por qué se decidió así | el Javadoc de `debeGenerarAlerta` no repite que compara con `>=`: explica que es una regla acordada con Producción y que un error igual al umbral sí alerta |
| Manejo explícito de errores | Un dato inválido produce un error con un mensaje claro, no un resultado silencioso ni un error genérico | con series vacías, `mad` lanza `IllegalArgumentException("Se necesita al menos un periodo")` en lugar de devolver `NaN` en silencio (ver el ciclo 4 de `docs/EJEMPLO_TDD.md`), y el `@ExceptionHandler` del controlador convierte esa excepción en una respuesta 400 con el mensaje |

Un ejemplo tomado del propio código:

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

    /**
     * Indica si un error de pronóstico debe disparar una alerta.
     *
     * <p>Regla acordada con Producción: la alerta se activa cuando el error es mayor
     * o igual al umbral. Un error exactamente igual al umbral sí alerta.
     */
    public boolean debeGenerarAlerta(double errorPorcentual, double umbral) {
        if (umbral < 0 || umbral > 100) {
            throw new IllegalArgumentException("El umbral debe estar entre 0 y 100");
        }
        return errorPorcentual >= umbral;
    }
```

En estos dos métodos se ven cinco de los seis principios: nombres que explican (`sumaErrores`, `debeGenerarAlerta`), una sola responsabilidad (calcular, sin saber nada de HTTP), ninguna duplicación (`validarSeries`), un comentario que explica el porqué (la regla acordada) y errores explícitos con un mensaje que dice qué está mal.

## Revisiones estáticas

Una **revisión** es el examen de un producto de trabajo (código, requisitos, escenarios, un plan de pruebas) hecho **por personas**, sin ejecutarlo. Es estática, como el análisis estático, pero no la hace una herramienta:

| | Análisis estático automatizado | Revisión estática |
|---|---|---|
| Quién revisa | Una herramienta (Checkstyle, en este proyecto) | Personas |
| Qué revisa | Solo código, contra reglas fijas | Cualquier producto de trabajo: código, requisitos, escenarios, documentos |
| Qué encuentra | Defectos que se pueden describir con una regla: variables sin usar, complejidad, nombres | Además, lo que una regla no ve: un requisito ambiguo, un caso que falta, un nombre que confunde, un diseño difícil de cambiar |
| Cuándo | En cada cambio, en segundos (en el pipeline) | Cuando el equipo lo planifica; cuesta tiempo de personas |

Las dos se complementan: la herramienta revisa lo mecánico en cada cambio y deja a las personas lo que requiere criterio.

**Tipos de revisión** (adaptación del temario ISTQB CTFL v4.0, sección 3.2.4). Van de la menos a la más formal:

| Tipo | Cómo es | Objetivo principal | Ejemplo en este proyecto |
|---|---|---|---|
| Revisión informal | No sigue un proceso definido ni exige dejar registro | Detectar anomalías | Pedirle a un compañero que lea la función nueva antes del commit |
| *Walkthrough* (recorrido) | La dirige el autor, que recorre el producto con los revisores; la revisión individual previa es opcional | Evaluar la calidad, generar confianza, enseñar, llegar a acuerdos, generar ideas y detectar anomalías | El autor recorre con Producción, línea por línea, el `.feature` de alertas para confirmar que dice lo que se acordó |
| Revisión técnica | La hacen revisores con conocimiento técnico y la dirige un moderador | Llegar a un acuerdo y tomar decisiones sobre un problema técnico; también detectar anomalías | El equipo decide si la complejidad máxima debe seguir en 5 |
| Inspección | La más formal: sigue el proceso completo, con roles definidos y métricas; el autor no puede ser líder ni secretario de la revisión | Encontrar el mayor número posible de anomalías | Inspeccionar el cálculo del MAPE con un checklist antes de entregarlo a Producción |

El temario describe un proceso genérico (sección 3.2.2): **planificación** (alcance, criterios de salida), **inicio** (que cada participante tenga el material y sepa su rol), **revisión individual** (cada revisor anota anomalías, por ejemplo con un checklist), **comunicación y análisis** (se discute cada anomalía y se decide qué hacer) y **corrección e informe**. Los roles principales (sección 3.2.3) son gerente, autor, moderador, secretario (quien registra), revisor y líder de la revisión. Cuanto más formal es la revisión, más de ese proceso se cumple.

En la actividad (Paso 8.5) hacen una revisión con el checklist de Clean Code.

## Testing en metodologías ágiles

En un equipo ágil las pruebas no son una fase al final: ocurren durante todo el sprint y son responsabilidad de todo el equipo (el temario ISTQB lo llama *whole team approach*, sección 1.5.2). Así encaja este proyecto en un sprint:

| Momento del sprint | Qué pasa | Qué parte del proyecto lo representa |
|---|---|---|
| Refinamiento de la historia | Producción, desarrollo y quien prueba escriben juntos la historia ("Como planeador de producción quiero una alerta…") y sus criterios de aceptación. Quien prueba pregunta por los casos límite: ¿un error igual al umbral alerta? | Los escenarios de `src/test/resources/features/` |
| Desarrollo | Cada función se construye con TDD; los escenarios se automatizan | `docs/EJEMPLO_TDD.md`, `src/test/java/pronosticos/unitarias/`, los pasos BDD |
| Integración continua | Cada push ejecuta el pipeline | `.github/workflows/ci-cd.yml` |
| Revisión del sprint | Se muestra lo terminado; los escenarios en verde son la evidencia de que se cumplieron los criterios | El job *Pruebas BDD* del pipeline |
| Retrospectiva | El equipo revisa qué mejorar: por ejemplo, ajustar una regla del análisis estático | `docs/ANALISIS_ESTATICO.md` |

Tres ideas que conviene recordar:

- **Quien prueba participa desde la definición de la historia.** Encontrar la ambigüedad de "¿igual al umbral alerta o no?" cuando se escribe la historia cuesta una conversación; encontrarla en producción cuesta un defecto. Es el principio de probar temprano (*shift left*, sección 2.1.5 del temario).
- **Los escenarios BDD son criterios de aceptación ejecutables.** El temario (sección 4.5.2) describe el formato Dado/Cuando/Entonces como una de las dos formas habituales de escribir criterios de aceptación. Aquí, además, se ejecutan: si el código deja de cumplir el criterio, el pipeline lo dice.
- **El pipeline en verde es parte de la *Definition of Done*.** Una historia está terminada cuando, además de funcionar, cumple lo que el equipo acordó. Para este proyecto, una *Definition of Done* razonable sería: pruebas unitarias y escenarios BDD en verde, análisis estático sin hallazgos, cobertura de 90 % o más, código revisado por otra persona y el pipeline en verde en `main`. Si una de esas condiciones falla, la historia no está terminada, aunque "funcione en mi equipo".

Este es también el sentido de las **pruebas continuas en DevOps** (sección 2.1.4 del temario): el pipeline da retroalimentación rápida en cada cambio y reduce el riesgo de regresión, pero no elimina las pruebas manuales ni las revisiones desde el punto de vista de quien usa el sistema.

## Tipos de prueba de la Unidad 2 en este proyecto

| Tema de la unidad | Dónde está en el proyecto | Cómo se practica |
|---|---|---|
| TDD | `docs/EJEMPLO_TDD.md` (ciclo completo de `mad`) | ACTIVIDAD, Paso 2 (suavización exponencial) y Paso 8.1 (`mse`) |
| BDD | `src/test/resources/features/promedio_movil.feature` y `src/test/java/pronosticos/bdd/PromedioMovilSteps.java` | `mvn test -Dtest=RunCucumberTest`; ACTIVIDAD, Pasos 4 y 8.3 |
| Pruebas unitarias | `src/test/java/pronosticos/unitarias/` | `mvn test -Dtest='**/unitarias/*Test'`; ACTIVIDAD, Pasos 3 y 8.2 |
| Pruebas funcionales | Los escenarios BDD (sección "Pruebas funcionales" de este README) | `mvn test -Dtest=RunCucumberTest`; ACTIVIDAD, Pasos 4 y 8.3 |
| Análisis estático | `config/checkstyle.xml` y el plugin `maven-checkstyle-plugin` de `pom.xml`; guía en `docs/ANALISIS_ESTATICO.md` | `mvn checkstyle:check`; ACTIVIDAD, Pasos 5 y 8.4 |
| Revisiones estáticas | Sección "Revisiones estáticas" de este README y checklist de la ACTIVIDAD | ACTIVIDAD, Paso 8.5 |
| Clean Code | Sección "Clean Code" de este README y el código de las reglas de negocio | ACTIVIDAD, refactor del Paso 2.3 y Paso 8.5 |
| Pruebas continuas en DevOps | `.github/workflows/ci-cd.yml` | ACTIVIDAD, Paso 7 |
| Testing en ágil | Sección "Testing en metodologías ágiles" de este README | ACTIVIDAD, Paso 7.5 y preguntas del informe (Paso 9) |

## Documentación oficial

- Spring Boot: https://docs.spring.io/spring-boot/
- Guía de migración a Spring Boot 4: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide
- JUnit: https://docs.junit.org/
- Cucumber JVM: https://cucumber.io/docs/installation/java/
- Cucumber con JUnit Platform: https://github.com/cucumber/cucumber-jvm/tree/main/cucumber-junit-platform-engine
- Checkstyle, reglas: https://checkstyle.org/checks.html
- JaCoCo para Maven: https://www.jacoco.org/jacoco/trunk/doc/maven.html
- Maven Surefire, selección de pruebas: https://maven.apache.org/surefire/maven-surefire-plugin/examples/single-test.html
- GitHub Actions: https://docs.github.com/actions
- Acción setup-java: https://github.com/actions/setup-java
