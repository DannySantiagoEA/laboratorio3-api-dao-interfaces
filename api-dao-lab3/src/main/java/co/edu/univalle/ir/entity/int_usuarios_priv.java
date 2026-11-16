package co.edu.univalle.ir.entity;

/**
 * La clase <b>int_usuarios_priv</b> representa la entidad de dominio de la
 * tabla <code>int_usuarios_priv</code> de la base de datos del Laboratorio
 * Virtual.
 * <p>
 * Guarda los privilegios de cada perfil de usuario. En la tabla cada
 * privilegio es un ENUM('0','1'); en Java se maneja como <code>boolean</code>
 * (true = '1', false = '0').
 * </p>
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/15
 * @see co.edu.univalle.ir.entity.int_usuarios_tipo
 */
public class int_usuarios_priv {

    /**
     * Identificador unico del registro de privilegios (PK).
     */
    private int id;

    /**
     * Identificador del perfil al que aplican los privilegios (FK).
     */
    private int int_usuarios_tipo_id;

    /**
     * Permiso para controlar el proceso (enviar salidas digitales).
     */
    private boolean control;

    /**
     * Permiso para cambiar la configuracion (por ejemplo, el tiempo de
     * muestreo).
     */
    private boolean configuracion;

    /**
     * Permiso para descargar los datos almacenados.
     */
    private boolean descarga_datos;

    /**
     * Permiso para monitorear (graficar) las variables del proceso.
     */
    private boolean monitor;

    /**
     * Permiso para enviar datos al sistema embebido.
     */
    private boolean envio_datos;

    /**
     * Constructor por defecto sin parametros (todos los privilegios en false).
     */
    public int_usuarios_priv() {
    }

    /**
     * Constructor sin ID, util para insertar privilegios nuevos.
     *
     * @param int_usuarios_tipo_id Integer con el perfil al que aplican.
     * @param control boolean con el permiso de control.
     * @param configuracion boolean con el permiso de configuracion.
     * @param descarga_datos boolean con el permiso de descarga de datos.
     * @param monitor boolean con el permiso de monitoreo.
     * @param envio_datos boolean con el permiso de envio de datos.
     */
    public int_usuarios_priv(int int_usuarios_tipo_id, boolean control, boolean configuracion,
            boolean descarga_datos, boolean monitor, boolean envio_datos) {
        this.int_usuarios_tipo_id = int_usuarios_tipo_id;
        this.control = control;
        this.configuracion = configuracion;
        this.descarga_datos = descarga_datos;
        this.monitor = monitor;
        this.envio_datos = envio_datos;
    }

    /**
     * Constructor completo con todos los campos de la tabla.
     *
     * @param id Integer con el identificador primario del registro.
     * @param int_usuarios_tipo_id Integer con el perfil al que aplican.
     * @param control boolean con el permiso de control.
     * @param configuracion boolean con el permiso de configuracion.
     * @param descarga_datos boolean con el permiso de descarga de datos.
     * @param monitor boolean con el permiso de monitoreo.
     * @param envio_datos boolean con el permiso de envio de datos.
     */
    public int_usuarios_priv(int id, int int_usuarios_tipo_id, boolean control,
            boolean configuracion, boolean descarga_datos, boolean monitor, boolean envio_datos) {
        this(int_usuarios_tipo_id, control, configuracion, descarga_datos, monitor, envio_datos);
        this.id = id;
    }

    /**
     * Convierte un valor ENUM('0','1') leido de MySQL en boolean.
     *
     * @param valorEnum String con el valor de la columna ('0' o '1').
     * @return boolean true si el valor es '1'; false en cualquier otro caso.
     */
    public static boolean desdeEnum(String valorEnum) {
        return "1".equals(valorEnum);
    }

    /**
     * Convierte un boolean en el valor ENUM('0','1') que espera MySQL.
     *
     * @param valor boolean con el privilegio.
     * @return String "1" si es true; "0" si es false.
     */
    public static String aEnum(boolean valor) {
        return valor ? "1" : "0";
    }

    /**
     * Obtiene el identificador primario del registro.
     *
     * @return int con el ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Establece el identificador primario del registro.
     *
     * @param id int con el nuevo ID.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Obtiene el perfil al que aplican los privilegios.
     *
     * @return int con el ID del tipo de usuario.
     */
    public int getInt_usuarios_tipo_id() {
        return int_usuarios_tipo_id;
    }

    /**
     * Establece el perfil al que aplican los privilegios.
     *
     * @param int_usuarios_tipo_id int con el ID del tipo de usuario.
     */
    public void setInt_usuarios_tipo_id(int int_usuarios_tipo_id) {
        this.int_usuarios_tipo_id = int_usuarios_tipo_id;
    }

    /**
     * Indica si el perfil tiene permiso de control.
     *
     * @return boolean con el permiso de control.
     */
    public boolean isControl() {
        return control;
    }

    /**
     * Establece el permiso de control.
     *
     * @param control boolean con el permiso.
     */
    public void setControl(boolean control) {
        this.control = control;
    }

    /**
     * Indica si el perfil tiene permiso de configuracion.
     *
     * @return boolean con el permiso de configuracion.
     */
    public boolean isConfiguracion() {
        return configuracion;
    }

    /**
     * Establece el permiso de configuracion.
     *
     * @param configuracion boolean con el permiso.
     */
    public void setConfiguracion(boolean configuracion) {
        this.configuracion = configuracion;
    }

    /**
     * Indica si el perfil tiene permiso de descarga de datos.
     *
     * @return boolean con el permiso de descarga.
     */
    public boolean isDescarga_datos() {
        return descarga_datos;
    }

    /**
     * Establece el permiso de descarga de datos.
     *
     * @param descarga_datos boolean con el permiso.
     */
    public void setDescarga_datos(boolean descarga_datos) {
        this.descarga_datos = descarga_datos;
    }

    /**
     * Indica si el perfil tiene permiso de monitoreo.
     *
     * @return boolean con el permiso de monitoreo.
     */
    public boolean isMonitor() {
        return monitor;
    }

    /**
     * Establece el permiso de monitoreo.
     *
     * @param monitor boolean con el permiso.
     */
    public void setMonitor(boolean monitor) {
        this.monitor = monitor;
    }

    /**
     * Indica si el perfil tiene permiso de envio de datos.
     *
     * @return boolean con el permiso de envio.
     */
    public boolean isEnvio_datos() {
        return envio_datos;
    }

    /**
     * Establece el permiso de envio de datos.
     *
     * @param envio_datos boolean con el permiso.
     */
    public void setEnvio_datos(boolean envio_datos) {
        this.envio_datos = envio_datos;
    }

    /**
     * Retorna una representacion en cadena de los privilegios.
     *
     * @return String con los cinco privilegios en formato 0/1.
     */
    @Override
    public String toString() {
        return "int_usuarios_priv{"
                + "id=" + id
                + ", tipo_id=" + int_usuarios_tipo_id
                + ", control=" + aEnum(control)
                + ", configuracion=" + aEnum(configuracion)
                + ", descarga_datos=" + aEnum(descarga_datos)
                + ", monitor=" + aEnum(monitor)
                + ", envio_datos=" + aEnum(envio_datos)
                + '}';
    }
}