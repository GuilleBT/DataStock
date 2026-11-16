package es.iesisidra.datastock.componentes;

/**
 * Excepción controlada lanzada por {@link LectorProductosJson} cuando ocurre
 * un error durante el acceso, lectura o análisis sintáctico de un fichero JSON.
 *
 * <p>Permite a los componentes clientes y capas de interfaz capturar y gestionar
 * de forma unificada errores como:</p>
 * <ul>
 *   <li>Fichero no encontrado o inaccesible por permisos.</li>
 *   <li>Ruta vacía o que no corresponde a un fichero regular.</li>
 *   <li>Sintaxis JSON corrupta, malformada o llaves no cerradas.</li>
 *   <li>Estructura de documento no conforme con el esquema esperado (falta de objeto raíz o array "productos").</li>
 * </ul>
 *
 * @author Guillermo Beltrán Tabares
 * @version 1.0 (UT3)
 */
public class LectorJsonException extends Exception {

    /**
     * Construye una nueva excepción con un mensaje de error descriptivo.
     *
     * @param mensaje Explicación legible del motivo del error.
     */
    public LectorJsonException(String mensaje) {
        super(mensaje);
    }

    /**
     * Construye una nueva excepción con un mensaje descriptivo y la causa original.
     *
     * @param mensaje Explicación legible del motivo del error.
     * @param causa   Excepción subyacente que motivó el fallo.
     */
    public LectorJsonException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
