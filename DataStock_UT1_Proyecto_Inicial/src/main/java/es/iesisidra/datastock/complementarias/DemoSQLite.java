package es.iesisidra.datastock.complementarias;

import es.iesisidra.datastock.util.ColoresConsola;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Actividad técnica complementaria obligatoria (RA2-b).
 * <p>Demostración práctica del funcionamiento de un Sistema Gestor de Bases
 * de Datos relacional embebido (SQLite) frente a uno cliente-servidor (MySQL).</p>
 * <p>Esta clase es ejecutable de manera independiente de los menús de DataStock:</p>
 * {@code mvn exec:java -Dexec.mainClass=es.iesisidra.datastock.complementarias.DemoSQLite}
 */
public class DemoSQLite {

    private static final String RUTA_BD = "datos/sqlite/productos_demo.db";
    private static final String JDBC_URL = "jdbc:sqlite:" + RUTA_BD;

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   ACTIVIDAD COMPLEMENTARIA: SQLite EMBEBIDO");
        System.out.println("=================================================");

        // 1. Asegurar la existencia del directorio contenedor
        File dir = new File("datos/sqlite");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 2. Establecer la conexión JDBC con SQLite y gestionar recursos
        try (Connection conn = DriverManager.getConnection(JDBC_URL)) {
            System.out.println(ColoresConsola.pintar("[OK] Conexión SQLite abierta: " + RUTA_BD, ColoresConsola.VERDE));

            // 3. Crear la tabla de demostración si no existe
            String createTableSql = "CREATE TABLE IF NOT EXISTS producto_demo (" +
                                    "codigo TEXT PRIMARY KEY NOT NULL, " +
                                    "nombre TEXT NOT NULL" +
                                    ");";
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createTableSql);
                System.out.println(ColoresConsola.pintar("[OK] Tabla producto_demo disponible.", ColoresConsola.VERDE));
            }

            // 4. Inserción con control de duplicados mediante INSERT OR IGNORE
            String insertSql = "INSERT OR IGNORE INTO producto_demo (codigo, nombre) VALUES (?, ?)";
            int insertados = 0;
            int yaExistentes = 0;

            String[][] datosDemo = {
                {"SQL-001", "Producto demo A"},
                {"SQL-002", "Producto demo B"}
            };

            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                for (String[] fila : datosDemo) {
                    ps.setString(1, fila[0]);
                    ps.setString(2, fila[1]);
                    int filasAfectadas = ps.executeUpdate();
                    if (filasAfectadas > 0) {
                        insertados++;
                    } else {
                        yaExistentes++;
                    }
                }
            }

            System.out.println("Insertados: " + insertados + " | Ya existentes: " + yaExistentes);

            // 5. Consulta y recorrido del ResultSet
            String selectSql = "SELECT codigo, nombre FROM producto_demo ORDER BY codigo ASC";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(selectSql)) {
                while (rs.next()) {
                    String codigo = rs.getString("codigo");
                    String nombre = rs.getString("nombre");
                    System.out.println(codigo + " | " + nombre);
                }
            }

            File archivoBd = new File(RUTA_BD);
            System.out.println("\n[OK] Fichero físico de base de datos persistente: " + archivoBd.getAbsolutePath() + " (" + archivoBd.length() + " bytes).");

        } catch (SQLException ex) {
            System.out.println(ColoresConsola.pintar("[ERROR] Error en demostración SQLite: " + ex.getMessage(), ColoresConsola.ROJO));
        } finally {
            try {
                com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
            } catch (Throwable ignored) { }
        }
    }
}
