package co.edu.univalle.ir.entity;

/**
 * La clase <b>int_usuarios</b> representa la entidad de dominio de la tabla
 * <code>int_usuarios</code> de la base de datos del Laboratorio Virtual.
 * <p>
 * Contiene los datos personales y las credenciales de acceso (email y clave) de
 * cada usuario, asi como el perfil al que pertenece en
 * <code>int_usuarios_tipo</code>. Es la entidad que usa la interfaz de login
 * del aplicativo (Entregable 3.a).
 * </p>
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/15
 * @see co.edu.univalle.ir.entity.int_usuarios_tipo
 */
public class int_usuarios {

    /**
     * Longitud maxima de la clave definida en la tabla (VARCHAR(30)).
     */
    public static final int LONGITUD_MAX_CLAVE = 30;

    /**
     * Identificador unico del usuario (PK).
     */
    private int id;

    /**
     * Identificador del perfil del usuario en int_usuarios_tipo (FK).
     */
    private int int_usuarios_tipo_id;

    /**
     * Nombres del usuario.
     */
    private String nombres;

    /**
     * Apellidos del usuario.
     */
    private String apellidos;

    /**
     * Correo electronico del usuario (usado como nombre de usuario en el
     * login).
     */
    private String email;

    /**
     * Clave de acceso del usuario.
     */
    private String clave;

    /**
     * Constructor por defecto sin parametros.
     */
    public int_usuarios() {
    }

    /**
     * Constructor sin ID, util para insertar un nuevo usuario
     *
     * @param nombres String con los nombres del usuario.
     * @param apellidos String con los apellidos del usuario.
     * @param email String con el correo electronico del usuario.
     * @param clave String con la clave de acceso (maximo 30 caracteres).
     * @param int_usuarios_tipo_id Integer con el perfil del usuario en la tabla
     * int_usuarios_tipo.
     */
    public int_usuarios(String nombres, String apellidos, String email, String clave,
            int int_usuarios_tipo_id) {
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.email = email;
        this.clave = clave;
        this.int_usuarios_tipo_id = int_usuarios_tipo_id;
    }

    /**
     * Constructor completo con todos los campos de la tabla int_usuarios.
     *
     * @param id Integer con el identificador primario del usuario.
     * @param int_usuarios_tipo_id Integer con el perfil del usuario (FK).
     * @param nombres String con los nombres del usuario.
     * @param apellidos String con los apellidos del usuario.
     * @param email String con el correo electronico del usuario.
     * @param clave String con la clave de acceso.
     */
    public int_usuarios(int id, int int_usuarios_tipo_id, String nombres, String apellidos,
            String email, String clave) {
        this.id = id;
        this.int_usuarios_tipo_id = int_usuarios_tipo_id;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.email = email;
        this.clave = clave;
    }

    /**
     * Obtiene el identificador primario del usuario.
     *
     * @return int con el ID del usuario.
     */
    public int getId() {
        return id;
    }

    /**
     * Establece el identificador primario del usuario.
     *
     * @param id int con el nuevo ID.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Obtiene el identificador del perfil del usuario.
     *
     * @return int con el ID del tipo de usuario.
     */
    public int getInt_usuarios_tipo_id() {
        return int_usuarios_tipo_id;
    }

    /**
     * Establece el identificador del perfil del usuario.
     *
     * @param int_usuarios_tipo_id int con el ID del tipo de usuario.
     */
    public void setInt_usuarios_tipo_id(int int_usuarios_tipo_id) {
        this.int_usuarios_tipo_id = int_usuarios_tipo_id;
    }

    /**
     * Obtiene los nombres del usuario.
     *
     * @return String con los nombres.
     */
    public String getNombres() {
        return nombres;
    }

    /**
     * Establece los nombres del usuario.
     *
     * @param nombres String con los nombres.
     */
    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    /**
     * Obtiene los apellidos del usuario.
     *
     * @return String con los apellidos.
     */
    public String getApellidos() {
        return apellidos;
    }

    /**
     * Establece los apellidos del usuario.
     *
     * @param apellidos String con los apellidos.
     */
    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    /**
     * Obtiene el correo electronico del usuario.
     *
     * @return String con el email.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Establece el correo electronico del usuario.
     *
     * @param email String con el email.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Obtiene la clave de acceso del usuario.
     *
     * @return String con la clave.
     */
    public String getClave() {
        return clave;
    }

    /**
     * Establece la clave de acceso del usuario.
     *
     * @param clave String con la clave (maximo 30 caracteres).
     */
    public void setClave(String clave) {
        this.clave = clave;
    }

    /**
     * Retorna una representacion en cadena del usuario. La clave no se incluye
     * para no exponerla en consola ni en registros.
     *
     * @return String con los datos principales del usuario.
     */
    @Override
    public String toString() {
        return "int_usuarios{"
                + "id=" + id
                + ", tipo_id=" + int_usuarios_tipo_id
                + ", nombres='" + nombres + '\''
                + ", apellidos='" + apellidos + '\''
                + ", email='" + email + '\''
                + '}';
    }
}
