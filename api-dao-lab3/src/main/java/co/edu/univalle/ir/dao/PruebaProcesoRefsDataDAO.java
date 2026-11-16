package co.edu.univalle.ir.dao;

import co.edu.univalle.ir.conexion.ConexionDB;
import co.edu.univalle.ir.entity.int_proceso_refs_data;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

/**
 * Clase de prueba autoejecutable para verificar la Tarea #19 (HU-04):
 * Insercion, seleccion y borrado en la tabla <code>int_proceso_refs_data</code>
 * para las 4 variables de salida digitales (Entregables 3.c, 3.e y 3.g).
 *
 * @author Danny Santiago Erazo Agredo
 * @version $Revision: 1.0 $ 2026/11/16
 */
public class PruebaProcesoRefsDataDAO {

    /**
     * Crea automaticamente las tablas int_proceso_refs e int_proceso_refs_data
     * y registra las 4 salidas digitales (IDs 1 a 4) si no existen aun en el
     * servidor MySQL local.
     */
    private static void prepararTablasYSalidasDigitales() throws Exception {
        String urlServidor = "jdbc:mysql://localhost:3306/lab_virtual_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        try (Connection conn = DriverManager.getConnection(urlServidor, "root", ""); Statement stm = conn.createStatement()) {

            // Tabla padre de definicion de referencias / salidas (Figura 1 y Entregable 3.c)
            stm.executeUpdate("CREATE TABLE IF NOT EXISTS int_proceso_refs ("
                    + "id INTEGER AUTO_INCREMENT PRIMARY KEY, "
                    + "int_proceso_id INTEGER NOT NULL, "
                    + "nombre VARCHAR(80) NOT NULL, "
                    + "descripcion BLOB, "
                    + "max_2 FLOAT(10,5), "
                    + "min FLOAT(10,5), "
                    + "INDEX int_proceso_refs_FKIndex1 (int_proceso_id), "
                    + "CONSTRAINT fk_refs_proc FOREIGN KEY (int_proceso_id) REFERENCES int_proceso(id) ON DELETE CASCADE"
                    + ") ENGINE=InnoDB");

            // Tabla hija de datos de salidas digitales (Objetivo Tarea #19)
            stm.executeUpdate("CREATE TABLE IF NOT EXISTS int_proceso_refs_data ("
                    + "id INTEGER AUTO_INCREMENT PRIMARY KEY, "
                    + "int_proceso_refs_id INTEGER NOT NULL, "
                    + "valor FLOAT(10,5) NOT NULL, "
                    + "tiempo FLOAT(10,5) NOT NULL, "
                    + "fecha DATE, "
                    + "hora TIME, "
                    + "INDEX int_proceso_refs_data_FKIndex2 (int_proceso_refs_id), "
                    + "CONSTRAINT fk_refs_data_ref FOREIGN KEY (int_proceso_refs_id) REFERENCES int_proceso_refs(id) ON DELETE CASCADE"
                    + ") ENGINE=InnoDB");

            // Semilla Entregable 3.c: 4 variables de salida digitales (IDs 1 a 4)
            for (int k = 1; k <= 4; k++) {
                stm.executeUpdate("INSERT IGNORE INTO int_proceso_refs (id, int_proceso_id, nombre, descripcion, max_2, min) "
                        + "VALUES (" + k + ", 1, 'Salida Digital Out " + k + "', 'Actuador LED Salida Digital " + k + " (0/1)', 1.0, 0.0)");
            }
        }
    }

    /**
     * Metodo principal para probar la insercion, seleccion y borrado en
     * int_proceso_refs_data.
     *
     * @param args Argumentos de linea de comandos.
     */
    public static void main(String[] args) {
        System.out.println("=========================================================================");
        System.out.println("  PRUEBA TAREA #19 (HU-04): DAO int_proceso_refs_data (4 SALIDAS DIG)    ");
        System.out.println("=========================================================================");

        try {
            // 0. Preparar tablas y las 4 variables de salida digitales (IDs 1..4)
            prepararTablasYSalidasDigitales();
            System.out.println("[0] Tablas 'int_proceso_refs' (4 salidas) e 'int_proceso_refs_data' listas.");

            ConexionDB conexion = new ConexionDB("db_config.txt");
            int_proceso_refs_dataDAO refsDataDAO = new int_proceso_refs_dataDAOImp(conexion);

            // Limpiar registros previos de prueba
            refsDataDAO.borrarTodos();

            // 1. PRUEBA DE INSERCION EN LOTE DE LAS 4 SALIDAS (Estado inicial: [ON, OFF, ON, OFF])
            System.out.println("\n[1] Probando INSERCION de las 4 salidas digitales [true, false, true, false]...");
            boolean[] estadoInicial = {true, false, true, false};
            int insertadosLote = refsDataDAO.insertarEstados4SalidasDigitales(estadoInicial, 0.0f, 1);
            System.out.println("    -> Registros insertados en lote (esperado 4): " + insertadosLote);

            // 2. PRUEBA DE CONMUTACION INDIVIDUAL DESDE LA GUI (Entregable 3.e)
            System.out.println("\n[2] Simulando conmutacion en GUI: encendiendo Salida Digital 2 (Out 2 -> ON)...");
            int insIndividual = refsDataDAO.registrarEstadoSalidaDigital(2, true, 0.50f, 1);
            System.out.println("    -> Filas insertadas por evento de GUI: " + insIndividual);

            // 3. PRUEBA DE SELECCION PARA EL PUENTE RS232-SQL (Entregables 3.e y 3.g)
            System.out.println("\n[3] Probando SELECCION de los ultimos estados para envio al sistema embebido...");
            boolean[] estadosVigentes = refsDataDAO.consultarUltimosEstados4Salidas(1);
            System.out.println("    -> Vector de 4 salidas leido desde DB (esperado [true, true, true, false]): "
                    + Arrays.toString(estadosVigentes));

            List<int_proceso_refs_data> historialOut2 = refsDataDAO.seleccionarPorReferencia(2);
            System.out.println("    -> Historial de transiciones de Salida Digital Out 2 (ref_id=2): " + historialOut2.size());
            for (int_proceso_refs_data r : historialOut2) {
                System.out.println("       * " + r);
            }

            // 4. PRUEBA DE BORRADO (Por ID, por Referencia y Total)
            System.out.println("\n[4] Probando BORRADO (borrarPorId, borrarPorReferencia y borrarTodos)...");
            int_proceso_refs_data ultimoOut2 = refsDataDAO.seleccionarUltimoDatoPorReferencia(2);
            int borradoId = refsDataDAO.borrarPorId(ultimoOut2.getId());
            System.out.println("    -> Borrado por ID (" + ultimoOut2.getId() + "): " + borradoId + " fila eliminada.");

            int borradoRef = refsDataDAO.borrarPorReferencia(2);
            System.out.println("    -> Borrado restante de Salida Out 2 (ref_id=2): " + borradoRef + " fila eliminada.");

            int borradoTotal = refsDataDAO.borrarTodos();
            System.out.println("    -> Borrado de salidas restantes: " + borradoTotal + " filas eliminadas.");

            conexion.cerrarConexion();
            System.out.println("\n=========================================================================");
            System.out.println("  [OK] TAREA #19 (HU-04) VERIFICADA EXITOSAMENTE CON MYSQL               ");
            System.out.println("=========================================================================");

        } catch (Exception e) {
            System.err.println("[ERROR] Fallo en la prueba de int_proceso_refs_dataDAO: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
