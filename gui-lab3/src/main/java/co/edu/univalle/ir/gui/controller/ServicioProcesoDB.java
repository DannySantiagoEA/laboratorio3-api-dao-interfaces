package co.edu.univalle.ir.gui.controller;

import co.edu.univalle.ir.conexion.ConexionDB;
import co.edu.univalle.ir.dao.int_procesoDAO;
import co.edu.univalle.ir.dao.int_procesoDAOImp;
import java.io.File;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.swing.SwingUtilities;

/**
 * Servicio asincrono de la Tarea #34 (HU-07 - Entregable 3.f): Consulta y
 * actualiza <code>tiempo_muestreo</code> en la tabla <code>int_proceso</code>
 * via {@link int_procesoDAO} en un hilo dedicado, sin bloquear el Event
 * Dispatch Thread (EDT) de Swing.
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/24
 */
public class ServicioProcesoDB implements AutoCloseable {

    public static final int TS_MINIMO_MS = 10;
    public static final int TS_MAXIMO_MS = 5000;
    public static final int TS_NO_DISPONIBLE = -1;

    private final ConexionDB conexion;
    private final int_procesoDAO procesoDAO;
    private final int idProceso;
    private final ExecutorService ejecutor;

    public ServicioProcesoDB(String rutaConfigDb, int idProceso) throws Exception {
        this.conexion = new ConexionDB(new File(rutaConfigDb));
        this.procesoDAO = new int_procesoDAOImp(conexion);
        this.idProceso = idProceso;
        this.ejecutor = Executors.newSingleThreadExecutor(tarea -> {
            Thread hilo = new Thread(tarea, "Hilo-GUI-Servicio-Proceso");
            hilo.setDaemon(true);
            return hilo;
        });
    }

    /**
     * Valida que el texto ingresado corresponda a un periodo entero dentro del
     * rango seguro del firmware (10 ms a 5000 ms).
     *
     * @param texto Entrada del usuario en la GUI.
     * @return Entero validado en milisegundos.
     * @throws IllegalArgumentException Si el formato o rango no son validos.
     */
    public static int validarPeriodoMs(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("Ingrese un tiempo de muestreo en milisegundos (10 a 5000 ms).");
        }
        int valor;
        try {
            valor = Integer.parseInt(texto.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("El tiempo de muestreo debe ser un numero entero (ej. 100, 250).");
        }
        if (valor < TS_MINIMO_MS || valor > TS_MAXIMO_MS) {
            throw new IllegalArgumentException(
                    "El tiempo de muestreo debe estar entre " + TS_MINIMO_MS + " ms y " + TS_MAXIMO_MS + " ms.");
        }
        return valor;
    }

    /**
     * Consulta en segundo plano el tiempo_muestreo vigente en int_proceso y
     * notifica el resultado en el EDT.
     *
     * @param alTerminar Callback invocado en el EDT con Ts en ms (o
     * TS_NO_DISPONIBLE).
     */
    public void consultarTiempoMuestreo(Consumer<Integer> alTerminar) {
        ejecutor.submit(() -> {
            int ts = TS_NO_DISPONIBLE;
            try {
                float valorDb = procesoDAO.consultarTiempoMuestreo(idProceso);
                if (valorDb > 0.0f) {
                    ts = Math.round(valorDb);
                }
            } catch (Exception e) {
                System.err.println("[ERROR #34] Consultar tiempo_muestreo: " + e.getMessage());
            }
            final int resultado = ts;
            SwingUtilities.invokeLater(() -> alTerminar.accept(resultado));
        });
    }

    /**
     * Actualiza en segundo plano el campo tiempo_muestreo en int_proceso via
     * DAO y notifica el resultado booleano en el EDT.
     *
     * @param nuevoTsMs Nuevo periodo en milisegundos (10..5000).
     * @param alTerminar Callback invocado en el EDT con true si se actualizo 1
     * fila.
     */
    public void actualizarTiempoMuestreo(int nuevoTsMs, Consumer<Boolean> alTerminar) {
        ejecutor.submit(() -> {
            boolean exito = false;
            try {
                exito = procesoDAO.actualizarTiempoMuestreo(idProceso, (float) nuevoTsMs) > 0;
            } catch (Exception e) {
                System.err.println("[ERROR #34] Actualizar tiempo_muestreo: " + e.getMessage());
            }
            final boolean resultado = exito;
            SwingUtilities.invokeLater(() -> alTerminar.accept(resultado));
        });
    }

    @Override
    public void close() {
        ejecutor.shutdown();
        try {
            ejecutor.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        try {
            conexion.cerrarConexion();
        } catch (SQLException ignored) {
        }
    }
}
