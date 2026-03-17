PRAGMA foreign_keys = ON;

-- Usuarios de ejemplo
INSERT OR IGNORE INTO usuarios (id, nombre_completo, email)
VALUES
  (1, 'Juan Perez', 'juan.perez@example.com'),
  (2, 'Maria Garcia', 'maria.garcia@example.com'),
  (3, 'Carlos Lopez', 'carlos.lopez@example.com');

-- Productos iniciales (coinciden con el EnumType.STRING de categoria)
INSERT INTO productos (id, nombre, categoria, marca, stock, descripcion, precio, image_path)
VALUES
  (1, 'Chronos Edge', 'RELOJ', 'GoldenLine', 15, 'Reloj de acero pulido con correa intercambiable y detalles en oro cepillado.', 420.00, '/resources/productos/1'),
  (2, 'Lunar Velvet', 'OTROS', 'GoldenLine', 8, 'Bolso monocromático en terciopelo con acabados metálicos dorados.', 265.00, '/resources/productos/2'),
  (3, 'Auric Steps', 'OTROS', 'GoldenLine', 12, 'Calzado premium en cuero negro con inserciones metálicas doradas.', 310.00, '/resources/productos/3'),
  (4, 'Gilded Harmony', 'COLLAR', 'GoldenLine', 20, 'Set de accesorios en acero ennegrecido con detalles bañados en oro.', 195.00, '/resources/productos/4')
ON CONFLICT(id) DO UPDATE SET
  nombre=excluded.nombre,
  categoria=excluded.categoria,
  marca=excluded.marca,
  stock=excluded.stock,
  descripcion=excluded.descripcion,
  precio=excluded.precio,
  image_path=excluded.image_path;
