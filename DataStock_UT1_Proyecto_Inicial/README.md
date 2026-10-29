# DataStock – Práctica Integradora Fase 1 (UT1)

**Módulo:** Acceso a Datos · 2.º DAM · IES Isidra de Guzmán · Curso 2026–2027  
**Alumno:** Guillermo Beltrán Tabares  
**Lenguaje:** Java 21 · **Construcción:** Apache Maven 3.9 · **Interfaz:** Consola ANSI / UTF-8  
**Repositorio GitHub:** [https://github.com/GuilleBT/DataStock](https://github.com/GuilleBT/DataStock)

---

## 1. Estado de la Implementación (UT1 - Ficheros y Formatos de Intercambio)

Todos los requisitos funcionales obligatorios y las actividades técnicas complementarias se encuentran **100% implementados, documentados y verificados** frente a los 34 casos de prueba de aceptación:

| Identificador | Requisito Funcional | Clase de Servicio / Actividad | Estado |
|---|---|---|:---:|
| **RF-FIC-01** | Explorar rutas, ficheros y directorios con NIO.2 | `ExploradorFicherosServicio` | ✅ Superado |
| **RF-FIC-02** | Importar catálogo JSON con Gson y validaciones | `ImportadorJsonServicio` | ✅ Superado |
| **RF-FIC-03** | Importar catálogo XML con DOM protegido contra XXE | `ImportadorXmlServicio` | ✅ Superado |
| **RF-FIC-04** | Consultar y buscar productos en memoria | `ConsultaProductoServicio` | ✅ Superado |
| **RF-FIC-05** | Exportar inventario a JSON (Gson) y XML (DOM) | `ExportadorInventarioServicio` | ✅ Superado |
| **RF-FIC-06** | Guardar y recuperar inventario con serialización binaria (.dat) | `SerializacionServicio` | ✅ Superado |
| **RF-FIC-07** | Consultas XML directas mediante expresiones XPath | `XPathActividad` | ✅ Superado |
| **RF-FIC-08** | Acceso aleatorio y modificación de stock (36 bytes/registro) | `AccesoAleatorioActividad` | ✅ Superado |

---

## 2. Requisitos Previos y Entorno

- **Java Development Kit (JDK):** Versión 21 (LTS) de 64 bits.
- **Gestor de dependencias:** Apache Maven 3.8 o superior.
- **Codificación:** Terminal configurada en UTF-8 para visualización de caracteres en español y símbolos monetarios (€).

---

## 3. Instrucciones de Compilación y Ejecución

Desde la carpeta raíz del proyecto Maven (`DataStock_UT1_Proyecto_Inicial`):

### 3.1. Compilación
```bash
mvn clean compile
```

### 3.2. Ejecutar la Aplicación Principal
Inicia el menú interactivo de gestión de inventario:
```bash
mvn exec:java
```

### 3.3. Ejecutar las Actividades Técnicas Complementarias
Ambas actividades son independientes del menú principal y disponen de su propio punto de entrada:

- **Actividad XPath (RF-FIC-07):**
  ```bash
  mvn exec:java -Dexec.mainClass=es.iesisidra.datastock.complementarias.XPathActividad
  ```

- **Actividad Acceso Aleatorio con RandomAccessFile (RF-FIC-08):**
  ```bash
  mvn exec:java -Dexec.mainClass=es.iesisidra.datastock.complementarias.AccesoAleatorioActividad
  ```

---

## 4. Arquitectura y Organización del Código

El diseño sigue una arquitectura modular en capas basada en **responsabilidad única**, separando la interacción por consola en `ui/` de la lógica de negocio y persistencia en `servicio/`:

```text
DataStock_UT1_Proyecto_Inicial/
├── pom.xml
├── README.md
├── datos/
│   ├── ejemplos/                     # Datos de prueba para importación y validación
│   │   ├── catalogo_valido.json      # Catálogo con productos correctos
│   │   ├── catalogo_con_errores.json # Productos con precios negativos y campos ausentes
│   │   ├── catalogo_malformado.json  # Fichero JSON sintácticamente roto
│   │   ├── catalogo_valido.xml       # Catálogo XML bien formado
│   │   ├── catalogo_tres_productos.xml # 3 productos para pruebas de aceptación
│   │   ├── catalogo_con_errores.xml  # XML con errores para informe de incidencias
│   │   └── catalogo_con_dtd.xml      # XML con DOCTYPE malicioso para prueba XXE
│   └── salidas/                      # Ficheros exportados, serializados y stock.bin
└── src/main/java/es/iesisidra/datastock/
    ├── App.java                      # Punto de entrada de la aplicación
    ├── modelo/
    │   ├── Producto.java             # Entidad del dominio (Serializable, serialVersionUID = 1L)
    │   ├── Categoria.java
    │   ├── Proveedor.java
    │   └── MovimientoStock.java
    ├── servicio/
    │   ├── Inventario.java           # Colección en memoria compartida (List<Producto>)
    │   ├── ValidadorProducto.java    # Validación de negocio centralizada (principio DRY)
    │   ├── ExploradorFicherosServicio.java   # NIO.2 (Path, Files, DirectoryStream)
    │   ├── ImportadorJsonServicio.java       # Gson con JsonParser granular
    │   ├── ImportadorXmlServicio.java        # DOM con protección activa contra XXE
    │   ├── ConsultaProductoServicio.java     # Consultas con copia defensiva inalterable
    │   ├── ExportadorInventarioServicio.java # JSON pretty printing y DOM Transformer
    │   └── SerializacionServicio.java        # Sustitución atómica mediante ObjectStreams
    ├── ui/
    │   └── MenuPrincipal.java        # Menú interactivo y delegación limpia
    ├── util/
    │   ├── LectorConsola.java        # Scanner centralizado con confirmaciones S/N
    │   └── ColoresConsola.java       # Formateo visual ANSI
    └── complementarias/
        ├── XPathActividad.java       # 4 consultas XPath directas sobre XML
        └── AccesoAleatorioActividad.java # RandomAccessFile O(1) con registros de 36 bytes
```

---

## 5. Decisiones Técnicas y Mecanismos de Seguridad

1. **Seguridad contra vulnerabilidades XXE (XML External Entity):** Tanto en `ImportadorXmlServicio` como en `XPathActividad`, la factoría `DocumentBuilderFactory` tiene activada la directiva `"http://apache.org/xml/features/disallow-doctype-decl"` y bloqueado el acceso a esquemas y DTDs externos. Si un XML incluye `<!DOCTYPE ...>`, el parser lo rechaza de inmediato (caso CP-XML-03 y CP-XPATH-05).
2. **Prevención de Inyección XPath:** Para la búsqueda por código en `XPathActividad`, se seleccionan los nodos `/catalogo/producto` y se compara el código en Java mediante `equalsIgnoreCase()`, evitando concatenar directamente la entrada del usuario en la sintaxis XPath.
3. **Principio de Sustitución Atómica:** En `SerializacionServicio`, la recuperación desde ficheros binarios `.dat` se realiza en una lista auxiliar temporal. Si el fichero está cortado o no es un flujo binario válido, se captura la excepción y la memoria previa queda 100% intacta (caso CP-SER-03).
4. **Reglas de Negocio Centralizadas (DRY):** Las validaciones de código (expresión regular `^[A-Z0-9-]+$`, máx 12 caracteres), nombre (3 a 100 caracteres), precio monetario (`BigDecimal > 0`), stock (`>= 0`) e identificadores de categoría y proveedor son comunes para JSON y XML a través de `ValidadorProducto`.
5. **Cálculo de Desplazamientos en Registros de Longitud Fija:** Cada registro en `stock.bin` ocupa exactamente 36 bytes:
   - Código: 12 caracteres Java (UTF-16 BE) = 24 bytes (`writeChars`).
   - Precio: 1 `double` (64 bits) = 8 bytes (`writeDouble`).
   - Stock: 1 `int` (32 bits) = 4 bytes (`writeInt`).
   - Offset registro $i = i \times 36$.
   - Offset campo stock registro $i = i \times 36 + 32$. Modifica únicamente ese campo sin reescribir el resto del archivo.

---

## 6. Registro de Pruebas de Aceptación

Los resultados detallados de la ejecución de las 34 pruebas de aceptación están recogidos en el documento Word oficial:
`DataStock_UT1_Plantilla_Registro_Pruebas.docx`
- **Total ejecutadas:** 34
- **Superadas:** 34 (100 %)
- **No superadas:** 0
