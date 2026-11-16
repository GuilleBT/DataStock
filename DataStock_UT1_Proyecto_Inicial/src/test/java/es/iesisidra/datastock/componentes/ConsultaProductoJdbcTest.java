package es.iesisidra.datastock.componentes;

import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.modelo.ProductoDetalleDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas automatizadas con JUnit 5 para el componente {@link ConsultaProductoJdbc} (UT3).
 *
 * <p>Verifica la prueba de aceptación obligatoria CP-COMP-03 frente al motor MySQL
 * mediante JDBC, comprobando códigos existentes e inexistentes, así como la correcta
 * semántica del resultado.</p>
 *
 * @author Guillermo Beltrán Tabares
 * @version 1.0 (UT3)
 */
class ConsultaProductoJdbcTest {

    private ConsultaProductoJdbc componente;

    @BeforeEach
    void setUp() {
        componente = new ConsultaProductoJdbc();
    }

    @Test
    @DisplayName("CP-COMP-03 (Parte 1): Buscar en MySQL un código existente devuelve ficha completa")
    void testBuscarProductoExistente_CP_COMP_03_Parte1() throws SQLException {
        // En la base de datos MySQL datastock existe el producto TEC-001
        String codigoExistente = "TEC-001";

        Optional<ProductoDetalleDTO> opt = componente.buscarPorCodigo(codigoExistente);

        assertTrue(opt.isPresent(), "El producto con código " + codigoExistente + " debe existir en la BD");

        ProductoDetalleDTO p = opt.get();
        assertEquals("TEC-001", p.getCodigo());
        assertNotNull(p.getNombre(), "El nombre no puede ser nulo");
        assertFalse(p.getNombre().isBlank(), "El nombre no puede estar vacío");
        assertTrue(p.getPrecio().compareTo(BigDecimal.ZERO) > 0, "El precio debe ser positivo");
        assertTrue(p.getStock() >= 0, "El stock debe ser no negativo");
        assertNotNull(p.getCategoriaNombre(), "Debe recuperar el nombre de la categoría vía JOIN");
        assertNotNull(p.getProveedorNombre(), "Debe recuperar el nombre del proveedor vía JOIN");
        assertTrue(p.getCategoriaId() > 0, "El ID de categoría debe ser válido");
        assertTrue(p.getProveedorId() > 0, "El ID de proveedor debe ser válido");

        // Verificamos compatibilidad con el método buscar() que devuelve DTO o null
        ProductoDetalleDTO dtoDirecto = componente.buscar(codigoExistente);
        assertNotNull(dtoDirecto);
        assertEquals("TEC-001", dtoDirecto.getCodigo());
    }

    @Test
    @DisplayName("CP-COMP-03 (Parte 2): Buscar en MySQL un código inexistente devuelve Optional vacío")
    void testBuscarProductoInexistente_CP_COMP_03_Parte2() throws SQLException {
        String codigoInexistente = "COD-NO-EXISTE-999";

        Optional<ProductoDetalleDTO> opt = componente.buscarPorCodigo(codigoInexistente);

        assertTrue(opt.isEmpty(), "Para un producto inexistente el Optional debe estar vacío");
        assertNull(componente.buscar(codigoInexistente), "El método buscar() debe devolver null para un código inexistente");
    }

    @Test
    @DisplayName("Robustez: Códigos nulos o en blanco no provocan excepciones y devuelven vacío")
    void testCodigosNulosOVacios() throws SQLException {
        assertTrue(componente.buscarPorCodigo(null).isEmpty());
        assertTrue(componente.buscarPorCodigo("").isEmpty());
        assertTrue(componente.buscarPorCodigo("     ").isEmpty());

        assertNull(componente.buscar(null));
        assertNull(componente.buscar("   "));

        assertFalse(componente.existe(null));
        assertFalse(componente.existe("   "));
    }

    @Test
    @DisplayName("Conversión de entidad: buscarProductoSimple devuelve el modelo base Producto")
    void testBuscarProductoSimple() throws SQLException {
        Optional<Producto> opt = componente.buscarProductoSimple("TEC-001");

        assertTrue(opt.isPresent());
        Producto p = opt.get();
        assertEquals("TEC-001", p.getCodigo());
        assertNotNull(p.getNombre());
        assertTrue(p.getPrecio().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("Comprobación de existencia: existe() devuelve booleano determinista")
    void testExiste() throws SQLException {
        assertTrue(componente.existe("TEC-001"), "TEC-001 debe existir en MySQL");
        assertFalse(componente.existe("COD-FANTASMA-404"), "Un código ficticio debe devolver false");
    }
}
