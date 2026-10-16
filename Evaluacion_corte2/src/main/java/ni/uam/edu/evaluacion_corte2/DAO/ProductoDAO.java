package ni.uam.edu.evaluacion_corte2.DAO;

import ni.uam.edu.evaluacion_corte2.database.ConexionBD;
import ni.uam.edu.evaluacion_corte2.model.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    public List<Producto> buscar(
            String texto,
            String categoria,
            boolean soloExistencias
    ) throws SQLException {

        List<Producto> productos = new ArrayList<>();

        String sql = """
                SELECT id_producto, codigo, nombre,
                       categoria, precio, stock
                FROM public.producto
                WHERE (
                    nombre ILIKE ? ESCAPE '!'
                    OR codigo ILIKE ? ESCAPE '!'
                )
                AND (? = 'Todas' OR categoria = ?)
                AND (? = FALSE OR stock > 0)
                ORDER BY codigo ASC
                """;

        String textoBusqueda = texto == null ? "" : texto.trim();

        // Hace que %, _ y ! se busquen como caracteres literales.
        textoBusqueda = textoBusqueda
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");

        String patron = "%" + textoBusqueda + "%";

        String categoriaFiltro =
                categoria == null || categoria.isBlank()
                        ? "Todas"
                        : categoria;

        try (
                Connection conexion = ConexionBD.getConexion();
                PreparedStatement sentencia =
                        conexion.prepareStatement(sql)
        ) {
            sentencia.setString(1, patron);
            sentencia.setString(2, patron);
            sentencia.setString(3, categoriaFiltro);
            sentencia.setString(4, categoriaFiltro);
            sentencia.setBoolean(5, soloExistencias);

            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    Producto producto = new Producto(
                            resultado.getInt("id_producto"),
                            resultado.getString("codigo"),
                            resultado.getString("nombre"),
                            resultado.getString("categoria"),
                            resultado.getBigDecimal("precio"),
                            resultado.getInt("stock")
                    );

                    productos.add(producto);
                }
            }
        }

        return productos;
    }

    // El ID lo genera PostgreSQL automáticamente.
    public int insertar(Producto producto) throws SQLException {
        String sql = """
                INSERT INTO public.producto
                    (codigo, nombre, categoria, precio, stock)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (
                Connection conexion = ConexionBD.getConexion();
                PreparedStatement sentencia =
                        conexion.prepareStatement(sql)
        ) {
            sentencia.setString(1, producto.getCodigo());
            sentencia.setString(2, producto.getNombre());
            sentencia.setString(3, producto.getCategoria());
            sentencia.setBigDecimal(4, producto.getPrecio());
            sentencia.setInt(5, producto.getStock());

            return sentencia.executeUpdate();
        }
    }

    // Modifica exclusivamente el producto identificado por su ID.
    public int actualizar(Producto producto) throws SQLException {
        String sql = """
                UPDATE public.producto
                SET codigo = ?,
                    nombre = ?,
                    categoria = ?,
                    precio = ?,
                    stock = ?
                WHERE id_producto = ?
                """;

        try (
                Connection conexion = ConexionBD.getConexion();
                PreparedStatement sentencia =
                        conexion.prepareStatement(sql)
        ) {
            sentencia.setString(1, producto.getCodigo());
            sentencia.setString(2, producto.getNombre());
            sentencia.setString(3, producto.getCategoria());
            sentencia.setBigDecimal(4, producto.getPrecio());
            sentencia.setInt(5, producto.getStock());
            sentencia.setInt(6, producto.getIdProducto());

            return sentencia.executeUpdate();
        }
    }

    // Elimina exclusivamente el producto seleccionado.
    public int eliminar(int idProducto) throws SQLException {
        String sql = """
                DELETE FROM public.producto
                WHERE id_producto = ?
                """;

        try (
                Connection conexion = ConexionBD.getConexion();
                PreparedStatement sentencia =
                        conexion.prepareStatement(sql)
        ) {
            sentencia.setInt(1, idProducto);

            return sentencia.executeUpdate();
        }
    }
}