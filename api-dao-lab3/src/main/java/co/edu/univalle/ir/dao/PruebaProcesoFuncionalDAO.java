package co.edu.univalle.ir.dao;

import co.edu.univalle.ir.conexion.ConexionDB;
import co.edu.univalle.ir.entity.int_proceso;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Clase de prueba autoejecutable para la Tarea #15: validacion funcional de la
 * creacion, la actualizacion del tiempo de muestreo y la eliminacion de
 * registros en <code>int_proceso</code>.
 * <p>
 * Trabaja siempre con un proceso temporal que ella misma crea y borra. Nunca
 * modifica ni borra el proceso 1: como las llaves foraneas son
 * <code>ON DELETE CASCADE</code>, borrarlo arrastraria sus 12 variables, sus 4
 * salidas y todo su historico.
 * </p>
 * <p>
 * Requiere que el proceso 1 ya exista (punto 3.b del enunciado).
 * </p>
 *
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/18
 */
public class PruebaProcesoFuncionalDAO {

    /**
     * Tolerancia al comparar floats leidos de MySQL (la columna es
     * FLOAT(10,2)).
     */
    private static final float TOL = 0.01f;

    /**
     * Id que no existe en la tabla.
     */
    private static final int ID_INEXISTENTE = 999999;

    /**
     * Nombre con el que se reconocen los procesos creados por esta prueba.
     */
    private static final String NOMBRE_PRUEBA = "Proceso de prueba #15";

    private static int aciertos = 0;
    private static int fallos = 0;

    /**
     * Registra el resultado de una comprobacion y lo imprime.
     *
     * @param descripcion Texto de lo que se comprueba.
     * @param condicion true si la comprobacion se cumple.
     */
    static void verificar(String descripcion, boolean condicion) {
        if (condicion) {
            aciertos++;
            System.out.println("    [PASA]  " + descripcion);
        } else {
            fallos++;
            System.out.println("    [FALLA] " + descripcion);
        }
    }

    /**
     * Imprime un hallazgo que no cuenta como acierto ni como fallo.
     *
     * @param descripcion Lo que se probo.
     * @param resultado Lo que hizo el DAO.
     */
    static void observar(String descripcion, String resultado) {
        System.out.println("    [OBS]   " + descripcion + ": " + resultado);
    }

    /**
     * Indica si actualizar el tiempo de muestreo con ese valor lanza
     * IllegalArgumentException.
     *
     * @param dao DAO de int_proceso.
     * @param id Id del proceso.
     * @param ts Tiempo de muestreo a probar.
     * @return true si el DAO rechazo el valor con IllegalArgumentException.
     */
    static boolean rechazaTs(int_procesoDAO dao, int id, float ts) {
        try {
            dao.actualizarTiempoMuestreo(id, ts);
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    /**
     * Ejecuta las pruebas funcionales de creacion, actualizacion del tiempo de
     * muestreo y eliminacion sobre un proceso temporal.
     *
     * @param dao DAO de la tabla int_proceso.
     * @param idTipo Id de un int_proceso_tipo que exista.
     */
    static void probarProceso(int_procesoDAO dao, int idTipo) {
        System.out.println("\n[1] CREAR un proceso temporal");
        int_proceso nuevo = new int_proceso(idTipo, NOMBRE_PRUEBA,
                "Creado por la prueba de la tarea #15", 100.0f,
                "Especificaciones de prueba", "Manual de prueba");
        verificar("insertarProceso devuelve 1 fila", dao.insertarProceso(nuevo) == 1);
        int id = nuevo.getId();
        verificar("el DAO asigna un id nuevo (" + id + ")", id > 0 && id != 1);
        if (id <= 0 || id == 1) {
            System.out.println("    Se detiene: sin un id propio no es seguro seguir.");
            return;
        }
        int_proceso leido = dao.seleccionarPorId(id);
        verificar("seleccionarPorId encuentra el proceso creado", leido != null);
        if (leido != null) {
            verificar("el nombre se guardo igual", NOMBRE_PRUEBA.equals(leido.getNombre()));
            verificar("el tipo se guardo igual", leido.getInt_proceso_tipo_id() == idTipo);
            verificar("el tiempo de muestreo inicial es 100.0 ms",
                    Math.abs(leido.getTiempo_muestreo() - 100.0f) < TOL);
        }

        System.out.println("\n[2] ACTUALIZAR el tiempo de muestreo");
        float[] valores = {250.0f, 10.0f, 33.33f, 5000.0f};
        for (float ts : valores) {
            boolean actualizo = dao.actualizarTiempoMuestreo(id, ts) == 1;
            float leidoTs = dao.consultarTiempoMuestreo(id);
            verificar("Ts = " + ts + " ms: actualiza 1 fila y se lee igual (" + leidoTs + ")",
                    actualizo && Math.abs(leidoTs - ts) < TOL);
        }
        int_proceso trasTs = dao.seleccionarPorId(id);
        verificar("actualizar el Ts no cambia el nombre ni el tipo",
                trasTs != null && NOMBRE_PRUEBA.equals(trasTs.getNombre())
                && trasTs.getInt_proceso_tipo_id() == idTipo);

        System.out.println("\n[3] ACTUALIZAR con valores invalidos o ids que no existen");
        verificar("Ts = 0 lanza IllegalArgumentException", rechazaTs(dao, id, 0.0f));
        verificar("Ts = -5 lanza IllegalArgumentException", rechazaTs(dao, id, -5.0f));
        verificar("tras los rechazos el Ts sigue en 5000.0",
                Math.abs(dao.consultarTiempoMuestreo(id) - 5000.0f) < TOL);
        verificar("actualizar un id inexistente devuelve 0 filas",
                dao.actualizarTiempoMuestreo(ID_INEXISTENTE, 100.0f) == 0);
        verificar("consultar el Ts de un id inexistente devuelve -1",
                dao.consultarTiempoMuestreo(ID_INEXISTENTE) == -1.0f);
        boolean acepto5 = dao.actualizarTiempoMuestreo(id, 5.0f) == 1;
        observar("Ts = 5 ms (el firmware pide minimo 10 ms)",
                acepto5 ? "el DAO lo ACEPTA, no valida el minimo" : "el DAO lo rechaza");
        dao.actualizarTiempoMuestreo(id, 100.0f);

        System.out.println("\n[4] EDITAR el proceso completo");
        int_proceso aEditar = dao.seleccionarPorId(id);
        if (aEditar != null) {
            aEditar.setNombre(NOMBRE_PRUEBA + " (editado)");
            aEditar.setTiempo_muestreo(120.0f);
            verificar("editarProceso devuelve 1 fila", dao.editarProceso(aEditar) == 1);
            int_proceso editado = dao.seleccionarPorId(id);
            verificar("el nombre y el Ts editados se leen igual",
                    editado != null
                    && (NOMBRE_PRUEBA + " (editado)").equals(editado.getNombre())
                    && Math.abs(editado.getTiempo_muestreo() - 120.0f) < TOL);
        } else {
            verificar("el proceso temporal sigue existiendo antes de editarlo", false);
        }

        System.out.println("\n[5] CREAR con datos invalidos"
                + " (el error de llave foranea que sale en la consola es el esperado)");
        verificar("insertarProceso(null) devuelve 0", dao.insertarProceso((int_proceso) null) == 0);
        verificar("un tipo de proceso inexistente devuelve 0 filas",
                dao.insertarProceso(ID_INEXISTENTE, NOMBRE_PRUEBA + " invalido",
                        "tipo inexistente", 100.0f, "", "") == 0);

        System.out.println("\n[6] ELIMINAR el proceso temporal");
        verificar("borrarProceso devuelve 1 fila", dao.borrarProceso(id) == 1);
        verificar("seleccionarPorId ya no lo encuentra", dao.seleccionarPorId(id) == null);
        verificar("consultarTiempoMuestreo devuelve -1 despues de borrar",
                dao.consultarTiempoMuestreo(id) == -1.0f);
        verificar("borrar otra vez devuelve 0 filas", dao.borrarProceso(id) == 0);
        boolean sigueListado = false;
        for (int_proceso p : dao.seleccionarTodos()) {
            if (p.getId() == id) {
                sigueListado = true;
            }
        }
        verificar("seleccionarTodos ya no lo lista", !sigueListado);
        verificar("el proceso 1 sigue intacto", dao.seleccionarPorId(1) != null);
    }

    /**
     * Borra los procesos que dejo esta prueba (nunca el proceso 1).
     *
     * @param dao DAO de la tabla int_proceso.
     */
    static void limpiar(int_procesoDAO dao) {
        // Se recorre una COPIA: el DAO modifica la lista que devolvio al borrar, y
        // borrar mientras se recorre la original lanza ConcurrentModificationException.
        for (int_proceso p : new ArrayList<>(dao.seleccionarTodos())) {
            if (p.getId() != 1 && p.getNombre() != null && p.getNombre().startsWith(NOMBRE_PRUEBA)) {
                dao.borrarProceso(p.getId());
            }
        }
    }

    /**
     * Punto de entrada de la prueba.
     *
     * @param args Argumentos de linea de comandos (no se usan).
     */
    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("  PRUEBA TAREA #15: CREAR, ACTUALIZAR MUESTREO Y BORRAR int_proceso ");
        System.out.println("==================================================================");
        ConexionDB conexion = null;
        try {
            conexion = new ConexionDB("db_config.txt");
            int_procesoDAO dao = new int_procesoDAOImp(conexion);
            int_proceso base = dao.seleccionarPorId(1);
            if (base == null) {
                fallos++;
                System.out.println("[ERROR] No existe el proceso 1: ingresalo primero (punto 3.b).");
            } else {
                limpiar(dao); // por si una corrida anterior se corto a la mitad
                try {
                    probarProceso(dao, base.getInt_proceso_tipo_id());
                } finally {
                    limpiar(dao);
                    try (Statement st = conexion.getConnection().createStatement()) {
                        // deja el contador de ids donde estaba antes de la prueba
                        st.executeUpdate("ALTER TABLE int_proceso AUTO_INCREMENT = 1");
                    } catch (SQLException e) {
                        System.out.println("    (no se pudo reiniciar AUTO_INCREMENT: " + e.getMessage() + ")");
                    }
                }
            }
        } catch (Exception e) {
            fallos++;
            System.err.println("[ERROR] No se pudo ejecutar la prueba: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conexion != null) {
                try {
                    conexion.cerrarConexion();
                } catch (SQLException e) {
                    System.err.println("[AVISO] No se pudo cerrar la conexion: " + e.getMessage());
                }
            }
        }
        System.out.println("\n==================================================================");
        System.out.println("  RESULTADO: " + aciertos + " pasan, " + fallos + " fallan");
        System.out.println("==================================================================");
    }
}
