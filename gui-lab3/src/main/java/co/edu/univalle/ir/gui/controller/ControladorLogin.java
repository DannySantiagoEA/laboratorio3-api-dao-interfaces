package co.edu.univalle.ir.gui.controller;

import co.edu.univalle.ir.conexion.ConexionDB;
import co.edu.univalle.ir.dao.int_usuariosDAO;
import co.edu.univalle.ir.dao.int_usuariosDAOImp;
import co.edu.univalle.ir.dao.int_usuarios_procesosDAO;
import co.edu.univalle.ir.dao.int_usuarios_procesosDAOImp;
import co.edu.univalle.ir.entity.int_usuarios;
import co.edu.univalle.ir.entity.int_usuarios_procesos;
import co.edu.univalle.ir.gui.model.SesionUsuario;
import co.edu.univalle.ir.gui.view.VentanaLogin;
import co.edu.univalle.ir.gui.view.VentanaPrincipal;
import java.awt.Color;
import java.io.File;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.Timer;

/**
 * Controlador de la Tarea #33 (HU-07 - Entregables 3.a y 3.h): Enlaza el boton
 * de ingreso de {@link VentanaLogin} con las consultas filtradas de
 * {@link int_usuariosDAO} y gestiona el control de acceso hacia
 * {@link VentanaPrincipal}.
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/22
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

    /**
     * Crea el controlador enlazado a la vista de login usando db_config.txt.
     *
     * @param vistaLogin Instancia de {@link VentanaLogin}.
     */
    public ControladorLogin(VentanaLogin vistaLogin) {
        this(vistaLogin, RUTA_CONFIG_DB);
    }

    /**
     * Constructor sobrecargado que permite indicar otra ruta de archivo de
     * configuracion.
     *
     * @param vistaLogin Instancia de {@link VentanaLogin}.
     * @param rutaConfigDb Ruta al archivo de credenciales JDBC.
     */
    public ControladorLogin(VentanaLogin vistaLogin, String rutaConfigDb) {
        this.vistaLogin = vistaLogin;
        this.rutaConfigDb = rutaConfigDb;
        enlazarEventos();
    }

    private void enlazarEventos() {
        this.vistaLogin.addIngresoListener(e -> procesarIngreso());
    }

    /**
     * Valida campos y dispara la autenticacion contra int_usuariosDAO en un
     * hilo secundario (SwingWorker) para no congelar la interfaz Swing.
     */
    private void procesarIngreso() {
        String email = vistaLogin.getEmail();
        String clave = vistaLogin.getClave();

        // 1. Validacion defensiva en el EDT
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

        // 2. Deshabilitar controles mientras consulta a MySQL via API DAO
        vistaLogin.setControlesHabilitados(false);
        vistaLogin.mostrarEstado("Consultando credenciales en int_usuarios (MySQL)...", COLOR_INFO);

        // 3. Consulta filtrada y control de acceso fuera del EDT
        SwingWorker<ResultadoAcceso, Void> worker = new SwingWorker<>() {
            @Override
            protected ResultadoAcceso doInBackground() throws Exception {
                ConexionDB conexionDB = new ConexionDB(new File(rutaConfigDb));
                if (!conexionDB.isConectado()) {
                    throw new SQLException("ConexionDB no logro abrir una sesion activa con MySQL.");
                }

                // Nota: si en su version de api-dao-lab3 el constructor recibe Connection, use conexionDB.getConnection()
                int_usuariosDAO usuarioDAO = new int_usuariosDAOImp(conexionDB);

                // Paso A: Consulta filtrada por email en int_usuarios
                int_usuarios usuarioPorEmail = usuarioDAO.seleccionarPorEmail(email);
                if (usuarioPorEmail == null) {
                    conexionDB.cerrarConexion();
                    return ResultadoAcceso.usuarioNoEncontrado();
                }

                // Paso B: Validacion estricta de credenciales (clave sensible a mayusculas)
                int_usuarios usuarioAutenticado = usuarioDAO.validarCredenciales(email, clave);
                if (usuarioAutenticado == null) {
                    conexionDB.cerrarConexion();
                    return ResultadoAcceso.claveIncorrecta();
                }

                // Paso C: Consultar horario asignado en int_usuarios_procesos (control de acceso al proceso)
                int_usuarios_procesos horario = null;
                try {
                    int_usuarios_procesosDAO horariosDAO = new int_usuarios_procesosDAOImp(conexionDB);
                    horario = horariosDAO.seleccionarPorUsuarioYProceso(
                            usuarioAutenticado.getId(),
                            ID_PROCESO_DEFECTO
                    );
                } catch (Exception ignored) {
                    // Si no hay horario cargado, se continua con los privilegios del perfil del usuario
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

    /**
     * Aplica el control de intentos fallidos y bloquea temporalmente el acceso
     * tras alcanzar MAX_INTENTOS_FALLIDOS.
     */
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

    /**
     * Bloquea los controles de inicio de sesion durante SEGUNDOS_BLOQUEO usando
     * un javax.swing.Timer para no bloquear el EDT.
     */
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
     * Cierra el formulario de login, despliega VentanaPrincipal y enlaza el
     * boton Cerrar Sesion para retornar al control de acceso de forma segura.
     */
    private void abrirVentanaPrincipal(SesionUsuario sesion) {
        vistaLogin.dispose();

        VentanaPrincipal ventanaPrincipal = new VentanaPrincipal(sesion);
        ventanaPrincipal.addCerrarSesionListener(e -> {
            sesion.cerrarConexionSegura();
            ventanaPrincipal.dispose();

            VentanaLogin nuevoLogin = new VentanaLogin();
            new ControladorLogin(nuevoLogin, rutaConfigDb);
            nuevoLogin.setVisible(true);
        });
        ventanaPrincipal.setVisible(true);
    }

    /**
     * Estructura interna inmutable para transportar el resultado del
     * SwingWorker al EDT.
     */
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
