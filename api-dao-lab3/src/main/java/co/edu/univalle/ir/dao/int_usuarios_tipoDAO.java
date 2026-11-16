package co.edu.univalle.ir.dao;

import co.edu.univalle.ir.entity.int_usuarios_tipo;
import java.util.List;

/**
 * La interfaz <b>int_usuarios_tipoDAO</b> define el contrato de acceso a datos
 * para la insercion, seleccion, edicion y borrado de los perfiles de usuario en
 * la tabla <code>int_usuarios_tipo</code> (Requerimiento 2).
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/15
 * @see co.edu.univalle.ir.entity.int_usuarios_tipo
 */
public interface int_usuarios_tipoDAO {

    /**
     * Inserta un nuevo perfil de usuario a partir de un objeto entidad. Si la
     * insercion es exitosa, el ID generado por MySQL queda asignado en el
     * objeto.
     *
     * @param tipo Objeto {@link int_usuarios_tipo} con los datos a registrar.
     * @return int con el numero de filas insertadas (1 si fue exitoso, 0 en
     * caso contrario).
     */
    public int insertarTipo(int_usuarios_tipo tipo);

    /**
     * Selecciona un perfil de usuario por su identificador primario.
     *
     * @param id Integer con el ID del perfil.
     * @return Objeto {@link int_usuarios_tipo} encontrado, o null si no
     * existe.
     */
    public int_usuarios_tipo seleccionarPorId(int id);

    /**
     * Selecciona un perfil de usuario por su nombre exacto
     *
     * @param nombre String con el nombre del perfil.
     * @return Objeto {@link int_usuarios_tipo} encontrado, o null si no
     * existe.
     */
    public int_usuarios_tipo seleccionarPorNombre(String nombre);

    /**
     * Selecciona todos los perfiles de usuario registrados.
     *
     * @return Lista de objetos {@link int_usuarios_tipo} ordenada por ID.
     */
    public List<int_usuarios_tipo> seleccionarTodos();

    /**
     * Edita el nombre y la descripcion de un perfil existente.
     *
     * @param tipo Objeto {@link int_usuarios_tipo} con el ID y los nuevos
     * datos.
     * @return int con el numero de filas actualizadas.
     */
    public int editarTipo(int_usuarios_tipo tipo);

    /**
     * Borra un perfil de usuario por su identificador primario. Por la
     * restriccion ON DELETE CASCADE tambien se borran sus usuarios y
     * privilegios asociados.
     *
     * @param id Integer con el ID del perfil a eliminar.
     * @return int con el numero de filas eliminadas.
     */
    public int borrarTipo(int id);
}