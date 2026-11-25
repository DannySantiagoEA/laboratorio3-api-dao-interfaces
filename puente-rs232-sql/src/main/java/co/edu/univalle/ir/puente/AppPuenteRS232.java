package co.edu.univalle.ir.puente;

import co.edu.univalle.ir.conexion.ConexionDB;
import co.edu.univalle.ir.puente.serial.SerialBridgeManager;
import co.edu.univalle.ir.puente.sync.DespachadorDatabaseASerial;
import co.edu.univalle.ir.puente.sync.ReceptorSerialADatabase;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Aplicativo principal full-duplex del programa puente RS232 <-> SQL (HU-06):
 * <ul>
 * <li>Tarea #28: recibe 8 ADC + 4 DIG por COM2 y los almacena en
 * <code>int_proceso_vars_data</code>.</li>
 * <li>Tarea #29: consulta <code>int_proceso</code> (Ts) e
 * <code>int_proceso_refs_data</code> (4 salidas) y despacha cambios por COM2
 * hacia el sistema embebido.</li>
 * </ul>
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.2 $ 2026/11/24
 */
public class AppPuenteRS232 {

    public static final String PUERTO_SERIAL_DEFECTO = "COM2";
    public static final int BAUD_RATE_DEFECTO = 9600;
    public static final String ARCHIVO_CONFIG_DB = "db_config.txt";

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
        System.out.println("  PUENTE FULL-DUPLEX RS232 <-> SQL | LABORATORIO VIRTUAL (HU-06)         ");
        System.out.println("=========================================================================");

        ConexionDB conexionRx = null;
        SerialBridgeManager serialManager = new SerialBridgeManager();
        ReceptorSerialADatabase receptor = null;
        DespachadorDatabaseASerial despachador = null;

        try {
            // 1. Conexiones JDBC independientes (una para RX #28 y otra exclusiva para TX #29)
            File archivoConfig = new File(ARCHIVO_CONFIG_DB);
            conexionRx = new ConexionDB(archivoConfig);
            ConexionDB conexionTx = new ConexionDB(archivoConfig);
            System.out.println("[1] Conexiones DB activas con: " + conexionRx.getBaseDatos());

            // 2. Abrir puerto serial RS232 (COM2 por defecto)
            serialManager.setModoAdc10Bits(false);
            serialManager.conectar(puertoObjetivo, BAUD_RATE_DEFECTO);

            // 3. Arrancar ambos sentidos del puente: RX (#28) y TX (#29)
            receptor = new ReceptorSerialADatabase(serialManager, conexionRx);
            receptor.iniciar();

            despachador = new DespachadorDatabaseASerial(serialManager, conexionTx);
            despachador.iniciar();

            System.out.println("\n>>> Puente activo en " + puertoObjetivo + " (RX -> vars_data | proceso/refs_data -> TX).");
            System.out.println(">>> Presione [ENTER] en esta consola para detener el puente de forma segura.\n");

            final ReceptorSerialADatabase receptorRef = receptor;
            final DespachadorDatabaseASerial despachadorRef = despachador;
            final ConexionDB conexionRxRef = conexionRx;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                despachadorRef.detener();
                receptorRef.detener();
                serialManager.desconectar();
                try {
                    conexionRxRef.cerrarConexion();
                } catch (Exception ignored) {
                }
            }));

            BufferedReader consola = new BufferedReader(new InputStreamReader(System.in));
            consola.readLine();

        } catch (Exception e) {
            System.err.println("[ERROR PUENTE] " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (despachador != null) {
                despachador.detener();
            }
            if (receptor != null) {
                receptor.detener();
            }
            serialManager.desconectar();
            if (conexionRx != null) {
                try {
                    conexionRx.cerrarConexion();
                    System.out.println("[OK DB] Conexion MySQL RX cerrada correctamente.");
                } catch (Exception ignored) {
                }
            }
            System.out.println("=========================================================================");
        }
    }
}
