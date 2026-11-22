package co.edu.univalle.ir.entity;

import java.sql.Date;
import java.sql.Time;

/**
 * La clase <b>int_proceso_refs_data</b> representa la entidad de dominio para
 * la tabla <code>int_proceso_refs_data</code> de la base de datos del
 * Laboratorio Virtual.
 * <p>
 * Almacena el historial y el estado logico vigente (0.0 = OFF, 1.0 = ON) de las
 * 4 variables de salida digitales comandadas desde la GUI para ser enviadas al
 * sistema embebido a traves del puente RS232-SQL (Entregables 3.e y 3.g).
 * </p>
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/16
 */
public class int_proceso_refs_data {

    /**
     * Identificador unico del registro en la tabla int_proceso_refs_data (PK).
     */
    private int id;

    /**
     * Identificador de la referencia o salida digital asociada en
     * int_proceso_refs (FK: 1..4).
     */
    private int int_proceso_refs_id;

    /**
     * Valor de la salida digital (1.0f para estado alto/ON, 0.0f para estado
     * bajo/OFF).
     */
    private float valor;

    /**
     * Instante de tiempo en segundos en el que se registro el cambio de estado.
     */
    private float tiempo;

    /**
     * Fecha en la que se almaceno el registro en formato SQL Date.
     */
    private Date fecha;

    /**
     * Hora en la que se almaceno el registro en formato SQL Time.
     */
    private Time hora;

    /**
     * Constructor por defecto sin parametros.
     */
    public int_proceso_refs_data() {
    }

    /**
     * Constructor rapido para eventos de la GUI: asigna automaticamente la
     * fecha y hora actuales del sistema al conmutar una salida digital.
     *
     * @param int_proceso_refs_id Integer con el ID de la salida en
     * int_proceso_refs (1..4).
     * @param valor float con el estado de la salida (1.0f = ON, 0.0f = OFF).
     * @param tiempo float con la estampa de tiempo en segundos.
     */
    public int_proceso_refs_data(int int_proceso_refs_id, float valor, float tiempo) {
        long ahora = System.currentTimeMillis();
        this.int_proceso_refs_id = int_proceso_refs_id;
        this.valor = valor;
        this.tiempo = tiempo;
        this.fecha = new Date(ahora);
        this.hora = new Time(ahora);
    }

    /**
     * Constructor sin ID (util para insertar un nuevo registro cuando el ID es
     * autoincremental en MySQL).
     *
     * @param int_proceso_refs_id Integer con el ID de la salida en
     * int_proceso_refs (FK).
     * @param valor float con el valor o estado de la referencia/salida.
     * @param tiempo float con el tiempo de registro.
     * @param fecha Objeto java.sql.Date con la fecha del registro.
     * @param hora Objeto java.sql.Time con la hora del registro.
     */
    public int_proceso_refs_data(int int_proceso_refs_id, float valor, float tiempo,
            Date fecha, Time hora) {
        this.int_proceso_refs_id = int_proceso_refs_id;
        this.valor = valor;
        this.tiempo = tiempo;
        this.fecha = fecha;
        this.hora = hora;
    }

    /**
     * Constructor completo con todos los atributos de la tabla
     * int_proceso_refs_data.
     *
     * @param id Integer con el identificador primario del registro (PK).
     * @param int_proceso_refs_id Integer con el ID de la referencia asociada
     * (FK).
     * @param valor float con el valor registrado de la salida digital.
     * @param tiempo float con el tiempo del evento.
     * @param fecha Objeto java.sql.Date con la fecha del registro.
     * @param hora Objeto java.sql.Time con la hora del registro.
     */
    public int_proceso_refs_data(int id, int int_proceso_refs_id, float valor, float tiempo,
            Date fecha, Time hora) {
        this.id = id;
        this.int_proceso_refs_id = int_proceso_refs_id;
        this.valor = valor;
        this.tiempo = tiempo;
        this.fecha = fecha;
        this.hora = hora;
    }

    /**
     * Obtiene el identificador primario del registro.
     *
     * @return int con el ID del registro.
     */
    public int getId() {
        return id;
    }

    /**
     * Establece el identificador primario del registro.
     *
     * @param id int con el nuevo ID del registro.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Obtiene el identificador de la referencia o salida asociada (clave
     * foranea).
     *
     * @return int con el ID en int_proceso_refs.
     */
    public int getInt_proceso_refs_id() {
        return int_proceso_refs_id;
    }

    /**
     * Establece el identificador de la referencia o salida asociada.
     *
     * @param int_proceso_refs_id int con el ID en int_proceso_refs.
     */
    public void setInt_proceso_refs_id(int int_proceso_refs_id) {
        this.int_proceso_refs_id = int_proceso_refs_id;
    }

    /**
     * Obtiene el valor numerico de la salida digital (0.0 o 1.0).
     *
     * @return float con el valor registrado.
     */
    public float getValor() {
        return valor;
    }

    /**
     * Establece el valor numerico de la salida digital.
     *
     * @param valor float con el nuevo valor de la salida.
     */
    public void setValor(float valor) {
        this.valor = valor;
    }

    /**
     * Evalua el valor numerico como un estado booleano para las salidas
     * digitales.
     *
     * @return boolean true si el valor es mayor o igual a 0.5f (ON); false si
     * es 0.0f (OFF).
     */
    public boolean isEstadoActivo() {
        return this.valor >= 0.5f;
    }

    /**
     * Obtiene el instante de tiempo asociado al registro.
     *
     * @return float con el tiempo en segundos.
     */
    public float getTiempo() {
        return tiempo;
    }

    /**
     * Establece el instante de tiempo asociado al registro.
     *
     * @param tiempo float con el tiempo en segundos.
     */
    public void setTiempo(float tiempo) {
        this.tiempo = tiempo;
    }

    /**
     * Obtiene la fecha del registro.
     *
     * @return Objeto Date con la fecha.
     */
    public Date getFecha() {
        return fecha;
    }

    /**
     * Establece la fecha del registro.
     *
     * @param fecha Objeto Date con la fecha.
     */
    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    /**
     * Obtiene la hora del registro.
     *
     * @return Objeto Time con la hora.
     */
    public Time getHora() {
        return hora;
    }

    /**
     * Establece la hora del registro.
     *
     * @param hora Objeto Time con la hora.
     */
    public void setHora(Time hora) {
        this.hora = hora;
    }

    /**
     * Retorna una representacion textual del registro de salida digital.
     *
     * @return String con los valores del registro en int_proceso_refs_data.
     */
    @Override
    public String toString() {
        return "int_proceso_refs_data{"
                + "id=" + id
                + ", ref_id=" + int_proceso_refs_id
                + ", valor=" + valor
                + " (" + (isEstadoActivo() ? "ON" : "OFF") + ")"
                + ", tiempo=" + tiempo
                + ", fecha=" + fecha
                + ", hora=" + hora
                + '}';
    }
}
