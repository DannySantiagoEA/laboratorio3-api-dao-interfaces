-- =====================================================================
-- Interfaces 710044M - Universidad del Valle
--
-- =====================================================================

CREATE DATABASE IF NOT EXISTS lab_virtual_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE lab_virtual_db;

-- ---------------------------------------------------------------------
-- Usuarios y perfiles
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS int_usuarios_tipo (
    id          INTEGER AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(80) NOT NULL,
    descripcion BLOB
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS int_usuarios (
    id                   INTEGER AUTO_INCREMENT PRIMARY KEY,
    int_usuarios_tipo_id INTEGER NOT NULL,
    nombres              VARCHAR(255) NOT NULL,
    apellidos            VARCHAR(255) NOT NULL,
    email                BLOB,
    clave                VARCHAR(30) NOT NULL,
    INDEX int_usuarios_FKIndex1 (int_usuarios_tipo_id),
    CONSTRAINT fk_usr_tipo FOREIGN KEY (int_usuarios_tipo_id)
        REFERENCES int_usuarios_tipo(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS int_usuarios_priv (
    id                   INTEGER AUTO_INCREMENT PRIMARY KEY,
    int_usuarios_tipo_id INTEGER NOT NULL,
    control              ENUM('0','1') NOT NULL DEFAULT '0',
    configuracion        ENUM('0','1') NOT NULL DEFAULT '0',
    descarga_datos       ENUM('0','1') NOT NULL DEFAULT '0',
    monitor              ENUM('0','1') NOT NULL DEFAULT '0',
    envio_datos          ENUM('0','1') NOT NULL DEFAULT '0',
    INDEX int_usuarios_priv_FKIndex1 (int_usuarios_tipo_id),
    CONSTRAINT fk_priv_tipo FOREIGN KEY (int_usuarios_tipo_id)
        REFERENCES int_usuarios_tipo(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Proceso e informacion
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS int_proceso_tipo (
    id          INTEGER AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(80) NOT NULL,
    descripcion BLOB
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS int_proceso (
    id                       INTEGER AUTO_INCREMENT PRIMARY KEY,
    int_proceso_tipo_id      INTEGER NOT NULL,
    nombre                   VARCHAR(255) NOT NULL,
    descripcion              BLOB,
    tiempo_muestreo          FLOAT(10,2) DEFAULT 100.00,
    archivo_especificaciones BLOB,
    archivo_manual           BLOB,
    INDEX int_proceso_FKIndex1 (int_proceso_tipo_id),
    CONSTRAINT fk_proc_tipo FOREIGN KEY (int_proceso_tipo_id)
        REFERENCES int_proceso_tipo(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS int_usuarios_procesos (
    int_usuarios_id INTEGER NOT NULL,
    int_proceso_id  INTEGER NOT NULL,
    fecha_inicio    DATE,
    fecha_fin       DATE,
    hora_inicio     TIME,
    hora_fin        TIME,
    PRIMARY KEY (int_usuarios_id, int_proceso_id),
    CONSTRAINT fk_up_usr FOREIGN KEY (int_usuarios_id)
        REFERENCES int_usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_up_prc FOREIGN KEY (int_proceso_id)
        REFERENCES int_proceso(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS int_proceso_vars (
    id             INTEGER AUTO_INCREMENT PRIMARY KEY,
    int_proceso_id INTEGER NOT NULL,
    nombre         VARCHAR(80) NOT NULL,
    descripcion    BLOB,
    max_2          FLOAT(10,4),
    min            FLOAT(10,4),
    INDEX int_proceso_vars_FKIndex1 (int_proceso_id),
    CONSTRAINT fk_vars_proc FOREIGN KEY (int_proceso_id)
        REFERENCES int_proceso(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS int_proceso_refs (
    id             INTEGER AUTO_INCREMENT PRIMARY KEY,
    int_proceso_id INTEGER NOT NULL,
    nombre         VARCHAR(80) NOT NULL,
    descripcion    BLOB,
    max_2          FLOAT(10,4),
    min            FLOAT(10,4),
    INDEX int_proceso_refs_FKIndex1 (int_proceso_id),
    CONSTRAINT fk_refs_proc FOREIGN KEY (int_proceso_id)
        REFERENCES int_proceso(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS int_proceso_control (
    id             INTEGER AUTO_INCREMENT PRIMARY KEY,
    int_proceso_id INTEGER NOT NULL,
    nombre         VARCHAR(80) NOT NULL,
    descripcion    BLOB,
    parametro1     FLOAT(10,4),
    parametro2     FLOAT(10,4),
    parametro3     FLOAT(10,4),
    parametro4     FLOAT(10,4),
    INDEX int_proceso_control_FKIndex1 (int_proceso_id),
    CONSTRAINT fk_ctrl_proc FOREIGN KEY (int_proceso_id)
        REFERENCES int_proceso(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS int_proceso_vars_data (
    id                  INTEGER AUTO_INCREMENT PRIMARY KEY,
    int_proceso_vars_id INTEGER NOT NULL,
    valor               FLOAT(10,4),
    tiempo              FLOAT(10,4),
    fecha               DATE,
    hora                TIME,
    INDEX int_proceso_vars_data_FKIndex1 (int_proceso_vars_id),
    CONSTRAINT fk_vdata_vars FOREIGN KEY (int_proceso_vars_id)
        REFERENCES int_proceso_vars(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS int_proceso_refs_data (
    id                  INTEGER AUTO_INCREMENT PRIMARY KEY,
    int_proceso_refs_id INTEGER NOT NULL,
    valor               FLOAT(10,4),
    tiempo              FLOAT(10,4),
    fecha               DATE,
    hora                TIME,
    INDEX int_proceso_refs_data_FKIndex1 (int_proceso_refs_id),
    CONSTRAINT fk_rdata_refs FOREIGN KEY (int_proceso_refs_id)
        REFERENCES int_proceso_refs(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Registros semilla (los mismos IDs que usan las pruebas del equipo)
-- ---------------------------------------------------------------------
INSERT IGNORE INTO int_usuarios_tipo (id, nombre, descripcion) VALUES
    (1, 'Estudiante',    'Perfil laboratorio'),
    (2, 'Administrador', 'Gestion completa del laboratorio virtual');

INSERT IGNORE INTO int_usuarios_priv
    (id, int_usuarios_tipo_id, control, configuracion, descarga_datos, monitor, envio_datos) VALUES
    (1, 1, '1', '0', '1', '1', '0'),
    (2, 2, '1', '1', '1', '1', '1');

INSERT IGNORE INTO int_usuarios (id, int_usuarios_tipo_id, nombres, apellidos, email, clave) VALUES
    (1, 1, 'Danny Santiago', 'Erazo Agredo', 'danny.erazo@correounivalle.edu.co', '12345');

INSERT IGNORE INTO int_proceso_tipo (id, nombre, descripcion) VALUES
    (1, 'Adquisicion Embebida', 'RS232 DAQ');

INSERT IGNORE INTO int_proceso (id, int_proceso_tipo_id, nombre, descripcion, tiempo_muestreo) VALUES
    (1, 1, 'Planta 8 ADC y 4 DIO', 'Hardware Lab 3', 100.0);