package co.edu.univalle.ir.gui.controller;

import co.edu.univalle.ir.conexion.ConexionDB;
import co.edu.univalle.ir.dao.int_usuariosDAO;
import co.edu.univalle.ir.dao.int_usuariosDAOImp;
import co.edu.univalle.ir.dao.int_usuarios_procesosDAO;
import co.edu.univalle.ir.dao.int_usuarios_procesosDAOImp;
import co.edu.univalle.ir.entity.int_usuarios;
import co.edu.univalle.ir.entity.int_usuarios_procesos;
import co.edu.univalle.ir.gui.model.SesionUsuario;
import co.edu.univalle.ir.gui.view.PanelSalidasDigitales;
import co.edu.univalle.ir.gui.view.VentanaLogin;
import co.edu.univalle.ir.gui.view.VentanaPrincipal;
import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.Timer;

/**
 * Controlador unificado del Sprint 2 (HU-07 y HU-08):
 * <ul>
 * <li>Tarea #33: autentica al usuario mediante {@link int_usuariosDAO} y
 * gestiona el control de acceso.</li>
 * <li>Tareas #34, #37 y #39: arranca {@link ControladorGraficacionDAO} (ventana
 * deslizante incremental y cambio de Ts).</li>
 * <li>Tarea #38: monta {@link PanelSalidasDigitales} y arranca
 * {@link ControladorSalidasDigitales} (4 salidas D1..D4 hacia
 * int_proceso_refs_data).</li>
 * </ul>
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.2 $ 2026/11/24
 */
public class ControladorLogin {

    /**
     * Ruta del archivo de configuracion de conexion JDBC de la GUI.
     */
    public static final String RUTA_CONFIG_DB = "db_config.txt";

    /**
     * ID del proceso principal registrado en la tabla int_proceso.
     */
    public static final int ID_PROCESO_DEFECTO = 1;

    /**
     * Limite de intentos fallidos consecutivos antes del bloqueo temporal.
     */
    private static final int MAX_INTENTOS_FALLIDOS = 3;

    /**
     * Duracion del bloqueo temporal en segundos tras exceder intentos fallidos.
     */
    private static final int SEGUNDOS_BLOQUEO = 10;

    private static final Color COLOR_INFO = new Color(9, 132, 227);
    private static final Color COLOR_EXITO = new Color(0, 148, 50);
    private static final Color COLOR_ERROR = new Color(214, 48, 49);

    private final VentanaLogin vistaLogin;
    private final String rutaConfigDb;
    private int intentosFallidos = 0;

    public ControladorLogin(VentanaLogin vistaLogin) {
        this(vistaLogin, RUTA_CONFIG_DB);
    }

    public ControladorLogin(VentanaLogin vistaLogin, String rutaConfigDb) {
        this.vistaLogin = vistaLogin;
        this.rutaConfigDb = rutaConfigDb;
        enlazarEventos();
    }

    private void enlazarEventos() {
        this.vistaLogin.addIngresoListener(e -> procesarIngreso());
    }

    private void procesarIngreso() {
        String email = vistaLogin.getEmail();
        String clave = vistaLogin.getClave();

        if (email.isEmpty() || clave.isEmpty()) {
            vistaLogin.mostrarEstado("Complete correo electronico y contrasena.", COLOR_ERROR);
            JOptionPane.showMessageDialog(
                    vistaLogin,
                    "Debe ingresar su correo electronico y su contrasena para acceder.",
                    "Campos Requeridos",
                    JOptionPane.WARNING_MESSAGE
            );
            if (email.isEmpty()) {
                vistaLogin.enfocarEmail();
            } else {
                vistaLogin.limpiarClave();
            }
            return;
        }

        vistaLogin.setControlesHabilitados(false);
        vistaLogin.mostrarEstado("Consultando credenciales en int_usuarios (MySQL)...", COLOR_INFO);

        SwingWorker<ResultadoAcceso, Void> worker = new SwingWorker<>() {
            @Override
            protected ResultadoAcceso doInBackground() throws Exception {
                ConexionDB conexionDB = new ConexionDB(new File(rutaConfigDb));
                if (!conexionDB.isConectado()) {
                    throw new SQLException("ConexionDB no logro abrir una sesion activa con MySQL.");
                }

                int_usuariosDAO usuarioDAO = new int_usuariosDAOImp(conexionDB);

                int_usuarios usuarioPorEmail = usuarioDAO.seleccionarPorEmail(email);
                if (usuarioPorEmail == null) {
                    conexionDB.cerrarConexion();
                    return ResultadoAcceso.usuarioNoEncontrado();
                }

                int_usuarios usuarioAutenticado = usuarioDAO.validarCredenciales(email, clave);
                if (usuarioAutenticado == null) {
                    conexionDB.cerrarConexion();
                    return ResultadoAcceso.claveIncorrecta();
                }

                int_usuarios_procesos horario = null;
                try {
                    int_usuarios_procesosDAO horariosDAO = new int_usuarios_procesosDAOImp(conexionDB);
                    horario = horariosDAO.seleccionarPorUsuarioYProceso(
                            usuarioAutenticado.getId(),
                            ID_PROCESO_DEFECTO
                    );
                } catch (Exception ignored) {
                }

                SesionUsuario sesion = new SesionUsuario(
                        usuarioAutenticado,
                        conexionDB,
                        ID_PROCESO_DEFECTO,
                        horario
                );
                return ResultadoAcceso.concedido(sesion);
            }

            @Override
            protected void done() {
                try {
                    ResultadoAcceso resultado = get();

                    if (resultado.accesoConcedido) {
                        intentosFallidos = 0;
                        SesionUsuario sesion = resultado.sesion;
                        vistaLogin.mostrarEstado("Acceso concedido. Bienvenido(a), "
                                + sesion.getNombreCompleto() + ".", COLOR_EXITO);

                        System.out.println("[AUTH OK #33] Acceso concedido a: "
                                + sesion.getNombreCompleto() + " [" + sesion.getDescripcionPerfil() + "]");

                        abrirVentanaPrincipal(sesion);
                    } else {
                        gestionarIntentoFallido(resultado.motivo);
                    }

                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    vistaLogin.setControlesHabilitados(true);
                } catch (ExecutionException ex) {
                    vistaLogin.setControlesHabilitados(true);
                    Throwable causa = ex.getCause();
                    String detalle = (causa != null && causa.getMessage() != null)
                            ? causa.getMessage()
                            : ex.getMessage();

                    vistaLogin.mostrarEstado("Error de conexion con la base de datos MySQL.", COLOR_ERROR);
                    System.err.println("[ERROR BD #33] " + detalle);

                    JOptionPane.showMessageDialog(
                            vistaLogin,
                            "No fue posible conectar con la base de datos 'lab_virtual_db'.\n"
                            + "Verifique que MySQL este encendido en XAMPP y revise el archivo '"
                            + rutaConfigDb + "'.\n\nDetalle tecnico: " + detalle,
                            "Error de Conexion con MySQL",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };

        worker.execute();
    }

    private void gestionarIntentoFallido(String motivo) {
        intentosFallidos++;
        int restantes = MAX_INTENTOS_FALLIDOS - intentosFallidos;

        vistaLogin.limpiarClave();
        System.out.println("[AUTH DENEGADO #33] " + motivo
                + " | Intento " + intentosFallidos + "/" + MAX_INTENTOS_FALLIDOS);

        if (restantes > 0) {
            vistaLogin.setControlesHabilitados(true);
            vistaLogin.mostrarEstado(
                    "Acceso denegado (" + motivo + "). Intentos restantes: " + restantes,
                    COLOR_ERROR
            );
            JOptionPane.showMessageDialog(
                    vistaLogin,
                    motivo + "\nIntentos restantes antes de bloqueo temporal: " + restantes,
                    "Acceso Denegado",
                    JOptionPane.ERROR_MESSAGE
            );
        } else {
            iniciarBloqueoTemporal();
        }
    }

    private void iniciarBloqueoTemporal() {
        vistaLogin.setControlesHabilitados(false);
        final int[] segundosRestantes = {SEGUNDOS_BLOQUEO};
        vistaLogin.mostrarEstado(
                "Demasiados intentos fallidos. Reintente en " + segundosRestantes[0] + " s...",
                COLOR_ERROR
        );

        JOptionPane.showMessageDialog(
                vistaLogin,
                "Se alcanzo el maximo de " + MAX_INTENTOS_FALLIDOS + " intentos fallidos.\n"
                + "El acceso quedara bloqueado temporalmente durante " + SEGUNDOS_BLOQUEO + " segundos.",
                "Control de Acceso Activado",
                JOptionPane.WARNING_MESSAGE
        );

        Timer temporizadorBloqueo = new Timer(1000, null);
        temporizadorBloqueo.addActionListener(e -> {
            segundosRestantes[0]--;
            if (segundosRestantes[0] > 0) {
                vistaLogin.mostrarEstado(
                        "Acceso bloqueado temporalmente. Espere " + segundosRestantes[0] + " s...",
                        COLOR_ERROR
                );
            } else {
                temporizadorBloqueo.stop();
                intentosFallidos = 0;
                vistaLogin.setControlesHabilitados(true);
                vistaLogin.mostrarEstado("Bloqueo finalizado. Ingrese sus credenciales.", COLOR_INFO);
                vistaLogin.enfocarEmail();
            }
        });
        temporizadorBloqueo.start();
    }

    /**
     * Despliega VentanaPrincipal integrando graficacion DAO (#34, #37, #39) y
     * salidas digitales D1..D4 (#38), asegurando cierre ordenado al salir o
     * cerrar sesion.
     */
    private void abrirVentanaPrincipal(SesionUsuario sesion) {
        vistaLogin.dispose();

        VentanaPrincipal ventanaPrincipal = new VentanaPrincipal(sesion);

        // 1. Controlador de graficacion incremental y tiempo de muestreo (#34, #37, #39)
        final ControladorGraficacionDAO controladorGraficas = new ControladorGraficacionDAO(
                ventanaPrincipal.getPanelMonitoreoEntradas(),
                sesion,
                rutaConfigDb
        );

        // 2. Panel y controlador de las 4 salidas digitales D1..D4 (#38)
        final ControladorSalidasDigitales controladorSalidas = enlazarSalidasDigitales(ventanaPrincipal, sesion);

        // Cierre seguro al pulsar la X de la ventana (antes de cerrar sesion.getConexionDB())
        ventanaPrincipal.setOnCerrarVentana(() -> {
            controladorGraficas.detener();
            if (controladorSalidas != null) {
                controladorSalidas.detener();
            }
        });

        // Cierre seguro al pulsar el boton "Cerrar Sesion"
        ventanaPrincipal.addCerrarSesionListener(e -> {
            controladorGraficas.detener();
            if (controladorSalidas != null) {
                controladorSalidas.detener();
            }
            sesion.cerrarConexionSegura();
            ventanaPrincipal.dispose();

            VentanaLogin nuevoLogin = new VentanaLogin();
            new ControladorLogin(nuevoLogin, rutaConfigDb);
            nuevoLogin.setVisible(true);
        });

        ventanaPrincipal.setVisible(true);
        controladorGraficas.iniciar();
    }

    /**
     * Tarea #38 (HU-08): monta PanelSalidasDigitales en la zona inferior del
     * panel central con margenes alineados al monitor de entradas y lo enlaza
     * con ControladorSalidasDigitales hacia int_proceso_refs_data.
     */
    private ControladorSalidasDigitales enlazarSalidasDigitales(VentanaPrincipal ventanaPrincipal,
            SesionUsuario sesion) {
        PanelSalidasDigitales panelSalidas = new PanelSalidasDigitales();

        JPanel contenedorSalidas = new JPanel(new BorderLayout());
        contenedorSalidas.setBackground(new Color(243, 246, 250));
        contenedorSalidas.setBorder(BorderFactory.createEmptyBorder(0, 14, 12, 14));
        contenedorSalidas.add(panelSalidas, BorderLayout.CENTER);

        ventanaPrincipal.getPanelContenidoCentral().add(contenedorSalidas, BorderLayout.SOUTH);

        ControladorSalidasDigitales controlador = null;
        try {
            controlador = new ControladorSalidasDigitales(panelSalidas, sesion.getConexionDB());
            controlador.sincronizarDesdeBaseDeDatos();
            System.out.println("[OK #38] ControladorSalidasDigitales iniciado (D1..D4 -> int_proceso_refs_data).");
        } catch (SQLException ex) {
            panelSalidas.setBotonesHabilitados(false);
            panelSalidas.mostrarEstado("No se pudo crear el DAO de las salidas digitales: "
                    + ex.getMessage(), COLOR_ERROR);
            System.err.println("[ERROR BD #38] " + ex.getMessage());
        }

        ventanaPrincipal.pack();
        ventanaPrincipal.setLocationRelativeTo(null);
        return controlador;
    }

    private static final class ResultadoAcceso {

        final boolean accesoConcedido;
        final SesionUsuario sesion;
        final String motivo;

        private ResultadoAcceso(boolean accesoConcedido, SesionUsuario sesion, String motivo) {
            this.accesoConcedido = accesoConcedido;
            this.sesion = sesion;
            this.motivo = motivo;
        }

        static ResultadoAcceso concedido(SesionUsuario sesion) {
            return new ResultadoAcceso(true, sesion, null);
        }

        static ResultadoAcceso usuarioNoEncontrado() {
            return new ResultadoAcceso(false, null, "El correo electronico no se encuentra registrado.");
        }

        static ResultadoAcceso claveIncorrecta() {
            return new ResultadoAcceso(false, null, "Contrasena incorrecta (verifique mayusculas y minusculas).");
        }
    }
}
