package es.iesisidra.datastock.servicio;

import es.iesisidra.datastock.modelo.Producto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Clase de utilidad para centralizar todas las reglas de validación de un producto.
 * Se utiliza tanto en la importación JSON como en la importación XML para garantizar
 * que ambos formatos sigan exactamente las mismas reglas de negocio.
 */
public class ValidadorProducto {

    // Patrón oficial del modelo de datos: solo letras mayúsculas, dígitos y guiones
    private static final Pattern PATRON_CODIGO = Pattern.compile("^[A-Z0-9-]+$");

    /**
     * Valida un producto completo comprobando sus datos obligatorios, rangos y duplicados.
     *
     * @param producto           El producto que queremos comprobar.
     * @param productosMemoria   Lista actual de productos cargados en memoria en la aplicación.
     * @param codigosProcesados  Conjunto con los códigos ya leídos en este mismo fichero (para evitar repetidos).
     * @return Mensaje de error explicando la incidencia, o null si el producto es totalmente válido.
     */
    public static String validar(Producto producto, List<Producto> productosMemoria, Set<String> codigosProcesados) {
        if (producto == null) {
            return "el registro del producto está vacío o no se ha podido leer";
        }

        // 1. Validación del código
        String codigo = producto.getCodigo();
        if (codigo == null || codigo.trim().isEmpty()) {
            return "el código es obligatorio";
        }

        codigo = codigo.trim();

        if (codigo.length() > 12) {
            return "el código no puede tener más de 12 caracteres";
        }

        if (!PATRON_CODIGO.matcher(codigo).matches()) {
            return "el código solo puede contener letras mayúsculas, números y guiones";
        }

        // Comprobamos si el código ya existe en la memoria de la aplicación (sin distinguir mayúsculas)
        String codigoNormalizado = codigo.toUpperCase();
        for (Producto p : productosMemoria) {
            if (p.getCodigo() != null && p.getCodigo().equalsIgnoreCase(codigoNormalizado)) {
                return "código duplicado (ya existe en el inventario)";
            }
        }

        // Comprobamos si el código viene repetido dentro del propio fichero que estamos leyendo
        if (codigosProcesados.contains(codigoNormalizado)) {
            return "código duplicado en el mismo fichero";
        }

        // 2. Validación del nombre
        String nombre = producto.getNombre();
        if (nombre == null || nombre.trim().isEmpty()) {
            return "el nombre es obligatorio";
        }

        nombre = nombre.trim();
        if (nombre.length() < 3 || nombre.length() > 100) {
            return "el nombre debe tener entre 3 y 100 caracteres";
        }

        // 3. Validación del precio
        BigDecimal precio = producto.getPrecio();
        if (precio == null) {
            return "el precio es obligatorio";
        }
        if (precio.compareTo(BigDecimal.ZERO) <= 0) {
            return "el precio debe ser mayor que 0";
        }

        // 4. Validación del stock
        if (producto.getStock() < 0) {
            return "el stock no puede ser negativo";
        }

        // 5. Validación de identificadores (categoría y proveedor deben ser positivos en UT1)
        if (producto.getCategoriaId() <= 0) {
            return "el identificador de categoría debe ser un número positivo";
        }

        if (producto.getProveedorId() <= 0) {
            return "el identificador de proveedor debe ser un número positivo";
        }

        // Si pasa todas las comprobaciones, el producto es válido
        return null;
    }
}
