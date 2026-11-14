package co.edu.univalle.ir.dao;

import co.edu.univalle.ir.conexion.ConexionDB;
import co.edu.univalle.ir.entity.int_proceso;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * La clase <b>int_procesoDAOImp</b> implementa la interfaz
 * {@link int_procesoDAO} para gestionar las operaciones CRUD sobre la tabla
 * <code>int_proceso</code> y proveer acceso directo de lectura y escritura al
 * parametro <code>tiempo_muestreo</code> (Tarea #14 - HU-03).
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/14
 * @see co.edu.univalle.ir.dao.int_procesoDAO
 * @see co.edu.univalle.ir.entity.int_proceso
 */
public class int_procesoDAOImp implements int_procesoDAO {

    /**
     * Descriptor de conexion JDBC activa con MySQL.
     */
    private Connection dbConn;

    /**
     * Cache en memoria de los procesos gestionados por el DAO.
     */
    private List<int_proceso> procesos;

    /**
     * Sentencia SQL para insertar un nuevo proceso.
     */
    private static final String SQL_INSERT
            = "INSERT INTO int_proceso "
            + "(int_proceso_tipo_id, nombre, descripcion, tiempo_muestreo, archivo_especificaciones, archivo_manual) "
            + "VALUES (?, ?, ?, ?, ?, ?)";

    /**
     * Sentencia SQL para seleccionar un proceso por su ID primario.
     */
    private static final String SQL_SELECT_BY_ID
            = "SELECT id, int_proceso_tipo_id, nombre, descripcion, tiempo_muestreo, "
            + "archivo_especificaciones, archivo_manual "
            + "FROM int_proceso WHERE id = ?";

    /**
     * Sentencia SQL para seleccionar todos los procesos.
     */
    private static final String SQL_SELECT_ALL
            = "SELECT id, int_proceso_tipo_id, nombre, descripcion, tiempo_muestreo, "
            + "archivo_especificaciones, archivo_manual "
            + "FROM int_proceso ORDER BY id ASC";

    /**
     * Sentencia SQL para actualizar un proceso completo por ID.
     */
    private static final String SQL_UPDATE
            = "UPDATE int_proceso "
            + "SET int_proceso_tipo_id = ?, nombre = ?, descripcion = ?, tiempo_muestreo = ?, "
            + "archivo_especificaciones = ?, archivo_manual = ? "
            + "WHERE id = ?";

    /**
     * Sentencia SQL para eliminar un proceso por ID.
     */
    private static final String SQL_DELETE
            = "DELETE FROM int_proceso WHERE id = ?";

    /**
     * Sentencia SQL optimizada (Tarea #14) para consultar solo el
     * tiempo_muestreo por ID.
     */
    private static final String SQL_SELECT_TIEMPO_MUESTREO
            = "SELECT tiempo_muestreo FROM int_proceso WHERE id = ?";

    /**
     * Sentencia SQL optimizada (Tarea #14) para actualizar solo el
     * tiempo_muestreo por ID.
     */
    private static final String SQL_UPDATE_TIEMPO_MUESTREO
            = "UPDATE int_proceso SET tiempo_muestreo = ? WHERE id = ?";

    /**
     * Constructor que inicializa el DAO recibiendo una conexion JDBC activa.
     *
     * @param dbConn Objeto {@link Connection} conectado a la base de datos.
     */
    public int_procesoDAOImp(Connection dbConn) {
        this.dbConn = dbConn;
        this.procesos = new ArrayList<>();
    }

    /**
     * Constructor sobrecargado que inicializa el DAO a partir de una instancia
     * de {@link ConexionDB}.
     *
     * @param conexionDB Objeto ConexionDB del paquete de conexion de la API.
     * @exception SQLException Si ocurre un error al obtener la conexion activa.
     */
    public int_procesoDAOImp(ConexionDB conexionDB) throws SQLException {
        this.dbConn = conexionDB.getConnection();
        this.procesos = new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int insertarProceso(int_proceso proceso) {
        if (proceso == null) {
            return 0;
        }
        int resRows = 0;
        try (PreparedStatement pstmt = this.dbConn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, proceso.getInt_proceso_tipo_id());
            pstmt.setString(2, proceso.getNombre());
            pstmt.setString(3, proceso.getDescripcion());
            pstmt.setFloat(4, proceso.getTiempo_muestreo());
            pstmt.setString(5, proceso.getArchivo_especificaciones());
            pstmt.setString(6, proceso.getArchivo_manual());

            resRows = pstmt.executeUpdate();
            if (resRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        proceso.setId(generatedKeys.getInt(1));
                    }
                }
                this.procesos.add(proceso);
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
    public int insertarProceso(int int_proceso_tipo_id, String nombre, String descripcion,
            float tiempo_muestreo, String archivo_especificaciones, String archivo_manual) {
        int_proceso nuevo = new int_proceso(
                int_proceso_tipo_id,
                nombre,
                descripcion,
                tiempo_muestreo,
                archivo_especificaciones,
                archivo_manual
        );
        return insertarProceso(nuevo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int_proceso seleccionarPorId(int id) {
        int_proceso encontrado = null;
        try (PreparedStatement pstmt = this.dbConn.prepareStatement(SQL_SELECT_BY_ID)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    encontrado = mapearProceso(rs);
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
    public List<int_proceso> seleccionarTodos() {
        List<int_proceso> lista = new ArrayList<>();
        try (PreparedStatement pstmt = this.dbConn.prepareStatement(SQL_SELECT_ALL); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearProceso(rs));
            }
            this.procesos = lista;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int editarProceso(int_proceso proceso) {
        if (proceso == null) {
            return 0;
        }
        int resRows = 0;
        try (PreparedStatement pstmt = this.dbConn.prepareStatement(SQL_UPDATE)) {
            pstmt.setInt(1, proceso.getInt_proceso_tipo_id());
            pstmt.setString(2, proceso.getNombre());
            pstmt.setString(3, proceso.getDescripcion());
            pstmt.setFloat(4, proceso.getTiempo_muestreo());
            pstmt.setString(5, proceso.getArchivo_especificaciones());
            pstmt.setString(6, proceso.getArchivo_manual());
            pstmt.setInt(7, proceso.getId());

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
    public int borrarProceso(int id) {
        int resRows = 0;
        try (PreparedStatement pstmt = this.dbConn.prepareStatement(SQL_DELETE)) {
            pstmt.setInt(1, id);
            resRows = pstmt.executeUpdate();
            if (resRows > 0) {
                this.procesos.removeIf(p -> p.getId() == id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resRows;
    }

    // =========================================================================
    // IMPLEMENTACION TAREA #14: CONSULTA Y ACTUALIZACION RAPIDA DE TIEMPO_MUESTREO
    // =========================================================================
    /**
     * {@inheritDoc}
     */
    @Override
    public float consultarTiempoMuestreo(int idProceso) {
        float tiempoMuestreo = -1.0f;
        try (PreparedStatement pstmt = this.dbConn.prepareStatement(SQL_SELECT_TIEMPO_MUESTREO)) {
            pstmt.setInt(1, idProceso);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    tiempoMuestreo = rs.getFloat("tiempo_muestreo");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tiempoMuestreo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int actualizarTiempoMuestreo(int idProceso, float nuevoTiempoMuestreo) {
        if (nuevoTiempoMuestreo <= 0.0f) {
            throw new IllegalArgumentException("El tiempo de muestreo debe ser mayor que 0.");
        }
        int resRows = 0;
        try (PreparedStatement pstmt = this.dbConn.prepareStatement(SQL_UPDATE_TIEMPO_MUESTREO)) {
            pstmt.setFloat(1, nuevoTiempoMuestreo);
            pstmt.setInt(2, idProceso);

            resRows = pstmt.executeUpdate();
            if (resRows > 0) {
                // Sincronizar el valor en la lista en memoria si el proceso estaba cargado
                for (int_proceso proc : this.procesos) {
                    if (proc.getId() == idProceso) {
                        proc.setTiempo_muestreo(nuevoTiempoMuestreo);
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resRows;
    }

    /**
     * Obtiene la lista en memoria de los procesos cargados en el DAO.
     *
     * @return Lista de objetos {@link int_proceso}.
     */
    public List<int_proceso> getProcesosEnMemoria() {
        return procesos;
    }

    /**
     * Convierte la fila actual de un ResultSet en una instancia de
     * {@link int_proceso}.
     *
     * @param rs ResultSet posicionado en un registro valido.
     * @return Objeto {@link int_proceso} con sus atributos poblados.
     * @exception SQLException Si falla la lectura de alguna columna.
     */
    private int_proceso mapearProceso(ResultSet rs) throws SQLException {
        return new int_proceso(
                rs.getInt("id"),
                rs.getInt("int_proceso_tipo_id"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getFloat("tiempo_muestreo"),
                rs.getString("archivo_especificaciones"),
                rs.getString("archivo_manual")
        );
    }
}
