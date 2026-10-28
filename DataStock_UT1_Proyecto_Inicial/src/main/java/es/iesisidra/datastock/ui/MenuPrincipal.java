package es.iesisidra.datastock.ui;

import es.iesisidra.datastock.servicio.ConsultaProductoServicio;
import es.iesisidra.datastock.servicio.ExploradorFicherosServicio;
import es.iesisidra.datastock.servicio.ImportadorJsonServicio;
import es.iesisidra.datastock.servicio.Inventario;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;

/**
 * Presentación de menús y navegación de la primera fase de DataStock.
 * <p>Los métodos marcados TODO son puntos de partida, NO soluciones de los
 * requisitos. El alumnado deberá delegar el acceso a ficheros, la validación
 * y el procesamiento en métodos o clases independientes.</p>
 */
public class MenuPrincipal {
    private final Inventario inventario;
    private final ExploradorFicherosServicio exploradorServicio = new ExploradorFicherosServicio();
    private final ImportadorJsonServicio importadorJsonServicio = new ImportadorJsonServicio();
    private final ConsultaProductoServicio consultaServicio = new ConsultaProductoServicio();

    /** @param inventario colección común de productos de esta ejecución. */
    public MenuPrincipal(Inventario inventario) { this.inventario = inventario; }

    /** Ejecuta el menú principal hasta que el usuario selecciona salir. */
    public void iniciar() {
        int opcion;
        do {
            mostrarCabecera();
            System.out.println("1. Explorar ficheros y directorios");
            System.out.println("2. Importar productos desde JSON");
            System.out.println("3. Importar productos desde XML");
            System.out.println("4. Consultar productos");
            System.out.println("5. Exportar productos");
            System.out.println("6. Guardar y recuperar inventario");
            System.out.println("0. Salir");
            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 6);
            switch (opcion) {
                case 1 -> ejecutarExploracion();
                case 2 -> ejecutarImportacionJson();
                case 3 -> pendiente("RF-FIC-03 · Importar productos desde XML");
                case 4 -> menuConsultas();
                case 5 -> menuExportaciones();
                case 6 -> menuSerializacion();
                case 0 -> System.out.println(ColoresConsola.pintar("Hasta pronto.", ColoresConsola.VERDE));
                default -> throw new IllegalStateException("Opción inesperada");
            }
        } while (opcion != 0);
    }

    private void mostrarCabecera() {
        System.out.println(ColoresConsola.pintar("\n================================================", ColoresConsola.CIAN));
        System.out.println(ColoresConsola.pintar("         DATASTOCK - GESTIÓN DE INVENTARIO", ColoresConsola.NEGRITA));
        System.out.println(ColoresConsola.pintar("                 FASE 1 - UT1", ColoresConsola.CIAN));
        System.out.println(ColoresConsola.pintar("================================================", ColoresConsola.CIAN));
        System.out.println(ColoresConsola.pintar("Productos en memoria: " + inventario.getProductos().size(), ColoresConsola.GRIS));
    }

    private void menuConsultas() {
        int opcion;
        do {
            System.out.println("\n---------- CONSULTA DE PRODUCTOS ----------");
            System.out.println("1. Listar todos los productos");
            System.out.println("2. Buscar producto por código");
            System.out.println("3. Buscar productos por nombre");
            System.out.println("0. Volver al menú principal");
            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 3);
            switch (opcion) {
                case 1 -> {
                    consultaServicio.listarTodos(inventario.getProductos());
                    LectorConsola.pausar();
                }
                case 2 -> {
                    String codigo = LectorConsola.leerTexto("Introduce el código a buscar: ");
                    consultaServicio.buscarPorCodigo(codigo, inventario.getProductos());
                    LectorConsola.pausar();
                }
                case 3 -> {
                    String nombre = LectorConsola.leerTexto("Introduce el texto a buscar en el nombre: ");
                    consultaServicio.buscarPorNombre(nombre, inventario.getProductos());
                    LectorConsola.pausar();
                }
                case 0 -> { }
                default -> throw new IllegalStateException("Opción inesperada");
            }
        } while (opcion != 0);
    }

    private void menuExportaciones() {
        int opcion;
        do {
            System.out.println("\n---------- EXPORTAR PRODUCTOS ----------");
            System.out.println("1. Exportar productos a JSON");
            System.out.println("2. Exportar productos a XML");
            System.out.println("0. Volver al menú principal");
            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 2);
            switch (opcion) {
                case 1 -> pendiente("RF-FIC-05 · Exportar JSON");
                case 2 -> pendiente("RF-FIC-05 · Exportar XML");
                case 0 -> { }
                default -> throw new IllegalStateException("Opción inesperada");
            }
        } while (opcion != 0);
    }

    private void menuSerializacion() {
        int opcion;
        do {
            System.out.println("\n---------- GUARDAR Y RECUPERAR INVENTARIO ----------");
            System.out.println("1. Guardar inventario en fichero .dat");
            System.out.println("2. Recuperar inventario desde fichero .dat");
            System.out.println("0. Volver al menú principal");
            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 2);
            switch (opcion) {
                case 1 -> pendiente("RF-FIC-06 · Guardar inventario");
                case 2 -> pendiente("RF-FIC-06 · Recuperar inventario");
                case 0 -> { }
                default -> throw new IllegalStateException("Opción inesperada");
            }
        } while (opcion != 0);
    }

    private void ejecutarExploracion() {
        String ruta = LectorConsola.leerTexto("\nIntroduce la ruta a explorar: ");
        exploradorServicio.explorar(ruta);
        LectorConsola.pausar();
    }

    private void ejecutarImportacionJson() {
        String ruta = LectorConsola.leerTexto("\nIntroduce la ruta del fichero JSON: ");
        importadorJsonServicio.importar(ruta, inventario);
        LectorConsola.pausar();
    }

    /** Sustituir las llamadas por servicios propios a medida que se implementen los RF. */
    private void pendiente(String requisito) {
        System.out.println(ColoresConsola.pintar("[PENDIENTE] " + requisito, ColoresConsola.AMARILLO));
        System.out.println("Esta funcionalidad debe desarrollarla el alumnado.");
        LectorConsola.pausar();
    }
}
