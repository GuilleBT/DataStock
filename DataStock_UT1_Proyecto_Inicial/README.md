# DataStock – Práctica Integradora (Fase 2 · UT2)

**Módulo:** Acceso a Datos · 2.º DAM · IES Isidra de Guzmán (Alcalá de Henares)  
**Curso:** 2026–2027  
**Alumno:** Guillermo Beltrán Tabares  
**Entorno:** Java 21 · Maven · MySQL 8.0 · SQLite  

---

## 1. Descripción del proyecto

DataStock es una aplicación de consola en Java desarrollada a lo largo del curso. En esta segunda fase (UT2) hemos incorporado persistencia real en una base de datos relacional MySQL mediante JDBC, manteniendo intactas todas las funciones de ficheros, JSON, XML y colecciones en memoria de la primera fase (UT1).

Además, se incluye una actividad complementaria obligatoria con SQLite embebido para comparar el funcionamiento de un gestor basado en servidor frente a uno embebido.

---

## 2. Requisitos previos

- **Java Development Kit (JDK):** Versión 21.
- **Apache Maven:** 3.8 o superior.
- **MySQL Server:** Versión 8.0 o superior (en local o mediante contenedor Docker/Podman).
- **Conectores JDBC utilizados:**
  - MySQL: `com.mysql:mysql-connector-j` versión `8.4.0`.
  - SQLite: `org.xerial:sqlite-jdbc` versión `3.47.1.0`.

---

## 3. Preparación de la base de datos MySQL

1. Asegúrate de tener arrancado el servicio de MySQL en el puerto 3306.
2. Ejecuta el script `DataStock_BD_MySQL.sql` incluido en el repositorio para crear la base de datos `datastock` y sus tablas:
   ```bash
   mysql -u root -p datastock < DataStock_BD_MySQL.sql
   ```
3. El script crea las cuatro tablas del modelo relacional (`categoria`, `proveedor`, `producto` y `movimiento_stock`) con todas sus restricciones de integridad (PRIMARY KEY, UNIQUE, FOREIGN KEY y CHECK), además del procedimiento almacenado `registrar_movimiento`.

---

## 4. Configuración de la conexión y seguridad

Por motivos de seguridad, las contraseñas nunca deben subirse al repositorio Git. La aplicación busca los datos de conexión en el siguiente orden:

1. **Fichero local `db.properties`** en la carpeta del proyecto (ignorado en `.gitignore`).  
   Para crearlo, basta con copiar la plantilla proporcionada:
   ```bash
   cp db.properties.ejemplo db.properties
   ```
   Y editarlo con tus credenciales:
   ```properties
   db.url=jdbc:mysql://localhost:3306/datastock?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8&useUnicode=true
   db.user=root
   db.password=tu_contraseña
   ```

2. **Variables de entorno del sistema:** Si no existe el archivo `db.properties`, leerá `JDBC_URL`, `JDBC_USER` y `JDBC_PASSWORD`.

3. **Valores por defecto:** Si no se configuran los anteriores, usa `localhost:3306/datastock`, usuario `root` y clave `datastock`.

---

## 5. Compilación y ejecución

Todas las órdenes se ejecutan desde el directorio `DataStock_UT1_Proyecto_Inicial/`:

### 5.1. Compilar el proyecto
```bash
mvn clean compile
```

### 5.2. Ejecutar DataStock (menú principal)
```bash
mvn exec:java
```

### 5.3. Ejecutar la actividad de SQLite (DemoSQLite)
Esta actividad es independiente del menú principal y se ejecuta con:
```bash
mvn exec:java -Dexec.mainClass=es.iesisidra.datastock.complementarias.DemoSQLite
```

### 5.4. Actividades complementarias de la UT1
Siguen disponibles y funcionando exactamente igual que en la primera entrega:
- **XPath:** `mvn exec:java -Dexec.mainClass=es.iesisidra.datastock.complementarias.XPathActividad`
- **RandomAccessFile:** `mvn exec:java -Dexec.mainClass=es.iesisidra.datastock.complementarias.AccesoAleatorioActividad`

---

## 6. Estructura de los menús

Al iniciar DataStock se muestra el menú principal que reúne las dos fases:

```text
DATASTOCK - GESTIÓN DE INVENTARIO
1. Explorar ficheros y directorios (UT1)
2. Importar productos desde JSON (UT1)
3. Importar productos desde XML (UT1)
4. Consultar productos en memoria (UT1)
5. Exportar productos (UT1)
6. Guardar y recuperar inventario (UT1)
7. Gestionar base de datos MySQL (UT2)
0. Salir
```

Al seleccionar la opción 7 se accede al menú específico de MySQL:

```text
GESTIÓN MYSQL - UT2
1. Comprobar conexión y mostrar información de la BD
2. Gestionar categorías (alta, listado, modificar, eliminar)
3. Gestionar proveedores (alta, listado, modificar, eliminar)
4. Gestionar productos (CRUD con validaciones)
5. Consultar y buscar productos (JOIN, LIKE y filtro por stock)
6. Registrar entrada o salida de stock (mediante procedimiento almacenado)
7. Consultar historial de movimientos (completo o filtrado por producto)
8. Importar productos de memoria a MySQL (copia controlada por lotes)
9. Demostración de transacción con commit/rollback
0. Volver al menú principal
```

---

## 7. Diferencias entre inventario en memoria y base de datos MySQL

Es importante tener clara la diferencia entre ambos almacenes:

- **Inventario en memoria (UT1):** Es una lista (`List<Producto>`) de objetos Java que reside únicamente en la memoria RAM del programa mientras está en ejecución. Al cerrar la aplicación, estos datos se pierden a menos que se hayan guardado en un fichero `.dat`, `.json` o `.xml`.
- **Base de datos MySQL (UT2):** Es un almacenamiento permanente e independiente. Los datos guardados aquí persisten entre distintas ejecuciones del programa.
- **Separación e importación explícita:** Cargar un fichero JSON o XML en la opción 2 o 3 solo mete los productos en memoria, **no los guarda en MySQL automáticamente**. Para pasarlos a la base de datos hay que utilizar expresamente la opción **7.8 (Importar productos de memoria a MySQL)**. Esta opción comprueba que existan las categorías y proveedores correspondientes, omite los códigos que ya existan en MySQL y realiza la inserción en un único lote transaccional con `commit` y `rollback`.

---

## 8. Comparativa técnica: MySQL frente a SQLite

En la siguiente tabla se resumen las principales diferencias observadas entre el gestor cliente-servidor (MySQL) y el gestor embebido (SQLite):

| Criterio | MySQL 8.0 (Gestor independiente) | SQLite 3 (Gestor embebido) |
|---|---|---|
| **¿Necesita servidor?** | **Sí**. Requiere un proceso demonio (`mysqld`) ejecutándose en segundo plano y escuchando en un puerto (3306). | **No**. El motor de la base de datos se ejecuta dentro del propio proceso de la aplicación Java. |
| **¿Dónde se guardan los datos?** | En el directorio de datos gestionado por el servidor MySQL (con sus propios ficheros de tablas e índices InnoDB). | En un único fichero normal en el sistema de archivos local (`datos/sqlite/productos_demo.db`). |
| **Conexión JDBC** | Se conecta por red mediante socket TCP/IP: `jdbc:mysql://localhost:3306/datastock`. Requiere usuario y contraseña. | Se conecta directamente al fichero local: `jdbc:sqlite:datos/sqlite/productos_demo.db`. No requiere contraseña ni red. |
| **Dependencia Maven** | `com.mysql:mysql-connector-j:8.4.0` | `org.xerial:sqlite-jdbc:3.47.1.0` |
| **Ventajas** | Soporta múltiples conexiones concurrentes, permisos avanzados por usuario, procedimientos almacenados y transacciones complejas. | Cero configuración, no requiere instalar servidores, es ligero, portátil y fácil de transportar en un solo archivo. |
| **Inconvenientes** | Mayor consumo de recursos y necesidad de instalar, configurar y mantener el servidor. | Bloqueo a nivel de fichero para escrituras (baja concurrencia), no admite procedimientos almacenados complejos ni usuarios. |
| **¿Para qué lo elegirías?** | Aplicaciones web, sistemas empresariales multiusuario o con alta concurrencia de datos. | Aplicaciones de escritorio mono-usuario, apps móviles (Android/iOS), prototipos o software embebido. |

---

## 9. ¿Qué es un pool de conexiones y por qué no es necesario aquí?

Un **pool de conexiones** (como HikariCP o Apache DBCP) es un almacén de conexiones a la base de datos que se mantienen abiertas y listas para ser reutilizadas por distintos hilos o peticiones, en lugar de abrir y cerrar una conexión física cada vez que se hace una consulta.

- **¿Cuándo resulta útil?** En aplicaciones web o servicios con cientos de usuarios simultáneos, porque abrir una conexión TCP/IP contra MySQL lleva tiempo de negociación (handshake, autenticación SSL, etc.). Reutilizar conexiones ahorra mucho tiempo y recursos en esos entornos.
- **¿Por qué DataStock funciona bien sin él?** DataStock es una aplicación interactiva de consola mono-usuario. Entre que el usuario pulsa una opción y otra pasan varios segundos o minutos. Por ello, abrir una conexión corta con `DriverManager.getConnection()` y cerrarla de inmediato con `try-with-resources` es una solución limpia, segura, fácil de entender y con un consumo de recursos mínimo para una práctica de consola.

---

## 10. Resumen de requisitos funcionales superados (UT2)

| Requisito | Descripción | Implementación |
|---|---|---|
| **RF-JDBC-01** | Conexión segura y metadatos del esquema | `ConexionBD` con lectura de `db.properties` / variables de entorno y `DatabaseMetaData`. |
| **RF-JDBC-02** | Mantenimiento de categorías y proveedores | `CategoriaDAO` y `ProveedorDAO` con control de duplicados e integridad referencial. |
| **RF-JDBC-03** | CRUD de productos en MySQL | `ProductoDAO` con validaciones de código, nombre, precio (BigDecimal) y claves foráneas. |
| **RF-JDBC-04** | Consultas con ResultSet y JOIN | Búsqueda exacta, listado con nombres combinados, `LIKE` y filtro estricto por stock. |
| **RF-JDBC-05** | Movimientos de stock con Stored Procedure | Invocación a `registrar_movimiento` con `CallableStatement` y control de excepciones. |
| **RF-JDBC-06** | Historial de movimientos | Consultas ordenadas por fecha e ID de la tabla `movimiento_stock` sin alterar datos. |
| **RF-JDBC-07** | Importación por lotes desde memoria | `ImportadorMemoriaMysqlServicio` con validaciones previas y transacción única `commit`/`rollback`. |
| **RF-JDBC-08** | Demostración de transacciones JDBC | `TransaccionDemoServicio` con casos prácticos de confirmación y reversión ante error forzado. |
| **Actividad SQLite** | Gestor embebido independiente | `DemoSQLite` con inserción con `INSERT OR IGNORE`, consultas y fichero `.db` persistente. |
