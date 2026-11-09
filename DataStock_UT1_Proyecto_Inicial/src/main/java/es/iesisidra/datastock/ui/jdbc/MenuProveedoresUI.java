package es.iesisidra.datastock.ui.jdbc;

import es.iesisidra.datastock.jdbc.ProveedorDAO;
import es.iesisidra.datastock.modelo.Proveedor;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;

/**
 * Submenú interactivo para la gestión de proveedores (CRUD) en MySQL.
 */
public class MenuProveedoresUI {

    private final ProveedorDAO proveedorDAO = new ProveedorDAO();

    /**
     * Muestra y gestiona el menú de operaciones con proveedores.
     */
    public void iniciar() {
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

            proveedorDAO.insertar(new Proveedor(id, nombre, email));
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
}
