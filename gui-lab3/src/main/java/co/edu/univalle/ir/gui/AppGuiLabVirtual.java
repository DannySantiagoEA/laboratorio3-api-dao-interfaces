package co.edu.univalle.ir.gui;

import co.edu.univalle.ir.gui.controller.ControladorLogin;
import co.edu.univalle.ir.gui.view.VentanaLogin;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Punto de entrada de la interfaz grafica (GUI) del Laboratorio Virtual (Sprint
 * 2 - HU-07 y HU-08).
 * <p>
 * Inicializa el Look & Feel del sistema y despliega la ventana de control de
 * acceso ({@link VentanaLogin}) en el Event Dispatch Thread (EDT).
 * </p>
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/22
 */
public class AppGuiLabVirtual {

    /**
     * Metodo principal de lanzamiento de la aplicacion GUI.
     *
     * @param args Argumentos de linea de comandos.
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            VentanaLogin vistaLogin = new VentanaLogin();
            new ControladorLogin(vistaLogin);
            vistaLogin.setVisible(true);
        });
    }
}
