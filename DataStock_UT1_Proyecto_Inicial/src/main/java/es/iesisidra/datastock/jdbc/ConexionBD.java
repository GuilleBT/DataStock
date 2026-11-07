package es.iesisidra.datastock.jdbc;

import es.iesisidra.datastock.util.ColoresConsola;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Gestor centralizado de conexiones JDBC para DataStock.
 * <p>Obtiene las credenciales de conexión siguiendo este orden de prioridad:</p>
 * <ol>
 *   <li>Fichero local {@code db.properties} (excluido de Git por seguridad).</li>
 *   <li>Variables de entorno del sistema ({@code JDBC_URL}, {@code JDBC_USER}, {@code JDBC_PASSWORD}).</li>
 *   <li>Valores por defecto de desarrollo local.</li>
 * </ol>
 */
public final class ConexionBD {

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/datastock?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "datastock";

    private ConexionBD() { }

    /**
     * Carga la configuración de conexión desde db.properties o variables de entorno.
     *
     * @return objeto {@link Properties} con las claves db.url, db.user y db.password.
     */
    public static Properties cargarConfiguracion() {
        Properties props = new Properties();

        // 1. Intentar cargar desde db.properties en rutas habituales
        String[] posiblesRutas = {
            "db.properties",
            "DataStock_UT1_Proyecto_Inicial/db.properties",
            "../db.properties"
        };

        boolean cargado = false;
        for (String ruta : posiblesRutas) {
            File f = new File(ruta);
            if (f.exists() && f.isFile()) {
                try (InputStream in = new FileInputStream(f)) {
                    props.load(in);
                    cargado = true;
                    break;
                } catch (Exception ignored) { }
            }
        }

        // Si no se encontró en disco, intentar desde el classpath
        if (!cargado) {
            try (InputStream in = ConexionBD.class.getResourceAsStream("/db.properties")) {
                if (in != null) {
                    props.load(in);
                    cargado = true;
                }
            } catch (Exception ignored) { }
        }

        // 2. Si faltan valores, buscar en variables de entorno
        String envUrl = System.getenv("JDBC_URL");
        String envUser = System.getenv("JDBC_USER");
        String envPassword = System.getenv("JDBC_PASSWORD");

        if (envUrl != null && !envUrl.isBlank()) {
            props.setProperty("db.url", envUrl);
        } else if (!props.containsKey("db.url")) {
            props.setProperty("db.url", DEFAULT_URL);
        }

        if (envUser != null && !envUser.isBlank()) {
            props.setProperty("db.user", envUser);
        } else if (!props.containsKey("db.user")) {
            props.setProperty("db.user", DEFAULT_USER);
        }

        if (envPassword != null) {
            props.setProperty("db.password", envPassword);
        } else if (!props.containsKey("db.password")) {
            props.setProperty("db.password", DEFAULT_PASSWORD);
        }

        return props;
    }

    /**
     * Abre y devuelve una nueva conexión con la base de datos MySQL.
     *
     * @return objeto {@link Connection} listo para su uso.
     * @throws SQLException si se produce un error al conectar.
     */
    public static Connection obtenerConexion() throws SQLException {
        Properties config = cargarConfiguracion();
        return DriverManager.getConnection(
            config.getProperty("db.url"),
            config.getProperty("db.user"),
            config.getProperty("db.password")
        );
    }

    /**
     * Abre y devuelve una conexión con credenciales explícitas (útil para pruebas de error).
     *
     * @param url      URL JDBC de conexión.
     * @param usuario  usuario de la base de datos.
     * @param password contraseña.
     * @return objeto {@link Connection}.
     * @throws SQLException si se produce un error de autenticación o conexión.
     */
    public static Connection obtenerConexion(String url, String usuario, String password) throws SQLException {
        return DriverManager.getConnection(url, usuario, password);
    }

    /**
     * RF-JDBC-01: Comprueba la conexión con la base de datos, recupera metadatos del
     * gestor y consulta el total de productos existentes. Gestiona adecuadamente
     * las situaciones de fallo sin abortar la aplicación.
     */
    public static void probarConexion() {
        System.out.println("\n--- COMPROBACIÓN DE CONEXIÓN CON MYSQL ---");
        try (Connection conn = obtenerConexion()) {
            DatabaseMetaData meta = conn.getMetaData();
            String producto = meta.getDatabaseProductName();
            String version = meta.getDatabaseProductVersion();
            String catalogo = conn.getCatalog();

            int totalProductos = 0;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM producto")) {
                if (rs.next()) {
                    totalProductos = rs.getInt(1);
                }
            }

            System.out.println(ColoresConsola.pintar("[OK] Conexión establecida correctamente.", ColoresConsola.VERDE));
            System.out.println("Base de datos: " + catalogo);
            System.out.println("Gestor: " + producto);
            System.out.println("Versión: " + version);
            System.out.println("Productos almacenados: " + totalProductos);

        } catch (SQLException ex) {
            System.out.println(ColoresConsola.pintar("[ERROR] No se pudo establecer la conexión con la base de datos.", ColoresConsola.ROJO));
            if (ex.getErrorCode() == 1045) {
                System.out.println(ColoresConsola.pintar("Detalle: Credenciales de acceso incorrectas (usuario o contraseña no válidos).", ColoresConsola.ROJO));
            } else if (ex.getErrorCode() == 1049) {
                System.out.println(ColoresConsola.pintar("Detalle: La base de datos especificada no existe en el servidor.", ColoresConsola.ROJO));
            } else {
                System.out.println(ColoresConsola.pintar("Detalle: " + ex.getMessage(), ColoresConsola.ROJO));
            }
        }
    }
}
