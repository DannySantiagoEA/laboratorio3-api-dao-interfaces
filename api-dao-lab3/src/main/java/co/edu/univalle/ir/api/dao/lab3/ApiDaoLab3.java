package co.edu.univalle.ir.api.dao.lab3;

/**
 * La clase <b>ApiDaoLab3</b> identifica la API DAO del Laboratorio Virtual e
 * imprime un resumen de sus paquetes cuando se ejecuta el JAR directamente.
 * <p>
 * La API se compone de tres paquetes:
 * </p>
 * <ul>
 * <li><code>co.edu.univalle.ir.conexion</code>: conexion JDBC con MySQL
 * (Requerimiento 1).</li>
 * <li><code>co.edu.univalle.ir.entity</code>: entidades (POJOs) de cada tabla de
 * la base de datos.</li>
 * <li><code>co.edu.univalle.ir.dao</code>: interfaces DAO y sus
 * implementaciones (Requerimientos 2, 3 y 4).</li>
 * </ul>
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @author Vanessa
 * @version $Revision: 1.0 $ 2026/11/16
 */
public class ApiDaoLab3 {

    /**
     * Version de la API.
     */
    public static final String VERSION = "1.0";

    /**
     * Base de datos para la que esta hecha la API.
     */
    public static final String BASE_DATOS = "lab_virtual_db";

    /**
     * Constructor privado: la clase solo contiene datos estaticos.
     */
    private ApiDaoLab3() {
    }

    /**
     * Imprime en consola el nombre, la version y los paquetes de la API.
     *
     * @param args Argumentos de linea de comandos (no se usan).
     */
    public static void main(String[] args) {
        System.out.println("API DAO Laboratorio Virtual v" + VERSION + " (base de datos " + BASE_DATOS + ")");
        System.out.println("  co.edu.univalle.ir.conexion -> ConexionDB");
        System.out.println("  co.edu.univalle.ir.entity   -> entidades de las tablas int_*");
        System.out.println("  co.edu.univalle.ir.dao      -> interfaces DAO e implementaciones *DAOImp");
        System.out.println("Universidad del Valle - Interfaces 710044M - Practica No. 3");
    }
}