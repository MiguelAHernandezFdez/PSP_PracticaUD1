import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParteA {
    static final int NUM_TORNILLOS_CAJA = 10; // Tornillos que lleva cada caja

    // IOException por start() e InterruptedException por waitFor()
    public static void main(String[] args) throws IOException, InterruptedException {
        // Si recibe "encargado" es el proceso hijo. Se mira args.length primero
        // para que el jefe (sin argumentos) no falle al leer args[0]
        if (args.length > 0 && args[0].equals("encargado")) {
            String etiqueta = args.length>1 ? args[1] : "Encargado";
            ejecutarEncargado(etiqueta);
            // Sin este return, el hijo ejecutaria el codigo del jefe y crearia hijos sin fin
            return;
        }

        // A partir de aqui solo llega el jefe
        System.out.println("JEFE PID = " + ProcessHandle.current().pid());

        // inheritIO() hace que lo que imprima el hijo salga en mi consola
        Process encargado = prepararEncargado("Seccion-1").inheritIO().start();
        System.out.println("ENCARGADO creado: " + encargado.pid());
        System.out.println("¿Está el encargado?: " + encargado.isAlive());
        // Espera a que el hijo termine y devuelve su codigo de salida
        int codigo = encargado.waitFor();
        System.out.println("Encargado se ha ido, código: " + codigo);
        System.out.println("¿Sigue el encargado aquí?: " + encargado.isAlive());
    }

    // Trabajo del proceso hijo: crea el camion y los dos hilos
    static void ejecutarEncargado (String etiqueta) throws InterruptedException {
        System.out.println(etiqueta + " PID = " + ProcessHandle.current().pid());
        // Un hueco por trabajador. Los hilos viven en este proceso, por eso comparten el array
        int[] camion = new int[2];
        // new Thread solo crea el hilo (estado NEW), no se ejecuta hasta start()
        // Trabajador_1: 30 cajas, escribe en el hueco 0
        Thread trabajador1 = new Thread(() -> tarea(camion, 0, 30), "_1");
        // Trabajador_2: 45 cajas, escribe en el hueco 1
        Thread trabajador2 = new Thread(() -> tarea(camion, 1, 45), "_2");

        System.out.println("Estado del Trabajador_1 antes de empezar: " + trabajador1.getState());

        trabajador1.start();
        trabajador2.start();

        // trabajador1.interrupt(); // Descomentar para comprobar que salta el catch de tarea

        // Los dos start() van antes que los join() para que trabajen a la vez
        trabajador1.join();
        trabajador2.join();

        System.out.println("Estado del Trabajador_1 después de terminar: " + trabajador1.getState());

        System.out.println("Hueco del camión izquierdo: " + camion[0]);
        System.out.println("Hueco del camión derecho: " + camion[1]);
        System.out.println("Carga total del camion: " + (camion[0] + camion[1]));
    }

    // Construye la orden: java -cp <classpath> ParteA encargado <etiqueta>
    static ProcessBuilder prepararEncargado(String... argumentos){
        // Ejecutable de Java segun el sistema operativo
        String ejecutable = Path.of(
                System.getProperty("java.home"), "bin",
                System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java"
        ).toString();

        List<String> orden = new ArrayList<>();
        orden.add(ejecutable);
        orden.add("-cp");
        orden.add(System.getProperty("java.class.path")); // Mismo classpath que el jefe
        orden.add(ParteA.class.getName());
        orden.add("encargado"); // Llega al hijo en args[0]
        Collections.addAll(orden, argumentos); // La etiqueta llega en args[1]

        // Solo prepara el proceso, no arranca hasta que se llame a start()
        return new ProcessBuilder(orden);
    }

    // Cada trabajador embala sus cajas y apunta el total en su hueco del camion
    static void tarea (int[] camion, int ladoCamion, int cajas) {
        System.out.println("Inicio del Trabajador" + Thread.currentThread().getName() + " PID = "
                + ProcessHandle.current().pid());
        // Acumulador local: cada hilo tiene el suyo, por eso no se pisan
        int suma = 0;
        // Cada vuelta es una caja embalada
        for (int i = 0; i < cajas; i++) {
            suma += NUM_TORNILLOS_CAJA;
        }

        try {
            // La pausa es solo para observar los procesos con pgrep
            Thread.sleep(10000);
            // Si interrumpen al hilo durante la pausa, no llega a apuntar nada
            camion[ladoCamion] = suma;
        }catch (InterruptedException e) {
            // Java borra la marca de interrupcion al lanzar la excepcion, se restaura
            Thread.currentThread().interrupt();
            System.err.println("Trabajo interrumpido.");
        }
    }
}