package co.edu.univalle.ir.dao;

import co.edu.univalle.ir.conexion.ConexionDB;
import co.edu.univalle.ir.entity.int_proceso_control;
import co.edu.univalle.ir.entity.int_proceso_refs;
import co.edu.univalle.ir.entity.int_proceso_vars;

/**
 * Clase de verificacion funcional para la Tarea #17: seleccion de registros en
 * <code>int_proceso_vars</code>, <code>int_proceso_refs</code> e
 * <code>int_proceso_control</code> mediante {@link int_proceso_infoDAOImp}.
 * <p>
 * Requiere haber ejecutado <code>sql/datos_proceso.sql</code> (12 variables, 4
 * referencias y 1 control para el proceso ID = 1).
 * </p>
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/16
 */
public class PruebaProcesoInfoDAO {

    /**
     * Archivo de configuracion de la conexion.
     */
    private static final String ARCHIVO_CONFIG = "db_config.txt";

    /**
     * Proceso del laboratorio (ATmega328P).
     */
    private static final int ID_PROCESO = 1;

    /**
     * Constructor privado: la clase solo se usa desde su metodo main.
     */
    private PruebaProcesoInfoDAO() {
    }

    /**
     * Metodo principal de la prueba de seleccion.
     *
     * @param args Argumentos de linea de comandos (no se usan).
     */
    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("  PRUEBA TAREA #17: SELECCION EN int_proceso_vars / refs / control");
        System.out.println("==================================================================");

        try {
            ConexionDB conexion = new ConexionDB(ARCHIVO_CONFIG);
            int_proceso_infoDAO dao = new int_proceso_infoDAOImp(conexion);

            System.out.println("\n[1] int_proceso_vars del proceso " + ID_PROCESO);
            for (int_proceso_vars v : dao.seleccionarVariables(ID_PROCESO)) {
                System.out.println("    " + v);
            }
            System.out.println("    seleccionarVariablePorId(9)      -> " + dao.seleccionarVariablePorId(9));
            System.out.println("    seleccionarVariablePorNombre(\"Analogica 3\") -> "
                    + dao.seleccionarVariablePorNombre(ID_PROCESO, "Analogica 3"));

            System.out.println("\n[2] int_proceso_refs del proceso " + ID_PROCESO);
            for (int_proceso_refs r : dao.seleccionarReferencias(ID_PROCESO)) {
                System.out.println("    " + r);
            }
            System.out.println("    seleccionarReferenciaPorNombre(\"Salida Digital 2\") -> "
                    + dao.seleccionarReferenciaPorNombre(ID_PROCESO, "Salida Digital 2"));

            System.out.println("\n[3] int_proceso_control del proceso " + ID_PROCESO);
            for (int_proceso_control c : dao.seleccionarControles(ID_PROCESO)) {
                System.out.println("    " + c);
                System.out.println("    descripcion: " + c.getDescripcion());
            }

            System.out.println("\n[4] Casos sin resultado");
            System.out.println("    seleccionarVariables(999)        -> "
                    + dao.seleccionarVariables(999).size() + " registros (esperado 0)");
            System.out.println("    seleccionarReferenciaPorId(999)  -> "
                    + dao.seleccionarReferenciaPorId(999) + " (esperado null)");
            System.out.println("    seleccionarControlPorId(999)     -> "
                    + dao.seleccionarControlPorId(999) + " (esperado null)");

            conexion.cerrarConexion();
            System.out.println("\n==================================================================");
            System.out.println("  [OK] TAREA #17 VERIFICADA CON MYSQL");
            System.out.println("==================================================================");

        } catch (Exception e) {
            System.err.println("[ERROR] Fallo en la prueba de int_proceso_infoDAO: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
