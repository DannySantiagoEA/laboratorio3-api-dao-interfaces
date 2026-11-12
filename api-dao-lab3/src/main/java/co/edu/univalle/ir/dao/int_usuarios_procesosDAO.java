package co.edu.univalle.ir.dao;

import co.edu.univalle.ir.entity.int_usuarios_procesos;
import java.sql.Date;
import java.sql.Time;
import java.util.List;

/**
 * La interfaz <b>int_usuarios_procesosDAO</b> especifica los metodos CRUD
 * (insercion, seleccion, edicion y borrado) para gestionar la programacion del
 * uso de un proceso particular por un usuario especifico en la tabla
 * <code>int_usuarios_procesos</code> (Requerimiento 2).
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/12
 * @see co.edu.univalle.ir.entity.int_usuarios_procesos
 */
public interface int_usuarios_procesosDAO {

    /**
     * Inserta una nueva programacion de proceso para un usuario utilizando un
     * objeto entidad.
     *
     * @param programacion Objeto {@link int_usuarios_procesos} con los datos a
     * insertar.
     * @return int con el numero de filas insertadas (1 si fue exitoso, 0 en
     * caso contrario).
     */
    public int insertarProgramacion(int_usuarios_procesos programacion);

    /**
     * Inserta una nueva programacion de proceso para un usuario especificando
     * cada campo por parametro.
     *
     * @param int_usuarios_id Integer correspondiente al ID del usuario en
     * int_usuarios.
     * @param int_proceso_id Integer correspondiente al ID del proceso en
     * int_proceso.
     * @param fecha_inicio Objeto Date con la fecha de inicio programada.
     * @param fecha_fin Objeto Date con la fecha de finalizacion programada.
     * @param hora_inicio Objeto Time con la hora de inicio programada.
     * @param hora_fin Objeto Time con la hora de finalizacion programada.
     * @return int con el numero de filas insertadas en la tabla.
     */
    public int insertarProgramacion(int int_usuarios_id, int int_proceso_id,
            Date fecha_inicio, Date fecha_fin, Time hora_inicio, Time hora_fin);

    /**
     * Selecciona un registro de programacion especifico buscando por el ID del
     * usuario y el ID del proceso.
     *
     * @param int_usuarios_id Integer con el ID del usuario.
     * @param int_proceso_id Integer con el ID del proceso.
     * @return Objeto {@link int_usuarios_procesos} encontrado, o null si no
     * existe.
     */
    public int_usuarios_procesos seleccionarPorUsuarioYProceso(int int_usuarios_id, int int_proceso_id);

    /**
     * Selecciona todas las programaciones de procesos asignadas a un usuario
     * especifico.
     *
     * @param int_usuarios_id Integer con el ID del usuario a consultar.
     * @return Lista de objetos {@link int_usuarios_procesos} asociados al
     * usuario.
     */
    public List<int_usuarios_procesos> seleccionarPorUsuario(int int_usuarios_id);

    /**
     * Selecciona todas las programaciones de usuarios asociadas a un proceso
     * particular.
     *
     * @param int_proceso_id Integer con el ID del proceso a consultar.
     * @return Lista de objetos {@link int_usuarios_procesos} asociados al
     * proceso.
     */
    public List<int_usuarios_procesos> seleccionarPorProceso(int int_proceso_id);

    /**
     * Selecciona todos los registros de programacion almacenados en la tabla
     * int_usuarios_procesos.
     *
     * @return Lista completa de objetos {@link int_usuarios_procesos}.
     */
    public List<int_usuarios_procesos> seleccionarTodos();

    /**
     * Edita (actualiza) las fechas y horas de una programacion existente en la
     * tabla int_usuarios_procesos mediante un objeto entidad.
     *
     * @param programacion Objeto {@link int_usuarios_procesos} con los datos
     * actualizados.
     * @return int con el numero de filas modificadas en la base de datos.
     */
    public int editarProgramacion(int_usuarios_procesos programacion);

    /**
     * Edita (actualiza) las fechas y horas de la programacion de un proceso
     * particular para un usuario especifico.
     *
     * @param int_usuarios_id Integer con el ID del usuario (clave de busqueda).
     * @param int_proceso_id Integer con el ID del proceso (clave de busqueda).
     * @param nueva_fecha_inicio Objeto Date con la nueva fecha de inicio.
     * @param nueva_fecha_fin Objeto Date con la nueva fecha de finalizacion.
     * @param nueva_hora_inicio Objeto Time con la nueva hora de inicio.
     * @param nueva_hora_fin Objeto Time con la nueva hora de finalizacion.
     * @return int con el numero de filas actualizadas.
     */
    public int editarProgramacion(int int_usuarios_id, int int_proceso_id,
            Date nueva_fecha_inicio, Date nueva_fecha_fin,
            Time nueva_hora_inicio, Time nueva_hora_fin);

    /**
     * Borra la programacion de un proceso particular para un usuario
     * especifico.
     *
     * @param int_usuarios_id Integer con el ID del usuario.
     * @param int_proceso_id Integer con el ID del proceso.
     * @return int con el numero de filas eliminadas de la tabla.
     */
    public int borrarProgramacion(int int_usuarios_id, int int_proceso_id);

    /**
     * Borra todas las programaciones asociadas a un usuario especifico.
     *
     * @param int_usuarios_id Integer con el ID del usuario.
     * @return int con el numero de registros eliminados.
     */
    public int borrarPorUsuario(int int_usuarios_id);
}
