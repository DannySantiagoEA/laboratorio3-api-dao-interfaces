package co.edu.univalle.ir.entity;

/**
 * La clase <b>int_usuarios_tipo</b> representa la entidad de dominio de la
 * tabla <code>int_usuarios_tipo</code> de la base de datos del Laboratorio
 * Virtual.
 * <p>
 * Define los perfiles de usuario (por ejemplo, Estudiante o Administrador) a
 * los que se asocian los registros de <code>int_usuarios</code> y los
 * privilegios de <code>int_usuarios_priv</code>.
 * </p>
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/15
 */
public class int_usuarios_tipo {

    /**
     * Identificador unico del tipo de usuario (PK).
     */
    private int id;

    /**
     * Nombre del perfil de usuario.
     */
    private String nombre;

    /**
     * Descripcion del perfil de usuario.
     */
    private String descripcion;

    /**
     * Constructor por defecto sin parametros.
     */
    public int_usuarios_tipo() {
    }

    /**
     * Constructor sin ID, util para insertar un nuevo usuario 
     * 
     * @param nombre String con el nombre del perfil.
     * @param descripcion String con la descripcion del perfil.
     */
    public int_usuarios_tipo(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    /**
     * Constructor completo con todos los campos de la tabla.
     *
     * @param id Integer con el identificador primario del tipo.
     * @param nombre String con el nombre del perfil.
     * @param descripcion String con la descripcion del perfil.
     */
    public int_usuarios_tipo(int id, String nombre, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    /**
     * Obtiene el identificador primario del tipo de usuario.
     *
     * @return int con el ID del tipo.
     */
    public int getId() {
        return id;
    }

    /**
     * Establece el identificador primario del tipo de usuario.
     *
     * @param id int con el nuevo ID.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Obtiene el nombre del perfil.
     *
     * @return String con el nombre del perfil.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el nombre del perfil.
     *
     * @param nombre String con el nombre del perfil.
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Obtiene la descripcion del perfil.
     *
     * @return String con la descripcion.
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Establece la descripcion del perfil.
     *
     * @param descripcion String con la descripcion.
     */
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * Retorna una representacion en cadena del tipo de usuario.
     *
     * @return String con el ID y el nombre del perfil.
     */
    @Override
    public String toString() {
        return "int_usuarios_tipo{id=" + id + ", nombre='" + nombre + "'}";
    }
}