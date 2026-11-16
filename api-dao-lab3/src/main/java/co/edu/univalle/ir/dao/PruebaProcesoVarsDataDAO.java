package co.edu.univalle.ir.dao;

import co.edu.univalle.ir.conexion.ConexionDB;
import co.edu.univalle.ir.entity.int_proceso_vars_data;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.Time;
import java.util.List;

/**
 * Clase de prueba autoejecutable para verificar la Tarea #18 (HU-04):
 * Insercion, seleccion y borrado en la tabla <code>int_proceso_vars_data</code>
 * para las 8 entradas analogicas y 4 entradas digitales.
 *
 * @author Danny Santiago Erazo Agredo
 * @version $Revision: 1.0 $ 2026/11/15
 */
public class PruebaProcesoVarsDataDAO {

    /**
     * Crea automaticamente las tablas int_proceso_vars e int_proceso_vars_data
     * y registra las 12 variables de entrada (8 analogicas + 4 digitales) si no
     * existen aun en la base de datos local.
     */
    private static void prepararTablasYVariablesEntrada() throws Exception {
        String urlServidor = "jdbc:mysql://localhost:3306/lab_virtual_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        try (Connection conn = DriverManager.getConnection(urlServidor, "root", ""); Statement stm = conn.createStatement()) {

            // Tabla padre de definicion de variables de entrada (Figura 1 y Entregable 3.c)
            stm.executeUpdate("CREATE TABLE IF NOT EXISTS int_proceso_vars ("
                    + "id INTEGER AUTO_INCREMENT PRIMARY KEY, "
                    + "int_proceso_id INTEGER NOT NULL, "
                    + "nombre VARCHAR(80) NOT NULL, "
                    + "descripcion BLOB, "
                    + "max_2 FLOAT(10,5), "
                    + "min FLOAT(10,5), "
                    + "INDEX int_proceso_vars_FKIndex1 (int_proceso_id), "
                    + "CONSTRAINT fk_vars_proc FOREIGN KEY (int_proceso_id) REFERENCES int_proceso(id) ON DELETE CASCADE"
                    + ") ENGINE=InnoDB");

            // Tabla hija de datos de variables (Objetivo Tarea #18)
            stm.executeUpdate("CREATE TABLE IF NOT EXISTS int_proceso_vars_data ("
                    + "id INTEGER AUTO_INCREMENT PRIMARY KEY, "
                    + "int_proceso_vars_id INTEGER NOT NULL, "
                    + "valor FLOAT(10,5) NOT NULL, "
                    + "tiempo FLOAT(10,5) NOT NULL, "
                    + "fecha DATE, "
                    + "hora TIME, "
                    + "INDEX int_proceso_vars_data_FKIndex1 (int_proceso_vars_id), "
                    + "CONSTRAINT fk_vars_data_var FOREIGN KEY (int_proceso_vars_id) REFERENCES int_proceso_vars(id) ON DELETE CASCADE"
                    + ") ENGINE=InnoDB");

            // Semilla Entregable 3.c: 8 variables de entrada analogicas (IDs 1 a 8)
            for (int i = 1; i <= 8; i++) {
                stm.executeUpdate("INSERT IGNORE INTO int_proceso_vars (id, int_proceso_id, nombre, descripcion, max_2, min) "
                        + "VALUES (" + i + ", 1, 'Entrada Analogica CH" + i + "', 'Canal ADC " + i + " (0-5V)', 5.0, 0.0)");
            }

            // Semilla Entregable 3.c: 4 variables de entrada digitales (IDs 9 a 12)
            for (int j = 1; j <= 4; j++) {
                int idVarDigital = 8 + j;
                stm.executeUpdate("INSERT IGNORE INTO int_proceso_vars (id, int_proceso_id, nombre, descripcion, max_2, min) "
                        + "VALUES (" + idVarDigital + ", 1, 'Entrada Digital D" + j + "', 'Canal Digital Entrada " + j + " (0/1)', 1.0, 0.0)");
            }
        }
    }

    /**
     * Metodo principal para probar la insercion, seleccion y borrado en
     * int_proceso_vars_data.
     *
     * @param args Argumentos de linea de comandos.
     */
    public static void main(String[] args) {
        System.out.println("=========================================================================");
        System.out.println("  PRUEBA TAREA #18 (HU-04): DAO int_proceso_vars_data (8 ADC y 4 DIG)    ");
        System.out.println("=========================================================================");

        try {
            // 0. Preparar tablas y las 12 variables de entrada (IDs 1..12)
            prepararTablasYVariablesEntrada();
            System.out.println("[0] Tablas 'int_proceso_vars' (12 canales) e 'int_proceso_vars_data' listas.");

            ConexionDB conexion = new ConexionDB("db_config.txt");
            int_proceso_vars_dataDAO varsDataDAO = new int_proceso_vars_dataDAOImp(conexion);

            // Limpiar muestras previas de prueba
            varsDataDAO.borrarTodos();

            // 1. PRUEBA DE INSERCION INDIVIDUAL (Canal Analogico 1 - ID=1)
            System.out.println("\n[1] Probando INSERCION individual (insertarDatoVariable) en CH1 (ID=1)...");
            int ins1 = varsDataDAO.insertarDatoVariable(
                    1,
                    2.50f,
                    0.00f,
                    Date.valueOf("2026-11-15"),
                    Time.valueOf("19:00:00")
            );
            System.out.println("    -> Filas insertadas individualmente: " + ins1);

            // 2. PRUEBA DE INSERCION SIMULTANEA DE LOS 12 CANALES (8 Analogicas + 4 Digitales)
            System.out.println("\n[2] Probando INSERCION de trama completa (8 Analogicas + 4 Digitales)...");
            float[] voltajesADC = {0.55f, 1.20f, 2.45f, 3.10f, 3.80f, 4.25f, 4.75f, 5.00f};
            int[] entradasDig = {1, 0, 1, 1}; // D1=1, D2=0, D3=1, D4=1
            int insertadosLote = varsDataDAO.insertarMuestra12Canales(voltajesADC, entradasDig, 0.10f, 1);
            System.out.println("    -> Muestras insertadas en el ciclo (esperado 12): " + insertadosLote);

            // 3. PRUEBA DE SELECCION POR VARIABLE (Para graficar en la GUI - Entregable 3.d)
            System.out.println("\n[3] Probando SELECCION por variable (CH1 Analogico ID=1 y D1 Digital ID=9)...");
            List<int_proceso_vars_data> historialCH1 = varsDataDAO.seleccionarUltimosPorVariable(1, 10);
            System.out.println("    -> Muestras recuperadas de Entrada Analogica CH1 (ID=1): " + historialCH1.size());
            for (int_proceso_vars_data m : historialCH1) {
                System.out.println("       * " + m);
            }

            int_proceso_vars_data ultimaDigital1 = varsDataDAO.seleccionarUltimoDatoPorVariable(9);
            System.out.println("    -> Ultima muestra de Entrada Digital D1 (ID=9): " + ultimaDigital1);

            // 4. PRUEBA DE BORRADO (Por ID, por Variable y Total)
            System.out.println("\n[4] Probando BORRADO (borrarPorId, borrarPorVariable y borrarTodos)...");
            int idBorrar = ultimaDigital1.getId();
            int borradoId = varsDataDAO.borrarPorId(idBorrar);
            System.out.println("    -> Borrado por ID (" + idBorrar + "): " + borradoId + " fila eliminada.");

            int borradoVar = varsDataDAO.borrarPorVariable(1);
            System.out.println("    -> Borrado de historial CH1 (var_id=1): " + borradoVar + " filas eliminadas.");

            int borradoRestante = varsDataDAO.borrarTodos();
            System.out.println("    -> Borrado de muestras restantes: " + borradoRestante + " filas eliminadas.");

            conexion.cerrarConexion();
            System.out.println("\n=========================================================================");
            System.out.println("  [OK] TAREA #18 (HU-04) VERIFICADA EXITOSAMENTE CON MYSQL               ");
            System.out.println("=========================================================================");

        } catch (Exception e) {
            System.err.println("[ERROR] Fallo en la prueba de int_proceso_vars_dataDAO: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
