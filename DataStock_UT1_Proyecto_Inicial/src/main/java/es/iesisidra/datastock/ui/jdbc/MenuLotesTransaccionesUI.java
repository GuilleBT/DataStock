package es.iesisidra.datastock.ui.jdbc;

import es.iesisidra.datastock.jdbc.ImportadorMemoriaMysqlServicio;
import es.iesisidra.datastock.jdbc.TransaccionDemoServicio;
import es.iesisidra.datastock.servicio.Inventario;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;

/**
 * Submenú interactivo para operaciones por lotes y demostraciones de transacciones JDBC.
 */
public class MenuLotesTransaccionesUI {

    private final Inventario inventario;
    private final ImportadorMemoriaMysqlServicio importadorMemoriaServicio = new ImportadorMemoriaMysqlServicio();
    private final TransaccionDemoServicio transaccionDemoServicio = new TransaccionDemoServicio();

    public MenuLotesTransaccionesUI(Inventario inventario) {
        this.inventario = inventario;
    }

    /**
     * Ejecuta el proceso interactivo de importación por lotes de productos en memoria a MySQL.
     */
    public void ejecutarImportacionMemoriaMysql() {
        System.out.println("\nIMPORTAR PRODUCTOS DE MEMORIA A MYSQL");
        System.out.println("-----------------------------------------------");
        int totalMemoria = inventario.getProductos().size();
        System.out.println("Productos disponibles en memoria: " + totalMemoria);

        if (totalMemoria == 0) {
            System.out.println(ColoresConsola.pintar("[AVISO] No hay productos cargados en memoria para importar.", ColoresConsola.AMARILLO));
            System.out.println("Carga primero un fichero JSON o XML desde las opciones 2 o 3 del menú principal.");
            LectorConsola.pausar();
            return;
        }

        if (!LectorConsola.confirmar(ColoresConsola.pintar("[AVISO] ¿Deseas importar estos productos a MySQL?", ColoresConsola.AMARILLO))) {
            System.out.println("Importación cancelada por el usuario.");
            LectorConsola.pausar();
            return;
        }

        ImportadorMemoriaMysqlServicio.ResumenImportacion resumen = importadorMemoriaServicio.importar(inventario);

        System.out.println("\n-----------------------------------------------");
        System.out.println("RESULTADO DE LA IMPORTACIÓN");
        System.out.println("-----------------------------------------------");
        System.out.println("Productos analizados:            " + resumen.analizados());
        System.out.println("Productos insertados:            " + resumen.insertados());
        System.out.println("Omitidos por código duplicado:   " + resumen.omitidosDuplicados());
        System.out.println("Rechazados por datos incorrectos:" + resumen.rechazadosErrores());
        System.out.println("-----------------------------------------------");

        if (resumen.exitoTransaccion()) {
            if (resumen.insertados() > 0) {
                System.out.println(ColoresConsola.pintar("[OK] Importación finalizada. Los productos válidos se han guardado en MySQL.", ColoresConsola.VERDE));
            } else {
                System.out.println(ColoresConsola.pintar("[AVISO] Importación finalizada. No se insertó ningún producto nuevo.", ColoresConsola.AMARILLO));
            }
        } else {
            System.out.println(ColoresConsola.pintar("[ERROR] Se produjo un error durante la importación. La transacción se revirtió sin cambios parciales.", ColoresConsola.ROJO));
        }

        LectorConsola.pausar();
    }

    /**
     * Ejecuta la demostración didáctica de control transaccional (commit y rollback).
     */
    public void ejecutarDemostracionTransacciones() {
        System.out.println("\nDEMOSTRACIÓN DE CONTROL DE TRANSACCIONES JDBC");
        System.out.println("-----------------------------------------------");
        System.out.println("Esta opción ejecutará pruebas técnicas de commit() y rollback()");
        System.out.println("utilizando categorías de prueba temporales.");

        if (!LectorConsola.confirmar(ColoresConsola.pintar("[AVISO] ¿Deseas iniciar la demostración?", ColoresConsola.AMARILLO))) {
            System.out.println("Demostración cancelada.");
            LectorConsola.pausar();
            return;
        }

        transaccionDemoServicio.ejecutarDemostracion();
        LectorConsola.pausar();
    }
}
