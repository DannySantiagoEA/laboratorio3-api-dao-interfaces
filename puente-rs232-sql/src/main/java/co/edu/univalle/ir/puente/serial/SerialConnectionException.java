package co.edu.univalle.ir.puente.serial;

/**
 * Excepcion verificada para reportar errores durante la apertura,
 * configuracion, lectura o escritura del puerto serial RS232 en el puente
 * RS232-SQL (HU-06).
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/21
 */
public class SerialConnectionException extends Exception {

    /**
     * Crea una nueva excepcion serial con un mensaje descriptivo.
     *
     * @param message Descripcion del fallo en el enlace serial.
     */
    public SerialConnectionException(String message) {
        super(message);
    }

    /**
     * Crea una nueva excepcion serial conservando la causa original.
     *
     * @param message Descripcion del fallo en el enlace serial.
     * @param cause Excepcion subyacente que origino el error.
     */
    public SerialConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
