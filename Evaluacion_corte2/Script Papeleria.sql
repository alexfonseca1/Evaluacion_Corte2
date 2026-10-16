CREATE TABLE producto (
    id_producto INTEGER GENERATED ALWAYS AS IDENTITY,
    codigo      VARCHAR(10) NOT NULL,
    nombre      VARCHAR(80) NOT NULL,
    categoria   VARCHAR(20) NOT NULL,
    precio      NUMERIC(10,2) NOT NULL,
    stock       INTEGER NOT NULL,

    CONSTRAINT pk_producto
        PRIMARY KEY (id_producto),

    CONSTRAINT uq_producto_codigo
        UNIQUE (codigo),

    -- Código no vacío, sin espacios laterales y en mayúsculas.
    CONSTRAINT chk_producto_codigo
        CHECK (
            CHAR_LENGTH(codigo) BETWEEN 1 AND 10
            AND codigo = BTRIM(codigo)
            AND codigo = UPPER(codigo)
        ),

    -- Nombre sin espacios laterales, de 3 a 80 caracteres.
    CONSTRAINT chk_producto_nombre
        CHECK (
            CHAR_LENGTH(nombre) BETWEEN 3 AND 80
            AND nombre = BTRIM(nombre)
        ),

    -- Solo se permiten estas tres categorías.
    CONSTRAINT chk_producto_categoria
        CHECK (
            categoria IN ('Papelería', 'Tecnología', 'Accesorios')
        ),

    -- Precio dentro del rango permitido.
    CONSTRAINT chk_producto_precio
        CHECK (
            precio BETWEEN 0.01 AND 99999999.99
        ),

    -- Se permite stock cero, pero no negativo.
    CONSTRAINT chk_producto_stock
        CHECK (stock >= 0)
);


INSERT INTO producto (codigo, nombre, categoria, precio, stock)
VALUES
    ('P001', 'Cuaderno universitario', 'Papelería', 65.00, 25),
    ('P002', 'Cuaderno cuadriculado', 'Papelería', 72.50, 12),
    ('P003', 'Lapicero azul', 'Papelería', 12.00, 80),
    ('P004', 'Resma de papel carta', 'Papelería', 185.00, 0),

    ('T001', 'Memoria USB 32 GB', 'Tecnología', 210.00, 8),
    ('T002', 'Mouse inalámbrico', 'Tecnología', 320.00, 6),
    ('T003', 'Teclado USB', 'Tecnología', 450.00, 4),
    ('T004', 'Cable HDMI', 'Tecnología', 180.00, 0),

    ('A001', 'Mochila escolar', 'Accesorios', 850.00, 5),
    ('A002', 'Estuche de lápices', 'Accesorios', 95.00, 18),
    ('A003', 'Botella reutilizable', 'Accesorios', 240.00, 0),
    ('A004', 'Regla metálica', 'Accesorios', 45.00, 20);

SELECT *
FROM producto
ORDER BY codigo ASC;

-- Debe mostrar papeleria_campus y un total de 12.
SELECT
    CURRENT_DATABASE() AS base_de_datos,
    COUNT(*) AS total_productos
FROM producto;