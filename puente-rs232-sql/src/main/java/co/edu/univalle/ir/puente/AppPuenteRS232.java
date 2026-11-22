package co.edu.univalle.ir.puente;

import co.edu.univalle.ir.conexion.ConexionDB;
import co.edu.univalle.ir.puente.serial.SerialBridgeManager;
import co.edu.univalle.ir.puente.sync.ReceptorSerialADatabase;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Aplicativo principal del programa puente en Java de interfaz RS232 a SQL
 * (HU-06 - Entregables 3.d, 3.g y 3.h).
 * <p>
 * Abre la conexion JDBC mediante la API DAO (api-dao-lab3-1.0.jar), se conecta
 * al puerto serial COM2 (enlazado a COM1 de Proteus) y ejecuta continuamente la
 * rutina de la Tarea #28 que almacena las 8 entradas analogicas y 4 digitales
 * en la tabla <code>int_proceso_vars_data</code>.
 * </p>
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.1 $ 2026/11/21
 */
public class AppPuenteRS232 {

    /**
     * Puerto virtual por defecto donde escucha Java (enlazado a COM1 de
     * Proteus).
     */
    public static final String PUERTO_SERIAL_DEFECTO = "COM2";

    /**
     * Velocidad serial estandarizada con el firmware del microcontrolador.
     */
    public static final int BAUD_RATE_DEFECTO = 9600;

    /**
     * Ruta del archivo de configuracion de conexion con MySQL.
     */
    public static final String ARCHIVO_CONFIG_DB = "db_config.txt";

    /**
     * Metodo principal del puente RS232 a SQL.
     *
     * @param args Opcional: args[0] puede indicar otro puerto COM (ej. COM2).
     */
    public static void main(String[] args) {
        try {
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
            System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
        } catch (Exception ignored) {
        }

        String puertoObjetivo = (args != null && args.length > 0 && !args[0].isBlank())
                ? args[0].trim().toUpperCase()
                : PUERTO_SERIAL_DEFECTO;

        System.out.println("=========================================================================");
        System.out.println("  PUENTE RS232 -> SQL | LABORATORIO VIRTUAL (HU-06 / TAREA #28)          ");
        System.out.println("=========================================================================");

        ConexionDB conexionDB = null;
        SerialBridgeManager serialManager = new SerialBridgeManager();
        ReceptorSerialADatabase receptor = null;

        try {
            // 1. Conectar a MySQL mediante el archivo de configuracion de la API DAO
            conexionDB = new ConexionDB(new File(ARCHIVO_CONFIG_DB));
            System.out.println("[1] ConexionDB activa con: " + conexionDB.getBaseDatos());

            // 2. Abrir puerto serial RS232 (COM2 por defecto) a 9600 bps, 8-N-1
            serialManager.setModoAdc10Bits(false); // Coincide con #define ADC_10_BITS 0 en Arduino
            serialManager.conectar(puertoObjetivo, BAUD_RATE_DEFECTO);

            // 3. Arrancar rutina Tarea #28 (8 entradas analogicas + 4 digitales -> int_proceso_vars_data)
            receptor = new ReceptorSerialADatabase(serialManager, conexionDB);
            receptor.iniciar();

            System.out.println("\n>>> Escuchando tramas del sistema embebido en " + puertoObjetivo + "...");
            System.out.println(">>> Presione [ENTER] en esta consola para detener el puente de forma segura.\n");

            // Cierre limpio si el proceso se detiene desde NetBeans (boton Stop)
            final ReceptorSerialADatabase receptorRef = receptor;
            final ConexionDB conexionRef = conexionDB;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                receptorRef.detener();
                serialManager.desconectar();
                try {
                    conexionRef.cerrarConexion();
                } catch (Exception ignored) {
                }
            }));

            BufferedReader consola = new BufferedReader(new InputStreamReader(System.in));
            consola.readLine();

        } catch (Exception e) {
            System.err.println("[ERROR PUENTE] " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (receptor != null) {
                receptor.detener();
            }
            serialManager.desconectar();
            if (conexionDB != null) {
                try {
                    conexionDB.cerrarConexion();
                    System.out.println("[OK DB] Conexion MySQL cerrada correctamente.");
                } catch (Exception ignored) {
                }
            }
            System.out.println("=========================================================================");
        }
    }
}
