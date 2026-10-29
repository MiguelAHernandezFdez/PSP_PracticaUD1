import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PracticaUD1 {
    // throws IOException porque start() puede fallar si no se puede crear el proceso
    public static void main(String[] args) throws IOException {
        // Si el primer argumento es "encargado", este proceso actua como hijo.
        // Se comprueba primero args.length > 0 para que el jefe (que no recibe
        // argumentos) no intente leer args[0] y lance ArrayIndexOutOfBoundsException
        if (args.length > 0 && args[0].equals("encargado")) {
            // La etiqueta es el segundo argumento. Si no llega, uso una por defecto
            String etiqueta = args.length>1 ? args[1] : "Encargado";
            ejecutarEncargado(etiqueta);
            // El hijo termina aqui y NO sigue con el codigo del jefe.
            // Sin este return, el hijo crearia otro hijo y asi sin fin
            return;
        }

        // A partir de aqui solo llega el JEFE (el proceso sin argumentos)
        System.out.println("JEFE PID = " + ProcessHandle.current().pid());

        // prepararEncargado solo construye la orden, el proceso nace con start().
        // inheritIO() conecta la entrada y salida del hijo con las del jefe,
        // asi lo que imprima el encargado se ve en mi misma consola
        Process encargado = prepararEncargado("Seccion-1").inheritIO().start();
        System.out.println("ENCARGADO creado: " + encargado.pid());
        // Recien lanzado deberia seguir vivo, por eso sale true
        System.out.println("¿Esta vivo?: " + encargado.isAlive());
    }

    // Lo que hace el proceso hijo cuando se ejecuta como encargado
    static void ejecutarEncargado (String etiqueta){
        // Muestra su etiqueta y su PID, que debe ser distinto al del jefe
        System.out.println(etiqueta + " PID = " + ProcessHandle.current().pid());
    }

    // Construye la orden: java -cp <classpath> PracticaUD1 encargado Seccion-1
    // String... permite recibir una cantidad variable de argumentos
    static ProcessBuilder prepararEncargado(String... argumentos){
        // Ruta al ejecutable de Java: carpeta de instalacion (java.home) + bin.
        // En Windows el ejecutable es "java.exe" y en Linux es "java"
        String ejecutable = Path.of(
                System.getProperty("java.home"), "bin",
                System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java"
        ).toString();

        // Lista con las partes del comando, en el orden en que se ejecutan
        List<String> orden = new ArrayList<>();
        orden.add(ejecutable);
        // -cp indica donde estan las clases, y uso el mismo classpath que el jefe
        orden.add("-cp");
        orden.add(System.getProperty("java.class.path"));
        // Nombre completo de esta clase (con el paquete si lo hay), que es la que ejecuta el hijo
        orden.add(PracticaUD1.class.getName());
        // Primer argumento que le llega al hijo en args[0]
        orden.add("encargado");
        // Argumentos recibidos (la etiqueta), que le llegan a args[1]
        Collections.addAll(orden, argumentos);

        // Solo prepara el proceso, hasta que no se llame a start() no comienza
        return new ProcessBuilder(orden);
    }
}
