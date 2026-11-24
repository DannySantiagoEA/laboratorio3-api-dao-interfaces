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

/**
 * Ventana principal protegida de la GUI del Laboratorio Virtual (HU-07 y
 * HU-08).
 * <p>
 * Aloja el {@link PanelMonitoreoEntradas} (Tarea #37) para la graficacion en
 * tiempo real de las 8 entradas analogicas y 4 digitales desde MySQL via DAO.
 * </p>
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.1 $ 2026/11/23
 */
public class VentanaPrincipal extends JFrame {

    private final SesionUsuario sesion;
    private JButton btnCerrarSesion;
    private JPanel panelContenidoCentral;
    private PanelMonitoreoEntradas panelMonitoreoEntradas;
    private Runnable onCerrarVentana;

    public VentanaPrincipal(SesionUsuario sesion) {
        if (sesion == null || sesion.getUsuario() == null) {
            throw new IllegalStateException("Acceso denegado: se requiere una sesion autenticada.");
        }
        this.sesion = sesion;
        inicializarComponentes();
        configurarCierreSeguro();
    }

    private void inicializarComponentes() {
        setTitle("Laboratorio Virtual Univalle - Monitor DAO en Tiempo Real (HU-07 / HU-08)");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1120, 680));
        getContentPane().setLayout(new BorderLayout());

        // 1. Barra superior con datos del usuario autenticado y boton Cerrar Sesion
        JPanel barraSuperior = new JPanel(new BorderLayout());
        barraSuperior.setBackground(new Color(24, 43, 73));
        barraSuperior.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));

        JLabel lblSistema = new JLabel("LABORATORIO VIRTUAL | Supervision 8 ADC + 4 DIG via API DAO");
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

        // 2. Panel central con el monitor de las 8 entradas analogicas y 4 digitales (Tarea #37)
        panelContenidoCentral = new JPanel(new BorderLayout());
        panelContenidoCentral.setBackground(new Color(243, 246, 250));

        panelMonitoreoEntradas = new PanelMonitoreoEntradas();
        panelContenidoCentral.add(panelMonitoreoEntradas, BorderLayout.CENTER);

        // 3. Barra inferior de sesion y proceso activo
        JPanel barraInferior = new JPanel(new BorderLayout());
        barraInferior.setBackground(new Color(235, 240, 246));
        barraInferior.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(205, 214, 224)),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)
        ));

        int_usuarios_procesos horario = sesion.getHorarioAsignado();
        String textoHorario = (horario != null)
                ? String.format("Proceso #%d | Horario: %s a %s (%s - %s)",
                        sesion.getIdProcesoActivo(),
                        horario.getFecha_inicio(), horario.getFecha_fin(),
                        horario.getHora_inicio(), horario.getHora_fin())
                : "Proceso #" + sesion.getIdProcesoActivo()
                + " | Base de datos: " + sesion.getConexionDB().getBaseDatos();

        JLabel lblPieIzq = new JLabel(textoHorario);
        lblPieIzq.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblPieIzq.setForeground(new Color(60, 72, 88));

        JLabel lblPieDer = new JLabel("Universidad del Valle - EIEE | Interfaces 710044M");
        lblPieDer.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblPieDer.setForeground(new Color(100, 112, 128));

        barraInferior.add(lblPieIzq, BorderLayout.WEST);
        barraInferior.add(lblPieDer, BorderLayout.EAST);

        getContentPane().add(barraSuperior, BorderLayout.NORTH);
        getContentPane().add(panelContenidoCentral, BorderLayout.CENTER);
        getContentPane().add(barraInferior, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    private void configurarCierreSeguro() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (onCerrarVentana != null) {
                    onCerrarVentana.run();
                }
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

    public PanelMonitoreoEntradas getPanelMonitoreoEntradas() {
        return panelMonitoreoEntradas;
    }

    public void setOnCerrarVentana(Runnable onCerrarVentana) {
        this.onCerrarVentana = onCerrarVentana;
    }

    public void addCerrarSesionListener(ActionListener listener) {
        btnCerrarSesion.addActionListener(listener);
    }
}
