package co.edu.univalle.ir.puente.sync;

import co.edu.univalle.ir.conexion.ConexionDB;
import co.edu.univalle.ir.dao.int_procesoDAO;
import co.edu.univalle.ir.dao.int_procesoDAOImp;
import co.edu.univalle.ir.dao.int_proceso_refs_dataDAO;
import co.edu.univalle.ir.dao.int_proceso_refs_dataDAOImp;
import co.edu.univalle.ir.puente.serial.SerialBridgeManager;
import com.fazecast.jSerialComm.SerialPort;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Prueba integral de la Tarea #29 (HU-06): Modifica tiempo_muestreo
 * (int_proceso) y las 4 salidas digitales (int_proceso_refs_data) en MySQL, y
 * verifica que DespachadorDatabaseASerial despache por COM2 -> COM1 las tramas
 * binarias [0xAA, 0x02, MSB, LSB, 0x55] y [0xAA, 0x01, MASCARA, 0x55].
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @version $Revision: 1.0 $ 2026/11/24
 */
public class PruebaDespachoDatabaseASerial {

    public static void main(String[] args) {
        try {
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
            System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
        } catch (Exception ignored) {
        }

        System.out.println("=========================================================================");
        System.out.println("  PRUEBA TAREA #29 (HU-06): DESPACHO DB (Ts + 4 SALIDAS) -> RS232        ");
        System.out.println("=========================================================================");

        ConexionDB connGuiSimulada = null;
        SerialBridgeManager serialCOM2 = new SerialBridgeManager();
        DespachadorDatabaseASerial despachador = null;
        SerialPort receptorCOM1 = null;

        try {
            File cfg = new File("db_config.txt");
            connGuiSimulada = new ConexionDB(cfg);
            int_procesoDAO procesoDAO = new int_procesoDAOImp(connGuiSimulada);
            int_proceso_refs_dataDAO refsDAO = new int_proceso_refs_dataDAOImp(connGuiSimulada);

            // Guardar Ts original para restaurarlo al terminar
            float tsOriginal = procesoDAO.consultarTiempoMuestreo(1);

            // 1. Abrir COM1 (simulando el microcontrolador que recibe comandos)
            receptorCOM1 = SerialPort.getCommPort("COM1");
            receptorCOM1.setComPortParameters(9600, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
            receptorCOM1.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
            if (!receptorCOM1.openPort()) {
                throw new IllegalStateException("No se pudo abrir COM1 (cierre Proteus durante la prueba).");
            }
            receptorCOM1.flushIOBuffers();

            // 2. Abrir COM2 e iniciar DespachadorDatabaseASerial
            serialCOM2.conectar("COM2", 9600);
            despachador = new DespachadorDatabaseASerial(serialCOM2, new ConexionDB(cfg), 1, 1, 120);
            despachador.iniciar();

            Thread.sleep(300); // Despacho inicial sincronizado
            receptorCOM1.flushIOBuffers();

            // 3. Simular cambios desde la GUI en MySQL: Ts = 250 ms y Salidas = [ON, OFF, ON, ON] (0x0D)
            System.out.println("\n[1] Simulando cambio en GUI: Ts = 250 ms y Salidas = [true, false, true, true]...");
            procesoDAO.actualizarTiempoMuestreo(1, 250.0f);
            refsDAO.insertarEstados4SalidasDigitales(new boolean[]{true, false, true, true}, 1.0f, 1);

            Thread.sleep(450);

            // 4. Leer bytes recibidos en COM1
            ByteArrayOutputStream recibidos = new ByteArrayOutputStream();
            int disponibles = receptorCOM1.bytesAvailable();
            if (disponibles > 0) {
                byte[] buf = new byte[disponibles];
                int n = receptorCOM1.readBytes(buf, buf.length);
                if (n > 0) {
                    recibidos.write(buf, 0, n);
                }
            }
            byte[] tramaRx = recibidos.toByteArray();

            StringBuilder hex = new StringBuilder();
            for (byte b : tramaRx) {
                hex.append(String.format("%02X ", b & 0xFF));
            }
            System.out.println("[2] Bytes recibidos en COM1 (" + tramaRx.length + " bytes): " + hex);

            // Buscar trama Ts=250 (0x00FA): AA 02 00 FA 55 y trama LEDs (0b1101 = 0x0D): AA 01 0D 55
            boolean okTs = contieneSecuencia(tramaRx, new byte[]{(byte) 0xAA, 0x02, 0x00, (byte) 0xFA, 0x55});
            boolean okLeds = contieneSecuencia(tramaRx, new byte[]{(byte) 0xAA, 0x01, 0x0D, 0x55});

            System.out.println("    -> Trama Ts=250ms  [AA 02 00 FA 55] detectada: " + okTs);
            System.out.println("    -> Trama LEDs 0x0D [AA 01 0D 55]    detectada: " + okLeds);

            // Restaurar Ts original
            if (tsOriginal > 0) {
                procesoDAO.actualizarTiempoMuestreo(1, tsOriginal);
            }

            System.out.println("\n=========================================================================");
            if (okTs && okLeds) {
                System.out.println("  [OK] TAREA #29 VERIFICADA: comandos Ts y 4 salidas despachados a COM1  ");
            } else {
                System.out.println("  [FALLO] Alguna trama esperada no llego a COM1                          ");
            }
            System.out.println("=========================================================================");

        } catch (Exception e) {
            System.err.println("[ERROR PRUEBA #29] " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (despachador != null) {
                despachador.detener();
            }
            serialCOM2.desconectar();
            if (receptorCOM1 != null && receptorCOM1.isOpen()) {
                receptorCOM1.closePort();
            }
            if (connGuiSimulada != null) {
                try {
                    connGuiSimulada.cerrarConexion();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private static boolean contieneSecuencia(byte[] fuente, byte[] objetivo) {
        if (fuente.length < objetivo.length) {
            return false;
        }
        for (int i = 0; i <= fuente.length - objetivo.length; i++) {
            boolean coincide = true;
            for (int j = 0; j < objetivo.length; j++) {
                if (fuente[i + j] != objetivo[j]) {
                    coincide = false;
                    break;
                }
            }
            if (coincide) {
                return true;
            }
        }
        return false;
    }
}
