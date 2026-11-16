package es.iesisidra.datastock.componentes;

import es.iesisidra.datastock.jdbc.ConexionBD;
import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.modelo.ProductoDetalleDTO;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Componente reutilizable de acceso a datos relacionales encargado de la consulta
 * y recuperación de productos en MySQL mediante JDBC (UT3).
 *
 * <p><b>Principios de diseño arquitectónico:</b></p>
 * <ul>
 *   <li><b>Desacoplamiento de capa de presentación:</b> El componente carece por completo
 *       de dependencias con la consola, colores o entrada por teclado. No invoca {@code System.out}
 *       ni manipula flujos de entrada interactivos.</li>
 *   <li><b>Gestión segura y determinista de recursos:</b> Todas las conexiones, sentencias preparadas
 *       ({@link PreparedStatement}) y conjuntos de resultados ({@link ResultSet}) se gestionan
 *       estrictamente mediante bloques {@code try-with-resources}, garantizando el cierre inmediato
 *       y la devolución de conexiones al sistema gestor.</li>
 *   <li><b>Semántica clara de ausencia de valor:</b> Emplea {@link Optional} para expresar
 *       explícitamente cuando un código consultado no existe en la base de datos, evitando riesgos
 *       de {@code NullPointerException} en el código cliente.</li>
 *   <li><b>Integridad con el modelo existente:</b> Se integra con las credenciales y configuración
 *       centralizada de {@link ConexionBD} sin duplicar configuraciones ni alterar el esquema relacional.</li>
 * </ul>
 *
 * @author Guillermo Beltrán Tabares
 * @version 1.0 (UT3)
 */
public class ConsultaProductoJdbc {

    private static final String SQL_BUSCAR_POR_CODIGO =
            "SELECT p.codigo, p.nombre, p.precio, p.stock, " +
            "       p.categoria_id, c.nombre AS categoria_nombre, " +
            "       p.proveedor_id, pr.nombre AS proveedor_nombre " +
            "FROM producto p " +
            "JOIN categoria c ON p.categoria_id = c.id " +
            "JOIN proveedor pr ON p.proveedor_id = pr.id " +
            "WHERE p.codigo = ?";

    private static final String SQL_EXISTE_POR_CODIGO =
            "SELECT 1 FROM producto WHERE codigo = ?";

    /**
     * Constructor por defecto del componente.
     */
    public ConsultaProductoJdbc() {
    }

    /**
     * Consulta un producto en MySQL a partir de su código único identificador, recuperando
     * tanto sus atributos propios como las denominaciones de su categoría y proveedor asociados (JOIN).
     *
     * @param codigo Código identificador del producto a localizar (por ejemplo, "TEC-001").
     * @return {@link Optional} que contiene el {@link ProductoDetalleDTO} con la información completa
     *         si el producto fue hallado, o {@link Optional#empty()} si no existe coincidencia
     *         o si el parámetro es nulo o vacío.
     * @throws SQLException Si ocurre un error de comunicación, fallo de red o error de sintaxis en el motor MySQL.
     */
    public Optional<ProductoDetalleDTO> buscarPorCodigo(String codigo) throws SQLException {
        if (codigo == null || codigo.isBlank()) {
            return Optional.empty();
        }

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(SQL_BUSCAR_POR_CODIGO)) {

            ps.setString(1, codigo.trim().toUpperCase());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ProductoDetalleDTO dto = mapearProductoDetalle(rs);
                    return Optional.of(dto);
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Versión compatible que devuelve directamente el objeto {@link ProductoDetalleDTO}
     * o {@code null} en caso de no existir. Facilita la interoperabilidad directa con capas DAO previas.
     *
     * @param codigo Código identificador del producto a consultar.
     * @return Instancia de {@link ProductoDetalleDTO} si existe; {@code null} en caso contrario.
     * @throws SQLException Si surge un error en la capa JDBC o en la base de datos MySQL.
     */
    public ProductoDetalleDTO buscar(String codigo) throws SQLException {
        return buscarPorCodigo(codigo).orElse(null);
    }

    /**
     * Recupera el producto en forma de entidad de modelo base {@link Producto} (sin los nombres de relaciones).
     *
     * @param codigo Código alfanumérico del producto a buscar.
     * @return {@link Optional} con el objeto {@link Producto} básico, o {@link Optional#empty()} si no existe.
     * @throws SQLException Si se produce un fallo al ejecutar la consulta JDBC.
     */
    public Optional<Producto> buscarProductoSimple(String codigo) throws SQLException {
        return buscarPorCodigo(codigo).map(dto -> new Producto(
                dto.getCodigo(),
                dto.getNombre(),
                dto.getPrecio(),
                dto.getStock(),
                dto.getCategoriaId(),
                dto.getProveedorId()
        ));
    }

    /**
     * Comprueba de forma optimizada y ligera si existe al menos un producto registrado con el código indicado.
     *
     * @param codigo Código que se desea contrastar.
     * @return {@code true} si el producto existe en la base de datos; {@code false} si no existe o el código es inválido.
     * @throws SQLException Si ocurre un error de comunicación con el gestor MySQL.
     */
    public boolean existe(String codigo) throws SQLException {
        if (codigo == null || codigo.isBlank()) {
            return false;
        }

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(SQL_EXISTE_POR_CODIGO)) {

            ps.setString(1, codigo.trim().toUpperCase());

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Mapea de forma privada los valores del registro actual de un {@link ResultSet} a un objeto DTO.
     *
     * @param rs Cursor apuntando a una fila válida del resultado de la consulta SQL.
     * @return Instancia poblada de {@link ProductoDetalleDTO}.
     * @throws SQLException Si ocurre un error al extraer cualquiera de las columnas.
     */
    private ProductoDetalleDTO mapearProductoDetalle(ResultSet rs) throws SQLException {
        String codigo = rs.getString("codigo");
        String nombre = rs.getString("nombre");
        BigDecimal precio = rs.getBigDecimal("precio");
        int stock = rs.getInt("stock");
        int categoriaId = rs.getInt("categoria_id");
        String categoriaNombre = rs.getString("categoria_nombre");
        int proveedorId = rs.getInt("proveedor_id");
        String proveedorNombre = rs.getString("proveedor_nombre");

        return new ProductoDetalleDTO(codigo, nombre, precio, stock,
                categoriaId, categoriaNombre, proveedorId, proveedorNombre);
    }
}
