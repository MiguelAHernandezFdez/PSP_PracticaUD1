# Práctica UD1 - Parte A

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

# Práctica UD1 - Parte B

## 1. Contexto de la práctica

El mismo almacén, ahora con muchas más cajas. Las cajas están numeradas y
la caja número n contiene n tornillos. El coordinador quiere saber cuántos
tornillos hay en un tramo de cajas (del límite inferior al superior) y
reparte el recuento entre varios trabajadores.

| Elemento del enunciado | Qué representa en el almacén                                                   |
|------------------------|--------------------------------------------------------------------------------|
| Coordinador            | El jefe del almacén: reparte los tramos, recoge los recuentos y suma el total  |
| Trabajadores (2 a 4)   | Cada uno cuenta los tornillos de su tramo de cajas                             |
| Encargo                | El tramo de cajas que recibe cada trabajador (desde, hasta)                    |
| Resultado parcial      | Tornillos contados por un trabajador                                           |
| Resultado final        | Suma de los recuentos de todos los trabajadores                                |

Vocabulario de la Parte B: aquí los trabajadores son procesos distintos
(no hilos) y no existe la figura del encargado. Cada trabajador tiene su
propio PID y su propia memoria, así que no hay camión compartido: el
resultado viaja por la salida estándar del trabajador.

## 2. Datos de entrada

El usuario escribe tres datos con Scanner. El modo llega como argumento
(por defecto, `ok`).

| Dato                   | Regla                             |
|------------------------|-----------------------------------|
| Límite inferior        | Al menos 1                        |
| Límite superior        | Como máximo 1.000.000             |
| Número de trabajadores | Entre 2 y 4                       |
| Tamaño del intervalo   | Al menos un número por trabajador |

### Validación

Se comprueba en este orden y se rechaza en el primer fallo. El orden importa:
la última regla solo tiene sentido con los límites ordenados y un número de
trabajadores válido.

| Orden | Condición de error                       | Mensaje                                                                         |
|-------|------------------------------------------|---------------------------------------------------------------------------------|
| 1     | Inferior menor que 1                     | El límite inferior debe ser al menos 1.                                         |
| 2     | Superior mayor que 1.000.000             | El límite superior no puede superar 1000000.                                    |
| 3     | Inferior mayor que superior              | El límite inferior (X) no puede ser mayor que el superior (Y).                  |
| 4     | Trabajadores menores que 2 o mayores que 4 | El número de trabajadores debe estar entre 2 y 4.                             |
| 5     | Menos números que trabajadores           | El intervalo tiene (X) números y hay (Y) trabajadores: debe haber al menos uno por trabajador. |

La validación se hace antes de crear ningún proceso, así que un error de
entrada no deja nada que limpiar.

## 3. Reparto del intervalo

Cada trabajador recibe un tramo sin solapes ni números omitidos, y la
diferencia entre dos trabajadores es como máximo de uno. El sobrante de la
división lo reciben los últimos trabajadores.

El desde de cada trabajador es el hasta del anterior más 1, porque
los tamaños ya no son todos iguales. El primero empieza en el límite inferior
que escribe el usuario, no en 1.

Comprobaciones: el hasta del último trabajador es el límite superior, y los
tamaños suman total.

## 4. Comunicación entre coordinador y trabajador

Contrato fijado por el enunciado:

| Canal                           | Contenido                                           |
|---------------------------------|-----------------------------------------------------|
| Entrada estándar del trabajador | Una línea con sus dos límites, separados por espacio |
| Salida estándar del trabajador  | Un único entero con el resultado, si termina bien   |
| Salida de error del trabajador  | Diagnóstico o explicación de fallos                 |

El coordinador cierra la entrada del trabajador tras enviar el encargo.
El trabajador valida su entrada aunque venga del coordinador.

| Código de salida | Significado          |
|------------------|----------------------|
| 0                | Finalización correcta |
| 2                | Entrada incorrecta   |
| 3                | Fallo simulado       |


## 5. Modos de prueba y fallos

Los modos especiales solo afectan al último trabajador.

| Modo       | Comportamiento del último trabajador                  |
|------------|-------------------------------------------------------|
| ok         | Todos calculan correctamente                          |
| error      | Informa de un fallo y termina con código 3            |
| lento      | Espera 10 segundos antes de responder                 |
| malformado | Termina con código 0 pero devuelve texto, no un número |

El coordinador espera como máximo 3 segundos por cada espera de un trabajador.

## 6. Casos de prueba

| Caso                               | Resultado esperado                          |
|------------------------------------|---------------------------------------------|
| 1 al 100 con 3 trabajadores        | 5050 (caso obligatorio)                     |
| Intervalo no divisible (1-10 con 4)| Total 55, con tramos de tamaño 2, 2, 3 y 3  |
| 1 al 4 con 4 trabajadores          | Válido, un número por trabajador            |
| 1 al 3 con 4 trabajadores          | Rechazado: intervalo demasiado pequeño      |
| Los cuatro modos                   | Pendiente                                   |