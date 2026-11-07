package es.iesisidra.datastock.jdbc;

import es.iesisidra.datastock.modelo.Categoria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Operaciones de acceso a datos para la tabla {@code categoria} (RF-JDBC-02).
 */
public class CategoriaDAO {

    /**
     * Comprueba si ya existe una categoría con el ID indicado.
     */
    public boolean existePorId(int id) throws SQLException {
        String sql = "SELECT 1 FROM categoria WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Comprueba si ya existe una categoría con el nombre indicado.
     */
    public boolean existePorNombre(String nombre) throws SQLException {
        String sql = "SELECT 1 FROM categoria WHERE LOWER(nombre) = LOWER(?)";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Comprueba si otro registro diferente a idActual tiene el mismo nombre.
     */
    public boolean existePorNombreExcluyendoId(String nombre, int idActual) throws SQLException {
        String sql = "SELECT 1 FROM categoria WHERE LOWER(nombre) = LOWER(?) AND id <> ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre.trim());
            ps.setInt(2, idActual);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Inserta una nueva categoría en la base de datos.
     */
    public boolean insertar(Categoria categoria) throws SQLException {
        String sql = "INSERT INTO categoria (id, nombre) VALUES (?, ?)";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoria.getId());
            ps.setString(2, categoria.getNombre().trim());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Recupera todas las categorías ordenadas por ID ascendente.
     */
    public List<Categoria> listarTodas() throws SQLException {
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT id, nombre FROM categoria ORDER BY id ASC";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Categoria(rs.getInt("id"), rs.getString("nombre")));
            }
        }
        return lista;
    }

    /**
     * Busca una categoría por su identificador.
     */
    public Categoria buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, nombre FROM categoria WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Categoria(rs.getInt("id"), rs.getString("nombre"));
                }
            }
        }
        return null;
    }

    /**
     * Modifica el nombre de una categoría existente.
     */
    public boolean modificarNombre(int id, String nuevoNombre) throws SQLException {
        String sql = "UPDATE categoria SET nombre = ? WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuevoNombre.trim());
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Comprueba cuántos productos están asociados a una categoría.
     */
    public int contarProductosAsociados(int categoriaId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM producto WHERE categoria_id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoriaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Elimina una categoría por su ID.
     */
    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM categoria WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
