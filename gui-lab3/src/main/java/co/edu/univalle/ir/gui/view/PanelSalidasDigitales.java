package co.edu.univalle.ir.gui.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

/**
 * Panel Swing con los 4 botones de las salidas digitales D1..D4 del sistema
 * embebido (HU-08 - Entregable 3.e).
 * <p>
 * Es solo vista: no contiene logica de acceso a datos ni consultas SQL. Cada
 * vez que el usuario conmuta un boton avisa al
 * {@link co.edu.univalle.ir.gui.controller.ControladorSalidasDigitales}, que es
 * quien guarda la conmutacion en <code>int_proceso_refs_data</code>.
 * </p>
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/23
 */
public class PanelSalidasDigitales extends JPanel {

    /**
     * Cantidad de salidas digitales del sistema embebido (D1..D4).
     */
    public static final int NUM_SALIDAS = 4;

    private static final Color COLOR_ON = new Color(0, 148, 50);
    private static final Color COLOR_OFF = new Color(99, 110, 114);

    /**
     * Escuchador que recibe cada conmutacion hecha por el usuario.
     */
    public interface SalidaListener {

        /**
         * Se invoca (en el EDT) cuando el usuario conmuta un boton.
         *
         * @param numeroSalida Numero de la salida (1 a 4).
         * @param encendida true si quedo en ON, false si quedo en OFF.
         */
        void salidaConmutada(int numeroSalida, boolean encendida);
    }

    private final JToggleButton[] botones = new JToggleButton[NUM_SALIDAS];
    private final JLabel lblEstado;

    public PanelSalidasDigitales() {
        setLayout(new BorderLayout(0, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(185, 195, 205), 1),
                        "Salidas digitales D1 a D4 (LEDs del sistema embebido)",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 13),
                        new Color(45, 52, 54)),
                BorderFactory.createEmptyBorder(8, 12, 12, 12)));

        JPanel panelBotones = new JPanel(new GridLayout(1, NUM_SALIDAS, 14, 0));
        panelBotones.setOpaque(false);
        for (int i = 0; i < NUM_SALIDAS; i++) {
            JToggleButton boton = new JToggleButton();
            boton.setFont(new Font("Segoe UI", Font.BOLD, 14));
            boton.setForeground(Color.WHITE);
            boton.setFocusPainted(false);
            boton.setContentAreaFilled(false);
            boton.setOpaque(true);
            boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            boton.setPreferredSize(new Dimension(150, 56));
            botones[i] = boton;
            actualizarAspecto(i);
            panelBotones.add(boton);
        }

        lblEstado = new JLabel("Pulse un boton para encender o apagar la salida.", SwingConstants.CENTER);
        lblEstado.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblEstado.setForeground(new Color(99, 110, 114));

        add(panelBotones, BorderLayout.CENTER);
        add(lblEstado, BorderLayout.SOUTH);
    }

    /**
     * Enlaza los 4 botones con el controlador.
     *
     * @param listener Escuchador de conmutaciones.
     */
    public void addSalidaListener(SalidaListener listener) {
        for (int i = 0; i < NUM_SALIDAS; i++) {
            final int numeroSalida = i + 1;
            final JToggleButton boton = botones[i];
            boton.addActionListener(e -> {
                actualizarAspecto(numeroSalida - 1);
                listener.salidaConmutada(numeroSalida, boton.isSelected());
            });
        }
    }

    /**
     * Fija el estado de un boton sin disparar el escuchador (se usa para
     * sincronizar con la base de datos o para deshacer una conmutacion que no
     * se pudo guardar).
     *
     * @param numeroSalida Numero de la salida (1 a 4).
     * @param encendida true para ON, false para OFF.
     */
    public void setEstadoSalida(int numeroSalida, boolean encendida) {
        botones[numeroSalida - 1].setSelected(encendida);
        actualizarAspecto(numeroSalida - 1);
    }

    /**
     * Estado actual mostrado por un boton.
     *
     * @param numeroSalida Numero de la salida (1 a 4).
     * @return true si el boton esta en ON.
     */
    public boolean getEstadoSalida(int numeroSalida) {
        return botones[numeroSalida - 1].isSelected();
    }

    /**
     * Devuelve el boton de una salida (util para pruebas).
     *
     * @param numeroSalida Numero de la salida (1 a 4).
     * @return El {@link JToggleButton} de esa salida.
     */
    public JToggleButton getBoton(int numeroSalida) {
        return botones[numeroSalida - 1];
    }

    public void setBotonesHabilitados(boolean habilitado) {
        for (JToggleButton boton : botones) {
            boton.setEnabled(habilitado);
        }
    }

    public void mostrarEstado(String mensaje, Color color) {
        lblEstado.setText(mensaje);
        lblEstado.setForeground(color);
    }

    private void actualizarAspecto(int indice) {
        JToggleButton boton = botones[indice];
        boolean encendida = boton.isSelected();
        boton.setText("D" + (indice + 1) + "  " + (encendida ? "ON" : "OFF"));
        boton.setBackground(encendida ? COLOR_ON : COLOR_OFF);
    }
}

