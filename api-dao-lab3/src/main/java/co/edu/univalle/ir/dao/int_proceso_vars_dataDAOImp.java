package co.edu.univalle.ir.dao;

import co.edu.univalle.ir.conexion.ConexionDB;
import co.edu.univalle.ir.entity.int_proceso_vars_data;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * La clase <b>int_proceso_vars_dataDAOImp</b> implementa la interfaz
 * {@link int_proceso_vars_dataDAO} para ejecutar las operaciones de insercion,
 * seleccion y borrado de registros sobre la tabla
 * <code>int_proceso_vars_data</code> (Requerimiento 4, Tarea #18 y Entregables
 * 3.d, 3.g).
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/15
 * @see co.edu.univalle.ir.dao.int_proceso_vars_dataDAO
 * @see co.edu.univalle.ir.entity.int_proceso_vars_data
 */
public class int_proceso_vars_dataDAOImp implements int_proceso_vars_dataDAO {

    /**
     * Numero estandar de entradas analogicas del sistema embebido.
     */
    public static final int NUM_ENTRADAS_ANALOGICAS = 8;

    /**
     * Numero estandar de entradas digitales del sistema embebido.
     */
    public static final int NUM_ENTRADAS_DIGITALES = 4;

    /**
     * Descriptor de conexion JDBC activa con MySQL.
     */
    private Connection dbConn;

    /**
     * Lista en memoria de las muestras consultadas o registradas por el DAO.
     */
    private List<int_proceso_vars_data> datosVariables;

    /**
     * Sentencia SQL para insertar una muestra en int_proceso_vars_data.
     */
    private static final String SQL_INSERT
            = "INSERT INTO int_proceso_vars_data "
            + "(int_proceso_vars_id, valor, tiempo, fecha, hora) "
            + "VALUES (?, ?, ?, ?, ?)";

    /**
     * Sentencia SQL para seleccionar una muestra por su ID primario.
     */
    private static final String SQL_SELECT_BY_ID
            = "SELECT id, int_proceso_vars_id, valor, tiempo, fecha, hora "
            + "FROM int_proceso_vars_data WHERE id = ?";

    /**
     * Sentencia SQL para seleccionar todas las muestras de una variable en
     * orden cronologico.
     */
    private static final String SQL_SELECT_BY_VAR
            = "SELECT id, int_proceso_vars_id, valor, tiempo, fecha, hora "
            + "FROM int_proceso_vars_data "
            + "WHERE int_proceso_vars_id = ? "
            + "ORDER BY id ASC";

    /**
     * Sentencia SQL para seleccionar las ultimas N muestras de una variable
     * (ventana deslizante GUI).
     */
    private static final String SQL_SELECT_LAST_N_BY_VAR
            = "SELECT id, int_proceso_vars_id, valor, tiempo, fecha, hora "
            + "FROM int_proceso_vars_data "
            + "WHERE int_proceso_vars_id = ? "
            + "ORDER BY id DESC LIMIT ?";

    /**
     * Sentencia SQL para seleccionar la muestra mas reciente de una variable.
     */
    private static final String SQL_SELECT_LATEST_BY_VAR
            = "SELECT id, int_proceso_vars_id, valor, tiempo, fecha, hora "
            + "FROM int_proceso_vars_data "
            + "WHERE int_proceso_vars_id = ? "
            + "ORDER BY id DESC LIMIT 1";

    /**
     * Sentencia SQL para seleccionar todos los registros de la tabla.
     */
    private static final String SQL_SELECT_ALL
            = "SELECT id, int_proceso_vars_id, valor, tiempo, fecha, hora "
            + "FROM int_proceso_vars_data ORDER BY id ASC";

    /**
     * Sentencia SQL para borrar una muestra especifica por su ID primario.
     */
    private static final String SQL_DELETE_BY_ID
            = "DELETE FROM int_proceso_vars_data WHERE id = ?";

    /**
     * Sentencia SQL para borrar todas las muestras de una variable especifica.
     */
    private static final String SQL_DELETE_BY_VAR
            = "DELETE FROM int_proceso_vars_data WHERE int_proceso_vars_id = ?";

    /**
     * Sentencia SQL para vaciar todos los registros de la tabla.
     */
    private static final String SQL_DELETE_ALL
            = "DELETE FROM int_proceso_vars_data";

    /**
     * Constructor que inicializa el DAO recibiendo una conexion JDBC activa.
     *
     * @param dbConn Objeto {@link Connection} conectado a MySQL.
     */
    public int_proceso_vars_dataDAOImp(Connection dbConn) {
        this.dbConn = dbConn;
        this.datosVariables = new ArrayList<>();
    }

    /**
     * Constructor sobrecargado que inicializa el DAO a partir de una instancia
     * de {@link ConexionDB}.
     *
     * @param conexionDB Objeto ConexionDB del modulo de conexion de la API.
     * @exception SQLException Si ocurre un error al obtener el descriptor de
     * conexion.
     */
    public int_proceso_vars_dataDAOImp(ConexionDB conexionDB) throws SQLException {
        this.dbConn = conexionDB.getConnection();
        this.datosVariables = new ArrayList<>();
    }

    // =========================================================================
    // 1. IMPLEMENTACION DE METODOS DE INSERCION
    // =========================================================================
    /**
     * {@inheritDoc}
     */
    @Override
    public int insertarDatoVariable(int_proceso_vars_data dato) {
        if (dato == null) {
            return 0;
        }
        long ahora = System.currentTimeMillis();
        Date fechaRegistro = (dato.getFecha() != null) ? dato.getFecha() : new Date(ahora);
        Time horaRegistro = (dato.getHora() != null) ? dato.getHora() : new Time(ahora);

        int resRows = 0;
        try (PreparedStatement pstmt = this.dbConn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, dato.getInt_proceso_vars_id());
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
    public int insertarDatoVariable(int int_proceso_vars_id, float valor, float tiempo,
            Date fecha, Time hora) {
        int_proceso_vars_data nuevoDato = new int_proceso_vars_data(
                int_proceso_vars_id, valor, tiempo, fecha, hora
        );
        return insertarDatoVariable(nuevoDato);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int insertarLoteDatosVariables(List<int_proceso_vars_data> muestras) {
        if (muestras == null || muestras.isEmpty()) {
            return 0;
        }
        int totalInsertados = 0;
        long ahora = System.currentTimeMillis();
        Date fechaPorDefecto = new Date(ahora);
        Time horaPorDefecto = new Time(ahora);

        try (PreparedStatement pstmt = this.dbConn.prepareStatement(SQL_INSERT)) {
            for (int_proceso_vars_data muestra : muestras) {
                Date f = (muestra.getFecha() != null) ? muestra.getFecha() : fechaPorDefecto;
                Time h = (muestra.getHora() != null) ? muestra.getHora() : horaPorDefecto;

                pstmt.setInt(1, muestra.getInt_proceso_vars_id());
                pstmt.setFloat(2, muestra.getValor());
                pstmt.setFloat(3, muestra.getTiempo());
                pstmt.setDate(4, f);
                pstmt.setTime(5, h);
                pstmt.addBatch();
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

    /**
     * {@inheritDoc}
     */
    @Override
    public int insertarMuestra12Canales(float[] analogicas8, int[] digitales4,
            float tiempo, int idVarBaseAnalogica) {
        if (analogicas8 == null || analogicas8.length < NUM_ENTRADAS_ANALOGICAS) {
            throw new IllegalArgumentException("Se requieren las 8 entradas analogicas.");
        }
        if (digitales4 == null || digitales4.length < NUM_ENTRADAS_DIGITALES) {
            throw new IllegalArgumentException("Se requieren las 4 entradas digitales.");
        }

        long ahora = System.currentTimeMillis();
        Date fechaActual = new Date(ahora);
        Time horaActual = new Time(ahora);

        List<int_proceso_vars_data> lote12Canales = new ArrayList<>(
                NUM_ENTRADAS_ANALOGICAS + NUM_ENTRADAS_DIGITALES
        );

        // 1. Empaquetar las 8 variables analogicas (ej. IDs 1 a 8)
        for (int i = 0; i < NUM_ENTRADAS_ANALOGICAS; i++) {
            int idVarAnalogica = idVarBaseAnalogica + i;
            lote12Canales.add(new int_proceso_vars_data(
                    idVarAnalogica, analogicas8[i], tiempo, fechaActual, horaActual
            ));
        }

        // 2. Empaquetar las 4 variables digitales de entrada (ej. IDs 9 a 12)
        int idVarBaseDigital = idVarBaseAnalogica + NUM_ENTRADAS_ANALOGICAS;
        for (int j = 0; j < NUM_ENTRADAS_DIGITALES; j++) {
            int idVarDigital = idVarBaseDigital + j;
            float valorDigital = (digitales4[j] != 0) ? 1.0f : 0.0f;
            lote12Canales.add(new int_proceso_vars_data(
                    idVarDigital, valorDigital, tiempo, fechaActual, horaActual
            ));
        }

        return insertarLoteDatosVariables(lote12Canales);
    }

    // =========================================================================
    // 2. IMPLEMENTACION DE METODOS DE SELECCION
    // =========================================================================
    /**
     * {@inheritDoc}
     */
    @Override
    public int_proceso_vars_data seleccionarPorId(int id) {
        int_proceso_vars_data encontrado = null;
        try (PreparedStatement pstmt = this.dbConn.prepareStatement(SQL_SELECT_BY_ID)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    encontrado = mapearDatoVariable(rs);
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
    public List<int_proceso_vars_data> seleccionarPorVariable(int int_proceso_vars_id) {
        List<int_proceso_vars_data> lista = new ArrayList<>();
        try (PreparedStatement pstmt = this.dbConn.prepareStatement(SQL_SELECT_BY_VAR)) {
            pstmt.setInt(1, int_proceso_vars_id);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearDatoVariable(rs));
                }
            }
            this.datosVariables = lista;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<int_proceso_vars_data> seleccionarUltimosPorVariable(int int_proceso_vars_id, int limite) {
        List<int_proceso_vars_data> listaRecientes = new ArrayList<>();
        int maxRegistros = (limite > 0) ? limite : 100;

        try (PreparedStatement pstmt = this.dbConn.prepareStatement(SQL_SELECT_LAST_N_BY_VAR)) {
            pstmt.setInt(1, int_proceso_vars_id);
            pstmt.setInt(2, maxRegistros);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    listaRecientes.add(mapearDatoVariable(rs));
                }
            }
            // Invertir para que queden en orden cronologico ascendente (t_0 -> t_actual) para JFreeChart
            Collections.reverse(listaRecientes);
            this.datosVariables = listaRecientes;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listaRecientes;
    }
    
        /**
     * {@inheritDoc}
     */
    @Override
    public List<int_proceso_vars_data> seleccionarPosterioresA(int[] idsVariables, int ultimoIdLeido, int limite) {
        List<int_proceso_vars_data> nuevas = new ArrayList<>();
        if (idsVariables == null || idsVariables.length == 0) {
            return nuevas;
        }
        int maxRegistros = (limite > 0) ? limite : 500;

        // Un marcador ? por cada variable: IN (?, ?, ...)
        StringBuilder marcadores = new StringBuilder();
        for (int i = 0; i < idsVariables.length; i++) {
            marcadores.append(i == 0 ? "?" : ", ?");
        }
        String sql = "SELECT id, int_proceso_vars_id, valor, tiempo, fecha, hora "
                + "FROM int_proceso_vars_data "
                + "WHERE id > ? AND int_proceso_vars_id IN (" + marcadores + ") "
                + "ORDER BY id ASC LIMIT ?";

        try (PreparedStatement pstmt = this.dbConn.prepareStatement(sql)) {
            int indice = 1;
            pstmt.setInt(indice++, ultimoIdLeido);
            for (int idVariable : idsVariables) {
                pstmt.setInt(indice++, idVariable);
            }
            pstmt.setInt(indice, maxRegistros);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    nuevas.add(mapearDatoVariable(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // No se guarda en datosVariables: esta consulta se repite cada pocos
        // milisegundos y acumular sus filas haria crecer la memoria sin limite.
        return nuevas;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int_proceso_vars_data seleccionarUltimoDatoPorVariable(int int_proceso_vars_id) {
        int_proceso_vars_data ultimo = null;
        try (PreparedStatement pstmt = this.dbConn.prepareStatement(SQL_SELECT_LATEST_BY_VAR)) {
            pstmt.setInt(1, int_proceso_vars_id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    ultimo = mapearDatoVariable(rs);
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
    public List<int_proceso_vars_data> seleccionarTodos() {
        List<int_proceso_vars_data> lista = new ArrayList<>();
        try (PreparedStatement pstmt = this.dbConn.prepareStatement(SQL_SELECT_ALL); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearDatoVariable(rs));
            }
            this.datosVariables = lista;
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
                this.datosVariables.removeIf(d -> d.getId() == id);
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
    public int borrarPorVariable(int int_proceso_vars_id) {
        int resRows = 0;
        try (PreparedStatement pstmt = this.dbConn.prepareStatement(SQL_DELETE_BY_VAR)) {
            pstmt.setInt(1, int_proceso_vars_id);
            resRows = pstmt.executeUpdate();
            if (resRows > 0) {
                this.datosVariables.removeIf(d -> d.getInt_proceso_vars_id() == int_proceso_vars_id);
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
            this.datosVariables.clear();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resRows;
    }

    /**
     * Obtiene la lista en memoria de las muestras cargadas en el DAO.
     *
     * @return Lista de objetos {@link int_proceso_vars_data}.
     */
    public List<int_proceso_vars_data> getDatosVariablesEnMemoria() {
        return datosVariables;
    }

    /**
     * Convierte la fila actual de un ResultSet en un objeto
     * {@link int_proceso_vars_data}.
     *
     * @param rs ResultSet posicionado en una fila valida.
     * @return Objeto {@link int_proceso_vars_data} poblado.
     * @exception SQLException Si ocurre un error al leer las columnas.
     */
    private int_proceso_vars_data mapearDatoVariable(ResultSet rs) throws SQLException {
        return new int_proceso_vars_data(
                rs.getInt("id"),
                rs.getInt("int_proceso_vars_id"),
                rs.getFloat("valor"),
                rs.getFloat("tiempo"),
                rs.getDate("fecha"),
                rs.getTime("hora")
        );
    }
}
