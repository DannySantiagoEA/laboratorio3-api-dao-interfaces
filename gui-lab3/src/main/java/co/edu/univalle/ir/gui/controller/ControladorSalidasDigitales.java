package co.edu.univalle.ir.gui.controller;

import co.edu.univalle.ir.conexion.ConexionDB;
import co.edu.univalle.ir.dao.int_proceso_refs_dataDAO;
import co.edu.univalle.ir.dao.int_proceso_refs_dataDAOImp;
import co.edu.univalle.ir.gui.view.PanelSalidasDigitales;
import java.awt.Color;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;

/**
 * Controlador de la Tarea #38 (HU-08 - Entregables 3.e y 3.h): enlaza los 4
 * botones de salidas digitales de {@link PanelSalidasDigitales} con
 * {@link int_proceso_refs_dataDAO} para guardar cada conmutacion en la tabla
 * <code>int_proceso_refs_data</code>.
 * <p>
 * Cada vez que el usuario pulsa un boton se inserta <b>un registro</b> con el
 * valor 1.0 (ON) o 0.0 (OFF), el tiempo en segundos desde que se abrio la
 * ventana principal y la fecha y hora (las pone el DAO). La referencia
 * <code>int_proceso_refs_id</code> de cada salida es
 * <code>idRefBase + (numero - 1)</code>, o sea 1..4 para D1..D4.
 * </p>
 * <p>
 * Las escrituras se hacen en un unico hilo secundario para no congelar la
 * interfaz y para que se guarden en el mismo orden en que el usuario pulso los
 * botones. Si el DAO no logra guardar la conmutacion, el boton vuelve a su
 * estado anterior y se avisa en la etiqueta de estado.
 * </p>
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/23
 */
public class ControladorSalidasDigitales {

    /**
     * ID de la primera salida (D1) en int_proceso_refs: D1..D4 son 1..4.
     */
    public static final int ID_REF_BASE_DEFECTO = 1;

    private static final Color COLOR_INFO = new Color(9, 132, 227);
    private static final Color COLOR_EXITO = new Color(0, 148, 50);
    private static final Color COLOR_ERROR = new Color(214, 48, 49);

    private final PanelSalidasDigitales vista;
    private final int_proceso_refs_dataDAO refsDAO;
    private final int idRefBase;
    private final long instanteInicioNanos = System.nanoTime();
    private final ExecutorService hiloBD = Executors.newSingleThreadExecutor(r -> {
        Thread hilo = new Thread(r, "Hilo-GUI-Salidas-DAO");
        hilo.setDaemon(true);
        return hilo;
    });

    /**
     * Ultimo estado de cada salida que SI quedo guardado en la base de datos.
     * Solo se toca desde el EDT.
     */
    private final boolean[] estadoGuardado = new boolean[PanelSalidasDigitales.NUM_SALIDAS];

    /**
     * Crea el controlador con el DAO sobre la conexion de la sesion.
     *
     * @param vista Panel con los 4 botones.
     * @param conexionDB Conexion activa a lab_virtual_db (la de la sesion).
     * @throws SQLException Si el DAO no puede inicializarse.
     */
    public ControladorSalidasDigitales(PanelSalidasDigitales vista, ConexionDB conexionDB)
            throws SQLException {
        this(vista, new int_proceso_refs_dataDAOImp(conexionDB), ID_REF_BASE_DEFECTO);
    }

    /**
     * Constructor con DAO e ID base configurables.
     *
     * @param vista Panel con los 4 botones.
     * @param refsDAO DAO de int_proceso_refs_data.
     * @param idRefBase ID de D1 en int_proceso_refs (usualmente 1).
     */
    public ControladorSalidasDigitales(PanelSalidasDigitales vista,
            int_proceso_refs_dataDAO refsDAO, int idRefBase) {
        this.vista = vista;
        this.refsDAO = refsDAO;
        this.idRefBase = idRefBase;
        this.vista.addSalidaListener(this::procesarConmutacion);
    }

    /**
     * Lee de la base de datos el ultimo estado guardado de D1..D4 y lo pinta
     * en los botones, para que la ventana arranque mostrando lo que hay en la
     * DB (las salidas sin registros se muestran en OFF).
     */
    public void sincronizarDesdeBaseDeDatos() {
        vista.setBotonesHabilitados(false);
        vista.mostrarEstado("Leyendo el ultimo estado de las salidas en la base de datos...", COLOR_INFO);
        try {
            hiloBD.submit(() -> {
                boolean[] estados = null;
                String error = null;
                try {
                    estados = refsDAO.consultarUltimosEstados4Salidas(idRefBase);
                } catch (RuntimeException ex) {
                    error = ex.getMessage();
                }
                final boolean[] leidos = estados;
                final String fallo = error;
                SwingUtilities.invokeLater(() -> {
                    if (leidos == null || leidos.length < PanelSalidasDigitales.NUM_SALIDAS) {
                        vista.mostrarEstado("No se pudo leer el estado de las salidas"
                                + (fallo != null ? ": " + fallo : "."), COLOR_ERROR);
                    } else {
                        for (int i = 0; i < PanelSalidasDigitales.NUM_SALIDAS; i++) {
                            estadoGuardado[i] = leidos[i];
                            vista.setEstadoSalida(i + 1, leidos[i]);
                        }
                        vista.mostrarEstado("Estado de las salidas leido de la base de datos.", COLOR_INFO);
                    }
                    vista.setBotonesHabilitados(true);
                });
            });
        } catch (RejectedExecutionException ex) {
            vista.setBotonesHabilitados(true);
        }
    }

    /**
     * Se ejecuta en el EDT cuando el usuario conmuta un boton: toma el tiempo
     * del clic y encola la insercion en int_proceso_refs_data.
     */
    private void procesarConmutacion(int numeroSalida, boolean encendida) {
        final float tiempo = segundosDesdeInicio();
        final String texto = "D" + numeroSalida + " = " + (encendida ? "ON" : "OFF");
        vista.mostrarEstado("Guardando " + texto + " en int_proceso_refs_data...", COLOR_INFO);

        try {
            hiloBD.submit(() -> {
                int filas = 0;
                String error = null;
                try {
                    filas = refsDAO.registrarEstadoSalidaDigital(numeroSalida, encendida, tiempo, idRefBase);
                } catch (RuntimeException ex) {
                    error = ex.getMessage();
                }
                final boolean guardado = filas > 0;
                final String fallo = error;
                SwingUtilities.invokeLater(() -> {
                    if (guardado) {
                        estadoGuardado[numeroSalida - 1] = encendida;
                        vista.mostrarEstado(String.format("%s guardado en la base de datos (t = %.2f s).",
                                texto, tiempo), COLOR_EXITO);
                    } else {
                        vista.setEstadoSalida(numeroSalida, estadoGuardado[numeroSalida - 1]);
                        vista.mostrarEstado("No se pudo guardar " + texto + ": la salida vuelve a su estado anterior"
                                + (fallo != null ? " (" + fallo + ")" : "") + ".", COLOR_ERROR);
                    }
                });
            });
        } catch (RejectedExecutionException ex) {
            vista.setEstadoSalida(numeroSalida, estadoGuardado[numeroSalida - 1]);
            vista.mostrarEstado("No se pudo guardar " + texto + ": la sesion ya se cerro.", COLOR_ERROR);
        }
    }

    private float segundosDesdeInicio() {
        float segundos = (System.nanoTime() - instanteInicioNanos) / 1_000_000_000.0f;
        return Math.round(segundos * 100.0f) / 100.0f;
    }

    /**
     * Espera a que terminen todas las escrituras pedidas hasta ahora y a que
     * la interfaz pinte sus resultados. Sirve para las pruebas; no debe
     * llamarse desde el EDT.
     *
     * @throws Exception Si la espera se interrumpe o se agota (10 s).
     */
    public void esperarPendientes() throws Exception {
        hiloBD.submit(() -> {
        }).get(10, TimeUnit.SECONDS);
        SwingUtilities.invokeAndWait(() -> {
        });
    }

    /**
     * Termina las escrituras pendientes y cierra el hilo. Debe llamarse antes
     * de cerrar la conexion de la sesion (por ejemplo al cerrar sesion).
     */
    public void detener() {
        hiloBD.shutdown();
        try {
            hiloBD.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
