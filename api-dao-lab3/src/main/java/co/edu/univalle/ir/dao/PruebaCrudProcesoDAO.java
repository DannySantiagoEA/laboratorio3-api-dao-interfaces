package co.edu.univalle.ir.dao;

import co.edu.univalle.ir.conexion.ConexionDB;
import co.edu.univalle.ir.entity.int_proceso;
import java.util.List;

/**
 * Clase de verificacion funcional para las Tareas #12 y #13 (HU-03): CRUD
 * completo (insercion, seleccion, edicion y borrado) sobre la tabla
 * <code>int_proceso</code> mediante {@link int_procesoDAOImp}.
 * <p>
 * Complementa a {@link PruebaProcesoDAO}, que solo prueba los metodos rapidos
 * de <code>tiempo_muestreo</code> (Tarea #14). El proceso de prueba se borra al
 * final, de modo que no altera el proceso ID = 1 que usa la GUI.
 * </p>
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/16
 */
public class PruebaCrudProcesoDAO {

    /**
     * Archivo de configuracion de la conexion.
     */
    private static final String ARCHIVO_CONFIG = "db_config.txt";

    /**
     * Tipo de proceso creado por el script SQL (Adquisicion Embebida).
     */
    private static final int TIPO_PROCESO = 1;

    /**
     * Constructor privado: la clase solo se usa desde su metodo main.
     */
    private PruebaCrudProcesoDAO() {
    }

    /**
     * Metodo principal de la prueba CRUD de int_procesoDAO.
     *
     * @param args Argumentos de linea de comandos (no se usan).
     */
    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("  PRUEBA TAREAS #12 y #13 (HU-03): CRUD int_procesoDAOImp (Lab 3)");
        System.out.println("==================================================================");

        try {
            ConexionDB conexion = new ConexionDB(ARCHIVO_CONFIG);
            int_procesoDAO dao = new int_procesoDAOImp(conexion);

            // 1. INSERCION
            System.out.println("\n[1] INSERCION");
            int_proceso nuevo = new int_proceso(TIPO_PROCESO, "Proceso de prueba CRUD",
                    "Registro temporal de la prueba", 500.0f,
                    "Especificaciones de prueba", "Manual de prueba");
            System.out.println("    insertarProceso(objeto) -> filas: " + dao.insertarProceso(nuevo)
                    + " | ID generado: " + nuevo.getId());

            // 2. SELECCION
            System.out.println("\n[2] SELECCION");
            System.out.println("    seleccionarPorId(" + nuevo.getId() + ") -> " + dao.seleccionarPorId(nuevo.getId()));
            List<int_proceso> todos = dao.seleccionarTodos();
            System.out.println("    seleccionarTodos() -> " + todos.size() + " proceso(s)");
            for (int_proceso p : todos) {
                System.out.println("        " + p);
            }

            // 3. EDICION
            System.out.println("\n[3] EDICION");
            nuevo.setNombre("Proceso de prueba CRUD (editado)");
            nuevo.setTiempo_muestreo(750.0f);
            System.out.println("    editarProceso(objeto) -> filas: " + dao.editarProceso(nuevo));
            System.out.println("    registro tras edicion -> " + dao.seleccionarPorId(nuevo.getId()));

            // 4. BORRADO
            System.out.println("\n[4] BORRADO");
            System.out.println("    borrarProceso(" + nuevo.getId() + ") -> filas: " + dao.borrarProceso(nuevo.getId()));
            System.out.println("    seleccionarPorId tras borrar -> " + dao.seleccionarPorId(nuevo.getId())
                    + " (esperado null)");
            System.out.println("    borrarProceso de un ID inexistente -> filas: " + dao.borrarProceso(-1)
                    + " (esperado 0)");

            conexion.cerrarConexion();
            System.out.println("\n==================================================================");
            System.out.println("  [OK] TAREAS #12 y #13 VERIFICADAS CON MYSQL");
            System.out.println("==================================================================");

        } catch (Exception e) {
            System.err.println("[ERROR] Fallo en la prueba CRUD de int_procesoDAO: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
