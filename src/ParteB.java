import java.util.Scanner;

public class ParteB {
    // Palabra que el coordinador pasa como primer argumento a cada trabajador.
    // Se usa en el if de main y al construir la orden, por eso esta en una constante
    static final String PALABRA = "trabajador";
    static final int MIN_TRABAJADORES = 2;
    static final int MAX_TRABAJADORES = 4;
    static final long MAX_LIMITE = 1000000;
    static final int MAX_ESPERA_SEG = 3; // Espera maxima por trabajador, en segundos

    public static void main(String[] args) {

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

        // El modo llega como argumento. Si no se indica, todos los trabajadores usan "ok"
        String modo = args.length == 0 ? "ok" : args[0];
    }

    // Trabajo de cada proceso trabajador
    static void ejecutarTrabajador(String modo){

    }
}
