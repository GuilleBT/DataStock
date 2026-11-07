package es.iesisidra.datastock.ui;

import es.iesisidra.datastock.jdbc.CategoriaDAO;
import es.iesisidra.datastock.jdbc.ConexionBD;
import es.iesisidra.datastock.jdbc.ProveedorDAO;
import es.iesisidra.datastock.servicio.Inventario;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;

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

    private void menuProductosCrud() {
        System.out.println(ColoresConsola.pintar("[EN DESARROLLO] Submenú CRUD de Productos", ColoresConsola.AMARILLO));
        LectorConsola.pausar();
    }

    private void menuConsultasProductos() {
        System.out.println(ColoresConsola.pintar("[EN DESARROLLO] Submenú Consultas de Productos", ColoresConsola.AMARILLO));
        LectorConsola.pausar();
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
