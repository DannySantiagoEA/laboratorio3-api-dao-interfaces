package co.edu.univalle.ir.conexion;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Clase de verificacion funcional para la Tarea #5 (HU-01). Ejecuta pruebas de
 * conexion a MySQL con credenciales validas, con credenciales invalidas y con
 * el archivo de configuracion externo <code>db_config.txt</code>.
 * <p>
 * Cada prueba indica el resultado esperado (conecta o falla) y lo compara con
 * el obtenido. Al final se imprime un resumen de pruebas aprobadas.
 * </p>
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.1 $ 2026/11/15
 * @see co.edu.univalle.ir.conexion.ConexionDB
 */
public class PruebaConexion {

    // =========================================================================
    // PARAMETROS DE PRUEBA (deben coincidir con db_config.txt)
    // =========================================================================
    /**
     * Servidor MySQL de pruebas.
     */
    private static final String HOST = "localhost";

    /**
     * Puerto correcto del servidor MySQL.
     */
    private static final String PUERTO = "3306";

    /**
     * Puerto en el que no escucha MySQL (para la prueba de puerto invalido).
     */
    private static final String PUERTO_INVALIDO = "3399";

    /**
     * Base de datos del laboratorio virtual.
     */
    private static final String BASE_DATOS = "lab_virtual_db";

    /**
     * Usuario valido de MySQL.
     */
    private static final String USUARIO = "root";

    /**
     * Clave valida del usuario (vacia en XAMPP por defecto).
     */
    private static final String CLAVE = "";

    /**
     * Archivo de configuracion externo por defecto.
     */
    private static final String ARCHIVO_CONFIG = "db_config.txt";

    /**
     * Contador de pruebas ejecutadas.
     */
    private static int totalPruebas = 0;

    /**
     * Contador de pruebas cuyo resultado coincidio con el esperado.
     */
    private static int pruebasAprobadas = 0;

    /**
     * Accion que intenta abrir una conexion. Permite pasar cada constructor de
     * {@link ConexionDB} como una expresion lambda.
     */
    @FunctionalInterface
    private interface IntentoConexion {

        /**
         * Intenta crear la conexion.
         *
         * @return Objeto {@link ConexionDB} creado.
         * @exception Exception Cualquier error de lectura o de conexion.
         */
        ConexionDB abrir() throws Exception;
    }

    /**
     * Constructor privado: la clase solo se usa desde su metodo main.
     */
    private PruebaConexion() {
    }

    /**
     * Metodo principal de las pruebas de conexion.
     *
     * @param args Opcional: args[0] con la ruta de otro archivo de
     * configuracion.
     */
    public static void main(String[] args) {
        String rutaConfig = (args.length > 0) ? args[0] : ARCHIVO_CONFIG;

        System.out.println("==================================================================");
        System.out.println("  PRUEBA TAREA #5 (HU-01): CONEXION JDBC VALIDA, INVALIDA Y POR ARCHIVO");
        System.out.println("==================================================================");

        // ---------------------------------------------------------------------
        // A. CREDENCIALES VALIDAS
        // ---------------------------------------------------------------------
        System.out.println("\n--- A. Credenciales validas ---");
        probar("A1 Parametros completos (host, puerto, BD, usuario, clave)", true,
                () -> new ConexionDB(HOST, PUERTO, BASE_DATOS, USUARIO, CLAVE));

        probar("A2 Parametros abreviados (BD, usuario, clave)", true,
                () -> new ConexionDB(BASE_DATOS, USUARIO, CLAVE));

        // ---------------------------------------------------------------------
        // B. CREDENCIALES INVALIDAS
        // ---------------------------------------------------------------------
        System.out.println("\n--- B. Credenciales invalidas (deben fallar) ---");
        probar("B1 Clave incorrecta", false,
                () -> new ConexionDB(HOST, PUERTO, BASE_DATOS, USUARIO, "clave_incorrecta"));

        probar("B2 Usuario inexistente", false,
                () -> new ConexionDB(HOST, PUERTO, BASE_DATOS, "usuario_inexistente", "123"));

        probar("B3 Base de datos inexistente", false,
                () -> new ConexionDB(HOST, PUERTO, "bd_que_no_existe", USUARIO, CLAVE));

        probar("B4 Puerto donde no escucha MySQL (" + PUERTO_INVALIDO + ")", false,
                () -> new ConexionDB(HOST, PUERTO_INVALIDO, BASE_DATOS, USUARIO, CLAVE));

        // ---------------------------------------------------------------------
        // C. ARCHIVO DE CONFIGURACION EXTERNO
        // ---------------------------------------------------------------------
        System.out.println("\n--- C. Archivo de configuracion externo ---");
        File archivo = new File(rutaConfig);
        System.out.println("    Archivo usado: " + archivo.getAbsolutePath());

        probar("C1 Constructor con File (" + rutaConfig + ")", true,
                () -> new ConexionDB(archivo));

        probar("C2 Constructor con ruta String (" + rutaConfig + ")", true,
                () -> new ConexionDB(rutaConfig));

        probar("C3 Archivo que no existe (debe fallar)", false,
                () -> new ConexionDB("no_existe_config.txt"));

        probar("C4 Archivo sin la propiedad database= (debe fallar)", false,
                () -> new ConexionDB(crearArchivoTemporal(
                        "host=" + HOST + "\nport=" + PUERTO + "\nuser=" + USUARIO + "\npassword=" + CLAVE + "\n")));

        probar("C5 Archivo con clave incorrecta (debe fallar)", false,
                () -> new ConexionDB(crearArchivoTemporal(
                        "host=" + HOST + "\nport=" + PUERTO + "\ndatabase=" + BASE_DATOS
                        + "\nuser=" + USUARIO + "\npassword=clave_incorrecta\n")));

        // ---------------------------------------------------------------------
        // RESUMEN
        // ---------------------------------------------------------------------
        System.out.println("\n==================================================================");
        System.out.printf("  RESULTADO: %d de %d pruebas con el comportamiento esperado%n",
                pruebasAprobadas, totalPruebas);
        System.out.println("==================================================================");
    }

    /**
     * Ejecuta una prueba de conexion, compara el resultado con el esperado e
     * imprime una linea con el veredicto. La conexion se cierra siempre que
     * llegue a abrirse.
     *
     * @param descripcion String con el nombre de la prueba.
     * @param debeConectar boolean true si se espera conexion exitosa; false si
     * se espera un error.
     * @param intento Lambda que llama al constructor de {@link ConexionDB}.
     */
    private static void probar(String descripcion, boolean debeConectar, IntentoConexion intento) {
        totalPruebas++;
        boolean conecto = false;
        String detalle;

        try {
            ConexionDB conexion = intento.abrir();
            conecto = conexion.isConectado();
            detalle = conecto
                    ? "conecto a '" + conexion.getBaseDatos() + "' como " + conexion.getUsuario()
                    : "se creo el objeto pero la conexion no es valida";
            conexion.cerrarConexion();
        } catch (Exception e) {
            detalle = e.getClass().getSimpleName() + ": " + primeraLinea(e.getMessage());
        }

        boolean aprobada = (conecto == debeConectar);
        if (aprobada) {
            pruebasAprobadas++;
        }

        System.out.printf("[%s] %s%n", aprobada ? "OK   " : "FALLO", descripcion);
        System.out.printf("        esperado: %-8s obtenido: %s%n",
                debeConectar ? "conecta" : "error", detalle);
    }

    /**
     * Crea un archivo de configuracion temporal con el contenido indicado. Se
     * borra automaticamente al terminar el programa.
     *
     * @param contenido String con las lineas clave=valor.
     * @return Objeto File del archivo temporal.
     * @exception IOException Si no se puede crear o escribir el archivo.
     */
    private static File crearArchivoTemporal(String contenido) throws IOException {
        File temporal = File.createTempFile("db_config_prueba", ".txt");
        temporal.deleteOnExit();
        Files.writeString(temporal.toPath(), contenido, StandardCharsets.UTF_8);
        return temporal;
    }

    /**
     * Devuelve solo la primera linea de un mensaje de error (los mensajes de
     * JDBC suelen traer varias lineas).
     *
     * @param mensaje String con el mensaje completo.
     * @return String con la primera linea, o "(sin mensaje)" si es null.
     */
    private static String primeraLinea(String mensaje) {
        if (mensaje == null) {
            return "(sin mensaje)";
        }
        int salto = mensaje.indexOf('\n');
        return (salto >= 0) ? mensaje.substring(0, salto).trim() : mensaje.trim();
    }
}
