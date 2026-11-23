package co.edu.univalle.ir.dao;

import co.edu.univalle.ir.conexion.ConexionDB;
import co.edu.univalle.ir.entity.int_proceso_refs_data;
import co.edu.univalle.ir.entity.int_proceso_vars_data;
import java.sql.Date;
import java.sql.Time;
import java.util.List;

/**
 * Clase de prueba autoejecutable para la Tarea #20: insercion continua, lectura
 * de la ultima muestra y purga de datos en las tablas historicas
 * <code>int_proceso_vars_data</code> e <code>int_proceso_refs_data</code>.
 * <p>
 * Requiere que la base <code>lab_virtual_db</code> tenga cargado el script de
 * las Tareas #2 y #3 (proceso 1, variables 1 a 12 y salidas 1 a 4).
 * </p>
 *
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/17
 */
public class PruebaHistoricosDAO {

    /**
     * Numero de ciclos de muestreo que se simulan (cada ciclo = 12 muestras).
     */
    private static final int CICLOS = 100;

    /**
     * Id de la primera variable analogica; CH1..CH8 = 1..8 y D1..D4 = 9..12.
     */
    private static final int ID_VAR_BASE = 1;

    /**
     * Id de la primera salida digital; D1..D4 = 1..4.
     */
    private static final int ID_REF_BASE = 1;

    /**
     * Tolerancia al comparar floats leidos de MySQL.
     */
    private static final float TOL = 1e-4f;

    private static int aciertos = 0;
    private static int fallos = 0;

    /**
     * Registra el resultado de una comprobacion y lo imprime.
     *
     * @param descripcion Texto de lo que se comprueba.
     * @param condicion true si la comprobacion se cumple.
     */
    static void verificar(String descripcion, boolean condicion) {
        if (condicion) {
            aciertos++;
            System.out.println("    [PASA]  " + descripcion);
        } else {
            fallos++;
            System.out.println("    [FALLA] " + descripcion);
        }
    }

    /**
     * Prueba la tabla <code>int_proceso_vars_data</code> (8 analogicas y 4
     * digitales de entrada).
     *
     * @param dao DAO de la tabla historica de variables de entrada.
     */
    static void probarVarsData(int_proceso_vars_dataDAO dao) {
        Date hoy = new Date(System.currentTimeMillis());
        Time ahora = new Time(System.currentTimeMillis());

        System.out.println("\n[1] int_proceso_vars_data: valores limite (0.0, 2.5 y 5.0 V)");
        dao.borrarTodos();
        verificar("inserta 0.0 V en CH1", dao.insertarDatoVariable(1, 0.0f, 0.0f, hoy, ahora) == 1);
        verificar("inserta 2.5 V en CH2", dao.insertarDatoVariable(2, 2.5f, 0.0f, hoy, ahora) == 1);
        verificar("inserta 5.0 V en CH3", dao.insertarDatoVariable(3, 5.0f, 0.0f, hoy, ahora) == 1);
        int_proceso_vars_data v3 = dao.seleccionarUltimoDatoPorVariable(3);
        verificar("CH3 se lee como 5.0 V (si da 0.99999 la tabla quedo con FLOAT(5,5))",
                v3 != null && Math.abs(v3.getValor() - 5.0f) < TOL);
        dao.borrarTodos();

        System.out.println("\n[2] int_proceso_vars_data: insercion continua de " + CICLOS + " ciclos");
        int ciclosCon12 = 0;
        int ultimasCorrectas = 0;
        float[] volt = new float[8];
        int[] dig = new int[4];
        long inicio = System.nanoTime();
        for (int c = 0; c < CICLOS; c++) {
            for (int i = 0; i < 8; i++) {
                volt[i] = (c * 0.05f + i * 0.5f) % 5.0f;
            }
            for (int j = 0; j < 4; j++) {
                dig[j] = (c + j) % 2;
            }
            float tiempo = c * 0.1f;
            if (dao.insertarMuestra12Canales(volt, dig, tiempo, ID_VAR_BASE) == 12) {
                ciclosCon12++;
            }
            // ultima muestra de CH1 y de D1 justo despues de insertar
            int_proceso_vars_data ch1 = dao.seleccionarUltimoDatoPorVariable(1);
            int_proceso_vars_data d1 = dao.seleccionarUltimoDatoPorVariable(9);
            if (ch1 != null && d1 != null
                    && Math.abs(ch1.getValor() - volt[0]) < TOL
                    && Math.abs(d1.getValor() - dig[0]) < TOL) {
                ultimasCorrectas++;
            }
        }
        double ms = (System.nanoTime() - inicio) / 1e6;
        verificar("cada ciclo inserto 12 muestras (" + ciclosCon12 + "/" + CICLOS + ")", ciclosCon12 == CICLOS);
        verificar("la ultima muestra de CH1 y D1 coincide tras cada ciclo (" + ultimasCorrectas + "/" + CICLOS + ")",
                ultimasCorrectas == CICLOS);
        verificar("total en la tabla = " + (CICLOS * 12), dao.seleccionarTodos().size() == CICLOS * 12);
        System.out.printf("    Tiempo total: %.1f ms (%.2f ms por ciclo de 12 muestras)%n", ms, ms / CICLOS);

        System.out.println("\n[3] int_proceso_vars_data: lectura de la ultima muestra");
        List<int_proceso_vars_data> ultimas = dao.seleccionarUltimosPorVariable(1, 10);
        verificar("seleccionarUltimosPorVariable(CH1, 10) devuelve 10 muestras", ultimas.size() == 10);
        verificar("vienen en orden cronologico (la ultima tiene el mayor tiempo)",
                ultimas.size() == 10 && Math.abs(ultimas.get(9).getTiempo() - (CICLOS - 1) * 0.1f) < TOL);
        verificar("CH1 conserva sus " + CICLOS + " muestras",
                dao.seleccionarPorVariable(1).size() == CICLOS);

        System.out.println("\n[4] int_proceso_vars_data: purga");
        int_proceso_vars_data una = dao.seleccionarUltimoDatoPorVariable(9);
        verificar("borrarPorId elimina 1 fila", una != null && dao.borrarPorId(una.getId()) == 1);
        verificar("borrarPorVariable(CH1) elimina " + CICLOS + " filas", dao.borrarPorVariable(1) == CICLOS);
        verificar("CH1 queda sin muestras", dao.seleccionarPorVariable(1).isEmpty());
        int restantes = CICLOS * 12 - 1 - CICLOS;
        verificar("borrarTodos elimina las " + restantes + " restantes", dao.borrarTodos() == restantes);
        verificar("la tabla queda vacia", dao.seleccionarTodos().isEmpty());
    }

    /**
     * Prueba la tabla <code>int_proceso_refs_data</code> (4 salidas digitales
     * D1..D4, ids 1 a 4 en <code>int_proceso_refs</code>). Mismo esquema que
     * {@link #probarVarsData}: estados limite, insercion continua, lectura del
     * ultimo estado y purga.
     *
     * @param dao DAO de la tabla historica de salidas digitales.
     */
    static void probarRefsData(int_proceso_refs_dataDAO dao) {
        System.out.println("\n[5] int_proceso_refs_data: estados ON/OFF de una salida");
        dao.borrarTodos();
        verificar("registra D1 = ON", dao.registrarEstadoSalidaDigital(1, true, 0.0f, ID_REF_BASE) == 1);
        verificar("registra D4 = OFF", dao.registrarEstadoSalidaDigital(4, false, 0.0f, ID_REF_BASE) == 1);
        int_proceso_refs_data d1 = dao.seleccionarUltimoDatoPorReferencia(ID_REF_BASE);
        int_proceso_refs_data d4 = dao.seleccionarUltimoDatoPorReferencia(ID_REF_BASE + 3);
        verificar("D1 se lee como ON (1.0)",
                d1 != null && d1.isEstadoActivo() && Math.abs(d1.getValor() - 1.0f) < TOL);
        verificar("D4 se lee como OFF (0.0)",
                d4 != null && !d4.isEstadoActivo() && Math.abs(d4.getValor()) < TOL);
        boolean[] vigentes = dao.consultarUltimosEstados4Salidas(ID_REF_BASE);
        verificar("consultarUltimosEstados4Salidas = [ON, OFF, OFF, OFF] (D2 y D3 sin registros se leen OFF)",
                vigentes.length == 4 && vigentes[0] && !vigentes[1] && !vigentes[2] && !vigentes[3]);
        dao.borrarTodos();

        System.out.println("\n[6] int_proceso_refs_data: insercion continua de " + CICLOS + " ciclos");
        int ciclosCon4 = 0;
        int vigentesCorrectos = 0;
        boolean[] estados = new boolean[4];
        long inicio = System.nanoTime();
        for (int c = 0; c < CICLOS; c++) {
            for (int j = 0; j < 4; j++) {
                estados[j] = ((c + j) % 2 == 0);
            }
            float tiempo = c * 0.1f;
            if (dao.insertarEstados4SalidasDigitales(estados, tiempo, ID_REF_BASE) == 4) {
                ciclosCon4++;
            }
            // estados vigentes justo despues de insertar = los que se guardaron
            boolean[] leidos = dao.consultarUltimosEstados4Salidas(ID_REF_BASE);
            if (java.util.Arrays.equals(leidos, estados)) {
                vigentesCorrectos++;
            }
        }
        double ms = (System.nanoTime() - inicio) / 1e6;
        verificar("cada ciclo inserto 4 estados (" + ciclosCon4 + "/" + CICLOS + ")", ciclosCon4 == CICLOS);
        verificar("los estados vigentes coinciden con lo guardado tras cada ciclo (" + vigentesCorrectos + "/" + CICLOS + ")",
                vigentesCorrectos == CICLOS);
        verificar("total en la tabla = " + (CICLOS * 4), dao.seleccionarTodos().size() == CICLOS * 4);
        System.out.printf("    Tiempo total: %.1f ms (%.2f ms por ciclo de 4 salidas)%n", ms, ms / CICLOS);

        System.out.println("\n[7] int_proceso_refs_data: lectura del historial de una salida");
        List<int_proceso_refs_data> historial = dao.seleccionarPorReferencia(ID_REF_BASE);
        int n = historial.size();
        float primerTiempo = (n > 0) ? historial.get(0).getTiempo() : -1f;
        float ultimoTiempo = (n > 0) ? historial.get(n - 1).getTiempo() : -1f;
        int idUltimo = (n > 0) ? historial.get(n - 1).getId() : -1;
        verificar("seleccionarPorReferencia(D1) devuelve " + CICLOS + " registros", n == CICLOS);
        verificar("vienen en orden cronologico (de tiempo 0.0 al mayor)",
                n == CICLOS && Math.abs(primerTiempo) < TOL
                && Math.abs(ultimoTiempo - (CICLOS - 1) * 0.1f) < TOL);
        int_proceso_refs_data porId = dao.seleccionarPorId(idUltimo);
        verificar("seleccionarPorId devuelve el ultimo registro de D1",
                porId != null && porId.getInt_proceso_refs_id() == ID_REF_BASE
                && Math.abs(porId.getTiempo() - ultimoTiempo) < TOL);

        System.out.println("\n[8] int_proceso_refs_data: purga");
        int_proceso_refs_data deD4 = dao.seleccionarUltimoDatoPorReferencia(ID_REF_BASE + 3);
        verificar("borrarPorId elimina 1 fila", deD4 != null && dao.borrarPorId(deD4.getId()) == 1);
        verificar("borrarPorReferencia(D1) elimina " + CICLOS + " filas",
                dao.borrarPorReferencia(ID_REF_BASE) == CICLOS);
        verificar("D1 queda sin registros", dao.seleccionarPorReferencia(ID_REF_BASE).isEmpty());
        int restantes = CICLOS * 4 - 1 - CICLOS;
        verificar("borrarTodos elimina las " + restantes + " restantes", dao.borrarTodos() == restantes);
        verificar("la tabla queda vacia", dao.seleccionarTodos().isEmpty());
    }

    /**
     * Punto de entrada de la prueba.
     *
     * @param args Argumentos de linea de comandos (no se usan).
     */
    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("  PRUEBA TAREA #20: INSERCION CONTINUA, ULTIMA MUESTRA Y PURGA     ");
        System.out.println("==================================================================");
        try {
            ConexionDB conexion = new ConexionDB("db_config.txt");
            int_proceso_vars_dataDAO varsDAO = new int_proceso_vars_dataDAOImp(conexion);

            int_proceso_refs_dataDAO refsDAO = new int_proceso_refs_dataDAOImp(conexion);

            probarVarsData(varsDAO);
            probarRefsData(refsDAO);

            conexion.cerrarConexion();
        } catch (Exception e) {
            fallos++;
            System.err.println("[ERROR] No se pudo ejecutar la prueba: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("\n==================================================================");
        System.out.println("  RESULTADO: " + aciertos + " pasan, " + fallos + " fallan");
        System.out.println("==================================================================");
    }
}
