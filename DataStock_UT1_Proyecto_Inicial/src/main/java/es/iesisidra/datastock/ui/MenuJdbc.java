package es.iesisidra.datastock.ui;

import es.iesisidra.datastock.jdbc.ConexionBD;
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

    private void menuCategorias() {
        System.out.println(ColoresConsola.pintar("[EN DESARROLLO] Submenú de Categorías", ColoresConsola.AMARILLO));
        LectorConsola.pausar();
    }

    private void menuProveedores() {
        System.out.println(ColoresConsola.pintar("[EN DESARROLLO] Submenú de Proveedores", ColoresConsola.AMARILLO));
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
