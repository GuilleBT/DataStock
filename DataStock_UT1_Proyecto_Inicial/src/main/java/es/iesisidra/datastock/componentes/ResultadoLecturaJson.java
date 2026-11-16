package es.iesisidra.datastock.componentes;

import es.iesisidra.datastock.modelo.Producto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsula el resultado detallado de la lectura y validación de un catálogo JSON
 * procesado por {@link LectorProductosJson}.
 *
 * <p>Proporciona acceso a:</p>
 * <ul>
 *   <li>La colección de productos sintáctica y semánticamente válidos listos para su uso.</li>
 *   <li>La lista de incidencias y motivos de rechazo encontrados en los elementos inválidos.</li>
 *   <li>Métricas estadísticas de la operación (total encontrados, importados y rechazados).</li>
 * </ul>
 *
 * @author Guillermo Beltrán Tabares
 * @version 1.0 (UT3)
 */
public class ResultadoLecturaJson {

    private final List<Producto> productosValidos;
    private final List<String> incidencias;
    private final int totalEncontrados;
    private final int rechazados;

    /**
     * Construye un nuevo objeto con el resultado de la lectura.
     *
     * @param productosValidos Colección de objetos {@link Producto} validados con éxito.
     * @param incidencias      Lista de mensajes explicativos de elementos rechazados.
     * @param totalEncontrados Número total de elementos presentes en el array JSON original.
     * @param rechazados       Número total de elementos descartados por inconsistencias.
     */
    public ResultadoLecturaJson(List<Producto> productosValidos,
                                List<String> incidencias,
                                int totalEncontrados,
                                int rechazados) {
        this.productosValidos = productosValidos != null
                ? new ArrayList<>(productosValidos)
                : new ArrayList<>();
        this.incidencias = incidencias != null
                ? new ArrayList<>(incidencias)
                : new ArrayList<>();
        this.totalEncontrados = totalEncontrados;
        this.rechazados = rechazados;
    }

    /**
     * Devuelve una vista inmodificable de los productos válidos extraídos del JSON.
     *
     * @return Lista inmodificable de objetos {@link Producto}.
     */
    public List<Producto> getProductosValidos() {
        return Collections.unmodifiableList(productosValidos);
    }

    /**
     * Devuelve una vista inmodificable de las incidencias registradas durante el procesamiento.
     *
     * @return Lista inmodificable de cadenas descriptivas con las incidencias.
     */
    public List<String> getIncidencias() {
        return Collections.unmodifiableList(incidencias);
    }

    /**
     * Devuelve el total de elementos analizados en el array raíz del fichero JSON.
     *
     * @return Cantidad de elementos encontrados.
     */
    public int getTotalEncontrados() {
        return totalEncontrados;
    }

    /**
     * Devuelve el número de productos rechazados debido a validaciones fallidas.
     *
     * @return Cantidad de elementos rechazados.
     */
    public int getRechazados() {
        return rechazados;
    }

    /**
     * Indica si se produjo alguna incidencia o rechazo durante la lectura.
     *
     * @return {@code true} si existen incidencias o rechazados; {@code false} si todos los elementos fueron válidos.
     */
    public boolean tieneIncidencias() {
        return !incidencias.isEmpty() || rechazados > 0;
    }

    /**
     * Indica si el proceso de lectura fue 100% limpio y todos los elementos encontrados se importaron.
     *
     * @return {@code true} si se importaron todos los elementos sin ninguna incidencia.
     */
    public boolean esTotalmenteValido() {
        return totalEncontrados > 0 && rechazados == 0 && incidencias.isEmpty();
    }
}
