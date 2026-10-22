import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PracticaUD1 {
    public static void main(String[] args) {

    }

    static ProcessBuilder prepararEncargado(String... argumentos){
        String ejecutable = Path.of(
                System.getProperty("java.home"), "bin",
                System.getProperty("os.name").startsWith("windows") ? "java.exe" : "java"
        ).toString();

        List<String> orden = new ArrayList<>();
        orden.add(ejecutable);
        orden.add("-cp");
        orden.add(System.getProperty("java.class.path"));
        orden.add(PracticaUD1.class.getName());
        orden.add("encargado");
        Collections.addAll(orden, argumentos);

        return new ProcessBuilder(orden);
    }

}
