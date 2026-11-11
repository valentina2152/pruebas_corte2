# Práctica propia — Paso 8

## MSE y TDD

La primera prueba usa las demandas [141, 138, 150] y los pronósticos
[126.67, 133.67, 135.67], porque verifica la fórmula con el ejemplo acordado.
El cálculo independiente es `(205.3489 + 18.7489 + 205.3489) / 3 = 143.1489`,
que se presenta como 143.15 con dos decimales.

El segundo caso exige cero con pronósticos exactos; detecta una implementación
que solo devuelva la constante del primer ejemplo. Después se agregan, de a una,
las pruebas de longitudes distintas y series vacías, verificando también los
mensajes. El refactor reutiliza `validarSeries` y nombra la suma de cuadrados.
Los commits y los registros `docs/evidencias/mse-*.log` documentan el orden real.

El valor 143.22 corresponde a usar pronósticos sin redondear (380/3, 401/3 y
407/3); no es el resultado para los datos de dos decimales de la tabla.
Producción debería conservar la precisión completa durante los cálculos y
redondear solo al presentar resultados. Si el dato de entrada almacenado ya
está redondeado a dos decimales, debe calcularse 143.15: el sistema no puede
recuperar una precisión que no recibió. La política de redondeo debe acordarse
con Producción y quedar en los criterios de aceptación.

## Casos nuevos

El diseño previo y los cálculos están en [CASOS_PROPIOS.md](CASOS_PROPIOS.md).
Se comprueban decimales en promedio móvil, exclusión de datos anteriores a la
ventana y ausencia de cancelación entre errores de signos opuestos en MAD.

## Regla de negocio del escenario propio

Se elige la exclusión de periodos con demanda cero al medir la desviación
porcentual. Está implementada en `CalculadoraPronosticos.mape`: omite el periodo
y divide entre los periodos válidos, no entre todos los periodos recibidos.

El escenario usa demandas 80, 0 y 95 y pronósticos 80, 0 y 100; espera 2,63 %.
Es una prueba funcional porque verifica un requisito con entradas y resultado
observables por Producción. Las frases no describen llamadas a métodos ni
detalles de implementación. Sus pasos ejecutan Java puro, por lo que no es una
prueba de interfaz HTTP. El idioma español exige coma en `2,63` para `{double}`.

## Regla propia de Checkstyle

Se agrega `EmptyStatement` dentro de `TreeWalker`, distinta de las reglas usadas
como ejemplos de configuración en la guía. Detecta sentencias vacías (`;`)
que pueden ser un error accidental, por ejemplo después de un condicional.
Se aplica tanto a producción como a pruebas, sin exclusiones ni supresiones.

La comprobación controlada agrega un `;` independiente al comienzo de `mse`,
ejecuta Checkstyle y exige que el error mencione `EmptyStatement`. Luego restaura
el código y vuelve a ejecutar el análisis. El resultado y el estado del código
existente se registran en el informe final.

Referencia: [documentación oficial de EmptyStatement](https://checkstyle.org/checks/coding/emptystatement.html).

## Definition of Done y participación de quien prueba

- Criterios de aceptación acordados con Producción, incluidos límites, entradas
  inválidas, unidades y política de redondeo.
- Implementación revisada y commits TDD que evidencien rojo, verde y refactor.
- Pruebas unitarias y escenarios BDD ejecutados automáticamente y sin fallos.
- Checkstyle sin infracciones y JaCoCo con al menos 90 % de líneas y ramas de
  negocio, con el alcance de las exclusiones explícito.
- Ejecución roja que bloquee el despliegue y posterior ejecución verde que lo habilite.
- Informe, reportes, capturas y enlaces disponibles en el repositorio propio.

Quien prueba participa desde el refinamiento: ayuda a definir ejemplos y casos
límite antes de programar. Durante el desarrollo revisa el diseño de pruebas y
las regresiones, y antes de cerrar la historia comprueba los criterios de
aceptación y las evidencias del pipeline. No se deja toda la validación para el
final del sprint.
