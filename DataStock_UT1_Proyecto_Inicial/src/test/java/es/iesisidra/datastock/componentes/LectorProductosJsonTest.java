package es.iesisidra.datastock.componentes;

import es.iesisidra.datastock.modelo.Producto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas automatizadas con JUnit 5 para el componente {@link LectorProductosJson} (UT3).
 *
 * <p>Verifica las pruebas de aceptación obligatorias CP-COMP-01 y CP-COMP-02,
 * así como casos límite y de robustez adicionales.</p>
 *
 * @author Guillermo Beltrán Tabares
 * @version 1.0 (UT3)
 */
class LectorProductosJsonTest {

    private LectorProductosJson lector;

    @BeforeEach
    void setUp() {
        lector = new LectorProductosJson();
    }

    @Test
    @DisplayName("CP-COMP-01: Importar JSON válido carga productos en memoria y devuelve colección")
    void testLeerJsonValido_CP_COMP_01() throws LectorJsonException {
        Path rutaValida = Paths.get("datos/ejemplos/catalogo_valido.json");

        ResultadoLecturaJson resultado = lector.leerDetallado(rutaValida);

        assertNotNull(resultado, "El resultado de la lectura no debe ser nulo");
        assertEquals(2, resultado.getTotalEncontrados(), "El catálogo debe contener 2 productos");
        assertEquals(0, resultado.getRechazados(), "No debe haber ningún producto rechazado");
        assertFalse(resultado.tieneIncidencias(), "No deben registrarse incidencias");
        assertTrue(resultado.esTotalmenteValido(), "La importación debe ser totalmente válida");

        List<Producto> productos = resultado.getProductosValidos();
        assertEquals(2, productos.size(), "La lista de productos válidos debe tener 2 elementos");

        Producto p1 = productos.get(0);
        assertEquals("TEC-001", p1.getCodigo());
        assertEquals("Teclado USB", p1.getNombre());
        assertEquals(new BigDecimal("19.95"), p1.getPrecio());
        assertEquals(12, p1.getStock());
        assertEquals(1, p1.getCategoriaId());
        assertEquals(1, p1.getProveedorId());

        Producto p2 = productos.get(1);
        assertEquals("RAT-002", p2.getCodigo());
        assertEquals("Ratón inalámbrico", p2.getNombre());
        assertEquals(new BigDecimal("14.50"), p2.getPrecio());
        assertEquals(25, p2.getStock());

        // Comprobación de método directo leerProductos
        List<Producto> productosDirectos = lector.leerProductos(rutaValida);
        assertEquals(2, productosDirectos.size());
    }

    @Test
    @DisplayName("CP-COMP-02 (Parte 1): Importar JSON inexistente lanza LectorJsonException controlada")
    void testLeerJsonInexistente_CP_COMP_02_Parte1() {
        Path rutaInexistente = Paths.get("datos/ejemplos/fichero_fantasma_no_existe_404.json");

        LectorJsonException ex = assertThrows(
                LectorJsonException.class,
                () -> lector.leerDetallado(rutaInexistente),
                "Debe lanzar LectorJsonException al intentar leer un fichero inexistente"
        );

        assertTrue(ex.getMessage().contains("no existe"),
                "El mensaje de error debe indicar claramente que el fichero no existe");
    }

    @Test
    @DisplayName("CP-COMP-02 (Parte 2): Importar JSON malformado lanza LectorJsonException controlada")
    void testLeerJsonMalformado_CP_COMP_02_Parte2() {
        Path rutaMalformada = Paths.get("datos/ejemplos/catalogo_malformado.json");

        LectorJsonException ex = assertThrows(
                LectorJsonException.class,
                () -> lector.leerDetallado(rutaMalformada),
                "Debe lanzar LectorJsonException si la sintaxis JSON está rota o incompleta"
        );

        assertTrue(ex.getMessage().contains("sintaxis JSON incorrecta") || ex.getMessage().contains("mal formado"),
                "El mensaje debe indicar que la sintaxis JSON es incorrecta o está mal formada");
    }

    @Test
    @DisplayName("Robustez: Catálogo con errores discrimina productos válidos y registra incidencias")
    void testLeerJsonConErroresParciales() throws LectorJsonException {
        Path rutaConErrores = Paths.get("datos/ejemplos/catalogo_con_errores.json");

        ResultadoLecturaJson resultado = lector.leerDetallado(rutaConErrores);

        assertNotNull(resultado);
        assertTrue(resultado.getTotalEncontrados() > 0, "Debe procesar los elementos del archivo");
        assertTrue(resultado.getRechazados() > 0, "Debe haber productos rechazados");
        assertTrue(resultado.tieneIncidencias(), "Debe reportar incidencias");
        assertFalse(resultado.getIncidencias().isEmpty(), "La lista de incidencias no debe estar vacía");
        assertFalse(resultado.getProductosValidos().isEmpty(), "Debe recuperar los productos que sí son válidos");
    }

    @Test
    @DisplayName("Validación cruzada: Detectar duplicados frente a productos ya cargados en inventario")
    void testDetectarDuplicadosMemoria() throws LectorJsonException {
        Path rutaValida = Paths.get("datos/ejemplos/catalogo_valido.json");

        List<Producto> memoriaExistente = new ArrayList<>();
        memoriaExistente.add(new Producto("TEC-001", "Teclado Previo", new BigDecimal("50.00"), 5, 1, 1));

        ResultadoLecturaJson resultado = lector.leerDetallado(rutaValida, memoriaExistente);

        // TEC-001 ya estaba en memoria, por lo que solo RAT-002 debe ser importado y TEC-001 rechazado por duplicidad
        assertEquals(2, resultado.getTotalEncontrados());
        assertEquals(1, resultado.getProductosValidos().size(), "Solo 1 producto debe ser aceptado");
        assertEquals("RAT-002", resultado.getProductosValidos().get(0).getCodigo());
        assertEquals(1, resultado.getRechazados(), "El producto repetido debe ser rechazado");
        assertTrue(resultado.getIncidencias().get(0).contains("duplicado"));
    }

    @Test
    @DisplayName("Control de parámetros: Ruta nula o vacía lanza LectorJsonException")
    void testParametrosInvalidos() {
        assertThrows(LectorJsonException.class, () -> lector.leerDetallado(null));
        assertThrows(LectorJsonException.class, () -> lector.leerProductos("   "));
        assertThrows(LectorJsonException.class, () -> lector.leerProductos((String) null));
    }
}
