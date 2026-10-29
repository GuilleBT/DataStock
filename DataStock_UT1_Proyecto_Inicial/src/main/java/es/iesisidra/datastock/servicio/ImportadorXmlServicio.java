package es.iesisidra.datastock.servicio;

import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.util.ColoresConsola;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Servicio encargado de importar catálogos de productos en formato XML utilizando la API DOM (RF-FIC-03).
 * <p>Aplica una configuración estricta para proteger la aplicación frente a vulnerabilidades XXE
 * (XML External Entity), deshabilita el procesamiento de DTDs y reutiliza las reglas de validación
 * de {@link ValidadorProducto} para mantener coherencia total con la importación JSON.</p>
 */
public class ImportadorXmlServicio {

    /**
     * Importa productos desde un archivo XML hacia la colección compartida del inventario.
     *
     * @param rutaTexto  Ruta al archivo XML introducida por el usuario.
     * @param inventario El inventario común en memoria donde se añadirán los productos válidos.
     */
    public void importar(String rutaTexto, Inventario inventario) {
        if (rutaTexto == null || rutaTexto.trim().isEmpty()) {
            System.out.println(ColoresConsola.pintar("Error: La ruta del fichero XML no puede estar vacía.", ColoresConsola.ROJO));
            return;
        }

        Path ruta = Paths.get(rutaTexto.trim());

        // 1. Validaciones previas de la ruta con NIO.2
        if (Files.notExists(ruta)) {
            System.out.println(ColoresConsola.pintar("Error: El fichero no existe: " + rutaTexto, ColoresConsola.ROJO));
            return;
        }
        if (!Files.isRegularFile(ruta)) {
            System.out.println(ColoresConsola.pintar("Error: La ruta no corresponde a un fichero regular: " + rutaTexto, ColoresConsola.ROJO));
            return;
        }
        if (!Files.isReadable(ruta)) {
            System.out.println(ColoresConsola.pintar("Error: No tienes permisos de lectura sobre el fichero: " + rutaTexto, ColoresConsola.ROJO));
            return;
        }

        System.out.println(ColoresConsola.pintar("\n===== IMPORTAR XML =====", ColoresConsola.CIAN));
        System.out.println("Ruta: " + rutaTexto);
        System.out.println("Leyendo documento XML...");

        // 2. Configuración segura del analizador DOM (Protección frente a ataques XXE y rechazo de DTD)
        DocumentBuilderFactory factoria = DocumentBuilderFactory.newInstance();
        try {
            // Deshabilitamos la inclusión de DOCTYPE/DTD para neutralizar ataques XXE (Caso CP-XML-03)
            factoria.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factoria.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factoria.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factoria.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factoria.setXIncludeAware(false);
            factoria.setExpandEntityReferences(false);
            factoria.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factoria.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        } catch (ParserConfigurationException e) {
            // Si la JVM no soporta alguna propiedad de seguridad, avisamos pero continuamos
            System.out.println(ColoresConsola.pintar("Aviso de seguridad al configurar el parser XML: " + e.getMessage(), ColoresConsola.AMARILLO));
        }

        Document documento;
        try {
            DocumentBuilder constructor = factoria.newDocumentBuilder();
            documento = constructor.parse(ruta.toFile());
            // Normalizamos el árbol DOM eliminando nodos vacíos y unificando textos adyacentes
            documento.getDocumentElement().normalize();
        } catch (SAXException e) {
            // Si el XML está mal formado o incluye un DOCTYPE prohibido (XXE)
            System.out.println(ColoresConsola.pintar("Error: El documento XML no es válido o incluye declaraciones DTD no permitidas.", ColoresConsola.ROJO));
            System.out.println("Detalle del error: " + e.getMessage());
            System.out.println("No se ha importado ningún producto y la memoria permanece intacta.");
            return;
        } catch (IOException e) {
            System.out.println(ColoresConsola.pintar("Error de entrada/salida al acceder al fichero XML: " + e.getMessage(), ColoresConsola.ROJO));
            return;
        } catch (ParserConfigurationException e) {
            System.out.println(ColoresConsola.pintar("Error al inicializar el analizador DOM: " + e.getMessage(), ColoresConsola.ROJO));
            return;
        }

        // 3. Comprobamos que el elemento raíz del XML sea exactamente <catalogo>
        Element elementoRaiz = documento.getDocumentElement();
        if (elementoRaiz == null || !"catalogo".equalsIgnoreCase(elementoRaiz.getTagName())) {
            System.out.println(ColoresConsola.pintar("Error: La raíz del documento XML debe ser '<catalogo>'. Importación cancelada.", ColoresConsola.ROJO));
            return;
        }

        // 4. Obtenemos todos los elementos <producto> del documento
        NodeList listaNodosProducto = elementoRaiz.getElementsByTagName("producto");
        int encontrados = listaNodosProducto.getLength();
        int importados = 0;
        int rechazados = 0;

        List<String> incidencias = new ArrayList<>();
        Set<String> codigosEnEsteFichero = new HashSet<>();
        List<Producto> productosValidosParaAnadir = new ArrayList<>();

        // 5. Recorremos cada etiqueta <producto> y extraemos sus campos
        for (int i = 0; i < encontrados; i++) {
            Node nodo = listaNodosProducto.item(i);

            if (nodo.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element elemProducto = (Element) nodo;

            // Extraemos los valores de las etiquetas hijas
            String codigo = obtenerTextoEtiqueta(elemProducto, "codigo");
            String nombre = obtenerTextoEtiqueta(elemProducto, "nombre");

            BigDecimal precio = null;
            String textoPrecio = obtenerTextoEtiqueta(elemProducto, "precio");
            if (textoPrecio != null) {
                try {
                    precio = new BigDecimal(textoPrecio.replace(',', '.'));
                } catch (Exception ex) {
                    precio = null;
                }
            }

            int stock = -1;
            String textoStock = obtenerTextoEtiqueta(elemProducto, "stock");
            if (textoStock != null) {
                try {
                    stock = Integer.parseInt(textoStock);
                } catch (NumberFormatException ex) {
                    stock = -1;
                }
            }

            int categoriaId = 0;
            String textoCat = obtenerTextoEtiqueta(elemProducto, "categoriaId");
            if (textoCat != null) {
                try {
                    categoriaId = Integer.parseInt(textoCat);
                } catch (NumberFormatException ex) {
                    categoriaId = 0;
                }
            }

            int proveedorId = 0;
            String textoProv = obtenerTextoEtiqueta(elemProducto, "proveedorId");
            if (textoProv != null) {
                try {
                    proveedorId = Integer.parseInt(textoProv);
                } catch (NumberFormatException ex) {
                    proveedorId = 0;
                }
            }

            // Construimos el objeto y aplicamos la misma validación que en JSON
            Producto nuevoProducto = new Producto(codigo, nombre, precio, stock, categoriaId, proveedorId);
            String error = ValidadorProducto.validar(nuevoProducto, inventario.getProductos(), codigosEnEsteFichero);

            if (error != null) {
                rechazados++;
                String identificador = (codigo != null && !codigo.trim().isEmpty()) ? codigo.trim() : "Producto " + (i + 1);
                incidencias.add(identificador + ": " + error);
            } else {
                importados++;
                String codigoLimpio = codigo.trim().toUpperCase();
                nuevoProducto.setCodigo(codigoLimpio);
                codigosEnEsteFichero.add(codigoLimpio);
                productosValidosParaAnadir.add(nuevoProducto);
            }
        }

        // 6. Añadimos los productos válidos al inventario general en memoria
        inventario.getProductos().addAll(productosValidosParaAnadir);

        // 7. Mostramos el resumen final en pantalla
        System.out.println("Productos encontrados: " + encontrados);
        System.out.println("Importados: " + importados + " | Rechazados: " + rechazados);

        if (!incidencias.isEmpty()) {
            System.out.println(ColoresConsola.pintar("INCIDENCIAS:", ColoresConsola.AMARILLO));
            for (String inc : incidencias) {
                System.out.println(" - " + inc);
            }
            System.out.println(ColoresConsola.pintar("Importación finalizada con incidencias.", ColoresConsola.AMARILLO));
        } else {
            System.out.println(ColoresConsola.pintar("Importación finalizada con éxito.", ColoresConsola.VERDE));
        }
    }

    /**
     * Método auxiliar para extraer el contenido de texto limpio de una etiqueta hija.
     * Devuelve null si la etiqueta no existe o no tiene texto.
     */
    private String obtenerTextoEtiqueta(Element padre, String nombreEtiqueta) {
        NodeList lista = padre.getElementsByTagName(nombreEtiqueta);
        if (lista != null && lista.getLength() > 0) {
            Node nodoHijo = lista.item(0);
            if (nodoHijo != null && nodoHijo.getTextContent() != null) {
                String contenido = nodoHijo.getTextContent().trim();
                return contenido.isEmpty() ? null : contenido;
            }
        }
        return null;
    }
}
