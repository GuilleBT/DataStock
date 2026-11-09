package es.iesisidra.datastock.ui.jdbc;

import es.iesisidra.datastock.jdbc.CategoriaDAO;
import es.iesisidra.datastock.modelo.Categoria;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;

/**
 * Submenú interactivo para la gestión de categorías (CRUD) en MySQL.
 */
public class MenuCategoriasUI {

    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    /**
     * Muestra y gestiona el menú de operaciones con categorías.
     */
    public void iniciar() {
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

            categoriaDAO.insertar(new Categoria(id, nombre));
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
}
