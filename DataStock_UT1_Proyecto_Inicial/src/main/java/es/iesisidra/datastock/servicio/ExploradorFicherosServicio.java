package es.iesisidra.datastock.servicio;

import es.iesisidra.datastock.util.ColoresConsola;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Servicio encargado de la exploración de rutas, ficheros y directorios (RF-FIC-01).
 * <p>Utiliza la API moderna de Java NIO.2 (Path, Files y DirectoryStream) para
 * verificar la existencia de rutas, distinguir entre ficheros y carpetas y listar
 * contenidos inmediatos de forma eficiente.</p>
 */
public class ExploradorFicherosServicio {

    /**
     * Explora la ruta indicada por parámetro y muestra la información requerida por consola.
     *
     * @param rutaTexto Cadena con la ruta introducida por el usuario.
     */
    public void explorar(String rutaTexto) {
        if (rutaTexto == null || rutaTexto.trim().isEmpty()) {
            System.out.println(ColoresConsola.pintar("Error: La ruta no puede estar vacía.", ColoresConsola.ROJO));
            return;
        }

        Path ruta = Paths.get(rutaTexto.trim());

        // Comprobación de existencia (CP-FIC-02)
        if (Files.notExists(ruta)) {
            System.out.println(ColoresConsola.pintar("Error: La ruta indicada no existe: " + rutaTexto, ColoresConsola.ROJO));
            return;
        }

        System.out.println(ColoresConsola.pintar("\n===== EXPLORAR RUTA =====", ColoresConsola.CIAN));
        System.out.println("Ruta analizada: " + rutaTexto);

        // Caso 1: La ruta corresponde a un fichero regular (CP-FIC-03)
        if (Files.isRegularFile(ruta)) {
            try {
                long tamanoBytes = Files.size(ruta);
                System.out.println("Tipo          : [FICHERO]");
                System.out.println("Nombre        : " + (ruta.getFileName() != null ? ruta.getFileName() : ruta));
                System.out.println("Ruta absoluta : " + ruta.toAbsolutePath());
                System.out.println("Tamaño        : " + tamanoBytes + " bytes");
            } catch (IOException e) {
                System.out.println(ColoresConsola.pintar("Error al obtener los datos del fichero: " + e.getMessage(), ColoresConsola.ROJO));
            }
            return;
        }

        // Caso 2: La ruta corresponde a un directorio (CP-FIC-01)
        if (Files.isDirectory(ruta)) {
            int totalFicheros = 0;
            int totalDirectorios = 0;
            boolean tieneElementos = false;

            // Recorremos los hijos inmediatos usando DirectoryStream con try-with-resources
            try (DirectoryStream<Path> flujoDirectorio = Files.newDirectoryStream(ruta)) {
                for (Path elemento : flujoDirectorio) {
                    tieneElementos = true;
                    String nombreElemento = elemento.getFileName().toString();

                    if (Files.isDirectory(elemento)) {
                        totalDirectorios++;
                        System.out.printf("[DIR]     %-25s%n", nombreElemento);
                    } else if (Files.isRegularFile(elemento)) {
                        totalFicheros++;
                        long tamanoBytes = 0;
                        try {
                            tamanoBytes = Files.size(elemento);
                        } catch (IOException ignored) {
                            // Si falla la obtención de tamaño para un elemento puntual, se mantiene 0
                        }
                        System.out.printf("[FICHERO] %-25s %d bytes%n", nombreElemento, tamanoBytes);
                    } else {
                        System.out.printf("[OTRO]    %-25s%n", nombreElemento);
                    }
                }

                if (!tieneElementos) {
                    System.out.println(ColoresConsola.pintar("El directorio está vacío.", ColoresConsola.AMARILLO));
                }

                System.out.println(ColoresConsola.pintar("------------------------------------------------", ColoresConsola.GRIS));
                System.out.printf("Total ficheros: %d | Directorios: %d%n", totalFicheros, totalDirectorios);

            } catch (IOException e) {
                System.out.println(ColoresConsola.pintar("Error al leer el directorio: " + e.getMessage(), ColoresConsola.ROJO));
            } catch (SecurityException e) {
                System.out.println(ColoresConsola.pintar("Permiso denegado para acceder a la ruta: " + e.getMessage(), ColoresConsola.ROJO));
            }
            return;
        }

        // Caso residual: elemento especial no reconocido como fichero regular ni directorio
        System.out.println(ColoresConsola.pintar("La ruta no es ni un fichero regular ni un directorio válido.", ColoresConsola.AMARILLO));
    }
}
