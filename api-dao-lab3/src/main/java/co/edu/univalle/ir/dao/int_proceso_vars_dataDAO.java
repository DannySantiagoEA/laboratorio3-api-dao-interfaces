package co.edu.univalle.ir.dao;

import co.edu.univalle.ir.entity.int_proceso_vars_data;
import java.sql.Date;
import java.sql.Time;
import java.util.List;

/**
 * La interfaz <b>int_proceso_vars_dataDAO</b> especifica los metodos de
 * insercion, seleccion y borrado para gestionar las muestras de las 8 variables
 * de entrada analogicas y las 4 variables de entrada digitales en la tabla
 * <code>int_proceso_vars_data</code> (Requerimiento 4, Tarea #18 y Entregables
 * 3.d, 3.g).
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/15
 * @see co.edu.univalle.ir.entity.int_proceso_vars_data
 */
public interface int_proceso_vars_dataDAO {

    // =========================================================================
    // 1. OPERACIONES DE INSERCION (CREATE)
    // =========================================================================
    /**
     * Inserta un nuevo registro de muestra en la tabla int_proceso_vars_data
     * usando un objeto entidad.
     *
     * @param dato Objeto {@link int_proceso_vars_data} con los datos de la
     * muestra.
     * @return int con el numero de filas insertadas (1 si fue exitoso, 0 en
     * caso contrario).
     */
    public int insertarDatoVariable(int_proceso_vars_data dato);

    /**
     * Inserta un nuevo registro de muestra en la tabla int_proceso_vars_data
     * especificando cada columna por parametro.
     *
     * @param int_proceso_vars_id Integer con el ID de la variable en
     * int_proceso_vars (1..8 ADC, 9..12 Dig).
     * @param valor float con el valor medido de la variable.
     * @param tiempo float con el instante de tiempo de la muestra.
     * @param fecha Objeto Date con la fecha de captura.
     * @param hora Objeto Time con la hora de captura.
     * @return int con el numero de filas insertadas.
     */
    public int insertarDatoVariable(int int_proceso_vars_id, float valor, float tiempo,
            Date fecha, Time hora);

    /**
     * Inserta por lotes (Batch Insert) una lista de muestras en la tabla
     * int_proceso_vars_data en una sola transaccion JDBC.
     *
     * @param muestras Lista de objetos {@link int_proceso_vars_data} a
     * almacenar.
     * @return int con el total de registros insertados exitosamente.
     */
    public int insertarLoteDatosVariables(List<int_proceso_vars_data> muestras);

    /**
     * Almacena simultaneamente las 8 entradas analogicas y las 4 entradas
     * digitales provenientes de una trama del sistema embebido (Entregables 3.d
     * y 3.g).
     * <p>
     * Asocia los 8 canales analogicos a los IDs <code>idVarBaseAnalogica</code>
     * .. <code>idVarBaseAnalogica + 7</code> (por defecto 1..8) y los 4 canales
     * digitales a los IDs <code>idVarBaseAnalogica + 8</code> ..
     * <code>idVarBaseAnalogica + 11</code> (por defecto 9..12).
     * </p>
     *
     * @param analogicas8 Arreglo float[8] con los voltajes de las 8 entradas
     * analogicas.
     * @param digitales4 Arreglo int[4] con los estados (0 o 1) de las 4
     * entradas digitales.
     * @param tiempo float con la estampa de tiempo comun del ciclo de muestreo.
     * @param idVarBaseAnalogica Integer con el ID inicial en int_proceso_vars
     * (usualmente 1).
     * @return int con el numero de muestras insertadas (12 si todas fueron
     * exitosas).
     */
    public int insertarMuestra12Canales(float[] analogicas8, int[] digitales4,
            float tiempo, int idVarBaseAnalogica);

    // =========================================================================
    // 2. OPERACIONES DE SELECCION (READ)
    // =========================================================================
    /**
     * Selecciona un registro de muestra especifico buscando por su
     * identificador primario (id).
     *
     * @param id Integer con el ID del registro en int_proceso_vars_data.
     * @return Objeto {@link int_proceso_vars_data} encontrado, o null si no
     * existe.
     */
    public int_proceso_vars_data seleccionarPorId(int id);

    /**
     * Selecciona todos los registros historicos asociados a una variable de
     * entrada especifica (ej. un canal analogico o digital para graficar en la
     * GUI).
     *
     * @param int_proceso_vars_id Integer con el ID de la variable en
     * int_proceso_vars.
     * @return Lista de objetos {@link int_proceso_vars_data} ordenados
     * cronologicamente.
     */
    public List<int_proceso_vars_data> seleccionarPorVariable(int int_proceso_vars_id);

    /**
     * Selecciona las ultimas <code>limite</code> muestras de una variable de
     * entrada especifica, ordenadas de forma ascendente por tiempo para su
     * trazado directo en la ventana deslizante de la GUI (Entregable 3.d).
     *
     * @param int_proceso_vars_id Integer con el ID de la variable en
     * int_proceso_vars.
     * @param limite Integer con la cantidad maxima de muestras recientes a
     * recuperar.
     * @return Lista de objetos {@link int_proceso_vars_data} lista para
     * graficar.
     */
    public List<int_proceso_vars_data> seleccionarUltimosPorVariable(int int_proceso_vars_id, int limite);

    /**
     * Selecciona la muestra mas reciente almacenada para una variable de
     * entrada especifica.
     *
     * @param int_proceso_vars_id Integer con el ID de la variable en
     * int_proceso_vars.
     * @return Objeto {@link int_proceso_vars_data} mas reciente, o null si no
     * hay datos.
     */
    public int_proceso_vars_data seleccionarUltimoDatoPorVariable(int int_proceso_vars_id);

    /**
     * Selecciona todos los registros almacenados en la tabla
     * int_proceso_vars_data.
     *
     * @return Lista completa de objetos {@link int_proceso_vars_data}.
     */
    public List<int_proceso_vars_data> seleccionarTodos();

    // =========================================================================
    // 3. OPERACIONES DE BORRADO (DELETE)
    // =========================================================================
    /**
     * Borra un registro de muestra especifico segun su identificador primario
     * (id).
     *
     * @param id Integer con el ID del registro a eliminar.
     * @return int con el numero de filas eliminadas.
     */
    public int borrarPorId(int id);

    /**
     * Borra todas las muestras asociadas a una variable de entrada especifica
     * (util para limpiar el historial de un canal en particular).
     *
     * @param int_proceso_vars_id Integer con el ID de la variable en
     * int_proceso_vars.
     * @return int con el numero de registros eliminados.
     */
    public int borrarPorVariable(int int_proceso_vars_id);

    /**
     * Borra todos los registros de muestras almacenados en la tabla
     * int_proceso_vars_data (util al iniciar una nueva sesion de laboratorio).
     *
     * @return int con el total de filas eliminadas.
     */
    public int borrarTodos();
}
