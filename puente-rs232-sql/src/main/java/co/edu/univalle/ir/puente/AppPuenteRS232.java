package co.edu.univalle.ir.puente;

import co.edu.univalle.ir.conexion.ConexionDB;
import co.edu.univalle.ir.dao.int_procesoDAO;
import co.edu.univalle.ir.dao.int_procesoDAOImp;
import co.edu.univalle.ir.dao.int_proceso_refs_dataDAO;
import co.edu.univalle.ir.dao.int_proceso_refs_dataDAOImp;
import co.edu.univalle.ir.dao.int_proceso_vars_dataDAO;
import co.edu.univalle.ir.dao.int_proceso_vars_dataDAOImp;
import co.edu.univalle.ir.entity.int_proceso_vars_data;
import com.fazecast.jSerialComm.SerialPort;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Punto de entrada del aplicativo puente en Java de interfaz RS232 a SQL (HU-06
 * - Entregables 3.g y 3.h).
 * <p>
 * En la Tarea #27 verifica la correcta importacion y operatividad conjunta del
 * archivo JAR de la API DAO (api-dao-lab3-1.0.jar) y la libreria serial
 * jSerialComm (2.10.4).
 * </p>
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/20
 */
public class AppPuenteRS232 {

    /**
     * Puerto virtual por defecto donde escucha Java (enlazado a COM1 de
     * Proteus).
     */
    public static final String PUERTO_SERIAL_DEFECTO = "COM2";

    /**
     * Identificador del proceso principal registrado en la tabla int_proceso.
     */
    public static final int ID_PROCESO_DEFECTO = 1;

    /**
     * Ruta del archivo de configuracion de conexion con MySQL.
     */
    public static final String ARCHIVO_CONFIG_DB = "db_config.txt";

    /**
     * Metodo principal de verificacion de dependencias (API DAO + jSerialComm)
     * y arranque del puente RS232-SQL.
     *
     * @param args Argumentos de linea de comandos.
     */
    public static void main(String[] args) {
        try {
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
            System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
        } catch (Exception ignored) {
        }

        System.out.println("=========================================================================");
        System.out.println("  TAREA #27 (HU-06): VERIFICACION PROYECTO PUENTE RS232-SQL (Lab 3)      ");
        System.out.println("=========================================================================");

        boolean serialOk = verificarLibreriaSerial();
        boolean apiDaoOk = verificarApiDaoJar();

        System.out.println("\n=========================================================================");
        if (serialOk && apiDaoOk) {
            System.out.println("  [OK] TAREA #27 COMPLETADA: jSerialComm y API DAO (.jar) operativos     ");
        } else {
            System.out.println("  [AVISO] Revise los mensajes anteriores antes de continuar a Tarea #28  ");
        }
        System.out.println("=========================================================================");
    }

    /**
     * Verifica la importacion de jSerialComm listando los puertos COM
     * detectados en el sistema operativo.
     *
     * @return true si la API jSerialComm responde correctamente.
     */
    private static boolean verificarLibreriaSerial() {
        System.out.println("\n[1] Verificando libreria serial (jSerialComm 2.10.4)...");
        try {
            SerialPort[] puertos = SerialPort.getCommPorts();
            System.out.println("    -> [OK] Libreria jSerialComm importada correctamente.");
            System.out.println("    -> Puertos seriales detectados en el sistema: " + puertos.length);

            boolean com2Detectado = false;
            for (SerialPort puerto : puertos) {
                String nombre = puerto.getSystemPortName();
                System.out.println("       * " + nombre + " - " + puerto.getDescriptivePortName());
                if (PUERTO_SERIAL_DEFECTO.equalsIgnoreCase(nombre)) {
                    com2Detectado = true;
                }
            }

            if (com2Detectado) {
                System.out.println("    -> [OK] Puerto virtual objetivo '" + PUERTO_SERIAL_DEFECTO
                        + "' detectado y listo para enlace con Proteus (COM1).");
            } else {
                System.out.println("    -> [INFO] No se detecto '" + PUERTO_SERIAL_DEFECTO
                        + "' activo en este momento (recuerde iniciar el puente COM1 <-> COM2 en HHD al probar con Proteus).");
            }
            return true;
        } catch (Throwable e) {
            System.err.println("    -> [FALLO] Error al cargar jSerialComm: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verifica la importacion del archivo JAR de la API DAO estableciendo
     * conexion con MySQL mediante ConexionDB e instanciando los DAOs que usara
     * el puente RS232-SQL.
     *
     * @return true si la conexion y las consultas a traves de los DAOs son
     * exitosas.
     */
    private static boolean verificarApiDaoJar() {
        System.out.println("\n[2] Verificando API DAO empaquetada en JAR (api-dao-lab3-1.0.jar)...");
        ConexionDB conexion = null;
        try {
            File archivoConfig = new File(ARCHIVO_CONFIG_DB);
            conexion = new ConexionDB(archivoConfig);

            if (!conexion.isConectado()) {
                System.err.println("    -> [FALLO] ConexionDB no pudo abrir enlace activo con MySQL.");
                return false;
            }
            System.out.println("    -> [OK] ConexionDB operativa con base de datos: " + conexion.getBaseDatos());

            // Instanciar los 3 DAOs que utilizara el puente RS232-SQL (Entregables 3.d, 3.e, 3.f, 3.g)
            int_procesoDAO procesoDAO = new int_procesoDAOImp(conexion);
            int_proceso_vars_dataDAO varsDataDAO = new int_proceso_vars_dataDAOImp(conexion);
            int_proceso_refs_dataDAO refsDataDAO = new int_proceso_refs_dataDAOImp(conexion);

            // Consulta de prueba 1: Tiempo de muestreo desde int_proceso
            float tsActual = procesoDAO.consultarTiempoMuestreo(ID_PROCESO_DEFECTO);
            System.out.println("    -> [OK] int_procesoDAO: tiempo_muestreo (ID="
                    + ID_PROCESO_DEFECTO + ") = " + tsActual + " ms");

            // Consulta de prueba 2: Ultimo estado de las 4 salidas digitales en int_proceso_refs_data
            boolean[] salidasDigitales = refsDataDAO.consultarUltimosEstados4Salidas(1);
            System.out.println("    -> [OK] int_proceso_refs_dataDAO: ultimas 4 salidas digitales = "
                    + Arrays.toString(salidasDigitales));

            // Consulta de prueba 3: Ultima muestra registrada en CH1 de int_proceso_vars_data
            int_proceso_vars_data ultimaCH1 = varsDataDAO.seleccionarUltimoDatoPorVariable(1);
            System.out.println("    -> [OK] int_proceso_vars_dataDAO: ultima muestra CH1 = "
                    + (ultimaCH1 != null ? ultimaCH1 : "sin muestras previas (tabla lista)"));

            return true;
        } catch (Exception e) {
            System.err.println("    -> [FALLO] Error usando la API DAO (.jar): " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (conexion != null) {
                try {
                    conexion.cerrarConexion();
                } catch (Exception ignored) {
                }
            }
        }
    }
}
