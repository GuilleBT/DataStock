package es.iesisidra.datastock.jdbc;

import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.modelo.ProductoDetalleDTO;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Operaciones de acceso a datos para la tabla {@code producto} (RF-JDBC-03 y RF-JDBC-04).
 */
public class ProductoDAO {

    /**
     * Comprueba si ya existe un producto con el código indicado.
     */
    public boolean existePorCodigo(String codigo) throws SQLException {
        if (codigo == null || codigo.isBlank()) return false;
        String sql = "SELECT 1 FROM producto WHERE codigo = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigo.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Inserta un nuevo producto en MySQL utilizando PreparedStatement y BigDecimal.
     */
    public boolean insertar(Producto producto) throws SQLException {
        String sql = "INSERT INTO producto (codigo, nombre, precio, stock, categoria_id, proveedor_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, producto.getCodigo().trim().toUpperCase());
            ps.setString(2, producto.getNombre().trim());
            ps.setBigDecimal(3, producto.getPrecio());
            ps.setInt(4, producto.getStock());
            ps.setInt(5, producto.getCategoriaId());
            ps.setInt(6, producto.getProveedorId());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Inserta un producto dentro de una conexión y transacción existente.
     */
    public boolean insertarEnTransaccion(Connection conn, Producto producto) throws SQLException {
        String sql = "INSERT INTO producto (codigo, nombre, precio, stock, categoria_id, proveedor_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, producto.getCodigo().trim().toUpperCase());
            ps.setString(2, producto.getNombre().trim());
            ps.setBigDecimal(3, producto.getPrecio());
            ps.setInt(4, producto.getStock());
            ps.setInt(5, producto.getCategoriaId());
            ps.setInt(6, producto.getProveedorId());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Consulta un producto por su código y obtiene los datos combinados con JOIN.
     */
    public ProductoDetalleDTO buscarPorCodigo(String codigo) throws SQLException {
        if (codigo == null || codigo.isBlank()) return null;
        String sql = "SELECT p.codigo, p.nombre, p.precio, p.stock, " +
                     "p.categoria_id, c.nombre AS categoria_nombre, " +
                     "p.proveedor_id, pr.nombre AS proveedor_nombre " +
                     "FROM producto p " +
                     "JOIN categoria c ON p.categoria_id = c.id " +
                     "JOIN proveedor pr ON p.proveedor_id = pr.id " +
                     "WHERE p.codigo = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigo.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearProductoDetalle(rs);
                }
            }
        }
        return null;
    }

    /**
     * Modifica los datos descriptivos de un producto (nombre, precio, categoría y proveedor).
     * No permite modificar directamente el código ni el stock.
     */
    public boolean modificar(String codigo, String nuevoNombre, BigDecimal nuevoPrecio,
                             int nuevaCategoriaId, int nuevoProveedorId) throws SQLException {
        String sql = "UPDATE producto SET nombre = ?, precio = ?, categoria_id = ?, proveedor_id = ? WHERE codigo = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuevoNombre.trim());
            ps.setBigDecimal(2, nuevoPrecio);
            ps.setInt(3, nuevaCategoriaId);
            ps.setInt(4, nuevoProveedorId);
            ps.setString(5, codigo.trim().toUpperCase());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Comprueba si un producto tiene movimientos históricos registrados en movimiento_stock.
     */
    public int contarMovimientos(String codigo) throws SQLException {
        String sql = "SELECT COUNT(*) FROM movimiento_stock WHERE producto_codigo = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigo.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Elimina un producto por su código.
     */
    public boolean eliminar(String codigo) throws SQLException {
        String sql = "DELETE FROM producto WHERE codigo = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigo.trim().toUpperCase());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Lista todos los productos ordenados por código con JOIN a categoría y proveedor.
     */
    public List<ProductoDetalleDTO> listarTodos() throws SQLException {
        List<ProductoDetalleDTO> lista = new ArrayList<>();
        String sql = "SELECT p.codigo, p.nombre, p.precio, p.stock, " +
                     "p.categoria_id, c.nombre AS categoria_nombre, " +
                     "p.proveedor_id, pr.nombre AS proveedor_nombre " +
                     "FROM producto p " +
                     "JOIN categoria c ON p.categoria_id = c.id " +
                     "JOIN proveedor pr ON p.proveedor_id = pr.id " +
                     "ORDER BY p.codigo ASC";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearProductoDetalle(rs));
            }
        }
        return lista;
    }

    /**
     * Busca productos por coincidencia parcial en el nombre mediante LIKE sensible a colación.
     */
    public List<ProductoDetalleDTO> buscarPorNombre(String fragmento) throws SQLException {
        List<ProductoDetalleDTO> lista = new ArrayList<>();
        String sql = "SELECT p.codigo, p.nombre, p.precio, p.stock, " +
                     "p.categoria_id, c.nombre AS categoria_nombre, " +
                     "p.proveedor_id, pr.nombre AS proveedor_nombre " +
                     "FROM producto p " +
                     "JOIN categoria c ON p.categoria_id = c.id " +
                     "JOIN proveedor pr ON p.proveedor_id = pr.id " +
                     "WHERE LOWER(p.nombre) LIKE LOWER(?) " +
                     "ORDER BY p.codigo ASC";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + fragmento.trim() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearProductoDetalle(rs));
                }
            }
        }
        return lista;
    }

    /**
     * Busca productos con existencias estrictamente inferiores a un umbral (stock < umbral).
     * Ordenado primero por stock ascendente y después por código.
     */
    public List<ProductoDetalleDTO> buscarPorStockInferior(int umbral) throws SQLException {
        List<ProductoDetalleDTO> lista = new ArrayList<>();
        String sql = "SELECT p.codigo, p.nombre, p.precio, p.stock, " +
                     "p.categoria_id, c.nombre AS categoria_nombre, " +
                     "p.proveedor_id, pr.nombre AS proveedor_nombre " +
                     "FROM producto p " +
                     "JOIN categoria c ON p.categoria_id = c.id " +
                     "JOIN proveedor pr ON p.proveedor_id = pr.id " +
                     "WHERE p.stock < ? " +
                     "ORDER BY p.stock ASC, p.codigo ASC";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, umbral);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearProductoDetalle(rs));
                }
            }
        }
        return lista;
    }

    private ProductoDetalleDTO mapearProductoDetalle(ResultSet rs) throws SQLException {
        return new ProductoDetalleDTO(
            rs.getString("codigo"),
            rs.getString("nombre"),
            rs.getBigDecimal("precio"),
            rs.getInt("stock"),
            rs.getInt("categoria_id"),
            rs.getString("categoria_nombre"),
            rs.getInt("proveedor_id"),
            rs.getString("proveedor_nombre")
        );
    }
}
