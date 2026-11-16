package co.edu.univalle.ir.entity;

import java.sql.Date;
import java.sql.Time;

/**
 * La clase <b>int_proceso_vars_data</b> representa la entidad de dominio para
 * la tabla <code>int_proceso_vars_data</code> de la base de datos del
 * Laboratorio Virtual.
 * <p>
 * Almacena las muestras historicas y en tiempo real de las 8 variables de
 * entrada analogicas (0.0 V a 5.0 V) y las 4 variables de entrada digitales
 * (estados logicos 0 o 1) enviadas por el sistema embebido (Entregables 3.d y
 * 3.g).
 * </p>
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/15
 */
public class int_proceso_vars_data {

    /**
     * Identificador unico del registro de muestra en int_proceso_vars_data
     * (PK).
     */
    private int id;

    /**
     * Identificador de la variable de proceso asociada en int_proceso_vars (FK:
     * 1..8 ADC, 9..12 Dig).
     */
    private int int_proceso_vars_id;

    /**
     * Valor numerico de la muestra (Voltaje 0.0-5.0V para analogicas o 0.0/1.0
     * para digitales).
     */
    private float valor;

    /**
     * Instante de tiempo relativo de la muestra en segundos (ej. t = k * Ts).
     */
    private float tiempo;

    /**
     * Fecha de captura del registro en formato SQL Date.
     */
    private Date fecha;

    /**
     * Hora de captura del registro en formato SQL Time.
     */
    private Time hora;

    /**
     * Constructor por defecto sin parametros.
     */
    public int_proceso_vars_data() {
    }

    /**
     * Constructor rapido para captura en tiempo real: asigna automaticamente la
     * fecha y hora actuales del sistema al momento de recibir la trama RS232.
     *
     * @param int_proceso_vars_id Integer con el ID de la variable en
     * int_proceso_vars.
     * @param valor float con el valor medido de la variable (analogica o
     * digital).
     * @param tiempo float con la estampa de tiempo de muestreo en segundos.
     */
    public int_proceso_vars_data(int int_proceso_vars_id, float valor, float tiempo) {
        long ahora = System.currentTimeMillis();
        this.int_proceso_vars_id = int_proceso_vars_id;
        this.valor = valor;
        this.tiempo = tiempo;
        this.fecha = new Date(ahora);
        this.hora = new Time(ahora);
    }

    /**
     * Constructor sin ID (util para insertar un nuevo registro cuando el ID es
     * autoincremental en MySQL).
     *
     * @param int_proceso_vars_id Integer con el ID de la variable en
     * int_proceso_vars (FK).
     * @param valor float con la magnitud medida de la senal.
     * @param tiempo float con el tiempo de captura de la muestra.
     * @param fecha Objeto java.sql.Date con la fecha del muestreo.
     * @param hora Objeto java.sql.Time con la hora del muestreo.
     */
    public int_proceso_vars_data(int int_proceso_vars_id, float valor, float tiempo,
            Date fecha, Time hora) {
        this.int_proceso_vars_id = int_proceso_vars_id;
        this.valor = valor;
        this.tiempo = tiempo;
        this.fecha = fecha;
        this.hora = hora;
    }

    /**
     * Constructor completo con todos los atributos de la tabla
     * int_proceso_vars_data.
     *
     * @param id Integer con el identificador primario del registro (PK).
     * @param int_proceso_vars_id Integer con el ID de la variable asociada
     * (FK).
     * @param valor float con el valor registrado de la variable.
     * @param tiempo float con el tiempo de la muestra.
     * @param fecha Objeto java.sql.Date con la fecha del registro.
     * @param hora Objeto java.sql.Time con la hora del registro.
     */
    public int_proceso_vars_data(int id, int int_proceso_vars_id, float valor, float tiempo,
            Date fecha, Time hora) {
        this.id = id;
        this.int_proceso_vars_id = int_proceso_vars_id;
        this.valor = valor;
        this.tiempo = tiempo;
        this.fecha = fecha;
        this.hora = hora;
    }

    /**
     * Obtiene el identificador primario del registro de datos.
     *
     * @return int con el ID del registro.
     */
    public int getId() {
        return id;
    }

    /**
     * Establece el identificador primario del registro de datos.
     *
     * @param id int con el nuevo ID del registro.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Obtiene el identificador de la variable de proceso asociada (clave
     * foranea).
     *
     * @return int con el ID de la variable en int_proceso_vars.
     */
    public int getInt_proceso_vars_id() {
        return int_proceso_vars_id;
    }

    /**
     * Establece el identificador de la variable de proceso asociada.
     *
     * @param int_proceso_vars_id int con el ID de la variable en
     * int_proceso_vars.
     */
    public void setInt_proceso_vars_id(int int_proceso_vars_id) {
        this.int_proceso_vars_id = int_proceso_vars_id;
    }

    /**
     * Obtiene el valor numerico de la muestra (voltaje o estado digital).
     *
     * @return float con el valor de la muestra.
     */
    public float getValor() {
        return valor;
    }

    /**
     * Establece el valor numerico de la muestra.
     *
     * @param valor float con el nuevo valor de la muestra.
     */
    public void setValor(float valor) {
        this.valor = valor;
    }

    /**
     * Obtiene el instante de tiempo asociado a la muestra.
     *
     * @return float con el tiempo de la muestra.
     */
    public float getTiempo() {
        return tiempo;
    }

    /**
     * Establece el instante de tiempo asociado a la muestra.
     *
     * @param tiempo float con el tiempo de la muestra.
     */
    public void setTiempo(float tiempo) {
        this.tiempo = tiempo;
    }

    /**
     * Obtiene la fecha en que se almaceno la muestra.
     *
     * @return Objeto Date con la fecha del registro.
     */
    public Date getFecha() {
        return fecha;
    }

    /**
     * Establece la fecha en que se almaceno la muestra.
     *
     * @param fecha Objeto Date con la fecha del registro.
     */
    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    /**
     * Obtiene la hora en que se almaceno la muestra.
     *
     * @return Objeto Time con la hora del registro.
     */
    public Time getHora() {
        return hora;
    }

    /**
     * Establece la hora en que se almaceno la muestra.
     *
     * @param hora Objeto Time con la hora del registro.
     */
    public void setHora(Time hora) {
        this.hora = hora;
    }

    /**
     * Retorna una representacion textual del registro de muestra.
     *
     * @return String con los valores del registro en int_proceso_vars_data.
     */
    @Override
    public String toString() {
        return "int_proceso_vars_data{"
                + "id=" + id
                + ", var_id=" + int_proceso_vars_id
                + ", valor=" + valor
                + ", tiempo=" + tiempo
                + ", fecha=" + fecha
                + ", hora=" + hora
                + '}';
    }
}
