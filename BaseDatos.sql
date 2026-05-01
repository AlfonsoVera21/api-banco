CREATE DATABASE IF NOT EXISTS api_banco;
USE api_banco;

CREATE TABLE IF NOT EXISTS personas (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(120) NOT NULL,
    genero VARCHAR(30) NOT NULL,
    edad INT NOT NULL,
    identificacion VARCHAR(30) NOT NULL,
    direccion VARCHAR(180) NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    fecha_creacion DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    fecha_actualizacion DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    fecha_eliminacion DATETIME(6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_personas_identificacion (identificacion)
);

CREATE TABLE IF NOT EXISTS clientes (
    cliente_id BIGINT NOT NULL,
    contrasena VARCHAR(100) NOT NULL,
    estado BOOLEAN NOT NULL,
    PRIMARY KEY (cliente_id),
    CONSTRAINT fk_clientes_personas FOREIGN KEY (cliente_id) REFERENCES personas(id)
);

CREATE TABLE IF NOT EXISTS cuentas (
    id BIGINT NOT NULL AUTO_INCREMENT,
    numero_cuenta VARCHAR(30) NOT NULL,
    tipo_cuenta VARCHAR(20) NOT NULL,
    saldo_inicial DECIMAL(19, 2) NOT NULL,
    estado BOOLEAN NOT NULL,
    cliente_id BIGINT NOT NULL,
    fecha_creacion DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    fecha_actualizacion DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    fecha_eliminacion DATETIME(6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cuentas_numero_cuenta (numero_cuenta),
    CONSTRAINT fk_cuentas_clientes FOREIGN KEY (cliente_id) REFERENCES clientes(cliente_id)
);

CREATE TABLE IF NOT EXISTS movimientos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    fecha DATETIME(6) NOT NULL,
    tipo_movimiento VARCHAR(20) NOT NULL,
    valor DECIMAL(19, 2) NOT NULL,
    saldo DECIMAL(19, 2) NOT NULL,
    estado BOOLEAN NOT NULL,
    cuenta_id BIGINT NOT NULL,
    fecha_creacion DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    fecha_actualizacion DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    fecha_eliminacion DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_movimientos_cuentas FOREIGN KEY (cuenta_id) REFERENCES cuentas(id)
);

INSERT INTO personas (id, nombre, genero, edad, identificacion, direccion, telefono)
VALUES
    (1, 'Jose Lema', 'MASCULINO', 30, 'ID-001', 'Otavalo sn y principal', '098254785'),
    (2, 'Marianela Montalvo', 'FEMENINO', 28, 'ID-002', 'Amazonas y NNUU', '097548965'),
    (3, 'Juan Osorio', 'MASCULINO', 32, 'ID-003', '13 junio y Equinoccial', '098874587')
ON DUPLICATE KEY UPDATE
    nombre = VALUES(nombre),
    genero = VALUES(genero),
    edad = VALUES(edad),
    direccion = VALUES(direccion),
    telefono = VALUES(telefono);

INSERT INTO clientes (cliente_id, contrasena, estado)
VALUES
    (1, '1234', TRUE),
    (2, '5678', TRUE),
    (3, '1245', TRUE)
ON DUPLICATE KEY UPDATE
    contrasena = VALUES(contrasena),
    estado = VALUES(estado);

INSERT INTO cuentas (id, numero_cuenta, tipo_cuenta, saldo_inicial, estado, cliente_id)
VALUES
    (1, '478758', 'AHORROS', 2000.00, TRUE, 1),
    (2, '225487', 'CORRIENTE', 100.00, TRUE, 2),
    (3, '495878', 'AHORROS', 0.00, TRUE, 3),
    (4, '496825', 'AHORROS', 540.00, TRUE, 2)
ON DUPLICATE KEY UPDATE
    numero_cuenta = VALUES(numero_cuenta),
    tipo_cuenta = VALUES(tipo_cuenta),
    saldo_inicial = VALUES(saldo_inicial),
    estado = VALUES(estado),
    cliente_id = VALUES(cliente_id);

INSERT INTO movimientos (id, fecha, tipo_movimiento, valor, saldo, estado, cuenta_id)
VALUES
    (1, '2026-04-30 06:44:53.255000', 'RETIRO', -575.00, 1425.00, TRUE, 1),
    (2, '2026-04-30 09:15:00.000000', 'DEPOSITO', 600.00, 700.00, TRUE, 2),
    (3, '2026-04-30 11:30:00.000000', 'DEPOSITO', 150.00, 150.00, TRUE, 3),
    (4, '2026-04-30 13:41:05.000000', 'RETIRO', -540.00, 0.00, TRUE, 4)
ON DUPLICATE KEY UPDATE
    fecha = VALUES(fecha),
    tipo_movimiento = VALUES(tipo_movimiento),
    valor = VALUES(valor),
    saldo = VALUES(saldo),
    estado = VALUES(estado),
    cuenta_id = VALUES(cuenta_id);
