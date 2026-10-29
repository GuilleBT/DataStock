package es.iesisidra.datastock.servicio;

import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio encargado de la persistencia y recuperación del inventario mediante
 * serialización binaria de objetos en Java (RF-FIC-06).
 * <p>Utiliza {@link ObjectOutputStream} y {@link ObjectInputStream} para guardar
 * y leer la colección completa en formato binario (.dat). Aplica sustitución atómica
 * en memoria, asegurando que ante cualquier error o cancelación la memoria previa
 * permanezca intacta.</p>
 */
public class SerializacionServicio {

    /**
     * Guarda la lista completa de productos en un archivo binario mediante ObjectOutputStream.
     *
     * @param rutaTexto  Ruta al archivo .dat donde se guardará la colección.
     * @param inventario Colección de productos en memoria.
     */
    public void guardar(String rutaTexto, Inventario inventario) {
        // 1. Verificamos que el inventario contenga productos (Caso CP-SER-04)
        if (inventario == null || inventario.getProductos() == null || inventario.getProductos().isEmpty()) {
            System.out.println(ColoresConsola.pintar("El inventario está vacío. No hay productos para guardar.", ColoresConsola.AMARILLO));
            return;
        }

        if (rutaTexto == null || rutaTexto.trim().isEmpty()) {
            System.out.println(ColoresConsola.pintar("Error: La ruta de destino no puede estar vacía.", ColoresConsola.ROJO));
            return;
        }

        Path ruta = Paths.get(rutaTexto.trim());

        // 2. Comprobamos la existencia del directorio contenedor
        Path dirPadre = ruta.getParent();
        if (dirPadre != null && Files.notExists(dirPadre)) {
            System.out.println(ColoresConsola.pintar("Error: El directorio de destino no existe: " + dirPadre, ColoresConsola.ROJO));
            return;
        }

        // 3. Confirmación expresa antes de sobrescribir un fichero existente
        if (Files.exists(ruta)) {
            boolean deseaSobrescribir = LectorConsola.confirmar("El fichero '" + ruta.getFileName() + "' ya existe. ¿Deseas sobrescribirlo?");
            if (!deseaSobrescribir) {
                System.out.println(ColoresConsola.pintar("Operación cancelada. El fichero original no se ha modificado.", ColoresConsola.AMARILLO));
                return;
            }
        }

        // 4. Copiamos los productos y los serializamos en el flujo de salida
        List<Producto> listaAGuardar = new ArrayList<>(inventario.getProductos());
        try (ObjectOutputStream flujoSalida = new ObjectOutputStream(Files.newOutputStream(ruta))) {
            flujoSalida.writeObject(listaAGuardar);

            System.out.println(ColoresConsola.pintar("Inventario guardado correctamente.", ColoresConsola.VERDE));
            System.out.println("Ruta: " + ruta.toAbsolutePath());
            System.out.println("Productos guardados: " + listaAGuardar.size());

        } catch (IOException e) {
            System.out.println(ColoresConsola.pintar("Error de entrada/salida al guardar el fichero: " + e.getMessage(), ColoresConsola.ROJO));
        }
    }

    /**
     * Recupera la colección de productos desde un fichero binario serializado fiable (.dat).
     *
     * @param rutaTexto  Ruta al archivo binario a leer.
     * @param inventario Inventario en memoria donde se sustituirán los datos.
     */
    public void recuperar(String rutaTexto, Inventario inventario) {
        if (rutaTexto == null || rutaTexto.trim().isEmpty()) {
            System.out.println(ColoresConsola.pintar("Error: La ruta no puede estar vacía.", ColoresConsola.ROJO));
            return;
        }

        Path ruta = Paths.get(rutaTexto.trim());

        // 1. Validaciones previas de la ruta física con NIO.2
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

        System.out.println(ColoresConsola.pintar("\n===== RECUPERAR INVENTARIO =====", ColoresConsola.CIAN));
        System.out.println("Ruta: " + rutaTexto);

        // 2. Si ya hay productos en memoria, advertir y pedir confirmación expresa (Caso CP-SER-02)
        if (inventario.getProductos() != null && !inventario.getProductos().isEmpty()) {
            System.out.println("Actualmente hay " + inventario.getProductos().size() + " productos en memoria.");
            System.out.println(ColoresConsola.pintar("La recuperación sustituirá el inventario actual.", ColoresConsola.AMARILLO));
            boolean continuar = LectorConsola.confirmar("¿Continuar?");
            if (!continuar) {
                System.out.println(ColoresConsola.pintar("Operación cancelada. Se conserva el inventario actual.", ColoresConsola.AMARILLO));
                return;
            }
        }

        // 3. Deserialización en una colección auxiliar temporal (principio de sustitución atómica)
        List<Producto> listaRecuperada = new ArrayList<>();
        try (ObjectInputStream flujoEntrada = new ObjectInputStream(Files.newInputStream(ruta))) {
            Object objetoLeido = flujoEntrada.readObject();

            // Validamos que el objeto leído sea realmente una lista
            if (!(objetoLeido instanceof List<?> listaCruda)) {
                System.out.println(ColoresConsola.pintar("Error: El fichero no contiene una colección válida de productos.", ColoresConsola.ROJO));
                System.out.println("La memoria actual permanece intacta.");
                return;
            }

            // Validamos cada producto individualmente dentro de la lista
            for (Object elemento : listaCruda) {
                if (!(elemento instanceof Producto producto)) {
                    System.out.println(ColoresConsola.pintar("Error: El archivo contiene elementos que no son instancias de Producto.", ColoresConsola.ROJO));
                    System.out.println("La memoria actual permanece intacta.");
                    return;
                }
                if (producto.getCodigo() == null || producto.getCodigo().trim().isEmpty()) {
                    System.out.println(ColoresConsola.pintar("Error: Se detectó un producto corrupto sin código.", ColoresConsola.ROJO));
                    System.out.println("La memoria actual permanece intacta.");
                    return;
                }
                listaRecuperada.add(producto);
            }

        } catch (ClassNotFoundException e) {
            // Ocurre si la clase Producto no coincide con la versión serializada
            System.out.println(ColoresConsola.pintar("Error: Incompatibilidad de clases durante la deserialización.", ColoresConsola.ROJO));
            System.out.println("Detalle: " + e.getMessage());
            System.out.println("La memoria actual permanece intacta.");
            return;
        } catch (IOException e) {
            // Captura ficheros dañados, truncados o no serializados (Caso CP-SER-03)
            System.out.println(ColoresConsola.pintar("Error: El fichero está dañado, incompleto o no es un fichero binario serializado válido.", ColoresConsola.ROJO));
            System.out.println("Detalle: " + e.getMessage());
            System.out.println("La memoria actual permanece intacta.");
            return;
        }

        // 4. Únicamente cuando la lectura y validación ha finalizado con éxito, sustituimos la memoria
        inventario.getProductos().clear();
        inventario.getProductos().addAll(listaRecuperada);

        // 5. Mostramos el mensaje de éxito final
        System.out.println(ColoresConsola.pintar("Inventario recuperado correctamente.", ColoresConsola.VERDE));
        System.out.println("Productos disponibles: " + inventario.getProductos().size());
    }
}
