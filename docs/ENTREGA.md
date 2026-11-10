# Evidencias pendientes de GitHub

La validación local no sustituye una ejecución en GitHub Actions.

1. Crear un repositorio vacío propio y conectar su URL como `origin`.
2. Subir la rama `main` y comprobar los cinco jobs del workflow CI/CD.
3. Guardar una captura de la ejecución verde y su URL.
4. Cambiar temporalmente `>=` por `>` en `debeGenerarAlerta`, hacer commit y push.
5. Comprobar que fallan el caso unitario de igualdad y el escenario BDD de igualdad,
   y que el despliegue aparece omitido. Guardar captura y URL de esa ejecución.
6. Revertir ese commit con `git revert HEAD --no-edit` y subir la corrección.
7. Guardar la captura del nuevo pipeline verde y del reporte JaCoCo.
8. Completar el informe con las URLs y capturas reales y revisar las respuestas
   para expresarlas con sus propias palabras.

No subir la mutación directamente al repositorio del curso.
