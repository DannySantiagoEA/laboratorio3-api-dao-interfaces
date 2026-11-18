-- =====================================================================
-- Datos del hardware del laboratorio 
-- Proceso ID = 1: ATmega328P con 8 entradas analogicas, 4 entradas
-- digitales y 4 salidas digitales, conectado por RS232 a 9600 bps.
--
-- Se ejecuta despues de lab_virtual_db.sql
-- Usa IDs fijos e INSERT IGNORE, asi que se puede ejecutar varias veces.
-- =====================================================================
USE lab_virtual_db;

-- 3.b Proceso que describe el hardware usado en las practicas 1 y 2
UPDATE int_proceso SET
    nombre = 'Planta 8 ADC y 4 DIO',
    descripcion = 'ATmega328P a 16 MHz. 8 entradas analogicas (potenciometros 0-5 V, ADC de 10 bits enviado en 8 bits), 4 entradas digitales (DIP-switch) y 4 salidas digitales (LEDs). Trama 0x7A 0x7B | digital | 8 ADC | 0x7C; comandos 0xAA ... 0x55.',
    tiempo_muestreo = 100.00,
    archivo_especificaciones = 'UART 9600 bps 8-N-1 (COM1 Proteus <-> COM2 Java). Ts entre 10 y 5000 ms.',
    archivo_manual = 'Manual de operacion del laboratorio virtual v1.0'
WHERE id = 1;

-- 3.c Variables de entrada: IDs 1..8 analogicas, 9..12 digitales
INSERT IGNORE INTO int_proceso_vars (id, int_proceso_id, nombre, descripcion, max_2, min) VALUES
    ( 1, 1, 'Analogica 1', 'Potenciometro en A0 (V)', 5.0, 0.0),
    ( 2, 1, 'Analogica 2', 'Potenciometro en A1 (V)', 5.0, 0.0),
    ( 3, 1, 'Analogica 3', 'Potenciometro en A2 (V)', 5.0, 0.0),
    ( 4, 1, 'Analogica 4', 'Potenciometro en A3 (V)', 5.0, 0.0),
    ( 5, 1, 'Analogica 5', 'Potenciometro en A4 (V)', 5.0, 0.0),
    ( 6, 1, 'Analogica 6', 'Potenciometro en A5 (V)', 5.0, 0.0),
    ( 7, 1, 'Analogica 7', 'Potenciometro en A6 (V)', 5.0, 0.0),
    ( 8, 1, 'Analogica 8', 'Potenciometro en A7 (V)', 5.0, 0.0),
    ( 9, 1, 'Digital 1',   'DIP-switch 1 en PD4 (0/1)', 1.0, 0.0),
    (10, 1, 'Digital 2',   'DIP-switch 2 en PD5 (0/1)', 1.0, 0.0),
    (11, 1, 'Digital 3',   'DIP-switch 3 en PD6 (0/1)', 1.0, 0.0),
    (12, 1, 'Digital 4',   'DIP-switch 4 en PD7 (0/1)', 1.0, 0.0);

-- 3.c Variables de salida (referencias): IDs 1..4
INSERT IGNORE INTO int_proceso_refs (id, int_proceso_id, nombre, descripcion, max_2, min) VALUES
    (1, 1, 'Salida Digital 1', 'LED 1 en PB0 (0/1)', 1.0, 0.0),
    (2, 1, 'Salida Digital 2', 'LED 2 en PB1 (0/1)', 1.0, 0.0),
    (3, 1, 'Salida Digital 3', 'LED 3 en PB2 (0/1)', 1.0, 0.0),
    (4, 1, 'Salida Digital 4', 'LED 4 en PB3 (0/1)', 1.0, 0.0);

-- Control del muestreo: parametro1 = Ts actual, 2 = Ts minimo, 3 = Ts maximo (ms), 4 = baudios
INSERT IGNORE INTO int_proceso_control
    (id, int_proceso_id, nombre, descripcion, parametro1, parametro2, parametro3, parametro4) VALUES
    (1, 1, 'Muestreo', 'Ts actual, Ts minimo, Ts maximo (ms) y velocidad UART (bps)', 100.0, 10.0, 5000.0, 9600.0);