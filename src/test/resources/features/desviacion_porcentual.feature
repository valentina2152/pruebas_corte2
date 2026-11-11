# language: es
Característica: Medición de la desviación porcentual con periodos sin demanda
  Como planeadora de producción
  Quiero medir la desviación solo sobre los periodos con demanda
  Para no reducir artificialmente el error por los periodos sin ventas

  Escenario: Un periodo sin demanda no participa en el promedio porcentual
    Dadas las demandas observadas de 80, 0 y 95 unidades
    Y las cantidades previstas de 80, 0 y 100 unidades
    Cuando reviso la desviación porcentual de la planificación
    Entonces la desviación media de los periodos con demanda es 2,63 %
