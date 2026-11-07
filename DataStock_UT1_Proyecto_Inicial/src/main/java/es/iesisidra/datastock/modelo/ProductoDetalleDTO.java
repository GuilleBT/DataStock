package es.iesisidra.datastock.modelo;

import java.math.BigDecimal;

/**
 * Objeto de transferencia de datos (DTO) que representa un producto con
 * la información combinada de su categoría y proveedor (RF-JDBC-04).
 */
public class ProductoDetalleDTO {
    private String codigo;
    private String nombre;
    private BigDecimal precio;
    private int stock;
    private int categoriaId;
    private String categoriaNombre;
    private int proveedorId;
    private String proveedorNombre;

    public ProductoDetalleDTO() { }

    public ProductoDetalleDTO(String codigo, String nombre, BigDecimal precio, int stock,
                              int categoriaId, String categoriaNombre,
                              int proveedorId, String proveedorNombre) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.categoriaId = categoriaId;
        this.categoriaNombre = categoriaNombre;
        this.proveedorId = proveedorId;
        this.proveedorNombre = proveedorNombre;
    }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public int getCategoriaId() { return categoriaId; }
    public void setCategoriaId(int categoriaId) { this.categoriaId = categoriaId; }

    public String getCategoriaNombre() { return categoriaNombre; }
    public void setCategoriaNombre(String categoriaNombre) { this.categoriaNombre = categoriaNombre; }

    public int getProveedorId() { return proveedorId; }
    public void setProveedorId(int proveedorId) { this.proveedorId = proveedorId; }

    public String getProveedorNombre() { return proveedorNombre; }
    public void setProveedorNombre(String proveedorNombre) { this.proveedorNombre = proveedorNombre; }

    @Override
    public String toString() {
        return "ProductoDetalleDTO{" +
                "codigo='" + codigo + '\'' +
                ", nombre='" + nombre + '\'' +
                ", precio=" + precio +
                ", stock=" + stock +
                ", categoria=" + categoriaNombre + " (" + categoriaId + ")" +
                ", proveedor=" + proveedorNombre + " (" + proveedorId + ")" +
                '}';
    }
}
