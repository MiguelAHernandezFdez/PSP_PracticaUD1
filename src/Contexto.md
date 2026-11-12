# Práctica UD1

## 1. Contexto de la práctica

La aplicación representa un almacén de logística.
Cada caja lleva 10 tornillos. Dos trabajadores de una misma sección embalan
y cargan en un camión un número fijo de cajas, y se calcula cuántos
tornillos lleva el camión en total.

| Elemento del enunciado    | Qué representa en el almacén                                         |
|---------------------------|----------------------------------------------------------------------|
| Proceso principal (padre) | El jefe del almacén: lanza al encargado y espera su resultado        |
| Proceso trabajador (hijo) | El encargado de una sección                                          |
| Hilos (Parte A)           | Los dos trabajadores de la sección: Trabajador_1 y Trabajador_2      |
| Array compartido          | El camión: un hueco por trabajador                                   |
| Resultado de cada hilo    | Tornillos de las cajas que ha embalado (cajas × 10)                  |
| Resultado combinado       | Total de tornillos del camión (suma de los dos huecos)               |

Vocabulario de la Parte A: "encargado" es el proceso hijo y "trabajador"
es cada hilo. En la Parte B los trabajadores serán procesos, y se
documentará aparte.

## 2. Datos de la Parte A

| Trabajador   | Cajas | Tornillos por caja | Resultado esperado | Hueco del camión |
|--------------|-------|--------------------|--------------------|------------------|
| Trabajador_1 | 30    | 10                 | 300                | 0                |
| Trabajador_2 | 45    | 10                 | 450                | 1                |
| Camión       |       |                    | 750                |                  |

## 3. Los tres niveles y qué es cada cosa

| Nivel         | Qué es en Java                                             | En el almacén               | PID                              |
|---------------|------------------------------------------------------------|-----------------------------|----------------------------------|
| Proceso padre | Primera ejecución del programa                             | Jefe                        | Uno propio                       |
| Proceso hijo  | Segunda ejecución del mismo programa, lanzada por el padre | Encargado                   | Uno propio, distinto al del jefe |
| Hilos         | Dos Thread dentro del hijo                                 | Trabajador_1 y Trabajador_2 | El mismo que el encargado        |