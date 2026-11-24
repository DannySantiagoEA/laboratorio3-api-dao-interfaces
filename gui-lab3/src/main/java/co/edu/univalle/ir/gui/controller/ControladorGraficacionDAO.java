package co.edu.univalle.ir.gui.controller;

import co.edu.univalle.ir.conexion.ConexionDB;
import co.edu.univalle.ir.dao.int_procesoDAO;
import co.edu.univalle.ir.dao.int_procesoDAOImp;
import co.edu.univalle.ir.dao.int_proceso_vars_dataDAO;
import co.edu.univalle.ir.dao.int_proceso_vars_dataDAOImp;
import co.edu.univalle.ir.entity.int_proceso_vars_data;
import co.edu.univalle.ir.gui.model.SesionUsuario;
import co.edu.univalle.ir.gui.view.GraficaTiempo;
import co.edu.univalle.ir.gui.view.PanelMonitoreoEntradas;
import java.awt.Color;
import java.io.File;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingWorker;
import javax.swing.Timer;

/**
 * Controlador de graficacion de la Tarea #37 (HU-08 - Entregables 3.d y 3.h):
 * Lee periodicamente las 8 senales analogicas (IDs 1..8) y las 4 senales
 * digitales (IDs 9..12) almacenadas en <code>int_proceso_vars_data</code>
 * mediante {@link int_proceso_vars_dataDAO} y refresca en tiempo real los
 * lienzos {@link GraficaTiempo} y el tablero de 12 canales.
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/23
 */
public class ControladorGraficacionDAO {

    /**
     * ID inicial de las 8 entradas analogicas en int_proceso_vars (CH1..CH8 ->
     * 1..8).
     */
    public static final int ID_BASE_ANALOGICAS = 1;

    /**
     * ID inicial de las 4 entradas digitales en int_proceso_vars (D1..D4 ->
     * 9..12).
     */
    public static final int ID_BASE_DIGITALES = 9;

    /**
     * Cantidad maxima de puntos historicos visibles en la ventana deslizante de
     * cada grafica.
     */
    public static final int VENTANA_MUESTRAS_GRAFICA = 120;

    /**
     * Periodo minimo y maximo (ms) del sondeo periodico de la GUI hacia MySQL.
     */
    private static final int PERIODO_MIN_SONDEO_MS = 150;
    private static final int PERIODO_MAX_SONDEO_MS = 1000;
    private static final int PERIODO_SONDEO_DEFECTO_MS = 250;

    private static final String[] NOMBRES_ANALOGICOS = {
        "Entrada Analogica CH1 (ID=1)", "Entrada Analogica CH2 (ID=2)",
        "Entrada Analogica CH3 (ID=3)", "Entrada Analogica CH4 (ID=4)",
        "Entrada Analogica CH5 (ID=5)", "Entrada Analogica CH6 (ID=6)",
        "Entrada Analogica CH7 (ID=7)", "Entrada Analogica CH8 (ID=8)"
    };

    private static final String[] NOMBRES_DIGITALES = {
        "Entrada Digital D1 (ID=9)",
        "Entrada Digital D2 (ID=10)",
        "Entrada Digital D3 (ID=11)",
        "Entrada Digital D4 (ID=12)"
    };

    private final PanelMonitoreoEntradas vistaMonitoreo;
    private final SesionUsuario sesion;
    private final String rutaConfigDb;

    private GraficaTiempo graficaAnalogica;
    private GraficaTiempo graficaDigital;

    private int canalAnalogicoActivo = 0; // 0..7 -> var_id 1..8
    private int canalDigitalActivo = 0;   // 0..3 -> var_id 9..12
    private int periodoSondeoMs = PERIODO_SONDEO_DEFECTO_MS;

    private Timer timerSondeo;
    private ConexionDB conexionLectura;
    private int_proceso_vars_dataDAO varsDataDAO;

    private final AtomicBoolean consultaEnCurso = new AtomicBoolean(false);
    private boolean sondeoActivo = false;

    /**
     * Crea el controlador de graficacion DAO conectado a
     * PanelMonitoreoEntradas.
     *
     * @param vistaMonitoreo Panel de monitoreo montado en VentanaPrincipal.
     * @param sesion Sesion autenticada proveniente de ControladorLogin.
     */
    public ControladorGraficacionDAO(PanelMonitoreoEntradas vistaMonitoreo, SesionUsuario sesion) {
        this(vistaMonitoreo, sesion, ControladorLogin.RUTA_CONFIG_DB);
    }

    /**
     * Constructor sobrecargado con ruta explicita al archivo db_config.txt.
     *
     * @param vistaMonitoreo Panel de monitoreo de entradas.
     * @param sesion Sesion autenticada del usuario.
     * @param rutaConfigDb Ruta al archivo de credenciales JDBC.
     */
    public ControladorGraficacionDAO(PanelMonitoreoEntradas vistaMonitoreo,
            SesionUsuario sesion, String rutaConfigDb) {
        this.vistaMonitoreo = vistaMonitoreo;
        this.sesion = sesion;
        this.rutaConfigDb = rutaConfigDb;

        inicializarGraficas();
        inicializarVista();
        conectarEventos();
        configurarTimerSondeo();
    }

    private void inicializarGraficas() {
        // Senal analogica: linea continua, 0.0 a 5.0 V, color rojo
        this.graficaAnalogica = new GraficaTiempo("Voltaje (V)", 0.0, 5.0, false, new Color(200, 30, 30));

        // Senal digital: escalon discreto, -0.2 a 1.5, color azul
        this.graficaDigital = new GraficaTiempo("Nivel Logico (0/1)", -0.2, 1.5, true, new Color(30, 110, 200));
    }

    private void inicializarVista() {
        vistaMonitoreo.montarGraficaAnalogica(graficaAnalogica);
        vistaMonitoreo.montarGraficaDigital(graficaDigital);
        vistaMonitoreo.cargarNombresCanales(NOMBRES_ANALOGICOS, NOMBRES_DIGITALES);

        graficaAnalogica.cambiarTitulo(NOMBRES_ANALOGICOS[canalAnalogicoActivo]);
        graficaDigital.cambiarTitulo(NOMBRES_DIGITALES[canalDigitalActivo]);
    }

    private void conectarEventos() {
        // 1. Selector de canal analogico (0..7 -> var_id 1..8)
        vistaMonitoreo.addListenerCanalAnalogico(e -> {
            int nuevoCanal = vistaMonitoreo.getCanalAnalogicoSeleccionado();
            if (nuevoCanal >= 0 && nuevoCanal < PanelMonitoreoEntradas.NUM_ANALOGICAS
                    && nuevoCanal != canalAnalogicoActivo) {
                canalAnalogicoActivo = nuevoCanal;
                graficaAnalogica.cambiarTitulo(NOMBRES_ANALOGICOS[canalAnalogicoActivo]);
                if (sondeoActivo) {
                    ejecutarCicloLecturaAsync();
                }
            }
        });

        // 2. Selector de canal digital (0..3 -> var_id 9..12)
        vistaMonitoreo.addListenerCanalDigital(e -> {
            int nuevoCanal = vistaMonitoreo.getCanalDigitalSeleccionado();
            if (nuevoCanal >= 0 && nuevoCanal < PanelMonitoreoEntradas.NUM_DIGITALES
                    && nuevoCanal != canalDigitalActivo) {
                canalDigitalActivo = nuevoCanal;
                graficaDigital.cambiarTitulo(NOMBRES_DIGITALES[canalDigitalActivo]);
                if (sondeoActivo) {
                    ejecutarCicloLecturaAsync();
                }
            }
        });

        // 3. Boton Pausar / Reanudar lectura periodica DAO
        vistaMonitoreo.addListenerPausarReanudar(e -> {
            if (sondeoActivo) {
                pausar();
            } else {
                reanudar();
            }
        });
    }

    private void configurarTimerSondeo() {
        this.timerSondeo = new Timer(periodoSondeoMs, e -> ejecutarCicloLecturaAsync());
        this.timerSondeo.setCoalesce(true);
    }

    /**
     * Abre la conexion JDBC dedicada para lecturas DAO, consulta el periodo Ts
     * vigente en int_proceso y arranca el sondeo periodico de
     * int_proceso_vars_data.
     */
    public void iniciar() {
        try {
            abrirConexionLecturaSiFalta();
            sincronizarPeriodoDesdeProceso();
            timerSondeo.setDelay(periodoSondeoMs);
            timerSondeo.setInitialDelay(0);
            sondeoActivo = true;
            timerSondeo.start();

            vistaMonitoreo.actualizarEstadoSondeo(
                    true,
                    "Lectura DAO Activa (" + periodoSondeoMs + " ms)",
                    "Consultando 8 ADC + 4 DIG en int_proceso_vars_data..."
            );
            System.out.println("[OK #37] ControladorGraficacionDAO iniciado | Sondeo cada "
                    + periodoSondeoMs + " ms.");
        } catch (Exception ex) {
            vistaMonitoreo.mostrarErrorSondeo("No se pudo iniciar DAO de graficas: " + ex.getMessage());
            System.err.println("[ERROR #37] Fallo al iniciar ControladorGraficacionDAO: " + ex.getMessage());
        }
    }

    private void abrirConexionLecturaSiFalta() throws Exception {
        if (conexionLectura == null || !conexionLectura.isConectado()) {
            conexionLectura = new ConexionDB(new File(rutaConfigDb));
            varsDataDAO = new int_proceso_vars_dataDAOImp(conexionLectura);
        }
    }

    /**
     * Ajusta la frecuencia de refresco de la GUI segun el tiempo_muestreo
     * registrado en int_proceso (acotado para no sobrecargar el EDT ni MySQL).
     */
    private void sincronizarPeriodoDesdeProceso() {
        try {
            int_procesoDAO procesoDAO = new int_procesoDAOImp(conexionLectura);
            float tsMs = procesoDAO.consultarTiempoMuestreo(sesion.getIdProcesoActivo());
            if (tsMs > 0) {
                int calculado = Math.round(tsMs);
                periodoSondeoMs = Math.max(PERIODO_MIN_SONDEO_MS, Math.min(PERIODO_MAX_SONDEO_MS, calculado));
            }
        } catch (SQLException ignored) {
            periodoSondeoMs = PERIODO_SONDEO_DEFECTO_MS;
        }
    }

    /**
     * Permite ajustar externamente el periodo de refresco (por ejemplo, cuando
     * el usuario cambie tiempo_muestreo en la GUI).
     *
     * @param nuevoTsMs Nuevo tiempo de muestreo en milisegundos.
     */
    public void actualizarPeriodoSondeo(int nuevoTsMs) {
        this.periodoSondeoMs = Math.max(PERIODO_MIN_SONDEO_MS, Math.min(PERIODO_MAX_SONDEO_MS, nuevoTsMs));
        if (timerSondeo != null) {
            timerSondeo.setDelay(this.periodoSondeoMs);
        }
    }

    /**
     * Pausa temporalmente las consultas periodicas manteniendo abierta la
     * ventana.
     */
    public void pausar() {
        sondeoActivo = false;
        if (timerSondeo != null && timerSondeo.isRunning()) {
            timerSondeo.stop();
        }
        vistaMonitoreo.actualizarEstadoSondeo(
                false,
                "Lectura DAO Pausada",
                "Presione 'Reanudar Lectura' para continuar consultando int_proceso_vars_data"
        );
    }

    /**
     * Reanuda las consultas periodicas hacia int_proceso_vars_data.
     */
    public void reanudar() {
        if (sondeoActivo) {
            return;
        }
        try {
            abrirConexionLecturaSiFalta();
            sondeoActivo = true;
            timerSondeo.start();
            ejecutarCicloLecturaAsync();
        } catch (Exception ex) {
            vistaMonitoreo.mostrarErrorSondeo("Error al reconectar MySQL: " + ex.getMessage());
        }
    }

    /**
     * Detiene definitivamente el temporizador y cierra la conexion dedicada de
     * lectura.
     */
    public void detener() {
        sondeoActivo = false;
        if (timerSondeo != null) {
            timerSondeo.stop();
        }
        if (conexionLectura != null) {
            try {
                conexionLectura.cerrarConexion();
                System.out.println("[OK #37] Conexion de lectura DAO de graficas cerrada.");
            } catch (Exception ignored) {
            } finally {
                conexionLectura = null;
                varsDataDAO = null;
            }
        }
    }

    /**
     * Lanza una lectura no bloqueante de las 8 senales analogicas y 4 digitales
     * en segundo plano (SwingWorker) y publica los resultados en el EDT.
     */
    private void ejecutarCicloLecturaAsync() {
        if (!sondeoActivo || !consultaEnCurso.compareAndSet(false, true)) {
            return;
        }

        final int idxAna = canalAnalogicoActivo;
        final int idxDig = canalDigitalActivo;
        final int idVarAna = ID_BASE_ANALOGICAS + idxAna;
        final int idVarDig = ID_BASE_DIGITALES + idxDig;

        SwingWorker<InstantaneaEntradas, Void> worker = new SwingWorker<>() {
            @Override
            protected InstantaneaEntradas doInBackground() throws Exception {
                abrirConexionLecturaSiFalta();

                // 1. Leer ultimo valor de las 8 senales analogicas (IDs 1..8)
                Float[] ultimosAna = new Float[PanelMonitoreoEntradas.NUM_ANALOGICAS];
                int_proceso_vars_data referenciaReciente = null;
                for (int i = 0; i < PanelMonitoreoEntradas.NUM_ANALOGICAS; i++) {
                    int_proceso_vars_data dato = varsDataDAO.seleccionarUltimoDatoPorVariable(ID_BASE_ANALOGICAS + i);
                    if (dato != null) {
                        ultimosAna[i] = dato.getValor();
                        if (referenciaReciente == null || dato.getId() > referenciaReciente.getId()) {
                            referenciaReciente = dato;
                        }
                    }
                }

                // 2. Leer ultimo valor de las 4 senales digitales (IDs 9..12)
                Integer[] ultimosDig = new Integer[PanelMonitoreoEntradas.NUM_DIGITALES];
                for (int i = 0; i < PanelMonitoreoEntradas.NUM_DIGITALES; i++) {
                    int_proceso_vars_data dato = varsDataDAO.seleccionarUltimoDatoPorVariable(ID_BASE_DIGITALES + i);
                    if (dato != null) {
                        ultimosDig[i] = (dato.getValor() >= 0.5f) ? 1 : 0;
                        if (referenciaReciente == null || dato.getId() > referenciaReciente.getId()) {
                            referenciaReciente = dato;
                        }
                    }
                }

                // 3. Recuperar series cronologicas de los canales seleccionados para JFreeChart
                List<int_proceso_vars_data> serieAnalogica = varsDataDAO.seleccionarUltimosPorVariable(
                        idVarAna, VENTANA_MUESTRAS_GRAFICA
                );
                List<int_proceso_vars_data> serieDigital = varsDataDAO.seleccionarUltimosPorVariable(
                        idVarDig, VENTANA_MUESTRAS_GRAFICA
                );

                return new InstantaneaEntradas(
                        ultimosAna,
                        ultimosDig,
                        serieAnalogica != null ? serieAnalogica : Collections.emptyList(),
                        serieDigital != null ? serieDigital : Collections.emptyList(),
                        referenciaReciente
                );
            }

            @Override
            protected void done() {
                try {
                    if (!sondeoActivo) {
                        return;
                    }
                    InstantaneaEntradas snap = get();

                    // Actualizar tablero en vivo de las 12 senales (8 ADC + 4 DIG)
                    vistaMonitoreo.actualizarLecturas12Canales(snap.ultimosAnalogicos, snap.ultimosDigitales);

                    // Actualizar ambas graficas JFreeChart
                    graficaAnalogica.actualizarDesdeMuestras(snap.serieAnalogica);
                    graficaDigital.actualizarDesdeMuestras(snap.serieDigital);

                    // Actualizar barra de estado
                    String detalle;
                    if (snap.ultimaReferencia != null) {
                        detalle = String.format(
                                "Ultima muestra DB: ID=%d | t=%.3fs | %s %s | Puntos visibles: ADC=%d, DIG=%d",
                                snap.ultimaReferencia.getId(),
                                snap.ultimaReferencia.getTiempo(),
                                snap.ultimaReferencia.getFecha(),
                                snap.ultimaReferencia.getHora(),
                                snap.serieAnalogica.size(),
                                snap.serieDigital.size()
                        );
                    } else {
                        detalle = "Tabla int_proceso_vars_data vacia (esperando tramas del puente RS232-SQL)";
                    }

                    vistaMonitoreo.actualizarEstadoSondeo(
                            true,
                            "Lectura DAO Activa (" + periodoSondeoMs + " ms)",
                            detalle
                    );

                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    Throwable causa = ex.getCause();
                    String msg = (causa != null && causa.getMessage() != null)
                            ? causa.getMessage()
                            : ex.getMessage();
                    vistaMonitoreo.mostrarErrorSondeo("Error SQL/DAO: " + msg);
                } finally {
                    consultaEnCurso.set(false);
                }
            }
        };

        worker.execute();
    }

    public GraficaTiempo getGraficaAnalogica() {
        return graficaAnalogica;
    }

    public GraficaTiempo getGraficaDigital() {
        return graficaDigital;
    }

    /**
     * Contenedor inmutable para transferir de forma segura los resultados del
     * SwingWorker hacia el Event Dispatch Thread (EDT).
     */
    private static final class InstantaneaEntradas {

        final Float[] ultimosAnalogicos;
        final Integer[] ultimosDigitales;
        final List<int_proceso_vars_data> serieAnalogica;
        final List<int_proceso_vars_data> serieDigital;
        final int_proceso_vars_data ultimaReferencia;

        InstantaneaEntradas(Float[] ultimosAnalogicos, Integer[] ultimosDigitales,
                List<int_proceso_vars_data> serieAnalogica,
                List<int_proceso_vars_data> serieDigital,
                int_proceso_vars_data ultimaReferencia) {
            this.ultimosAnalogicos = ultimosAnalogicos;
            this.ultimosDigitales = ultimosDigitales;
            this.serieAnalogica = serieAnalogica;
            this.serieDigital = serieDigital;
            this.ultimaReferencia = ultimaReferencia;
        }
    }
}
