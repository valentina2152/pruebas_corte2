# Informe de la actividad — Valentina Barreto

## Actualización de los requisitos

Se incorporó la actualización del profesor `59db024`, que añade los ejemplos
BDD de promedio móvil, las guías de TDD y análisis estático y un nuevo Paso 8.
La entrega ahora indica **10 de noviembre de 2026, 11:59 p. m.**

Los requisitos nuevos se desarrollaron así:

- 8.1: `mse` tiene cuatro pruebas y nueve commits que muestran los ciclos rojo, verde y refactor.
- 8.2: [tres casos nuevos y su tabla de riesgos](docs/CASOS_PROPIOS.md), implementados en `CasosPropiosTest`.
- 8.3: `desviacion_porcentual.feature` comprueba la exclusión de demanda cero en MAPE.
- 8.4: regla `EmptyStatement`, con [fallo provocado](docs/evidencias/regla-propia-rojo.log) y [corrección verificada](docs/evidencias/regla-propia-verde.log).
- 8.5: [checklist completo y tres hallazgos](docs/REVISION_ESTATICA.md).
- Paso 9: [respuestas de la práctica y Definition of Done](docs/PRACTICA_PROPIA.md).

La primera prueba de MSE define el cálculo del requisito; después se comprueban
pronósticos exactos y cada validación por separado. La justificación de 143.15
frente a 143.22, la decisión propuesta sobre redondeo y la participación de quien
prueba están explicadas en el documento de práctica propia enlazado arriba.

La regla `EmptyStatement` detectó una sentencia vacía introducida deliberadamente.
Al quitarla, Checkstyle no encontró infracciones en producción ni pruebas, incluido
el código existente. No fue necesario suprimir ninguna advertencia.

Los resultados de 36 pruebas documentados abajo corresponden a la versión
anterior a esta actualización. Se conservan como evidencia histórica.

La verificación de la actualización (`mvn clean verify`) pasó con **39 pruebas:
33 unitarias y 6 escenarios BDD**, sin fallos ni errores; Checkstyle y el mínimo
de JaCoCo también pasaron. Registro: [actualización del profesor](docs/evidencias/10-actualizacion-profesor.log).

## Repositorio y evidencias

### Verificación completa del Paso 8

`mvn clean verify` terminó con **47 pruebas correctas: 40 unitarias y 7 escenarios
BDD**, cero errores y cero omisiones. Checkstyle no reportó infracciones.
JaCoCo alcanzó **51/51 líneas y 34/34 ramas (100 %)** de la calculadora, incluido
MSE. [Registro completo](docs/evidencias/11-paso8-completo.log).

![Cobertura actualizada con MSE](docs/evidencias/jacoco-paso8.png)

- Repositorio propio: [valentina2152/pruebas_corte2](https://github.com/valentina2152/pruebas_corte2).
- Pipeline rojo en GitHub Actions: **pendiente de ejecución y captura**.
- Pipeline verde en GitHub Actions: **pendiente de ejecución y captura**.
- Reporte de cobertura: `target/site/jacoco/index.html` después de `mvn clean verify`.
- Evidencias locales de TDD: carpeta [docs/evidencias](docs/evidencias).

### Resultados locales anteriores al Paso 8

`mvn clean verify` terminó con `BUILD SUCCESS`: **36 pruebas**, sin fallos ni
errores (33 casos unitarios y 3 escenarios BDD). Checkstyle reportó cero
infracciones. JaCoCo registró **45/45 líneas y 32/32 ramas cubiertas (100 %)**
en `CalculadoraPronosticos`, por encima del mínimo de 90 %.

![Reporte real de JaCoCo](docs/evidencias/jacoco.png)

| Comprobación | Resultado observado | Evidencia |
|---|---|---|
| Método ausente | Error de compilación `cannot find symbol` | [Rojo TDD](docs/evidencias/01-tdd-rojo.log) |
| Implementación inicial | 11 pruebas correctas | [Verde TDD](docs/evidencias/02-tdd-verde.log) |
| Validaciones ausentes | 6 fallos de los casos nuevos | [Rojo de validaciones](docs/evidencias/03-validaciones-rojo.log) |
| Validaciones implementadas | 20 pruebas correctas | [Refactor](docs/evidencias/04-tdd-refactor.log) |
| Importación sin usar | Falla `UnusedImports` | [Checkstyle rojo](docs/evidencias/06-checkstyle-rojo.log) |
| Cambio de `>=` a `>` | 4 fallos: igualdad a 10, extremos 0 y 100, escenario BDD de igualdad | [Alertas rojo](docs/evidencias/07-alertas-rojo.log) |
| Solo ejecutar MadTest | Falla JaCoCo: ratio de líneas 0.24 y ramas 0.18, mínimo 0.90 | [Cobertura roja](docs/evidencias/08-cobertura-rojo.log) |
| Código restaurado y compilación limpia | 36 pruebas correctas y cobertura aprobada | [Verificación final](docs/evidencias/09-verificacion-final.log) |

Las salidas locales no acreditan una ejecución remota del pipeline. Los pasos para
completar las evidencias están en [docs/ENTREGA.md](docs/ENTREGA.md).

## Respuestas para revisar y expresar con palabras propias

### ¿Qué ocurrió en el rojo de TDD?

El compilador reportó `cannot find symbol` al invocar `suavizacionExponencial`.
La prueba se escribió antes del método. Java comprueba que los métodos existan
durante la compilación, por lo que ni siquiera pudo ejecutar esa prueba.
Después se implementó la fórmula y las 11 pruebas de ese momento pasaron.
El resultado sin redondear es 137.55456; la tabla lo presenta como 137.5546.
Se usa la tolerancia 0.01 que indica la guía.

### ¿Por qué el MAPE de [80, 0, 95] se divide entre dos?

El periodo cuya demanda real es cero se excluye porque su error porcentual
implicaría dividir entre cero. Quedan dos periodos válidos:
`(0 / 80 × 100 + 5 / 95 × 100) / 2 = 2.631578... %`.
Dividir entre tres reduciría artificialmente el error.

### ¿Por qué se crea la calculadora con new?

Sus operaciones son Java puro y no dependen de Spring, una base de datos ni un
servidor. Crear la clase directamente permite comprobar la lógica con pruebas
rápidas, aisladas y repetibles.

### ¿Qué diferencia hay entre unitarias y BDD aquí?

Las pruebas unitarias comprueban métodos, cálculos y excepciones con JUnit.
Los escenarios BDD expresan la regla de alerta en español, mediante Dado,
Cuando y Entonces. Cucumber conecta esas frases con llamadas a la calculadora
y aserciones. En este proyecto los escenarios BDD comprueban negocio; no son
pruebas HTTP ni levantan Spring.

### ¿Qué debe ocurrir al cambiar >= por >?

En la comprobación local fallaron el caso de igualdad a 10, los dos casos de
umbrales extremos (0 y 100) y el escenario BDD de igualdad. La regla exige una
alerta cuando error y umbral coinciden. El job de cobertura también
ejecuta estas pruebas. El despliegue depende de los cuatro jobs mediante `needs`,
así que una verificación fallida impide su ejecución. La evidencia remota de
este comportamiento está pendiente; no se presenta como una ejecución observada.

## Alcance de las verificaciones

Checkstyle revisa fuentes de producción y de pruebas. JaCoCo debe exigir al menos
90 % en líneas y ramas de la lógica de negocio; esta condición se comprobó tanto
en verde como provocando cobertura insuficiente. Se conservan las exclusiones del
proyecto base para el controlador HTTP y el arranque de Spring; por ello la
cobertura de la calculadora no equivale a cobertura de toda la API.
