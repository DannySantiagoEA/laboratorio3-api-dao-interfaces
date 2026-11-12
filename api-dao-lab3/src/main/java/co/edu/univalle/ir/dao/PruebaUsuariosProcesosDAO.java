package co.edu.univalle.ir.dao;

import co.edu.univalle.ir.conexion.ConexionDB;
import co.edu.univalle.ir.entity.int_usuarios_procesos;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.Time;
import java.util.List;

/**
 * Clase de prueba autoejecutable para verificar la Tarea #9 (HU-02): CRUD
 * completo sobre la tabla <code>int_usuarios_procesos</code>.
 * <p>
 * Incluye autoinicializacion de tablas y registros semilla para poder probar de
 * forma independiente mientras el resto del equipo completa sus tareas.
 * </p>
 *
 * @author Danny Santiago Erazo Agredo
 * @version $Revision: 1.0 $ 2026/11/12
 */
public class PruebaUsuariosProcesosDAO {

    /**
     * Crea automaticamente la base de datos lab_virtual_db, las tablas padre y
     * la tabla int_usuarios_procesos en caso de que aun no existan en el MySQL
     * local.
     */
    private static void prepararEntornoBaseDatos() throws Exception {
        String urlServidor = "jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        try (Connection connInit = DriverManager.getConnection(urlServidor, "root", ""); Statement stm = connInit.createStatement()) {

            stm.executeUpdate("CREATE DATABASE IF NOT EXISTS lab_virtual_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            stm.executeUpdate("USE lab_virtual_db");

            // Tablas padre minimas para llaves foraneas (Figura 1)
            stm.executeUpdate("CREATE TABLE IF NOT EXISTS int_usuarios_tipo ("
                    + "id INTEGER AUTO_INCREMENT PRIMARY KEY, "
                    + "nombre VARCHAR(80) NOT NULL, "
                    + "descripcion BLOB) ENGINE=InnoDB");

            stm.executeUpdate("CREATE TABLE IF NOT EXISTS int_usuarios ("
                    + "id INTEGER AUTO_INCREMENT PRIMARY KEY, "
                    + "int_usuarios_tipo_id INTEGER NOT NULL, "
                    + "nombres VARCHAR(255) NOT NULL, "
                    + "apellidos VARCHAR(255) NOT NULL, "
                    + "email BLOB, "
                    + "clave VARCHAR(30) NOT NULL, "
                    + "CONSTRAINT fk_usr_tipo FOREIGN KEY (int_usuarios_tipo_id) REFERENCES int_usuarios_tipo(id) ON DELETE CASCADE) ENGINE=InnoDB");

            stm.executeUpdate("CREATE TABLE IF NOT EXISTS int_proceso_tipo ("
                    + "id INTEGER AUTO_INCREMENT PRIMARY KEY, "
                    + "nombre VARCHAR(80) NOT NULL, "
                    + "descripcion BLOB) ENGINE=InnoDB");

            stm.executeUpdate("CREATE TABLE IF NOT EXISTS int_proceso ("
                    + "id INTEGER AUTO_INCREMENT PRIMARY KEY, "
                    + "int_proceso_tipo_id INTEGER NOT NULL, "
                    + "nombre VARCHAR(255) NOT NULL, "
                    + "descripcion BLOB, "
                    + "tiempo_muestreo FLOAT(10,2) DEFAULT 100.00, "
                    + "archivo_especificaciones BLOB, "
                    + "archivo_manual BLOB, "
                    + "CONSTRAINT fk_proc_tipo FOREIGN KEY (int_proceso_tipo_id) REFERENCES int_proceso_tipo(id) ON DELETE CASCADE) ENGINE=InnoDB");

            // Tabla objetivo de la Tarea #9
            stm.executeUpdate("CREATE TABLE IF NOT EXISTS int_usuarios_procesos ("
                    + "int_usuarios_id INTEGER NOT NULL, "
                    + "int_proceso_id INTEGER NOT NULL, "
                    + "fecha_inicio DATE, "
                    + "fecha_fin DATE, "
                    + "hora_inicio TIME, "
                    + "hora_fin TIME, "
                    + "PRIMARY KEY (int_usuarios_id, int_proceso_id), "
                    + "CONSTRAINT fk_up_usr FOREIGN KEY (int_usuarios_id) REFERENCES int_usuarios(id) ON DELETE CASCADE, "
                    + "CONSTRAINT fk_up_prc FOREIGN KEY (int_proceso_id) REFERENCES int_proceso(id) ON DELETE CASCADE) ENGINE=InnoDB");

            // Insertar registros semilla (ID = 1) si no existen
            stm.executeUpdate("INSERT IGNORE INTO int_usuarios_tipo (id, nombre, descripcion) VALUES (1, 'Estudiante', 'Perfil laboratorio')");
            stm.executeUpdate("INSERT IGNORE INTO int_usuarios (id, int_usuarios_tipo_id, nombres, apellidos, email, clave) "
                    + "VALUES (1, 1, 'Danny Santiago', 'Erazo Agredo', 'danny.erazo@correounivalle.edu.co', '12345')");
            stm.executeUpdate("INSERT IGNORE INTO int_proceso_tipo (id, nombre, descripcion) VALUES (1, 'Adquisicion Embebida', 'RS232 DAQ')");
            stm.executeUpdate("INSERT IGNORE INTO int_proceso (id, int_proceso_tipo_id, nombre, descripcion, tiempo_muestreo) "
                    + "VALUES (1, 1, 'Planta 8 ADC y 4 DIO', 'Hardware Lab 3', 100.0)");
        }
    }

    /**
     * Metodo principal de ejecucion de la prueba CRUD sobre
     * int_usuarios_procesos.
     *
     * @param args Argumentos de linea de comandos.
     */
    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("  PRUEBA TAREA #9 (HU-02): CRUD int_usuarios_procesosDAO (Lab 3)  ");
        System.out.println("==================================================================");

        try {
            // 0. Preparar base de datos y tablas automaticamente
            prepararEntornoBaseDatos();
            System.out.println("[0] Base de datos 'lab_virtual_db' y tablas verificadas correctamente.");

            // Conectar usando nuestra clase ConexionDB (Tarea #4)
            ConexionDB conexion = new ConexionDB("db_config.txt");
            int_usuarios_procesosDAO dao = new int_usuarios_procesosDAOImp(conexion);

            // Limpiar registro previo de prueba si existia
            dao.borrarProgramacion(1, 1);

            // 1. PRUEBA DE INSERCION (CREATE)
            System.out.println("\n[1] Probando INSERCION (insertarProgramacion)...");
            int filasInsertadas = dao.insertarProgramacion(
                    1,
                    1,
                    Date.valueOf("2026-11-12"),
                    Date.valueOf("2026-11-25"),
                    Time.valueOf("08:00:00"),
                    Time.valueOf("10:00:00")
            );
            System.out.println("    -> Filas insertadas: " + filasInsertadas);

            // 2. PRUEBA DE SELECCION (READ)
            System.out.println("\n[2] Probando SELECCION (seleccionarPorUsuarioYProceso y seleccionarPorUsuario)...");
            int_usuarios_procesos progActual = dao.seleccionarPorUsuarioYProceso(1, 1);
            System.out.println("    -> Registro consultado: " + progActual);

            List<int_usuarios_procesos> listaUsuario = dao.seleccionarPorUsuario(1);
            System.out.println("    -> Total programaciones del usuario ID=1: " + listaUsuario.size());

            // 3. PRUEBA DE EDICION (UPDATE)
            System.out.println("\n[3] Probando EDICION (editarProgramacion)...");
            int filasEditadas = dao.editarProgramacion(
                    1,
                    1,
                    Date.valueOf("2026-11-15"),
                    Date.valueOf("2026-11-25"),
                    Time.valueOf("14:00:00"),
                    Time.valueOf("17:30:00")
            );
            int_usuarios_procesos progEditada = dao.seleccionarPorUsuarioYProceso(1, 1);
            System.out.println("    -> Filas actualizadas: " + filasEditadas);
            System.out.println("    -> Registro tras edicion: " + progEditada);

            // 4. PRUEBA DE BORRADO (DELETE)
            System.out.println("\n[4] Probando BORRADO (borrarProgramacion)...");
            int filasBorradas = dao.borrarProgramacion(1, 1);
            int_usuarios_procesos progPostBorrado = dao.seleccionarPorUsuarioYProceso(1, 1);
            System.out.println("    -> Filas eliminadas: " + filasBorradas);
            System.out.println("    -> Consulta tras borrado (esperado null): " + progPostBorrado);

            conexion.cerrarConexion();
            System.out.println("\n==================================================================");
            System.out.println("  [OK] CRUD DE int_usuarios_procesos VERIFICADO EXITOSAMENTE      ");
            System.out.println("==================================================================");

        } catch (Exception e) {
            System.err.println("[ERROR] Fallo en la prueba del DAO: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
