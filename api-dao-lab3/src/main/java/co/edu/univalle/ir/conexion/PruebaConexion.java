package co.edu.univalle.ir.conexion;

import java.io.File;

/**
 * Clase de verificacion funcional para la Tarea #4 (HU-01). Comprueba la
 * conexion a MySQL por parametros del constructor y por archivo de texto.
 *
 * @author Danny Santiago Erazo Agredo
 * @version $Revision: 1.0 $
 */
public class PruebaConexion {

    /**
     * Metodo principal de prueba de conectividad JDBC.
     *
     * @param args Argumentos de linea de comandos.
     */
    public static void main(String[] args) {
        System.out.println("==============================================================");
        System.out.println("  PRUEBA TAREA #4 (HU-01): MODULO DE CONEXION JDBC (Lab 3)   ");
        System.out.println("==============================================================");

        // 1. Prueba por parametros del constructor
        System.out.println("\n[1] Probando conexion mediante parametros del constructor...");
        try {
            ConexionDB connParametros = new ConexionDB("localhost", "3306", "lab_virtual_db", "root", "");
            if (connParametros.isConectado()) {
                System.out.println("    -> [OK] Conexion exitosa por parametros a: " + connParametros.getBaseDatos());
            }
            connParametros.cerrarConexion();
        } catch (Exception e) {
            System.err.println("    -> [FALLO] Error en conexion por parametros: " + e.getMessage());
        }

        // 2. Prueba mediante archivo de texto (db_config.txt)
        System.out.println("\n[2] Probando conexion mediante archivo de texto (db_config.txt)...");
        try {
            File archivo = new File("db_config.txt");
            ConexionDB connArchivo = new ConexionDB(archivo);
            if (connArchivo.isConectado()) {
                System.out.println("    -> [OK] Conexion exitosa desde archivo TXT: " + archivo.getAbsolutePath());
                System.out.println("    -> Usuario conectado: " + connArchivo.getUsuario());
            }
            connArchivo.cerrarConexion();
        } catch (Exception e) {
            System.err.println("    -> [FALLO] Error en conexion por archivo de texto: " + e.getMessage());
        }

        System.out.println("==============================================================");
    }
}
