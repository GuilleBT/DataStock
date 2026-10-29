package es.iesisidra.datastock.complementarias;

import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Actividad técnica complementaria RF-FIC-08: Acceso aleatorio a un fichero binario de stock.
 * <p>Esta clase es un ejecutable independiente que implementa la persistencia binaria con
 * registros de longitud fija (36 bytes) mediante {@link RandomAccessFile}. Permite consultar
 * y modificar cualquier registro en tiempo constante O(1) usando desplazamientos calculados
 * con {@code seek()}, sin necesidad de recorrer secuencialmente el archivo.</p>
 */
public final class AccesoAleatorioActividad {

    /** Tamaño fijo de cada registro en bytes: 12 chars (24 bytes) + double (8 bytes) + int (4 bytes) = 36 bytes. */
    public static final int TAMANO_REGISTRO = 36;
    /** Longitud fija del código de producto en caracteres Java (UTF-16 BE). */
    public static final int LONGITUD_CODIGO = 12;
    /** Desplazamiento relativo del campo stock dentro de cada registro (24 bytes código + 8 bytes precio = 32). */
    public static final int OFFSET_STOCK = 32;

    /** Ruta por defecto para almacenar el fichero binario de registros. */
    private static final String RUTA_POR_DEFECTO = "datos/salidas/stock.bin";

    /** Copia independiente de productos en memoria para alimentar la generación del fichero binario. */
    private static final List<Producto> inventarioMemoria = new ArrayList<>();

    private AccesoAleatorioActividad() { }

    public static void main(String[] args) {
        System.out.println(ColoresConsola.pintar("\n==================================================", ColoresConsola.CIAN));
        System.out.println(ColoresConsola.pintar("     DATASTOCK - ACCESO ALEATORIO CON RANDOM      ", ColoresConsola.CIAN));
        System.out.println(ColoresConsola.pintar("              ACTIVIDAD RF-FIC-08                 ", ColoresConsola.CIAN));
        System.out.println(ColoresConsola.pintar("==================================================", ColoresConsola.CIAN));

        String rutaFichero = RUTA_POR_DEFECTO;
        int opcion;

        do {
            System.out.println("\n===== ACCESO ALEATORIO =====");
            System.out.println("Fichero actual: " + rutaFichero);
            System.out.println("Productos en memoria: " + inventarioMemoria.size());
            System.out.println("1. Cargar 4 productos de prueba en memoria (CP-RAF-01)");
            System.out.println("2. Generar fichero 'stock.bin' desde los productos en memoria");
            System.out.println("3. Listar todos los registros de 'stock.bin' (secuencial)");
            System.out.println("4. Consultar registro por posición lógica (seek directo)");
            System.out.println("5. Modificar únicamente el stock por posición (seek a offset + 32)");
            System.out.println("6. Cambiar ruta del fichero binario");
            System.out.println("0. Salir");

            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 6);

            switch (opcion) {
                case 1 -> cargarProductosPrueba();
                case 2 -> generarFicheroBinario(rutaFichero);
                case 3 -> listarTodosLosRegistros(rutaFichero);
                case 4 -> consultarPorPosicion(rutaFichero);
                case 5 -> modificarStockPorPosicion(rutaFichero);
                case 6 -> {
                    String nuevaRuta = LectorConsola.leerTexto("Introduce la nueva ruta del fichero binario: ").trim();
                    if (!nuevaRuta.isEmpty()) {
                        rutaFichero = nuevaRuta;
                        System.out.println(ColoresConsola.pintar("Ruta actualizada a: " + rutaFichero, ColoresConsola.VERDE));
                    }
                }
                case 0 -> System.out.println("Saliendo de la actividad de acceso aleatorio.");
                default -> throw new IllegalStateException("Opción inesperada");
            }

        } while (opcion != 0);
    }

    /**
     * Carga 4 productos de ejemplo en la memoria de la actividad para facilitar la verificación
     * de los casos de aceptación CP-RAF-01, CP-RAF-02 y CP-RAF-03.
     */
    private static void cargarProductosPrueba() {
        inventarioMemoria.clear();
        inventarioMemoria.add(new Producto("TEC-001", "Teclado USB", new BigDecimal("19.95"), 12, 1, 1));
        inventarioMemoria.add(new Producto("RAT-002", "Ratón inalámbrico", new BigDecimal("14.50"), 25, 1, 1));
        inventarioMemoria.add(new Producto("MON-003", "Monitor 24 pulgadas", new BigDecimal("149.99"), 8, 2, 2));
        inventarioMemoria.add(new Producto("AUR-004", "Auriculares Gaming", new BigDecimal("45.50"), 18, 1, 1));

        System.out.println(ColoresConsola.pintar("4 productos cargados en la memoria de la actividad:", ColoresConsola.VERDE));
        for (Producto p : inventarioMemoria) {
            System.out.printf(" - %s: %s | %.2f € | stock %d%n", p.getCodigo(), p.getNombre(), p.getPrecio(), p.getStock());
        }
    }

    /**
     * 1. Genera el fichero binario a partir de una copia ordenada de los productos en memoria.
     * <p>Si el inventario está vacío, informa y cancela la operación sin crear ningún fichero.
     * Si el fichero ya existe, solicita confirmación y lo trunca con {@code setLength(0)} antes de escribir.</p>
     */
    private static void generarFicheroBinario(String rutaTexto) {
        if (inventarioMemoria.isEmpty()) {
            System.out.println(ColoresConsola.pintar("El inventario está vacío. Carga primero productos en memoria (opción 1).", ColoresConsola.AMARILLO));
            return;
        }

        Path ruta = Paths.get(rutaTexto);
        Path dirPadre = ruta.getParent();

        // Creamos directorios contenedores si no existen
        try {
            if (dirPadre != null && Files.notExists(dirPadre)) {
                Files.createDirectories(dirPadre);
            }
        } catch (IOException e) {
            System.out.println(ColoresConsola.pintar("Error al preparar el directorio destino: " + e.getMessage(), ColoresConsola.ROJO));
            return;
        }

        // Si el archivo ya existe, pedimos confirmación de sobrescritura
        if (Files.exists(ruta)) {
            boolean deseaSobrescribir = LectorConsola.confirmar("El fichero '" + ruta.getFileName() + "' ya existe. ¿Deseas sobrescribirlo?");
            if (!deseaSobrescribir) {
                System.out.println(ColoresConsola.pintar("Operación cancelada. El fichero binario no se ha modificado.", ColoresConsola.AMARILLO));
                return;
            }
        }

        // Ordenamos una copia defensiva por código alfabéticamente
        List<Producto> copia = new ArrayList<>(inventarioMemoria);
        copia.sort(Comparator.comparing(Producto::getCodigo, String.CASE_INSENSITIVE_ORDER));

        File archivoFisico = ruta.toFile();
        try (RandomAccessFile raf = new RandomAccessFile(archivoFisico, "rw")) {
            // Truncamos el fichero a 0 bytes para asegurar un archivo limpio antes de escribir
            raf.setLength(0);

            for (Producto p : copia) {
                // 1) Código de 12 caracteres (24 bytes): formateado y rellenado con espacios
                String codigoFormateado = formatearCodigo(p.getCodigo());
                raf.writeChars(codigoFormateado);

                // 2) Precio en double (8 bytes)
                double precioDouble = (p.getPrecio() != null) ? p.getPrecio().doubleValue() : 0.0;
                raf.writeDouble(precioDouble);

                // 3) Stock en int (4 bytes)
                raf.writeInt(p.getStock());
            }

            long tamanoFinal = raf.length();
            System.out.println(ColoresConsola.pintar("\nFichero binario generado correctamente.", ColoresConsola.VERDE));
            System.out.println("Ruta: " + ruta.toAbsolutePath());
            System.out.println("Registros generados: " + copia.size());
            System.out.println("Tamaño total: " + tamanoFinal + " bytes (" + copia.size() + " registros × " + TAMANO_REGISTRO + " bytes)");

        } catch (IOException e) {
            System.out.println(ColoresConsola.pintar("Error de E/S al escribir en el fichero binario: " + e.getMessage(), ColoresConsola.ROJO));
        }
    }

    /**
     * 3. Muestra todos los registros del fichero binario mediante lectura secuencial.
     */
    private static void listarTodosLosRegistros(String rutaTexto) {
        File archivo = new File(rutaTexto);
        if (!archivo.exists() || !archivo.isFile()) {
            System.out.println(ColoresConsola.pintar("Error: El fichero binario no existe: " + rutaTexto, ColoresConsola.ROJO));
            return;
        }

        try (RandomAccessFile raf = new RandomAccessFile(archivo, "r")) {
            long totalRegistros = validarTamanoYObtenerRegistros(raf);
            if (totalRegistros <= 0) {
                return;
            }

            System.out.println(ColoresConsola.pintar("\n===== LISTADO COMPLETO DE STOCK.BIN =====", ColoresConsola.CIAN));
            System.out.println("Fichero: " + rutaTexto + " | Registros: " + totalRegistros + " | Tamaño: " + raf.length() + " bytes");
            System.out.println("---------------------------------------------------------");

            raf.seek(0);
            for (int i = 0; i < totalRegistros; i++) {
                String codigo = leerCodigo(raf);
                double precio = raf.readDouble();
                int stock = raf.readInt();
                System.out.printf("Registro [%d] -> Código: %-12s | Precio: %8.2f € | Stock: %d%n", i, codigo, precio, stock);
            }
            System.out.println("---------------------------------------------------------");

        } catch (IOException e) {
            System.out.println(ColoresConsola.pintar("Error al leer el fichero: " + e.getMessage(), ColoresConsola.ROJO));
        }
    }

    /**
     * 4. Consulta un registro concreto por su posición lógica en tiempo O(1) usando {@code seek(posicion * 36)}.
     * <p>Caso CP-RAF-02: Acceso directo al registro solicitado sin recorrer los anteriores.</p>
     */
    private static void consultarPorPosicion(String rutaTexto) {
        File archivo = new File(rutaTexto);
        if (!archivo.exists() || !archivo.isFile()) {
            System.out.println(ColoresConsola.pintar("Error: El fichero binario no existe: " + rutaTexto, ColoresConsola.ROJO));
            return;
        }

        try (RandomAccessFile raf = new RandomAccessFile(archivo, "r")) {
            long totalRegistros = validarTamanoYObtenerRegistros(raf);
            if (totalRegistros <= 0) {
                return;
            }

            System.out.println("Fichero: " + rutaTexto);
            System.out.println("Registros: " + totalRegistros + " | Tamaño: " + raf.length() + " bytes");

            int pos = LectorConsola.leerEntero("Consultar posición (0-" + (totalRegistros - 1) + "): ");

            // Validación de límites: si está fuera de rango, rechazar (Caso CP-RAF-04)
            if (pos < 0 || pos >= totalRegistros) {
                System.out.println(ColoresConsola.pintar("Error: Posición fuera de rango. Debe ser entre 0 y " + (totalRegistros - 1) + ".", ColoresConsola.ROJO));
                return;
            }

            // Calculamos el puntero de desplazamiento directo O(1): posición * 36
            long desplazamiento = (long) pos * TAMANO_REGISTRO;
            raf.seek(desplazamiento);

            String codigo = leerCodigo(raf);
            double precio = raf.readDouble();
            int stock = raf.readInt();

            System.out.println(ColoresConsola.pintar("\n--- Registro consultado (Posición " + pos + ") ---", ColoresConsola.VERDE));
            System.out.printf("%s | %.2f € | stock %d%n", codigo, precio, stock);

        } catch (IOException e) {
            System.out.println(ColoresConsola.pintar("Error al acceder al registro: " + e.getMessage(), ColoresConsola.ROJO));
        }
    }

    /**
     * 5. Modifica únicamente el campo stock de un registro específico usando {@code seek(posicion * 36 + 32)}.
     * <p>Caso CP-RAF-03: Solo cambia ese campo en disco. El registro no se reescribe entero
     * y los productos en memoria no se modifican.</p>
     */
    private static void modificarStockPorPosicion(String rutaTexto) {
        File archivo = new File(rutaTexto);
        if (!archivo.exists() || !archivo.isFile()) {
            System.out.println(ColoresConsola.pintar("Error: El fichero binario no existe: " + rutaTexto, ColoresConsola.ROJO));
            return;
        }

        try (RandomAccessFile raf = new RandomAccessFile(archivo, "rw")) {
            long totalRegistros = validarTamanoYObtenerRegistros(raf);
            if (totalRegistros <= 0) {
                return;
            }

            System.out.println("Fichero: " + rutaTexto);
            System.out.println("Registros: " + totalRegistros + " | Tamaño: " + raf.length() + " bytes");

            int pos = LectorConsola.leerEntero("Selecciona la posición a modificar (0-" + (totalRegistros - 1) + "): ");

            if (pos < 0 || pos >= totalRegistros) {
                System.out.println(ColoresConsola.pintar("Error: Posición fuera de rango. Debe ser entre 0 y " + (totalRegistros - 1) + ".", ColoresConsola.ROJO));
                return;
            }

            // Leemos primero los datos actuales del registro para mostrarlos
            long inicioRegistro = (long) pos * TAMANO_REGISTRO;
            raf.seek(inicioRegistro);
            String codigo = leerCodigo(raf);
            double precio = raf.readDouble();
            int stockAnterior = raf.readInt();

            System.out.printf("%s | %.2f € | stock %d%n", codigo, precio, stockAnterior);

            // Solicitamos el nuevo valor de stock
            int nuevoStock = LectorConsola.leerEntero("Nuevo stock: ");
            if (nuevoStock < 0) {
                System.out.println(ColoresConsola.pintar("Error: El stock no puede ser un valor negativo.", ColoresConsola.ROJO));
                return;
            }

            // Calculamos el desplazamiento exacto del campo stock: posición * 36 + 32
            long desplazamientoStock = inicioRegistro + OFFSET_STOCK;
            raf.seek(desplazamientoStock);

            // Sobrescribimos ÚNICAMENTE los 4 bytes del entero stock
            raf.writeInt(nuevoStock);

            System.out.println(ColoresConsola.pintar("Actualización correcta: stock " + stockAnterior + " → " + nuevoStock, ColoresConsola.VERDE));
            System.out.println("Aviso: El fichero binario ha sido actualizado. Los objetos en memoria RAM permanecen intactos.");

        } catch (IOException e) {
            System.out.println(ColoresConsola.pintar("Error al modificar el registro: " + e.getMessage(), ColoresConsola.ROJO));
        }
    }

    /**
     * Valida que el tamaño del archivo sea un múltiplo exacto de 36 bytes.
     * Devuelve el total de registros o -1 si el archivo está dañado (Caso CP-RAF-05).
     */
    private static long validarTamanoYObtenerRegistros(RandomAccessFile raf) throws IOException {
        long longitud = raf.length();
        if (longitud % TAMANO_REGISTRO != 0) {
            System.out.println(ColoresConsola.pintar("Error: El fichero es inválido. Su tamaño (" + longitud + " bytes) no es múltiplo de " + TAMANO_REGISTRO + " bytes.", ColoresConsola.ROJO));
            return -1;
        }
        return longitud / TAMANO_REGISTRO;
    }

    /**
     * Lee un código de 12 caracteres desde la posición actual del puntero del fichero.
     */
    private static String leerCodigo(RandomAccessFile raf) throws IOException {
        StringBuilder sb = new StringBuilder(LONGITUD_CODIGO);
        for (int i = 0; i < LONGITUD_CODIGO; i++) {
            sb.append(raf.readChar());
        }
        return sb.toString().trim();
    }

    /**
     * Ajusta la cadena a exactamente 12 caracteres rellenando con espacios si es más corta,
     * o truncando a 12 si superara la longitud máxima.
     */
    private static String formatearCodigo(String codigo) {
        if (codigo == null) {
            codigo = "";
        }
        codigo = codigo.trim();
        if (codigo.length() > LONGITUD_CODIGO) {
            return codigo.substring(0, LONGITUD_CODIGO);
        }
        return String.format("%-" + LONGITUD_CODIGO + "s", codigo);
    }
}
