package co.edu.univalle.ir.puente.serial;

import java.util.Arrays;

/**
 * Objeto inmutable que representa una trama de telemetria recibida del sistema
 * embebido por RS232 tras validar su cabecera (0x7A, 0x7B) y su cola (0x7C).
 * <p>
 * Contiene las 8 lecturas analogicas escaladas (0.0 a 5.0 V) y los 4 estados
 * digitales de entrada (0 o 1) listos para almacenarse en
 * <code>int_proceso_vars_data</code> (Tarea #28 - Entregables 3.d y 3.g).
 * </p>
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/21
 */
public class TramaTelemetria {

    private final long numeroSecuencia;
    private final byte byteDigitalCrudo;
    private final int[] estadosDigitales;        // 4 entradas digitales (0 o 1)
    private final int[] cuentasAdc;              // 8 cuentas crudas (0..255 o 0..1023)
    private final float[] voltajesAnalogicos;    // 8 voltajes (0.0f..5.0f V) para DAO
    private final long timestampNanos;

    /**
     * Construye una instancia de trama decodificada.
     *
     * @param numeroSecuencia Contador incremental de tramas validas.
     * @param byteDigitalCrudo Byte crudo de los 4 interruptores digitales
     * (b0..b3).
     * @param estadosDigitales Arreglo int[4] con los estados logicos (0 o 1).
     * @param cuentasAdc Arreglo int[8] con las lecturas crudas del ADC.
     * @param voltajesAnalogicos Arreglo float[8] con los voltajes de 0.0 a 5.0
     * V.
     */
    public TramaTelemetria(long numeroSecuencia, byte byteDigitalCrudo,
            int[] estadosDigitales, int[] cuentasAdc, float[] voltajesAnalogicos) {
        this.numeroSecuencia = numeroSecuencia;
        this.byteDigitalCrudo = byteDigitalCrudo;
        this.estadosDigitales = estadosDigitales.clone();
        this.cuentasAdc = cuentasAdc.clone();
        this.voltajesAnalogicos = voltajesAnalogicos.clone();
        this.timestampNanos = System.nanoTime();
    }

    public long getNumeroSecuencia() {
        return numeroSecuencia;
    }

    public byte getByteDigitalCrudo() {
        return byteDigitalCrudo;
    }

    public int[] getEstadosDigitales() {
        return estadosDigitales.clone();
    }

    public int[] getCuentasAdc() {
        return cuentasAdc.clone();
    }

    public float[] getVoltajesAnalogicos() {
        return voltajesAnalogicos.clone();
    }

    public long getTimestampNanos() {
        return timestampNanos;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Trama #%d | Dig(0x%02X)=%s | ADC(V)=[",
                numeroSecuencia, byteDigitalCrudo & 0xFF, Arrays.toString(estadosDigitales)));
        for (int i = 0; i < voltajesAnalogicos.length; i++) {
            sb.append(String.format("%.2f", voltajesAnalogicos[i]));
            if (i < voltajesAnalogicos.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
