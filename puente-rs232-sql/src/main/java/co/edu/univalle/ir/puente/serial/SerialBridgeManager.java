package co.edu.univalle.ir.puente.serial;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortInvalidPortException;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

/**
 * Gestor de comunicacion RS232 mediante jSerialComm para el programa puente
 * RS232-SQL (HU-06 - Tarea #28 y soporte Tarea #29).
 * <p>
 * Implementa el protocolo binario del sistema embebido:
 * <ul>
 * <li>Recepcion (Micro -> PC): [0x7A, 0x7B, BYTE_DIG, ADC0..ADC7, 0x7C] (12
 * bytes en 8 bits o 20 bytes en 10 bits).</li>
 * <li>Transmision (PC -> Micro): [0xAA, 0x01, MASCARA_LEDS, 0x55] y [0xAA,
 * 0x02, MSB_Ts, LSB_Ts, 0x55].</li>
 * </ul>
 * </p>
 *
 * @author Danny Santiago Erazo Agredo
 * @author Brayan Alexis Gomez Castano
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/21
 */
public class SerialBridgeManager {

    // Parametros seriales estandarizados (9600 bps, 8-N-1)
    public static final int DEFAULT_BAUD_RATE = 9600;
    public static final int DEFAULT_DATA_BITS = 8;
    public static final int DEFAULT_STOP_BITS = SerialPort.ONE_STOP_BIT;
    public static final int DEFAULT_PARITY = SerialPort.NO_PARITY;

    // Delimitadores de trama continua (Micro -> PC)
    public static final byte HEADER_BYTE_1 = (byte) 0x7A;
    public static final byte HEADER_BYTE_2 = (byte) 0x7B;
    public static final byte TAIL_BYTE = (byte) 0x7C;

    // Delimitadores y tipos de comando (PC -> Micro)
    public static final byte CMD_START_BYTE = (byte) 0xAA;
    public static final byte CMD_END_BYTE = (byte) 0x55;
    public static final byte CMD_TIPO_LEDS = (byte) 0x01;
    public static final byte CMD_TIPO_TS = (byte) 0x02;
    public static final byte CMD_TIPO_FLUJO = (byte) 0x03;

    // Dimensiones del hardware del laboratorio virtual
    public static final int NUM_CANALES_ANALOGICOS = 8;
    public static final int NUM_CANALES_DIGITALES = 4;
    public static final float VOLTAJE_REFERENCIA = 5.0f;

    public static final int LONGITUD_TRAMA_8_BITS = 12;
    public static final int LONGITUD_TRAMA_10_BITS = 20;

    private SerialPort puertoActivo;
    private final ByteArrayOutputStream bufferRecepcion = new ByteArrayOutputStream();
    private boolean modoAdc10Bits = false; // Debe coincidir con #define ADC_10_BITS 0 en firmware

    private long tramasValidasRecibidas = 0;
    private long bytesDescartadosPorRuido = 0;

    private TramaRecibidaListener listenerTrama;

    /**
     * Callback invocado cada vez que el parser valida una trama completa.
     */
    @FunctionalInterface
    public interface TramaRecibidaListener {

        void onTramaValida(TramaTelemetria trama);
    }

    public void setTramaRecibidaListener(TramaRecibidaListener listener) {
        this.listenerTrama = listener;
    }

    public synchronized void setModoAdc10Bits(boolean usar10Bits) {
        this.modoAdc10Bits = usar10Bits;
        this.bufferRecepcion.reset();
    }

    public synchronized boolean isModoAdc10Bits() {
        return modoAdc10Bits;
    }

    public synchronized int getLongitudTramaEsperada() {
        return modoAdc10Bits ? LONGITUD_TRAMA_10_BITS : LONGITUD_TRAMA_8_BITS;
    }

    /**
     * Verifica si el nombre del puerto COM existe en el sistema operativo.
     *
     * @param nombrePuerto Nombre del puerto (ej. "COM2").
     * @return true si el puerto esta presente.
     */
    public boolean existePuerto(String nombrePuerto) {
        if (nombrePuerto == null || nombrePuerto.isBlank()) {
            return false;
        }
        for (SerialPort puerto : SerialPort.getCommPorts()) {
            if (puerto.getSystemPortName().equalsIgnoreCase(nombrePuerto.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Abre y configura el puerto serial a 9600 bps, 8-N-1, registrando el
     * escuchador asincrono de recepcion de bytes.
     *
     * @param nombrePuerto Nombre del puerto COM (ej. "COM2").
     * @throws SerialConnectionException Si el puerto no existe o esta ocupado.
     */
    public synchronized void conectar(String nombrePuerto) throws SerialConnectionException {
        conectar(nombrePuerto, DEFAULT_BAUD_RATE);
    }

    /**
     * Abre y configura el puerto serial a la velocidad en baudios indicada.
     *
     * @param nombrePuerto Nombre del puerto COM (ej. "COM2").
     * @param baudRate Velocidad en baudios (ej. 9600).
     * @throws SerialConnectionException Si falla la apertura o configuracion.
     */
    public synchronized void conectar(String nombrePuerto, int baudRate) throws SerialConnectionException {
        if (nombrePuerto == null || nombrePuerto.isBlank()) {
            throw new SerialConnectionException("Debe especificar un puerto COM valido (ej. COM2).");
        }
        String puertoLimpio = nombrePuerto.trim().toUpperCase();

        if (isConectado()) {
            desconectar();
        }
        if (!existePuerto(puertoLimpio)) {
            throw new SerialConnectionException("El puerto '" + puertoLimpio + "' no fue detectado en el sistema.");
        }

        try {
            SerialPort candidato = SerialPort.getCommPort(puertoLimpio);
            if (!candidato.setComPortParameters(baudRate, DEFAULT_DATA_BITS, DEFAULT_STOP_BITS, DEFAULT_PARITY)) {
                throw new SerialConnectionException("No se pudieron configurar los parametros 8-N-1 en " + puertoLimpio);
            }
            candidato.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);

            if (!candidato.openPort()) {
                throw new SerialConnectionException(
                        "No se pudo abrir " + puertoLimpio + ". Verifique que no este ocupado por otra aplicacion.");
            }

            candidato.flushIOBuffers();
            this.puertoActivo = candidato;
            this.bufferRecepcion.reset();
            registrarEscuchadorAsincrono();

            System.out.printf("[OK SERIAL] Puerto %s abierto (%d bps, 8-N-1) | Modo ADC: %s (%d bytes/trama)%n",
                    puertoActivo.getSystemPortName(),
                    baudRate,
                    modoAdc10Bits ? "10 bits" : "8 bits",
                    getLongitudTramaEsperada());

        } catch (SerialPortInvalidPortException ex) {
            this.puertoActivo = null;
            throw new SerialConnectionException("Puerto serial invalido: " + ex.getMessage(), ex);
        } catch (SerialConnectionException ex) {
            this.puertoActivo = null;
            throw ex;
        } catch (Exception ex) {
            this.puertoActivo = null;
            throw new SerialConnectionException("Error inesperado abriendo " + puertoLimpio + ": " + ex.getMessage(), ex);
        }
    }

    /**
     * Cierra de forma segura el puerto serial liberando los recursos nativos.
     */
    public synchronized void desconectar() {
        if (puertoActivo != null) {
            String nombre = puertoActivo.getSystemPortName();
            try {
                puertoActivo.removeDataListener();
                if (puertoActivo.isOpen()) {
                    puertoActivo.flushIOBuffers();
                    puertoActivo.closePort();
                    System.out.println("[OK SERIAL] Puerto " + nombre + " cerrado correctamente.");
                }
            } catch (Exception e) {
                System.err.println("[ERROR SERIAL] Al cerrar " + nombre + ": " + e.getMessage());
            } finally {
                puertoActivo = null;
                bufferRecepcion.reset();
            }
        }
    }

    public synchronized boolean isConectado() {
        return puertoActivo != null && puertoActivo.isOpen();
    }

    private void registrarEscuchadorAsincrono() {
        puertoActivo.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
            }

            @Override
            public void serialEvent(SerialPortEvent event) {
                if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
                    return;
                }
                if (puertoActivo == null || !puertoActivo.isOpen()) {
                    return;
                }
                int disponibles = puertoActivo.bytesAvailable();
                if (disponibles <= 0) {
                    return;
                }
                byte[] bloque = new byte[disponibles];
                int leidos = puertoActivo.readBytes(bloque, bloque.length);
                if (leidos > 0) {
                    procesarFlujoBytes(bloque, leidos);
                }
            }
        });
    }

    /**
     * Acumula los bytes recibidos, sincroniza por cabecera [0x7A, 0x7B] y cola
     * [0x7C], descarta ruido y notifica cada trama valida al listener
     * registrado.
     *
     * @param nuevosBytes Arreglo de bytes entrantes.
     * @param cantidad Cantidad de bytes validos en el arreglo.
     */
    public synchronized void procesarFlujoBytes(byte[] nuevosBytes, int cantidad) {
        if (nuevosBytes == null || cantidad <= 0) {
            return;
        }

        bufferRecepcion.write(nuevosBytes, 0, cantidad);
        byte[] buffer = bufferRecepcion.toByteArray();

        int longitudTrama = getLongitudTramaEsperada();
        int cursor = 0;

        while (cursor <= buffer.length - longitudTrama) {
            if (buffer[cursor] == HEADER_BYTE_1 && buffer[cursor + 1] == HEADER_BYTE_2) {
                int posCola = cursor + longitudTrama - 1;
                if (buffer[posCola] == TAIL_BYTE) {
                    byte[] tramaCompleta = Arrays.copyOfRange(buffer, cursor, cursor + longitudTrama);
                    TramaTelemetria trama = desempaquetarTrama(tramaCompleta);
                    if (listenerTrama != null) {
                        listenerTrama.onTramaValida(trama);
                    }
                    cursor += longitudTrama;
                } else {
                    bytesDescartadosPorRuido++;
                    cursor++;
                }
            } else {
                bytesDescartadosPorRuido++;
                cursor++;
            }
        }

        bufferRecepcion.reset();
        if (cursor < buffer.length) {
            bufferRecepcion.write(buffer, cursor, buffer.length - cursor);
        }
    }

    private TramaTelemetria desempaquetarTrama(byte[] trama) {
        tramasValidasRecibidas++;

        // Byte 2: 4 entradas digitales (bits b0..b3)
        byte byteDigital = trama[2];
        int[] digitales4 = new int[NUM_CANALES_DIGITALES];
        for (int i = 0; i < NUM_CANALES_DIGITALES; i++) {
            digitales4[i] = (byteDigital >> i) & 0x01;
        }

        // Bytes 3..10 (8 bits) o 3..18 (10 bits): 8 entradas analogicas (0.0f a 5.0f V)
        int[] cuentasAdc = new int[NUM_CANALES_ANALOGICOS];
        float[] voltajes8 = new float[NUM_CANALES_ANALOGICOS];

        if (!modoAdc10Bits) {
            for (int ch = 0; ch < NUM_CANALES_ANALOGICOS; ch++) {
                int cuenta8Bits = trama[3 + ch] & 0xFF;
                cuentasAdc[ch] = cuenta8Bits;
                voltajes8[ch] = (cuenta8Bits * VOLTAJE_REFERENCIA) / 255.0f;
            }
        } else {
            for (int ch = 0; ch < NUM_CANALES_ANALOGICOS; ch++) {
                int offset = 3 + (ch * 2);
                int msb = trama[offset] & 0xFF;
                int lsb = trama[offset + 1] & 0xFF;
                int cuenta10Bits = ((msb << 8) | lsb) & 0x03FF;
                cuentasAdc[ch] = cuenta10Bits;
                voltajes8[ch] = (cuenta10Bits * VOLTAJE_REFERENCIA) / 1023.0f;
            }
        }

        return new TramaTelemetria(
                tramasValidasRecibidas,
                byteDigital,
                digitales4,
                cuentasAdc,
                voltajes8
        );
    }

    // =========================================================================
    // METODOS DE TRANSMISION DE COMANDOS (SOPORTE PARA TAREA #29)
    // =========================================================================
    /**
     * Envia el estado de las 4 salidas digitales hacia el microcontrolador:
     * [0xAA, 0x01, MASCARA_LEDS, 0x55].
     *
     * @param estados4Salidas Arreglo boolean[4] con el estado ON/OFF de las
     * salidas.
     * @throws SerialConnectionException Si no hay puerto abierto o falla la
     * escritura.
     */
    public synchronized void enviarComandoLeds(boolean[] estados4Salidas) throws SerialConnectionException {
        if (!isConectado()) {
            throw new SerialConnectionException("Puerto serial desconectado para envio de salidas digitales.");
        }
        if (estados4Salidas == null || estados4Salidas.length < NUM_CANALES_DIGITALES) {
            throw new IllegalArgumentException("Se requieren 4 estados de salida digital.");
        }
        byte mascara = 0;
        for (int i = 0; i < NUM_CANALES_DIGITALES; i++) {
            if (estados4Salidas[i]) {
                mascara |= (1 << i);
            }
        }
        byte[] trama = new byte[]{CMD_START_BYTE, CMD_TIPO_LEDS, (byte) (mascara & 0x0F), CMD_END_BYTE};
        int escritos = puertoActivo.writeBytes(trama, trama.length);
        if (escritos != trama.length) {
            throw new SerialConnectionException("Escritura incompleta de comando LEDs.");
        }
    }

    /**
     * Envia el nuevo periodo de muestreo Ts en milisegundos hacia el
     * microcontrolador: [0xAA, 0x02, MSB_Ts, LSB_Ts, 0x55].
     *
     * @param periodoMs Periodo de muestreo en milisegundos (10 a 65535 ms).
     * @throws SerialConnectionException Si el puerto no esta conectado o falla
     * el envio.
     */
    public synchronized void enviarComandoMuestreo(int periodoMs) throws SerialConnectionException {
        if (!isConectado()) {
            throw new SerialConnectionException("Puerto serial desconectado para envio de tiempo de muestreo.");
        }
        if (periodoMs < 10 || periodoMs > 65535) {
            throw new IllegalArgumentException("El periodo de muestreo debe estar entre 10 ms y 65535 ms.");
        }
        byte msb = (byte) ((periodoMs >> 8) & 0xFF);
        byte lsb = (byte) (periodoMs & 0xFF);
        byte[] trama = new byte[]{CMD_START_BYTE, CMD_TIPO_TS, msb, lsb, CMD_END_BYTE};
        int escritos = puertoActivo.writeBytes(trama, trama.length);
        if (escritos != trama.length) {
            throw new SerialConnectionException("Escritura incompleta de comando Ts.");
        }
    }

    public synchronized long getTramasValidasRecibidas() {
        return tramasValidasRecibidas;
    }

    public synchronized long getBytesDescartadosPorRuido() {
        return bytesDescartadosPorRuido;
    }
}
