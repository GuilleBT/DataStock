package es.iesisidra.datastock.jdbc;

import es.iesisidra.datastock.modelo.Proveedor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Operaciones de acceso a datos para la tabla {@code proveedor} (RF-JDBC-02).
 */
public class ProveedorDAO {

    /**
     * Comprueba si ya existe un proveedor con el ID indicado.
     */
    public boolean existePorId(int id) throws SQLException {
        String sql = "SELECT 1 FROM proveedor WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Comprueba si ya existe un proveedor con el correo electrónico indicado.
     */
    public boolean existePorEmail(String email) throws SQLException {
        if (email == null || email.isBlank()) {
            return false;
        }
        String sql = "SELECT 1 FROM proveedor WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Comprueba si otro proveedor distinto a idActual tiene el mismo email.
     */
    public boolean existePorEmailExcluyendoId(String email, int idActual) throws SQLException {
        if (email == null || email.isBlank()) {
            return false;
        }
        String sql = "SELECT 1 FROM proveedor WHERE LOWER(email) = LOWER(?) AND id <> ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            ps.setInt(2, idActual);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Inserta un nuevo proveedor en la base de datos.
     */
    public boolean insertar(Proveedor proveedor) throws SQLException {
        String sql = "INSERT INTO proveedor (id, nombre, email) VALUES (?, ?, ?)";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, proveedor.getId());
            ps.setString(2, proveedor.getNombre().trim());
            if (proveedor.getEmail() != null && !proveedor.getEmail().isBlank()) {
                ps.setString(3, proveedor.getEmail().trim());
            } else {
                ps.setNull(3, Types.VARCHAR);
            }
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Recupera todos los proveedores ordenados por ID ascendente.
     */
    public List<Proveedor> listarTodos() throws SQLException {
        List<Proveedor> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, email FROM proveedor ORDER BY id ASC";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Proveedor(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getString("email")
                ));
            }
        }
        return lista;
    }

    /**
     * Busca un proveedor por su identificador.
     */
    public Proveedor buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, nombre, email FROM proveedor WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Proveedor(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("email")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Modifica el nombre y correo de un proveedor existente.
     */
    public boolean modificar(int id, String nuevoNombre, String nuevoEmail) throws SQLException {
        String sql = "UPDATE proveedor SET nombre = ?, email = ? WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuevoNombre.trim());
            if (nuevoEmail != null && !nuevoEmail.isBlank()) {
                ps.setString(2, nuevoEmail.trim());
            } else {
                ps.setNull(2, Types.VARCHAR);
            }
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Comprueba cuántos productos están asociados a un proveedor.
     */
    public int contarProductosAsociados(int proveedorId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM producto WHERE proveedor_id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, proveedorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Elimina un proveedor por su ID.
     */
    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM proveedor WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
