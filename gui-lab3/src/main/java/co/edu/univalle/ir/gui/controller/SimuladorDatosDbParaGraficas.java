package co.edu.univalle.ir.gui.controller;

import co.edu.univalle.ir.conexion.ConexionDB;
import co.edu.univalle.ir.dao.int_proceso_vars_dataDAO;
import co.edu.univalle.ir.dao.int_proceso_vars_dataDAOImp;
import java.io.File;

/**
 * Utilidad de verificacion funcional para la Tarea #37 (HU-08): Inyecta
 * periodicamente 8 senales analogicas diferenciadas y 4 senales digitales en
 * <code>int_proceso_vars_data</code> via DAO para validar en vivo la lectura y
 * graficacion periodica desde la GUI.
 *
 * @author Danny Santiago Erazo Agredo
 * @version $Revision: 1.0 $ 2026/11/23
 */
public class SimuladorDatosDbParaGraficas {

    public static void main(String[] args) {
        System.out.println("=========================================================================");
        System.out.println("  SIMULADOR DE PRUEBA TAREA #37: INYECCION 8 ADC + 4 DIG EN MYSQL        ");
        System.out.println("=========================================================================");

        ConexionDB conexion = null;
        try {
            conexion = new ConexionDB(new File(ControladorLogin.RUTA_CONFIG_DB));
            int_proceso_vars_dataDAO varsDAO = new int_proceso_vars_dataDAOImp(conexion);

            // Limpiar muestras previas para observar las curvas limpias desde t = 0.0 s
            varsDAO.borrarTodos();
            System.out.println("[1] Tabla int_proceso_vars_data limpiada. Iniciando flujo de 120 ciclos (30 s)...");
            System.out.println("    -> Abra AppGuiLabVirtual, inicie sesion y cambie entre CH1..CH8 y D1..D4.");

            for (int ciclo = 0; ciclo < 120; ciclo++) {
                float t = ciclo * 0.25f;

                float[] adc8 = new float[]{
                    (float) (2.5 + 2.2 * Math.sin(2.0 * Math.PI * 0.15 * t)), // CH1: Seno
                    (float) (2.5 + 2.0 * Math.cos(2.0 * Math.PI * 0.10 * t)), // CH2: Coseno
                    (t * 0.5f) % 5.0f, // CH3: Rampa 0..5V
                    Math.abs(((t * 1.0f) % 10.0f) - 5.0f), // CH4: Triangular
                    1.25f + (ciclo % 2 == 0 ? 0.25f : -0.25f), // CH5: Pulso ~1.25V
                    3.75f + (float) (0.5 * Math.sin(t)), // CH6: Oscilacion alta
                    (float) (2.5 + 2.4 * Math.sin(2.0 * Math.PI * 0.30 * t)), // CH7: Seno rapido
                    0.5f + ((ciclo % 10) * 0.45f) // CH8: Escalera
                };

                int[] dig4 = new int[]{
                    (ciclo / 2) % 2, // D1: conmuta cada 0.5 s
                    (ciclo / 4) % 2, // D2: conmuta cada 1.0 s
                    (ciclo / 8) % 2, // D3: conmuta cada 2.0 s
                    (ciclo % 6 == 0) ? 1 : 0 // D4: pulso breve cada 1.5 s
                };

                int insertadas = varsDAO.insertarMuestra12Canales(adc8, dig4, t, 1);
                if (ciclo % 10 == 0) {
                    System.out.printf("[Ciclo %03d | t=%5.2fs] Insertadas %d/12 | CH1=%.2fV CH3=%.2fV | D1=%d D2=%d%n",
                            ciclo + 1, t, insertadas, adc8[0], adc8[2], dig4[0], dig4[1]);
                }
                Thread.sleep(250);
            }

            System.out.println("\n[OK] Simulador finalizado (1440 muestras insertadas en int_proceso_vars_data).");

        } catch (Exception e) {
            System.err.println("[ERROR SIMULADOR #37] " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conexion != null) {
                try {
                    conexion.cerrarConexion();
                } catch (Exception ignored) {
                }
            }
        }
    }
}
