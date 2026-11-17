package co.edu.univalle.ir.dao;

import co.edu.univalle.ir.conexion.ConexionDB;
import co.edu.univalle.ir.entity.int_proceso_refs_data;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

/**
 * La clase <b>int_proceso_refs_dataDAOImp</b> implementa la interfaz
 * {@link int_proceso_refs_dataDAO} para gestionar la insercion, seleccion y
 * borrado de los estados de las 4 salidas digitales en la tabla
 * <code>int_proceso_refs_data</code> (Requerimiento 4, Tarea #19 y Entregables
 * 3.e, 3.g).
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/16
 * @see co.edu.univalle.ir.dao.int_proceso_refs_dataDAO
 * @see co.edu.univalle.ir.entity.int_proceso_refs_data
 */
public class int_proceso_refs_dataDAOImp implements int_proceso_refs_dataDAO {

    /**
     * Numero estandar de salidas digitales del sistema embebido.
     */
    public static final int NUM_SALIDAS_DIGITALES = 4;

    /**
     * Descriptor de conexion JDBC activa con MySQL.
     */
    private Connection dbConn;

    /**
     * Lista en memoria de los registros de salidas digitales gestionados por el
     * DAO.
     */
    private List<int_proceso_refs_data> datosReferencias;

    /**
     * Sentencia SQL para insertar un registro en int_proceso_refs_data.
     */
    private static final String SQL_INSERT
            = "INSERT INTO int_proceso_refs_data "
            + "(int_proceso_refs_id, valor, tiempo, fecha, hora) "
            + "VALUES (?, ?, ?, ?, ?)";

    /**
     * Sentencia SQL para seleccionar un registro por su ID primario.
     */
    private static final String SQL_SELECT_BY_ID
            = "SELECT id, int_proceso_refs_id, valor, tiempo, fecha, hora "
            + "FROM int_proceso_refs_data WHERE id = ?";

    /**
     * Sentencia SQL para seleccionar el historial de una salida digital
     * especifica.
     */
    private static final String SQL_SELECT_BY_REF
            = "SELECT id, int_proceso_refs_id, valor, tiempo, fecha, hora "
            + "FROM int_proceso_refs_data "
            + "WHERE int_proceso_refs_id = ? "
            + "ORDER BY id ASC";

    /**
     * Sentencia SQL para obtener el ultimo estado registrado de una salida
     * digital.
     */
    private static final String SQL_SELECT_LATEST_BY_REF
            = "SELECT id, int_proceso_refs_id, valor, tiempo, fecha, hora "
            + "FROM int_proceso_refs_data "
            + "WHERE int_proceso_refs_id = ? "
            + "ORDER BY id DESC LIMIT 1";

    /**
     * Sentencia SQL para seleccionar todos los registros de la tabla.
     */
    private static final String SQL_SELECT_ALL
            = "SELECT id, int_proceso_refs_id, valor, tiempo, fecha, hora "
            + "FROM int_proceso_refs_data ORDER BY id ASC";

    /**
     * Sentencia SQL para borrar un registro especifico por su ID primario.
     */
    private static final String SQL_DELETE_BY_ID
            = "DELETE FROM int_proceso_refs_data WHERE id = ?";

    /**
     * Sentencia SQL para borrar todos los registros de una salida digital
     * especifica.
     */
    private static final String SQL_DELETE_BY_REF
            = "DELETE FROM int_proceso_refs_data WHERE int_proceso_refs_id = ?";

    /**
     * Sentencia SQL para vaciar todos los registros de la tabla.
     */
    private static final String SQL_DELETE_ALL
            = "DELETE FROM int_proceso_refs_data";

    /**
     * Constructor que inicializa el DAO recibiendo una conexion JDBC activa.
     *
     * @param dbConn Objeto {@link Connection} conectado a MySQL.
     */
    public int_proceso_refs_dataDAOImp(Connection dbConn) {
        this.dbConn = dbConn;
        this.datosReferencias = new ArrayList<>();
    }

    /**
     * Constructor sobrecargado que inicializa el DAO a partir de una instancia
     * de {@link ConexionDB}.
     *
     * @param conexionDB Objeto ConexionDB del modulo de conexion de la API.
     * @exception SQLException Si ocurre un error al obtener el descriptor de
     * conexion.
     */
    public int_proceso_refs_dataDAOImp(ConexionDB conexionDB) throws SQLException {
        this.dbConn = conexionDB.getConnection();
        this.datosReferencias = new ArrayList<>();
    }

    // =========================================================================
    // 1. IMPLEMENTACION DE METODOS DE INSERCION
    // =========================================================================
    /**
     * {@inheritDoc}
     */
    @Override
    public int insertarDatoReferencia(int_proceso_refs_data dato) {
        if (dato == null) {
            return 0;
        }
        long ahora = System.currentTimeMillis();
        Date fechaRegistro = (dato.getFecha() != null) ? dato.getFecha() : new Date(ahora);
        Time horaRegistro = (dato.getHora() != null) ? dato.getHora() : new Time(ahora);

        int resRows = 0;
        try (PreparedStatement pstmt = this.dbConn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, dato.getInt_proceso_refs_id());
            pstmt.setFloat(2, dato.getValor());
            pstmt.setFloat(3, dato.getTiempo());
            pstmt.setDate(4, fechaRegistro);
            pstmt.setTime(5, horaRegistro);

            resRows = pstmt.executeUpdate();
            if (resRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        dato.setId(generatedKeys.getInt(1));
                    }
                }
                dato.setFecha(fechaRegistro);
                dato.setHora(horaRegistro);
                this.datosReferencias.add(dato);
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
    public int insertarDatoReferencia(int int_proceso_refs_id, float valor, float tiempo,
            Date fecha, Time hora) {
        int_proceso_refs_data nuevo = new int_proceso_refs_data(
                int_proceso_refs_id, valor, tiempo, fecha, hora
        );
        return insertarDatoReferencia(nuevo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int registrarEstadoSalidaDigital(int numeroSalida, boolean estadoActivo,
            float tiempo, int idRefBase) {
        if (numeroSalida < 1 || numeroSalida > NUM_SALIDAS_DIGITALES) {
            throw new IllegalArgumentException(
                    "El numero de salida digital debe estar entre 1 y " + NUM_SALIDAS_DIGITALES
            );
        }
        int idReferencia = idRefBase + (numeroSalida - 1);
        float valorLogico = estadoActivo ? 1.0f : 0.0f;
        int_proceso_refs_data registro = new int_proceso_refs_data(idReferencia, valorLogico, tiempo);
        return insertarDatoReferencia(registro);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int insertarEstados4SalidasDigitales(boolean[] estados4Salidas,
            float tiempo, int idRefBase) {
        if (estados4Salidas == null || estados4Salidas.length < NUM_SALIDAS_DIGITALES) {
            throw new IllegalArgumentException("Se requiere un arreglo con las 4 salidas digitales.");
        }

        int totalInsertados = 0;
        long ahora = System.currentTimeMillis();
        Date fechaActual = new Date(ahora);
        Time horaActual = new Time(ahora);

        try (PreparedStatement pstmt = this.dbConn.prepareStatement(SQL_INSERT)) {
            for (int i = 0; i < NUM_SALIDAS_DIGITALES; i++) {
                int idRef = idRefBase + i;
                float valor = estados4Salidas[i] ? 1.0f : 0.0f;

                pstmt.setInt(1, idRef);
                pstmt.setFloat(2, valor);
                pstmt.setFloat(3, tiempo);
                pstmt.setDate(4, fechaActual);
                pstmt.setTime(5, horaActual);
                pstmt.addBatch();

                this.datosReferencias.add(new int_proceso_refs_data(
                        idRef, valor, tiempo, fechaActual, horaActual
                ));
            }

            int[] resultados = pstmt.executeBatch();
            for (int r : resultados) {
                if (r > 0 || r == Statement.SUCCESS_NO_INFO) {
                    totalInsertados++;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return totalInsertados;
    }

    // =========================================================================
    // 2. IMPLEMENTACION DE METODOS DE SELECCION
    // =========================================================================
    /**
     * {@inheritDoc}
     */
    @Override
    public int_proceso_refs_data seleccionarPorId(int id) {
        int_proceso_refs_data encontrado = null;
        try (PreparedStatement pstmt = this.dbConn.prepareStatement(SQL_SELECT_BY_ID)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    encontrado = mapearDatoReferencia(rs);
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
    public List<int_proceso_refs_data> seleccionarPorReferencia(int int_proceso_refs_id) {
        List<int_proceso_refs_data> lista = new ArrayList<>();
        try (PreparedStatement pstmt = this.dbConn.prepareStatement(SQL_SELECT_BY_REF)) {
            pstmt.setInt(1, int_proceso_refs_id);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearDatoReferencia(rs));
                }
            }
            this.datosReferencias = lista;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int_proceso_refs_data seleccionarUltimoDatoPorReferencia(int int_proceso_refs_id) {
        int_proceso_refs_data ultimo = null;
        try (PreparedStatement pstmt = this.dbConn.prepareStatement(SQL_SELECT_LATEST_BY_REF)) {
            pstmt.setInt(1, int_proceso_refs_id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    ultimo = mapearDatoReferencia(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ultimo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean[] consultarUltimosEstados4Salidas(int idRefBase) {
        boolean[] estados = new boolean[NUM_SALIDAS_DIGITALES];
        for (int i = 0; i < NUM_SALIDAS_DIGITALES; i++) {
            int idRef = idRefBase + i;
            int_proceso_refs_data ultimoRegistro = seleccionarUltimoDatoPorReferencia(idRef);
            estados[i] = (ultimoRegistro != null) && ultimoRegistro.isEstadoActivo();
        }
        return estados;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<int_proceso_refs_data> seleccionarTodos() {
        List<int_proceso_refs_data> lista = new ArrayList<>();
        try (PreparedStatement pstmt = this.dbConn.prepareStatement(SQL_SELECT_ALL); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearDatoReferencia(rs));
            }
            this.datosReferencias = lista;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // =========================================================================
    // 3. IMPLEMENTACION DE METODOS DE BORRADO
    // =========================================================================
    /**
     * {@inheritDoc}
     */
    @Override
    public int borrarPorId(int id) {
        int resRows = 0;
        try (PreparedStatement pstmt = this.dbConn.prepareStatement(SQL_DELETE_BY_ID)) {
            pstmt.setInt(1, id);
            resRows = pstmt.executeUpdate();
            if (resRows > 0) {
                this.datosReferencias.removeIf(d -> d.getId() == id);
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
    public int borrarPorReferencia(int int_proceso_refs_id) {
        int resRows = 0;
        try (PreparedStatement pstmt = this.dbConn.prepareStatement(SQL_DELETE_BY_REF)) {
            pstmt.setInt(1, int_proceso_refs_id);
            resRows = pstmt.executeUpdate();
            if (resRows > 0) {
                this.datosReferencias.removeIf(d -> d.getInt_proceso_refs_id() == int_proceso_refs_id);
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
    public int borrarTodos() {
        int resRows = 0;
        try (PreparedStatement pstmt = this.dbConn.prepareStatement(SQL_DELETE_ALL)) {
            resRows = pstmt.executeUpdate();
            this.datosReferencias.clear();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resRows;
    }

    /**
     * Obtiene la lista en memoria de los registros cargados en el DAO.
     *
     * @return Lista de objetos {@link int_proceso_refs_data}.
     */
    public List<int_proceso_refs_data> getDatosReferenciasEnMemoria() {
        return datosReferencias;
    }

    /**
     * Convierte la fila actual de un ResultSet en un objeto
     * {@link int_proceso_refs_data}.
     *
     * @param rs ResultSet posicionado en un registro valido.
     * @return Objeto {@link int_proceso_refs_data} hidratado.
     * @exception SQLException Si ocurre un error al leer las columnas.
     */
    private int_proceso_refs_data mapearDatoReferencia(ResultSet rs) throws SQLException {
        return new int_proceso_refs_data(
                rs.getInt("id"),
                rs.getInt("int_proceso_refs_id"),
                rs.getFloat("valor"),
                rs.getFloat("tiempo"),
                rs.getDate("fecha"),
                rs.getTime("hora")
        );
    }
}
