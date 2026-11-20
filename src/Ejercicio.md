# PRÁCTICA UD1

## 1. Situación de partida

Debes crear una aplicación que reparta trabajo entre procesos y permita observar cómo se ejecutan varias tareas.

Tú elegirás el contexto de la aplicación. Puede representar, por ejemplo, un taller, un laboratorio o un centro de distribución. También puedes inventar otro escenario.

El contexto debe tener sentido: explicarás qué representa el proceso principal, qué representan los trabajadores y qué resultado producen.

No se proporciona una plantilla de código. Debes decidir la estructura de clases, los métodos, los mensajes y la organización del programa.

La práctica tendrá dos partes que podrán ejecutarse por separado.

## 2. Parte A. Un proceso trabajador con dos hilos

Construye una aplicación con el siguiente comportamiento:

- El proceso principal muestra su identificador de proceso —PID— y crea un proceso Java independiente.
- El proceso creado recibe una etiqueta que permita identificarlo.
- Dentro del proceso trabajador se crean dos hilos con nombres diferentes.
- Cada hilo realiza una tarea que tú debes diseñar y produce un resultado numérico.
- Ambos hilos almacenan sus resultados en una estructura compartida. Cada uno debe escribir en una posición diferente.
- El proceso trabajador espera a que terminen los dos hilos y combina sus resultados.
- El proceso principal espera a que termine el trabajador e informa de su código de salida.

La aplicación debe mostrar:

- El PID del proceso principal y del trabajador.
- El nombre y el PID desde el que se ejecuta cada hilo.
- El estado de uno de los hilos antes de iniciarlo y después de esperar a su finalización.
- Los resultados individuales y el resultado combinado.
- Si el proceso trabajador sigue vivo al consultarlo y después de que haya terminado.

Introduce una pausa controlada que permita observar los procesos durante la ejecución. Esa pausa sirve para la observación y no sustituye el trabajo que deben realizar los hilos.

Debes evitar que el trabajador vuelva a crear procesos indefinidamente. También debes gestionar la interrupción de sus tareas.

**Decisiones que te corresponden:**

- Qué calculan los hilos.
- Qué estructura comparten.
- Cómo se distinguen los modos de ejecución.
- Cómo se organizan las clases y los métodos.

## 3. Parte B. Reparto de un cálculo entre varios procesos

Construye un coordinador que calcule la suma de los números enteros de un intervalo, repartiendo el trabajo entre procesos Java independientes.

El usuario indicará:

- El límite inferior.
- El límite superior.
- El número de procesos trabajadores.

**Condiciones de entrada:**

- Los límites deben cumplir: 1 ≤ límite inferior ≤ límite superior ≤ 1.000.000.
- Se podrán crear entre 2 y 4 trabajadores.
- El intervalo debe contener al menos tantos números como trabajadores.
- Una entrada incorrecta debe rechazarse con un mensaje comprensible.

**El coordinador debe:**

- Dividir el intervalo en subintervalos sin solapamientos ni números omitidos.
- Repartir la carga de forma equilibrada: la diferencia entre las cantidades de números asignadas a dos trabajadores será, como máximo, de uno.
- Iniciar todos los trabajadores y enviarles su encargo antes de comenzar a esperar sus resultados.
- Recoger y validar cada resultado parcial.
- Sumar los resultados y mostrar el total únicamente si todas las tareas han terminado correctamente.

Cada trabajador debe calcular su suma mediante un recorrido de los números asignados. Puedes utilizar la fórmula matemática de la suma para comprobar el resultado, pero no para sustituir el trabajo de los procesos.

**Caso de comprobación obligatorio:** el intervalo del 1 al 100 debe producir un total de 5050.

También debes probar un intervalo cuyo tamaño no sea divisible entre el número de trabajadores.

## 4. Comunicación entre coordinador y trabajadores

El intercambio de información debe cumplir este contrato:

| Canal | Contenido |
|---|---|
| Entrada estándar del trabajador | Una línea con los dos límites de su intervalo, separados por un espacio |
| Salida estándar del trabajador | Un único número entero con el resultado, cuando termina correctamente |
| Salida de error del trabajador | Mensajes de diagnóstico o explicación de fallos |

El coordinador debe cerrar la entrada del trabajador después de enviar el encargo.

No debes mezclar mensajes informativos con el resultado numérico en la salida estándar del trabajador.

Cada trabajador validará su entrada, aunque proceda del coordinador.

Se utilizarán estos códigos de salida:

| Código | Significado |
|---|---|
| 0 | Finalización correcta |
| 2 | Entrada incorrecta |
| 3 | Fallo simulado |

Puedes recoger la salida mediante flujos o redirigirla a archivos temporales. Debes justificar tu elección y evitar que los procesos queden bloqueados por una gestión incorrecta de sus canales.

## 5. Detección y gestión de fallos

La aplicación debe permitir ejecutar cuatro modos de prueba. Los modos especiales afectarán únicamente al último trabajador.

| Modo | Comportamiento |
|---|---|
| ok | Todos los trabajadores calculan correctamente |
| error | El último trabajador informa de un fallo y termina con código 3 |
| lento | El último trabajador introduce una espera de 10 segundos |
| malformado | El último trabajador termina con código 0, pero devuelve un texto que no es un número |

El coordinador esperará como máximo 3 segundos en cada operación de espera de un trabajador. Esta condición no significa que toda la aplicación deba terminar en 3 segundos.

Debe distinguir entre:

- Un trabajador que termina con código de error.
- Un trabajador que supera el tiempo de espera.
- Un trabajador que devuelve una respuesta que incumple el contrato.

Cuando ocurra un fallo:

- Se identificará el trabajador afectado y el motivo.
- No se mostrará un total como si la ejecución hubiera sido correcta.
- Se finalizarán los procesos creados por la aplicación que sigan activos.
- Se esperará a su finalización.
- Se cerrarán los recursos y se eliminarán los archivos temporales creados, si los hay.