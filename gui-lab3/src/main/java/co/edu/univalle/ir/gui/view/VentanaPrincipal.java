package co.edu.univalle.ir.gui.view;

import co.edu.univalle.ir.entity.int_usuarios_procesos;
import co.edu.univalle.ir.gui.model.SesionUsuario;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Ventana principal protegida de la GUI del Laboratorio Virtual (HU-07 y
 * HU-08).
 * <p>
 * Solo es instanciada por
 * {@link co.edu.univalle.ir.gui.controller.ControladorLogin} tras validar las
 * credenciales contra <code>int_usuarios</code>.
 * </p>
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/22
 */
public class VentanaPrincipal extends JFrame {

    private final SesionUsuario sesion;
    private JButton btnCerrarSesion;
    private JPanel panelContenidoCentral;

    public VentanaPrincipal(SesionUsuario sesion) {
        if (sesion == null || sesion.getUsuario() == null) {
            throw new IllegalStateException("Acceso denegado: se requiere una sesion autenticada.");
        }
        this.sesion = sesion;
        inicializarComponentes();
        configurarCierreSeguro();
    }

    private void inicializarComponentes() {
        setTitle("Laboratorio Virtual Univalle - Panel de Supervision y Control (DAO)");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(980, 620));
        getContentPane().setLayout(new BorderLayout());

        // 1. Barra superior con datos del usuario autenticado y boton Cerrar Sesion
        JPanel barraSuperior = new JPanel(new BorderLayout());
        barraSuperior.setBackground(new Color(24, 43, 73));
        barraSuperior.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));

        JLabel lblSistema = new JLabel("LABORATORIO VIRTUAL | Procesos en Tiempo Real via API DAO");
        lblSistema.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblSistema.setForeground(Color.WHITE);

        JPanel panelUsuario = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        panelUsuario.setOpaque(false);

        JLabel lblUsuario = new JLabel(
                "Usuario: " + sesion.getNombreCompleto()
                + " [" + sesion.getDescripcionPerfil() + "]"
        );
        lblUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblUsuario.setForeground(new Color(129, 236, 236));

        btnCerrarSesion = new JButton("Cerrar Sesion");
        btnCerrarSesion.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCerrarSesion.setBackground(new Color(192, 57, 43));
        btnCerrarSesion.setForeground(Color.WHITE);
        btnCerrarSesion.setFocusPainted(false);
        btnCerrarSesion.setContentAreaFilled(false);
        btnCerrarSesion.setOpaque(true);
        btnCerrarSesion.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCerrarSesion.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));

        panelUsuario.add(lblUsuario);
        panelUsuario.add(btnCerrarSesion);

        barraSuperior.add(lblSistema, BorderLayout.WEST);
        barraSuperior.add(panelUsuario, BorderLayout.EAST);

        // 2. Panel central (alojara el control de tiempo de muestreo HU-07 y graficas/salidas HU-08)
        panelContenidoCentral = new JPanel(new BorderLayout());
        panelContenidoCentral.setBackground(new Color(245, 247, 250));
        panelContenidoCentral.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        int_usuarios_procesos horario = sesion.getHorarioAsignado();
        String textoHorario = (horario != null)
                ? String.format("Horario asignado al Proceso #%d: %s a %s (%s - %s)",
                        sesion.getIdProcesoActivo(),
                        horario.getFecha_inicio(), horario.getFecha_fin(),
                        horario.getHora_inicio(), horario.getHora_fin())
                : "Acceso habilitado para el Proceso #" + sesion.getIdProcesoActivo()
                + (sesion.isAdministrador() ? " (Privilegio Administrador)" : " (Sin restriccion horaria registrada)");

        JLabel lblResumen = new JLabel(
                "<html><div style='text-align:center;'>"
                + "<h2>Acceso concedido correctamente (Tarea #33 - HU-07)</h2>"
                + "<p>Operador autenticado: <b>" + sesion.getNombreCompleto() + "</b> (" + sesion.getUsuario().getEmail() + ")</p>"
                + "<p>Perfil: <b>" + sesion.getDescripcionPerfil() + "</b> | Base de datos: <code>"
                + sesion.getConexionDB().getBaseDatos() + "</code></p>"
                + "<p style='color:#2d3436; margin-top:8px;'>" + textoHorario + "</p>"
                + "<p style='color:#636e72; margin-top:16px;'>Contenedor listo para integrar el control de <code>tiempo_muestreo</code> (HU-07) y paneles de graficacion/salidas (HU-08).</p>"
                + "</div></html>",
                SwingConstants.CENTER
        );
        lblResumen.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panelContenidoCentral.add(lblResumen, BorderLayout.CENTER);

        getContentPane().add(barraSuperior, BorderLayout.NORTH);
        getContentPane().add(panelContenidoCentral, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }

    private void configurarCierreSeguro() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                sesion.cerrarConexionSegura();
            }
        });
    }

    public SesionUsuario getSesion() {
        return sesion;
    }

    public JPanel getPanelContenidoCentral() {
        return panelContenidoCentral;
    }

    public void addCerrarSesionListener(ActionListener listener) {
        btnCerrarSesion.addActionListener(listener);
    }
}
