package es.iesisidra.datastock.ui.jdbc;

import es.iesisidra.datastock.jdbc.MovimientoStockDAO;
import es.iesisidra.datastock.jdbc.ProductoDAO;
import es.iesisidra.datastock.modelo.MovimientoStock;
import es.iesisidra.datastock.modelo.ProductoDetalleDTO;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Submenú interactivo para el registro y consulta de movimientos de stock en MySQL.
 */
public class MenuMovimientosUI {

    private final MovimientoStockDAO movimientoDAO = new MovimientoStockDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Muestra y gestiona el submenú de registro de movimientos (Entrada / Salida).
     */
    public void iniciarMovimientos() {
        int opcion;
        do {
            System.out.println("\n---------- MOVIMIENTOS DE STOCK (PROCEDIMIENTO ALMACENADO) ----------");
            System.out.println("1. Registrar una ENTRADA de stock");
            System.out.println("2. Registrar una SALIDA de stock");
            System.out.println("0. Volver al menú MySQL");

            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 2);
            switch (opcion) {
                case 1 -> registrarMovimientoStock("ENTRADA");
                case 2 -> registrarMovimientoStock("SALIDA");
                case 0 -> { }
                default -> throw new IllegalStateException("Opción inesperada");
            }
        } while (opcion != 0);
    }

    /**
     * Muestra y gestiona el submenú de consulta de historial de movimientos.
     */
    public void iniciarHistorial() {
        int opcion;
        do {
            System.out.println("\n---------- HISTORIAL DE MOVIMIENTOS ----------");
            System.out.println("1. Consultar todos los movimientos");
            System.out.println("2. Consultar movimientos de un producto");
            System.out.println("0. Volver al menú MySQL");

            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 2);
            switch (opcion) {
                case 1 -> consultarTodosLosMovimientos();
                case 2 -> consultarMovimientosPorProducto();
                case 0 -> { }
                default -> throw new IllegalStateException("Opción inesperada");
            }
        } while (opcion != 0);
    }

    private void registrarMovimientoStock(String tipo) {
        System.out.println("\nREGISTRAR " + tipo + " DE STOCK");
        System.out.println("---------------------------------------");
        String codigo = LectorConsola.leerTexto("Código del producto: ").trim().toUpperCase();
        try {
            ProductoDetalleDTO prod = productoDAO.buscarPorCodigo(codigo);
            if (prod == null) {
                System.out.println(ColoresConsola.pintar("[ERROR] No existe el producto con código '" + codigo + "' en la base de datos.", ColoresConsola.ROJO));
                LectorConsola.pausar();
                return;
            }

            System.out.println("Producto:     " + prod.getCodigo() + " - " + prod.getNombre());
            System.out.println("Stock actual: " + prod.getStock() + " unidades");

            int cantidad = LectorConsola.leerEntero("Cantidad de unidades (" + tipo + "): ");
            if (cantidad <= 0) {
                System.out.println(ColoresConsola.pintar("[ERROR] La cantidad debe ser un entero estrictamente positivo.", ColoresConsola.ROJO));
                LectorConsola.pausar();
                return;
            }

            if (!LectorConsola.confirmar(ColoresConsola.pintar("[AVISO] ¿Confirmas registrar la " + tipo + " de " + cantidad + " unidades?", ColoresConsola.AMARILLO))) {
                System.out.println("Operación cancelada por el usuario.");
                LectorConsola.pausar();
                return;
            }

            MovimientoStockDAO.ResultadoMovimiento resultado = movimientoDAO.registrarMovimiento(codigo, tipo, cantidad);
            if (resultado != null) {
                System.out.println(ColoresConsola.pintar("[OK] Movimiento registrado correctamente.", ColoresConsola.VERDE));
                System.out.println("ID Movimiento:   " + resultado.movimientoId());
                System.out.println("Tipo:            " + resultado.tipo());
                System.out.println("Cantidad:        " + resultado.cantidad());
                System.out.println("Stock anterior:  " + resultado.stockAnterior() + " -> Stock posterior: " + resultado.stockPosterior());
                System.out.println("Fecha y hora:    " + resultado.fecha().format(FORMATO_FECHA));
            } else {
                System.out.println(ColoresConsola.pintar("[ERROR] No se pudo recuperar el resultado del movimiento.", ColoresConsola.ROJO));
            }

        } catch (Exception ex) {
            String mensaje = ex.getMessage();
            if (mensaje != null && mensaje.toLowerCase().contains("stock insuficiente")) {
                System.out.println(ColoresConsola.pintar("[ERROR] Stock insuficiente. La salida no puede superar las existencias actuales.", ColoresConsola.ROJO));
            } else if (mensaje != null && mensaje.toLowerCase().contains("producto inexistente")) {
                System.out.println(ColoresConsola.pintar("[ERROR] Producto inexistente en la base de datos.", ColoresConsola.ROJO));
            } else {
                System.out.println(ColoresConsola.pintar("[ERROR] Error al registrar movimiento: " + mensaje, ColoresConsola.ROJO));
            }
        }
        LectorConsola.pausar();
    }

    private void consultarTodosLosMovimientos() {
        System.out.println("\nHISTORIAL COMPLETO DE MOVIMIENTOS DE STOCK");
        System.out.println("------------------------------------------------------------------------------------------------------");
        try {
            List<MovimientoStock> movimientos = movimientoDAO.consultarTodos();
            if (movimientos.isEmpty()) {
                System.out.println(ColoresConsola.pintar("[AVISO] No hay movimientos registrados en la base de datos.", ColoresConsola.AMARILLO));
            } else {
                mostrarTablaMovimientos(movimientos);
                System.out.println("Total de movimientos: " + movimientos.size());
            }
        } catch (Exception ex) {
            System.out.println(ColoresConsola.pintar("[ERROR] Error al consultar el historial: " + ex.getMessage(), ColoresConsola.ROJO));
        }
        LectorConsola.pausar();
    }

    private void consultarMovimientosPorProducto() {
        System.out.println("\nHISTORIAL DE MOVIMIENTOS POR PRODUCTO");
        System.out.println("---------------------------------------");
        String codigo = LectorConsola.leerTexto("Introduce el código del producto: ").trim().toUpperCase();
        try {
            if (!productoDAO.existePorCodigo(codigo)) {
                System.out.println(ColoresConsola.pintar("[ERROR] El producto con código '" + codigo + "' no existe en la base de datos.", ColoresConsola.ROJO));
                LectorConsola.pausar();
                return;
            }

            List<MovimientoStock> movimientos = movimientoDAO.consultarPorProducto(codigo);
            if (movimientos.isEmpty()) {
                System.out.println(ColoresConsola.pintar("[AVISO] Sin movimientos registrados para el producto " + codigo + ".", ColoresConsola.AMARILLO));
            } else {
                mostrarTablaMovimientos(movimientos);
                System.out.println("Total de movimientos del producto " + codigo + ": " + movimientos.size());
            }
        } catch (Exception ex) {
            System.out.println(ColoresConsola.pintar("[ERROR] Error al consultar movimientos del producto: " + ex.getMessage(), ColoresConsola.ROJO));
        }
        LectorConsola.pausar();
    }

    private void mostrarTablaMovimientos(List<MovimientoStock> movimientos) {
        System.out.printf("%-6s %-20s %-12s %-9s %-9s %-15s %-15s%n",
                "ID", "FECHA Y HORA", "CÓDIGO", "TIPO", "CANTIDAD", "STOCK ANTERIOR", "STOCK POSTERIOR");
        System.out.println("------------------------------------------------------------------------------------------------------");
        for (MovimientoStock m : movimientos) {
            String strFecha = m.getFecha() != null ? m.getFecha().format(FORMATO_FECHA) : "—";
            System.out.printf("%-6d %-20s %-12s %-9s %-9d %-15d %-15d%n",
                    m.getId(),
                    strFecha,
                    m.getProductoCodigo(),
                    m.getTipo(),
                    m.getCantidad(),
                    m.getStockAnterior(),
                    m.getStockPosterior());
        }
        System.out.println("------------------------------------------------------------------------------------------------------");
    }
}
