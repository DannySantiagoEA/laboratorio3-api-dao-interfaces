package co.edu.univalle.ir.entity;

import java.sql.Date;
import java.sql.Time;

/**
 * La clase <b>int_usuarios_procesos</b> representa la entidad de dominio para
 * la tabla asociativa <code>int_usuarios_procesos</code> de la base de datos
 * del Laboratorio Virtual.
 * <p>
 * Almacena la programacion (fechas y horas de inicio y fin) asignada a un
 * usuario especifico para la operacion de un proceso particular del
 * laboratorio.
 * </p>
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/12
 */
public class int_usuarios_procesos {

    /**
     * Identificador del usuario asociado (Clave foranea a int_usuarios).
     */
    private int int_usuarios_id;

    /**
     * Identificador del proceso asociado (Clave foranea a int_proceso).
     */
    private int int_proceso_id;

    /**
     * Fecha de inicio programada para el uso del proceso.
     */
    private Date fecha_inicio;

    /**
     * Fecha de finalizacion programada para el uso del proceso.
     */
    private Date fecha_fin;

    /**
     * Hora de inicio programada para el uso del proceso.
     */
    private Time hora_inicio;

    /**
     * Hora de finalizacion programada para el uso del proceso.
     */
    private Time hora_fin;

    /**
     * Constructor por defecto sin parametros.
     */
    public int_usuarios_procesos() {
    }

    /**
     * Constructor completo para inicializar un registro de programacion de uso
     * de un proceso por un usuario.
     *
     * @param int_usuarios_id Integer correspondiente al ID del usuario en
     * int_usuarios.
     * @param int_proceso_id Integer correspondiente al ID del proceso en
     * int_proceso.
     * @param fecha_inicio Objeto java.sql.Date con la fecha de inicio de la
     * programacion.
     * @param fecha_fin Objeto java.sql.Date con la fecha de fin de la
     * programacion.
     * @param hora_inicio Objeto java.sql.Time con la hora de inicio de la
     * programacion.
     * @param hora_fin Objeto java.sql.Time con la hora de fin de la
     * programacion.
     */
    public int_usuarios_procesos(int int_usuarios_id, int int_proceso_id,
            Date fecha_inicio, Date fecha_fin, Time hora_inicio, Time hora_fin) {
        this.int_usuarios_id = int_usuarios_id;
        this.int_proceso_id = int_proceso_id;
        this.fecha_inicio = fecha_inicio;
        this.fecha_fin = fecha_fin;
        this.hora_inicio = hora_inicio;
        this.hora_fin = hora_fin;
    }

    /**
     * Obtiene el identificador del usuario.
     *
     * @return int con el ID del usuario.
     */
    public int getInt_usuarios_id() {
        return int_usuarios_id;
    }

    /**
     * Establece el identificador del usuario.
     *
     * @param int_usuarios_id int con el nuevo ID del usuario.
     */
    public void setInt_usuarios_id(int int_usuarios_id) {
        this.int_usuarios_id = int_usuarios_id;
    }

    /**
     * Obtiene el identificador del proceso.
     *
     * @return int con el ID del proceso.
     */
    public int getInt_proceso_id() {
        return int_proceso_id;
    }

    /**
     * Establece el identificador del proceso.
     *
     * @param int_proceso_id int con el nuevo ID del proceso.
     */
    public void setInt_proceso_id(int int_proceso_id) {
        this.int_proceso_id = int_proceso_id;
    }

    /**
     * Obtiene la fecha de inicio de la programacion.
     *
     * @return Objeto Date con la fecha de inicio.
     */
    public Date getFecha_inicio() {
        return fecha_inicio;
    }

    /**
     * Establece la fecha de inicio de la programacion.
     *
     * @param fecha_inicio Objeto Date con la fecha de inicio.
     */
    public void setFecha_inicio(Date fecha_inicio) {
        this.fecha_inicio = fecha_inicio;
    }

    /**
     * Obtiene la fecha de finalizacion de la programacion.
     *
     * @return Objeto Date con la fecha de fin.
     */
    public Date getFecha_fin() {
        return fecha_fin;
    }

    /**
     * Establece la fecha de finalizacion de la programacion.
     *
     * @param fecha_fin Objeto Date con la fecha de fin.
     */
    public void setFecha_fin(Date fecha_fin) {
        this.fecha_fin = fecha_fin;
    }

    /**
     * Obtiene la hora de inicio de la programacion.
     *
     * @return Objeto Time con la hora de inicio.
     */
    public Time getHora_inicio() {
        return hora_inicio;
    }

    /**
     * Establece la hora de inicio de la programacion.
     *
     * @param hora_inicio Objeto Time con la hora de inicio.
     */
    public void setHora_inicio(Time hora_inicio) {
        this.hora_inicio = hora_inicio;
    }

    /**
     * Obtiene la hora de finalizacion de la programacion.
     *
     * @return Objeto Time con la hora de fin.
     */
    public Time getHora_fin() {
        return hora_fin;
    }

    /**
     * Establece la hora de finalizacion de la programacion.
     *
     * @param hora_fin Objeto Time con la hora de fin.
     */
    public void setHora_fin(Time hora_fin) {
        this.hora_fin = hora_fin;
    }

    /**
     * Retorna una representacion textual del registro de programacion.
     *
     * @return String con los datos de la programacion usuario-proceso.
     */
    @Override
    public String toString() {
        return "int_usuarios_procesos{"
                + "usuario_id=" + int_usuarios_id
                + ", proceso_id=" + int_proceso_id
                + ", fecha_inicio=" + fecha_inicio
                + ", fecha_fin=" + fecha_fin
                + ", hora_inicio=" + hora_inicio
                + ", hora_fin=" + hora_fin
                + '}';
    }
}
