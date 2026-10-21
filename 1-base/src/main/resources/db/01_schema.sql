

CREATE DATABASE IF NOT EXISTS tiendita_don_pepe
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE tiendita_don_pepe;

-- Tabla 21 — USUARIO ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuario (
    id_usuario       INT          NOT NULL AUTO_INCREMENT,
    nombre_completo  VARCHAR(100) NOT NULL,
    usuario_login    VARCHAR(30)  NOT NULL,
    contrasena_hash  VARCHAR(255) NOT NULL,
    rol              VARCHAR(20)  NOT NULL,
    estado           VARCHAR(10)  NOT NULL DEFAULT 'Activo',
    PRIMARY KEY (id_usuario),
    UNIQUE KEY uq_usuario_login (usuario_login),
    CONSTRAINT chk_usuario_rol    CHECK (rol IN ('Administrador', 'Cajero')),
    CONSTRAINT chk_usuario_estado CHECK (estado IN ('Activo', 'Inactivo'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla 22 — CLIENTE ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS cliente (
    id_cliente       INT          NOT NULL AUTO_INCREMENT,
    nombre_completo  VARCHAR(100) NOT NULL,
    dni              VARCHAR(8)   NULL,           -- opcional; sirve para autocompletar el comprobante simple
    ruc              VARCHAR(11)  NULL,           -- opcional; obligatorio para emitir factura completa
    telefono         VARCHAR(20)  NULL,
    direccion        VARCHAR(150) NULL,
    fecha_registro   DATE         NOT NULL DEFAULT (CURRENT_DATE),
    PRIMARY KEY (id_cliente),
    UNIQUE KEY uk_cliente_dni (dni),
    UNIQUE KEY uk_cliente_ruc (ruc),
    CONSTRAINT chk_cliente_dni CHECK (dni IS NULL OR dni REGEXP '^[0-9]{8}$'),
    CONSTRAINT chk_cliente_ruc CHECK (ruc IS NULL OR ruc REGEXP '^[0-9]{11}$')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla 23 — PROVEEDOR -------------------------------------------------------
CREATE TABLE IF NOT EXISTS proveedor (
    id_proveedor     VARCHAR(5)   NOT NULL,
    razon_social     VARCHAR(120) NOT NULL,
    ruc              VARCHAR(11)  NOT NULL,
    telefono         VARCHAR(20)  NULL,
    correo           VARCHAR(100) NULL,
    direccion        VARCHAR(150) NULL,
    estado           VARCHAR(10)  NOT NULL DEFAULT 'Activo',
    PRIMARY KEY (id_proveedor),
    UNIQUE KEY uq_proveedor_ruc (ruc),
    CONSTRAINT chk_proveedor_estado CHECK (estado IN ('Activo', 'Inactivo'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla 24 — PRODUCTO --------------------------------------------------------
CREATE TABLE IF NOT EXISTS producto (
    id_producto        INT           NOT NULL AUTO_INCREMENT,
    nombre             VARCHAR(80)   NOT NULL,
    marca              VARCHAR(50)   NOT NULL,
    presentacion       VARCHAR(40)   NOT NULL,
    precio_venta       DECIMAL(8,2)  NOT NULL,
    stock_actual       INT           NOT NULL DEFAULT 0,
    stock_minimo       INT           NOT NULL DEFAULT 0,
    fecha_vencimiento  DATE          NOT NULL,
    estado             VARCHAR(10)   NOT NULL DEFAULT 'Activo',
    id_proveedor       VARCHAR(5)    NOT NULL,
    PRIMARY KEY (id_producto),
    UNIQUE KEY uq_producto (nombre, marca, presentacion),
    KEY idx_producto_proveedor (id_proveedor),
    CONSTRAINT fk_producto_proveedor FOREIGN KEY (id_proveedor) REFERENCES proveedor (id_proveedor),
    CONSTRAINT chk_producto_precio CHECK (precio_venta >= 0),
    CONSTRAINT chk_producto_stock  CHECK (stock_actual >= 0),
    CONSTRAINT chk_producto_minimo CHECK (stock_minimo >= 0),
    CONSTRAINT chk_producto_estado CHECK (estado IN ('Activo', 'Inactivo'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla 25 — VENTA (el id_venta es el número de comprobante: inicia en 1001) --
CREATE TABLE IF NOT EXISTS venta (
    id_venta           INT           NOT NULL AUTO_INCREMENT,
    fecha_hora         DATETIME      NOT NULL,
    id_cliente         INT           NULL,
    id_usuario         INT           NOT NULL,
    tipo_comprobante   VARCHAR(20)   NOT NULL,   -- el diccionario indica VARCHAR(15), pero "Factura completa" tiene 16 caracteres
    forma_pago         VARCHAR(15)   NOT NULL,
    dni_ruc            VARCHAR(11)   NULL,
    direccion_entrega  VARCHAR(150)  NULL,
    subtotal           DECIMAL(8,2)  NOT NULL,
    igv                DECIMAL(8,2)  NOT NULL DEFAULT 0,
    total              DECIMAL(8,2)  NOT NULL,
    estado             VARCHAR(15)   NOT NULL DEFAULT 'Borrador',
    PRIMARY KEY (id_venta),
    KEY idx_venta_fecha (fecha_hora),
    KEY idx_venta_cliente (id_cliente),
    KEY idx_venta_usuario (id_usuario),
    CONSTRAINT fk_venta_cliente FOREIGN KEY (id_cliente) REFERENCES cliente (id_cliente),
    CONSTRAINT fk_venta_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario),
    CONSTRAINT chk_venta_tipo   CHECK (tipo_comprobante IN ('Simple', 'Factura completa')),
    CONSTRAINT chk_venta_pago   CHECK (forma_pago IN ('Efectivo', 'Yape-Plin', 'Tarjeta')),
    CONSTRAINT chk_venta_estado CHECK (estado IN ('Borrador', 'Emitida', 'Anulada')),
    CONSTRAINT chk_venta_total  CHECK (total >= 0)
) ENGINE=InnoDB AUTO_INCREMENT=1001 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla 26 — DETALLE_VENTA (composición: se elimina junto con la venta) -------
CREATE TABLE IF NOT EXISTS detalle_venta (
    id_detalle       INT           NOT NULL AUTO_INCREMENT,
    id_venta         INT           NOT NULL,
    id_producto      INT           NOT NULL,
    cantidad         INT           NOT NULL,
    precio_unitario  DECIMAL(8,2)  NOT NULL,
    subtotal         DECIMAL(8,2)  NOT NULL,
    PRIMARY KEY (id_detalle),
    KEY idx_detalle_venta (id_venta),
    KEY idx_detalle_producto (id_producto),
    CONSTRAINT fk_detalle_venta    FOREIGN KEY (id_venta)    REFERENCES venta (id_venta) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_producto FOREIGN KEY (id_producto) REFERENCES producto (id_producto),
    CONSTRAINT chk_detalle_cantidad CHECK (cantidad > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla 27 — MOVIMIENTO_INVENTARIO -------------------------------------------
CREATE TABLE IF NOT EXISTS movimiento_inventario (
    id_movimiento    INT           NOT NULL AUTO_INCREMENT,
    fecha_hora       DATETIME      NOT NULL,
    tipo_movimiento  VARCHAR(10)   NOT NULL,
    id_producto      INT           NOT NULL,
    cantidad         INT           NOT NULL,
    saldo            INT           NOT NULL,
    detalle          VARCHAR(150)  NULL,
    id_usuario       INT           NOT NULL,
    id_venta         INT           NULL,
    id_proveedor     VARCHAR(5)    NULL,
    PRIMARY KEY (id_movimiento),
    KEY idx_mov_fecha (fecha_hora),
    KEY idx_mov_producto (id_producto),
    KEY idx_mov_usuario (id_usuario),
    KEY idx_mov_venta (id_venta),
    KEY idx_mov_proveedor (id_proveedor),
    CONSTRAINT fk_mov_producto  FOREIGN KEY (id_producto)  REFERENCES producto (id_producto),
    CONSTRAINT fk_mov_usuario   FOREIGN KEY (id_usuario)   REFERENCES usuario (id_usuario),
    CONSTRAINT fk_mov_venta     FOREIGN KEY (id_venta)     REFERENCES venta (id_venta),
    CONSTRAINT fk_mov_proveedor FOREIGN KEY (id_proveedor) REFERENCES proveedor (id_proveedor),
    CONSTRAINT chk_mov_tipo     CHECK (tipo_movimiento IN ('Ingreso', 'Salida')),
    CONSTRAINT chk_mov_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_mov_saldo    CHECK (saldo >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
