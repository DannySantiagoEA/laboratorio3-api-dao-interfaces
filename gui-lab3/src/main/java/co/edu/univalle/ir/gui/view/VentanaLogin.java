package co.edu.univalle.ir.gui.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * Vista de inicio de sesion en Swing para el Laboratorio Virtual (HU-07 -
 * Entregable 3.a).
 * <p>
 * No contiene logica de acceso a datos ni consultas SQL: delega la validacion
 * al {@link co.edu.univalle.ir.gui.controller.ControladorLogin}.
 * </p>
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/22
 */
public class VentanaLogin extends JFrame {

    private static final Color COLOR_PRIMARIO = new Color(180, 26, 36); // Rojo institucional Univalle
    private static final Color COLOR_BOTON = new Color(24, 58, 107);
    private static final Color COLOR_DESHABILITADO = new Color(170, 180, 190);

    private JTextField txtEmail;
    private JPasswordField txtClave;
    private JCheckBox chkMostrarClave;
    private JButton btnIngresar;
    private JLabel lblEstado;
    private char echoCharOriginal;

    public VentanaLogin() {
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        setTitle("Laboratorio Virtual - Control de Acceso (Univalle)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setLayout(new BorderLayout());

        // 1. Encabezado institucional
        JPanel panelEncabezado = new JPanel(new BorderLayout(0, 4));
        panelEncabezado.setBackground(COLOR_PRIMARIO);
        panelEncabezado.setBorder(BorderFactory.createEmptyBorder(18, 28, 18, 28));

        JLabel lblTitulo = new JLabel("LABORATORIO VIRTUAL - INTERFACES", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(Color.WHITE);

        JLabel lblSubtitulo = new JLabel("Universidad del Valle - EIEE | Autenticacion mediante API DAO", SwingConstants.CENTER);
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitulo.setForeground(new Color(255, 230, 230));

        panelEncabezado.add(lblTitulo, BorderLayout.NORTH);
        panelEncabezado.add(lblSubtitulo, BorderLayout.CENTER);

        // 2. Formulario central
        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBackground(Color.WHITE);
        panelFormulario.setBorder(BorderFactory.createEmptyBorder(24, 34, 22, 34));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        // Correo electronico (campo email en int_usuarios)
        JLabel lblEmail = new JLabel("Correo electronico (email):");
        lblEmail.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblEmail.setForeground(new Color(45, 52, 54));
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 5, 0);
        panelFormulario.add(lblEmail, gbc);

        txtEmail = new JTextField(24);
        txtEmail.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtEmail.setPreferredSize(new Dimension(310, 34));
        txtEmail.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(185, 195, 205), 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 12, 0);
        panelFormulario.add(txtEmail, gbc);

        // Clave (campo clave en int_usuarios)
        JLabel lblClave = new JLabel("Contrasena:");
        lblClave.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblClave.setForeground(new Color(45, 52, 54));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 5, 0);
        panelFormulario.add(lblClave, gbc);

        txtClave = new JPasswordField(24);
        txtClave.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtClave.setPreferredSize(new Dimension(310, 34));
        txtClave.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(185, 195, 205), 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        echoCharOriginal = txtClave.getEchoChar();
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 6, 0);
        panelFormulario.add(txtClave, gbc);

        // Casilla opcional para mostrar/ocultar contrasena
        chkMostrarClave = new JCheckBox("Mostrar contrasena");
        chkMostrarClave.setBackground(Color.WHITE);
        chkMostrarClave.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkMostrarClave.setForeground(new Color(80, 90, 100));
        chkMostrarClave.addActionListener(e -> {
            if (chkMostrarClave.isSelected()) {
                txtClave.setEchoChar((char) 0);
            } else {
                txtClave.setEchoChar(echoCharOriginal);
            }
        });
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 14, 0);
        panelFormulario.add(chkMostrarClave, gbc);

        // Boton de ingreso (estilo solido compatible con Windows 11)
        btnIngresar = new JButton("Ingresar");
        btnIngresar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnIngresar.setBackground(COLOR_BOTON);
        btnIngresar.setForeground(Color.WHITE);
        btnIngresar.setFocusPainted(false);
        btnIngresar.setContentAreaFilled(false);
        btnIngresar.setOpaque(true);
        btnIngresar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnIngresar.setPreferredSize(new Dimension(310, 38));
        gbc.gridy = 5;
        gbc.insets = new Insets(2, 0, 12, 0);
        panelFormulario.add(btnIngresar, gbc);

        // Etiqueta de estado
        lblEstado = new JLabel("Ingrese su correo y contrasena registrados en lab_virtual_db.", SwingConstants.CENTER);
        lblEstado.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblEstado.setForeground(new Color(99, 110, 114));
        gbc.gridy = 6;
        gbc.insets = new Insets(2, 0, 0, 0);
        panelFormulario.add(lblEstado, gbc);

        getRootPane().setDefaultButton(btnIngresar);

        getContentPane().add(panelEncabezado, BorderLayout.NORTH);
        getContentPane().add(panelFormulario, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }

    // =========================================================================
    // API PUBLICA PARA EL CONTROLADOR (TAREA #33)
    // =========================================================================
    public String getEmail() {
        return txtEmail.getText().trim();
    }

    public String getClave() {
        return new String(txtClave.getPassword());
    }

    public void limpiarClave() {
        txtClave.setText("");
        txtClave.requestFocusInWindow();
    }

    public void enfocarEmail() {
        txtEmail.requestFocusInWindow();
    }

    /**
     * Enlaza el boton Ingresar y la tecla Enter en ambos campos con el
     * controlador.
     *
     * @param listener Escuchador de autenticacion.
     */
    public void addIngresoListener(ActionListener listener) {
        btnIngresar.addActionListener(listener);
        txtEmail.addActionListener(listener);
        txtClave.addActionListener(listener);
    }

    /**
     * Habilita o deshabilita el formulario durante la consulta SQL o bloqueo
     * temporal.
     *
     * @param habilitado true para habilitar controles, false para bloquearlos.
     */
    public void setControlesHabilitados(boolean habilitado) {
        txtEmail.setEnabled(habilitado);
        txtClave.setEnabled(habilitado);
        chkMostrarClave.setEnabled(habilitado);
        btnIngresar.setEnabled(habilitado);
        btnIngresar.setBackground(habilitado ? COLOR_BOTON : COLOR_DESHABILITADO);
        setCursor(habilitado ? Cursor.getDefaultCursor() : Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    public void mostrarEstado(String mensaje, Color color) {
        lblEstado.setText(mensaje);
        lblEstado.setForeground(color);
    }
}
