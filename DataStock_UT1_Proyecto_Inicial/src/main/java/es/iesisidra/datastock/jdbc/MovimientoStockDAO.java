package es.iesisidra.datastock.jdbc;

import es.iesisidra.datastock.modelo.MovimientoStock;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos y llamadas a procedimientos almacenados para movimientos de stock
 * (RF-JDBC-05 y RF-JDBC-06).
 */
public class MovimientoStockDAO {

    /**
     * DTO para el resultado devuelto tras invocar al procedimiento almacenado.
     */
    public record ResultadoMovimiento(
        long movimientoId,
        String codigo,
        String tipo,
        int cantidad,
        int stockAnterior,
        int stockPosterior,
        LocalDateTime fecha
    ) { }

    /**
     * Invoca al procedimiento almacenado {@code registrar_movimiento(?, ?, ?)} mediante
     * {@link CallableStatement}. Gestiona la transacción atómica delegada en MySQL.
     *
     * @param codigo   código del producto en mayúsculas.
     * @param tipo     'ENTRADA' o 'SALIDA'.
     * @param cantidad unidades enteras estrictamente positivas.
     * @return {@link ResultadoMovimiento} con los datos consolidados.
     * @throws SQLException si MySQL lanza un error (stock insuficiente, producto inexistente, etc.).
     */
    public ResultadoMovimiento registrarMovimiento(String codigo, String tipo, int cantidad) throws SQLException {
        String callSql = "{CALL registrar_movimiento(?, ?, ?)}";
        String selectSql = "SELECT id, fecha, stock_anterior, stock_posterior FROM movimiento_stock " +
                           "WHERE producto_codigo = ? ORDER BY id DESC LIMIT 1";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            // 1. Invocación al procedimiento almacenado
            try (CallableStatement cs = conn.prepareCall(callSql)) {
                cs.setString(1, codigo.trim().toUpperCase());
                cs.setString(2, tipo.trim().toUpperCase());
                cs.setInt(3, cantidad);
                cs.execute();
            }

            // 2. Consulta del último movimiento generado para este producto
            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                ps.setString(1, codigo.trim().toUpperCase());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        long id = rs.getLong("id");
                        Timestamp ts = rs.getTimestamp("fecha");
                        LocalDateTime fecha = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
                        int stockAnt = rs.getInt("stock_anterior");
                        int stockPost = rs.getInt("stock_posterior");
                        return new ResultadoMovimiento(id, codigo, tipo, cantidad, stockAnt, stockPost, fecha);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Recupera todos los movimientos registrados ordenados de forma estable por fecha e ID.
     */
    public List<MovimientoStock> consultarTodos() throws SQLException {
        List<MovimientoStock> lista = new ArrayList<>();
        String sql = "SELECT id, producto_codigo, tipo, cantidad, fecha, stock_anterior, stock_posterior " +
                     "FROM movimiento_stock ORDER BY fecha ASC, id ASC";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearMovimiento(rs));
            }
        }
        return lista;
    }

    /**
     * Recupera los movimientos registrados para un producto concreto ordenados por fecha e ID.
     */
    public List<MovimientoStock> consultarPorProducto(String codigo) throws SQLException {
        List<MovimientoStock> lista = new ArrayList<>();
        String sql = "SELECT id, producto_codigo, tipo, cantidad, fecha, stock_anterior, stock_posterior " +
                     "FROM movimiento_stock WHERE producto_codigo = ? ORDER BY fecha ASC, id ASC";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigo.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearMovimiento(rs));
                }
            }
        }
        return lista;
    }

    private MovimientoStock mapearMovimiento(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String cod = rs.getString("producto_codigo");
        String strTipo = rs.getString("tipo");
        MovimientoStock.TipoMovimiento tipo = MovimientoStock.TipoMovimiento.valueOf(strTipo);
        int cantidad = rs.getInt("cantidad");
        Timestamp ts = rs.getTimestamp("fecha");
        LocalDateTime fecha = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
        int stockAnt = rs.getInt("stock_anterior");
        int stockPost = rs.getInt("stock_posterior");

        return new MovimientoStock(id, cod, tipo, cantidad, fecha, stockAnt, stockPost);
    }
}
