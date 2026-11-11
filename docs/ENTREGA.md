# Entrega del Corte 2

Repositorio: https://github.com/valentina2152/pruebas_corte2

Informe: [INFORME_Valentina_Barreto.md](../INFORME_Valentina_Barreto.md).

Se completaron MSE con TDD, tres casos unitarios propios, el escenario BDD de
MAPE con demanda cero, la regla EmptyStatement y la revisión estática con checklist.

Verificación local: `mvn clean verify`, 47 pruebas correctas, sin infracciones de
Checkstyle y 100 % de líneas y ramas de la calculadora. El mínimo exigido es 90 %.

## Evidencias remotas

- [Verde inicial](https://github.com/valentina2152/pruebas_corte2/actions/runs/43119850641).
- [Rojo provocado](https://github.com/valentina2152/pruebas_corte2/actions/runs/43120102531):
  fallan unitarias, BDD y cobertura; el despliegue se omite.
- [Verde después de revertir](https://github.com/valentina2152/pruebas_corte2/actions/runs/43120295469):
  pasan los cuatro controles y el despliegue simulado.

Las capturas y los registros están en `docs/evidencias/` y se muestran en el
informe. Los registros TDD con nombre `rojo` contienen fallos deliberados
históricos, no el estado final del proyecto.

El enlace del repositorio está listo para entregar en la plataforma del curso.
Lee el informe para poder explicar los cálculos, las decisiones y los hallazgos.
