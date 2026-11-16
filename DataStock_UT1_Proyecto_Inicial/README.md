# DataStock – Práctica Integradora (Entrega Final · UT3: Componentes Reutilizables)

**Módulo:** Acceso a Datos · 2.º DAM · IES Isidra de Guzmán (Alcalá de Henares)  
**Curso:** 2026–2027  
**Alumno:** Guillermo Beltrán Tabares  
**Entorno:** Java 21 · Maven 3.8+ · MySQL 8.0 · SQLite · JUnit 5 · Javadoc  
**Repositorio GitHub:** [https://github.com/GuilleBT/DataStock](https://github.com/GuilleBT/DataStock)  
**Rama:** `ut3` | **Etiqueta:** `UT3-v1.0`  

---

## 1. Descripción del proyecto

**DataStock** es una aplicación integral de gestión de inventarios y catálogos de productos desarrollada en Java a lo largo de tres unidades de trabajo:

1. **UT1 (Ficheros y Formatos de Intercambio):** Persistencia en ficheros binarios (`.dat`), importación/exportación estructurada en **JSON** (Gson) y **XML** (DOM), colecciones dinámicas en memoria y actividades complementarias de acceso aleatorio (`RandomAccessFile`) y consultas jerárquicas (`XPath`).
2. **UT2 (Bases de Datos Relacionales mediante JDBC):** Persistencia en un gestor relacional independiente **MySQL 8.0** utilizando JDBC estándar (`DriverManager`, `PreparedStatement`, `ResultSet`, `CallableStatement`), control estricto de transacciones (`commit`/`rollback`), importación masiva por lotes y actividad complementaria con gestor embebido **SQLite**.
3. **UT3 (Desarrollo y Evaluación de Componentes Reutilizables):** Refactorización arquitectónica de dos núcleos clave de la aplicación para extraerlos a **componentes desacoplados, autónomos y reutilizables** en el paquete `es.iesisidra.datastock.componentes`:
   - `LectorProductosJson`: Componente de gestión de ficheros que lee y valida documentos JSON sin dependencias de consola (`System.out` ni `Scanner`).
   - `ConsultaProductoJdbc`: Componente relacional que ejecuta búsquedas parametrizadas con `JOIN` mediante JDBC y devuelve resultados encapsulados en `Optional<ProductoDetalleDTO>`.
   - Batería completa de pruebas automatizadas con **JUnit 5** y documentación técnica con **Javadoc**.

---

## 2. Requisitos previos

- **Java Development Kit (JDK):** Versión 21 (LTS).
- **Apache Maven:** Versión 3.8 o superior.
- **MySQL Server:** Versión 8.0 o superior (en local o en contenedor Docker/Podman).
- **Bibliotecas y dependencias del proyecto:**
  - `com.google.code.gson:gson:2.11.0` (Serialización y análisis JSON).
  - `com.mysql:mysql-connector-j:8.4.0` (Conector JDBC oficial MySQL).
  - `org.xerial:sqlite-jdbc:3.47.1.0` (Conector JDBC para SQLite embebido).
  - `org.junit.jupiter:junit-jupiter:5.11.3` (Framework de pruebas automatizadas JUnit 5).

---

## 3. Preparación de la base de datos MySQL

1. Asegúrate de tener en ejecución el motor MySQL (por ejemplo, en el contenedor `datastock-mysql` en el puerto 3306).
2. Ejecuta el script `DataStock_BD_MySQL.sql` ubicado en la raíz del repositorio para crear el esquema `datastock`, las cuatro tablas (`categoria`, `proveedor`, `producto`, `movimiento_stock`) con todas sus restricciones de integridad (PK, FK, UNIQUE, CHECK) y el procedimiento almacenado `registrar_movimiento`:
   ```bash
   mysql -h 127.0.0.1 -u root -p datastock < DataStock_BD_MySQL.sql
   ```

---

## 4. Configuración de la conexión y seguridad

Por buenas prácticas de seguridad, las credenciales nunca deben registrarse en el control de versiones. La clase `ConexionBD` busca la configuración en cascada:

1. **Fichero local `db.properties`:** Ubicado en la raíz del proyecto (ignorado en `.gitignore`):
   ```properties
   db.url=jdbc:mysql://localhost:3306/datastock?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8&useUnicode=true
   db.user=root
   db.password=datastock
   ```
2. **Variables de entorno:** `JDBC_URL`, `JDBC_USER` y `JDBC_PASSWORD`.
3. **Valores por defecto de desarrollo:** `localhost:3306/datastock`, usuario `root` y contraseña `datastock`.

---

## 5. Compilación, pruebas y ejecución

Todas las órdenes deben ejecutarse desde el directorio del proyecto Maven (`DataStock_UT1_Proyecto_Inicial/`):

### 5.1. Compilar el proyecto
```bash
mvn clean compile
```

### 5.2. Ejecutar las pruebas automatizadas (JUnit 5)
Ejecuta la suite completa de 11 pruebas unitarias y de integración que verifican los casos obligatorios CP-COMP-01, CP-COMP-02 y CP-COMP-03:
```bash
mvn test
```

### 5.3. Generar la documentación técnica (Javadoc)
Genera la documentación HTML de la API en `target/site/apidocs/`:
```bash
mvn javadoc:javadoc
```

### 5.4. Ejecutar la aplicación interactiva DataStock
Inicia el menú principal de consola:
```bash
mvn exec:java
```

### 5.5. Actividades complementarias
- **SQLite embebido (UT2):**
  ```bash
  mvn exec:java -Dexec.mainClass=es.iesisidra.datastock.complementarias.DemoSQLite
  ```
- **XPath jerárquico (UT1):**
  ```bash
  mvn exec:java -Dexec.mainClass=es.iesisidra.datastock.complementarias.XPathActividad
  ```
- **RandomAccessFile (UT1):**
  ```bash
  mvn exec:java -Dexec.mainClass=es.iesisidra.datastock.complementarias.AccesoAleatorioActividad
  ```

---

## 6. Estructura de navegación de menús

### Menú Principal
```text
================================================
         DATASTOCK - GESTIÓN DE INVENTARIO
================================================
Productos en memoria: X
1. Explorar ficheros y directorios (UT1)
2. Importar productos desde JSON (UT1 / UT3 -> LectorProductosJson)
3. Importar productos desde XML (UT1)
4. Consultar productos en memoria (UT1)
5. Exportar productos (UT1)
6. Guardar y recuperar inventario (UT1)
7. Gestionar base de datos MySQL (UT2)
0. Salir
```

### Submenú de Gestión MySQL (Opción 7)
```text
================================================
              GESTIÓN MYSQL - UT2
================================================
1. Comprobar conexión y mostrar información de la BD
2. Gestionar categorías (alta, listado, modificar, eliminar)
3. Gestionar proveedores (alta, listado, modificar, eliminar)
4. Gestionar productos (CRUD)
5. Consultar y buscar productos
   1. Listar todos los productos
   2. Buscar producto por código (UT2 / UT3 -> ConsultaProductoJdbc)
   3. Buscar productos por nombre
   4. Consultar productos con stock inferior a un umbral
6. Registrar entrada o salida de stock (Stored Procedure)
7. Consultar historial de movimientos
8. Importar productos de memoria a MySQL (lote transaccional)
9. Demostración de transacción con commit/rollback
0. Volver al menú principal
```

---

## 7. Comparativa técnica: MySQL frente a SQLite (UT2)

| Criterio | MySQL 8.0 (Gestor cliente-servidor) | SQLite 3 (Gestor embebido) |
|---|---|---|
| **¿Requiere servidor?** | **Sí**. Proceso demonio (`mysqld`) en puerto TCP/IP 3306. | **No**. El motor corre en el mismo proceso de la JVM. |
| **Almacenamiento** | Directorio administrado por el motor (tablas e índices InnoDB). | Fichero ordinario único local (`datos/sqlite/productos_demo.db`). |
| **Conexión JDBC** | Socket de red: `jdbc:mysql://localhost:3306/datastock`. | Fichero directo: `jdbc:sqlite:datos/sqlite/productos_demo.db`. |
| **Concurrencia** | Elevada concurrencia y granularidad por fila (InnoDB MVCC). | Bloqueo a nivel de fichero para escrituras (baja concurrencia). |
| **Uso idóneo** | Sistemas empresariales multiusuario, microservicios, backend web. | Apps móviles, herramientas CLI, prototipos, almacenamiento local. |

---

## 8. ¿Qué es un pool de conexiones y por qué no es necesario aquí?

Un **pool de conexiones** (como HikariCP) mantiene un conjunto de conexiones abiertas con la base de datos para amortizar el coste de establecimiento de conexión (handshake TCP, negociación SSL y autenticación) en entornos altamente concurrentes (APIs web con cientos de peticiones por segundo).

En **DataStock**, al ser una aplicación interactiva mono-usuario de consola, la apertura mediante `DriverManager.getConnection()` y su cierre automático e inmediato mediante `try-with-resources` es una solución óptima, limpia, robusta y con un consumo de memoria mínimo.

---

## 9. UT3: Componentes reutilizables (Reflexión técnica y diseño)

### 9.1. ¿Qué ventaja tiene separar la lectura JSON del menú? ¿Qué coste introduce crear más clases?

**Ventajas clave:**
- **Principio de Responsabilidad Única (SRP):** El componente `LectorProductosJson` se encarga exclusivamente de resolver el acceso I/O, el análisis sintáctico (Gson) y la validación semántica de los datos, mientras que el menú (`MenuPrincipal`) asume únicamente la interacción con el usuario (solicitar la ruta por teclado, orquestar la llamada y pintar en consola con formato y colores).
- **Reutilización directa en cualquier cliente:** Al carecer por completo de dependencias de `System.out`, colores ANSI o `Scanner`, el componente puede ser utilizado de forma idéntica desde una interfaz gráfica (JavaFX/Swing), un servicio web REST (Spring Boot/Quarkus), un proceso por lotes en segundo plano o una batería de pruebas automatizadas con **JUnit 5**.
- **Testabilidad y mantenibilidad:** Es posible probar todas las casuísticas de lectura (fichero válido, inexistente, corrupto o con datos incoherentes) de forma desatendida mediante aserciones unitarias, sin necesidad de simular flujos de entrada estándar ni interceptar flujos de salida. Además, si en el futuro se decide migrar de Gson a Jackson, el cambio se confina dentro del componente sin afectar a la interfaz.

**Coste e inconvenientes:**
- **Mayor número de clases y dispersión de artefactos:** Se introducen nuevas clases (`LectorProductosJson`, `ResultadoLecturaJson`, `LectorJsonException`), incrementando la navegación de ficheros y el código de transporte de datos (*boilerplate* y DTOs).
- **Sobrecarga de diseño inicial:** Exige definir formalmente contratos públicos (excepciones tipadas checked, colecciones inmodificables y objetos resultado) en lugar de una lectura secuencial rápida dentro del propio método del menú. Para *scripts* pequeños y efímeros esto añade complejidad, pero en ingeniería de software profesional es el pilar de un código escalable y mantenible.

### 9.2. ¿Por qué conviene separar una consulta JDBC de la presentación por consola?

- **Independencia tecnológica de la capa de vista:** El componente `ConsultaProductoJdbc` consulta la base de datos MySQL mediante sentencias preparadas parametrizadas (`PreparedStatement`) con cláusulas `JOIN` y devuelve los datos estructurados en un `Optional<ProductoDetalleDTO>`. Si el componente incluyera llamadas a `System.out.println`, quedaría acoplado irremediablemente a una consola de texto, impidiendo su reutilización en exportaciones a PDF, respuestas JSON de un endpoint o interfaces de usuario modernas.
- **Gestión estricta y determinista de recursos:** La apertura, ejecución y cierre seguro de `Connection`, `PreparedStatement` y `ResultSet` se encapsula dentro del componente mediante bloques `try-with-resources`. Se garantiza así que nunca ocurran fugas de conexiones (*connection leaks*) ni cursores huérfanos, sin importar cómo la capa superior decida procesar o presentar los datos.
- **Semántica enriquecida de ausencia de valor y errores:** El uso de `Optional` comunica de forma limpia que un código buscado puede no existir en la base de datos, eliminando el riesgo de errores en tiempo de ejecución por referencias nulas (`NullPointerException`). Asimismo, las excepciones técnicas de persistencia (`SQLException`) se propagan limpiamente para que la capa superior decida el tratamiento adecuado (un aviso amarillo en consola, un mensaje de error rojo o un registro de auditoría en log).

### 9.3. ¿Qué herramientas has utilizado y para qué sirve cada una?

- **IDE (Visual Studio Code / Antigravity CLI / IntelliJ):** Entorno de desarrollo para la edición del código fuente con soporte de Language Server Protocol (LSP), detección de advertencias en tiempo real, refactorización asistida y navegación fluida por la estructura del proyecto.
- **Apache Maven:** Herramienta central de construcción y gestión del ciclo de vida del proyecto (*build tool*). Automatiza la resolución transitiva de dependencias externas (Gson, MySQL Connector/J, SQLite JDBC y JUnit Jupiter), estandariza la compilación en Java 21 (`maven-compiler-plugin`) y orquesta la ejecución de tests (`maven-surefire-plugin`), la generación documental (`maven-javadoc-plugin`) y la ejecución (`exec-maven-plugin`).
- **Javadoc:** Herramienta oficial de generación de documentación de API a partir de comentarios estructurados en el código fuente (`/** ... */`). Se ha utilizado para documentar exhaustivamente el propósito de las clases, parámetros (`@param`), valores de retorno (`@return`) y posibles excepciones (`@throws`) en todos los métodos públicos de los componentes.
- **JUnit 5 (JUnit Jupiter 5.11.3):** Framework líder de pruebas unitarias en el ecosistema Java. Se han desarrollado 11 casos de prueba automatizados (`LectorProductosJsonTest` y `ConsultaProductoJdbcTest`) que validan de forma continua, desatendida y reproducible los requisitos CP-COMP-01, CP-COMP-02 y CP-COMP-03.

### 9.4. ¿Qué dos opciones de DataStock has modificado para integrar los componentes?

1. **Opción 1: Menú Principal -> Opción 2 (`Importar productos desde JSON`):**
   - El método `ejecutarImportacionJson()` en `MenuPrincipal.java` delega directamente en el componente `LectorProductosJson.leerDetallado(Path ruta, List<Producto> productosExistentes)`.
   - El componente analiza el archivo y devuelve un `ResultadoLecturaJson` o lanza `LectorJsonException` en caso de fallo crítico.
   - Es el propio menú el responsable de añadir los productos a la memoria (`inventario.getProductos().addAll(...)`), informar de las incidencias en amarillo y mostrar el mensaje de éxito `[OK]` en color verde o `[ERROR]` en rojo si el fichero es inexistente o corrupto, manteniendo la aplicación en ejecución.
2. **Opción 2: Menú Principal -> Opción 7 (`Gestión MySQL`) -> Opción 5 (`Consultar y buscar productos`) -> Opción 2 (`Buscar producto por código`):**
   - En `MenuProductosUI.java`, el método `consultarProductoPorCodigo()` invoca al componente `ConsultaProductoJdbc.buscarPorCodigo(String codigo)`.
   - El componente ejecuta la consulta relacional con `PreparedStatement` y `JOIN`, cerrando los recursos con `try-with-resources` y devolviendo un `Optional<ProductoDetalleDTO>`.
   - La interfaz interactiva solicita el código al usuario, muestra el resultado `[OK]` en verde junto a la ficha detallada si el producto existe, o muestra un aviso `[AVISO]` en amarillo si el `Optional` está vacío, sin excepciones sin controlar.
   - Adicionalmente, `ProductoDAO.buscarPorCodigo(codigo)` y `ImportadorJsonServicio` fueron adaptados para reutilizar internamente los nuevos componentes, garantizando total coherencia y compatibilidad retrospectiva con la UT1 y la UT2.

---

## 10. Resumen de requisitos funcionales y pruebas de la UT3

| Caso de Prueba | Acción realizada | Componente y opción integrada | Resultado esperado | Resultado obtenido | Estado |
|---|---|---|---|---|---|
| **CP-COMP-01** | Importar JSON válido desde menú | `LectorProductosJson` (Opción 2) con `datos/ejemplos/catalogo_valido.json` | Carga productos en memoria y muestra `[OK]`. | Se cargan 2 productos (`TEC-001`, `RAT-002`). Muestra `[OK]` en verde. 0 rechazados. | **SUPERADA** |
| **CP-COMP-02.1** | Importar JSON inexistente | `LectorProductosJson` (Opción 2) con `datos/ejemplos/archivo_no_existe.json` | Error controlado en rojo; aplicación continúa. | Muestra `[ERROR] El fichero no existe...` en rojo. Memoria intacta. Menú activo. | **SUPERADA** |
| **CP-COMP-02.2** | Importar JSON malformado | `LectorProductosJson` (Opción 2) con `datos/ejemplos/catalogo_malformado.json` | Error sintáctico en rojo; aplicación continúa. | Muestra `[ERROR] sintaxis JSON incorrecta o mal formado` en rojo. Menú activo. | **SUPERADA** |
| **CP-COMP-03.1** | Buscar código MySQL existente | `ConsultaProductoJdbc` (Opción 7 > 5 > 2) con código `TEC-001` | Muestra ficha correcta y mensaje `[OK]`. | Muestra `[OK]` y ficha de `Teclado mecánico` (Cat: Periféricos, Prov: Dist. Norte). | **SUPERADA** |
| **CP-COMP-03.2** | Buscar código MySQL inexistente | `ConsultaProductoJdbc` (Opción 7 > 5 > 2) con código `XYZ-999` | Muestra aviso sin excepciones sin controlar. | Muestra `[AVISO] No se ha encontrado ningún producto...` en amarillo. Retorno limpio. | **SUPERADA** |
| **CP-JUNIT-01** | Suite tests JUnit 5 JSON | `LectorProductosJsonTest` (6 pruebas unitarias automáticas) | Aserciones unitarias al 100%. | 6 de 6 tests pasados satisfactoriamente. `BUILD SUCCESS`. | **SUPERADA** |
| **CP-JUNIT-02** | Suite tests JUnit 5 JDBC | `ConsultaProductoJdbcTest` (5 pruebas integración automáticas) | Verificación de PreparedStatement y Optional. | 5 de 5 tests pasados satisfactoriamente. `BUILD SUCCESS`. | **SUPERADA** |

---

## 11. Entregables generados en la entrega final UT3

1. **Código fuente:** En la rama `ut3` del repositorio Git con etiqueta `UT3-v1.0`.
2. **Componentes reutilizables:** `LectorProductosJson`, `ResultadoLecturaJson`, `LectorJsonException` y `ConsultaProductoJdbc` en `es.iesisidra.datastock.componentes`.
3. **Pruebas automatizadas:** `LectorProductosJsonTest` y `ConsultaProductoJdbcTest` en `src/test/java/`.
4. **Documentación de pruebas:**
   - [DataStock_UT3_Plantilla_Registro_Pruebas.docx](file:///home/guillechum/ACCESO%20A%20DATOS/DataStock/DataStock_UT3_Plantilla_Registro_Pruebas.docx)
   - [DataStock_UT3_Plantilla_Registro_Pruebas.pdf](file:///home/guillechum/ACCESO%20A%20DATOS/DataStock/DataStock_UT3_Plantilla_Registro_Pruebas.pdf)
5. **Documentación técnica:** Javadoc generado con `mvn javadoc:javadoc`.
