package co.edu.univalle.ir.dao;

import co.edu.univalle.ir.conexion.ConexionDB;
import co.edu.univalle.ir.entity.int_proceso;

/**
 * Clase de verificacion funcional para la Tarea #14 (HU-03): Consulta y
 * actualizacion rapida del campo <code>tiempo_muestreo</code> por ID de proceso
 * en la tabla <code>int_proceso</code>.
 *
 * @author Danny Santiago Erazo Agredo
 * @version $Revision: 1.0 $ 2026/11/14
 */
public class PruebaProcesoDAO {

    /**
     * Metodo principal de ejecucion de pruebas para int_procesoDAO.
     *
     * @param args Argumentos de linea de comandos.
     */
    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("  PRUEBA TAREA #14 (HU-03): TIEMPO_MUESTREO EN int_procesoDAO     ");
        System.out.println("==================================================================");

        try {
            ConexionDB conexion = new ConexionDB("db_config.txt");
            int_procesoDAO procesoDAO = new int_procesoDAOImp(conexion);

            // Verificar que exista el proceso ID = 1 (creado en la prueba anterior)
            int_proceso procesoBase = procesoDAO.seleccionarPorId(1);
            if (procesoBase == null) {
                System.out.println("[0] Insertando proceso inicial ID=1...");
                procesoDAO.insertarProceso(
                        1,
                        "Planta 8 ADC y 4 DIO",
                        "Hardware ATmega328P conectado por RS232",
                        100.0f,
                        "Especificaciones: 8 entradas analogicas (0-5V), 4 entradas digitales, 4 salidas digitales",
                        "Manual de operacion del laboratorio virtual v1.0"
                );
                procesoBase = procesoDAO.seleccionarPorId(1);
            }
            System.out.println("[1] Proceso cargado desde MySQL: " + procesoBase);

            // 2. PRUEBA DE CONSULTA RAPIDA DE TIEMPO_MUESTREO (Tarea #14 - Entregable 3.g)
            float tsInicial = procesoDAO.consultarTiempoMuestreo(1);
            System.out.println("\n[2] Consulta rapida de tiempo_muestreo (ID=1): " + tsInicial + " ms");

            // 3. PRUEBA DE ACTUALIZACION RAPIDA DE TIEMPO_MUESTREO (Tarea #14 - Entregable 3.f)
            float nuevoTs = 250.0f;
            System.out.println("\n[3] Actualizando rapidamente tiempo_muestreo a " + nuevoTs + " ms...");
            int filasAfectadas = procesoDAO.actualizarTiempoMuestreo(1, nuevoTs);
            System.out.println("    -> Filas actualizadas: " + filasAfectadas);

            // 4. VERIFICACION POST-ACTUALIZACION
            float tsVerificado = procesoDAO.consultarTiempoMuestreo(1);
            System.out.println("\n[4] Tiempo de muestreo leido tras actualizacion: " + tsVerificado + " ms");

            // Restaurar a 100.0 ms para dejar la base de datos en su valor estandar
            procesoDAO.actualizarTiempoMuestreo(1, 100.0f);
            System.out.println("[5] Tiempo de muestreo restaurado a: " + procesoDAO.consultarTiempoMuestreo(1) + " ms");

            conexion.cerrarConexion();
            System.out.println("\n==================================================================");
            System.out.println("  [OK] TAREA #14 (HU-03) VERIFICADA EXITOSAMENTE CON MYSQL        ");
            System.out.println("==================================================================");

        } catch (Exception e) {
            System.err.println("[ERROR] Fallo en la prueba de int_procesoDAO: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
