package es.iesisidra.datastock.componentes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.servicio.ValidadorProducto;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Componente reutilizable de acceso a ficheros encargado de la lectura,
 * análisis sintáctico y validación de catálogos de productos en formato JSON (UT3).
 *
 * <p><b>Características de diseño:</b></p>
 * <ul>
 *   <li><b>Desacoplamiento total de consola:</b> No realiza operaciones con {@code System.out},
 *       {@code System.err} ni lecturas con {@code Scanner}. Toda la comunicación de resultados
 *       y errores se realiza mediante colecciones devueltas y excepciones tipadas.</li>
 *   <li><b>Sin efectos colaterales sobre persistencia:</b> No inserta datos directamente en MySQL
 *       ni manipula colecciones de inventario globales de forma intrusiva.</li>
 *   <li><b>Validación controlada y resiliente:</b> Los fallos críticos de fichero o sintaxis abortan
 *       mediante {@link LectorJsonException}, mientras que los registros individuales con datos inválidos
 *       o duplicados son discriminados y registrados en la lista de incidencias, permitiendo recuperar
 *       todos los registros válidos presentes en el archivo.</li>
 * </ul>
 *
 * @author Guillermo Beltrán Tabares
 * @version 1.0 (UT3)
 */
public class LectorProductosJson {

    private List<String> ultimasIncidencias = new ArrayList<>();
    private int ultimoTotalEncontrados = 0;
    private int ultimoTotalRechazados = 0;

    /**
     * Constructor por defecto del componente.
     */
    public LectorProductosJson() {
    }

    /**
     * Lee y valida los productos contenidos en un fichero JSON a partir de su ruta textual.
     *
     * @param rutaTexto Cadena con la ruta absoluta o relativa al archivo JSON.
     * @return Lista de objetos {@link Producto} válidos encontrados.
     * @throws LectorJsonException Si la ruta es nula/vacía, el archivo no existe, no es legible
     *                             o la sintaxis JSON es incorrecta.
     */
    public List<Producto> leerProductos(String rutaTexto) throws LectorJsonException {
        if (rutaTexto == null || rutaTexto.trim().isEmpty()) {
            throw new LectorJsonException("La ruta del fichero no puede estar vacía.");
        }
        return leerProductos(Paths.get(rutaTexto.trim()), Collections.emptyList());
    }

    /**
     * Lee y valida los productos contenidos en un fichero JSON utilizando un {@link Path}.
     *
     * @param ruta Objeto {@link Path} que apunta al fichero JSON a procesar.
     * @return Lista de objetos {@link Producto} válidos encontrados.
     * @throws LectorJsonException Si el archivo no existe, no es regular, no es legible o su estructura JSON es inválida.
     */
    public List<Producto> leerProductos(Path ruta) throws LectorJsonException {
        return leerProductos(ruta, Collections.emptyList());
    }

    /**
     * Lee y valida los productos de un fichero JSON comprobando duplicados frente a una lista
     * de productos preexistentes en memoria.
     *
     * @param ruta                Ruta del fichero JSON.
     * @param productosExistentes Colección de productos ya existentes contra los que validar duplicidad de código.
     * @return Lista de objetos {@link Producto} válidos listos para ser incorporados.
     * @throws LectorJsonException Si ocurre un error estructural de acceso o análisis en el archivo.
     */
    public List<Producto> leerProductos(Path ruta, List<Producto> productosExistentes) throws LectorJsonException {
        ResultadoLecturaJson resultado = leerDetallado(ruta, productosExistentes);
        return resultado.getProductosValidos();
    }

    /**
     * Procesa un archivo JSON y devuelve un informe detallado con productos válidos,
     * incidencias y métricas cuantitativas.
     *
     * @param ruta Ruta del fichero JSON a leer.
     * @return Objeto {@link ResultadoLecturaJson} con el desglose completo del procesamiento.
     * @throws LectorJsonException Si el fichero no existe, no es legible o presenta errores sintácticos irrecuperables.
     */
    public ResultadoLecturaJson leerDetallado(Path ruta) throws LectorJsonException {
        return leerDetallado(ruta, Collections.emptyList());
    }

    /**
     * Procesa exhaustivamente un archivo JSON comprobando reglas de negocio e integridad
     * frente a un conjunto de productos ya cargados en memoria.
     *
     * @param ruta                Ruta del archivo en el sistema de ficheros.
     * @param productosExistentes Colección de productos existentes para verificar colisión de claves únicas.
     * @return Objeto {@link ResultadoLecturaJson} con los productos válidos y el registro de incidencias.
     * @throws LectorJsonException Si el archivo no existe, carece de permisos de lectura,
     *                             no es un archivo regular, contiene JSON malformado o no cumple
     *                             el esquema raíz obligatorio.
     */
    public ResultadoLecturaJson leerDetallado(Path ruta, List<Producto> productosExistentes) throws LectorJsonException {
        if (ruta == null) {
            throw new LectorJsonException("La ruta del fichero no puede ser nula.");
        }

        // 1. Verificación de existencia y permisos del archivo
        if (Files.notExists(ruta)) {
            throw new LectorJsonException("El fichero no existe: " + ruta);
        }
        if (!Files.isRegularFile(ruta)) {
            throw new LectorJsonException("La ruta no corresponde a un fichero regular: " + ruta);
        }
        if (!Files.isReadable(ruta)) {
            throw new LectorJsonException("No se tienen permisos de lectura sobre el fichero: " + ruta);
        }

        // 2. Parseo sintáctico con Gson en UTF-8 utilizando try-with-resources
        JsonElement elementoRaiz;
        try (BufferedReader lector = Files.newBufferedReader(ruta, StandardCharsets.UTF_8)) {
            elementoRaiz = JsonParser.parseReader(lector);
        } catch (JsonSyntaxException e) {
            throw new LectorJsonException("El fichero tiene una sintaxis JSON incorrecta o está mal formado.", e);
        } catch (IOException e) {
            throw new LectorJsonException("Error de entrada/salida al leer el fichero: " + e.getMessage(), e);
        }

        // 3. Validación de la estructura raíz del documento
        if (elementoRaiz == null || !elementoRaiz.isJsonObject()) {
            throw new LectorJsonException("La raíz del documento JSON debe ser un objeto. Importación cancelada.");
        }

        JsonObject objetoRaiz = elementoRaiz.getAsJsonObject();
        if (!objetoRaiz.has("productos") || !objetoRaiz.get("productos").isJsonArray()) {
            throw new LectorJsonException("El JSON no contiene el array obligatorio 'productos'. Importación cancelada.");
        }

        JsonArray arrayProductos = objetoRaiz.getAsJsonArray("productos");
        int totalEncontrados = arrayProductos.size();
        int rechazados = 0;

        List<String> incidencias = new ArrayList<>();
        Set<String> codigosEnEsteFichero = new HashSet<>();
        List<Producto> productosValidos = new ArrayList<>();
        List<Producto> memoriaBase = productosExistentes != null ? productosExistentes : Collections.emptyList();

        // 4. Procesamiento iterativo de cada elemento producto
        for (int i = 0; i < totalEncontrados; i++) {
            JsonElement elemento = arrayProductos.get(i);

            if (!elemento.isJsonObject()) {
                rechazados++;
                incidencias.add("Elemento " + (i + 1) + ": no tiene el formato de un objeto JSON producto.");
                continue;
            }

            JsonObject objProducto = elemento.getAsJsonObject();

            String codigo = objProducto.has("codigo") && !objProducto.get("codigo").isJsonNull()
                    ? objProducto.get("codigo").getAsString() : null;

            String nombre = objProducto.has("nombre") && !objProducto.get("nombre").isJsonNull()
                    ? objProducto.get("nombre").getAsString() : null;

            BigDecimal precio = null;
            if (objProducto.has("precio") && !objProducto.get("precio").isJsonNull()) {
                try {
                    precio = objProducto.get("precio").getAsBigDecimal();
                } catch (Exception ex) {
                    precio = null;
                }
            }

            int stock = -1;
            if (objProducto.has("stock") && !objProducto.get("stock").isJsonNull()) {
                try {
                    stock = objProducto.get("stock").getAsInt();
                } catch (Exception ex) {
                    stock = -1;
                }
            }

            int categoriaId = 0;
            if (objProducto.has("categoriaId") && !objProducto.get("categoriaId").isJsonNull()) {
                try {
                    categoriaId = objProducto.get("categoriaId").getAsInt();
                } catch (Exception ex) {
                    categoriaId = 0;
                }
            }

            int proveedorId = 0;
            if (objProducto.has("proveedorId") && !objProducto.get("proveedorId").isJsonNull()) {
                try {
                    proveedorId = objProducto.get("proveedorId").getAsInt();
                } catch (Exception ex) {
                    proveedorId = 0;
                }
            }

            Producto nuevoProducto = new Producto(codigo, nombre, precio, stock, categoriaId, proveedorId);

            // Validación semántica según las reglas de negocio de DataStock
            String error = ValidadorProducto.validar(nuevoProducto, memoriaBase, codigosEnEsteFichero);

            if (error != null) {
                rechazados++;
                String identificador = (codigo != null && !codigo.trim().isEmpty())
                        ? codigo.trim()
                        : "Producto en posición " + (i + 1);
                incidencias.add(identificador + ": " + error);
            } else {
                String codigoLimpio = codigo.trim().toUpperCase();
                nuevoProducto.setCodigo(codigoLimpio);
                codigosEnEsteFichero.add(codigoLimpio);
                productosValidos.add(nuevoProducto);
            }
        }

        // Actualizamos las propiedades internas de auditoría de la última lectura
        this.ultimasIncidencias = new ArrayList<>(incidencias);
        this.ultimoTotalEncontrados = totalEncontrados;
        this.ultimoTotalRechazados = rechazados;

        return new ResultadoLecturaJson(productosValidos, incidencias, totalEncontrados, rechazados);
    }

    /**
     * Devuelve las incidencias registradas en la última operación de lectura realizada.
     *
     * @return Lista inmodificable de incidencias.
     */
    public List<String> getUltimasIncidencias() {
        return Collections.unmodifiableList(ultimasIncidencias);
    }

    /**
     * Devuelve el total de elementos leídos en la última operación.
     *
     * @return Total de productos encontrados en el array.
     */
    public int getUltimoTotalEncontrados() {
        return ultimoTotalEncontrados;
    }

    /**
     * Devuelve el total de elementos rechazados en la última operación.
     *
     * @return Total de elementos descartados.
     */
    public int getUltimoTotalRechazados() {
        return ultimoTotalRechazados;
    }
}
