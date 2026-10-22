# Práctica UD1

## 1. Contexto de la práctica

La aplicación representa un almacén de logística.
Tiene cajas numeradas del 1 al N, y la caja número n contiene n tornillos.
Lo que queremos saber es cuántos tornillos hay en total en un tramo de cajas.

| Elemento del enunciado    | Qué representa en el almacén                                         |
|---------------------------|----------------------------------------------------------------------|
| Proceso principal (padre) | El jefe del almacén: reparte el trabajo y recoge resultados          |
| Proceso trabajador (hijo) | El encargado de una sección                                          |
| Hilos (Parte A)           | Los dos trabajadores de la sección: Trabajador_1 y Trabajador_2      |
| Array compartido          | El camión: un hueco por trabajador                                   |
| Resultado de cada hilo    | Tornillos contados en su tramo de cajas                              |
| Resultado combinado       | Total de tornillos de la sección (suma de los dos huecos del camión) |

## 2. Los tres niveles y qué es cada cosa

| Nivel         | Qué es en Java                                             | En el almacén               | PID                              |
|---------------|------------------------------------------------------------|-----------------------------|----------------------------------|
| Proceso padre | Primera ejecución del programa                             | Jefe                        | Uno propio                       |
| Proceso hijo  | Segunda ejecución del mismo programa, lanzada por el padre | Encargado                   | Uno propio, distinto al del jefe |
| Hilos         | Dos Thread dentro del hijo                                 | Trabajador_1 y Trabajador_2 | El mismo que el encargado        |

