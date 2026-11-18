package co.edu.univalle.ir.conexion;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Clase utilitaria para ejecutar el comando
 * <code>javadoc @options @packages</code> (Tarea #23 - HU-05) directamente
 * desde el entorno de desarrollo y verificar la generacion del sitio web HTML
 * del Entregable 2.
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/17
 */
public class GeneradorJavaDoc {

    /**
     * Metodo principal que invoca la herramienta javadoc del JDK con los
     * archivos @options y @packages.
     *
     * @param args Argumentos de linea de comandos.
     */
    public static void main(String[] args) {
        System.out.println("=========================================================================");
        System.out.println("  TAREA #23 (HU-05): EJECUCION DE javadoc @options @packages (Lab 3)     ");
        System.out.println("=========================================================================");

        try {
            File dirProyecto = new File(System.getProperty("user.dir"));
            File archivoOptions = new File(dirProyecto, "options");
            File archivoPackages = new File(dirProyecto, "packages");

            if (!archivoOptions.exists() || !archivoPackages.exists()) {
                System.err.println("[ERROR] No se encontraron los archivos 'options' o 'packages' en: "
                        + dirProyecto.getAbsolutePath());
                return;
            }

            // Localizar el ejecutable javadoc dentro del JAVA_HOME actual (JDK 22)
            String javaHome = System.getProperty("java.home");
            File javadocExe = new File(javaHome, "bin" + File.separator + "javadoc.exe");
            String comandoJavadoc = javadocExe.exists() ? javadocExe.getAbsolutePath() : "javadoc";

            System.out.println("[1] Directorio de trabajo : " + dirProyecto.getAbsolutePath());
            System.out.println("[2] Ejecutando comando    : " + comandoJavadoc + " @options @packages\n");

            ProcessBuilder pb = new ProcessBuilder(comandoJavadoc, "@options", "@packages");
            pb.directory(dirProyecto);
            pb.redirectErrorStream(true);

            Process proceso = pb.start();
            try (BufferedReader lector = new BufferedReader(
                    new InputStreamReader(proceso.getInputStream(), StandardCharsets.UTF_8))) {
                String linea;
                while ((linea = lector.readLine()) != null) {
                    System.out.println("    " + linea);
                }
            }

            int codigoSalida = proceso.waitFor();
            File indexHtml = new File(dirProyecto, "doc" + File.separator + "index.html");

            if (codigoSalida == 0 && indexHtml.exists()) {
                System.out.println("\n[3] -> [OK] Documentacion JavaDoc generada exitosamente en:");
                System.out.println("       " + indexHtml.getAbsolutePath());
                System.out.println("=========================================================================");
                System.out.println("  [OK] TAREA #23 (HU-05) COMPLETADA EXITOSAMENTE                         ");
                System.out.println("=========================================================================");
            } else {
                System.err.println("\n[FALLO] javadoc finalizo con codigo: " + codigoSalida);
            }

        } catch (Exception e) {
            System.err.println("[ERROR] Fallo al ejecutar javadoc: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
