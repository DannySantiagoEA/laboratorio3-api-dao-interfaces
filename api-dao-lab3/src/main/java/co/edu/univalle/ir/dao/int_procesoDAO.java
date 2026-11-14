package co.edu.univalle.ir.dao;

import co.edu.univalle.ir.entity.int_proceso;
import java.util.List;

/**
 * La interfaz <b>int_procesoDAO</b> define el contrato de acceso a datos para
 * la insercion, seleccion, edicion y borrado de registros en la tabla
 * <code>int_proceso</code> (Requerimiento 3), incluyendo los metodos de
 * consulta y actualizacion rapida del campo <code>tiempo_muestreo</code> por ID
 * de proceso (Tarea #14 - Entregables 3.f y 3.g).
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/14
 * @see co.edu.univalle.ir.entity.int_proceso
 */
public interface int_procesoDAO {

    /**
     * Inserta un nuevo proceso en la tabla int_proceso a partir de un objeto
     * entidad.
     *
     * @param proceso Objeto {@link int_proceso} con la informacion a registrar.
     * @return int con el numero de filas insertadas (1 si fue exitoso, 0 en
     * caso contrario).
     */
    public int insertarProceso(int_proceso proceso);

    /**
     * Inserta un nuevo proceso en la tabla int_proceso recibiendo cada campo
     * por separado.
     *
     * @param int_proceso_tipo_id Integer correspondiente al tipo de proceso en
     * int_proceso_tipo.
     * @param nombre String con el nombre del proceso.
     * @param descripcion String con la descripcion del hardware.
     * @param tiempo_muestreo float con el tiempo de muestreo inicial.
     * @param archivo_especificaciones String con las especificaciones del
     * proceso.
     * @param archivo_manual String con el manual del proceso.
     * @return int con el numero de filas insertadas.
     */
    public int insertarProceso(int int_proceso_tipo_id, String nombre, String descripcion,
            float tiempo_muestreo, String archivo_especificaciones, String archivo_manual);

    /**
     * Selecciona un proceso especifico de la tabla int_proceso buscando por su
     * identificador primario.
     *
     * @param id Integer con el ID del proceso a buscar.
     * @return Objeto {@link int_proceso} encontrado, o null si no existe.
     */
    public int_proceso seleccionarPorId(int id);

    /**
     * Selecciona todos los procesos registrados en la tabla int_proceso.
     *
     * @return Lista de objetos {@link int_proceso}.
     */
    public List<int_proceso> seleccionarTodos();

    /**
     * Edita (actualiza) todos los campos de un registro existente en la tabla
     * int_proceso.
     *
     * @param proceso Objeto {@link int_proceso} con el ID y los nuevos datos.
     * @return int con el numero de filas actualizadas en la base de datos.
     */
    public int editarProceso(int_proceso proceso);

    /**
     * Borra un registro de la tabla int_proceso segun su identificador
     * primario.
     *
     * @param id Integer con el ID del proceso a eliminar.
     * @return int con el numero de filas eliminadas.
     */
    public int borrarProceso(int id);

    // =========================================================================
    // METODOS ESPECIFICOS TAREA #14 (HU-03 / ENTREGABLES 3.f y 3.g)
    // =========================================================================
    /**
     * Consulta de forma rapida y ligera el valor actual del campo
     * <code>tiempo_muestreo</code> para un proceso especifico sin transferir
     * las columnas BLOB de archivos o descripciones.
     * <p>
     * Utilizado por el programa puente RS232-SQL (Entregable 3.g) y por la GUI
     * para sincronizar el periodo de muestreo con el sistema embebido.
     * </p>
     *
     * @param idProceso Integer con el ID del proceso en la tabla int_proceso.
     * @return float con el tiempo de muestreo vigente, o -1.0f si el proceso no
     * existe.
     */
    public float consultarTiempoMuestreo(int idProceso);

    /**
     * Actualiza de forma rapida y directa el campo <code>tiempo_muestreo</code>
     * de un proceso especifico identificado por su ID.
     * <p>
     * Utilizado por la interfaz grafica GUI (Entregable 3.f) cuando el usuario
     * modifica el tiempo de muestreo para que posteriormente sea leido y
     * enviado al sistema embebido.
     * </p>
     *
     * @param idProceso Integer con el ID del proceso a modificar.
     * @param nuevoTiempoMuestreo float con el nuevo valor de tiempo de muestreo
     * (debe ser mayor a 0).
     * @return int con el numero de filas afectadas (1 si se actualizo
     * correctamente, 0 si fallo).
     */
    public int actualizarTiempoMuestreo(int idProceso, float nuevoTiempoMuestreo);
}
