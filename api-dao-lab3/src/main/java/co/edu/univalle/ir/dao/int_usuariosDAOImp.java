package co.edu.univalle.ir.dao;

import co.edu.univalle.ir.conexion.ConexionDB;
import co.edu.univalle.ir.entity.int_usuarios;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * La clase <b>int_usuariosDAOImp</b> implementa la interfaz
 * {@link int_usuariosDAO} para gestionar los registros de la tabla
 * <code>int_usuarios</code>: insercion, edicion, borrado y seleccion filtrada
 * por diversos campos (Tarea #8), mas la validacion de credenciales del login.
 * <p>
 * Todas las sentencias usan {@link PreparedStatement}, de modo que los valores
 * escritos por el usuario nunca se concatenan dentro del SQL.
 * </p>
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/15
 * @see co.edu.univalle.ir.dao.int_usuariosDAO
 * @see co.edu.univalle.ir.entity.int_usuarios
 */
public class int_usuariosDAOImp implements int_usuariosDAO {

    /**
     * Columnas que se leen en todas las consultas. El email es BLOB, por eso
     * se convierte a texto con CONVERT(... USING utf8mb4).
     */
    private static final String SQL_SELECT_BASE
            = "SELECT id, int_usuarios_tipo_id, nombres, apellidos, "
            + "CONVERT(email USING utf8mb4) AS email, clave "
            + "FROM int_usuarios";

    /**
     * Orden comun de los resultados.
     */
    private static final String SQL_ORDEN = " ORDER BY id ASC";

    /**
     * Sentencia SQL para insertar un usuario.
     */
    private static final String SQL_INSERT
            = "INSERT INTO int_usuarios "
            + "(int_usuarios_tipo_id, nombres, apellidos, email, clave) "
            + "VALUES (?, ?, ?, ?, ?)";

    /**
     * Sentencia SQL para buscar por email exacto.
     */
    private static final String SQL_SELECT_BY_EMAIL
            = SQL_SELECT_BASE + " WHERE CONVERT(email USING utf8mb4) = ?";

    /**
     * Sentencia SQL del login. BINARY hace que la clave distinga mayusculas
     * de minusculas.
     */
    private static final String SQL_LOGIN
            = SQL_SELECT_BASE + " WHERE CONVERT(email USING utf8mb4) = ? AND BINARY clave = ?";

    /**
     * Sentencia SQL para editar todos los campos de un usuario.
     */
    private static final String SQL_UPDATE
            = "UPDATE int_usuarios "
            + "SET int_usuarios_tipo_id = ?, nombres = ?, apellidos = ?, email = ?, clave = ? "
            + "WHERE id = ?";

    /**
     * Sentencia SQL para cambiar solo la clave.
     */
    private static final String SQL_UPDATE_CLAVE
            = "UPDATE int_usuarios SET clave = ? WHERE id = ?";

    /**
     * Sentencia SQL para borrar un usuario.
     */
    private static final String SQL_DELETE
            = "DELETE FROM int_usuarios WHERE id = ?";

    /**
     * Descriptor de conexion JDBC activa con MySQL.
     */
    private Connection dbConn;

    /**
     * Lista en memoria con los ultimos usuarios consultados o insertados.
     */
    private List<int_usuarios> usuarios;

    /**
     * Constructor que inicializa el DAO recibiendo una conexion JDBC activa.
     *
     * @param dbConn Objeto {@link Connection} conectado a la base de datos.
     */
    public int_usuariosDAOImp(Connection dbConn) {
        this.dbConn = dbConn;
        this.usuarios = new ArrayList<>();
    }

    /**
     * Constructor sobrecargado que inicializa el DAO a partir de una instancia
     * de {@link ConexionDB}.
     *
     * @param conexionDB Objeto ConexionDB del paquete de conexion de la API.
     * @exception SQLException Si ocurre un error al obtener la conexion activa.
     */
    public int_usuariosDAOImp(ConexionDB conexionDB) throws SQLException {
        this(conexionDB.getConnection());
    }

    // =========================================================================
    // INSERCION
    // =========================================================================
    /**
     * {@inheritDoc}
     */
    @Override
    public int insertarUsuario(int_usuarios usuario) {
        if (usuario == null) {
            return 0;
        }
        validarDatos(usuario.getNombres(), usuario.getApellidos(), usuario.getClave());

        int resRows = 0;
        try (PreparedStatement pstmt = this.dbConn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, usuario.getInt_usuarios_tipo_id());
            pstmt.setString(2, usuario.getNombres().trim());
            pstmt.setString(3, usuario.getApellidos().trim());
            pstmt.setString(4, usuario.getEmail());
            pstmt.setString(5, usuario.getClave());

            resRows = pstmt.executeUpdate();
            if (resRows > 0) {
                try (ResultSet llaves = pstmt.getGeneratedKeys()) {
                    if (llaves.next()) {
                        usuario.setId(llaves.getInt(1));
                    }
                }
                this.usuarios.add(usuario);
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
    public int insertarUsuario(String nombres, String apellidos, String email, String clave,
            int int_usuarios_tipo_id) {
        return insertarUsuario(new int_usuarios(nombres, apellidos, email, clave, int_usuarios_tipo_id));
    }

    // =========================================================================
    // SELECCION
    // =========================================================================
    /**
     * {@inheritDoc}
     */
    @Override
    public int_usuarios seleccionarPorId(int id) {
        return primero(seleccionarPorCampos(id, null, null, null, null));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<int_usuarios> seleccionarTodos() {
        return seleccionarPorCampos(null, null, null, null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<int_usuarios> seleccionarPorTipo(int int_usuarios_tipo_id) {
        return seleccionarPorCampos(null, int_usuarios_tipo_id, null, null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<int_usuarios> seleccionarPorNombres(String nombres) {
        return seleccionarPorCampos(null, null, nombres, null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<int_usuarios> seleccionarPorApellidos(String apellidos) {
        return seleccionarPorCampos(null, null, null, apellidos, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int_usuarios seleccionarPorEmail(String email) {
        if (estaVacio(email)) {
            return null;
        }
        return primero(ejecutarConsulta(SQL_SELECT_BY_EMAIL, email.trim()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<int_usuarios> seleccionarPorCampos(Integer id, Integer int_usuarios_tipo_id,
            String nombres, String apellidos, String email) {

        // Se arma el WHERE solo con los filtros que llegaron; cada valor va
        // como parametro (?) del PreparedStatement.
        StringBuilder sql = new StringBuilder(SQL_SELECT_BASE).append(" WHERE 1 = 1");
        List<Object> parametros = new ArrayList<>();

        if (id != null) {
            sql.append(" AND id = ?");
            parametros.add(id);
        }
        if (int_usuarios_tipo_id != null) {
            sql.append(" AND int_usuarios_tipo_id = ?");
            parametros.add(int_usuarios_tipo_id);
        }
        if (!estaVacio(nombres)) {
            sql.append(" AND nombres LIKE ?");
            parametros.add("%" + nombres.trim() + "%");
        }
        if (!estaVacio(apellidos)) {
            sql.append(" AND apellidos LIKE ?");
            parametros.add("%" + apellidos.trim() + "%");
        }
        if (!estaVacio(email)) {
            sql.append(" AND CONVERT(email USING utf8mb4) LIKE ?");
            parametros.add("%" + email.trim() + "%");
        }
        sql.append(SQL_ORDEN);

        List<int_usuarios> resultado = ejecutarConsulta(sql.toString(), parametros.toArray());
        this.usuarios = resultado;
        return resultado;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int_usuarios validarCredenciales(String email, String clave) {
        if (estaVacio(email) || clave == null) {
            return null;
        }
        return primero(ejecutarConsulta(SQL_LOGIN, email.trim(), clave));
    }

    // =========================================================================
    // EDICION
    // =========================================================================
    /**
     * {@inheritDoc}
     */
    @Override
    public int editarUsuario(int_usuarios usuario) {
        if (usuario == null) {
            return 0;
        }
        validarDatos(usuario.getNombres(), usuario.getApellidos(), usuario.getClave());

        int resRows = 0;
        try (PreparedStatement pstmt = this.dbConn.prepareStatement(SQL_UPDATE)) {
            pstmt.setInt(1, usuario.getInt_usuarios_tipo_id());
            pstmt.setString(2, usuario.getNombres().trim());
            pstmt.setString(3, usuario.getApellidos().trim());
            pstmt.setString(4, usuario.getEmail());
            pstmt.setString(5, usuario.getClave());
            pstmt.setInt(6, usuario.getId());

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
    public int cambiarClave(int id, String nuevaClave) {
        validarClave(nuevaClave);

        int resRows = 0;
        try (PreparedStatement pstmt = this.dbConn.prepareStatement(SQL_UPDATE_CLAVE)) {
            pstmt.setString(1, nuevaClave);
            pstmt.setInt(2, id);
            resRows = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resRows;
    }

    // =========================================================================
    // BORRADO
    // =========================================================================
    /**
     * {@inheritDoc}
     */
    @Override
    public int borrarUsuario(int id) {
        int resRows = 0;
        try (PreparedStatement pstmt = this.dbConn.prepareStatement(SQL_DELETE)) {
            pstmt.setInt(1, id);
            resRows = pstmt.executeUpdate();
            if (resRows > 0) {
                this.usuarios.removeIf(u -> u.getId() == id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resRows;
    }

    /**
     * Obtiene la lista en memoria con los ultimos usuarios consultados.
     *
     * @return Lista de objetos {@link int_usuarios}.
     */
    public List<int_usuarios> getUsuariosEnMemoria() {
        return usuarios;
    }

    // =========================================================================
    // METODOS AUXILIARES
    // =========================================================================
    /**
     * Ejecuta una consulta SELECT con sus parametros y convierte cada fila en
     * un objeto {@link int_usuarios}.
     *
     * @param sql String con la sentencia SELECT (con ? como marcadores).
     * @param parametros Valores que reemplazan los ? en orden.
     * @return Lista de usuarios encontrados (vacia si no hay o si falla la
     * consulta).
     */
    private List<int_usuarios> ejecutarConsulta(String sql, Object... parametros) {
        List<int_usuarios> resultado = new ArrayList<>();
        try (PreparedStatement pstmt = this.dbConn.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                pstmt.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapearUsuario(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultado;
    }

    /**
     * Convierte la fila actual de un ResultSet en un objeto
     * {@link int_usuarios}.
     *
     * @param rs ResultSet posicionado en un registro valido.
     * @return Objeto {@link int_usuarios} con sus atributos poblados.
     * @exception SQLException Si falla la lectura de alguna columna.
     */
    private int_usuarios mapearUsuario(ResultSet rs) throws SQLException {
        return new int_usuarios(
                rs.getInt("id"),
                rs.getInt("int_usuarios_tipo_id"),
                rs.getString("nombres"),
                rs.getString("apellidos"),
                rs.getString("email"),
                rs.getString("clave")
        );
    }

    /**
     * Retorna el primer elemento de una lista, o null si esta vacia.
     *
     * @param lista Lista de usuarios.
     * @return Primer {@link int_usuarios} o null.
     */
    private static int_usuarios primero(List<int_usuarios> lista) {
        return lista.isEmpty() ? null : lista.get(0);
    }

    /**
     * Indica si un texto es null o solo tiene espacios.
     *
     * @param texto String a revisar.
     * @return boolean true si el texto esta vacio.
     */
    private static boolean estaVacio(String texto) {
        return texto == null || texto.isBlank();
    }

    /**
     * Verifica los campos NOT NULL de la tabla antes de insertar o editar.
     *
     * @param nombres String con los nombres.
     * @param apellidos String con los apellidos.
     * @param clave String con la clave.
     * @exception IllegalArgumentException Si algun campo obligatorio esta
     * vacio o la clave es demasiado larga.
     */
    private static void validarDatos(String nombres, String apellidos, String clave) {
        if (estaVacio(nombres)) {
            throw new IllegalArgumentException("Los nombres del usuario son obligatorios.");
        }
        if (estaVacio(apellidos)) {
            throw new IllegalArgumentException("Los apellidos del usuario son obligatorios.");
        }
        validarClave(clave);
    }

    /**
     * Verifica que la clave no este vacia y quepa en la columna VARCHAR(30).
     *
     * @param clave String con la clave.
     * @exception IllegalArgumentException Si la clave esta vacia o supera 30
     * caracteres.
     */
    private static void validarClave(String clave) {
        if (estaVacio(clave)) {
            throw new IllegalArgumentException("La clave es obligatoria.");
        }
        if (clave.length() > int_usuarios.LONGITUD_MAX_CLAVE) {
            throw new IllegalArgumentException("La clave no puede superar "
                    + int_usuarios.LONGITUD_MAX_CLAVE + " caracteres.");
        }
    }
}