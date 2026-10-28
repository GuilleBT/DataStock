package es.iesisidra.datastock.servicio;

import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.util.ColoresConsola;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Servicio encargado de las consultas y búsquedas de productos en memoria (RF-FIC-04).
 * <p>Trabaja directamente sobre la colección compartida de objetos Producto sin tocar disco.
 * Incluye ordenación por código, búsqueda exacta por código y búsqueda parcial por nombre.</p>
 */
public class ConsultaProductoServicio {

    /**
     * Muestra el listado de todos los productos disponibles en memoria, ordenados por código.
     *
     * @param productos Lista de productos actual en el inventario.
     */
    public void listarTodos(List<Producto> productos) {
        System.out.println(ColoresConsola.pintar("\n===== LISTADO DE PRODUCTOS =====", ColoresConsola.CIAN));

        if (productos == null || productos.isEmpty()) {
            System.out.println(ColoresConsola.pintar("El inventario está vacío. No hay productos registrados.", ColoresConsola.AMARILLO));
            return;
        }

        // Creamos una copia de la lista para ordenarla sin alterar el orden original de la memoria
        List<Producto> listaOrdenada = new ArrayList<>(productos);
        listaOrdenada.sort(Comparator.comparing(
                p -> p.getCodigo() != null ? p.getCodigo().toUpperCase() : ""
        ));

        System.out.println("Total de productos en memoria: " + listaOrdenada.size());
        System.out.println(ColoresConsola.pintar("------------------------------------------------", ColoresConsola.GRIS));

        for (Producto p : listaOrdenada) {
            mostrarFichaProducto(p);
        }
    }

    /**
     * Busca un producto por su código exacto (ignorando mayúsculas y espacios en blanco).
     *
     * @param codigoBuscado Código introducido por el usuario.
     * @param productos      Lista de productos donde realizar la búsqueda.
     */
    public void buscarPorCodigo(String codigoBuscado, List<Producto> productos) {
        System.out.println(ColoresConsola.pintar("\n===== BÚSQUEDA POR CÓDIGO =====", ColoresConsola.CIAN));

        if (codigoBuscado == null || codigoBuscado.trim().isEmpty()) {
            System.out.println(ColoresConsola.pintar("Error: Debes introducir un código para buscar.", ColoresConsola.ROJO));
            return;
        }

        if (productos == null || productos.isEmpty()) {
            System.out.println(ColoresConsola.pintar("El inventario está vacío. No hay productos para buscar.", ColoresConsola.AMARILLO));
            return;
        }

        String codigoNormalizado = codigoBuscado.trim().toUpperCase();
        Producto productoEncontrado = null;

        for (Producto p : productos) {
            if (p.getCodigo() != null && p.getCodigo().equalsIgnoreCase(codigoNormalizado)) {
                productoEncontrado = p;
                break; // El código es único, podemos detener la búsqueda en cuanto lo encontramos
            }
        }

        if (productoEncontrado != null) {
            System.out.println(ColoresConsola.pintar("Producto encontrado:", ColoresConsola.VERDE));
            mostrarFichaProducto(productoEncontrado);
        } else {
            System.out.println(ColoresConsola.pintar("No se encontró ningún producto con el código: " + codigoBuscado, ColoresConsola.AMARILLO));
        }
    }

    /**
     * Busca productos cuyo nombre contenga el fragmento de texto introducido (case-insensitive).
     *
     * @param textoBuscado Fragmento de texto a buscar dentro del nombre.
     * @param productos    Lista de productos donde buscar.
     */
    public void buscarPorNombre(String textoBuscado, List<Producto> productos) {
        System.out.println(ColoresConsola.pintar("\n===== BÚSQUEDA POR NOMBRE =====", ColoresConsola.CIAN));

        if (textoBuscado == null || textoBuscado.trim().isEmpty()) {
            System.out.println(ColoresConsola.pintar("Error: Debes introducir un texto para buscar.", ColoresConsola.ROJO));
            return;
        }

        if (productos == null || productos.isEmpty()) {
            System.out.println(ColoresConsola.pintar("El inventario está vacío. No hay productos para buscar.", ColoresConsola.AMARILLO));
            return;
        }

        String textoNormalizado = textoBuscado.trim().toLowerCase();
        List<Producto> coincidencias = new ArrayList<>();

        for (Producto p : productos) {
            if (p.getNombre() != null && p.getNombre().toLowerCase().contains(textoNormalizado)) {
                coincidencias.add(p);
            }
        }

        if (coincidencias.isEmpty()) {
            System.out.println(ColoresConsola.pintar("No se encontraron productos cuyo nombre contenga: \"" + textoBuscado + "\"", ColoresConsola.AMARILLO));
        } else {
            System.out.println("Productos encontrados (" + coincidencias.size() + "):");
            System.out.println(ColoresConsola.pintar("------------------------------------------------", ColoresConsola.GRIS));
            for (Producto p : coincidencias) {
                mostrarFichaProducto(p);
            }
        }
    }

    /**
     * Imprime los datos formateados de un producto de manera clara y legible en consola.
     */
    private void mostrarFichaProducto(Producto p) {
        System.out.println("Código: " + ColoresConsola.pintar(p.getCodigo(), ColoresConsola.NEGRITA));
        System.out.println("Nombre: " + p.getNombre());
        System.out.printf("Precio: %.2f € | Stock: %d%n", p.getPrecio(), p.getStock());
        System.out.printf("Categoría: %d | Proveedor: %d%n", p.getCategoriaId(), p.getProveedorId());
        System.out.println(ColoresConsola.pintar("------------------------------------------------", ColoresConsola.GRIS));
    }
}
