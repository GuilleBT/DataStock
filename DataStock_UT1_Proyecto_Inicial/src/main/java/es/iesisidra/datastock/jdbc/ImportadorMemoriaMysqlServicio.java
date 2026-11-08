package es.iesisidra.datastock.jdbc;

import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.servicio.Inventario;
import es.iesisidra.datastock.util.ColoresConsola;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de importación controlada por lotes desde el inventario en memoria
 * (UT1) hacia la base de datos MySQL con soporte transaccional (RF-JDBC-07).
 */
public class ImportadorMemoriaMysqlServicio {

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final ProveedorDAO proveedorDAO = new ProveedorDAO();

    /**
     * DTO con el resultado estadístico de la importación.
     */
    public record ResumenImportacion(
        int analizados,
        int insertados,
        int omitidosDuplicados,
        int rechazadosErrores,
        boolean exitoTransaccion
    ) { }

    /**
     * Ejecuta el proceso de importación por lotes desde memoria a MySQL.
     *
     * @param inventario inventario en memoria de la aplicación.
     * @return resumen con los totales del proceso.
     */
    public ResumenImportacion importar(Inventario inventario) {
        List<Producto> productosMemoria = inventario.getProductos();
        int total = productosMemoria.size();
        int omitidos = 0;
        int rechazados = 0;

        List<Producto> validosParaInsertar = new ArrayList<>();

        System.out.println("Iniciando comprobación de productos...");
        for (Producto p : productosMemoria) {
            String codigo = p.getCodigo() != null ? p.getCodigo().trim().toUpperCase() : "";
            System.out.print("Comprobando " + codigo + (p.getNombre() != null ? " - " + p.getNombre() : "") + "... ");

            // 1. Validaciones individuales de datos
            if (codigo.isEmpty() || !codigo.matches("^[A-Z0-9-]{1,12}$")) {
                System.out.println(ColoresConsola.pintar("[ERROR] Código '" + codigo + "' inválido. Rechazado.", ColoresConsola.ROJO));
                rechazados++;
                continue;
            }

            String nombre = p.getNombre() != null ? p.getNombre().trim() : "";
            if (nombre.length() < 3 || nombre.length() > 100) {
                System.out.println(ColoresConsola.pintar("[ERROR] Longitud de nombre incorrecta (3-100). Rechazado.", ColoresConsola.ROJO));
                rechazados++;
                continue;
            }

            BigDecimal precio = p.getPrecio();
            if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0 || precio.scale() > 2) {
                System.out.println(ColoresConsola.pintar("[ERROR] Precio inválido o con más de 2 decimales. Rechazado.", ColoresConsola.ROJO));
                rechazados++;
                continue;
            }

            if (p.getStock() < 0) {
                System.out.println(ColoresConsola.pintar("[ERROR] Stock negativo. Rechazado.", ColoresConsola.ROJO));
                rechazados++;
                continue;
            }

            // 2. Comprobación en base de datos MySQL
            try {
                if (productoDAO.existePorCodigo(codigo)) {
                    System.out.println(ColoresConsola.pintar("[AVISO] El producto ya existe en MySQL. Se omite.", ColoresConsola.AMARILLO));
                    omitidos++;
                    continue;
                }

                if (!categoriaDAO.existePorId(p.getCategoriaId())) {
                    System.out.println(ColoresConsola.pintar("[ERROR] No existe la categoría con ID " + p.getCategoriaId() + ". Producto rechazado.", ColoresConsola.ROJO));
                    rechazados++;
                    continue;
                }

                if (!proveedorDAO.existePorId(p.getProveedorId())) {
                    System.out.println(ColoresConsola.pintar("[ERROR] No existe el proveedor con ID " + p.getProveedorId() + ". Producto rechazado.", ColoresConsola.ROJO));
                    rechazados++;
                    continue;
                }

                // Superó todas las validaciones
                System.out.println(ColoresConsola.pintar("[OK] Producto válido para importar.", ColoresConsola.VERDE));
                validosParaInsertar.add(new Producto(codigo, nombre, precio, p.getStock(), p.getCategoriaId(), p.getProveedorId()));

            } catch (SQLException ex) {
                System.out.println(ColoresConsola.pintar("[ERROR] Error consultando restricciones: " + ex.getMessage(), ColoresConsola.ROJO));
                rechazados++;
            }
        }

        // 3. Ejecución transaccional del lote de productos válidos
        int insertados = 0;
        boolean exitoTx = true;

        if (!validosParaInsertar.isEmpty()) {
            String insertSql = "INSERT INTO producto (codigo, nombre, precio, stock, categoria_id, proveedor_id) VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection conn = ConexionBD.obtenerConexion()) {
                conn.setAutoCommit(false); // Transacción manual

                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    for (Producto prod : validosParaInsertar) {
                        ps.setString(1, prod.getCodigo());
                        ps.setString(2, prod.getNombre());
                        ps.setBigDecimal(3, prod.getPrecio());
                        ps.setInt(4, prod.getStock());
                        ps.setInt(5, prod.getCategoriaId());
                        ps.setInt(6, prod.getProveedorId());
                        ps.executeUpdate();
                        insertados++;
                    }
                    conn.commit(); // Confirmar todas las inserciones del lote
                } catch (SQLException sqlEx) {
                    conn.rollback(); // Deshacer el lote ante fallo inesperado
                    exitoTx = false;
                    insertados = 0;
                    System.out.println(ColoresConsola.pintar("[ERROR] Fallo durante la inserción del lote. Transacción revertida (ROLLBACK): " + sqlEx.getMessage(), ColoresConsola.ROJO));
                } finally {
                    conn.setAutoCommit(true);
                }

            } catch (SQLException connEx) {
                System.out.println(ColoresConsola.pintar("[ERROR] Error de conexión durante la transacción: " + connEx.getMessage(), ColoresConsola.ROJO));
                exitoTx = false;
            }
        }

        return new ResumenImportacion(total, insertados, omitidos, rechazados, exitoTx);
    }
}
