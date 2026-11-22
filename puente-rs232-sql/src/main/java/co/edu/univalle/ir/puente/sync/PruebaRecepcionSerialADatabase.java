package co.edu.univalle.ir.puente.sync;

import co.edu.univalle.ir.conexion.ConexionDB;
import co.edu.univalle.ir.dao.int_proceso_vars_dataDAO;
import co.edu.univalle.ir.dao.int_proceso_vars_dataDAOImp;
import co.edu.univalle.ir.entity.int_proceso_vars_data;
import co.edu.univalle.ir.puente.serial.SerialBridgeManager;
import com.fazecast.jSerialComm.SerialPort;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Prueba integral de la Tarea #28 (HU-06): Emite 5 tramas binarias reales
 * [0x7A, 0x7B, DIG, 8_ADC, 0x7C] por COM1 (simulando el sistema embebido), las
 * recibe en COM2 con ReceptorSerialADatabase y comprueba en MySQL
 * (int_proceso_vars_data) el almacenamiento exacto de las 8 entradas analogicas
 * (IDs 1..8) y 4 entradas digitales (IDs 9..12).
 *
 * @author Danny Santiago Erazo Agredo
 * @version $Revision: 1.0 $ 2026/11/21
 */
public class PruebaRecepcionSerialADatabase {

    private static final String PUERTO_EMISOR_SIMULADO = "COM1";
    private static final String PUERTO_RECEPTOR_PUENTE = "COM2";

    /**
     * Construye una trama de 12 bytes identica a la funcion construirTrama()
     * del firmware codigo_arduino.ino (ADC_10_BITS = 0).
     */
    private static byte[] construirTramaArduino8Bits(byte byteDigital, int[] cuentas8Bits) {
        byte[] trama = new byte[12];
        trama[0] = SerialBridgeManager.HEADER_BYTE_1; // 0x7A
        trama[1] = SerialBridgeManager.HEADER_BYTE_2; // 0x7B
        trama[2] = byteDigital;                       // b0..b3 = D1..D4
        for (int i = 0; i < 8; i++) {
            trama[3 + i] = (byte) (cuentas8Bits[i] & 0xFF);
        }
        trama[11] = SerialBridgeManager.TAIL_BYTE;    // 0x7C
        return trama;
    }

    public static void main(String[] args) {
        try {
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
            System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
        } catch (Exception ignored) {
        }

        System.out.println("=========================================================================");
        System.out.println("  PRUEBA TAREA #28 (HU-06): RECEPCION RS232 -> int_proceso_vars_data     ");
        System.out.println("=========================================================================");

        ConexionDB conexion = null;
        SerialBridgeManager receptorSerial = new SerialBridgeManager();
        ReceptorSerialADatabase receptorDB = null;
        SerialPort emisorCOM1 = null;

        try {
            // 1. Conectar a MySQL y limpiar tabla historica para iniciar prueba limpia
            conexion = new ConexionDB(new File("db_config.txt"));
            int_proceso_vars_dataDAO varsDataDAO = new int_proceso_vars_dataDAOImp(conexion);
            varsDataDAO.borrarTodos();
            System.out.println("[1] ConexionDB activa y tabla int_proceso_vars_data preparada.");

            // 2. Abrir COM2 (receptor del puente) e iniciar ReceptorSerialADatabase
            receptorSerial.setModoAdc10Bits(false);
            receptorSerial.conectar(PUERTO_RECEPTOR_PUENTE, 9600);
            receptorDB = new ReceptorSerialADatabase(receptorSerial, varsDataDAO, 1);
            receptorDB.iniciar();

            // 3. Abrir COM1 (emulando el microcontrolador de Proteus)
            emisorCOM1 = SerialPort.getCommPort(PUERTO_EMISOR_SIMULADO);
            emisorCOM1.setComPortParameters(9600, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
            if (!emisorCOM1.openPort()) {
                throw new IllegalStateException("No se pudo abrir COM1 (verifique que Proteus este cerrado durante esta prueba).");
            }
            System.out.println("[2] Emulador de microcontrolador conectado en " + PUERTO_EMISOR_SIMULADO + ".");

            // Inyectar 3 bytes de ruido aleatorio para probar resincronizacion del parser
            byte[] ruido = new byte[]{(byte) 0xFF, (byte) 0x12, (byte) 0x7A};
            emisorCOM1.writeBytes(ruido, ruido.length);

            // 4. Transmitir 5 tramas reales de 12 bytes separadas por 100 ms (Ts = 100 ms)
            int totalTramasAEnviar = 5;
            System.out.println("\n[3] Transmitiendo " + totalTramasAEnviar + " tramas RS232 desde COM1 hacia COM2...\n");

            for (int k = 1; k <= totalTramasAEnviar; k++) {
                // Alternar patron digital: en la ultima trama (k=5) queda 0b1011 -> D1=1, D2=1, D3=0, D4=1
                byte dig = (k == totalTramasAEnviar) ? (byte) 0x0B : (byte) (k & 0x0F);
                int[] adc = new int[]{
                    (k * 51) % 256, // CH1: en k=5 -> 255 (5.00 V)
                    128, // CH2: ~2.51 V
                    64, // CH3: ~1.25 V
                    191, // CH4: ~3.75 V
                    0, // CH5: 0.00 V
                    255, // CH6: 5.00 V
                    102, // CH7: 2.00 V
                    153 // CH8: 3.00 V
                };
                if (k == totalTramasAEnviar) {
                    adc[0] = 255; // CH1 = 5.00 V exacto en la trama final
                }

                byte[] trama = construirTramaArduino8Bits(dig, adc);
                emisorCOM1.writeBytes(trama, trama.length);
                Thread.sleep(120);
            }

            // Esperar a que el hilo de persistencia termine de insertar la ultima trama en MySQL
            Thread.sleep(500);
            receptorDB.detener();

            // 5. Verificar directamente en MySQL mediante el DAO
            System.out.println("\n[4] Verificando registros almacenados en MySQL (int_proceso_vars_data)...");
            List<int_proceso_vars_data> todos = varsDataDAO.seleccionarTodos();
            int_proceso_vars_data ultimoCH1 = varsDataDAO.seleccionarUltimoDatoPorVariable(1);
            int_proceso_vars_data ultimoCH5 = varsDataDAO.seleccionarUltimoDatoPorVariable(5);
            int_proceso_vars_data ultimoD1 = varsDataDAO.seleccionarUltimoDatoPorVariable(9);
            int_proceso_vars_data ultimoD3 = varsDataDAO.seleccionarUltimoDatoPorVariable(11);
            int_proceso_vars_data ultimoD4 = varsDataDAO.seleccionarUltimoDatoPorVariable(12);

            System.out.println("    -> Total filas en int_proceso_vars_data (esperado 60): " + todos.size());
            System.out.println("    -> Ultima muestra CH1 (var_id=1, esperado 5.0V)  : " + ultimoCH1);
            System.out.println("    -> Ultima muestra CH5 (var_id=5, esperado 0.0V)  : " + ultimoCH5);
            System.out.println("    -> Ultima muestra D1  (var_id=9, esperado 1.0)   : " + ultimoD1);
            System.out.println("    -> Ultima muestra D3  (var_id=11, esperado 0.0)  : " + ultimoD3);
            System.out.println("    -> Ultima muestra D4  (var_id=12, esperado 1.0)  : " + ultimoD4);
            System.out.println("    -> Bytes de ruido descartados por el parser      : "
                    + receptorSerial.getBytesDescartadosPorRuido());

            boolean verificacionOk = (todos.size() == totalTramasAEnviar * 12)
                    && ultimoCH1 != null && Math.abs(ultimoCH1.getValor() - 5.0f) < 0.01f
                    && ultimoCH5 != null && Math.abs(ultimoCH5.getValor() - 0.0f) < 0.01f
                    && ultimoD1 != null && ultimoD1.getValor() == 1.0f
                    && ultimoD3 != null && ultimoD3.getValor() == 0.0f
                    && ultimoD4 != null && ultimoD4.getValor() == 1.0f;

            System.out.println("\n=========================================================================");
            if (verificacionOk) {
                System.out.println("  [OK] TAREA #28 VERIFICADA: 60 muestras (8 ADC + 4 DIG x 5) en MySQL    ");
            } else {
                System.out.println("  [FALLO] Alguna verificacion en MySQL no coincidio con lo esperado      ");
            }
            System.out.println("=========================================================================");

        } catch (Exception e) {
            System.err.println("[ERROR PRUEBA #28] " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (emisorCOM1 != null && emisorCOM1.isOpen()) {
                emisorCOM1.closePort();
            }
            if (receptorDB != null) {
                receptorDB.detener();
            }
            receptorSerial.desconectar();
            if (conexion != null) {
                try {
                    conexion.cerrarConexion();
                } catch (Exception ignored) {
                }
            }
        }
    }
}
