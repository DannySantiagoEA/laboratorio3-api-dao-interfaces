package co.edu.univalle.ir.entity;

/**
 * La clase <b>int_proceso_vars</b> representa la entidad de dominio de la tabla
 * <code>int_proceso_vars</code> de la base de datos del Laboratorio Virtual.
 * <p>
 * Describe cada variable de entrada del proceso: las 8 entradas analogicas y
 * las 4 entradas digitales que envia el sistema embebido (Entregable 3.c). Sus
 * valores medidos se guardan en <code>int_proceso_vars_data</code>.
 * </p>
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/16
 * @see co.edu.univalle.ir.entity.int_proceso
 */
public class int_proceso_vars {

    /**
     * El identificador primario de la variable.
     */
    private int id;

    /**
     * El ID del proceso al que pertenece la variable (FK a int_proceso).
     */
    private int int_proceso_id;

    /**
     * El nombre de la variable.
     */
    private String nombre;

    /**
     * La descripcion de la variable.
     */
    private String descripcion;

    /**
     * El valor maximo de la variable.
     */
    private float max_2;

    /**
     * El valor minimo de la variable.
     */
    private float min;

    /**
     * Constructor por defecto sin parametros.
     */
    public int_proceso_vars() {
    }

    /**
     * Constructor completo con todos los campos de la tabla.
     *
     * @param id int con el identificador primario de la variable.
     * @param int_proceso_id int con el ID del proceso al que pertenece la
     * variable (FK a int_proceso).
     * @param nombre String con el nombre de la variable.
     * @param descripcion String con la descripcion de la variable.
     * @param max_2 float con el valor maximo de la variable.
     * @param min float con el valor minimo de la variable.
     */
    public int_proceso_vars(int id, int int_proceso_id, String nombre, String descripcion,
            float max_2, float min) {
        this.id = id;
        this.int_proceso_id = int_proceso_id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.max_2 = max_2;
        this.min = min;
    }

    /**
     * Obtiene el identificador primario de la variable.
     *
     * @return int con el identificador primario de la variable.
     */
    public int getId() {
        return id;
    }

    /**
     * Establece el identificador primario de la variable.
     *
     * @param id int con el identificador primario de la variable.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Obtiene el ID del proceso al que pertenece la variable (FK a
     * int_proceso).
     *
     * @return int con el ID del proceso al que pertenece la variable (FK a
     * int_proceso).
     */
    public int getInt_proceso_id() {
        return int_proceso_id;
    }

    /**
     * Establece el ID del proceso al que pertenece la variable (FK a
     * int_proceso).
     *
     * @param int_proceso_id int con el ID del proceso al que pertenece la
     * variable (FK a int_proceso).
     */
    public void setInt_proceso_id(int int_proceso_id) {
        this.int_proceso_id = int_proceso_id;
    }

    /**
     * Obtiene el nombre de la variable.
     *
     * @return String con el nombre de la variable.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el nombre de la variable.
     *
     * @param nombre String con el nombre de la variable.
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Obtiene la descripcion de la variable.
     *
     * @return String con la descripcion de la variable.
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Establece la descripcion de la variable.
     *
     * @param descripcion String con la descripcion de la variable.
     */
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * Obtiene el valor maximo de la variable.
     *
     * @return float con el valor maximo de la variable.
     */
    public float getMax_2() {
        return max_2;
    }

    /**
     * Establece el valor maximo de la variable.
     *
     * @param max_2 float con el valor maximo de la variable.
     */
    public void setMax_2(float max_2) {
        this.max_2 = max_2;
    }

    /**
     * Obtiene el valor minimo de la variable.
     *
     * @return float con el valor minimo de la variable.
     */
    public float getMin() {
        return min;
    }

    /**
     * Establece el valor minimo de la variable.
     *
     * @param min float con el valor minimo de la variable.
     */
    public void setMin(float min) {
        this.min = min;
    }

    /**
     * Retorna una representacion en cadena del registro.
     *
     * @return String con los valores principales del registro.
     */
    @Override
    public String toString() {
        return "int_proceso_vars{"
                + "id=" + id
                + ", proceso_id=" + int_proceso_id
                + ", nombre='" + nombre + '\''
                + ", min=" + min
                + ", max_2=" + max_2
                + '}';
    }
}
