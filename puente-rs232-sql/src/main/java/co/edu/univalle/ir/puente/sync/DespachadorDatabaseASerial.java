package co.edu.univalle.ir.puente.sync;

import co.edu.univalle.ir.conexion.ConexionDB;
import co.edu.univalle.ir.dao.int_procesoDAO;
import co.edu.univalle.ir.dao.int_procesoDAOImp;
import co.edu.univalle.ir.dao.int_proceso_refs_dataDAO;
import co.edu.univalle.ir.dao.int_proceso_refs_dataDAOImp;
import co.edu.univalle.ir.puente.serial.SerialBridgeManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Nucleo de la Tarea #29 (HU-06 - Entregables 3.e, 3.f y 3.g): Hilo periodico
 * con conexion JDBC exclusiva que consulta en MySQL:
 * <ul>
 * <li><code>int_proceso.tiempo_muestreo</code> via {@link int_procesoDAO} -&gt;
 * comando [0xAA, 0x02, MSB, LSB, 0x55].</li>
 * <li>Ultimos estados de las 4 salidas digitales en
 * <code>int_proceso_refs_data</code> via {@link int_proceso_refs_dataDAO} -&gt;
 * comando [0xAA, 0x01, MASK, 0x55].</li>
 * </ul>
 * Solo despacha al puerto serial cuando detecta un cambio respecto al ultimo
 * valor enviado al microcontrolador, evitando saturar el canal UART.
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/24
 */
public class DespachadorDatabaseASerial implements AutoCloseable {

    public static final int ID_PROCESO_DEFECTO = 1;
    public static final int ID_REF_BASE_DEFECTO = 1;
    public static final long PERIODO_SONDEO_MS = 150L;

    private final SerialBridgeManager serialManager;
    private final ConexionDB conexionExclusiva;
    private final int_procesoDAO procesoDAO;
    private final int_proceso_refs_dataDAO refsDataDAO;
    private final int idProceso;
    private final int idRefBase;
    private final long periodoSondeoMs;

    private ScheduledExecutorService planificador;
    private Integer ultimoTsDespachado = null;
    private boolean[] ultimasSalidasDespachadas = null;

    private final AtomicLong comandosTsEnviados = new AtomicLong(0);
    private final AtomicLong comandosLedsEnviados = new AtomicLong(0);

    public DespachadorDatabaseASerial(SerialBridgeManager serialManager, ConexionDB conexionExclusiva)
            throws SQLException {
        this(serialManager, conexionExclusiva, ID_PROCESO_DEFECTO, ID_REF_BASE_DEFECTO, PERIODO_SONDEO_MS);
    }

    public DespachadorDatabaseASerial(SerialBridgeManager serialManager, ConexionDB conexionExclusiva,
            int idProceso, int idRefBase, long periodoSondeoMs) throws SQLException {
        this.serialManager = serialManager;
        this.conexionExclusiva = conexionExclusiva;
        this.procesoDAO = new int_procesoDAOImp(conexionExclusiva);
        this.refsDataDAO = new int_proceso_refs_dataDAOImp(conexionExclusiva);
        this.idProceso = idProceso;
        this.idRefBase = idRefBase;
        this.periodoSondeoMs = Math.max(50L, periodoSondeoMs);
    }

    /**
     * Arranca el hilo de consulta periodica DB -> RS232.
     */
    public synchronized void iniciar() {
        if (planificador != null) {
            return;
        }
        ultimoTsDespachado = null;
        ultimasSalidasDespachadas = null;

        planificador = Executors.newSingleThreadScheduledExecutor(tarea -> {
            Thread hilo = new Thread(tarea, "Hilo-Puente-DB-a-RS232");
            hilo.setDaemon(true);
            return hilo;
        });
        planificador.scheduleWithFixedDelay(this::cicloConsultaYDespacho, 0, periodoSondeoMs, TimeUnit.MILLISECONDS);
        System.out.println("[OK #29] DespachadorDatabaseASerial iniciado (sondeo cada " + periodoSondeoMs + " ms).");
    }

    private void cicloConsultaYDespacho() {
        if (!serialManager.isConectado()) {
            return;
        }
        try {
            // 1. Verificar tiempo_muestreo en int_proceso (Entregable 3.f)
            float tsDb = procesoDAO.consultarTiempoMuestreo(idProceso);
            if (tsDb >= 10.0f && tsDb <= 65535.0f) {
                int tsMs = Math.round(tsDb);
                if (ultimoTsDespachado == null || ultimoTsDespachado != tsMs) {
                    serialManager.enviarComandoMuestreo(tsMs);
                    ultimoTsDespachado = tsMs;
                    long num = comandosTsEnviados.incrementAndGet();
                    System.out.printf("[DB->TX #%d] tiempo_muestreo actualizado -> enviado Ts = %d ms al hardware.%n",
                            num, tsMs);
                }
            }

            // 2. Verificar ultimos estados de las 4 salidas digitales en int_proceso_refs_data (Entregable 3.e)
            boolean[] salidasDb = refsDataDAO.consultarUltimosEstados4Salidas(idRefBase);
            if (salidasDb != null && salidasDb.length >= 4) {
                if (ultimasSalidasDespachadas == null || !Arrays.equals(ultimasSalidasDespachadas, salidasDb)) {
                    serialManager.enviarComandoLeds(salidasDb);
                    ultimasSalidasDespachadas = salidasDb.clone();
                    long num = comandosLedsEnviados.incrementAndGet();
                    System.out.printf("[DB->TX #%d] 4 salidas digitales actualizadas -> enviado %s al hardware.%n",
                            num, Arrays.toString(salidasDb));
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR #29] Ciclo de despacho DB -> RS232: " + e.getMessage());
        }
    }

    public long getComandosTsEnviados() {
        return comandosTsEnviados.get();
    }

    public long getComandosLedsEnviados() {
        return comandosLedsEnviados.get();
    }

    public synchronized void detener() {
        close();
    }

    @Override
    public synchronized void close() {
        if (planificador != null) {
            planificador.shutdown();
            try {
                planificador.awaitTermination(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            planificador = null;
        }
        if (conexionExclusiva != null) {
            try {
                conexionExclusiva.cerrarConexion();
            } catch (Exception ignored) {
            }
        }
        System.out.printf("[OK #29] Despachador detenido | Comandos Ts enviados: %d | Comandos LEDs enviados: %d%n",
                comandosTsEnviados.get(), comandosLedsEnviados.get());
    }
}
