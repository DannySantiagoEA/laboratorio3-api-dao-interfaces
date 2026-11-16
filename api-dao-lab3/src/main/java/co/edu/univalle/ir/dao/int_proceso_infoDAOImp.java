package co.edu.univalle.ir.dao;

import co.edu.univalle.ir.conexion.ConexionDB;
import co.edu.univalle.ir.entity.int_proceso_control;
import co.edu.univalle.ir.entity.int_proceso_refs;
import co.edu.univalle.ir.entity.int_proceso_vars;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * La clase <b>int_proceso_infoDAOImp</b> implementa la interfaz
 * {@link int_proceso_infoDAO} con las sentencias SELECT sobre las tablas
 * <code>int_proceso_vars</code>, <code>int_proceso_refs</code> e
 * <code>int_proceso_control</code> (Tarea #17).
 * <p>
 * Las tres tablas se leen con un mismo metodo generico
 * (<code>consultar</code>); cada tabla solo aporta su
 * sentencia SQL y la forma de convertir una fila en su entidad.
 * </p>
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina

 * @version $Revision: 1.0 $ 2026/11/16
 * @see co.edu.univalle.ir.dao.int_proceso_infoDAO
 */
public class int_proceso_infoDAOImp implements int_proceso_infoDAO {

    /**
     * Columnas comunes de int_proceso_vars e int_proceso_refs. La descripcion
     * es BLOB, por eso se convierte a texto.
     */
    private static final String COLUMNAS_VARS_REFS
            = "SELECT id, int_proceso_id, nombre, "
            + "CONVERT(descripcion USING utf8mb4) AS descripcion, max_2, min FROM ";

    /**
     * Consulta base de int_proceso_vars.
     */
    private static final String SQL_VARS = COLUMNAS_VARS_REFS + "int_proceso_vars";

    /**
     * Consulta base de int_proceso_refs.
     */
    private static final String SQL_REFS = COLUMNAS_VARS_REFS + "int_proceso_refs";

    /**
     * Consulta base de int_proceso_control.
     */
    private static final String SQL_CONTROL
            = "SELECT id, int_proceso_id, nombre, "
            + "CONVERT(descripcion USING utf8mb4) AS descripcion, "
            + "parametro1, parametro2, parametro3, parametro4 FROM int_proceso_control";

    /**
     * Filtro por proceso, ordenado por ID.
     */
    private static final String WHERE_PROCESO = " WHERE int_proceso_id = ? ORDER BY id ASC";

    /**
     * Filtro por identificador primario.
     */
    private static final String WHERE_ID = " WHERE id = ?";

    /**
     * Filtro por proceso y nombre exacto.
     */
    private static final String WHERE_PROCESO_NOMBRE = " WHERE int_proceso_id = ? AND nombre = ?";

    /**
     * Convierte la fila actual de un ResultSet en un objeto de tipo T.
     *
     * @param <T> Tipo de entidad que produce.
     */
    @FunctionalInterface
    private interface Mapeador<T> {

        /**
         * Convierte la fila actual en una entidad.
         *
         * @param rs ResultSet posicionado en una fila valida.
         * @return Entidad con los valores de la fila.
         * @exception SQLException Si falla la lectura de alguna columna.
         */
        T mapear(ResultSet rs) throws SQLException;
    }

    /**
     * Descriptor de conexion JDBC activa con MySQL.
     */
    private Connection dbConn;

    /**
     * Constructor que inicializa el DAO recibiendo una conexion JDBC activa.
     *
     * @param dbConn Objeto {@link Connection} conectado a la base de datos.
     */
    public int_proceso_infoDAOImp(Connection dbConn) {
        this.dbConn = dbConn;
    }

    /**
     * Constructor sobrecargado que inicializa el DAO a partir de una instancia
     * de {@link ConexionDB}.
     *
     * @param conexionDB Objeto ConexionDB del paquete de conexion de la API.
     * @exception SQLException Si ocurre un error al obtener la conexion activa.
     */
    public int_proceso_infoDAOImp(ConexionDB conexionDB) throws SQLException {
        this(conexionDB.getConnection());
    }

    // =========================================================================
    // int_proceso_vars
    // =========================================================================
    /**
     * {@inheritDoc}
     */
    @Override
    public List<int_proceso_vars> seleccionarVariables(int int_proceso_id) {
        return consultar(SQL_VARS + WHERE_PROCESO, int_proceso_infoDAOImp::mapearVariable, int_proceso_id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int_proceso_vars seleccionarVariablePorId(int id) {
        return primero(consultar(SQL_VARS + WHERE_ID, int_proceso_infoDAOImp::mapearVariable, id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int_proceso_vars seleccionarVariablePorNombre(int int_proceso_id, String nombre) {
        return primero(consultar(SQL_VARS + WHERE_PROCESO_NOMBRE, int_proceso_infoDAOImp::mapearVariable,
                int_proceso_id, nombre));
    }

    // =========================================================================
    // int_proceso_refs
    // =========================================================================
    /**
     * {@inheritDoc}
     */
    @Override
    public List<int_proceso_refs> seleccionarReferencias(int int_proceso_id) {
        return consultar(SQL_REFS + WHERE_PROCESO, int_proceso_infoDAOImp::mapearReferencia, int_proceso_id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int_proceso_refs seleccionarReferenciaPorId(int id) {
        return primero(consultar(SQL_REFS + WHERE_ID, int_proceso_infoDAOImp::mapearReferencia, id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int_proceso_refs seleccionarReferenciaPorNombre(int int_proceso_id, String nombre) {
        return primero(consultar(SQL_REFS + WHERE_PROCESO_NOMBRE, int_proceso_infoDAOImp::mapearReferencia,
                int_proceso_id, nombre));
    }

    // =========================================================================
    // int_proceso_control
    // =========================================================================
    /**
     * {@inheritDoc}
     */
    @Override
    public List<int_proceso_control> seleccionarControles(int int_proceso_id) {
        return consultar(SQL_CONTROL + WHERE_PROCESO, int_proceso_infoDAOImp::mapearControl, int_proceso_id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int_proceso_control seleccionarControlPorId(int id) {
        return primero(consultar(SQL_CONTROL + WHERE_ID, int_proceso_infoDAOImp::mapearControl, id));
    }

    // =========================================================================
    // METODOS AUXILIARES
    // =========================================================================
    /**
     * Ejecuta una consulta SELECT con sus parametros y convierte cada fila con
     * el mapeador indicado.
     *
     * @param <T> Tipo de entidad que se devuelve.
     * @param sql String con la sentencia SELECT (con ? como marcadores).
     * @param mapeador Funcion que convierte una fila en la entidad.
     * @param parametros Valores que reemplazan los ? en orden.
     * @return Lista de entidades encontradas (vacia si no hay o si falla la
     * consulta).
     */
    private <T> List<T> consultar(String sql, Mapeador<T> mapeador, Object... parametros) {
        List<T> resultado = new ArrayList<>();
        try (PreparedStatement pstmt = this.dbConn.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                pstmt.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapeador.mapear(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultado;
    }

    /**
     * Retorna el primer elemento de una lista, o null si esta vacia.
     *
     * @param <T> Tipo de los elementos.
     * @param lista Lista de entidades.
     * @return Primer elemento o null.
     */
    private static <T> T primero(List<T> lista) {
        return lista.isEmpty() ? null : lista.get(0);
    }

    /**
     * Convierte una fila de int_proceso_vars en un objeto
     * {@link int_proceso_vars}.
     *
     * @param rs ResultSet posicionado en una fila valida.
     * @return Objeto {@link int_proceso_vars} con sus atributos poblados.
     * @exception SQLException Si falla la lectura de alguna columna.
     */
    private static int_proceso_vars mapearVariable(ResultSet rs) throws SQLException {
        return new int_proceso_vars(
                rs.getInt("id"),
                rs.getInt("int_proceso_id"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getFloat("max_2"),
                rs.getFloat("min")
        );
    }

    /**
     * Convierte una fila de int_proceso_refs en un objeto
     * {@link int_proceso_refs}.
     *
     * @param rs ResultSet posicionado en una fila valida.
     * @return Objeto {@link int_proceso_refs} con sus atributos poblados.
     * @exception SQLException Si falla la lectura de alguna columna.
     */
    private static int_proceso_refs mapearReferencia(ResultSet rs) throws SQLException {
        return new int_proceso_refs(
                rs.getInt("id"),
                rs.getInt("int_proceso_id"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getFloat("max_2"),
                rs.getFloat("min")
        );
    }

    /**
     * Convierte una fila de int_proceso_control en un objeto
     * {@link int_proceso_control}.
     *
     * @param rs ResultSet posicionado en una fila valida.
     * @return Objeto {@link int_proceso_control} con sus atributos poblados.
     * @exception SQLException Si falla la lectura de alguna columna.
     */
    private static int_proceso_control mapearControl(ResultSet rs) throws SQLException {
        return new int_proceso_control(
                rs.getInt("id"),
                rs.getInt("int_proceso_id"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getFloat("parametro1"),
                rs.getFloat("parametro2"),
                rs.getFloat("parametro3"),
                rs.getFloat("parametro4")
        );
    }
}