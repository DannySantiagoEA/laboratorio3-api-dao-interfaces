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
 * {@link int_proceso_vars_dataDAO} para gestionar las operaciones sobre
 * <code>int_proceso_vars_data</code>, incluyendo la lectura incremental
 * optimizada para ventana deslizante (Tareas #18 y #39).
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.1 $ 2026/11/24
 */
public class int_proceso_vars_dataDAOImp implements int_proceso_vars_dataDAO {

    public static final int NUM_ENTRADAS_ANALOGICAS = 8;
    public static final int NUM_ENTRADAS_DIGITALES = 4;

    private Connection dbConn;
    private List<int_proceso_vars_data> datosVariables;

    private static final String SQL_INSERT
            = "INSERT INTO int_proceso_vars_data "
            + "(int_proceso_vars_id, valor, tiempo, fecha, hora) "
            + "VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_SELECT_BY_ID
            = "SELECT id, int_proceso_vars_id, valor, tiempo, fecha, hora "
            + "FROM int_proceso_vars_data WHERE id = ?";

    private static final String SQL_SELECT_BY_VAR
            = "SELECT id, int_proceso_vars_id, valor, tiempo, fecha, hora "
            + "FROM int_proceso_vars_data "
            + "WHERE int_proceso_vars_id = ? "
            + "ORDER BY id ASC";

    private static final String SQL_SELECT_LAST_N_BY_VAR
            = "SELECT id, int_proceso_vars_id, valor, tiempo, fecha, hora "
            + "FROM int_proceso_vars_data "
            + "WHERE int_proceso_vars_id = ? "
            + "ORDER BY id DESC LIMIT ?";

    private static final String SQL_SELECT_LATEST_BY_VAR
            = "SELECT id, int_proceso_vars_id, valor, tiempo, fecha, hora "
            + "FROM int_proceso_vars_data "
            + "WHERE int_proceso_vars_id = ? "
            + "ORDER BY id DESC LIMIT 1";

    private static final String SQL_SELECT_ALL
            = "SELECT id, int_proceso_vars_id, valor, tiempo, fecha, hora "
            + "FROM int_proceso_vars_data ORDER BY id ASC";

    private static final String SQL_DELETE_BY_ID
            = "DELETE FROM int_proceso_vars_data WHERE id = ?";

    private static final String SQL_DELETE_BY_VAR
            = "DELETE FROM int_proceso_vars_data WHERE int_proceso_vars_id = ?";

    private static final String SQL_DELETE_ALL
            = "DELETE FROM int_proceso_vars_data";

    public int_proceso_vars_dataDAOImp(Connection dbConn) {
        this.dbConn = dbConn;
        this.datosVariables = new ArrayList<>();
    }

    public int_proceso_vars_dataDAOImp(ConexionDB conexionDB) throws SQLException {
        this.dbConn = conexionDB.getConnection();
        this.datosVariables = new ArrayList<>();
    }

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

    @Override
    public int insertarDatoVariable(int int_proceso_vars_id, float valor, float tiempo,
            Date fecha, Time hora) {
        return insertarDatoVariable(new int_proceso_vars_data(
                int_proceso_vars_id, valor, tiempo, fecha, hora
        ));
    }

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

        for (int i = 0; i < NUM_ENTRADAS_ANALOGICAS; i++) {
            lote12Canales.add(new int_proceso_vars_data(
                    idVarBaseAnalogica + i, analogicas8[i], tiempo, fechaActual, horaActual
            ));
        }

        int idVarBaseDigital = idVarBaseAnalogica + NUM_ENTRADAS_ANALOGICAS;
        for (int j = 0; j < NUM_ENTRADAS_DIGITALES; j++) {
            float valorDigital = (digitales4[j] != 0) ? 1.0f : 0.0f;
            lote12Canales.add(new int_proceso_vars_data(
                    idVarBaseDigital + j, valorDigital, tiempo, fechaActual, horaActual
            ));
        }

        return insertarLoteDatosVariables(lote12Canales);
    }

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
            Collections.reverse(listaRecientes);
            this.datosVariables = listaRecientes;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listaRecientes;
    }

    @Override
    public List<int_proceso_vars_data> seleccionarPosterioresA(int[] idsVariables, int ultimoIdLeido, int limite) {
        List<int_proceso_vars_data> nuevas = new ArrayList<>();
        if (idsVariables == null || idsVariables.length == 0) {
            return nuevas;
        }
        int maxRegistros = (limite > 0) ? limite : 500;

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
        return nuevas;
    }

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

    public List<int_proceso_vars_data> getDatosVariablesEnMemoria() {
        return datosVariables;
    }

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
