package co.edu.univalle.ir.puente.sync;

import co.edu.univalle.ir.conexion.ConexionDB;
import co.edu.univalle.ir.dao.int_proceso_vars_dataDAO;
import co.edu.univalle.ir.dao.int_proceso_vars_dataDAOImp;
import co.edu.univalle.ir.puente.serial.SerialBridgeManager;
import co.edu.univalle.ir.puente.serial.TramaTelemetria;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Nucleo de la Tarea #28 (HU-06 - Entregables 3.d, 3.g y 3.h): Rutina asincrona
 * que recibe del puerto serial RS232 las 8 entradas analogicas y las 4 entradas
 * digitales provenientes del sistema embebido y las almacena en la tabla
 * <code>int_proceso_vars_data</code> usando la API DAO.
 * <p>
 * Emplea un patron Productor-Consumidor con {@link LinkedBlockingQueue} para
 * desacoplar la recepcion de bytes del puerto COM de las escrituras en MySQL,
 * evitando bloqueos en el hilo de eventos de jSerialComm.
 * </p>
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/21
 */
public class ReceptorSerialADatabase {

    /**
     * ID inicial de las 8 entradas analogicas en int_proceso_vars (1..8 ADC,
     * 9..12 Dig).
     */
    public static final int ID_VAR_BASE_DEFECTO = 1;

    /**
     * Capacidad maxima de la cola intermedia en memoria para proteger contra
     * saturacion.
     */
    private static final int CAPACIDAD_MAXIMA_COLA = 200;

    private final SerialBridgeManager serialManager;
    private final int_proceso_vars_dataDAO varsDataDAO;
    private final int idVarBaseAnalogica;

    private final BlockingQueue<TramaTelemetria> colaTramas = new LinkedBlockingQueue<>(CAPACIDAD_MAXIMA_COLA);
    private final AtomicBoolean ejecutando = new AtomicBoolean(false);
    private final AtomicLong tramasAlmacenadas = new AtomicLong(0);
    private final AtomicLong muestrasInsertadasTotal = new AtomicLong(0);
    private final AtomicLong tramasDescartadasCola = new AtomicLong(0);

    private Thread hiloPersistencia;
    private long instanteInicioNanos;
    private boolean imprimirCadaTrama = true;

    /**
     * Constructor que recibe el gestor serial y la conexion JDBC de la API DAO.
     *
     * @param serialManager Instancia de {@link SerialBridgeManager} que escucha
     * el puerto COM.
     * @param conexionDB Instancia activa de {@link ConexionDB} de api-dao-lab3.
     * @throws SQLException Si falla la inicializacion de
     * int_proceso_vars_dataDAOImp.
     */
    public ReceptorSerialADatabase(SerialBridgeManager serialManager, ConexionDB conexionDB)
            throws SQLException {
        this(serialManager, new int_proceso_vars_dataDAOImp(conexionDB), ID_VAR_BASE_DEFECTO);
    }

    /**
     * Constructor sobrecargado con DAO e ID base configurables.
     *
     * @param serialManager Gestor serial conectado o listo para conectar.
     * @param varsDataDAO Implementacion del DAO para int_proceso_vars_data.
     * @param idVarBaseAnalogica ID inicial de CH1 en int_proceso_vars
     * (usualmente 1).
     */
    public ReceptorSerialADatabase(SerialBridgeManager serialManager,
            int_proceso_vars_dataDAO varsDataDAO, int idVarBaseAnalogica) {
        this.serialManager = serialManager;
        this.varsDataDAO = varsDataDAO;
        this.idVarBaseAnalogica = idVarBaseAnalogica;
    }

    public void setImprimirCadaTrama(boolean imprimirCadaTrama) {
        this.imprimirCadaTrama = imprimirCadaTrama;
    }

    /**
     * Inicia la escucha de tramas seriales y arranca el hilo consumidor que
     * persiste cada muestra de 12 canales (8 ADC + 4 Digitales) en MySQL.
     */
    public synchronized void iniciar() {
        if (ejecutando.get()) {
            return;
        }

        colaTramas.clear();
        tramasAlmacenadas.set(0);
        muestrasInsertadasTotal.set(0);
        tramasDescartadasCola.set(0);
        instanteInicioNanos = System.nanoTime();
        ejecutando.set(true);

        // 1. Productor: cuando jSerialComm valida una trama, la encola sin bloquear el puerto
        serialManager.setTramaRecibidaListener(trama -> {
            if (!ejecutando.get()) {
                return;
            }
            if (!colaTramas.offer(trama)) {
                // Si la cola se llena, descartar la mas antigua para priorizar telemetria actual
                colaTramas.poll();
                colaTramas.offer(trama);
                tramasDescartadasCola.incrementAndGet();
            }
        });

        // 2. Consumidor: hilo dedicado a insertar las 12 variables en int_proceso_vars_data via DAO
        hiloPersistencia = new Thread(this::buclePersistenciaSQL, "Hilo-Puente-RS232-a-VarsData");
        hiloPersistencia.setDaemon(true);
        hiloPersistencia.start();

        System.out.println("[OK #28] ReceptorSerialADatabase iniciado: almacenando 8 ADC (IDs "
                + idVarBaseAnalogica + ".." + (idVarBaseAnalogica + 7) + ") y 4 Digitales (IDs "
                + (idVarBaseAnalogica + 8) + ".." + (idVarBaseAnalogica + 11) + ") en int_proceso_vars_data.");
    }

    /**
     * Bucle del hilo consumidor que toma cada trama encolada y ejecuta el
     * insertarMuestra12Canales(...) del DAO.
     */
    private void buclePersistenciaSQL() {
        while (ejecutando.get() || !colaTramas.isEmpty()) {
            try {
                TramaTelemetria trama = colaTramas.poll(150, TimeUnit.MILLISECONDS);
                if (trama == null) {
                    continue;
                }

                // Calcular tiempo relativo en segundos con precision de milisegundos
                float tiempoSeg = (trama.getTimestampNanos() - instanteInicioNanos) / 1_000_000_000.0f;
                if (tiempoSeg < 0.0f) {
                    tiempoSeg = 0.0f;
                }
                tiempoSeg = Math.round(tiempoSeg * 1000.0f) / 1000.0f;

                int insertadas = varsDataDAO.insertarMuestra12Canales(
                        trama.getVoltajesAnalogicos(),
                        trama.getEstadosDigitales(),
                        tiempoSeg,
                        idVarBaseAnalogica
                );

                if (insertadas > 0) {
                    long numTramaGuardada = tramasAlmacenadas.incrementAndGet();
                    muestrasInsertadasTotal.addAndGet(insertadas);

                    if (imprimirCadaTrama) {
                        System.out.printf("[RX->DB #%d | t=%.3fs] Guardadas %d/12 variables | %s%n",
                                numTramaGuardada, tiempoSeg, insertadas, trama);
                    }
                } else {
                    System.err.println("[AVISO #28] El DAO no inserto registros para la trama #"
                            + trama.getNumeroSecuencia());
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("[ERROR #28] Fallo al almacenar trama en int_proceso_vars_data: "
                        + e.getMessage());
            }
        }
    }

    /**
     * Detiene limpiamente la recepcion y espera a que el hilo consumidor
     * termine de vaciar las tramas ya recibidas en cola.
     */
    public synchronized void detener() {
        if (!ejecutando.get()) {
            return;
        }
        ejecutando.set(false);
        serialManager.setTramaRecibidaListener(null);

        if (hiloPersistencia != null) {
            try {
                hiloPersistencia.join(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.printf("[OK #28] Receptor detenido | Tramas guardadas: %d | Muestras en DB: %d | Descartadas: %d%n",
                tramasAlmacenadas.get(), muestrasInsertadasTotal.get(), tramasDescartadasCola.get());
    }

    public boolean isEjecutando() {
        return ejecutando.get();
    }

    public long getTramasAlmacenadas() {
        return tramasAlmacenadas.get();
    }

    public long getMuestrasInsertadasTotal() {
        return muestrasInsertadasTotal.get();
    }

    public long getTramasDescartadasCola() {
        return tramasDescartadasCola.get();
    }
}
