# Revisión informal de debeGenerarAlerta

Revisión por lectura del método existente, sin ejecutarlo. Referencias a
`src/main/java/pronosticos/CalculadoraPronosticos.java`, después del refactor de
MSE (`ae5e5e5`). No se modifica la regla de negocio como parte de esta revisión.

| # | Pregunta | Respuesta | Evidencia y explicación |
|---|---|---|---|
| 1 | ¿El nombre dice qué hace? | Sí | Línea 109: `debeGenerarAlerta` expresa una decisión booleana sobre la alerta. |
| 2 | ¿Los nombres indican qué contienen? | Sí | Línea 109: `errorPorcentual` identifica la unidad; `umbral` es el límite usado en la comparación. |
| 3 | ¿Tiene una sola responsabilidad? | No, estrictamente | Líneas 110–111 validan el umbral y la 113 decide la alerta. La validación es breve y local, pero son dos tareas distinguibles. |
| 4 | ¿Repite lógica existente? | No | Líneas 110–113: no hay otra validación del intervalo porcentual 0–100 en la clase; la validación de alfa usa otro dominio. |
| 5 | ¿Hay números que deberían tener nombre? | Sí | Línea 110: `100` representa el máximo porcentual permitido; una constante comunicaría ese significado. |
| 6 | ¿Los comentarios explican el porqué? | Sí | Líneas 106–107: explican el acuerdo con Producción de incluir la igualdad, una decisión que podría confundirse con `>`. |
| 7 | ¿Cada dato inválido produce un error explícito? | No | Líneas 110–111: solo se rechaza el umbral menor que 0 o mayor que 100. `NaN` no satisface ninguna comparación; tampoco se valida un error porcentual negativo o no finito. |
| 8 | ¿Se entiende de una lectura? | Sí | Líneas 110–113: un `if` con dos condiciones unidas por OR y una comparación de resultado. Complejidad ciclomática 3 (base 1 + if + OR). |
| 9 | ¿Cada comportamiento tiene una prueba? | No, para entradas no definidas | `AlertasTest` cubre menor, igual, mayor, extremos 0/100 y umbrales inválidos -1/150. No hay casos de NaN, error negativo ni infinitos. |

## Hallazgos

1. **El umbral NaN no se rechaza** (línea 110). Por las reglas de comparación de
   `double`, ambas condiciones son falsas, y la comparación final tampoco activa
   la alerta. Un valor sin significado numérico podría pasar inadvertido.
   Propuesta: acordar rechazo de datos no finitos, escribir primero la prueba y
   luego validar con `Double.isFinite`.
2. **El contrato del error porcentual está incompleto** (líneas 109 y 113).
   No especifica qué hacer ante valores negativos, NaN o infinitos. Se podría
   ocultar una entrada errónea o generar una alerta por un infinito.
   Propuesta: acordar con Producción que el error sea finito y no negativo,
   sin limitarlo automáticamente a 100 porque un MAPE sí puede superar 100 %.
   Después añadir pruebas antes de cambiar la implementación.
3. **Límite porcentual sin nombre** (línea 110). Propuesta: una constante
   `UMBRAL_MAXIMO_PORCENTUAL` para expresar el significado del 100 si se refactoriza.

No se cambió el contrato en esta revisión: la guía permite registrar propuestas.
Checkstyle puede detectar problemas de forma, pero estas reglas configuradas no
deducen qué datos debería aceptar Producción ni la consecuencia de una alerta
silenciosamente omitida. Esos son los hallazgos que aporta la lectura del dominio.
