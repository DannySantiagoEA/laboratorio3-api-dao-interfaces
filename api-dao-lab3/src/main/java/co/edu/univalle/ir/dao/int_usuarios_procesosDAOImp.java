package co.edu.univalle.ir.dao;

import co.edu.univalle.ir.conexion.ConexionDB;
import co.edu.univalle.ir.entity.int_usuarios_procesos;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

/**
 * La clase <b>int_usuarios_procesosDAOImp</b> implementa la interfaz
 * {@link int_usuarios_procesosDAO} con el fin de proveer todos los metodos CRUD
 * (insercion, seleccion, edicion y borrado) sobre la tabla
 * <code>int_usuarios_procesos</code>.
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/12
 * @see co.edu.univalle.ir.dao.int_usuarios_procesosDAO
 * @see co.edu.univalle.ir.entity.int_usuarios_procesos
 */
public class int_usuarios_procesosDAOImp implements int_usuarios_procesosDAO {

    /**
     * Descriptor de conexion JDBC hacia la base de datos MySQL.
     */
    private Connection dbConn;

    /**
     * Lista en memoria de las programaciones gestionadas por el DAO.
     */
    private List<int_usuarios_procesos> programaciones;

    /**
     * Sentencia SQL para insertar un registro en int_usuarios_procesos.
     */
    private static final String SQL_INSERT
            = "INSERT INTO int_usuarios_procesos "
            + "(int_usuarios_id, int_proceso_id, fecha_inicio, fecha_fin, hora_inicio, hora_fin) "
            + "VALUES (?, ?, ?, ?, ?, ?)";

    /**
     * Sentencia SQL para seleccionar por usuario y proceso especificos.
     */
    private static final String SQL_SELECT_BY_PK
            = "SELECT int_usuarios_id, int_proceso_id, fecha_inicio, fecha_fin, hora_inicio, hora_fin "
            + "FROM int_usuarios_procesos "
            + "WHERE int_usuarios_id = ? AND int_proceso_id = ?";

    /**
     * Sentencia SQL para seleccionar programaciones por ID de usuario.
     */
    private static final String SQL_SELECT_BY_USER
            = "SELECT int_usuarios_id, int_proceso_id, fecha_inicio, fecha_fin, hora_inicio, hora_fin "
            + "FROM int_usuarios_procesos "
            + "WHERE int_usuarios_id = ? "
            + "ORDER BY fecha_inicio ASC, hora_inicio ASC";

    /**
     * Sentencia SQL para seleccionar programaciones por ID de proceso.
     */
    private static final String SQL_SELECT_BY_PROCESS
            = "SELECT int_usuarios_id, int_proceso_id, fecha_inicio, fecha_fin, hora_inicio, hora_fin "
            + "FROM int_usuarios_procesos "
            + "WHERE int_proceso_id = ? "
            + "ORDER BY fecha_inicio ASC, hora_inicio ASC";

    /**
     * Sentencia SQL para seleccionar todos los registros de la tabla.
     */
    private static final String SQL_SELECT_ALL
            = "SELECT int_usuarios_id, int_proceso_id, fecha_inicio, fecha_fin, hora_inicio, hora_fin "
            + "FROM int_usuarios_procesos "
            + "ORDER BY fecha_inicio ASC, hora_inicio ASC";

    /**
     * Sentencia SQL para editar (actualizar) la programacion de un usuario y
     * proceso.
     */
    private static final String SQL_UPDATE
            = "UPDATE int_usuarios_procesos "
            + "SET fecha_inicio = ?, fecha_fin = ?, hora_inicio = ?, hora_fin = ? "
            + "WHERE int_usuarios_id = ? AND int_proceso_id = ?";

    /**
     * Sentencia SQL para borrar una programacion especifica de usuario y
     * proceso.
     */
    private static final String SQL_DELETE_BY_PK
            = "DELETE FROM int_usuarios_procesos "
            + "WHERE int_usuarios_id = ? AND int_proceso_id = ?";

    /**
     * Sentencia SQL para borrar todas las programaciones de un usuario.
     */
    private static final String SQL_DELETE_BY_USER
            = "DELETE FROM int_usuarios_procesos WHERE int_usuarios_id = ?";

    /**
     * Constructor que inicializa el DAO a partir de un objeto
     * {@link Connection} activo.
     *
     * @param dbConn Objeto Connection conectado a la base de datos.
     */
    public int_usuarios_procesosDAOImp(Connection dbConn) {
        this.dbConn = dbConn;
        this.programaciones = new ArrayList<>();
    }

    /**
     * Constructor sobrecargado que inicializa el DAO a partir del gestor
     * {@link ConexionDB} de la API.
     *
     * @param conexionDB Instancia de ConexionDB configurada.
     * @exception SQLException Si ocurre un error al obtener el descriptor de
     * conexion.
     */
    public int_usuarios_procesosDAOImp(ConexionDB conexionDB) throws SQLException {
        this.dbConn = conexionDB.getConnection();
        this.programaciones = new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int insertarProgramacion(int_usuarios_procesos programacion) {
        if (programacion == null) {
            return 0;
        }
        return insertarProgramacion(
                programacion.getInt_usuarios_id(),
                programacion.getInt_proceso_id(),
                programacion.getFecha_inicio(),
                programacion.getFecha_fin(),
                programacion.getHora_inicio(),
                programacion.getHora_fin()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int insertarProgramacion(int int_usuarios_id, int int_proceso_id,
            Date fecha_inicio, Date fecha_fin, Time hora_inicio, Time hora_fin) {
        int resRows = 0;
        try (PreparedStatement pstmt = this.dbConn.prepareStatement(SQL_INSERT)) {
            pstmt.setInt(1, int_usuarios_id);
            pstmt.setInt(2, int_proceso_id);
            pstmt.setDate(3, fecha_inicio);
            pstmt.setDate(4, fecha_fin);
            pstmt.setTime(5, hora_inicio);
            pstmt.setTime(6, hora_fin);

            resRows = pstmt.executeUpdate();
            if (resRows > 0) {
                int_usuarios_procesos nuevaProg = new int_usuarios_procesos(
                        int_usuarios_id, int_proceso_id,
                        fecha_inicio, fecha_fin, hora_inicio, hora_fin
                );
                this.programaciones.add(nuevaProg);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resRows;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int_usuarios_procesos seleccionarPorUsuarioYProceso(int int_usuarios_id, int int_proceso_id) {
        int_usuarios_procesos encontrado = null;
        try (PreparedStatement pstmt = this.dbConn.prepareStatement(SQL_SELECT_BY_PK)) {
            pstmt.setInt(1, int_usuarios_id);
            pstmt.setInt(2, int_proceso_id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    encontrado = mapearRegistro(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return encontrado;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<int_usuarios_procesos> seleccionarPorUsuario(int int_usuarios_id) {
        List<int_usuarios_procesos> resultado = new ArrayList<>();
        try (PreparedStatement pstmt = this.dbConn.prepareStatement(SQL_SELECT_BY_USER)) {
            pstmt.setInt(1, int_usuarios_id);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapearRegistro(rs));
                }
            }
            this.programaciones = resultado;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultado;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<int_usuarios_procesos> seleccionarPorProceso(int int_proceso_id) {
        List<int_usuarios_procesos> resultado = new ArrayList<>();
        try (PreparedStatement pstmt = this.dbConn.prepareStatement(SQL_SELECT_BY_PROCESS)) {
            pstmt.setInt(1, int_proceso_id);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapearRegistro(rs));
                }
            }
            this.programaciones = resultado;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultado;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<int_usuarios_procesos> seleccionarTodos() {
        List<int_usuarios_procesos> resultado = new ArrayList<>();
        try (PreparedStatement pstmt = this.dbConn.prepareStatement(SQL_SELECT_ALL); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                resultado.add(mapearRegistro(rs));
            }
            this.programaciones = resultado;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultado;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int editarProgramacion(int_usuarios_procesos programacion) {
        if (programacion == null) {
            return 0;
        }
        return editarProgramacion(
                programacion.getInt_usuarios_id(),
                programacion.getInt_proceso_id(),
                programacion.getFecha_inicio(),
                programacion.getFecha_fin(),
                programacion.getHora_inicio(),
                programacion.getHora_fin()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int editarProgramacion(int int_usuarios_id, int int_proceso_id,
            Date nueva_fecha_inicio, Date nueva_fecha_fin,
            Time nueva_hora_inicio, Time nueva_hora_fin) {
        int resRows = 0;
        try (PreparedStatement pstmt = this.dbConn.prepareStatement(SQL_UPDATE)) {
            pstmt.setDate(1, nueva_fecha_inicio);
            pstmt.setDate(2, nueva_fecha_fin);
            pstmt.setTime(3, nueva_hora_inicio);
            pstmt.setTime(4, nueva_hora_fin);
            pstmt.setInt(5, int_usuarios_id);
            pstmt.setInt(6, int_proceso_id);

            resRows = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resRows;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int borrarProgramacion(int int_usuarios_id, int int_proceso_id) {
        int resRows = 0;
        try (PreparedStatement pstmt = this.dbConn.prepareStatement(SQL_DELETE_BY_PK)) {
            pstmt.setInt(1, int_usuarios_id);
            pstmt.setInt(2, int_proceso_id);

            resRows = pstmt.executeUpdate();
            if (resRows > 0) {
                this.programaciones.removeIf(p
                        -> p.getInt_usuarios_id() == int_usuarios_id
                        && p.getInt_proceso_id() == int_proceso_id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resRows;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int borrarPorUsuario(int int_usuarios_id) {
        int resRows = 0;
        try (PreparedStatement pstmt = this.dbConn.prepareStatement(SQL_DELETE_BY_USER)) {
            pstmt.setInt(1, int_usuarios_id);

            resRows = pstmt.executeUpdate();
            if (resRows > 0) {
                this.programaciones.removeIf(p -> p.getInt_usuarios_id() == int_usuarios_id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resRows;
    }

    /**
     * Obtiene la lista en memoria con las ultimas programaciones cargadas.
     *
     * @return Lista de objetos {@link int_usuarios_procesos}.
     */
    public List<int_usuarios_procesos> getProgramacionesEnMemoria() {
        return programaciones;
    }

    /**
     * Convierte la fila actual de un ResultSet en un objeto
     * {@link int_usuarios_procesos}.
     *
     * @param rs ResultSet posicionado en una fila valida.
     * @return Objeto {@link int_usuarios_procesos} hidratado.
     * @exception SQLException Si ocurre un error al leer las columnas del
     * ResultSet.
     */
    private int_usuarios_procesos mapearRegistro(ResultSet rs) throws SQLException {
        return new int_usuarios_procesos(
                rs.getInt("int_usuarios_id"),
                rs.getInt("int_proceso_id"),
                rs.getDate("fecha_inicio"),
                rs.getDate("fecha_fin"),
                rs.getTime("hora_inicio"),
                rs.getTime("hora_fin")
        );
    }
}
