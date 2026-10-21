-- Datos iniciales: usuarios, proveedores (ficticios), productos y stock inicial.
USE tiendita_don_pepe;

-- Contraseñas (PBKDF2-SHA256): admin -> Admin2026 | cajero -> Cajero2026
INSERT INTO usuario (id_usuario, nombre_completo, usuario_login, contrasena_hash, rol, estado) VALUES
  (1, 'José Pérez Quispe', 'admin', 'pbkdf2_sha256$100000$Sdsiol/6n5Gvjgm44/t/+w==$Qz7UBeBPlq3SCKNM1wpkASyTqMbGhhJxo2S2v3ZJnzs=', 'Administrador', 'Activo'),
  (2, 'Lucía Ramos Huamán', 'cajero', 'pbkdf2_sha256$100000$SjqKCwMvaRjGDea42JiSgA==$A7eCNkHaHu7p8Qrnyb2E9N/WErkD5f9N3vx6+KcxP4Q=', 'Cajero', 'Activo');

-- Proveedores ficticios (no representan empresas reales)
INSERT INTO proveedor (id_proveedor, razon_social, ruc, telefono, correo, direccion, estado) VALUES
  ('P001', 'Pesquera El Ancla S.A.C.', '20111222333', '987111222', 'pedidos@elancla-ficticio.pe', 'Av. Los Faisanes 340, San Martín de Porres', 'Activo'),
  ('P002', 'Distribuidora Costa Azul E.I.R.L.', '20444555666', '912333444', 'ventas@costaazul-ficticio.pe', 'Jr. Las Gaviotas 118, San Martín de Porres', 'Activo'),
  ('P003', 'Comercial Litoral Perú SAC', '20777888999', '998555666', 'contacto@litoralperu-ficticio.pe', 'Av. Los Pescadores 205, Independencia', 'Inactivo');

-- Productos: el sistema administra únicamente atún enlatado (3 presentaciones)
INSERT INTO producto (id_producto, nombre, marca, presentacion, precio_venta, stock_actual, stock_minimo, fecha_vencimiento, estado, id_proveedor) VALUES
  (1, 'Atún en agua', 'Costa Dorada', 'Lata 170 g', 4.50, 64, 20, DATE_ADD(CURDATE(), INTERVAL 380 DAY), 'Activo', 'P001'),
  (2, 'Atún en aceite', 'Costa Dorada', 'Lata 170 g', 4.80, 76, 20, DATE_ADD(CURDATE(), INTERVAL 38 DAY), 'Activo', 'P002'),
  (3, 'Atún en aceite', 'Mar Azul', 'Lata 140 g', 3.90, 52, 15, DATE_ADD(CURDATE(), INTERVAL 210 DAY), 'Activo', 'P001');

-- Clientes frecuentes
INSERT INTO cliente (id_cliente, nombre_completo, dni, ruc, telefono, direccion, fecha_registro) VALUES
  (1, 'María Torres Quispe', '45123789', '10451237891', '945112233', 'Jr. Amazonas 145, San Martín de Porres', DATE_SUB(CURDATE(), INTERVAL 60 DAY)),
  (2, 'Jorge Villanueva Cano', '41876532', '20555666777', '998877665', 'Av. Universitaria 2201, San Martín de Porres', DATE_SUB(CURDATE(), INTERVAL 45 DAY)),
  (3, 'Rosa Huamán Effio', '43219876', NULL, '987001122', 'Calle Las Flores 90, San Martín de Porres', DATE_SUB(CURDATE(), INTERVAL 20 DAY));

-- Stock inicial (ingreso de apertura de inventario registrado por el administrador)
INSERT INTO movimiento_inventario (fecha_hora, tipo_movimiento, id_producto, cantidad, saldo, detalle, id_usuario, id_venta, id_proveedor) VALUES
  (TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 21 DAY), '08:31:00'), 'Ingreso', 1, 64, 64, 'Stock inicial – compra a Pesquera El Ancla S.A.C.', 1, NULL, 'P001'),
  (TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 21 DAY), '08:32:00'), 'Ingreso', 2, 76, 76, 'Stock inicial – compra a Distribuidora Costa Azul E.I.R.L.', 1, NULL, 'P002'),
  (TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 21 DAY), '08:33:00'), 'Ingreso', 3, 52, 52, 'Stock inicial – compra a Pesquera El Ancla S.A.C.', 1, NULL, 'P001');
