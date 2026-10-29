package es.iesisidra.datastock.complementarias;

import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Actividad técnica complementaria RF-FIC-07: Consultas XML mediante XPath.
 * <p>Esta clase es un ejecutable independiente del menú principal que permite realizar
 * consultas directas sobre documentos XML mediante el lenguaje de expresiones XPath,
 * manteniendo el analizador DOM protegido contra ataques XXE y declaraciones DTD.</p>
 */
public final class XPathActividad {

    private XPathActividad() { }

    public static void main(String[] args) {
        System.out.println(ColoresConsola.pintar("\n==================================================", ColoresConsola.CIAN));
        System.out.println(ColoresConsola.pintar("     DATASTOCK - CONSULTAS XML MEDIANTE XPATH     ", ColoresConsola.CIAN));
        System.out.println(ColoresConsola.pintar("              ACTIVIDAD RF-FIC-07                 ", ColoresConsola.CIAN));
        System.out.println(ColoresConsola.pintar("==================================================", ColoresConsola.CIAN));

        // 1. Solicitamos la ruta del fichero XML a consultar
        String rutaTexto = LectorConsola.leerTexto("\nIntroduce la ruta del fichero XML: ").trim();
        if (rutaTexto.isEmpty()) {
            System.out.println(ColoresConsola.pintar("Ruta vacía. Finalizando actividad.", ColoresConsola.AMARILLO));
            return;
        }

        Path ruta = Paths.get(rutaTexto);

        // Validaciones previas de la ruta con NIO.2
        if (Files.notExists(ruta)) {
            System.out.println(ColoresConsola.pintar("Error: El fichero no existe: " + rutaTexto, ColoresConsola.ROJO));
            return;
        }
        if (!Files.isRegularFile(ruta)) {
            System.out.println(ColoresConsola.pintar("Error: La ruta no corresponde a un archivo regular: " + rutaTexto, ColoresConsola.ROJO));
            return;
        }
        if (!Files.isReadable(ruta)) {
            System.out.println(ColoresConsola.pintar("Error: No tienes permisos de lectura sobre el archivo: " + rutaTexto, ColoresConsola.ROJO));
            return;
        }

        // 2. Analizamos el XML con analizador DOM protegido frente a ataques XXE y DTD
        Document documentoXml = cargarDocumentoProtegido(ruta);
        if (documentoXml == null) {
            return;
        }

        // Verificamos que la raíz sea exactamente <catalogo>
        Element raiz = documentoXml.getDocumentElement();
        if (raiz == null || !"catalogo".equalsIgnoreCase(raiz.getTagName())) {
            System.out.println(ColoresConsola.pintar("Error: La raíz del XML debe ser '<catalogo>'. Fichero no compatible.", ColoresConsola.ROJO));
            return;
        }

        // 3. Inicializamos el motor de evaluación XPath
        XPathFactory factoriaXPath = XPathFactory.newInstance();
        XPath xpath = factoriaXPath.newXPath();

        System.out.println(ColoresConsola.pintar("\nDocumento XML cargado correctamente: " + ruta.getFileName(), ColoresConsola.VERDE));

        // 4. Submenú interactivo para permitir múltiples consultas sobre el documento abierto
        int opcion;
        do {
            System.out.println("\n===== CONSULTAS XPATH =====");
            System.out.println("Fichero: " + rutaTexto);
            System.out.println("1. Buscar por código exacto");
            System.out.println("2. Filtrar por identificador de categoría");
            System.out.println("3. Filtrar por precio superior a un valor");
            System.out.println("4. Contar elementos <producto> en el catálogo");
            System.out.println("0. Salir");

            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 4);

            switch (opcion) {
                case 1 -> consultarPorCodigo(xpath, documentoXml);
                case 2 -> consultarPorCategoria(xpath, documentoXml);
                case 3 -> consultarPorPrecioSuperior(xpath, documentoXml);
                case 4 -> consultarContadorTotal(xpath, documentoXml);
                case 0 -> System.out.println("Saliendo de la actividad XPath.");
                default -> throw new IllegalStateException("Opción inesperada");
            }

        } while (opcion != 0);
    }

    /**
     * Consulta 1: Busca un producto por código.
     * <p>Siguiendo las recomendaciones de seguridad del enunciado, en lugar de concatenar
     * texto introducido por el usuario directamente en una expresión XPath (lo que provocaría
     * riesgo de inyección XPath), seleccionamos los nodos de producto y comparamos su código
     * de forma segura en Java (case-insensitive).</p>
     */
    private static void consultarPorCodigo(XPath xpath, Document doc) {
        String codigoBuscado = LectorConsola.leerTexto("Introduce el código del producto a buscar: ").trim();
        if (codigoBuscado.isEmpty()) {
            System.out.println(ColoresConsola.pintar("El código no puede estar vacío.", ColoresConsola.AMARILLO));
            return;
        }

        try {
            // Obtenemos todos los productos mediante XPath y filtramos por código en memoria
            String expresion = "/catalogo/producto";
            NodeList listaNodos = (NodeList) xpath.evaluate(expresion, doc, XPathConstants.NODESET);

            boolean encontrado = false;
            for (int i = 0; i < listaNodos.getLength(); i++) {
                Node nodo = listaNodos.item(i);
                if (nodo.getNodeType() == Node.ELEMENT_NODE) {
                    Element elem = (Element) nodo;
                    String codigoXml = obtenerTextoEtiqueta(elem, "codigo");
                    if (codigoXml != null && codigoXml.equalsIgnoreCase(codigoBuscado)) {
                        System.out.println(ColoresConsola.pintar("\n--- Producto encontrado ---", ColoresConsola.VERDE));
                        mostrarDetalleProducto(elem);
                        encontrado = true;
                        break;
                    }
                }
            }

            if (!encontrado) {
                System.out.println(ColoresConsola.pintar("No se encontró ningún producto con el código '" + codigoBuscado + "'.", ColoresConsola.AMARILLO));
            }

        } catch (XPathExpressionException e) {
            System.out.println(ColoresConsola.pintar("Error al evaluar la expresión XPath: " + e.getMessage(), ColoresConsola.ROJO));
        }
    }

    /**
     * Consulta 2: Filtra productos cuya categoría coincida exactamente con la solicitada.
     * <p>Expresión orientativa: {@code /catalogo/producto[categoriaId=1]}</p>
     */
    private static void consultarPorCategoria(XPath xpath, Document doc) {
        int categoriaId = LectorConsola.leerEntero("Introduce el ID de categoría: ");
        if (categoriaId <= 0) {
            System.out.println(ColoresConsola.pintar("El ID de categoría debe ser un número positivo.", ColoresConsola.AMARILLO));
            return;
        }

        try {
            // Construimos la expresión XPath validando que categoriaId es un entero
            String expresion = "/catalogo/producto[categoriaId=" + categoriaId + "]";
            NodeList listaNodos = (NodeList) xpath.evaluate(expresion, doc, XPathConstants.NODESET);

            int total = listaNodos.getLength();
            if (total == 0) {
                System.out.println(ColoresConsola.pintar("No se encontraron productos pertenecientes a la categoría " + categoriaId + ".", ColoresConsola.AMARILLO));
                return;
            }

            System.out.println(ColoresConsola.pintar("\n--- Productos de la categoría " + categoriaId + " ---", ColoresConsola.VERDE));
            for (int i = 0; i < total; i++) {
                Node nodo = listaNodos.item(i);
                if (nodo.getNodeType() == Node.ELEMENT_NODE) {
                    mostrarDetalleProducto((Element) nodo);
                }
            }
            System.out.println("Productos encontrados: " + total);

        } catch (XPathExpressionException e) {
            System.out.println(ColoresConsola.pintar("Error al evaluar la expresión XPath: " + e.getMessage(), ColoresConsola.ROJO));
        }
    }

    /**
     * Consulta 3: Filtra productos cuyo precio sea estrictamente superior al mínimo introducido.
     * <p>Expresión orientativa: {@code /catalogo/producto[precio > 100]}</p>
     * <p>Caso CP-XPATH-03: Excluye los productos con precio exactamente igual al límite.</p>
     */
    private static void consultarPorPrecioSuperior(XPath xpath, Document doc) {
        String textoPrecio = LectorConsola.leerTexto("Precio mínimo: ").trim();
        double precioMinimo;
        try {
            precioMinimo = Double.parseDouble(textoPrecio.replace(',', '.'));
            if (precioMinimo < 0) {
                System.out.println(ColoresConsola.pintar("El precio mínimo no puede ser negativo.", ColoresConsola.AMARILLO));
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println(ColoresConsola.pintar("Introduce un valor numérico de precio válido.", ColoresConsola.AMARILLO));
            return;
        }

        try {
            // Usamos operador estrictamente mayor '>' para excluir productos con precio igual al filtro
            String expresion = "/catalogo/producto[precio > " + precioMinimo + "]";
            NodeList listaNodos = (NodeList) xpath.evaluate(expresion, doc, XPathConstants.NODESET);

            int total = listaNodos.getLength();
            if (total == 0) {
                System.out.println(ColoresConsola.pintar("No se encontraron productos con precio superior a " + precioMinimo + " €.", ColoresConsola.AMARILLO));
                return;
            }

            System.out.println(ColoresConsola.pintar("\n--- Productos con precio > " + precioMinimo + " € ---", ColoresConsola.VERDE));
            for (int i = 0; i < total; i++) {
                Node nodo = listaNodos.item(i);
                if (nodo.getNodeType() == Node.ELEMENT_NODE) {
                    mostrarDetalleProducto((Element) nodo);
                }
            }
            System.out.println("Productos encontrados: " + total);

        } catch (XPathExpressionException e) {
            System.out.println(ColoresConsola.pintar("Error al evaluar la expresión XPath: " + e.getMessage(), ColoresConsola.ROJO));
        }
    }

    /**
     * Consulta 4: Cuenta el total de elementos producto del catálogo.
     * <p>Expresión orientativa: {@code count(/catalogo/producto)}</p>
     * <p>Cuenta todos los elementos producto aunque alguno contenga datos incorrectos (Caso CP-XPATH-04).</p>
     */
    private static void consultarContadorTotal(XPath xpath, Document doc) {
        try {
            String expresion = "count(/catalogo/producto)";
            Double resultado = (Double) xpath.evaluate(expresion, doc, XPathConstants.NUMBER);
            int total = resultado != null ? resultado.intValue() : 0;

            System.out.println(ColoresConsola.pintar("\n--- Conteo total de productos ---", ColoresConsola.VERDE));
            System.out.println("Expresión XPath: " + expresion);
            System.out.println("Total de elementos <producto> en el catálogo XML: " + total);

        } catch (XPathExpressionException e) {
            System.out.println(ColoresConsola.pintar("Error al evaluar la función count() de XPath: " + e.getMessage(), ColoresConsola.ROJO));
        }
    }

    /**
     * Carga y parsea un archivo XML aplicando directivas de seguridad contra vulnerabilidades XXE y DTDs.
     */
    private static Document cargarDocumentoProtegido(Path ruta) {
        DocumentBuilderFactory factoria = DocumentBuilderFactory.newInstance();
        try {
            // Protección activa contra XXE (XML External Entity)
            factoria.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factoria.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factoria.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factoria.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factoria.setXIncludeAware(false);
            factoria.setExpandEntityReferences(false);
            factoria.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factoria.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

            DocumentBuilder constructor = factoria.newDocumentBuilder();
            Document doc = constructor.parse(ruta.toFile());
            doc.getDocumentElement().normalize();
            return doc;

        } catch (SAXException e) {
            // Si el XML incluye un DOCTYPE prohibido o está mal formado (Caso CP-XPATH-05)
            System.out.println(ColoresConsola.pintar("Error: El documento XML contiene declaraciones DTD no permitidas o está mal formado.", ColoresConsola.ROJO));
            System.out.println("Detalle del error de seguridad/formato: " + e.getMessage());
            return null;
        } catch (ParserConfigurationException e) {
            System.out.println(ColoresConsola.pintar("Error de configuración del analizador DOM: " + e.getMessage(), ColoresConsola.ROJO));
            return null;
        } catch (IOException e) {
            System.out.println(ColoresConsola.pintar("Error de E/S al leer el fichero: " + e.getMessage(), ColoresConsola.ROJO));
            return null;
        }
    }

    /**
     * Muestra la información de un nodo <producto> en el formato claro solicitado.
     */
    private static void mostrarDetalleProducto(Element elemProducto) {
        String codigo = obtenerTextoEtiqueta(elemProducto, "codigo");
        String nombre = obtenerTextoEtiqueta(elemProducto, "nombre");
        String precio = obtenerTextoEtiqueta(elemProducto, "precio");
        String stock = obtenerTextoEtiqueta(elemProducto, "stock");
        String cat = obtenerTextoEtiqueta(elemProducto, "categoriaId");
        String prov = obtenerTextoEtiqueta(elemProducto, "proveedorId");

        System.out.println(codigo != null ? codigo : "[SIN CÓDIGO]");
        System.out.println(nombre != null ? nombre : "[SIN NOMBRE]");
        System.out.println((precio != null ? precio : "0.00") + " € | Stock: " + (stock != null ? stock : "0")
                + " | Cat: " + (cat != null ? cat : "?") + " | Prov: " + (prov != null ? prov : "?"));
        System.out.println("----------------------------------------");
    }

    /**
     * Extrae el texto limpio de un elemento hijo directo.
     */
    private static String obtenerTextoEtiqueta(Element padre, String nombreEtiqueta) {
        NodeList hijos = padre.getElementsByTagName(nombreEtiqueta);
        if (hijos != null && hijos.getLength() > 0) {
            Node nodoHijo = hijos.item(0);
            if (nodoHijo != null && nodoHijo.getTextContent() != null) {
                String texto = nodoHijo.getTextContent().trim();
                return texto.isEmpty() ? null : texto;
            }
        }
        return null;
    }
}
