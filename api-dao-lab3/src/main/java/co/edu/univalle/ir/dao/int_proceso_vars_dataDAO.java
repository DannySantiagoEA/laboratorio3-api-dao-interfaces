package co.edu.univalle.ir.dao;

import co.edu.univalle.ir.entity.int_proceso_vars_data;
import java.sql.Date;
import java.sql.Time;
import java.util.List;

/**
 * La interfaz <b>int_proceso_vars_dataDAO</b> especifica los metodos de
 * insercion, seleccion y borrado para gestionar las muestras de las 8 variables
 * de entrada analogicas y las 4 variables de entrada digitales en la tabla
 * <code>int_proceso_vars_data</code> (Requerimiento 4, Tareas #18 y #39,
 * Entregables 3.d y 3.g).
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.1 $ 2026/11/24
 * @see co.edu.univalle.ir.entity.int_proceso_vars_data
 */
public interface int_proceso_vars_dataDAO {

    public int insertarDatoVariable(int_proceso_vars_data dato);

    public int insertarDatoVariable(int int_proceso_vars_id, float valor, float tiempo,
            Date fecha, Time hora);

    public int insertarLoteDatosVariables(List<int_proceso_vars_data> muestras);

    public int insertarMuestra12Canales(float[] analogicas8, int[] digitales4,
            float tiempo, int idVarBaseAnalogica);

    public int_proceso_vars_data seleccionarPorId(int id);

    public List<int_proceso_vars_data> seleccionarPorVariable(int int_proceso_vars_id);

    public List<int_proceso_vars_data> seleccionarUltimosPorVariable(int int_proceso_vars_id, int limite);

    public int_proceso_vars_data seleccionarUltimoDatoPorVariable(int int_proceso_vars_id);

    public List<int_proceso_vars_data> seleccionarTodos();

    /**
     * Selecciona, en orden cronologico, las muestras de varias variables cuyo
     * ID es mayor que el ultimo ID ya leido (consulta incremental de ventana
     * deslizante - Tarea #39).
     *
     * @param idsVariables Arreglo con los ID de int_proceso_vars a consultar.
     * @param ultimoIdLeido ID de la ultima muestra ya recibida (0 al inicio).
     * @param limite Maximo de filas por consulta.
     * @return Lista ordenada por ID ascendente (vacia si no hay datos nuevos).
     */
    public List<int_proceso_vars_data> seleccionarPosterioresA(int[] idsVariables, int ultimoIdLeido, int limite);

    public int borrarPorId(int id);

    public int borrarPorVariable(int int_proceso_vars_id);

    public int borrarTodos();
}
