package es.iesisidra.datastock.servicio;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Servicio encargado de exportar los productos en memoria a ficheros JSON y XML (RF-FIC-05).
 * <p>Exporta la totalidad de los artículos ordenados por código en codificación UTF-8,
 * comprobando que el directorio de destino exista y solicitando confirmación expresa
 * al usuario antes de sobrescribir cualquier fichero preexistente.</p>
 */
public class ExportadorInventarioServicio {

    /**
     * Exporta el catálogo completo a un archivo JSON utilizando la biblioteca Gson con formato legible.
     *
     * @param rutaTexto  Ruta destino introducida por el usuario.
     * @param inventario Colección compartida de productos en memoria.
     */
    public void exportarJson(String rutaTexto, Inventario inventario) {
        if (!validarInventario(inventario)) {
            return;
        }

        Path ruta = prepararRutaDestino(rutaTexto);
        if (ruta == null) {
            return; // Ruta inválida o usuario canceló sobrescritura
        }

        // 1. Obtenemos una copia defensiva ordenada por código para no alterar la colección en memoria
        List<Producto> listaOrdenada = obtenerProductosOrdenados(inventario);

        try {
            // 2. Construimos la estructura JSON requerida: un objeto raíz con la clave "productos"
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject raizJson = new JsonObject();
            raizJson.add("productos", gson.toJsonTree(listaOrdenada));

            // 3. Escribimos en disco asegurando codificación UTF-8 mediante NIO.2
            try (BufferedWriter escritor = Files.newBufferedWriter(ruta, StandardCharsets.UTF_8)) {
                gson.toJson(raizJson, escritor);
            }

            // 4. Mostramos el resumen de éxito con la información solicitada
            mostrarResumenExito(ruta, "JSON", listaOrdenada.size());

        } catch (IOException e) {
            System.out.println(ColoresConsola.pintar("Error de escritura al generar el fichero JSON: " + e.getMessage(), ColoresConsola.ROJO));
        }
    }

    /**
     * Exporta el catálogo completo a un archivo XML utilizando la API DOM y formato indentado.
     *
     * @param rutaTexto  Ruta destino introducida por el usuario.
     * @param inventario Colección compartida de productos en memoria.
     */
    public void exportarXml(String rutaTexto, Inventario inventario) {
        if (!validarInventario(inventario)) {
            return;
        }

        Path ruta = prepararRutaDestino(rutaTexto);
        if (ruta == null) {
            return; // Ruta inválida o usuario canceló sobrescritura
        }

        // 1. Obtenemos una copia defensiva ordenada por código
        List<Producto> listaOrdenada = obtenerProductosOrdenados(inventario);

        try {
            // 2. Creamos un nuevo documento XML en memoria con la API DOM
            DocumentBuilderFactory factoria = DocumentBuilderFactory.newInstance();
            DocumentBuilder constructor = factoria.newDocumentBuilder();
            Document docXml = constructor.newDocument();

            // Elemento raíz: <catalogo>
            Element elementoRaiz = docXml.createElement("catalogo");
            docXml.appendChild(elementoRaiz);

            // 3. Añadimos cada producto como un nodo hijo <producto> con sus etiquetas de datos
            for (Producto producto : listaOrdenada) {
                Element elemProducto = docXml.createElement("producto");

                crearEtiquetaTexto(docXml, elemProducto, "codigo", producto.getCodigo());
                crearEtiquetaTexto(docXml, elemProducto, "nombre", producto.getNombre());
                crearEtiquetaTexto(docXml, elemProducto, "precio", producto.getPrecio() != null ? producto.getPrecio().toPlainString() : "0.00");
                crearEtiquetaTexto(docXml, elemProducto, "stock", String.valueOf(producto.getStock()));
                crearEtiquetaTexto(docXml, elemProducto, "categoriaId", String.valueOf(producto.getCategoriaId()));
                crearEtiquetaTexto(docXml, elemProducto, "proveedorId", String.valueOf(producto.getProveedorId()));

                elementoRaiz.appendChild(elemProducto);
            }

            // 4. Configuramos el transformador XSLT para volcar el árbol DOM a disco con sangría UTF-8
            TransformerFactory factoriaTransformador = TransformerFactory.newInstance();
            Transformer transformador = factoriaTransformador.newTransformer();
            transformador.setOutputProperty(OutputKeys.INDENT, "yes");
            transformador.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformador.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            DOMSource origenDom = new DOMSource(docXml);
            try (BufferedWriter escritor = Files.newBufferedWriter(ruta, StandardCharsets.UTF_8)) {
                StreamResult destinoFichero = new StreamResult(escritor);
                transformador.transform(origenDom, destinoFichero);
            }

            // 5. Mostramos el resumen de éxito
            mostrarResumenExito(ruta, "XML", listaOrdenada.size());

        } catch (ParserConfigurationException e) {
            System.out.println(ColoresConsola.pintar("Error al configurar el generador DOM de XML: " + e.getMessage(), ColoresConsola.ROJO));
        } catch (TransformerException e) {
            System.out.println(ColoresConsola.pintar("Error al transformar el documento DOM a texto XML: " + e.getMessage(), ColoresConsola.ROJO));
        } catch (IOException e) {
            System.out.println(ColoresConsola.pintar("Error de escritura al generar el fichero XML: " + e.getMessage(), ColoresConsola.ROJO));
        }
    }

    /**
     * Comprueba si el inventario contiene al menos un producto para exportar.
     */
    private boolean validarInventario(Inventario inventario) {
        if (inventario == null || inventario.getProductos() == null || inventario.getProductos().isEmpty()) {
            System.out.println(ColoresConsola.pintar("El inventario está vacío. No hay productos para exportar.", ColoresConsola.AMARILLO));
            return false;
        }
        return true;
    }

    /**
     * Valida la ruta destino, verifica que el directorio padre exista y pide confirmación si el archivo ya existe.
     * Devuelve el objeto {@link Path} si la ruta es válida y confirmada, o {@code null} en caso de error o cancelación.
     */
    private Path prepararRutaDestino(String rutaTexto) {
        if (rutaTexto == null || rutaTexto.trim().isEmpty()) {
            System.out.println(ColoresConsola.pintar("Error: La ruta de destino no puede estar vacía.", ColoresConsola.ROJO));
            return null;
        }

        Path ruta = Paths.get(rutaTexto.trim());

        // Verificamos que el directorio contenedor exista
        Path directorioPadre = ruta.getParent();
        if (directorioPadre != null && Files.notExists(directorioPadre)) {
            System.out.println(ColoresConsola.pintar("Error: El directorio de destino no existe: " + directorioPadre, ColoresConsola.ROJO));
            return null;
        }

        // Si el archivo ya existe en disco, solicitamos confirmación para sobrescribir (Caso CP-EXP-03)
        if (Files.exists(ruta)) {
            boolean deseaSobrescribir = LectorConsola.confirmar("El fichero '" + ruta.getFileName() + "' ya existe. ¿Deseas sobrescribirlo?");
            if (!deseaSobrescribir) {
                System.out.println(ColoresConsola.pintar("Operación cancelada. El fichero original no se ha modificado.", ColoresConsola.AMARILLO));
                return null;
            }
        }

        return ruta;
    }

    /**
     * Crea una copia de la lista de productos y la ordena alfabéticamente por código.
     */
    private List<Producto> obtenerProductosOrdenados(Inventario inventario) {
        List<Producto> copia = new ArrayList<>(inventario.getProductos());
        copia.sort(Comparator.comparing(Producto::getCodigo, String.CASE_INSENSITIVE_ORDER));
        return copia;
    }

    /**
     * Crea un elemento hijo con un nodo de texto y lo añade al elemento padre XML.
     */
    private void crearEtiquetaTexto(Document doc, Element padre, String nombreEtiqueta, String contenido) {
        Element etiqueta = doc.createElement(nombreEtiqueta);
        etiqueta.setTextContent(contenido != null ? contenido : "");
        padre.appendChild(etiqueta);
    }

    /**
     * Muestra en pantalla el resumen de la exportación según el formato del enunciado.
     */
    private void mostrarResumenExito(Path ruta, String formato, int cantidadProductos) {
        System.out.println(ColoresConsola.pintar("Fichero generado correctamente.", ColoresConsola.VERDE));
        System.out.println("Ruta: " + ruta.toAbsolutePath());
        System.out.println("Formato: " + formato);
        System.out.println("Productos exportados: " + cantidadProductos);
    }
}
