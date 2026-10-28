package es.iesisidra.datastock.servicio;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.util.ColoresConsola;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Servicio encargado de importar catálogos de productos en formato JSON (RF-FIC-02).
 * <p>Utiliza la biblioteca Gson para analizar la estructura del documento.
 * Si el fichero está mal formado o no tiene la raíz correcta, se rechaza entero.
 * Si tiene productos con datos incorrectos o repetidos, solo se rechazan esos y se añaden los válidos.</p>
 */
public class ImportadorJsonServicio {

    /**
     * Importa productos desde un fichero JSON a la colección del inventario.
     *
     * @param rutaTexto  Ruta al fichero introducida por el usuario.
     * @param inventario El inventario compartido donde se guardarán los productos válidos.
     */
    public void importar(String rutaTexto, Inventario inventario) {
        if (rutaTexto == null || rutaTexto.trim().isEmpty()) {
            System.out.println(ColoresConsola.pintar("Error: La ruta del fichero no puede estar vacía.", ColoresConsola.ROJO));
            return;
        }

        Path ruta = Paths.get(rutaTexto.trim());

        // 1. Verificamos que el fichero existe, es regular y tenemos permisos para leerlo
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

        System.out.println(ColoresConsola.pintar("\n===== IMPORTAR JSON =====", ColoresConsola.CIAN));
        System.out.println("Ruta: " + rutaTexto);

        // 2. Leemos y parseamos el JSON con Gson usando try-with-resources y codificación UTF-8
        JsonElement elementoRaiz;
        try (BufferedReader lector = Files.newBufferedReader(ruta, StandardCharsets.UTF_8)) {
            elementoRaiz = JsonParser.parseReader(lector);
        } catch (JsonSyntaxException e) {
            // Caso CP-JSON-03: fichero malformado (llaves sin cerrar, sintaxis rota)
            System.out.println(ColoresConsola.pintar("Error: El fichero tiene una sintaxis JSON incorrecta o está mal formado.", ColoresConsola.ROJO));
            System.out.println("No se ha importado ningún producto y la memoria no ha sufrido cambios.");
            return;
        } catch (IOException e) {
            System.out.println(ColoresConsola.pintar("Error de entrada/salida al leer el fichero: " + e.getMessage(), ColoresConsola.ROJO));
            return;
        }

        // 3. Comprobamos la estructura raíz del JSON: debe ser un objeto con la propiedad "productos"
        if (elementoRaiz == null || !elementoRaiz.isJsonObject()) {
            System.out.println(ColoresConsola.pintar("Error: La raíz del documento JSON debe ser un objeto. Importación cancelada.", ColoresConsola.ROJO));
            return;
        }

        JsonObject objetoRaiz = elementoRaiz.getAsJsonObject();
        if (!objetoRaiz.has("productos") || !objetoRaiz.get("productos").isJsonArray()) {
            System.out.println(ColoresConsola.pintar("Error: El JSON no contiene el array obligatorio 'productos'. Importación cancelada.", ColoresConsola.ROJO));
            return;
        }

        JsonArray arrayProductos = objetoRaiz.getAsJsonArray("productos");
        int encontrados = arrayProductos.size();
        int importados = 0;
        int rechazados = 0;

        List<String> incidencias = new ArrayList<>();
        Set<String> codigosEnEsteFichero = new HashSet<>();
        List<Producto> productosValidosParaAnadir = new ArrayList<>();

        // 4. Recorremos cada elemento del array para construir y validar el producto
        for (int i = 0; i < encontrados; i++) {
            JsonElement elemento = arrayProductos.get(i);

            if (!elemento.isJsonObject()) {
                rechazados++;
                incidencias.add("Elemento " + (i + 1) + ": no tiene el formato de un objeto producto");
                continue;
            }

            JsonObject objProducto = elemento.getAsJsonObject();

            // Extraemos los campos comprobando que existan y no sean null en el JSON
            String codigo = objProducto.has("codigo") && !objProducto.get("codigo").isJsonNull()
                    ? objProducto.get("codigo").getAsString() : null;

            String nombre = objProducto.has("nombre") && !objProducto.get("nombre").isJsonNull()
                    ? objProducto.get("nombre").getAsString() : null;

            BigDecimal precio = null;
            if (objProducto.has("precio") && !objProducto.get("precio").isJsonNull()) {
                try {
                    precio = objProducto.get("precio").getAsBigDecimal();
                } catch (Exception ex) {
                    precio = null; // Si no es un número convertible a BigDecimal
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

            // Validamos las reglas de negocio comunes con nuestra clase ValidadorProducto
            String error = ValidadorProducto.validar(nuevoProducto, inventario.getProductos(), codigosEnEsteFichero);

            if (error != null) {
                rechazados++;
                // Usamos el código si existe para identificar la incidencia, o la posición si venía vacío
                String identificador = (codigo != null && !codigo.trim().isEmpty()) ? codigo.trim() : "Producto " + (i + 1);
                incidencias.add(identificador + ": " + error);
            } else {
                // Producto correcto: lo guardamos en la lista temporal del lote
                importados++;
                String codigoLimpio = codigo.trim().toUpperCase();
                nuevoProducto.setCodigo(codigoLimpio);
                codigosEnEsteFichero.add(codigoLimpio);
                productosValidosParaAnadir.add(nuevoProducto);
            }
        }

        // 5. Añadimos todos los productos válidos a la memoria de la aplicación
        inventario.getProductos().addAll(productosValidosParaAnadir);

        // 6. Mostramos el resumen final por consola tal como pide el enunciado
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
}
