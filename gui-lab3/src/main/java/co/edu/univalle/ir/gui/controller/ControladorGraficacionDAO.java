package co.edu.univalle.ir.gui.controller;

import co.edu.univalle.ir.conexion.ConexionDB;
import co.edu.univalle.ir.entity.int_proceso_vars_data;
import co.edu.univalle.ir.gui.model.SesionUsuario;
import co.edu.univalle.ir.gui.view.GraficaTiempo;
import co.edu.univalle.ir.gui.view.PanelMonitoreoEntradas;
import java.awt.Color;
import java.io.File;
import java.util.List;
import javax.swing.JOptionPane;

/**
 * Controlador unificado de las Tareas #34, #37 y #39 (HU-07 y HU-08):
 * <ul>
 * <li>Tarea #39: usa {@link LectorVentanaDeslizante} con consultas
 * incrementales (<code>seleccionarPosterioresA</code>) y redibujo unico por
 * lote sin congelar el EDT.</li>
 * <li>Tarea #37: alimenta en vivo las 8 tarjetas analogicas, los 4 indicadores
 * digitales y ambas graficas JFreeChart.</li>
 * <li>Tarea #34: enlaza el cambio de <code>tiempo_muestreo</code> con
 * {@link ServicioProcesoDB} para actualizar <code>int_proceso</code> via
 * DAO.</li>
 * </ul>
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.1 $ 2026/11/24
 */
public class ControladorGraficacionDAO {

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

    private int canalAnalogicoActivo = 0;
    private int canalDigitalActivo = 0;
    private int tiempoMuestreoVigenteMs = 100;

    private LectorVentanaDeslizante lectorVentana; // #39
    private ServicioProcesoDB servicioProceso;     // #34

    public ControladorGraficacionDAO(PanelMonitoreoEntradas vistaMonitoreo, SesionUsuario sesion) {
        this(vistaMonitoreo, sesion, ControladorLogin.RUTA_CONFIG_DB);
    }

    public ControladorGraficacionDAO(PanelMonitoreoEntradas vistaMonitoreo,
            SesionUsuario sesion, String rutaConfigDb) {
        this.vistaMonitoreo = vistaMonitoreo;
        this.sesion = sesion;
        this.rutaConfigDb = rutaConfigDb;

        inicializarGraficas();
        inicializarVista();
        conectarEventos();
    }

    private void inicializarGraficas() {
        this.graficaAnalogica = new GraficaTiempo("Voltaje (V)", 0.0, 5.0, false, new Color(200, 30, 30));
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
        // 1. Selector canal analogico (0..7)
        vistaMonitoreo.addListenerCanalAnalogico(e -> {
            int nuevoCanal = vistaMonitoreo.getCanalAnalogicoSeleccionado();
            if (nuevoCanal >= 0 && nuevoCanal < PanelMonitoreoEntradas.NUM_ANALOGICAS
                    && nuevoCanal != canalAnalogicoActivo) {
                canalAnalogicoActivo = nuevoCanal;
                graficaAnalogica.cambiarTitulo(NOMBRES_ANALOGICOS[canalAnalogicoActivo]);
                if (lectorVentana != null) {
                    lectorVentana.seleccionarCanales(canalAnalogicoActivo, canalDigitalActivo);
                }
            }
        });

        // 2. Selector canal digital (0..3)
        vistaMonitoreo.addListenerCanalDigital(e -> {
            int nuevoCanal = vistaMonitoreo.getCanalDigitalSeleccionado();
            if (nuevoCanal >= 0 && nuevoCanal < PanelMonitoreoEntradas.NUM_DIGITALES
                    && nuevoCanal != canalDigitalActivo) {
                canalDigitalActivo = nuevoCanal;
                graficaDigital.cambiarTitulo(NOMBRES_DIGITALES[canalDigitalActivo]);
                if (lectorVentana != null) {
                    lectorVentana.seleccionarCanales(canalAnalogicoActivo, canalDigitalActivo);
                }
            }
        });

        // 3. Tarea #34: cambio de tiempo_muestreo en GUI -> int_proceso via DAO
        vistaMonitoreo.addListenerCambiarMuestreo(e -> aplicarNuevoTiempoMuestreo());

        // 4. Pausar / Reanudar lectura incremental
        vistaMonitoreo.addListenerPausarReanudar(e -> {
            if (lectorVentana == null) {
                return;
            }
            boolean pausar = !lectorVentana.isPausado();
            lectorVentana.setPausado(pausar);
            if (pausar) {
                vistaMonitoreo.actualizarEstadoSondeo(
                        false,
                        "Lectura Incremental Pausada",
                        "Presione 'Reanudar Lectura' para continuar consultando int_proceso_vars_data"
                );
            } else {
                vistaMonitoreo.actualizarEstadoSondeo(
                        true,
                        "Lectura Incremental Activa (Ts=" + tiempoMuestreoVigenteMs + " ms)",
                        "Reanudando consulta incremental sobre int_proceso_vars_data..."
                );
            }
        });
    }

    /**
     * Inicia el servicio de proceso (#34) y el lector incremental de ventana
     * deslizante (#39), cada uno con su propia conexion JDBC.
     */
    public void iniciar() {
        try {
            servicioProceso = new ServicioProcesoDB(rutaConfigDb, sesion.getIdProcesoActivo());
            servicioProceso.consultarTiempoMuestreo(ts -> {
                if (ts != ServicioProcesoDB.TS_NO_DISPONIBLE) {
                    tiempoMuestreoVigenteMs = ts;
                    vistaMonitoreo.setTiempoMuestreoActual(ts);
                }
            });

            ConexionDB conexionLector = new ConexionDB(new File(rutaConfigDb));
            lectorVentana = new LectorVentanaDeslizante(conexionLector, this::procesarLoteDB);
            lectorVentana.seleccionarCanales(canalAnalogicoActivo, canalDigitalActivo);
            lectorVentana.iniciar();

            vistaMonitoreo.actualizarEstadoSondeo(
                    true,
                    "Lectura Incremental Activa (100 ms)",
                    "Ventana deslizante optimizada (#39) conectada a int_proceso_vars_data"
            );
            System.out.println("[OK #34/#37/#39] ControladorGraficacionDAO iniciado con ventana deslizante incremental.");
        } catch (Exception ex) {
            vistaMonitoreo.mostrarErrorSondeo("Fallo al iniciar servicios DAO: " + ex.getMessage());
            System.err.println("[ERROR #37/#39] " + ex.getMessage());
        }
    }

    /**
     * Callback ejecutado siempre en el EDT por LectorVentanaDeslizante (#39).
     */
    private void procesarLoteDB(List<double[]> analogica, List<double[]> digital,
            Float[] ultimosAnalogicos, Integer[] ultimosDigitales,
            int_proceso_vars_data ultimaMuestra, boolean reiniciar) {
        if (reiniciar) {
            graficaAnalogica.limpiar();
            graficaDigital.limpiar();
        }
        vistaMonitoreo.actualizarLecturas12Canales(ultimosAnalogicos, ultimosDigitales);
        graficaAnalogica.agregarPuntos(analogica);
        graficaDigital.agregarPuntos(digital);

        String detalle;
        if (ultimaMuestra != null) {
            detalle = String.format(
                    "Ultima muestra DB: ID=%d | t=%.3fs | %s %s | Ts=%d ms | Puntos: ADC=%d, DIG=%d",
                    ultimaMuestra.getId(),
                    ultimaMuestra.getTiempo(),
                    ultimaMuestra.getFecha(),
                    ultimaMuestra.getHora(),
                    tiempoMuestreoVigenteMs,
                    graficaAnalogica.getCantidadPuntos(),
                    graficaDigital.getCantidadPuntos()
            );
        } else {
            detalle = "Tabla int_proceso_vars_data vacia (esperando tramas del puente RS232-SQL)";
        }

        vistaMonitoreo.actualizarEstadoSondeo(
                true,
                "Lectura Incremental Activa (Ts=" + tiempoMuestreoVigenteMs + " ms)",
                detalle
        );
    }

    /**
     * Tarea #34: valida el nuevo Ts ingresado y actualiza int_proceso via DAO
     * fuera del EDT para que el puente RS232 (#29) lo despache al hardware.
     */
    private void aplicarNuevoTiempoMuestreo() {
        if (servicioProceso == null) {
            return;
        }
        final int nuevoTs;
        try {
            nuevoTs = ServicioProcesoDB.validarPeriodoMs(vistaMonitoreo.getTiempoMuestreoTexto());
        } catch (IllegalArgumentException ex) {
            vistaMonitoreo.setTiempoMuestreoActual(tiempoMuestreoVigenteMs);
            JOptionPane.showMessageDialog(
                    vistaMonitoreo,
                    ex.getMessage(),
                    "Tiempo de Muestreo Invalido",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        vistaMonitoreo.setControlesMuestreoHabilitados(false);
        servicioProceso.actualizarTiempoMuestreo(nuevoTs, exito -> {
            vistaMonitoreo.setControlesMuestreoHabilitados(true);
            if (exito) {
                tiempoMuestreoVigenteMs = nuevoTs;
                vistaMonitoreo.setTiempoMuestreoActual(nuevoTs);
                System.out.println("[OK #34] tiempo_muestreo = " + nuevoTs
                        + " ms guardado en int_proceso (listo para despacho del puente #29).");
                JOptionPane.showMessageDialog(
                        vistaMonitoreo,
                        "Tiempo de muestreo actualizado a " + nuevoTs + " ms en int_proceso.\n"
                        + "El puente RS232-SQL lo despachara automaticamente al sistema embebido.",
                        "Muestreo Actualizado",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                vistaMonitoreo.setTiempoMuestreoActual(tiempoMuestreoVigenteMs);
                JOptionPane.showMessageDialog(
                        vistaMonitoreo,
                        "No se pudo actualizar tiempo_muestreo en MySQL.\nVerifique la conexion con XAMPP.",
                        "Error de Base de Datos",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }

    /**
     * Cierra ordenadamente el lector incremental (#39) y el servicio de proceso
     * (#34).
     */
    public void detener() {
        if (lectorVentana != null) {
            lectorVentana.close();
            lectorVentana = null;
        }
        if (servicioProceso != null) {
            servicioProceso.close();
            servicioProceso = null;
        }
        System.out.println("[OK #34/#37/#39] Recursos DB y lector incremental cerrados correctamente.");
    }

    public GraficaTiempo getGraficaAnalogica() {
        return graficaAnalogica;
    }

    public GraficaTiempo getGraficaDigital() {
        return graficaDigital;
    }
}
