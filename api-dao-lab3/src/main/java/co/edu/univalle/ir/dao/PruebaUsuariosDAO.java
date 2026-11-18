package co.edu.univalle.ir.dao;

import co.edu.univalle.ir.conexion.ConexionDB;
import co.edu.univalle.ir.entity.int_usuarios;
import java.util.List;

/**
 * Clase de verificacion funcional para la Tarea #8 (HU-02): insercion, edicion,
 * borrado y seleccion filtrada por diversos campos en la tabla
 * <code>int_usuarios</code> mediante {@link int_usuariosDAOImp}.
 * <p>
 * Requiere que la base de datos se haya creado con el script
 * <code>sql/lab_virtual_db.sql</code> (perfil ID = 1 y ID = 2). Los usuarios de
 * prueba se borran al final, asi que se puede ejecutar varias veces.
 * </p>
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/15
 */
public class PruebaUsuariosDAO {

    /**
     * Archivo de configuracion de la conexion.
     */
    private static final String ARCHIVO_CONFIG = "db_config.txt";

    /**
     * Perfil Estudiante creado por el script SQL.
     */
    private static final int TIPO_ESTUDIANTE = 1;

    /**
     * Perfil Administrador creado por el script SQL.
     */
    private static final int TIPO_ADMINISTRADOR = 2;

    /**
     * Constructor privado: la clase solo se usa desde su metodo main.
     */
    private PruebaUsuariosDAO() {
    }

    /**
     * Metodo principal de ejecucion de la prueba del DAO de usuarios.
     *
     * @param args Argumentos de linea de comandos (no se usan).
     */
    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("  PRUEBA TAREA #8 (HU-02): int_usuariosDAOImp (Lab 3)");
        System.out.println("==================================================================");

        try {
            ConexionDB conexion = new ConexionDB(ARCHIVO_CONFIG);
            int_usuariosDAO dao = new int_usuariosDAOImp(conexion);

            // Limpiar restos de una ejecucion anterior
            borrarSiExiste(dao, "ana.prueba@correounivalle.edu.co");
            borrarSiExiste(dao, "luis.prueba@correounivalle.edu.co");

            // 1. INSERCION
            System.out.println("\n[1] INSERCION");
            int_usuarios ana = new int_usuarios("Ana Maria", "Prueba Lopez",
                    "ana.prueba@correounivalle.edu.co", "Clave123", TIPO_ESTUDIANTE);
            System.out.println("    insertarUsuario(objeto)  -> filas: " + dao.insertarUsuario(ana)
                    + " | ID generado: " + ana.getId());
            System.out.println("    insertarUsuario(campos)  -> filas: " + dao.insertarUsuario(
                    "Luis Carlos", "Prueba Ruiz", "luis.prueba@correounivalle.edu.co", "Admin456", TIPO_ADMINISTRADOR));
            int_usuarios luis = dao.seleccionarPorEmail("luis.prueba@correounivalle.edu.co");

            try {
                dao.insertarUsuario("Sin", "Clave", "x@x.com", "", TIPO_ESTUDIANTE);
            } catch (IllegalArgumentException ex) {
                System.out.println("    insercion con clave vacia rechazada: " + ex.getMessage());
            }

            // 2. SELECCION FILTRADA POR DIVERSOS CAMPOS
            System.out.println("\n[2] SELECCION");
            System.out.println("    seleccionarPorId(" + ana.getId() + ")      -> " + dao.seleccionarPorId(ana.getId()));
            System.out.println("    seleccionarPorEmail(luis)  -> " + luis);
            imprimirLista("seleccionarPorNombres(\"ana\")", dao.seleccionarPorNombres("ana"));
            imprimirLista("seleccionarPorApellidos(\"Prueba\")", dao.seleccionarPorApellidos("Prueba"));
            imprimirLista("seleccionarPorTipo(" + TIPO_ADMINISTRADOR + ")", dao.seleccionarPorTipo(TIPO_ADMINISTRADOR));
            imprimirLista("seleccionarPorCampos(tipo=1, apellidos=\"Prueba\")",
                    dao.seleccionarPorCampos(null, TIPO_ESTUDIANTE, null, "Prueba", null));
            imprimirLista("seleccionarPorCampos(email contiene \"prueba@\")",
                    dao.seleccionarPorCampos(null, null, null, null, "prueba@"));
            System.out.println("    seleccionarTodos()         -> " + dao.seleccionarTodos().size() + " usuarios");

            // 3. LOGIN (validarCredenciales)
            System.out.println("\n[3] LOGIN");
            System.out.println("    email y clave correctos    -> " + dao.validarCredenciales(ana.getEmail(), "Clave123"));
            System.out.println("    clave incorrecta           -> " + dao.validarCredenciales(ana.getEmail(), "otra"));
            System.out.println("    clave con otra mayuscula   -> " + dao.validarCredenciales(ana.getEmail(), "clave123"));
            System.out.println("    email inexistente          -> " + dao.validarCredenciales("nadie@x.com", "Clave123"));

            // 4. EDICION
            System.out.println("\n[4] EDICION");
            ana.setApellidos("Prueba Lopez Editado");
            ana.setInt_usuarios_tipo_id(TIPO_ADMINISTRADOR);
            System.out.println("    editarUsuario(ana)         -> filas: " + dao.editarUsuario(ana));
            System.out.println("    registro tras edicion      -> " + dao.seleccionarPorId(ana.getId()));
            System.out.println("    cambiarClave(ana, Nueva789)-> filas: " + dao.cambiarClave(ana.getId(), "Nueva789"));
            System.out.println("    login con clave anterior   -> " + dao.validarCredenciales(ana.getEmail(), "Clave123"));
            System.out.println("    login con clave nueva      -> " + dao.validarCredenciales(ana.getEmail(), "Nueva789"));

            // 5. BORRADO
            System.out.println("\n[5] BORRADO");
            System.out.println("    borrarUsuario(ana)         -> filas: " + dao.borrarUsuario(ana.getId()));
            System.out.println("    borrarUsuario(luis)        -> filas: " + dao.borrarUsuario(luis.getId()));
            System.out.println("    seleccionarPorId(ana)      -> " + dao.seleccionarPorId(ana.getId()) + " (esperado null)");

            conexion.cerrarConexion();
            System.out.println("\n==================================================================");
            System.out.println("  [OK] TAREA #8 VERIFICADA CON MYSQL");
            System.out.println("==================================================================");

        } catch (Exception e) {
            System.err.println("[ERROR] Fallo en la prueba de int_usuariosDAO: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Imprime el tamano de una lista de usuarios y sus elementos.
     *
     * @param titulo String con el metodo probado.
     * @param lista Lista de usuarios devuelta por el DAO.
     */
    private static void imprimirLista(String titulo, List<int_usuarios> lista) {
        System.out.println("    " + titulo + " -> " + lista.size() + " resultado(s)");
        for (int_usuarios u : lista) {
            System.out.println("        " + u);
        }
    }

    /**
     * Borra un usuario de prueba si quedo de una ejecucion anterior.
     *
     * @param dao DAO de usuarios.
     * @param email String con el email del usuario de prueba.
     */
    private static void borrarSiExiste(int_usuariosDAO dao, String email) {
        int_usuarios previo = dao.seleccionarPorEmail(email);
        if (previo != null) {
            dao.borrarUsuario(previo.getId());
        }
    }
}
