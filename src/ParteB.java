import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ParteB {
    // Palabra que el coordinador pasa como primer argumento a cada trabajador.
    // Se usa en el if de main y al construir la orden, por eso esta en una constante
    static final String PALABRA = "trabajador";
    static final int MIN_TRABAJADORES = 2;
    static final int MAX_TRABAJADORES = 4;
    static final long MAX_LIMITE = 1000000;
    static final int MAX_ESPERA_SEG = 3; // Espera maxima por trabajador, en segundos

    public static void main(String[] args) throws Exception {

        // Si el primer argumento es la palabra, este proceso es un trabajador.
        // Se mira args.length primero para no leer args[0] si no hay argumentos
        if  (args.length > 0 && args[0].equals(PALABRA)) {
            // El modo es el segundo argumento. Si no llega, uso "ok"
            String modo = args.length>1 ? args[1] : "ok";
            ejecutarTrabajador(modo);
            // Sin este return, el trabajador ejecutaria el codigo del coordinador
            return;
        }

        // A partir de aqui solo llega el coordinador. Los tres datos los escribe el usuario
        int limiteInferior;
        int limiteSuperior;
        int trabajadores;

        try {
            Scanner sc = new Scanner(System.in);
            System.out.println("Escribe el limite inferior (Minimo 1).");
            limiteInferior = sc.nextInt();
            System.out.println("Escribe el limite superior (Máximo 1000000).");
            limiteSuperior = sc.nextInt();
            System.out.println("Escribe la cantidad de trabajadores entre 2 y 4.");
            trabajadores = sc.nextInt();
        } catch (InputMismatchException e) {
            System.err.println("Debes escribir un número entero, por favor.");
            return;
        }

        // Si la entrada no es valida, termina sin lanzar ningun trabajador
        if (!validarEntrada(limiteInferior, limiteSuperior, trabajadores)) {
            return;
        }

        // El modo llega como argumento (por defecto "ok") y solo lo recibe el ultimo trabajador
        String modo = args.length == 0 ? "ok" : args[0];

        // Reparto del intervalo: el sobrante de la division va a los ultimos trabajadores
        int total = limiteSuperior - limiteInferior + 1;
        int base = total / trabajadores;
        int resto = total % trabajadores;
        int siguienteDesde = limiteInferior;

        // Un hueco por trabajador. Si solo se llegan a crear algunos, el resto queda en null
        Process[] procesos = new Process[trabajadores];
        Path[] archivos = new Path[trabajadores];

        // Carpeta del proyecto para los archivos temporales (esta en el .gitignore)
        Path carpeta = Path.of("temporales");
        Files.createDirectories(carpeta);

        try {
            // Primer bucle: lanza todos los trabajadores y les envia su encargo, sin esperar a ninguno
            for (int i = 0; i < trabajadores; i++) {
                int tamano = base;
                if (i >= trabajadores - resto) {
                    tamano++;
                }
                int desde = siguienteDesde;
                int hasta = desde + tamano - 1;
                siguienteDesde = hasta + 1;

                // Solo el ultimo trabajador recibe el modo especial, los demas usan "ok"
                String modoTrabajador = i == trabajadores - 1 ? modo : "ok";

                // Resultado en archivo temporal y no en flujo: asi waitFor con limite manda y nada se bloquea
                // Se guarda en cuanto se crea, para poder borrarlo aunque start() falle
                archivos[i] = Files.createTempFile(carpeta, "trabajador-", ".txt");
                procesos[i] = prepararTrabajador(modoTrabajador)
                        .redirectError(ProcessBuilder.Redirect.INHERIT)
                        .redirectOutput(archivos[i].toFile()).start();

                // Al salir del try se cierra la entrada y el trabajador deja de esperar
                try (PrintWriter pw = new PrintWriter(procesos[i].getOutputStream())) {
                    pw.println(desde + " " + hasta);
                }
            }

            // Segundo bucle: con todos en marcha, se espera a cada uno y se recoge su resultado
            long sumaTotal = 0;
            boolean todoCorrecto = true;

            for (int i = 0; i < trabajadores; i++) {
                // true si termino a tiempo, false si se agoto la espera y sigue vivo
                boolean terminado = procesos[i].waitFor(MAX_ESPERA_SEG, TimeUnit.SECONDS);

                if (!terminado) {
                    System.out.println("Trabajador " + i + ": tiempo agotado (más de " + MAX_ESPERA_SEG + " segundos).");
                    todoCorrecto = false;
                // exitValue() falla si el proceso sigue vivo, por eso va despues de comprobar el tiempo
                } else if (procesos[i].exitValue() != 0) {
                    System.out.println("Trabajador " + i + ": terminó con error, código " + procesos[i].exitValue() + ".");
                    todoCorrecto = false;
                } else {
                    String textoLeido = Files.readString(archivos[i]).trim();
                    // Solo la conversion puede fallar: el trabajador termino bien pero pudo devolver texto
                    try {
                        sumaTotal += Long.parseLong(textoLeido);
                    } catch (NumberFormatException e) {
                        System.out.println("Trabajador " + i +
                                ": respuesta no valida, devolvio " + textoLeido + " en lugar de un número.");
                        todoCorrecto = false;
                    }
                }
            }

            if (todoCorrecto) {
                System.out.println("Total de tornillos: " + sumaTotal);
            }
        } finally {
            // Se ejecuta siempre, aunque algo falle: mata los trabajadores que sigan vivos, espera a que terminen y
            // borra sus archivos. Solo se toca lo que llego a crearse
            for (int i = 0; i < trabajadores; i++) {
                if (procesos[i] != null && procesos[i].isAlive()) {
                    procesos[i].destroyForcibly();
                    procesos[i].waitFor();
                }
                if (archivos[i] != null) {
                    Files.deleteIfExists(archivos[i]); // Borrar los archivos temporales si los hay
                }
            }
        }
    }

    // Comprueba las reglas del enunciado, de la primera a la ultima.
    // Devuelve false en cuanto una falla, para no seguir con datos incorrectos
    static boolean validarEntrada(int limiteInferior, int limiteSuperior, int trabajadores) {
        if (limiteInferior < 1) {
            System.out.println("El límite inferior debe ser al menos 1.");
            return false;
        }

        if (limiteSuperior > MAX_LIMITE) {
            System.out.println("El límite superior no puede superar " + MAX_LIMITE + ".");
            return false;
        }

        if (limiteInferior > limiteSuperior) {
            System.out.println("El límite inferior (" + limiteInferior
                    + ") no puede ser mayor que el superior (" + limiteSuperior + ").");
            return false;
        }

        if (trabajadores < MIN_TRABAJADORES || trabajadores > MAX_TRABAJADORES) {
            System.out.println("El número de trabajadores debe estar entre "
                    + MIN_TRABAJADORES + " y " + MAX_TRABAJADORES + ".");
            return false;
        }

        // Va la ultima en comprobaciones: necesita los limites ordenados y un numero de trabajadores valido. El +1 es
        // porque se cuentan los dos extremos
        int total = limiteSuperior - limiteInferior + 1;
        if (total < trabajadores) {
            System.out.println("El intervalo tiene " + total + " números y hay " + trabajadores
                    + " trabajadores: debe haber al menos uno por trabajador.");
            return false;
        }
        // Si ninguna condicion ha fallado, la entrada es valida
        return true;
    }



    // Trabajo de cada proceso trabajador
    static void ejecutarTrabajador(String modo) throws Exception {
        int desde; // Guarda el limite inferior del rango
        int hasta; // Guarda el limite superior del rango

        try {
            // Crear un lector de texto
            var lector = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)); // Interpreta los bytes como UTF-8

            // Leer la linea y comprobar que no es null
            String linea = lector.readLine();
            if (linea == null) {
                throw new IllegalArgumentException("La entrada está vacía");
            }

            // Quita espacios sobrantes y separa por espacios
            String[] partes = linea.trim().split("\\s+");
            // Debe de haber exactamente dos valores: inicio y final
            if (partes.length != 2) {
                throw new IllegalArgumentException("Se esperan dos limites");
            }

            desde = Integer.parseInt(partes[0]); // Convierte el primer texto a int
            hasta = Integer.parseInt(partes[1]); // Convierte el segundo texto a int

            // Comprobar las tres reglas del rango
            if (desde < 1 || hasta > MAX_LIMITE || desde > hasta) {
                throw new IllegalArgumentException("Rango no válido, debe ser desde 1 hasta 1.000.000 y de menor a mayor.");
            }

        } catch (IllegalArgumentException e) {
            System.err.println("ENTRADA INVALIDA: " + e.getMessage());
            System.exit(2);
            return;
        }

        // Solo retrasa: despues calcula con normalidad
        if (modo.equals("lento")){
            Thread.sleep(10_000);
        }

        // Si pide simular un error
        if (modo.equals("error")){
            System.err.println("Fallo simulado");
            System.exit(3); // Termino con codigo error 3
        }

        // Si se pidio una respuesta con formato incorrecto
        if (modo.equals("malformado")){
            System.out.println("no-numero"); /// Escribe texto en vez de un numero; el return evita escribir tambien el resultado
            return;
        }

        long suma = 0;
        // Recorre todos los numeros del rango, incluido ambos limites
        for (int n = desde; n <= hasta; n++) {
            suma += n;
        }

        // Envia resultado
        System.out.println(suma);
    }

    // Construye la orden: java -cp <classpath> ParteB coordinador <modo>
    static ProcessBuilder prepararTrabajador(String... argumentos){
        // Construir la ruta al ejecutable de java
        String ejecutable = Path.of(
                System.getProperty("java.home"), "bin",
                System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java"
        ).toString();

        // creo una lista vacía con las partes del comando que se van a ejecutar
        List<String> orden = new ArrayList<>();

        orden.add(ejecutable);
        orden.add("-cp");
        orden.add(System.getProperty("java.class.path"));
        orden.add(ParteB.class.getName());
        // Primer argumento del nuevo proceso
        orden.add(PALABRA);
        // Añado los argumentos recibidos
        Collections.addAll(orden, argumentos);
        // Devuelve el objeto que permite lanzar el comando
        return new ProcessBuilder(orden);
    }
}
