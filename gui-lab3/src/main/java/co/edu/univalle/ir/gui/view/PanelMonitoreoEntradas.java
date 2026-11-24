package co.edu.univalle.ir.gui.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

/**
 * Panel principal de visualizacion de entradas para la GUI del Laboratorio
 * Virtual (HU-08 - Tarea #37).
 * <p>
 * Presenta en tiempo real los valores actuales de las 8 entradas analogicas
 * (CH1..CH8) y las 4 entradas digitales (D1..D4), junto con dos lienzos
 * {@link GraficaTiempo} y sus selectores de canal conectados al controlador
 * DAO.
 * </p>
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/23
 */
public class PanelMonitoreoEntradas extends JPanel {

    public static final int NUM_ANALOGICAS = 8;
    public static final int NUM_DIGITALES = 4;

    private static final Color COLOR_ACTIVO = new Color(0, 148, 50);
    private static final Color COLOR_PAUSADO = new Color(214, 137, 16);
    private static final Color COLOR_OFF = new Color(110, 120, 130);
    private static final Color COLOR_ON_BG = new Color(220, 248, 228);
    private static final Color COLOR_OFF_BG = new Color(238, 241, 245);

    private JLabel lblEstadoLectura;
    private JLabel lblResumenDb;
    private JButton btnPausarReanudar;

    private final JLabel[] lblValoresAnalogicos = new JLabel[NUM_ANALOGICAS];
    private final JLabel[] lblValoresDigitales = new JLabel[NUM_DIGITALES];

    private JComboBox<String> cmbCanalAnalogico;
    private JComboBox<String> cmbCanalDigital;
    private JPanel contenedorGraficaAnalogica;
    private JPanel contenedorGraficaDigital;

    public PanelMonitoreoEntradas() {
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(243, 246, 250));
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        // 1. Barra superior: estado de lectura periodica DAO y boton Pausar/Reanudar
        JPanel barraEstado = new JPanel(new BorderLayout(10, 0));
        barraEstado.setBackground(Color.WHITE);
        barraEstado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 218, 228), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        JPanel panelIzqEstado = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        panelIzqEstado.setOpaque(false);

        lblEstadoLectura = new JLabel("Estado DAO: Inicializando lectura periodica...");
        lblEstadoLectura.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblEstadoLectura.setForeground(new Color(24, 58, 107));

        lblResumenDb = new JLabel("Tabla: int_proceso_vars_data | Sin muestras aun");
        lblResumenDb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblResumenDb.setForeground(new Color(95, 106, 118));

        panelIzqEstado.add(lblEstadoLectura);
        panelIzqEstado.add(new JLabel("|"));
        panelIzqEstado.add(lblResumenDb);

        btnPausarReanudar = new JButton("Pausar Lectura");
        btnPausarReanudar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnPausarReanudar.setBackground(new Color(24, 58, 107));
        btnPausarReanudar.setForeground(Color.WHITE);
        btnPausarReanudar.setFocusPainted(false);
        btnPausarReanudar.setContentAreaFilled(false);
        btnPausarReanudar.setOpaque(true);
        btnPausarReanudar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPausarReanudar.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));

        barraEstado.add(panelIzqEstado, BorderLayout.CENTER);
        barraEstado.add(btnPausarReanudar, BorderLayout.EAST);

        // 2. Panel superior de telemetria en vivo (8 Analogicas + 4 Digitales)
        JPanel panelTelemetria12 = new JPanel(new BorderLayout(10, 0));
        panelTelemetria12.setOpaque(false);

        JPanel panelAdc = new JPanel(new GridLayout(1, NUM_ANALOGICAS, 6, 6));
        panelAdc.setBackground(Color.WHITE);
        panelAdc.setBorder(crearBordeTitulado("8 Entradas Analogicas en Vivo (int_proceso_vars_data IDs 1..8)"));

        for (int i = 0; i < NUM_ANALOGICAS; i++) {
            JPanel tarjeta = new JPanel(new BorderLayout(0, 2));
            tarjeta.setBackground(new Color(252, 245, 245));
            tarjeta.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(230, 195, 195), 1),
                    BorderFactory.createEmptyBorder(4, 6, 4, 6)
            ));

            JLabel lblTitulo = new JLabel("CH" + (i + 1), SwingConstants.CENTER);
            lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblTitulo.setForeground(new Color(145, 30, 30));

            lblValoresAnalogicos[i] = new JLabel("-- V", SwingConstants.CENTER);
            lblValoresAnalogicos[i].setFont(new Font("Consolas", Font.BOLD, 13));
            lblValoresAnalogicos[i].setForeground(new Color(30, 41, 59));

            tarjeta.add(lblTitulo, BorderLayout.NORTH);
            tarjeta.add(lblValoresAnalogicos[i], BorderLayout.CENTER);
            panelAdc.add(tarjeta);
        }

        JPanel panelDig = new JPanel(new GridLayout(1, NUM_DIGITALES, 6, 6));
        panelDig.setBackground(Color.WHITE);
        panelDig.setPreferredSize(new Dimension(280, 74));
        panelDig.setBorder(crearBordeTitulado("4 Entradas Digitales (IDs 9..12)"));

        for (int i = 0; i < NUM_DIGITALES; i++) {
            lblValoresDigitales[i] = new JLabel("D" + (i + 1) + ": --", SwingConstants.CENTER);
            lblValoresDigitales[i].setOpaque(true);
            lblValoresDigitales[i].setBackground(COLOR_OFF_BG);
            lblValoresDigitales[i].setForeground(COLOR_OFF);
            lblValoresDigitales[i].setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblValoresDigitales[i].setBorder(BorderFactory.createLineBorder(new Color(195, 204, 214), 1));
            panelDig.add(lblValoresDigitales[i]);
        }

        panelTelemetria12.add(panelAdc, BorderLayout.CENTER);
        panelTelemetria12.add(panelDig, BorderLayout.EAST);

        JPanel bloqueNorte = new JPanel(new BorderLayout(0, 8));
        bloqueNorte.setOpaque(false);
        bloqueNorte.add(barraEstado, BorderLayout.NORTH);
        bloqueNorte.add(panelTelemetria12, BorderLayout.CENTER);

        // 3. Zona central: dos graficas JFreeChart con sus selectores de canal
        JPanel panelGraficas = new JPanel(new GridLayout(1, 2, 12, 0));
        panelGraficas.setOpaque(false);

        // Panel izquierda: Grafica Analogica
        JPanel cardAnalogica = new JPanel(new BorderLayout(0, 8));
        cardAnalogica.setBackground(Color.WHITE);
        cardAnalogica.setBorder(crearBordeTitulado("Monitor de Senal Analogica (0.0 - 5.0 V)"));

        JPanel barraSelAnalogica = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        barraSelAnalogica.setOpaque(false);
        JLabel lblSelAna = new JLabel("Canal Analogico:");
        lblSelAna.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cmbCanalAnalogico = new JComboBox<>();
        cmbCanalAnalogico.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbCanalAnalogico.setPreferredSize(new Dimension(240, 28));
        barraSelAnalogica.add(lblSelAna);
        barraSelAnalogica.add(cmbCanalAnalogico);

        contenedorGraficaAnalogica = new JPanel(new BorderLayout());
        contenedorGraficaAnalogica.setBackground(Color.WHITE);

        cardAnalogica.add(barraSelAnalogica, BorderLayout.NORTH);
        cardAnalogica.add(contenedorGraficaAnalogica, BorderLayout.CENTER);

        // Panel derecha: Grafica Digital
        JPanel cardDigital = new JPanel(new BorderLayout(0, 8));
        cardDigital.setBackground(Color.WHITE);
        cardDigital.setBorder(crearBordeTitulado("Monitor de Senal Digital (Nivel Logico 0/1)"));

        JPanel barraSelDigital = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        barraSelDigital.setOpaque(false);
        JLabel lblSelDig = new JLabel("Canal Digital:");
        lblSelDig.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cmbCanalDigital = new JComboBox<>();
        cmbCanalDigital.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbCanalDigital.setPreferredSize(new Dimension(240, 28));
        barraSelDigital.add(lblSelDig);
        barraSelDigital.add(cmbCanalDigital);

        contenedorGraficaDigital = new JPanel(new BorderLayout());
        contenedorGraficaDigital.setBackground(Color.WHITE);

        cardDigital.add(barraSelDigital, BorderLayout.NORTH);
        cardDigital.add(contenedorGraficaDigital, BorderLayout.CENTER);

        panelGraficas.add(cardAnalogica);
        panelGraficas.add(cardDigital);

        add(bloqueNorte, BorderLayout.NORTH);
        add(panelGraficas, BorderLayout.CENTER);
    }

    private TitledBorder crearBordeTitulado(String titulo) {
        TitledBorder borde = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(205, 214, 224), 1),
                titulo
        );
        borde.setTitleFont(new Font("Segoe UI", Font.BOLD, 12));
        borde.setTitleColor(new Color(30, 58, 95));
        return borde;
    }

    // =========================================================================
    // API PUBLICA PARA ControladorGraficacionDAO (TAREA #37)
    // =========================================================================
    public void montarGraficaAnalogica(GraficaTiempo grafica) {
        contenedorGraficaAnalogica.removeAll();
        contenedorGraficaAnalogica.add(grafica, BorderLayout.CENTER);
        contenedorGraficaAnalogica.revalidate();
        contenedorGraficaAnalogica.repaint();
    }

    public void montarGraficaDigital(GraficaTiempo grafica) {
        contenedorGraficaDigital.removeAll();
        contenedorGraficaDigital.add(grafica, BorderLayout.CENTER);
        contenedorGraficaDigital.revalidate();
        contenedorGraficaDigital.repaint();
    }

    public void cargarNombresCanales(String[] nombresAnalogicos, String[] nombresDigitales) {
        cmbCanalAnalogico.removeAllItems();
        for (String nombre : nombresAnalogicos) {
            cmbCanalAnalogico.addItem(nombre);
        }
        cmbCanalDigital.removeAllItems();
        for (String nombre : nombresDigitales) {
            cmbCanalDigital.addItem(nombre);
        }
    }

    public int getCanalAnalogicoSeleccionado() {
        return Math.max(0, cmbCanalAnalogico.getSelectedIndex());
    }

    public int getCanalDigitalSeleccionado() {
        return Math.max(0, cmbCanalDigital.getSelectedIndex());
    }

    public void addListenerCanalAnalogico(ActionListener listener) {
        cmbCanalAnalogico.addActionListener(listener);
    }

    public void addListenerCanalDigital(ActionListener listener) {
        cmbCanalDigital.addActionListener(listener);
    }

    public void addListenerPausarReanudar(ActionListener listener) {
        btnPausarReanudar.addActionListener(listener);
    }

    /**
     * Actualiza las 8 tarjetas de voltaje analogico y los 4 indicadores
     * digitales.
     *
     * @param ultimosAnalogicos Arreglo Float[8] (null si el canal no tiene
     * muestras).
     * @param ultimosDigitales Arreglo Integer[4] (0, 1 o null si no tiene
     * muestras).
     */
    public void actualizarLecturas12Canales(Float[] ultimosAnalogicos, Integer[] ultimosDigitales) {
        for (int i = 0; i < NUM_ANALOGICAS; i++) {
            Float v = (ultimosAnalogicos != null && i < ultimosAnalogicos.length) ? ultimosAnalogicos[i] : null;
            lblValoresAnalogicos[i].setText(v != null ? String.format("%.2f V", v) : "-- V");
        }
        for (int i = 0; i < NUM_DIGITALES; i++) {
            Integer d = (ultimosDigitales != null && i < ultimosDigitales.length) ? ultimosDigitales[i] : null;
            if (d == null) {
                lblValoresDigitales[i].setText("D" + (i + 1) + ": --");
                lblValoresDigitales[i].setBackground(COLOR_OFF_BG);
                lblValoresDigitales[i].setForeground(COLOR_OFF);
            } else if (d >= 1) {
                lblValoresDigitales[i].setText("D" + (i + 1) + ": 1");
                lblValoresDigitales[i].setBackground(COLOR_ON_BG);
                lblValoresDigitales[i].setForeground(COLOR_ACTIVO);
            } else {
                lblValoresDigitales[i].setText("D" + (i + 1) + ": 0");
                lblValoresDigitales[i].setBackground(COLOR_OFF_BG);
                lblValoresDigitales[i].setForeground(COLOR_OFF);
            }
        }
    }

    public void actualizarEstadoSondeo(boolean activo, String mensajePrincipal, String detalleDb) {
        lblEstadoLectura.setText(mensajePrincipal);
        lblEstadoLectura.setForeground(activo ? COLOR_ACTIVO : COLOR_PAUSADO);
        lblResumenDb.setText(detalleDb);
        btnPausarReanudar.setText(activo ? "Pausar Lectura" : "Reanudar Lectura");
        btnPausarReanudar.setBackground(activo ? new Color(24, 58, 107) : new Color(0, 148, 50));
    }

    public void mostrarErrorSondeo(String mensajeError) {
        lblEstadoLectura.setText("Error leyendo int_proceso_vars_data");
        lblEstadoLectura.setForeground(new Color(214, 48, 49));
        lblResumenDb.setText(mensajeError);
    }
}
