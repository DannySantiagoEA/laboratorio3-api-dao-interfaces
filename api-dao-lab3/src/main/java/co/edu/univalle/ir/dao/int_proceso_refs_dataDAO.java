package co.edu.univalle.ir.dao;

import co.edu.univalle.ir.entity.int_proceso_refs_data;
import java.sql.Date;
import java.sql.Time;
import java.util.List;

/**
 * La interfaz <b>int_proceso_refs_dataDAO</b> especifica los metodos de
 * insercion, seleccion y borrado para gestionar los estados de las 4 variables
 * de salida digitales en la tabla <code>int_proceso_refs_data</code>
 * (Requerimiento 4, Tarea #19 y Entregables 3.e, 3.g).
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/16
 * @see co.edu.univalle.ir.entity.int_proceso_refs_data
 */
public interface int_proceso_refs_dataDAO {

    // =========================================================================
    // 1. OPERACIONES DE INSERCION (CREATE)
    // =========================================================================
    /**
     * Inserta un nuevo registro de estado de salida en la tabla
     * int_proceso_refs_data a partir de un objeto entidad.
     *
     * @param dato Objeto {@link int_proceso_refs_data} con los datos a
     * almacenar.
     * @return int con el numero de filas insertadas (1 si fue exitoso, 0 en
     * caso contrario).
     */
    public int insertarDatoReferencia(int_proceso_refs_data dato);

    /**
     * Inserta un nuevo registro en la tabla int_proceso_refs_data especificando
     * cada campo por parametro.
     *
     * @param int_proceso_refs_id Integer con el ID de la salida en
     * int_proceso_refs (1..4).
     * @param valor float con el estado de la salida digital (1.0f = ON, 0.0f =
     * OFF).
     * @param tiempo float con el instante de tiempo del cambio de estado.
     * @param fecha Objeto Date con la fecha del registro.
     * @param hora Objeto Time con la hora del registro.
     * @return int con el numero de filas insertadas.
     */
    public int insertarDatoReferencia(int int_proceso_refs_id, float valor, float tiempo,
            Date fecha, Time hora);

    /**
     * Registra directamente desde la GUI el cambio de estado booleano (ON/OFF)
     * de una de las 4 salidas digitales (Entregable 3.e).
     *
     * @param numeroSalida Integer con el numero de la salida digital (1 a 4).
     * @param estadoActivo boolean true para encender (1.0f), false para apagar
     * (0.0f).
     * @param tiempo float con el tiempo actual en segundos.
     * @param idRefBase Integer con el ID inicial en int_proceso_refs
     * (usualmente 1).
     * @return int con el numero de filas insertadas (1 si fue exitoso).
     */
    public int registrarEstadoSalidaDigital(int numeroSalida, boolean estadoActivo,
            float tiempo, int idRefBase);

    /**
     * Inserta en lote (Batch Insert) el estado de las 4 salidas digitales en
     * una sola operacion JDBC.
     *
     * @param estados4Salidas Arreglo boolean[4] con el estado de las 4 salidas
     * digitales.
     * @param tiempo float con la estampa de tiempo comun.
     * @param idRefBase Integer con el ID inicial en int_proceso_refs
     * (usualmente 1).
     * @return int con el total de registros insertados (4 si todos fueron
     * exitosos).
     */
    public int insertarEstados4SalidasDigitales(boolean[] estados4Salidas,
            float tiempo, int idRefBase);

    // =========================================================================
    // 2. OPERACIONES DE SELECCION (READ)
    // =========================================================================
    /**
     * Selecciona un registro especifico de int_proceso_refs_data buscando por
     * su identificador primario (id).
     *
     * @param id Integer con el ID primario del registro.
     * @return Objeto {@link int_proceso_refs_data} encontrado, o null si no
     * existe.
     */
    public int_proceso_refs_data seleccionarPorId(int id);

    /**
     * Selecciona todo el historial de cambios de estado asociado a una salida
     * digital especifica.
     *
     * @param int_proceso_refs_id Integer con el ID de la referencia en
     * int_proceso_refs.
     * @return Lista de objetos {@link int_proceso_refs_data} ordenados
     * cronologicamente.
     */
    public List<int_proceso_refs_data> seleccionarPorReferencia(int int_proceso_refs_id);

    /**
     * Selecciona el registro mas reciente almacenado para una salida digital
     * especifica.
     *
     * @param int_proceso_refs_id Integer con el ID de la salida en
     * int_proceso_refs (1..4).
     * @return Objeto {@link int_proceso_refs_data} con el ultimo estado, o null
     * si no hay registros.
     */
    public int_proceso_refs_data seleccionarUltimoDatoPorReferencia(int int_proceso_refs_id);

    /**
     * Obtiene el estado booleano vigente de las 4 variables de salida digitales
     * almacenadas en la base de datos para ser enviadas por el programa puente
     * RS232-SQL al sistema embebido (Entregables 3.e y 3.g).
     *
     * @param idRefBase Integer con el ID inicial de las salidas en
     * int_proceso_refs (usualmente 1).
     * @return Arreglo boolean[4] donde cada posicion (0 a 3) representa el
     * estado ON/OFF de D1..D4.
     */
    public boolean[] consultarUltimosEstados4Salidas(int idRefBase);

    /**
     * Selecciona todos los registros almacenados en la tabla
     * int_proceso_refs_data.
     *
     * @return Lista completa de objetos {@link int_proceso_refs_data}.
     */
    public List<int_proceso_refs_data> seleccionarTodos();

    // =========================================================================
    // 3. OPERACIONES DE BORRADO (DELETE)
    // =========================================================================
    /**
     * Borra un registro especifico de int_proceso_refs_data segun su ID
     * primario.
     *
     * @param id Integer con el ID del registro a eliminar.
     * @return int con el numero de filas eliminadas.
     */
    public int borrarPorId(int id);

    /**
     * Borra todos los registros historicos asociados a una salida digital
     * especifica.
     *
     * @param int_proceso_refs_id Integer con el ID de la salida en
     * int_proceso_refs.
     * @return int con el numero de filas eliminadas.
     */
    public int borrarPorReferencia(int int_proceso_refs_id);

    /**
     * Borra todos los registros almacenados en la tabla int_proceso_refs_data.
     *
     * @return int con el total de registros eliminados.
     */
    public int borrarTodos();
}
