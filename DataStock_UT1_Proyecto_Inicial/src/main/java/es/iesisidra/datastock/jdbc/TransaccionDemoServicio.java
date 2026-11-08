package es.iesisidra.datastock.jdbc;

import es.iesisidra.datastock.util.ColoresConsola;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Servicio de demostración técnica de control de transacciones JDBC mediante
 * operaciones explícitas de commit y rollback (RF-JDBC-08).
 */
public class TransaccionDemoServicio {

    private static final int ID_CAT_A1 = 9901;
    private static final int ID_CAT_A2 = 9902;
    private static final int ID_CAT_B1 = 9903;

    /**
     * Ejecuta la demostración completa de los escenarios de commit y rollback.
     */
    public void ejecutarDemostracion() {
        System.out.println("\n=======================================================");
        System.out.println("  DEMOSTRACIÓN DE TRANSACCIONES JDBC (COMMIT / ROLLBACK)");
        System.out.println("=======================================================");
        System.out.println("Esta prueba técnica verifica el control manual de transacciones desde Java.");
        System.out.println("Se emplearán identificadores de categorías de prueba (" + ID_CAT_A1 + ", " + ID_CAT_A2 + ", " + ID_CAT_B1 + ").");

        limpiarDatosPrueba();

        // ----------------------------------------------------
        // Escenario A: Confirmación exitosa con commit()
        // ----------------------------------------------------
        System.out.println("\n--- ESCENARIO A: Confirmación de transacción (COMMIT) ---");
        System.out.println("1. Desactivando confirmación automática con setAutoCommit(false)...");

        String insertSql = "INSERT INTO categoria (id, nombre) VALUES (?, ?)";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                System.out.println("2. Insertando categoría de prueba [" + ID_CAT_A1 + "] 'Cat Demo Commit 1'...");
                ps.setInt(1, ID_CAT_A1);
                ps.setString(2, "Cat Demo Commit 1");
                ps.executeUpdate();

                System.out.println("3. Insertando categoría de prueba [" + ID_CAT_A2 + "] 'Cat Demo Commit 2'...");
                ps.setInt(1, ID_CAT_A2);
                ps.setString(2, "Cat Demo Commit 2");
                ps.executeUpdate();

                System.out.println("4. Ejecutando conn.commit()...");
                conn.commit();
                System.out.println(ColoresConsola.pintar("[OK] Transacción A confirmada con éxito (COMMIT).", ColoresConsola.VERDE));

            } catch (SQLException ex) {
                conn.rollback();
                System.out.println(ColoresConsola.pintar("[ERROR] Fallo inesperado en Escenario A: " + ex.getMessage(), ColoresConsola.ROJO));
            } finally {
                conn.setAutoCommit(true);
            }

            // Comprobación de persistencia con SELECT
            boolean existeA1 = comprobarExistenciaCategoria(conn, ID_CAT_A1);
            boolean existeA2 = comprobarExistenciaCategoria(conn, ID_CAT_A2);
            System.out.println("Comprobación en MySQL: Categoría " + ID_CAT_A1 + " existe: " + existeA1 + " | Categoría " + ID_CAT_A2 + " existe: " + existeA2);

        } catch (SQLException e) {
            System.out.println(ColoresConsola.pintar("[ERROR] Error de conexión: " + e.getMessage(), ColoresConsola.ROJO));
        }

        // ----------------------------------------------------
        // Escenario B: Deshacer cambios con rollback()
        // ----------------------------------------------------
        System.out.println("\n--- ESCENARIO B: Reversión de transacción ante error (ROLLBACK) ---");
        System.out.println("1. Iniciando nueva transacción con setAutoCommit(false)...");

        try (Connection conn = ConexionBD.obtenerConexion()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                System.out.println("2. Insertando categoría inicial [" + ID_CAT_B1 + "] 'Cat Demo Rollback'...");
                ps.setInt(1, ID_CAT_B1);
                ps.setString(2, "Cat Demo Rollback");
                ps.executeUpdate();
                System.out.println("   (Primera inserción realizada en el contexto de la transacción, aún no confirmada)");

                System.out.println("3. Provocando fallo intencionado: duplicar clave primaria existente [" + ID_CAT_A1 + "]...");
                ps.setInt(1, ID_CAT_A1); // Ya existe por el Escenario A
                ps.setString(2, "Cat Duplicada Error");
                ps.executeUpdate(); // Debe lanzar SQLException por PK duplicada

                conn.commit(); // No debe alcanzarse
            } catch (SQLException ex) {
                System.out.println(ColoresConsola.pintar("   -> Excepción capturada esperada: " + ex.getMessage(), ColoresConsola.AMARILLO));
                System.out.println("4. Ejecutando conn.rollback() para revertir todas las operaciones del lote...");
                conn.rollback();
                System.out.println(ColoresConsola.pintar("[OK] Transacción B revertida correctamente (ROLLBACK).", ColoresConsola.VERDE));
            } finally {
                conn.setAutoCommit(true);
            }

            // Comprobación de que la categoría B1 NO permanece en MySQL
            boolean existeB1 = comprobarExistenciaCategoria(conn, ID_CAT_B1);
            System.out.println("Comprobación en MySQL: Categoría inicial " + ID_CAT_B1 + " existe en BD: " + existeB1);
            if (!existeB1) {
                System.out.println(ColoresConsola.pintar("[OK] Verificado: Ningún cambio parcial del intento fallido ha quedado en la base de datos.", ColoresConsola.VERDE));
            } else {
                System.out.println(ColoresConsola.pintar("[ERROR] La categoría " + ID_CAT_B1 + " no debió persistir.", ColoresConsola.ROJO));
            }

        } catch (SQLException e) {
            System.out.println(ColoresConsola.pintar("[ERROR] Error de conexión: " + e.getMessage(), ColoresConsola.ROJO));
        }

        // Limpieza final de registros de prueba
        limpiarDatosPrueba();
        System.out.println("\n[OK] Datos de demostración limpiados correctamente.");
    }

    private boolean comprobarExistenciaCategoria(Connection conn, int id) {
        String sql = "SELECT 1 FROM categoria WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    private void limpiarDatosPrueba() {
        String sql = "DELETE FROM categoria WHERE id IN (?, ?, ?)";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ID_CAT_A1);
            ps.setInt(2, ID_CAT_A2);
            ps.setInt(3, ID_CAT_B1);
            ps.executeUpdate();
        } catch (SQLException ignored) { }
    }
}
