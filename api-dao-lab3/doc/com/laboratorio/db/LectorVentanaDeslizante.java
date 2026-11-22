package com.laboratorio.db;

import co.edu.univalle.ir.conexion.ConexionDB;
import co.edu.univalle.ir.dao.int_proceso_vars_dataDAO;
import co.edu.univalle.ir.dao.int_proceso_vars_dataDAOImp;
import co.edu.univalle.ir.entity.int_proceso_vars_data;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.SwingUtilities;

/**
 * Tarea #39 (Entregable 3.d): lectura en ventana deslizante de
 * <code>int_proceso_vars_data</code> para graficar en la GUI sin congelar el
 * Event Dispatch Thread (EDT) de Swing.
 * <p>
 * Optimizaciones respecto a consultar la tabla desde un Timer de Swing:
 * </p>
 * <ol>
 * <li>Las consultas corren en un hilo propio con retardo fijo; el EDT solo
 * recibe listas ya armadas y pinta.</li>
 * <li>Lectura incremental: se recuerda el ID de la ultima fila leida y cada
 * ciclo trae solo las filas nuevas (<code>id &gt; ultimoId</code>) de las dos
 * variables visibles, en una sola consulta. La ventana completa
 * (<code>TAM_VENTANA_INICIAL</code> puntos por variable) solo se lee al
 * arrancar o al cambiar de canal.</li>
 * <li>Contrapresion: si el EDT aun no ha pintado el lote anterior, el ciclo se
 * salta; los datos no se pierden porque siguen en la DB y se leen en el
 * siguiente ciclo.</li>
 * <li>Los lotes que llegan despues de un cambio de canal se descartan (contador
 * de generacion), para no mezclar datos de dos variables.</li>
 * <li>Conexion JDBC exclusiva para este hilo.</li>
 * </ol>
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/22
 */
public class LectorVentanaDeslizante implements AutoCloseable {

    /**
     * ID en int_proceso_vars de la entrada Analogica 1 (Analogica 1..8 son los
     * IDs 1..8).
     */
    public static final int ID_VAR_ANALOGICA_BASE = 1;

    /**
     * ID en int_proceso_vars de la entrada Digital 1 (Digital 1..4 son los IDs
     * 9..12).
     */
    public static final int ID_VAR_DIGITAL_BASE = 9;

    /**
     * Periodo entre consultas (ms).
     */
    public static final long PERIODO_LECTURA_MS = 100;

    /**
     * Puntos por variable que se cargan al arrancar o al cambiar de canal.
     */
    public static final int TAM_VENTANA_INICIAL = 500;

    /**
     * Maximo de filas nuevas por consulta incremental.
     */
    public static final int LIMITE_POR_CONSULTA = 1000;

    /**
     * Receptor de los lotes de puntos. Se ejecuta siempre en el EDT.
     */
    @FunctionalInterface
    public interface OyenteVentana {

        /**
         * Entrega un lote de puntos {tiempo (s), valor} para cada grafica.
         *
         * @param analogica Puntos nuevos de la variable analogica visible.
         * @param digital Puntos nuevos de la variable digital visible.
         * @param reiniciar true si las graficas deben limpiarse antes de
         * agregar el lote (arranque o cambio de canal).
         */
        void alRecibirLote(List<double[]> analogica, List<double[]> digital, boolean reiniciar);
    }

    /**
     * Conexion JDBC exclusiva del hilo lector.
     */
    private final ConexionDB conexion;

    /**
     * DAO de int_proceso_vars_data.
     */
    private final int_proceso_vars_dataDAO varsDataDAO;

    /**
     * Quien recibe los lotes en el EDT (el controlador de la GUI).
     */
    private final OyenteVentana oyente;

    /**
     * Hilo que ejecuta las consultas.
     */
    private ScheduledExecutorService planificador;

    /**
     * ID de la variable analogica visible.
     */
    private volatile int idVarAnalogica = ID_VAR_ANALOGICA_BASE;

    /**
     * ID de la variable digital visible.
     */
    private volatile int idVarDigital = ID_VAR_DIGITAL_BASE;

    /**
     * Indica que el siguiente ciclo debe recargar la ventana completa.
     */
    private final AtomicBoolean recargarVentana = new AtomicBoolean(true);

    /**
     * Aumenta en cada cambio de canal; permite descartar lotes viejos.
     */
    private final AtomicInteger generacion = new AtomicInteger();

    /**
     * true mientras haya un lote esperando a ser pintado en el EDT.
     */
    private final AtomicBoolean loteEnCamino = new AtomicBoolean(false);

    /**
     * ID de la ultima fila leida. Solo lo usa el hilo lector.
     */
    private int ultimoIdLeido = 0;

    /**
     * Crea el lector.
     *
     * @param conexionExclusiva Conexion JDBC que usara solo este lector.
     * @param oyente Receptor de los lotes (se invoca en el EDT).
     * @exception SQLException Si falla la creacion del DAO.
     */
    public LectorVentanaDeslizante(ConexionDB conexionExclusiva, OyenteVentana oyente) throws SQLException {
        this.conexion = conexionExclusiva;
        this.varsDataDAO = new int_proceso_vars_dataDAOImp(conexionExclusiva);
        this.oyente = oyente;
    }

    /**
     * Cambia las variables que se grafican. El siguiente ciclo carga la
     * ventana completa de las nuevas variables.
     *
     * @param canalAnalogico Integer de 0 a 7 (indice del selector analogico).
     * @param canalDigital Integer de 0 a 3 (indice del selector digital).
     */
    public void seleccionarCanales(int canalAnalogico, int canalDigital) {
        this.idVarAnalogica = ID_VAR_ANALOGICA_BASE + canalAnalogico;
        this.idVarDigital = ID_VAR_DIGITAL_BASE + canalDigital;
        generacion.incrementAndGet();
        recargarVentana.set(true);
    }

    /**
     * Arranca el hilo lector.
     */
    public synchronized void iniciar() {
        if (planificador != null) {
            return;
        }
        planificador = Executors.newSingleThreadScheduledExecutor(tarea -> {
            Thread hilo = new Thread(tarea, "Hilo-GUI-Lector-VarsData");
            hilo.setDaemon(true);
            return hilo;
        });
        planificador.scheduleWithFixedDelay(this::cicloLectura, 0, PERIODO_LECTURA_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * Un ciclo de lectura. Atrapa cualquier excepcion para que el hilo nunca
     * se detenga en silencio.
     */
    private void cicloLectura() {
        boolean esRecarga = false;
        try {
            if (loteEnCamino.get()) {
                return;   // el EDT aun no pinta el lote anterior
            }
            esRecarga = recargarVentana.getAndSet(false);
            final boolean reiniciar = esRecarga;
            final int generacionLote = generacion.get();
            final int idA = idVarAnalogica;
            final int idD = idVarDigital;

            List<int_proceso_vars_data> filas;
            if (reiniciar) {
                filas = new ArrayList<>(varsDataDAO.seleccionarUltimosPorVariable(idA, TAM_VENTANA_INICIAL));
                filas.addAll(varsDataDAO.seleccionarUltimosPorVariable(idD, TAM_VENTANA_INICIAL));
            } else {
                filas = varsDataDAO.seleccionarPosterioresA(new int[]{idA, idD}, ultimoIdLeido, LIMITE_POR_CONSULTA);
            }

            final List<double[]> analogica = new ArrayList<>();
            final List<double[]> digital = new ArrayList<>();
            for (int_proceso_vars_data fila : filas) {
                ultimoIdLeido = Math.max(ultimoIdLeido, fila.getId());
                double[] punto = {fila.getTiempo(), fila.getValor()};
                if (fila.getInt_proceso_vars_id() == idA) {
                    analogica.add(punto);
                } else {
                    digital.add(punto);
                }
            }
            if (!reiniciar && analogica.isEmpty() && digital.isEmpty()) {
                return;   // no hay filas nuevas: no se molesta al EDT
            }

            loteEnCamino.set(true);
            SwingUtilities.invokeLater(() -> {
                try {
                    if (generacionLote == generacion.get()) {
                        oyente.alRecibirLote(analogica, digital, reiniciar);
                    }
                } finally {
                    loteEnCamino.set(false);
                }
            });
        } catch (Exception e) {
            if (esRecarga) {
                recargarVentana.set(true);   // se reintenta la recarga en el siguiente ciclo
            }
            System.err.println("[ERROR #39] Lectura de int_proceso_vars_data: " + e.getMessage());
        }
    }

    /**
     * Detiene el hilo lector y cierra su conexion.
     */
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
        try {
            conexion.cerrarConexion();
        } catch (SQLException ignored) {
        }
    }
}