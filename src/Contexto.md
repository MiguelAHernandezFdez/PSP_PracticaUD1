# Práctica UD1

Repositorio con dos clases que se ejecutan por separado:

| Clase       | Qué es  | Cómo se ejecuta                                                                                   |
|-------------|---------|---------------------------------------------------------------------------------------------------|
| ParteA.java | Parte A | Sin argumentos                                                                                    |
| ParteB.java | Parte B | Escribe los tres datos por consola. El modo (ok, error, lento o malformado) es un argumento opcional |

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

## 4. Pausa, interrupción y decisiones

| Requisito                    | Cómo se resuelve                                                              |
|------------------------------|-------------------------------------------------------------------------------|
| Evitar procesos infinitos    | El hijo se reconoce por el argumento "encargado" y sale con return            |
| Pausa de observación         | Thread.sleep de 10 segundos en cada hilo, solo para verlos con pgrep          |
| Interrupción                 | El catch restaura la marca con interrupt() y avisa por System.err             |

Si se interrumpe a un hilo durante la pausa no llega a escribir su hueco del
camión, que se queda en 0. Se comprobó interrumpiendo a Trabajador_1.

Decisiones: los hilos recorren sus cajas y suman 10 tornillos por vuelta;
comparten un array int[] de dos huecos; el programa se distingue por
argumentos (sin argumentos es el jefe, con "encargado" es el hijo); una sola
clase con un método por responsabilidad.

# Práctica UD1 - Parte B

## 1. Contexto de la práctica

El mismo almacén, ahora con muchas más cajas. Las cajas están numeradas y
la caja número n contiene n tornillos. El coordinador quiere saber cuántos
tornillos hay en un tramo de cajas (del límite inferior al superior) y
reparte el recuento entre varios trabajadores.

Diferencia con la Parte A: allí todas las cajas llevan 10 tornillos. Aquí
la caja n lleva n tornillos, para que el recuento sea la suma de los
números de un intervalo, que es lo que pide el enunciado.

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
resultado viaja por la salida estándar del trabajador, que el coordinador
redirige a un archivo temporal.

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

| Orden | Condición de error                         | Mensaje                                                                                        |
|-------|--------------------------------------------|------------------------------------------------------------------------------------------------|
| 1     | Algún dato no es un número entero          | Debes escribir un número entero, por favor.                                                    |
| 2     | Inferior menor que 1                       | El límite inferior debe ser al menos 1.                                                        |
| 3     | Superior mayor que 1.000.000               | El límite superior no puede superar 1000000.                                                   |
| 4     | Inferior mayor que superior                | El límite inferior (X) no puede ser mayor que el superior (Y).                                 |
| 5     | Trabajadores menores que 2 o mayores que 4 | El número de trabajadores debe estar entre 2 y 4.                                              |
| 6     | Menos números que trabajadores             | El intervalo tiene (X) números y hay (Y) trabajadores: debe haber al menos uno por trabajador. |

La primera condición se comprueba al leer los datos con Scanner (captura
InputMismatchException) y las demás en el método validarEntrada.

## 3. Reparto del intervalo

Cada trabajador recibe un tramo sin solapes ni números omitidos, y la
diferencia entre dos trabajadores es como máximo de uno. El sobrante de la
división lo reciben los últimos trabajadores.

El desde de cada trabajador es el hasta del anterior más 1, porque
los tamaños ya no son todos iguales. El primero empieza en el límite inferior
que escribe el usuario, no en 1.

Comprobaciones: el hasta del último trabajador es el límite superior, y los
tamaños suman total.

```
total = superior - inferior + 1
base  = total / trabajadores        (división entera)
resto = total % trabajadores
siguienteDesde = inferior

para i desde 0 hasta trabajadores - 1:
    tamaño = base
    si i >= trabajadores - resto:
        tamaño = tamaño + 1
    desde = siguienteDesde
    hasta = desde + tamaño - 1
    siguienteDesde = hasta + 1
```

| Intervalo | Trabajadores | Reparto                |
|-----------|--------------|------------------------|
| 1-10      | 4            | 1-2, 3-4, 5-7, 8-10    |
| 1-100     | 3            | 1-33, 34-66, 67-100    |
| 5-11      | 2            | 5-7, 8-11              |

## 4. Comunicación entre coordinador y trabajador

Contrato fijado por el enunciado:

| Canal                           | Contenido                                           |
|---------------------------------|-----------------------------------------------------|
| Entrada estándar del trabajador | Una línea con sus dos límites, separados por espacio |
| Salida estándar del trabajador  | Un único entero con el resultado, si termina bien   |
| Salida de error del trabajador  | Diagnóstico o explicación de fallos                 |

El coordinador cierra la entrada del trabajador tras enviar el encargo.
El trabajador valida su entrada aunque venga del coordinador. Si es
incorrecta (línea vacía, no son dos enteros o el rango no es válido),
escribe el motivo por la salida de error y termina con código 2.

| Código de salida | Significado          |
|------------------|----------------------|
| 0                | Finalización correcta |
| 2                | Entrada incorrecta   |
| 3                | Fallo simulado       |

### Recogida de resultados: archivos temporales

El resultado de cada trabajador se recoge en un archivo temporal propio
y no leyendo su salida con un flujo. Los archivos temporales se guardan en la carpeta temporales/.

Motivo: el coordinador espera como máximo 3 segundos a cada trabajador.
Leer un flujo con readAllBytes() bloquea hasta que el trabajador cierra su
salida, así que un trabajador lento dejaría al coordinador parado y el límite
de 3 segundos no se aplicaría. Con archivos, el waitFor con tiempo máximo
manda y el archivo se lee después, solo si el trabajador terminó bien.

Cómo se evita el bloqueo de los canales:

| Canal            | Cómo se gestiona                                | Por qué no bloquea                                  |
|------------------|-------------------------------------------------|-----------------------------------------------------|
| Entrada estándar | El coordinador escribe una línea y cierra el canal | El trabajador recibe el fin de entrada y no espera más datos |
| Salida estándar  | Redirigida a un archivo temporal                | No hay tubería que se pueda llenar                  |
| Salida de error  | Hereda la consola del coordinador               | Tampoco hay tubería                                 |

Hay que borrar los archivos al terminar, incluso si algo falla.
Se hace en un bloque finally.

## 5. Modos de prueba y fallos

Los modos especiales solo afectan al último trabajador: el coordinador le
pasa el modo del usuario y a los demás les pasa siempre ok.

| Modo       | Comportamiento del último trabajador                  |
|------------|-------------------------------------------------------|
| ok         | Todos calculan correctamente                          |
| error      | Informa de un fallo y termina con código 3            |
| lento      | Espera 10 segundos antes de responder                 |
| malformado | Termina con código 0 pero devuelve texto, no un número |

### Detección de fallos

El coordinador lanza a todos los trabajadores y después espera a cada uno
como máximo 3 segundos. Distingue tres fallos:

| Fallo                          | Cómo se detecta                                  | Mensaje                                    |
|--------------------------------|--------------------------------------------------|--------------------------------------------|
| Tiempo agotado                 | waitFor con límite devuelve false                | Trabajador N: tiempo agotado               |
| Termina con error              | Terminó y su código no es 0                      | Trabajador N: terminó con error, código C  |
| Respuesta incumple el contrato | Código 0, pero su salida no es un número         | Trabajador N: respuesta no válida          |

El tiempo se comprueba antes que el código porque exitValue() falla si el
proceso sigue vivo. Si falla algún trabajador no se muestra el total.

### Limpieza

Se hace en un bloque finally, así que ocurre siempre, también si el fallo
llega a mitad del lanzamiento. Los procesos y los archivos están en arrays
con una posición por trabajador, y las que no llegaron a crearse quedan en
null y se saltan.

- Los procesos que siguen vivos se matan con destroyForcibly() y se espera
  a que terminen.
- Los archivos temporales se borran. La carpeta temporales/ queda vacía y
  está en el .gitignore.

## 6. Casos de prueba

Todos los casos se ejecutaron y el resultado coincide con el esperado. Tras
cada fallo se comprobó con `pgrep -af ParteB` que no quedaba ningún proceso
vivo y que la carpeta `temporales/` estaba vacía.

### Entradas válidas

| Caso                           | Resultado                                  |
|--------------------------------|--------------------------------------------|
| desde 1 al 100 con 3 trabajadores    | 5050 (caso obligatorio)                    |
| desde 1 al 10 con 4 trabajadores     | 55, con tramos de tamaño 2, 2, 3 y 3       |
| desde 5 al 11 con 2 trabajadores     | 56                                         |
| desde 1 al 4 con 4 trabajadores      | 10, un número por trabajador               |
| desde 1 al 1000000 con 4 trabajadores| 500000500000 (la suma necesita long)       |

### Entradas rechazadas

| Caso                           | Resultado                                  |
|--------------------------------|--------------------------------------------|
| desde 1 al 3 con 4 trabajadores      | Intervalo demasiado pequeño                |
| Límite inferior 0              | El límite inferior debe ser al menos 1     |
| Límite superior 2000000        | El límite superior no puede superar 1000000|
| Inferior 50 y superior 10      | El inferior no puede ser mayor que el superior |
| 5 o 1 trabajadores             | El número de trabajadores debe estar entre 2 y 4 |
| Letras o decimales             | Mensaje de número entero, sin excepción    |

### Fallos (desde 1 al 100)

| Modo       | Trabajadores | Resultado                                              |
|------------|--------------|--------------------------------------------------------|
| error      | 3            | Trabajador 2 termina con código 3, sin total           |
| lento      | 3            | Tiempo agotado a los 3 segundos, sin total             |
| malformado | 3            | Respuesta no válida del trabajador 2, sin total        |
| cualquiera | 4 y 2        | El fallo cae siempre en el último (el 3 y el 1)        |