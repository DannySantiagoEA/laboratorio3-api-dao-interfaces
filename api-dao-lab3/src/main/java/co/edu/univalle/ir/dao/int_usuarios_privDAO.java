package co.edu.univalle.ir.dao;

import co.edu.univalle.ir.entity.int_usuarios_priv;
import java.util.List;

/**
 * La interfaz <b>int_usuarios_privDAO</b> define el contrato de acceso a datos
 * para la tabla <code>int_usuarios_priv</code>, que guarda los privilegios
 * (control, configuracion, descarga de datos, monitor y envio de datos) de
 * cada perfil de usuario (Requerimiento 2).
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/15
 * @see co.edu.univalle.ir.entity.int_usuarios_priv
 */
public interface int_usuarios_privDAO {

    /**
     * Inserta un nuevo registro de privilegios para un perfil.
     *
     * @param privilegios Objeto {@link int_usuarios_priv} con los permisos a
     * registrar.
     * @return int con el numero de filas insertadas.
     */
    public int insertarPrivilegios(int_usuarios_priv privilegios);

    /**
     * Selecciona un registro de privilegios por su identificador primario.
     *
     * @param id Integer con el ID del registro.
     * @return Objeto {@link int_usuarios_priv} encontrado, o null si no
     * existe.
     */
    public int_usuarios_priv seleccionarPorId(int id);

    /**
     * Selecciona los privilegios asignados a un perfil de usuario. Es el
     * metodo que usa la GUI despues del login para habilitar o deshabilitar
     * sus controles.
     *
     * @param int_usuarios_tipo_id Integer con el ID del perfil en
     * int_usuarios_tipo.
     * @return Objeto {@link int_usuarios_priv} del perfil, o null si no tiene
     * privilegios registrados.
     */
    public int_usuarios_priv seleccionarPorTipoUsuario(int int_usuarios_tipo_id);

    /**
     * Selecciona todos los registros de privilegios.
     *
     * @return Lista de objetos {@link int_usuarios_priv} ordenada por ID.
     */
    public List<int_usuarios_priv> seleccionarTodos();

    /**
     * Edita los privilegios de un registro existente.
     *
     * @param privilegios Objeto {@link int_usuarios_priv} con el ID y los
     * nuevos permisos.
     * @return int con el numero de filas actualizadas.
     */
    public int editarPrivilegios(int_usuarios_priv privilegios);

    /**
     * Borra un registro de privilegios por su identificador primario.
     *
     * @param id Integer con el ID del registro a eliminar.
     * @return int con el numero de filas eliminadas.
     */
    public int borrarPrivilegios(int id);
}