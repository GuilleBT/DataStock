package es.iesisidra.datastock.ui;

import es.iesisidra.datastock.jdbc.CategoriaDAO;
import es.iesisidra.datastock.jdbc.ConexionBD;
import es.iesisidra.datastock.jdbc.ProductoDAO;
import es.iesisidra.datastock.jdbc.ProveedorDAO;
import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.modelo.ProductoDetalleDTO;
import es.iesisidra.datastock.servicio.Inventario;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;

import java.math.BigDecimal;
import java.util.List;

/**
 * Submenú interactivo para las operaciones JDBC con MySQL (UT2).
 */
public class MenuJdbc {

    private final Inventario inventario;

    public MenuJdbc(Inventario inventario) {
        this.inventario = inventario;
    }

    /**
     * Inicia el bucle de navegación del menú JDBC.
     */
    public void iniciar() {
        int opcion;
        do {
            mostrarCabecera();
            System.out.println("1. Comprobar conexión y mostrar información de la BD");
            System.out.println("2. Gestionar categorías");
            System.out.println("3. Gestionar proveedores");
            System.out.println("4. Gestionar productos (CRUD)");
            System.out.println("5. Consultar y buscar productos");
            System.out.println("6. Registrar entrada o salida de stock");
            System.out.println("7. Consultar historial de movimientos");
            System.out.println("8. Importar productos de memoria a MySQL");
            System.out.println("9. Demostración de transacción con commit/rollback");
            System.out.println("0. Volver al menú principal");

            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 9);
            switch (opcion) {
                case 1 -> {
                    ConexionBD.probarConexion();
                    LectorConsola.pausar();
                }
                case 2 -> menuCategorias();
                case 3 -> menuProveedores();
                case 4 -> menuProductosCrud();
                case 5 -> menuConsultasProductos();
                case 6 -> menuMovimientosStock();
                case 7 -> menuHistorialMovimientos();
                case 8 -> ejecutarImportacionMemoriaMysql();
                case 9 -> ejecutarDemostracionTransacciones();
                case 0 -> { }
                default -> throw new IllegalStateException("Opción inesperada");
            }
        } while (opcion != 0);
    }

    private void mostrarCabecera() {
        System.out.println(ColoresConsola.pintar("\n================================================", ColoresConsola.AZUL));
        System.out.println(ColoresConsola.pintar("              GESTIÓN MYSQL - UT2", ColoresConsola.NEGRITA));
        System.out.println(ColoresConsola.pintar("================================================", ColoresConsola.AZUL));
    }

    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final ProveedorDAO proveedorDAO = new ProveedorDAO();

    private void menuCategorias() {
        int opcion;
        do {
            System.out.println("\n---------- MENÚ DE CATEGORÍAS ----------");
            System.out.println("1. Dar de alta una categoría");
            System.out.println("2. Listar categorías");
            System.out.println("3. Modificar el nombre de una categoría");
            System.out.println("4. Eliminar una categoría");
            System.out.println("0. Volver al menú MySQL");

            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 4);
            switch (opcion) {
                case 1 -> altaCategoria();
                case 2 -> listarCategorias();
                case 3 -> modificarCategoria();
                case 4 -> eliminarCategoria();
                case 0 -> { }
                default -> throw new IllegalStateException("Opción inesperada");
            }
        } while (opcion != 0);
    }

    private void altaCategoria() {
        System.out.println("\nALTA DE CATEGORÍA");
        System.out.println("---------------------------------------");
        int id = LectorConsola.leerEntero("ID de categoría: ");
        if (id <= 0) {
            System.out.println(ColoresConsola.pintar("[ERROR] El identificador debe ser un entero positivo.", ColoresConsola.ROJO));
            LectorConsola.pausar();
            return;
        }

        String nombre = LectorConsola.leerTexto("Nombre: ").trim();
        if (nombre.isEmpty() || nombre.length() > 60) {
            System.out.println(ColoresConsola.pintar("[ERROR] El nombre es obligatorio y debe tener entre 1 y 60 caracteres.", ColoresConsola.ROJO));
            LectorConsola.pausar();
            return;
        }

        try {
            if (categoriaDAO.existePorId(id)) {
                System.out.println(ColoresConsola.pintar("[ERROR] Ya existe una categoría con el ID " + id + ".", ColoresConsola.ROJO));
                LectorConsola.pausar();
                return;
            }
            if (categoriaDAO.existePorNombre(nombre)) {
                System.out.println(ColoresConsola.pintar("[ERROR] Ya existe una categoría con el nombre '" + nombre + "'.", ColoresConsola.ROJO));
                LectorConsola.pausar();
                return;
            }

            categoriaDAO.insertar(new es.iesisidra.datastock.modelo.Categoria(id, nombre));
            System.out.println(ColoresConsola.pintar("[OK] Categoría registrada correctamente.", ColoresConsola.VERDE));
        } catch (Exception ex) {
            System.out.println(ColoresConsola.pintar("[ERROR] No se pudo guardar la categoría: " + ex.getMessage(), ColoresConsola.ROJO));
        }
        LectorConsola.pausar();
    }

    private void listarCategorias() {
        System.out.println("\nLISTADO DE CATEGORÍAS");
        System.out.println("---------------------------------------");
        try {
            var categorias = categoriaDAO.listarTodas();
            if (categorias.isEmpty()) {
                System.out.println("No hay categorías registradas.");
            } else {
                System.out.printf("%-6s %-30s%n", "ID", "NOMBRE");
                System.out.println("---------------------------------------");
                for (var cat : categorias) {
                    System.out.printf("%-6d %-30s%n", cat.getId(), cat.getNombre());
                }
            }
        } catch (Exception ex) {
            System.out.println(ColoresConsola.pintar("[ERROR] Error al consultar las categorías: " + ex.getMessage(), ColoresConsola.ROJO));
        }
        LectorConsola.pausar();
    }

    private void modificarCategoria() {
        System.out.println("\nMODIFICAR CATEGORÍA");
        System.out.println("---------------------------------------");
        int id = LectorConsola.leerEntero("Introduce el ID de la categoría a modificar: ");
        try {
            var cat = categoriaDAO.buscarPorId(id);
            if (cat == null) {
                System.out.println(ColoresConsola.pintar("[ERROR] No existe ninguna categoría con ID " + id + ".", ColoresConsola.ROJO));
                LectorConsola.pausar();
                return;
            }

            System.out.println("Nombre actual: " + cat.getNombre());
            String nuevoNombre = LectorConsola.leerTexto("Nuevo nombre: ").trim();
            if (nuevoNombre.isEmpty() || nuevoNombre.length() > 60) {
                System.out.println(ColoresConsola.pintar("[ERROR] El nombre es obligatorio y debe tener entre 1 y 60 caracteres.", ColoresConsola.ROJO));
                LectorConsola.pausar();
                return;
            }

            if (categoriaDAO.existePorNombreExcluyendoId(nuevoNombre, id)) {
                System.out.println(ColoresConsola.pintar("[ERROR] Ya existe otra categoría con el nombre '" + nuevoNombre + "'.", ColoresConsola.ROJO));
                LectorConsola.pausar();
                return;
            }

            if (!LectorConsola.confirmar(ColoresConsola.pintar("[AVISO] ¿Confirmas la modificación?", ColoresConsola.AMARILLO))) {
                System.out.println("Modificación cancelada por el usuario.");
                LectorConsola.pausar();
                return;
            }

            boolean modificado = categoriaDAO.modificarNombre(id, nuevoNombre);
            if (modificado) {
                System.out.println(ColoresConsola.pintar("[OK] Categoría modificada correctamente.", ColoresConsola.VERDE));
            } else {
                System.out.println(ColoresConsola.pintar("[ERROR] No se pudo modificar la categoría.", ColoresConsola.ROJO));
            }
        } catch (Exception ex) {
            System.out.println(ColoresConsola.pintar("[ERROR] Error al modificar la categoría: " + ex.getMessage(), ColoresConsola.ROJO));
        }
        LectorConsola.pausar();
    }

    private void eliminarCategoria() {
        System.out.println("\nELIMINAR CATEGORÍA");
        System.out.println("---------------------------------------");
        int id = LectorConsola.leerEntero("Introduce el ID de la categoría a eliminar: ");
        try {
            var cat = categoriaDAO.buscarPorId(id);
            if (cat == null) {
                System.out.println(ColoresConsola.pintar("[ERROR] No existe ninguna categoría con ID " + id + ".", ColoresConsola.ROJO));
                LectorConsola.pausar();
                return;
            }

            System.out.println("Categoría encontrada: [" + cat.getId() + "] " + cat.getNombre());
            int prods = categoriaDAO.contarProductosAsociados(id);
            if (prods > 0) {
                System.out.println(ColoresConsola.pintar("[ERROR] No se puede eliminar la categoría. Existen " + prods + " producto(s) asociado(s).", ColoresConsola.ROJO));
                LectorConsola.pausar();
                return;
            }

            if (!LectorConsola.confirmar(ColoresConsola.pintar("[AVISO] ¿Confirmas la eliminación de la categoría?", ColoresConsola.AMARILLO))) {
                System.out.println("Eliminación cancelada.");
                LectorConsola.pausar();
                return;
            }

            boolean eliminado = categoriaDAO.eliminar(id);
            if (eliminado) {
                System.out.println(ColoresConsola.pintar("[OK] Categoría eliminada correctamente.", ColoresConsola.VERDE));
            } else {
                System.out.println(ColoresConsola.pintar("[ERROR] No se pudo eliminar la categoría.", ColoresConsola.ROJO));
            }
        } catch (Exception ex) {
            System.out.println(ColoresConsola.pintar("[ERROR] No se puede eliminar la categoría: " + ex.getMessage(), ColoresConsola.ROJO));
        }
        LectorConsola.pausar();
    }

    private void menuProveedores() {
        int opcion;
        do {
            System.out.println("\n---------- MENÚ DE PROVEEDORES ----------");
            System.out.println("1. Dar de alta un proveedor");
            System.out.println("2. Listar proveedores");
            System.out.println("3. Modificar un proveedor");
            System.out.println("4. Eliminar un proveedor");
            System.out.println("0. Volver al menú MySQL");

            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 4);
            switch (opcion) {
                case 1 -> altaProveedor();
                case 2 -> listarProveedores();
                case 3 -> modificarProveedor();
                case 4 -> eliminarProveedor();
                case 0 -> { }
                default -> throw new IllegalStateException("Opción inesperada");
            }
        } while (opcion != 0);
    }

    private void altaProveedor() {
        System.out.println("\nALTA DE PROVEEDOR");
        System.out.println("---------------------------------------");
        int id = LectorConsola.leerEntero("ID de proveedor: ");
        if (id <= 0) {
            System.out.println(ColoresConsola.pintar("[ERROR] El identificador debe ser un entero positivo.", ColoresConsola.ROJO));
            LectorConsola.pausar();
            return;
        }

        String nombre = LectorConsola.leerTexto("Nombre: ").trim();
        if (nombre.isEmpty() || nombre.length() > 100) {
            System.out.println(ColoresConsola.pintar("[ERROR] El nombre es obligatorio y no debe superar los 100 caracteres.", ColoresConsola.ROJO));
            LectorConsola.pausar();
            return;
        }

        String email = LectorConsola.leerTexto("Email (opcional): ").trim();
        if (!email.isEmpty()) {
            if (email.length() > 120 || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                System.out.println(ColoresConsola.pintar("[ERROR] El formato del correo electrónico no es válido o supera los 120 caracteres.", ColoresConsola.ROJO));
                LectorConsola.pausar();
                return;
            }
        } else {
            email = null;
        }

        try {
            if (proveedorDAO.existePorId(id)) {
                System.out.println(ColoresConsola.pintar("[ERROR] Ya existe un proveedor con el ID " + id + ".", ColoresConsola.ROJO));
                LectorConsola.pausar();
                return;
            }
            if (email != null && proveedorDAO.existePorEmail(email)) {
                System.out.println(ColoresConsola.pintar("[ERROR] Ya existe un proveedor con el correo electrónico '" + email + "'.", ColoresConsola.ROJO));
                LectorConsola.pausar();
                return;
            }

            proveedorDAO.insertar(new es.iesisidra.datastock.modelo.Proveedor(id, nombre, email));
            System.out.println(ColoresConsola.pintar("[OK] Proveedor registrado correctamente.", ColoresConsola.VERDE));
        } catch (Exception ex) {
            System.out.println(ColoresConsola.pintar("[ERROR] No se pudo guardar el proveedor: " + ex.getMessage(), ColoresConsola.ROJO));
        }
        LectorConsola.pausar();
    }

    private void listarProveedores() {
        System.out.println("\nLISTADO DE PROVEEDORES");
        System.out.println("---------------------------------------");
        try {
            var proveedores = proveedorDAO.listarTodos();
            if (proveedores.isEmpty()) {
                System.out.println("No hay proveedores registrados.");
            } else {
                System.out.printf("%-6s %-30s %-35s%n", "ID", "NOMBRE", "EMAIL");
                System.out.println("-------------------------------------------------------------------------");
                for (var prov : proveedores) {
                    String correo = (prov.getEmail() != null && !prov.getEmail().isBlank()) ? prov.getEmail() : "—";
                    System.out.printf("%-6d %-30s %-35s%n", prov.getId(), prov.getNombre(), correo);
                }
            }
        } catch (Exception ex) {
            System.out.println(ColoresConsola.pintar("[ERROR] Error al consultar los proveedores: " + ex.getMessage(), ColoresConsola.ROJO));
        }
        LectorConsola.pausar();
    }

    private void modificarProveedor() {
        System.out.println("\nMODIFICAR PROVEEDOR");
        System.out.println("---------------------------------------");
        int id = LectorConsola.leerEntero("Introduce el ID del proveedor a modificar: ");
        try {
            var prov = proveedorDAO.buscarPorId(id);
            if (prov == null) {
                System.out.println(ColoresConsola.pintar("[ERROR] No existe ningún proveedor con ID " + id + ".", ColoresConsola.ROJO));
                LectorConsola.pausar();
                return;
            }

            System.out.println("Nombre actual: " + prov.getNombre());
            System.out.println("Email actual:  " + (prov.getEmail() != null ? prov.getEmail() : "—"));
            System.out.println("(Pulsa Intro para conservar el valor actual, o escribe '-' para borrar el email)");

            String nuevoNombre = LectorConsola.leerTexto("Nuevo nombre: ").trim();
            if (nuevoNombre.isEmpty()) {
                nuevoNombre = prov.getNombre();
            } else if (nuevoNombre.length() > 100) {
                System.out.println(ColoresConsola.pintar("[ERROR] El nombre no debe superar los 100 caracteres.", ColoresConsola.ROJO));
                LectorConsola.pausar();
                return;
            }

            String nuevoEmail = LectorConsola.leerTexto("Nuevo email: ").trim();
            if (nuevoEmail.isEmpty()) {
                nuevoEmail = prov.getEmail();
            } else if (nuevoEmail.equals("-")) {
                nuevoEmail = null;
            } else {
                if (nuevoEmail.length() > 120 || !nuevoEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                    System.out.println(ColoresConsola.pintar("[ERROR] Formato de email no válido o supera 120 caracteres.", ColoresConsola.ROJO));
                    LectorConsola.pausar();
                    return;
                }
                if (proveedorDAO.existePorEmailExcluyendoId(nuevoEmail, id)) {
                    System.out.println(ColoresConsola.pintar("[ERROR] Ya existe otro proveedor con el correo '" + nuevoEmail + "'.", ColoresConsola.ROJO));
                    LectorConsola.pausar();
                    return;
                }
            }

            if (!LectorConsola.confirmar(ColoresConsola.pintar("[AVISO] ¿Confirmas la modificación?", ColoresConsola.AMARILLO))) {
                System.out.println("Modificación cancelada por el usuario.");
                LectorConsola.pausar();
                return;
            }

            boolean modificado = proveedorDAO.modificar(id, nuevoNombre, nuevoEmail);
            if (modificado) {
                System.out.println(ColoresConsola.pintar("[OK] Proveedor modificado correctamente.", ColoresConsola.VERDE));
            } else {
                System.out.println(ColoresConsola.pintar("[ERROR] No se pudo modificar el proveedor.", ColoresConsola.ROJO));
            }
        } catch (Exception ex) {
            System.out.println(ColoresConsola.pintar("[ERROR] Error al modificar el proveedor: " + ex.getMessage(), ColoresConsola.ROJO));
        }
        LectorConsola.pausar();
    }

    private void eliminarProveedor() {
        System.out.println("\nELIMINAR PROVEEDOR");
        System.out.println("---------------------------------------");
        int id = LectorConsola.leerEntero("Introduce el ID del proveedor a eliminar: ");
        try {
            var prov = proveedorDAO.buscarPorId(id);
            if (prov == null) {
                System.out.println(ColoresConsola.pintar("[ERROR] No existe ningún proveedor con ID " + id + ".", ColoresConsola.ROJO));
                LectorConsola.pausar();
                return;
            }

            System.out.println("Proveedor encontrado: [" + prov.getId() + "] " + prov.getNombre());
            int prods = proveedorDAO.contarProductosAsociados(id);
            if (prods > 0) {
                System.out.println(ColoresConsola.pintar("[ERROR] No se puede eliminar el proveedor. Existen " + prods + " producto(s) asociado(s).", ColoresConsola.ROJO));
                LectorConsola.pausar();
                return;
            }

            if (!LectorConsola.confirmar(ColoresConsola.pintar("[AVISO] ¿Confirmas la eliminación del proveedor?", ColoresConsola.AMARILLO))) {
                System.out.println("Eliminación cancelada.");
                LectorConsola.pausar();
                return;
            }

            boolean eliminado = proveedorDAO.eliminar(id);
            if (eliminado) {
                System.out.println(ColoresConsola.pintar("[OK] Proveedor eliminado correctamente.", ColoresConsola.VERDE));
            } else {
                System.out.println(ColoresConsola.pintar("[ERROR] No se pudo eliminar el proveedor.", ColoresConsola.ROJO));
            }
        } catch (Exception ex) {
            System.out.println(ColoresConsola.pintar("[ERROR] No se puede eliminar el proveedor: " + ex.getMessage(), ColoresConsola.ROJO));
        }
        LectorConsola.pausar();
    }

    private final ProductoDAO productoDAO = new ProductoDAO();

    private void menuProductosCrud() {
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

    private void altaProducto() {
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

    private void consultarProductoPorCodigo() {
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

    private void modificarProducto() {
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

    private void eliminarProducto() {
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

    private void menuConsultasProductos() {
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

    private void listarTodosProductos() {
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

    private void buscarProductosPorNombre() {
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

    private void consultarPorStockInferior() {
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

    private void mostrarFichaProducto(ProductoDetalleDTO p) {
        System.out.println("-----------------------------------------------------------");
        System.out.println("Código:    " + p.getCodigo());
        System.out.println("Nombre:    " + p.getNombre());
        System.out.printf("Precio:    %.2f €%n", p.getPrecio());
        System.out.println("Stock:     " + p.getStock());
        System.out.println("Categoría: " + p.getCategoriaNombre() + " (ID: " + p.getCategoriaId() + ")");
        System.out.println("Proveedor: " + p.getProveedorNombre() + " (ID: " + p.getProveedorId() + ")");
        System.out.println("-----------------------------------------------------------");
    }

    private void mostrarTablaProductos(List<ProductoDetalleDTO> productos) {
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

    private void menuMovimientosStock() {
        System.out.println(ColoresConsola.pintar("[EN DESARROLLO] Submenú Movimientos de Stock", ColoresConsola.AMARILLO));
        LectorConsola.pausar();
    }

    private void menuHistorialMovimientos() {
        System.out.println(ColoresConsola.pintar("[EN DESARROLLO] Submenú Historial de Movimientos", ColoresConsola.AMARILLO));
        LectorConsola.pausar();
    }

    private void ejecutarImportacionMemoriaMysql() {
        System.out.println(ColoresConsola.pintar("[EN DESARROLLO] Importación de memoria a MySQL", ColoresConsola.AMARILLO));
        LectorConsola.pausar();
    }

    private void ejecutarDemostracionTransacciones() {
        System.out.println(ColoresConsola.pintar("[EN DESARROLLO] Demostración de transacciones", ColoresConsola.AMARILLO));
        LectorConsola.pausar();
    }
}
