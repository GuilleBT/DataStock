package es.iesisidra.datastock.ui;

import es.iesisidra.datastock.jdbc.ConexionBD;
import es.iesisidra.datastock.servicio.Inventario;
import es.iesisidra.datastock.ui.jdbc.MenuCategoriasUI;
import es.iesisidra.datastock.ui.jdbc.MenuLotesTransaccionesUI;
import es.iesisidra.datastock.ui.jdbc.MenuMovimientosUI;
import es.iesisidra.datastock.ui.jdbc.MenuProductosUI;
import es.iesisidra.datastock.ui.jdbc.MenuProveedoresUI;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;

/**
 * Submenú interactivo para las operaciones JDBC con MySQL (UT2).
 * Actúa como orquestador delegando la presentación en controladores UI específicos.
 */
public class MenuJdbc {

    private final Inventario inventario;
    private final MenuCategoriasUI menuCategoriasUI;
    private final MenuProveedoresUI menuProveedoresUI;
    private final MenuProductosUI menuProductosUI;
    private final MenuMovimientosUI menuMovimientosUI;
    private final MenuLotesTransaccionesUI menuLotesTransaccionesUI;

    public MenuJdbc(Inventario inventario) {
        this.inventario = inventario;
        this.menuCategoriasUI = new MenuCategoriasUI();
        this.menuProveedoresUI = new MenuProveedoresUI();
        this.menuProductosUI = new MenuProductosUI();
        this.menuMovimientosUI = new MenuMovimientosUI();
        this.menuLotesTransaccionesUI = new MenuLotesTransaccionesUI(inventario);
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
                case 2 -> menuCategoriasUI.iniciar();
                case 3 -> menuProveedoresUI.iniciar();
                case 4 -> menuProductosUI.iniciarCrud();
                case 5 -> menuProductosUI.iniciarConsultas();
                case 6 -> menuMovimientosUI.iniciarMovimientos();
                case 7 -> menuMovimientosUI.iniciarHistorial();
                case 8 -> menuLotesTransaccionesUI.ejecutarImportacionMemoriaMysql();
                case 9 -> menuLotesTransaccionesUI.ejecutarDemostracionTransacciones();
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
}
