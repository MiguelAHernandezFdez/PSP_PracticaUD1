import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
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

        // Si recibe la palabra, es un trabajador. Se mira args.length primero
        // para que el coordinador (sin argumentos) no falle al leer args[0]
        if  (args.length > 0 && args[0].equals(PALABRA)) {
            // El modo es el segundo argumento. Si no llega, uso "ok"
            String modo = args.length>1 ? args[1] : "ok";
            ejecutarTrabajador(modo);
            // Sin este return, el trabajador ejecutaria el codigo del coordinador
            return;
        }

        // A partir de aqui solo llega el coordinador. Los tres datos los escribe el usuario
        Scanner sc = new Scanner(System.in);
        System.out.println("Escribe el limite inferior (Minimo 1).");
        int limiteInferior = sc.nextInt();
        System.out.println("Escribe el limite superior (Máximo 1000000).");
        int limiteSuperior = sc.nextInt();
        System.out.println("Escribe la cantidad de trabajadores entre 2 y 4.");
        int trabajadores = sc.nextInt();

        // Si la entrada no es valida, el programa termina sin lanzar ningun trabajador
        if (!validarEntrada(limiteInferior, limiteSuperior, trabajadores)) {
            return;
        }

        // El modo llega como argumento. Si no se indica, todos los trabajadores usan "ok"
        String modo = args.length == 0 ? "ok" : args[0];
        Process trabajador = prepararTrabajador(modo).redirectError(ProcessBuilder.Redirect.INHERIT).start();
        try (PrintWriter pw = new PrintWriter(trabajador.getOutputStream())) {
            pw.println(limiteInferior + " " + limiteSuperior);
        }

        int codigo = trabajador.waitFor();
        System.out.println("Trabajador terminado, código: " + codigo);
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

        // Solo tiene sentido con los limites ordenados y los trabajadores validos, por eso es importante ponerla
        // la ultima.
        // El +1 es porque se cuentan los dos extremos
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

            // Eliminar los espacios y separar por uno cada espacio
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

        long suma = 0;
        // Recorre todos los numeros del rango, incluido ambos limites
        for (int n = desde; n <= hasta; n++) {
            suma += n;
        }

        // Envia resultado
        System.out.println(suma);
    }

    // Prepara otro proceso de Java con la instalacion y el classpath del padre
    // String... permite recibir una cantidad variable de argumentos
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
