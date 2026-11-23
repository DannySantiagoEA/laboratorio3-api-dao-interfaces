package co.edu.univalle.ir.gui.model;

import co.edu.univalle.ir.conexion.ConexionDB;
import co.edu.univalle.ir.entity.int_usuarios;
import co.edu.univalle.ir.entity.int_usuarios_procesos;

/**
 * Representa la sesion autenticada activa en la interfaz grafica del
 * Laboratorio Virtual (HU-07 - Tarea #33).
 * <p>
 * Mantiene el usuario validado desde <code>int_usuarios</code>, su conexion
 * activa mediante {@link ConexionDB} y la programacion de acceso al proceso
 * obtenida de <code>int_usuarios_procesos</code>.
 * </p>
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/22
 */
public class SesionUsuario {

    /**
     * ID de perfil correspondiente a Administrador en int_usuarios_tipo.
     */
    public static final int PERFIL_ADMINISTRADOR = 1;

    private final int_usuarios usuario;
    private final ConexionDB conexionDB;
    private final int idProcesoActivo;
    private final int_usuarios_procesos horarioAsignado;

    /**
     * Construye una sesion autenticada.
     *
     * @param usuario Entidad {@link int_usuarios} recuperada por el DAO.
     * @param conexionDB Conexion JDBC activa a lab_virtual_db.
     * @param idProcesoActivo ID del proceso de laboratorio asignado (por
     * defecto 1).
     * @param horarioAsignado Registro de int_usuarios_procesos (puede ser null
     * si no tiene horario asignado).
     */
    public SesionUsuario(int_usuarios usuario, ConexionDB conexionDB,
            int idProcesoActivo, int_usuarios_procesos horarioAsignado) {
        this.usuario = usuario;
        this.conexionDB = conexionDB;
        this.idProcesoActivo = idProcesoActivo;
        this.horarioAsignado = horarioAsignado;
    }

    public int_usuarios getUsuario() {
        return usuario;
    }

    public ConexionDB getConexionDB() {
        return conexionDB;
    }

    public int getIdProcesoActivo() {
        return idProcesoActivo;
    }

    public int_usuarios_procesos getHorarioAsignado() {
        return horarioAsignado;
    }

    /**
     * Indica si el usuario autenticado posee perfil de administrador.
     *
     * @return true si int_usuarios_tipo_id == 1.
     */
    public boolean isAdministrador() {
        return usuario != null && usuario.getInt_usuarios_tipo_id() == PERFIL_ADMINISTRADOR;
    }

    /**
     * Retorna el nombre completo del usuario autenticado.
     *
     * @return Nombres y apellidos concatenados.
     */
    public String getNombreCompleto() {
        if (usuario == null) {
            return "Desconocido";
        }
        return (usuario.getNombres() + " " + usuario.getApellidos()).trim();
    }

    /**
     * Retorna una etiqueta legible del perfil para mostrar en la barra
     * superior.
     *
     * @return Descripcion del tipo de usuario.
     */
    public String getDescripcionPerfil() {
        if (usuario == null) {
            return "Sin perfil";
        }
        return switch (usuario.getInt_usuarios_tipo_id()) {
            case 1 ->
                "ADMINISTRADOR";
            case 2 ->
                "ESTUDIANTE / OPERADOR";
            default ->
                "PERFIL #" + usuario.getInt_usuarios_tipo_id();
        };
    }

    /**
     * Cierra de forma segura la conexion JDBC asociada a la sesion.
     */
    public void cerrarConexionSegura() {
        if (conexionDB != null) {
            try {
                conexionDB.cerrarConexion();
            } catch (Exception ignored) {
            }
        }
    }
}
