package co.edu.univalle.ir.entity;

/**
 * La clase <b>int_proceso_control</b> representa la entidad de dominio de la tabla
 * <code>int_proceso_control</code> de la base de datos del Laboratorio Virtual.
 * <p>
 * Guarda la configuracion de un control del proceso con hasta cuatro
 * parametros numericos (por ejemplo, ganancias o limites de operacion).
 * </p>
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/16
 * @see co.edu.univalle.ir.entity.int_proceso
 */
public class int_proceso_control {

    /**
     * El identificador primario del control.
     */
    private int id;

    /**
     * El ID del proceso al que pertenece el control (FK a int_proceso).
     */
    private int int_proceso_id;

    /**
     * El nombre del control.
     */
    private String nombre;

    /**
     * La descripcion del control.
     */
    private String descripcion;

    /**
     * El parametro 1 del control.
     */
    private float parametro1;

    /**
     * El parametro 2 del control.
     */
    private float parametro2;

    /**
     * El parametro 3 del control.
     */
    private float parametro3;

    /**
     * El parametro 4 del control.
     */
    private float parametro4;

    /**
     * Constructor por defecto sin parametros.
     */
    public int_proceso_control() {
    }

    /**
     * Constructor completo con todos los campos de la tabla.
     *
     * @param id int con el identificador primario del control.
     * @param int_proceso_id int con el ID del proceso al que pertenece el control (FK a int_proceso).
     * @param nombre String con el nombre del control.
     * @param descripcion String con la descripcion del control.
     * @param parametro1 float con el parametro 1 del control.
     * @param parametro2 float con el parametro 2 del control.
     * @param parametro3 float con el parametro 3 del control.
     * @param parametro4 float con el parametro 4 del control.
     */
    public int_proceso_control(int id, int int_proceso_id, String nombre, String descripcion,
            float parametro1, float parametro2, float parametro3, float parametro4) {
        this.id = id;
        this.int_proceso_id = int_proceso_id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.parametro1 = parametro1;
        this.parametro2 = parametro2;
        this.parametro3 = parametro3;
        this.parametro4 = parametro4;
    }

    /**
     * Obtiene el identificador primario del control.
     *
     * @return int con el identificador primario del control.
     */
    public int getId() {
        return id;
    }

    /**
     * Establece el identificador primario del control.
     *
     * @param id int con el identificador primario del control.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Obtiene el ID del proceso al que pertenece el control (FK a int_proceso).
     *
     * @return int con el ID del proceso al que pertenece el control (FK a int_proceso).
     */
    public int getInt_proceso_id() {
        return int_proceso_id;
    }

    /**
     * Establece el ID del proceso al que pertenece el control (FK a int_proceso).
     *
     * @param int_proceso_id int con el ID del proceso al que pertenece el control (FK a int_proceso).
     */
    public void setInt_proceso_id(int int_proceso_id) {
        this.int_proceso_id = int_proceso_id;
    }

    /**
     * Obtiene el nombre del control.
     *
     * @return String con el nombre del control.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el nombre del control.
     *
     * @param nombre String con el nombre del control.
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Obtiene la descripcion del control.
     *
     * @return String con la descripcion del control.
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Establece la descripcion del control.
     *
     * @param descripcion String con la descripcion del control.
     */
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * Obtiene el parametro 1 del control.
     *
     * @return float con el parametro 1 del control.
     */
    public float getParametro1() {
        return parametro1;
    }

    /**
     * Establece el parametro 1 del control.
     *
     * @param parametro1 float con el parametro 1 del control.
     */
    public void setParametro1(float parametro1) {
        this.parametro1 = parametro1;
    }

    /**
     * Obtiene el parametro 2 del control.
     *
     * @return float con el parametro 2 del control.
     */
    public float getParametro2() {
        return parametro2;
    }

    /**
     * Establece el parametro 2 del control.
     *
     * @param parametro2 float con el parametro 2 del control.
     */
    public void setParametro2(float parametro2) {
        this.parametro2 = parametro2;
    }

    /**
     * Obtiene el parametro 3 del control.
     *
     * @return float con el parametro 3 del control.
     */
    public float getParametro3() {
        return parametro3;
    }

    /**
     * Establece el parametro 3 del control.
     *
     * @param parametro3 float con el parametro 3 del control.
     */
    public void setParametro3(float parametro3) {
        this.parametro3 = parametro3;
    }

    /**
     * Obtiene el parametro 4 del control.
     *
     * @return float con el parametro 4 del control.
     */
    public float getParametro4() {
        return parametro4;
    }

    /**
     * Establece el parametro 4 del control.
     *
     * @param parametro4 float con el parametro 4 del control.
     */
    public void setParametro4(float parametro4) {
        this.parametro4 = parametro4;
    }

    /**
     * Retorna una representacion en cadena del registro.
     *
     * @return String con los valores principales del registro.
     */
    @Override
    public String toString() {
        return "int_proceso_control{"
                + "id=" + id
                + ", proceso_id=" + int_proceso_id
                + ", nombre='" + nombre + '\''
                + ", parametro1=" + parametro1
                + ", parametro2=" + parametro2
                + ", parametro3=" + parametro3
                + ", parametro4=" + parametro4
                + '}';
    }
}