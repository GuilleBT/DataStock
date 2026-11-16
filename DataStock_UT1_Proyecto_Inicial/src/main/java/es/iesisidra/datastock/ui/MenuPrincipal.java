package es.iesisidra.datastock.ui;

import es.iesisidra.datastock.componentes.LectorJsonException;
import es.iesisidra.datastock.componentes.LectorProductosJson;
import es.iesisidra.datastock.componentes.ResultadoLecturaJson;
import es.iesisidra.datastock.servicio.ConsultaProductoServicio;
import es.iesisidra.datastock.servicio.ExportadorInventarioServicio;
import es.iesisidra.datastock.servicio.SerializacionServicio;
import es.iesisidra.datastock.servicio.ExploradorFicherosServicio;
import es.iesisidra.datastock.servicio.ImportadorJsonServicio;
import es.iesisidra.datastock.servicio.ImportadorXmlServicio;
import es.iesisidra.datastock.servicio.Inventario;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Presentación de menús y navegación de la primera fase de DataStock.
 * <p>Los métodos marcados TODO son puntos de partida, NO soluciones de los
 * requisitos. El alumnado deberá delegar el acceso a ficheros, la validación
 * y el procesamiento en métodos o clases independientes.</p>
 */
public class MenuPrincipal {
    private final Inventario inventario;
    private final ExploradorFicherosServicio exploradorServicio = new ExploradorFicherosServicio();
    private final LectorProductosJson lectorProductosJson = new LectorProductosJson();
    private final ImportadorJsonServicio importadorJsonServicio = new ImportadorJsonServicio();
    private final ImportadorXmlServicio importadorXmlServicio = new ImportadorXmlServicio();
    private final ConsultaProductoServicio consultaServicio = new ConsultaProductoServicio();
    private final ExportadorInventarioServicio exportadorServicio = new ExportadorInventarioServicio();
    private final SerializacionServicio serializacionServicio = new SerializacionServicio();

    /** @param inventario colección común de productos de esta ejecución. */
    public MenuPrincipal(Inventario inventario) { this.inventario = inventario; }

    /** Ejecuta el menú principal hasta que el usuario selecciona salir. */
    public void iniciar() {
        int opcion;
        do {
            mostrarCabecera();
            System.out.println("1. Explorar ficheros y directorios (UT1)");
            System.out.println("2. Importar productos desde JSON (UT1)");
            System.out.println("3. Importar productos desde XML (UT1)");
            System.out.println("4. Consultar productos en memoria (UT1)");
            System.out.println("5. Exportar productos (UT1)");
            System.out.println("6. Guardar y recuperar inventario (UT1)");
            System.out.println("7. Gestionar base de datos MySQL (UT2)");
            System.out.println("0. Salir");
            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 7);
            switch (opcion) {
                case 1 -> ejecutarExploracion();
                case 2 -> ejecutarImportacionJson();
                case 3 -> ejecutarImportacionXml();
                case 4 -> menuConsultas();
                case 5 -> menuExportaciones();
                case 6 -> menuSerializacion();
                case 7 -> new MenuJdbc(inventario).iniciar();
                case 0 -> System.out.println(ColoresConsola.pintar("Hasta pronto.", ColoresConsola.VERDE));
                default -> throw new IllegalStateException("Opción inesperada");
            }
        } while (opcion != 0);
    }

    private void mostrarCabecera() {
        System.out.println(ColoresConsola.pintar("\n================================================", ColoresConsola.CIAN));
        System.out.println(ColoresConsola.pintar("         DATASTOCK - GESTIÓN DE INVENTARIO", ColoresConsola.NEGRITA));
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
                case 1 -> {
                    String ruta = LectorConsola.leerTexto("\nIntroduce la ruta del fichero JSON de destino: ");
                    exportadorServicio.exportarJson(ruta, inventario);
                    LectorConsola.pausar();
                }
                case 2 -> {
                    String ruta = LectorConsola.leerTexto("\nIntroduce la ruta del fichero XML de destino: ");
                    exportadorServicio.exportarXml(ruta, inventario);
                    LectorConsola.pausar();
                }
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
                case 1 -> {
                    String ruta = LectorConsola.leerTexto("\nIntroduce la ruta del fichero .dat para guardar: ");
                    serializacionServicio.guardar(ruta, inventario);
                    LectorConsola.pausar();
                }
                case 2 -> {
                    String ruta = LectorConsola.leerTexto("\nIntroduce la ruta del fichero .dat para recuperar: ");
                    serializacionServicio.recuperar(ruta, inventario);
                    LectorConsola.pausar();
                }
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
        System.out.println(ColoresConsola.pintar("\n===== IMPORTAR PRODUCTOS DESDE JSON (COMPONENTE UT3) =====", ColoresConsola.CIAN));
        String rutaTexto = LectorConsola.leerTexto("Introduce la ruta del fichero JSON: ");
        if (rutaTexto == null || rutaTexto.trim().isEmpty()) {
            System.out.println(ColoresConsola.pintar("[ERROR] La ruta del fichero no puede estar vacía.", ColoresConsola.ROJO));
            LectorConsola.pausar();
            return;
        }

        try {
            Path ruta = Paths.get(rutaTexto.trim());
            // Invocación al componente LectorProductosJson (UT3)
            ResultadoLecturaJson resultado = lectorProductosJson.leerDetallado(ruta, inventario.getProductos());

            // El menú añade al inventario los productos válidos devueltos por el componente
            inventario.getProductos().addAll(resultado.getProductosValidos());

            System.out.println("Ruta procesada: " + rutaTexto.trim());
            System.out.println("Productos encontrados: " + resultado.getTotalEncontrados());
            System.out.println("Importados: " + resultado.getProductosValidos().size() + " | Rechazados: " + resultado.getRechazados());

            if (resultado.tieneIncidencias()) {
                System.out.println(ColoresConsola.pintar("INCIDENCIAS:", ColoresConsola.AMARILLO));
                for (String inc : resultado.getIncidencias()) {
                    System.out.println(" - " + inc);
                }
                System.out.println(ColoresConsola.pintar("[AVISO] Importación finalizada con incidencias.", ColoresConsola.AMARILLO));
            }

            System.out.println(ColoresConsola.pintar("[OK] Se han importado " + resultado.getProductosValidos().size() + " productos válidos en el inventario.", ColoresConsola.VERDE));

        } catch (LectorJsonException ex) {
            // Error controlado en rojo sin abortar la aplicación (CP-COMP-02)
            System.out.println(ColoresConsola.pintar("[ERROR] " + ex.getMessage(), ColoresConsola.ROJO));
        } catch (Exception ex) {
            System.out.println(ColoresConsola.pintar("[ERROR] Error inesperado al procesar el fichero: " + ex.getMessage(), ColoresConsola.ROJO));
        }

        LectorConsola.pausar();
    }

    private void ejecutarImportacionXml() {
        String ruta = LectorConsola.leerTexto("\nIntroduce la ruta del fichero XML: ");
        importadorXmlServicio.importar(ruta, inventario);
        LectorConsola.pausar();
    }

    /** Sustituir las llamadas por servicios propios a medida que se implementen los RF. */
    private void pendiente(String requisito) {
        System.out.println(ColoresConsola.pintar("[PENDIENTE] " + requisito, ColoresConsola.AMARILLO));
        System.out.println("Esta funcionalidad debe desarrollarla el alumnado.");
        LectorConsola.pausar();
    }
}
