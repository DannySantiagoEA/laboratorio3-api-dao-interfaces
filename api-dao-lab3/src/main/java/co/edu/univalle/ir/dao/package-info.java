/**
 * Interfaces DAO (Data Access Objects) y sus implementaciones para operar la
 * base de datos del Laboratorio Virtual.
 * <p>
 * Cada interfaz <code>*DAO</code> define el contrato de acceso a una o varias
 * tablas y su clase <code>*DAOImp</code> lo implementa con sentencias SQL
 * preparadas sobre una conexion obtenida de
 * {@link co.edu.univalle.ir.conexion.ConexionDB}. Las clases
 * <code>Prueba*</code> son programas de verificacion de cada tarea y no hacen
 * parte del JAR distribuible.
 * </p>
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/16
 */
package co.edu.univalle.ir.dao;