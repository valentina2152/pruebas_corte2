# Diseño previo de casos propios (Paso 8.2)

Casos definidos antes de escribir sus pruebas. No aparecen en los tests ni en
los ejemplos resueltos del profesor.

| Función | Entrada | Resultado calculado a mano | Riesgo cubierto |
|---|---|---|---|
| promedioMovil | serie [2.25, 3.75], ventana 2 | (2.25 + 3.75) / 2 = 3.00 | Truncar los decimales antes de sumar daría 2.50. |
| promedioMovil | serie [9000, 2, 4], ventana 2 | (2 + 4) / 2 = 3.00 | Incluir valores anteriores a la ventana contaminaría el pronóstico. |
| mad | real [10, 20], pronóstico [14, 16] | (abs(-4) + abs(4)) / 2 = 4.00 | Sumar errores con signo los cancelaría y devolvería cero. |

No se modificará el resultado esperado para acomodarlo a una implementación.
