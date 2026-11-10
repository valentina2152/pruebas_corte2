# Pronósticos de demanda · Java con Spring Boot

Proyecto base del curso de Pruebas de Software. Calcula pronósticos de demanda y mide qué tan lejos quedaron de la demanda real, con las mismas reglas del proyecto nuclear "Sistema web para la predicción de la demanda en producción industrial". Expone una API HTTP con Spring Boot.

No usa bases de datos, contenedores ni servicios externos: solo Java y Maven. Spring Boot trae su propio servidor (Tomcat embebido), así que no hay que instalar ni configurar un servidor de aplicaciones.

La actividad que deben desarrollar está en [ACTIVIDAD.md](ACTIVIDAD.md).

## Solución del Corte 2

Se implementó `suavizacionExponencial(serie, alfa)` mediante TDD. Las pruebas
cubren MAPE, alertas y validaciones, y Cucumber ejecuta los tres escenarios de
alerta definidos en español. El historial conserva los estados rojo, verde y
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
El borrador del informe está en [INFORME_Valentina_Barreto.md](INFORME_Valentina_Barreto.md).

---

## Qué hace

| Método | Qué calcula | Ejemplo con la serie 120, 132, 128, 141, 138, 150 |
|---|---|---|
| `promedioMovil(serie, ventana)` | Pronóstico del siguiente periodo con el promedio de los últimos `ventana` valores | Ventana 3: (141 + 138 + 150) / 3 = 143,00 |
| `mad(real, pronostico)` | Error absoluto medio, en unidades | 11,00 para los periodos 4 a 6 |
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
git clone <url-de-este-repositorio>
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

Al iniciar el proyecto hay 10 pruebas unitarias y todas pasan.

Comandos que van a usar en la actividad:

| Comando | Qué hace |
|---|---|
| `mvn test -Dtest='**/unitarias/*Test'` | Pruebas unitarias (paquete `unitarias`) |
| `mvn test -Dtest=RunCucumberTest` | Escenarios BDD con Cucumber (los crean ustedes) |
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
│   └── bdd/                                    Pasos de Cucumber (vacía al iniciar)
├── src/test/resources/features/                Escenarios BDD (vacía al iniciar)
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

### BDD (desarrollo guiado por comportamiento)

El comportamiento esperado se describe en lenguaje natural con el formato **Dado / Cuando / Entonces** (Gherkin), antes de programarlo. Una persona del negocio puede leer el escenario y confirmar si es lo que necesita. En Cucumber, cada línea del escenario se conecta con un método anotado con `@Dado`, `@Cuando` o `@Entonces` (paquete `io.cucumber.java.es`).

La primera línea del archivo `.feature` debe ser `# language: es` para usar las palabras clave en español. Si esa línea no está de primera, el archivo se lee en inglés y "Característica", "Dado" o "Cuando" no se reconocen.

### Análisis estático

Revisa el código sin ejecutarlo. La configuración de Checkstyle de este proyecto es corta a propósito: detecta importaciones y variables sin usar, bloques vacíos, `if` sin llaves, nombres que no siguen las convenciones de Java y métodos con **complejidad ciclomática** (número de decisiones más uno) mayor a 5.

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

`.github/workflows/ci-cd.yml` tiene dos jobs:

1. **Pruebas unitarias**: instala Java 21 y corre las pruebas del paquete `unitarias`.
2. **Despliegue (simulado)**: corre solo si el job anterior pasó y solo en un push a `main`. Imprime `Desplegado`.

En la actividad agregan análisis estático, pruebas BDD y cobertura, y hacen que el despliegue dependa de todo.

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
