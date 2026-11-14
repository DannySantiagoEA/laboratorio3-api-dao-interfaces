package co.edu.univalle.ir.entity;

/**
 * La clase <b>int_proceso</b> representa la entidad de dominio correspondiente
 * a la tabla <code>int_proceso</code> de la base de datos del Laboratorio
 * Virtual.
 * <p>
 * Contiene la informacion descriptiva del hardware de adquisicion, el tipo de
 * proceso y el parametro critico <code>tiempo_muestreo</code> compartido entre
 * la GUI y el aplicativo puente RS232-SQL.
 * </p>
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/14
 */
public class int_proceso {

    /**
     * Identificador unico del proceso en la tabla int_proceso (PK).
     */
    private int id;

    /**
     * Identificador del tipo de proceso asociado en int_proceso_tipo (FK).
     */
    private int int_proceso_tipo_id;

    /**
     * Nombre descriptivo del proceso de laboratorio.
     */
    private String nombre;

    /**
     * Descripcion general del proceso y del hardware empleado.
     */
    private String descripcion;

    /**
     * Tiempo de muestreo del proceso en milisegundos o segundos segun escala.
     */
    private float tiempo_muestreo;

    /**
     * Especificaciones tecnicas del proceso o ruta/contenido del archivo.
     */
    private String archivo_especificaciones;

    /**
     * Manual de usuario del proceso o ruta/contenido del archivo.
     */
    private String archivo_manual;

    /**
     * Constructor por defecto sin parametros.
     */
    public int_proceso() {
    }

    /**
     * Constructor sin ID (util para insertar un nuevo proceso cuando el ID es
     * autoincremental en MySQL).
     *
     * @param int_proceso_tipo_id Integer correspondiente al tipo de proceso en
     * int_proceso_tipo.
     * @param nombre String con el nombre del proceso.
     * @param descripcion String con la descripcion del hardware/proceso.
     * @param tiempo_muestreo float con el periodo o tiempo de muestreo inicial.
     * @param archivo_especificaciones String con las especificaciones tecnicas.
     * @param archivo_manual String con el manual de operacion del proceso.
     */
    public int_proceso(int int_proceso_tipo_id, String nombre, String descripcion,
            float tiempo_muestreo, String archivo_especificaciones, String archivo_manual) {
        this.int_proceso_tipo_id = int_proceso_tipo_id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tiempo_muestreo = tiempo_muestreo;
        this.archivo_especificaciones = archivo_especificaciones;
        this.archivo_manual = archivo_manual;
    }

    /**
     * Constructor completo con todos los campos de la tabla int_proceso.
     *
     * @param id Integer con el identificador primario del proceso.
     * @param int_proceso_tipo_id Integer correspondiente al tipo de proceso
     * (FK).
     * @param nombre String con el nombre del proceso.
     * @param descripcion String con la descripcion del proceso.
     * @param tiempo_muestreo float con el tiempo de muestreo configurado.
     * @param archivo_especificaciones String con las especificaciones tecnicas.
     * @param archivo_manual String con el manual del proceso.
     */
    public int_proceso(int id, int int_proceso_tipo_id, String nombre, String descripcion,
            float tiempo_muestreo, String archivo_especificaciones, String archivo_manual) {
        this.id = id;
        this.int_proceso_tipo_id = int_proceso_tipo_id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tiempo_muestreo = tiempo_muestreo;
        this.archivo_especificaciones = archivo_especificaciones;
        this.archivo_manual = archivo_manual;
    }

    /**
     * Obtiene el identificador primario del proceso.
     *
     * @return int con el ID del proceso.
     */
    public int getId() {
        return id;
    }

    /**
     * Establece el identificador primario del proceso.
     *
     * @param id int con el nuevo ID del proceso.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Obtiene el identificador del tipo de proceso (clave foranea).
     *
     * @return int con el ID del tipo de proceso.
     */
    public int getInt_proceso_tipo_id() {
        return int_proceso_tipo_id;
    }

    /**
     * Establece el identificador del tipo de proceso.
     *
     * @param int_proceso_tipo_id int con el ID del tipo de proceso.
     */
    public void setInt_proceso_tipo_id(int int_proceso_tipo_id) {
        this.int_proceso_tipo_id = int_proceso_tipo_id;
    }

    /**
     * Obtiene el nombre del proceso.
     *
     * @return String con el nombre del proceso.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el nombre del proceso.
     *
     * @param nombre String con el nombre del proceso.
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Obtiene la descripcion del proceso.
     *
     * @return String con la descripcion del proceso.
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Establece la descripcion del proceso.
     *
     * @param descripcion String con la descripcion del proceso.
     */
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * Obtiene el tiempo de muestreo configurado para el proceso.
     *
     * @return float con el tiempo de muestreo.
     */
    public float getTiempo_muestreo() {
        return tiempo_muestreo;
    }

    /**
     * Establece el tiempo de muestreo para el proceso.
     *
     * @param tiempo_muestreo float con el nuevo tiempo de muestreo.
     */
    public void setTiempo_muestreo(float tiempo_muestreo) {
        this.tiempo_muestreo = tiempo_muestreo;
    }

    /**
     * Obtiene el contenido o referencia del archivo de especificaciones.
     *
     * @return String con el archivo de especificaciones.
     */
    public String getArchivo_especificaciones() {
        return archivo_especificaciones;
    }

    /**
     * Establece el contenido o referencia del archivo de especificaciones.
     *
     * @param archivo_especificaciones String con las especificaciones.
     */
    public void setArchivo_especificaciones(String archivo_especificaciones) {
        this.archivo_especificaciones = archivo_especificaciones;
    }

    /**
     * Obtiene el contenido o referencia del archivo manual.
     *
     * @return String con el archivo manual.
     */
    public String getArchivo_manual() {
        return archivo_manual;
    }

    /**
     * Establece el contenido o referencia del archivo manual.
     *
     * @param archivo_manual String con el manual del proceso.
     */
    public void setArchivo_manual(String archivo_manual) {
        this.archivo_manual = archivo_manual;
    }

    /**
     * Retorna una representacion en cadena del objeto int_proceso.
     *
     * @return String con los valores principales del proceso.
     */
    @Override
    public String toString() {
        return "int_proceso{"
                + "id=" + id
                + ", tipo_id=" + int_proceso_tipo_id
                + ", nombre='" + nombre + '\''
                + ", tiempo_muestreo=" + tiempo_muestreo
                + ", descripcion='" + descripcion + '\''
                + '}';
    }
}
