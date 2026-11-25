package co.edu.univalle.ir.gui.controller;

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
 * Tarea #39 integrada con Tarea #37 (Entregable 3.d): Lectura optimizada en
 * ventana deslizante sobre <code>int_proceso_vars_data</code> sin congelar el
 * Event Dispatch Thread (EDT) de Swing.
 * <p>
 * Combina las 5 optimizaciones de la Tarea #39 (hilo dedicado, consulta
 * incremental <code>seleccionarPosterioresA</code>, contrapresion
 * <code>loteEnCamino</code>, contador de <code>generacion</code> y conexion
 * JDBC exclusiva) entregando ademas los ultimos valores vigentes de los 12
 * canales (8 analogicas + 4 digitales) en una sola consulta por ciclo.
 * </p>
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.1 $ 2026/11/24
 */
public class LectorVentanaDeslizante implements AutoCloseable {

    public static final int ID_VAR_ANALOGICA_BASE = 1;
    public static final int ID_VAR_DIGITAL_BASE = 9;
    public static final long PERIODO_LECTURA_MS = 100L;
    public static final int TAM_VENTANA_INICIAL = 300;
    public static final int LIMITE_POR_CONSULTA = 1000;

    private static final int[] IDS_12_CANALES = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};

    /**
     * Receptor de los lotes de puntos y telemetria de 12 canales (siempre en el
     * EDT).
     */
    @FunctionalInterface
    public interface OyenteVentana {

        void alRecibirLote(List<double[]> analogica, List<double[]> digital,
                Float[] ultimosAnalogicos, Integer[] ultimosDigitales,
                int_proceso_vars_data ultimaMuestra, boolean reiniciar);
    }

    private final ConexionDB conexion;
    private final int_proceso_vars_dataDAO varsDataDAO;
    private final OyenteVentana oyente;

    private ScheduledExecutorService planificador;
    private volatile int idVarAnalogica = ID_VAR_ANALOGICA_BASE;
    private volatile int idVarDigital = ID_VAR_DIGITAL_BASE;
    private volatile boolean pausado = false;

    private final AtomicBoolean recargarVentana = new AtomicBoolean(true);
    private final AtomicInteger generacion = new AtomicInteger();
    private final AtomicBoolean loteEnCamino = new AtomicBoolean(false);

    // Estado privado del hilo lector
    private int ultimoIdLeido = 0;
    private final Float[] cacheUltimosAnalogicos = new Float[8];
    private final Integer[] cacheUltimosDigitales = new Integer[4];
    private int_proceso_vars_data ultimaMuestraGlobal = null;

    public LectorVentanaDeslizante(ConexionDB conexionExclusiva, OyenteVentana oyente) throws SQLException {
        this.conexion = conexionExclusiva;
        this.varsDataDAO = new int_proceso_vars_dataDAOImp(conexionExclusiva);
        this.oyente = oyente;
    }

    public void seleccionarCanales(int canalAnalogico, int canalDigital) {
        this.idVarAnalogica = ID_VAR_ANALOGICA_BASE + canalAnalogico;
        this.idVarDigital = ID_VAR_DIGITAL_BASE + canalDigital;
        generacion.incrementAndGet();
        recargarVentana.set(true);
    }

    public void setPausado(boolean pausado) {
        this.pausado = pausado;
        if (!pausado) {
            recargarVentana.set(true);
        }
    }

    public boolean isPausado() {
        return pausado;
    }

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

    private void cicloLectura() {
        if (pausado) {
            return;
        }
        boolean esRecarga = false;
        try {
            if (loteEnCamino.get()) {
                return; // Contrapresion: el EDT aun no termina de pintar el lote anterior
            }
            esRecarga = recargarVentana.getAndSet(false);
            final boolean reiniciar = esRecarga;
            final int generacionLote = generacion.get();
            final int idA = idVarAnalogica;
            final int idD = idVarDigital;

            final List<double[]> analogica = new ArrayList<>();
            final List<double[]> digital = new ArrayList<>();

            if (reiniciar) {
                ultimoIdLeido = 0;

                // 1. Cargar ventana inicial de los dos canales graficados
                List<int_proceso_vars_data> histA = varsDataDAO.seleccionarUltimosPorVariable(idA, TAM_VENTANA_INICIAL);
                List<int_proceso_vars_data> histD = varsDataDAO.seleccionarUltimosPorVariable(idD, TAM_VENTANA_INICIAL);

                for (int_proceso_vars_data f : histA) {
                    actualizarMetadatos(f);
                    analogica.add(new double[]{f.getTiempo(), f.getValor()});
                }
                for (int_proceso_vars_data f : histD) {
                    actualizarMetadatos(f);
                    digital.add(new double[]{f.getTiempo(), f.getValor()});
                }

                // 2. Hidratar la lectura mas reciente de los 12 canales (8 ADC + 4 DIG)
                for (int idVar : IDS_12_CANALES) {
                    int_proceso_vars_data u = varsDataDAO.seleccionarUltimoDatoPorVariable(idVar);
                    if (u != null) {
                        actualizarMetadatos(u);
                    }
                }
            } else {
                // Lectura incremental (Tarea #39): 1 sola consulta SQL para los 12 canales con id > ultimoIdLeido
                List<int_proceso_vars_data> filasNuevas = varsDataDAO.seleccionarPosterioresA(
                        IDS_12_CANALES, ultimoIdLeido, LIMITE_POR_CONSULTA
                );
                if (filasNuevas.isEmpty()) {
                    return; // Sin datos nuevos: no se despierta al EDT
                }
                for (int_proceso_vars_data f : filasNuevas) {
                    actualizarMetadatos(f);
                    if (f.getInt_proceso_vars_id() == idA) {
                        analogica.add(new double[]{f.getTiempo(), f.getValor()});
                    } else if (f.getInt_proceso_vars_id() == idD) {
                        digital.add(new double[]{f.getTiempo(), f.getValor()});
                    }
                }
            }

            final Float[] copiaAna = cacheUltimosAnalogicos.clone();
            final Integer[] copiaDig = cacheUltimosDigitales.clone();
            final int_proceso_vars_data refReciente = ultimaMuestraGlobal;

            loteEnCamino.set(true);
            SwingUtilities.invokeLater(() -> {
                try {
                    if (generacionLote == generacion.get()) {
                        oyente.alRecibirLote(analogica, digital, copiaAna, copiaDig, refReciente, reiniciar);
                    }
                } finally {
                    loteEnCamino.set(false);
                }
            });
        } catch (Exception e) {
            if (esRecarga) {
                recargarVentana.set(true);
            }
            System.err.println("[ERROR #39] Lectura incremental int_proceso_vars_data: " + e.getMessage());
        }
    }

    private void actualizarMetadatos(int_proceso_vars_data fila) {
        if (fila.getId() > ultimoIdLeido) {
            ultimoIdLeido = fila.getId();
            ultimaMuestraGlobal = fila;
        }
        int idVar = fila.getInt_proceso_vars_id();
        if (idVar >= ID_VAR_ANALOGICA_BASE && idVar < ID_VAR_ANALOGICA_BASE + 8) {
            cacheUltimosAnalogicos[idVar - ID_VAR_ANALOGICA_BASE] = fila.getValor();
        } else if (idVar >= ID_VAR_DIGITAL_BASE && idVar < ID_VAR_DIGITAL_BASE + 4) {
            cacheUltimosDigitales[idVar - ID_VAR_DIGITAL_BASE] = (fila.getValor() >= 0.5f) ? 1 : 0;
        }
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
        try {
            conexion.cerrarConexion();
        } catch (SQLException ignored) {
        }
    }
}
