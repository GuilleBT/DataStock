package es.iesisidra.datastock.servicio;

import es.iesisidra.datastock.componentes.LectorJsonException;
import es.iesisidra.datastock.componentes.LectorProductosJson;
import es.iesisidra.datastock.componentes.ResultadoLecturaJson;
import es.iesisidra.datastock.util.ColoresConsola;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Servicio encargado de importar catálogos de productos en formato JSON (RF-FIC-02).
 * <p>En la UT3 delega la lectura y validación en el componente reutilizable desacoplado {@link LectorProductosJson}.</p>
 *
 * @author Guillermo Beltrán Tabares
 * @version 1.0 (UT3)
 */
public class ImportadorJsonServicio {

    private final LectorProductosJson lectorProductosJson = new LectorProductosJson();

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

        System.out.println(ColoresConsola.pintar("\n===== IMPORTAR JSON =====", ColoresConsola.CIAN));
        System.out.println("Ruta: " + rutaTexto);

        try {
            Path ruta = Paths.get(rutaTexto.trim());
            ResultadoLecturaJson resultado = lectorProductosJson.leerDetallado(ruta, inventario.getProductos());

            inventario.getProductos().addAll(resultado.getProductosValidos());

            System.out.println("Productos encontrados: " + resultado.getTotalEncontrados());
            System.out.println("Importados: " + resultado.getProductosValidos().size() + " | Rechazados: " + resultado.getRechazados());

            if (resultado.tieneIncidencias()) {
                System.out.println(ColoresConsola.pintar("INCIDENCIAS:", ColoresConsola.AMARILLO));
                for (String inc : resultado.getIncidencias()) {
                    System.out.println(" - " + inc);
                }
                System.out.println(ColoresConsola.pintar("Importación finalizada con incidencias.", ColoresConsola.AMARILLO));
            } else {
                System.out.println(ColoresConsola.pintar("Importación finalizada con éxito.", ColoresConsola.VERDE));
            }
        } catch (LectorJsonException e) {
            System.out.println(ColoresConsola.pintar("Error: " + e.getMessage(), ColoresConsola.ROJO));
        } catch (Exception e) {
            System.out.println(ColoresConsola.pintar("Error inesperado: " + e.getMessage(), ColoresConsola.ROJO));
        }
    }
}
