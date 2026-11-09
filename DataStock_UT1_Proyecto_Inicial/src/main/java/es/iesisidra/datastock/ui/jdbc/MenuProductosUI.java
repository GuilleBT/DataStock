package es.iesisidra.datastock.ui.jdbc;

import es.iesisidra.datastock.jdbc.CategoriaDAO;
import es.iesisidra.datastock.jdbc.ProductoDAO;
import es.iesisidra.datastock.jdbc.ProveedorDAO;
import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.modelo.ProductoDetalleDTO;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;

import java.math.BigDecimal;
import java.util.List;

/**
 * Submenú interactivo para la gestión de productos (CRUD y consultas) en MySQL.
 */
public class MenuProductosUI {

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final ProveedorDAO proveedorDAO = new ProveedorDAO();

    /**
     * Muestra y gestiona el menú de operaciones CRUD de productos.
     */
    public void iniciarCrud() {
        int opcion;
        do {
            System.out.println("\n---------- MENÚ DE PRODUCTOS (CRUD) ----------");
            System.out.println("1. Dar de alta un producto");
            System.out.println("2. Consultar producto por código");
            System.out.println("3. Modificar un producto");
            System.out.println("4. Eliminar un producto");
            System.out.println("0. Volver al menú MySQL");

            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 4);
            switch (opcion) {
                case 1 -> altaProducto();
                case 2 -> consultarProductoPorCodigo();
                case 3 -> modificarProducto();
                case 4 -> eliminarProducto();
                case 0 -> { }
                default -> throw new IllegalStateException("Opción inesperada");
            }
        } while (opcion != 0);
    }

    /**
     * Muestra y gestiona el menú de consultas avanzadas de productos.
     */
    public void iniciarConsultas() {
        int opcion;
        do {
            System.out.println("\n---------- CONSULTAS DE PRODUCTOS (MYSQL) ----------");
            System.out.println("1. Listar todos los productos");
            System.out.println("2. Buscar producto por código");
            System.out.println("3. Buscar productos por nombre");
            System.out.println("4. Consultar productos con stock inferior a un umbral");
            System.out.println("0. Volver al menú MySQL");

            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 4);
            switch (opcion) {
                case 1 -> listarTodosProductos();
                case 2 -> consultarProductoPorCodigo();
                case 3 -> buscarProductosPorNombre();
                case 4 -> consultarPorStockInferior();
                case 0 -> { }
                default -> throw new IllegalStateException("Opción inesperada");
            }
        } while (opcion != 0);
    }

    public void altaProducto() {
        System.out.println("\nALTA DE PRODUCTO");
        System.out.println("---------------------------------------");
        String codigo = LectorConsola.leerTexto("Código del producto: ").trim().toUpperCase();
        if (!codigo.matches("^[A-Z0-9-]{1,12}$")) {
            System.out.println(ColoresConsola.pintar("[ERROR] El código debe tener entre 1 y 12 caracteres alfanuméricos en mayúsculas o guiones (-).", ColoresConsola.ROJO));
            LectorConsola.pausar();
            return;
        }

        try {
            if (productoDAO.existePorCodigo(codigo)) {
                System.out.println(ColoresConsola.pintar("[ERROR] Ya existe un producto con el código '" + codigo + "'.", ColoresConsola.ROJO));
                LectorConsola.pausar();
                return;
            }

            String nombre = LectorConsola.leerTexto("Nombre del producto: ").trim();
            if (nombre.length() < 3 || nombre.length() > 100) {
                System.out.println(ColoresConsola.pintar("[ERROR] El nombre debe tener entre 3 y 100 caracteres.", ColoresConsola.ROJO));
                LectorConsola.pausar();
                return;
            }

            String strPrecio = LectorConsola.leerTexto("Precio de venta (€): ").trim().replace(',', '.');
            BigDecimal precio;
            try {
                precio = new BigDecimal(strPrecio);
                if (precio.compareTo(BigDecimal.ZERO) <= 0 || precio.scale() > 2) {
                    System.out.println(ColoresConsola.pintar("[ERROR] El precio debe ser un número positivo con un máximo de 2 decimales.", ColoresConsola.ROJO));
                    LectorConsola.pausar();
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println(ColoresConsola.pintar("[ERROR] Formato de precio no válido.", ColoresConsola.ROJO));
                LectorConsola.pausar();
                return;
            }

            int stock = LectorConsola.leerEntero("Stock inicial (>= 0): ");
            if (stock < 0) {
                System.out.println(ColoresConsola.pintar("[ERROR] El stock inicial no puede ser negativo.", ColoresConsola.ROJO));
                LectorConsola.pausar();
                return;
            }

            int categoriaId = LectorConsola.leerEntero("ID de categoría: ");
            if (!categoriaDAO.existePorId(categoriaId)) {
                System.out.println(ColoresConsola.pintar("[ERROR] No existe la categoría con ID " + categoriaId + " en la base de datos.", ColoresConsola.ROJO));
                LectorConsola.pausar();
                return;
            }

            int proveedorId = LectorConsola.leerEntero("ID de proveedor: ");
            if (!proveedorDAO.existePorId(proveedorId)) {
                System.out.println(ColoresConsola.pintar("[ERROR] No existe el proveedor con ID " + proveedorId + " en la base de datos.", ColoresConsola.ROJO));
                LectorConsola.pausar();
                return;
            }

            Producto prod = new Producto(codigo, nombre, precio, stock, categoriaId, proveedorId);
            boolean insertado = productoDAO.insertar(prod);
            if (insertado) {
                System.out.println(ColoresConsola.pintar("[OK] Producto " + codigo + " registrado correctamente.", ColoresConsola.VERDE));
            } else {
                System.out.println(ColoresConsola.pintar("[ERROR] No se pudo registrar el producto en MySQL.", ColoresConsola.ROJO));
            }

        } catch (Exception ex) {
            System.out.println(ColoresConsola.pintar("[ERROR] Error al registrar el producto: " + ex.getMessage(), ColoresConsola.ROJO));
        }
        LectorConsola.pausar();
    }

    public void consultarProductoPorCodigo() {
        System.out.println("\nCONSULTA DE PRODUCTO POR CÓDIGO");
        System.out.println("---------------------------------------");
        String codigo = LectorConsola.leerTexto("Introduce el código a buscar: ").trim().toUpperCase();
        try {
            ProductoDetalleDTO prod = productoDAO.buscarPorCodigo(codigo);
            if (prod == null) {
                System.out.println(ColoresConsola.pintar("[AVISO] No se ha encontrado ningún producto con código '" + codigo + "'.", ColoresConsola.AMARILLO));
            } else {
                mostrarFichaProducto(prod);
            }
        } catch (Exception ex) {
            System.out.println(ColoresConsola.pintar("[ERROR] Error al consultar producto: " + ex.getMessage(), ColoresConsola.ROJO));
        }
        LectorConsola.pausar();
    }

    public void modificarProducto() {
        System.out.println("\nMODIFICAR PRODUCTO");
        System.out.println("---------------------------------------");
        String codigo = LectorConsola.leerTexto("Introduce el código del producto a modificar: ").trim().toUpperCase();
        try {
            ProductoDetalleDTO actual = productoDAO.buscarPorCodigo(codigo);
            if (actual == null) {
                System.out.println(ColoresConsola.pintar("[ERROR] No existe el producto con código '" + codigo + "'.", ColoresConsola.ROJO));
                LectorConsola.pausar();
                return;
            }

            System.out.println("\nValores actuales:");
            mostrarFichaProducto(actual);
            System.out.println(ColoresConsola.pintar("\nNota: El código y el stock no se pueden modificar aquí.", ColoresConsola.GRIS));
            System.out.println(ColoresConsola.pintar("(Pulsa Intro para conservar el valor actual en cada campo)", ColoresConsola.GRIS));

            String nuevoNombre = LectorConsola.leerTexto("Nuevo nombre: ").trim();
            if (nuevoNombre.isEmpty()) {
                nuevoNombre = actual.getNombre();
            } else if (nuevoNombre.length() < 3 || nuevoNombre.length() > 100) {
                System.out.println(ColoresConsola.pintar("[ERROR] El nombre debe tener entre 3 y 100 caracteres.", ColoresConsola.ROJO));
                LectorConsola.pausar();
                return;
            }

            String strPrecio = LectorConsola.leerTexto("Nuevo precio (€): ").trim().replace(',', '.');
            BigDecimal nuevoPrecio = actual.getPrecio();
            if (!strPrecio.isEmpty()) {
                try {
                    nuevoPrecio = new BigDecimal(strPrecio);
                    if (nuevoPrecio.compareTo(BigDecimal.ZERO) <= 0 || nuevoPrecio.scale() > 2) {
                        System.out.println(ColoresConsola.pintar("[ERROR] El precio debe ser un número positivo con hasta 2 decimales.", ColoresConsola.ROJO));
                        LectorConsola.pausar();
                        return;
                    }
                } catch (NumberFormatException e) {
                    System.out.println(ColoresConsola.pintar("[ERROR] Formato de precio no válido.", ColoresConsola.ROJO));
                    LectorConsola.pausar();
                    return;
                }
            }

            String strCat = LectorConsola.leerTexto("Nuevo ID de categoría: ").trim();
            int nuevaCatId = actual.getCategoriaId();
            if (!strCat.isEmpty()) {
                try {
                    nuevaCatId = Integer.parseInt(strCat);
                    if (!categoriaDAO.existePorId(nuevaCatId)) {
                        System.out.println(ColoresConsola.pintar("[ERROR] No existe la categoría con ID " + nuevaCatId + ".", ColoresConsola.ROJO));
                        LectorConsola.pausar();
                        return;
                    }
                } catch (NumberFormatException e) {
                    System.out.println(ColoresConsola.pintar("[ERROR] ID de categoría no válido.", ColoresConsola.ROJO));
                    LectorConsola.pausar();
                    return;
                }
            }

            String strProv = LectorConsola.leerTexto("Nuevo ID de proveedor: ").trim();
            int nuevoProvId = actual.getProveedorId();
            if (!strProv.isEmpty()) {
                try {
                    nuevoProvId = Integer.parseInt(strProv);
                    if (!proveedorDAO.existePorId(nuevoProvId)) {
                        System.out.println(ColoresConsola.pintar("[ERROR] No existe el proveedor con ID " + nuevoProvId + ".", ColoresConsola.ROJO));
                        LectorConsola.pausar();
                        return;
                    }
                } catch (NumberFormatException e) {
                    System.out.println(ColoresConsola.pintar("[ERROR] ID de proveedor no válido.", ColoresConsola.ROJO));
                    LectorConsola.pausar();
                    return;
                }
            }

            if (!LectorConsola.confirmar(ColoresConsola.pintar("[AVISO] ¿Confirmas la modificación del producto?", ColoresConsola.AMARILLO))) {
                System.out.println("Modificación cancelada.");
                LectorConsola.pausar();
                return;
            }

            boolean modificado = productoDAO.modificar(codigo, nuevoNombre, nuevoPrecio, nuevaCatId, nuevoProvId);
            if (modificado) {
                System.out.println(ColoresConsola.pintar("[OK] Producto " + codigo + " modificado correctamente.", ColoresConsola.VERDE));
            } else {
                System.out.println(ColoresConsola.pintar("[ERROR] No se pudo modificar el producto.", ColoresConsola.ROJO));
            }

        } catch (Exception ex) {
            System.out.println(ColoresConsola.pintar("[ERROR] Error al modificar el producto: " + ex.getMessage(), ColoresConsola.ROJO));
        }
        LectorConsola.pausar();
    }

    public void eliminarProducto() {
        System.out.println("\nELIMINAR PRODUCTO");
        System.out.println("---------------------------------------");
        String codigo = LectorConsola.leerTexto("Introduce el código del producto a eliminar: ").trim().toUpperCase();
        try {
            ProductoDetalleDTO prod = productoDAO.buscarPorCodigo(codigo);
            if (prod == null) {
                System.out.println(ColoresConsola.pintar("[ERROR] No existe el producto con código '" + codigo + "'.", ColoresConsola.ROJO));
                LectorConsola.pausar();
                return;
            }

            System.out.println("Producto encontrado:");
            mostrarFichaProducto(prod);

            int movs = productoDAO.contarMovimientos(codigo);
            if (movs > 0) {
                System.out.println(ColoresConsola.pintar("[ERROR] No se puede eliminar el producto porque tiene " + movs + " movimiento(s) de stock registrado(s).", ColoresConsola.ROJO));
                LectorConsola.pausar();
                return;
            }

            if (!LectorConsola.confirmar(ColoresConsola.pintar("[AVISO] ¿Confirmas la eliminación definitiva del producto?", ColoresConsola.AMARILLO))) {
                System.out.println("Eliminación cancelada.");
                LectorConsola.pausar();
                return;
            }

            boolean eliminado = productoDAO.eliminar(codigo);
            if (eliminado) {
                System.out.println(ColoresConsola.pintar("[OK] Producto " + codigo + " eliminado correctamente.", ColoresConsola.VERDE));
            } else {
                System.out.println(ColoresConsola.pintar("[ERROR] No se pudo eliminar el producto.", ColoresConsola.ROJO));
            }

        } catch (Exception ex) {
            System.out.println(ColoresConsola.pintar("[ERROR] Error al eliminar el producto: " + ex.getMessage(), ColoresConsola.ROJO));
        }
        LectorConsola.pausar();
    }

    public void listarTodosProductos() {
        System.out.println("\nPRODUCTOS ALMACENADOS EN MYSQL");
        System.out.println("--------------------------------------------------------------------------------------------------");
        try {
            List<ProductoDetalleDTO> productos = productoDAO.listarTodos();
            if (productos.isEmpty()) {
                System.out.println(ColoresConsola.pintar("[AVISO] No hay productos registrados en MySQL.", ColoresConsola.AMARILLO));
            } else {
                mostrarTablaProductos(productos);
                System.out.println("Total de productos encontrados: " + productos.size());
            }
        } catch (Exception ex) {
            System.out.println(ColoresConsola.pintar("[ERROR] Error al listar productos: " + ex.getMessage(), ColoresConsola.ROJO));
        }
        LectorConsola.pausar();
    }

    public void buscarProductosPorNombre() {
        System.out.println("\nBÚSQUEDA DE PRODUCTOS POR NOMBRE");
        System.out.println("---------------------------------------");
        String texto = LectorConsola.leerTexto("Introduce el fragmento de texto a buscar: ").trim();
        if (texto.isEmpty()) {
            System.out.println(ColoresConsola.pintar("[ERROR] El texto de búsqueda no puede estar vacío.", ColoresConsola.ROJO));
            LectorConsola.pausar();
            return;
        }

        try {
            List<ProductoDetalleDTO> resultados = productoDAO.buscarPorNombre(texto);
            if (resultados.isEmpty()) {
                System.out.println(ColoresConsola.pintar("[AVISO] No se encontraron productos que contengan '" + texto + "'.", ColoresConsola.AMARILLO));
            } else {
                mostrarTablaProductos(resultados);
                System.out.println("Total de coincidencias encontradas: " + resultados.size());
            }
        } catch (Exception ex) {
            System.out.println(ColoresConsola.pintar("[ERROR] Error al buscar productos: " + ex.getMessage(), ColoresConsola.ROJO));
        }
        LectorConsola.pausar();
    }

    public void consultarPorStockInferior() {
        System.out.println("\nCONSULTA POR UMBRAL DE STOCK");
        System.out.println("---------------------------------------");
        int umbral = LectorConsola.leerEntero("Introduce el umbral de existencias: ");
        if (umbral < 0) {
            System.out.println(ColoresConsola.pintar("[ERROR] El umbral debe ser un número entero mayor o igual que cero.", ColoresConsola.ROJO));
            LectorConsola.pausar();
            return;
        }

        try {
            List<ProductoDetalleDTO> resultados = productoDAO.buscarPorStockInferior(umbral);
            if (resultados.isEmpty()) {
                System.out.println(ColoresConsola.pintar("[AVISO] No hay ningún producto con stock inferior a " + umbral + " unidades.", ColoresConsola.AMARILLO));
            } else {
                mostrarTablaProductos(resultados);
                System.out.println("Total de productos con stock < " + umbral + ": " + resultados.size());
            }
        } catch (Exception ex) {
            System.out.println(ColoresConsola.pintar("[ERROR] Error al consultar por stock: " + ex.getMessage(), ColoresConsola.ROJO));
        }
        LectorConsola.pausar();
    }

    public void mostrarFichaProducto(ProductoDetalleDTO p) {
        System.out.println("-----------------------------------------------------------");
        System.out.println("Código:    " + p.getCodigo());
        System.out.println("Nombre:    " + p.getNombre());
        System.out.printf("Precio:    %.2f €%n", p.getPrecio());
        System.out.println("Stock:     " + p.getStock());
        System.out.println("Categoría: " + p.getCategoriaNombre() + " (ID: " + p.getCategoriaId() + ")");
        System.out.println("Proveedor: " + p.getProveedorNombre() + " (ID: " + p.getProveedorId() + ")");
        System.out.println("-----------------------------------------------------------");
    }

    public void mostrarTablaProductos(List<ProductoDetalleDTO> productos) {
        System.out.printf("%-12s %-32s %-12s %-8s %-20s %-22s%n",
                "CÓDIGO", "NOMBRE", "PRECIO", "STOCK", "CATEGORÍA", "PROVEEDOR");
        System.out.println("--------------------------------------------------------------------------------------------------");
        for (ProductoDetalleDTO p : productos) {
            System.out.printf("%-12s %-32s %8.2f €  %-8d %-20s %-22s%n",
                    p.getCodigo(),
                    p.getNombre().length() > 30 ? p.getNombre().substring(0, 27) + "..." : p.getNombre(),
                    p.getPrecio(),
                    p.getStock(),
                    p.getCategoriaNombre(),
                    p.getProveedorNombre());
        }
        System.out.println("--------------------------------------------------------------------------------------------------");
    }
}
