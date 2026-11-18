package co.edu.univalle.ir.dao;

import co.edu.univalle.ir.entity.int_usuarios;
import java.util.List;

/**
 * La interfaz <b>int_usuariosDAO</b> define el contrato de acceso a datos para
 * la gestion de registros de la tabla <code>int_usuarios</code> (Requerimiento
 * 2): insercion, edicion, borrado y seleccion filtrada por diversos campos de
 * la tabla, ademas de la validacion de credenciales que usa la interfaz de
 * login del aplicativo (Entregable 3.a).
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/15
 * @see co.edu.univalle.ir.entity.int_usuarios
 */
public interface int_usuariosDAO {

    // =========================================================================
    // INSERCION
    // =========================================================================
    /**
     * Inserta un nuevo usuario a partir de un objeto entidad. Si la insercion
     * es exitosa, el ID generado por MySQL queda asignado en el objeto.
     *
     * @param usuario Objeto {@link int_usuarios} con los datos a registrar.
     * @return int con el numero de filas insertadas (1 si fue exitoso, 0 en
     * caso contrario).
     * @exception IllegalArgumentException Si faltan nombres, apellidos o clave,
     * o si la clave supera 30 caracteres.
     */
    public int insertarUsuario(int_usuarios usuario);

    /**
     * Inserta un nuevo usuario recibiendo cada campo por separado.
     *
     * @param nombres String con los nombres del usuario.
     * @param apellidos String con los apellidos del usuario.
     * @param email String con el correo electronico del usuario.
     * @param clave String con la clave de acceso (maximo 30 caracteres).
     * @param int_usuarios_tipo_id Integer con el perfil del usuario en la tabla
     * int_usuarios_tipo.
     * @return int con el numero de filas insertadas.
     * @exception IllegalArgumentException Si algun dato obligatorio es
     * invalido.
     */
    public int insertarUsuario(String nombres, String apellidos, String email, String clave,
            int int_usuarios_tipo_id);

    // =========================================================================
    // SELECCION FILTRADA POR DIVERSOS CAMPOS
    // =========================================================================
    /**
     * Selecciona un usuario por su identificador primario.
     *
     * @param id Integer con el ID del usuario.
     * @return Objeto {@link int_usuarios} encontrado, o null si no existe.
     */
    public int_usuarios seleccionarPorId(int id);

    /**
     * Selecciona todos los usuarios registrados.
     *
     * @return Lista de objetos {@link int_usuarios} ordenada por ID.
     */
    public List<int_usuarios> seleccionarTodos();

    /**
     * Selecciona los usuarios que pertenecen a un perfil.
     *
     * @param int_usuarios_tipo_id Integer con el ID del perfil en
     * int_usuarios_tipo.
     * @return Lista de objetos {@link int_usuarios} del perfil.
     */
    public List<int_usuarios> seleccionarPorTipo(int int_usuarios_tipo_id);

    /**
     * Selecciona los usuarios cuyos nombres contienen el texto indicado (no
     * distingue mayusculas de minusculas).
     *
     * @param nombres String con el texto a buscar en la columna nombres.
     * @return Lista de objetos {@link int_usuarios} que coinciden.
     */
    public List<int_usuarios> seleccionarPorNombres(String nombres);

    /**
     * Selecciona los usuarios cuyos apellidos contienen el texto indicado (no
     * distingue mayusculas de minusculas).
     *
     * @param apellidos String con el texto a buscar en la columna apellidos.
     * @return Lista de objetos {@link int_usuarios} que coinciden.
     */
    public List<int_usuarios> seleccionarPorApellidos(String apellidos);

    /**
     * Selecciona el usuario con un correo electronico exacto.
     *
     * @param email String con el correo electronico.
     * @return Objeto {@link int_usuarios} encontrado, o null si no existe.
     */
    public int_usuarios seleccionarPorEmail(String email);

    /**
     * Selecciona usuarios combinando varios filtros. Los parametros en null (o
     * texto vacio) no se tienen en cuenta; los demas se combinan con AND. Los
     * filtros de texto buscan coincidencias parciales.
     *
     * @param id Integer con el ID exacto, o null para no filtrar.
     * @param int_usuarios_tipo_id Integer con el perfil, o null para no
     * filtrar.
     * @param nombres String con parte de los nombres, o null para no filtrar.
     * @param apellidos String con parte de los apellidos, o null para no
     * filtrar.
     * @param email String con parte del email, o null para no filtrar.
     * @return Lista de objetos {@link int_usuarios} que cumplen todos los
     * filtros (todos los usuarios si todos los filtros son null).
     */
    public List<int_usuarios> seleccionarPorCampos(Integer id, Integer int_usuarios_tipo_id,
            String nombres, String apellidos, String email);

    /**
     * Valida las credenciales de un usuario para la interfaz de login La clave
     * distingue mayusculas de minusculas.
     *
     * @param email String con el correo electronico del usuario.
     * @param clave String con la clave de acceso.
     * @return Objeto {@link int_usuarios} autenticado, o null si el email no
     * existe o la clave no coincide.
     */
    public int_usuarios validarCredenciales(String email, String clave);

    // =========================================================================
    // EDICION
    // =========================================================================
    /**
     * Edita todos los campos de un usuario existente identificado por su ID.
     *
     * @param usuario Objeto {@link int_usuarios} con el ID y los nuevos datos.
     * @return int con el numero de filas actualizadas.
     * @exception IllegalArgumentException Si algun dato obligatorio es
     * invalido.
     */
    public int editarUsuario(int_usuarios usuario);

    /**
     * Cambia unicamente la clave de un usuario.
     *
     * @param id Integer con el ID del usuario.
     * @param nuevaClave String con la nueva clave (maximo 30 caracteres).
     * @return int con el numero de filas actualizadas.
     * @exception IllegalArgumentException Si la clave esta vacia o supera 30
     * caracteres.
     */
    public int cambiarClave(int id, String nuevaClave);

    // =========================================================================
    // BORRADO
    // =========================================================================
    /**
     * Borra un usuario por su identificador primario. Por la restriccion ON
     * DELETE CASCADE tambien se borran sus programaciones en
     * int_usuarios_procesos.
     *
     * @param id Integer con el ID del usuario a eliminar.
     * @return int con el numero de filas eliminadas.
     */
    public int borrarUsuario(int id);
}
