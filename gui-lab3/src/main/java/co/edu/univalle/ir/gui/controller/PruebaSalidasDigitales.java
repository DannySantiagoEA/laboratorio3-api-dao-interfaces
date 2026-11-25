package co.edu.univalle.ir.gui.controller;

import co.edu.univalle.ir.conexion.ConexionDB;
import co.edu.univalle.ir.dao.int_proceso_refs_dataDAO;
import co.edu.univalle.ir.dao.int_proceso_refs_dataDAOImp;
import co.edu.univalle.ir.entity.int_proceso_refs_data;
import co.edu.univalle.ir.gui.view.PanelSalidasDigitales;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;

/**
 * Prueba autoejecutable de la Tarea #38 (HU-08): comprueba que los 4 botones de
 * salidas digitales de la GUI guardan cada conmutacion en
 * <code>int_proceso_refs_data</code> a traves del DAO.
 * <p>
 * No abre ventanas: crea el {@link PanelSalidasDigitales} sin mostrarlo y
 * "pulsa" los botones con <code>doClick()</code>, asi que corre sin pantalla.
 * Usa <code>db_config.txt</code> del directorio de trabajo (el de gui-lab3
 * cuando se ejecuta desde NetBeans). Al terminar borra solo los registros que
 * ella misma inserto.
 * </p>
 * <p>
 * Resultado esperado: todas las lineas <code>[PASA]</code> y "0 fallan". Las
 * lineas <code>[OBS]</code> son solo informativas.
 * </p>
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/23
 */
public class PruebaSalidasDigitales {

    private static final int ID_REF_BASE = ControladorSalidasDigitales.ID_REF_BASE_DEFECTO;
    private static final float TOLERANCIA = 0.001f;

    private static int pasan = 0;
    private static int fallan = 0;

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        System.out.println("=== PRUEBA TAREA #38: botones de salidas digitales -> int_proceso_refs_data ===");

        ConexionDB conexion = new ConexionDB(new File("db_config.txt"));
        if (!conexion.isConectado()) {
            System.out.println("[FALLA] No se pudo conectar a MySQL con db_config.txt");
            System.exit(1);
        }
        int_proceso_refs_dataDAO dao = new int_proceso_refs_dataDAOImp(conexion);
        int idMaximoAntes = idMaximo(dao);
        int totalAntes = dao.seleccionarTodos().size();
        System.out.println("[INFO] Registros en int_proceso_refs_data antes de la prueba: " + totalAntes);

        try {
            probarPanel();
            probarSincronizacion(conexion, dao);
            probarConmutacionesUnaAUna(conexion, dao);
            probarConmutacionesRapidas(conexion, dao);
            probarFalloDeBaseDeDatos(conexion);
        } finally {
            limpiar(dao, idMaximoAntes);
            int totalDespues = dao.seleccionarTodos().size();
            verificar("[6] limpieza: la tabla queda con los mismos registros que al empezar",
                    totalDespues == totalAntes);
            conexion.cerrarConexion();
        }

        System.out.println();
        System.out.println("RESULTADO: " + pasan + " pasan, " + fallan + " fallan.");
        System.exit(fallan == 0 ? 0 : 1);
    }

    // ---------------------------------------------------------------- [1]
    private static void probarPanel() {
        System.out.println("\n[1] El panel tiene 4 botones, todos en OFF al crearse");
        PanelSalidasDigitales panel = new PanelSalidasDigitales();
        boolean todosOff = true;
        for (int i = 1; i <= PanelSalidasDigitales.NUM_SALIDAS; i++) {
            todosOff &= !panel.getEstadoSalida(i);
        }
        verificar("[1] hay " + PanelSalidasDigitales.NUM_SALIDAS + " salidas", PanelSalidasDigitales.NUM_SALIDAS == 4);
        verificar("[1] los 4 botones arrancan en OFF", todosOff);
        verificar("[1] el texto del boton D1 es 'D1  OFF'", "D1  OFF".equals(panel.getBoton(1).getText()));
    }

    // ---------------------------------------------------------------- [2]
    private static void probarSincronizacion(ConexionDB conexion, int_proceso_refs_dataDAO dao) throws Exception {
        System.out.println("\n[2] Al abrir, los botones muestran el ultimo estado guardado en la DB");
        // Deja guardado D1=OFF, D2=ON, D3=OFF, D4=ON
        int filas = dao.insertarEstados4SalidasDigitales(new boolean[]{false, true, false, true}, 0.0f, ID_REF_BASE);
        verificar("[2] se prepararon 4 registros de estado en la DB", filas == 4);

        PanelSalidasDigitales panel = new PanelSalidasDigitales();
        ControladorSalidasDigitales ctrl = new ControladorSalidasDigitales(panel, dao, ID_REF_BASE);
        ctrl.sincronizarDesdeBaseDeDatos();
        ctrl.esperarPendientes();
        verificar("[2] D1 se muestra en OFF", !panel.getEstadoSalida(1));
        verificar("[2] D2 se muestra en ON", panel.getEstadoSalida(2));
        verificar("[2] D3 se muestra en OFF", !panel.getEstadoSalida(3));
        verificar("[2] D4 se muestra en ON", panel.getEstadoSalida(4));
        verificar("[2] el texto del boton D2 es 'D2  ON'", "D2  ON".equals(panel.getBoton(2).getText()));
        verificar("[2] los botones quedan habilitados", panel.getBoton(1).isEnabled());
        ctrl.detener();
    }

    // ---------------------------------------------------------------- [3]
    private static void probarConmutacionesUnaAUna(ConexionDB conexion, int_proceso_refs_dataDAO dao) throws Exception {
        System.out.println("\n[3] Cada pulsacion guarda exactamente un registro (ON y luego OFF en cada salida)");
        PanelSalidasDigitales panel = new PanelSalidasDigitales();
        ControladorSalidasDigitales ctrl = new ControladorSalidasDigitales(panel, dao, ID_REF_BASE);
        // Los 4 botones parten en OFF (no se sincronizo con la DB)

        for (int salida = 1; salida <= PanelSalidasDigitales.NUM_SALIDAS; salida++) {
            int idRef = ID_REF_BASE + (salida - 1);
            int antes = contar(dao, idRef);

            clic(panel, salida);
            ctrl.esperarPendientes();
            int_proceso_refs_data ultimo = dao.seleccionarUltimoDatoPorReferencia(idRef);
            verificar("[3] D" + salida + " ON: se guardo 1 registro nuevo", contar(dao, idRef) == antes + 1);
            verificar("[3] D" + salida + " ON: el ultimo valor es 1.0 (ON)",
                    ultimo != null && Math.abs(ultimo.getValor() - 1.0f) < TOLERANCIA && ultimo.isEstadoActivo());

            clic(panel, salida);
            ctrl.esperarPendientes();
            ultimo = dao.seleccionarUltimoDatoPorReferencia(idRef);
            verificar("[3] D" + salida + " OFF: se guardo otro registro (2 en total)", contar(dao, idRef) == antes + 2);
            verificar("[3] D" + salida + " OFF: el ultimo valor es 0.0 (OFF)",
                    ultimo != null && Math.abs(ultimo.getValor()) < TOLERANCIA && !ultimo.isEstadoActivo());
        }

        // Deja D1 y D4 en ON y comprueba que el DAO lee lo mismo que muestran los botones
        clic(panel, 1);
        clic(panel, 4);
        ctrl.esperarPendientes();
        boolean[] enDb = dao.consultarUltimosEstados4Salidas(ID_REF_BASE);
        verificar("[3] estado final en la DB = ON,OFF,OFF,ON (igual que los botones)",
                enDb[0] && !enDb[1] && !enDb[2] && enDb[3]
                && panel.getEstadoSalida(1) && !panel.getEstadoSalida(2)
                && !panel.getEstadoSalida(3) && panel.getEstadoSalida(4));
        ctrl.detener();
    }

    // ---------------------------------------------------------------- [4]
    private static void probarConmutacionesRapidas(ConexionDB conexion, int_proceso_refs_dataDAO dao) throws Exception {
        System.out.println("\n[4] 50 pulsaciones seguidas en D3 sin esperar: se guardan todas y en orden");
        PanelSalidasDigitales panel = new PanelSalidasDigitales();
        ControladorSalidasDigitales ctrl = new ControladorSalidasDigitales(panel, dao, ID_REF_BASE);
        int idRef = ID_REF_BASE + 2;
        int antes = contar(dao, idRef);
        int idMaximoPrevio = idMaximo(dao);

        for (int i = 0; i < 50; i++) {
            clic(panel, 3);
        }
        ctrl.esperarPendientes();

        verificar("[4] se guardaron 50 registros nuevos (uno por pulsacion)", contar(dao, idRef) == antes + 50);
        verificar("[4] 50 pulsaciones dejan D3 en OFF (par)", !panel.getEstadoSalida(3)
                && !dao.consultarUltimosEstados4Salidas(ID_REF_BASE)[2]);

        List<int_proceso_refs_data> nuevos = new ArrayList<>();
        for (int_proceso_refs_data r : new ArrayList<>(dao.seleccionarPorReferencia(idRef))) {
            if (r.getId() > idMaximoPrevio) {
                nuevos.add(r);
            }
        }
        nuevos.sort((a, b) -> Integer.compare(a.getId(), b.getId()));
        boolean alterna = nuevos.size() == 50;
        boolean tiempoNoDecrece = nuevos.size() == 50;
        for (int i = 0; i < nuevos.size(); i++) {
            boolean esperadoOn = (i % 2 == 0); // 1a pulsacion = ON, 2a = OFF...
            alterna &= nuevos.get(i).isEstadoActivo() == esperadoOn;
            if (i > 0) {
                tiempoNoDecrece &= nuevos.get(i).getTiempo() >= nuevos.get(i - 1).getTiempo() - TOLERANCIA;
            }
        }
        verificar("[4] los registros alternan ON,OFF,ON,OFF... en el orden de las pulsaciones", alterna);
        verificar("[4] el tiempo (segundos) nunca decrece entre un registro y el siguiente", tiempoNoDecrece);
        if (!nuevos.isEmpty()) {
            observar("tiempo del primer y del ultimo registro: " + nuevos.get(0).getTiempo()
                    + " s y " + nuevos.get(nuevos.size() - 1).getTiempo() + " s");
        }
        ctrl.detener();
    }

    // ---------------------------------------------------------------- [5]
    private static void probarFalloDeBaseDeDatos(ConexionDB conexionPrincipal) throws Exception {
        System.out.println("\n[5] Si la DB falla, el boton vuelve a su estado anterior y no se rompe la GUI");
        System.out.println("    (se va a imprimir un error SQL esperado: la conexion se cierra a proposito)");
        ConexionDB conexionAparte = new ConexionDB(new File("db_config.txt"));
        int_proceso_refs_dataDAO daoAparte = new int_proceso_refs_dataDAOImp(conexionAparte);

        PanelSalidasDigitales panel = new PanelSalidasDigitales();
        ControladorSalidasDigitales ctrl = new ControladorSalidasDigitales(panel, daoAparte, ID_REF_BASE);

        conexionAparte.cerrarConexion();
        clic(panel, 1);
        ctrl.esperarPendientes();
        verificar("[5] D1 vuelve a OFF porque no se pudo guardar", !panel.getEstadoSalida(1));
        verificar("[5] el panel sigue habilitado (la GUI no se rompio)", panel.getBoton(1).isEnabled());
        ctrl.detener();

        clic(panel, 2);
        verificar("[5] despues de detener(), una pulsacion nueva tampoco rompe la GUI y se deshace",
                esperarYComprobarOff(panel, 2));
    }

    private static boolean esperarYComprobarOff(PanelSalidasDigitales panel, int salida) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
        });
        return !panel.getEstadoSalida(salida);
    }

    // ---------------------------------------------------------------- utilidades
    private static void clic(PanelSalidasDigitales panel, int salida) throws Exception {
        SwingUtilities.invokeAndWait(() -> panel.getBoton(salida).doClick());
    }

    private static int contar(int_proceso_refs_dataDAO dao, int idRef) {
        return new ArrayList<>(dao.seleccionarPorReferencia(idRef)).size();
    }

    private static int idMaximo(int_proceso_refs_dataDAO dao) {
        int maximo = 0;
        for (int_proceso_refs_data r : new ArrayList<>(dao.seleccionarTodos())) {
            maximo = Math.max(maximo, r.getId());
        }
        return maximo;
    }

    /**
     * Borra solo los registros que inserto esta prueba (los de id mayor al que
     * habia al empezar). Se copia la lista antes de borrar porque los select del
     * DAO devuelven su lista interna y borrar mientras se recorre da
     * ConcurrentModificationException.
     */
    private static void limpiar(int_proceso_refs_dataDAO dao, int idMaximoAntes) {
        for (int_proceso_refs_data r : new ArrayList<>(dao.seleccionarTodos())) {
            if (r.getId() > idMaximoAntes) {
                dao.borrarPorId(r.getId());
            }
        }
    }

    private static void verificar(String descripcion, boolean condicion) {
        if (condicion) {
            pasan++;
            System.out.println("  [PASA]  " + descripcion);
        } else {
            fallan++;
            System.out.println("  [FALLA] " + descripcion);
        }
    }

    private static void observar(String texto) {
        System.out.println("  [OBS]   " + texto);
    }
}

