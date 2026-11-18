package co.edu.univalle.ir.dao;

import co.edu.univalle.ir.entity.int_proceso_control;
import co.edu.univalle.ir.entity.int_proceso_control;
import co.edu.univalle.ir.entity.int_proceso_refs;
import co.edu.univalle.ir.entity.int_proceso_refs;
import co.edu.univalle.ir.entity.int_proceso_vars;
import co.edu.univalle.ir.entity.int_proceso_vars;
import java.util.List;

/**
 * La interfaz <b>int_proceso_infoDAO</b> define el contrato de seleccion de
 * registros en las tablas <code>int_proceso_vars</code>,
 * <code>int_proceso_refs</code> e <code>int_proceso_control</code>
 * (Requerimiento 4, Tarea #17).
 * <p>
 * Estas tablas describen el hardware (variables de entrada, salidas y
 * controles) y se llenan una sola vez desde phpMyAdmin (Entregable 3.c); por
 * eso la API solo necesita leerlas. La GUI y el programa puente RS232-SQL usan
 * estos metodos para saber que ID corresponde a cada canal antes de guardar
 * datos en <code>int_proceso_vars_data</code> e
 * <code>int_proceso_refs_data</code>.
 * </p>
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 *
 * @version $Revision: 1.0 $ 2026/11/16
 * @see co.edu.univalle.ir.entity.int_proceso_vars
 * @see co.edu.univalle.ir.entity.int_proceso_refs
 * @see co.edu.univalle.ir.entity.int_proceso_control
 */
public interface int_proceso_infoDAO {

    // =========================================================================
    // int_proceso_vars (variables de entrada)
    // =========================================================================
    /**
     * Selecciona todas las variables de entrada de un proceso, ordenadas por ID
     * (en el laboratorio: 8 analogicas y luego 4 digitales).
     *
     * @param int_proceso_id Integer con el ID del proceso en int_proceso.
     * @return Lista de objetos {@link int_proceso_vars} del proceso (vacia si
     * no tiene).
     */
    public List<int_proceso_vars> seleccionarVariables(int int_proceso_id);

    /**
     * Selecciona una variable de entrada por su identificador primario.
     *
     * @param id Integer con el ID de la variable.
     * @return Objeto {@link int_proceso_vars} encontrado, o null si no existe.
     */
    public int_proceso_vars seleccionarVariablePorId(int id);

    /**
     * Selecciona una variable de entrada de un proceso por su nombre exacto
     * (por ejemplo, "Analogica 1").
     *
     * @param int_proceso_id Integer con el ID del proceso.
     * @param nombre String con el nombre de la variable.
     * @return Objeto {@link int_proceso_vars} encontrado, o null si no existe.
     */
    public int_proceso_vars seleccionarVariablePorNombre(int int_proceso_id, String nombre);

    // =========================================================================
    // int_proceso_refs (referencias o salidas)
    // =========================================================================
    /**
     * Selecciona todas las referencias (salidas) de un proceso, ordenadas por
     * ID (en el laboratorio: las 4 salidas digitales).
     *
     * @param int_proceso_id Integer con el ID del proceso en int_proceso.
     * @return Lista de objetos {@link int_proceso_refs} del proceso (vacia si
     * no tiene).
     */
    public List<int_proceso_refs> seleccionarReferencias(int int_proceso_id);

    /**
     * Selecciona una referencia por su identificador primario.
     *
     * @param id Integer con el ID de la referencia.
     * @return Objeto {@link int_proceso_refs} encontrado, o null si no existe.
     */
    public int_proceso_refs seleccionarReferenciaPorId(int id);

    /**
     * Selecciona una referencia de un proceso por su nombre exacto (por
     * ejemplo, "Salida Digital 1").
     *
     * @param int_proceso_id Integer con el ID del proceso.
     * @param nombre String con el nombre de la referencia.
     * @return Objeto {@link int_proceso_refs} encontrado, o null si no existe.
     */
    public int_proceso_refs seleccionarReferenciaPorNombre(int int_proceso_id, String nombre);

    // =========================================================================
    // int_proceso_control
    // =========================================================================
    /**
     * Selecciona todos los controles configurados para un proceso.
     *
     * @param int_proceso_id Integer con el ID del proceso en int_proceso.
     * @return Lista de objetos {@link int_proceso_control} del proceso (vacia
     * si no tiene).
     */
    public List<int_proceso_control> seleccionarControles(int int_proceso_id);

    /**
     * Selecciona un control por su identificador primario.
     *
     * @param id Integer con el ID del control.
     * @return Objeto {@link int_proceso_control} encontrado, o null si no
     * existe.
     */
    public int_proceso_control seleccionarControlPorId(int id);
}
