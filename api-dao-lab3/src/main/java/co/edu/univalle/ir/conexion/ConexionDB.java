package co.edu.univalle.ir.conexion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * La clase <b>ConexionDB</b> gestiona el establecimiento y cierre de la
 * conexion con la base de datos MySQL del Laboratorio Virtual mediante JDBC.
 * <p>
 * Cumple con el Requerimiento 1 de la Practica de Laboratorio No. 3,
 * permitiendo inicializar la conexion a traves de los parametros del
 * constructor o mediante la lectura de un archivo de texto plano.
 * </p>
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/11
 */
public class ConexionDB {

    /**
     * Driver JDBC oficial para MySQL.
     */
    private static final String DRIVER_MYSQL = "com.mysql.cj.jdbc.Driver";

    /**
     * Parametros adicionales de conexion para compatibilidad con MySQL 8+.
     */
    private static final String PARAMETROS_JDBC = "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    /**
     * Direccion del servidor de base de datos (ej. localhost).
     */
    private String host;

    /**
     * Puerto de escucha del servidor MySQL (ej. 3306).
     */
    private String puerto;

    /**
     * Nombre del esquema o base de datos en MySQL.
     */
    private String baseDatos;

    /**
     * Nombre de usuario de la base de datos.
     */
    private String usuario;

    /**
     * Contrasena del usuario de la base de datos.
     */
    private String clave;

    /**
     * URL JDBC completa construida para la conexion.
     */
    private String urlJdbc;

    /**
     * Instancia activa de la conexion con la base de datos.
     */
    private Connection dbConn;

    /**
     * Constructor 1 (Por parametros detallados): Establece la conexion con la
     * base de datos recibiendo directamente el host, puerto, nombre de la base
     * de datos, usuario y contrasena.
     *
     * @param host String con la direccion IP o nombre del servidor (ej.
     * "localhost").
     * @param puerto String con el puerto del motor MySQL (ej. "3306").
     * @param baseDatos String con el nombre de la base de datos del
     * laboratorio.
     * @param usuario String con el usuario de acceso a MySQL (ej. "root").
     * @param clave String con la contrasena de acceso a MySQL.
     * @exception SQLException Si ocurre un error al conectar con el servidor
     * MySQL.
     * @exception ClassNotFoundException Si no se encuentra el driver JDBC en el
     * classpath.
     */
    public ConexionDB(String host, String puerto, String baseDatos, String usuario, String clave)
            throws SQLException, ClassNotFoundException {
        this.host = host;
        this.puerto = puerto;
        this.baseDatos = baseDatos;
        this.usuario = usuario;
        this.clave = (clave != null) ? clave : "";
        this.urlJdbc = construirUrlJdbc(this.host, this.puerto, this.baseDatos);
        establecerConexion();
    }

    /**
     * Constructor 2 (Por parametros abreviados): Establece la conexion con la
     * base de datos en localhost:3306 recibiendo el nombre de la base de datos,
     * el usuario y la contrasena.
     *
     * @param baseDatos String con el nombre de la base de datos en localhost.
     * @param usuario String con el nombre de usuario de la base de datos.
     * @param clave String con la contrasena del usuario.
     * @exception SQLException Si las credenciales son invalidas o MySQL esta
     * apagado.
     * @exception ClassNotFoundException Si no se encuentra el driver JDBC.
     */
    public ConexionDB(String baseDatos, String usuario, String clave)
            throws SQLException, ClassNotFoundException {
        this("localhost", "3306", baseDatos, usuario, clave);
    }

    /**
     * Constructor 3 (Por archivo de texto File): Establece la conexion con la
     * base de datos leyendo los parametros (host, port, database, user,
     * password) desde un objeto {@link File} de texto plano.
     *
     * @param archivoConfig Objeto File que apunta al archivo de texto con las
     * credenciales.
     * @exception IOException Si el archivo no existe o no puede leerse.
     * @exception SQLException Si ocurre un fallo al abrir la conexion JDBC.
     * @exception ClassNotFoundException Si no se encuentra el driver JDBC de
     * MySQL.
     */
    public ConexionDB(File archivoConfig)
            throws IOException, SQLException, ClassNotFoundException {
        cargarParametrosDesdeArchivo(archivoConfig);
        this.urlJdbc = construirUrlJdbc(this.host, this.puerto, this.baseDatos);
        establecerConexion();
    }

    /**
     * Constructor 4 (Por ruta de archivo de texto): Establece la conexion con
     * la base de datos recibiendo la ruta en disco del archivo de texto de
     * configuracion.
     *
     * @param rutaArchivoConfig String con la ruta del archivo de texto (ej.
     * "db_config.txt").
     * @exception IOException Si la ruta es invalida o falla la lectura del
     * archivo.
     * @exception SQLException Si ocurre un fallo al conectar con la base de
     * datos.
     * @exception ClassNotFoundException Si no se encuentra el driver JDBC de
     * MySQL.
     */
    public ConexionDB(String rutaArchivoConfig)
            throws IOException, SQLException, ClassNotFoundException {
        this(new File(rutaArchivoConfig));
    }

    /**
     * Lee y procesa el archivo de texto plano en formato clave=valor para
     * extraer los parametros de conexion a la base de datos.
     *
     * @param archivo Objeto File del archivo de texto a leer.
     * @exception IOException Si el archivo no existe, es un directorio o falla
     * su lectura.
     */
    private void cargarParametrosDesdeArchivo(File archivo) throws IOException {
        if (archivo == null || !archivo.exists() || !archivo.isFile()) {
            throw new IOException("No se encontro el archivo de configuracion de DB en: "
                    + (archivo != null ? archivo.getAbsolutePath() : "null"));
        }

        Map<String, String> parametros = new HashMap<>();

        try (BufferedReader lector = new BufferedReader(
                new FileReader(archivo, StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                linea = linea.trim();
                // Ignorar lineas vacias o comentarios que inicien con '#' o '//'
                if (linea.isEmpty() || linea.startsWith("#") || linea.startsWith("//")) {
                    continue;
                }
                int indiceIgual = linea.indexOf('=');
                if (indiceIgual > 0) {
                    String claveParam = linea.substring(0, indiceIgual).trim().toLowerCase();
                    String valorParam = linea.substring(indiceIgual + 1).trim();
                    parametros.put(claveParam, valorParam);
                }
            }
        }

        this.host = parametros.getOrDefault("host", "localhost");
        this.puerto = parametros.getOrDefault("port", "3306");
        this.baseDatos = parametros.get("database");
        this.usuario = parametros.getOrDefault("user", "root");
        this.clave = parametros.getOrDefault("password", "");

        if (this.baseDatos == null || this.baseDatos.isEmpty()) {
            throw new IOException("El archivo de texto no especifica la propiedad obligatoria 'database='.");
        }
    }

    /**
     * Construye la cadena URL de conexion JDBC para MySQL.
     *
     * @param host String con la direccion del servidor.
     * @param puerto String con el puerto TCP de MySQL.
     * @param baseDatos String con el nombre de la base de datos.
     * @return String con la URL en formato jdbc:mysql://host:puerto/baseDatos.
     */
    private String construirUrlJdbc(String host, String puerto, String baseDatos) {
        return "jdbc:mysql://" + host + ":" + puerto + "/" + baseDatos + PARAMETROS_JDBC;
    }

    /**
     * Registra el driver JDBC en la maquina virtual de Java y abre la conexion
     * fisica hacia el servidor de base de datos MySQL.
     *
     * @return Objeto Connection activo establecido con la base de datos.
     * @exception SQLException Si falla la autenticacion o la comunicacion con
     * MySQL.
     * @exception ClassNotFoundException Si no se encuentra la clase del driver
     * JDBC.
     */
    public final Connection establecerConexion() throws SQLException, ClassNotFoundException {
        Class.forName(DRIVER_MYSQL);
        if (this.dbConn == null || this.dbConn.isClosed()) {
            this.dbConn = DriverManager.getConnection(this.urlJdbc, this.usuario, this.clave);
        }
        return this.dbConn;
    }

    /**
     * Retorna el objeto de conexion JDBC actual. Si la conexion fue cerrada
     * previamente, intenta reabrirla con los parametros almacenados.
     *
     * @return Objeto Connection activo para ser utilizado por las clases DAO.
     * @exception SQLException Si no es posible obtener o reabrir la conexion.
     */
    public Connection getConnection() throws SQLException {
        if (this.dbConn == null || this.dbConn.isClosed()) {
            this.dbConn = DriverManager.getConnection(this.urlJdbc, this.usuario, this.clave);
        }
        return this.dbConn;
    }

    /**
     * Verifica si la conexion con la base de datos se encuentra abierta y
     * valida.
     *
     * @return boolean true si la conexion esta activa; false en caso contrario.
     */
    public boolean isConectado() {
        try {
            return this.dbConn != null && !this.dbConn.isClosed() && this.dbConn.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Cierra de forma segura la conexion activa con la base de datos para
     * liberar los recursos del servidor MySQL.
     *
     * @exception SQLException Si ocurre un error al cerrar el descriptor de
     * conexion.
     */
    public void cerrarConexion() throws SQLException {
        if (this.dbConn != null && !this.dbConn.isClosed()) {
            this.dbConn.close();
        }
    }

    /**
     * Obtiene la URL JDBC configurada en esta instancia.
     *
     * @return String con la URL JDBC.
     */
    public String getUrlJdbc() {
        return urlJdbc;
    }

    /**
     * Obtiene el nombre de la base de datos conectada.
     *
     * @return String con el nombre de la base de datos.
     */
    public String getBaseDatos() {
        return baseDatos;
    }

    /**
     * Obtiene el usuario configurado para la conexion.
     *
     * @return String con el nombre de usuario.
     */
    public String getUsuario() {
        return usuario;
    }
}
