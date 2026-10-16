# Papelería Campus — Catálogo de productos

Aplicación de escritorio desarrollada con JavaFX y PostgreSQL para consultar y administrar el catálogo de productos de la papelería Campus. El acceso a la base de datos se realiza mediante JDBC y las operaciones disponibles son crear, consultar, actualizar y eliminar productos.

## Autor

- **Nombre:** Alex Josue Fonseca elasquez 
- **Asignatura:** Programación de Aplicaciones de Escritorio.
- **Actividad:** Laboratorio de acceso a datos y visualización en TableView.

## Tecnologías y requisitos

| Componente | Versión o requisito |
|---|---|
| Java | JDK 21 |
| JavaFX Controls y FXML | 21.0.6 |
| Controlador JDBC de PostgreSQL | 42.7.5 |
| Maven Compiler Plugin | 3.13.0 |
| JavaFX Maven Plugin | 0.0.8 |
| JUnit Jupiter | 5.12.1; dependencia de pruebas del proyecto |
| PostgreSQL | Servidor local; se recomienda 15 o superior |
| Entorno de desarrollo | IntelliJ IDEA con soporte Maven |
| Herramientas auxiliares | pgAdmin para SQL y Scene Builder para editar el FXML |

Las versiones de las dependencias corresponden al `pom.xml` preparado para el proyecto. La dependencia JUnit no significa que existan pruebas automatizadas: los resultados de las pruebas realizadas se documentan en `pruebas.md`.

## Funcionalidades

- TableView con ID, código, nombre, categoría, precio y stock.
- Carga de los productos desde PostgreSQL, inicialmente ordenados por código ascendente.
- Contador de registros visibles.
- Selección de una fila para cargar sus datos en el formulario y conservar su ID internamente.
- Registro, actualización por ID y eliminación por ID con confirmación.
- Búsqueda parcial por nombre o código, sin distinguir mayúsculas y minúsculas.
- Filtro por categoría: Todas, Papelería, Tecnología o Accesorios.
- Filtro de productos con stock mayor que cero.
- Combinación de los filtros mediante AND y consulta a PostgreSQL en cada búsqueda.
- Recarga del listado después de una escritura, conservando los filtros vigentes.
- Validaciones de entrada, mensajes de error y diagnóstico de operación y SQLState en consola.

**Nuevo** limpia el formulario y la selección. **Limpiar filtros** restablece la búsqueda, selecciona Todas y desmarca Solo con existencias. **Buscar** aplica los controles de búsqueda y **Recargar** consulta nuevamente con los filtros actuales. También se puede presionar Enter en el campo de búsqueda.

## Organización del proyecto

```text
Evaluacion_corte2/
├── config/
│   ├── database.properties.example
│   └── database.properties          # Configuración local excluida de Git
├── sql/
│   ├── estructura.sql
│   └── datos.sql
├── evidencias/
├── src/
│   └── main/
│       ├── java/
│       │   ├── module-info.java
│       │   └── ni/uam/edu/evaluacion_corte2/
│       │       ├── HelloApplication.java
│       │       ├── Launcher.java
│       │       ├── controller/
│       │       │   └── CampusController.java
│       │       ├── database/
│       │       │   └── ConexionBD.java
│       │       ├── dao/
│       │       │   └── ProductoDAO.java
│       │       └── model/
│       │           └── Producto.java
│       └── resources/
│           └── ni/uam/edu/evaluacion_corte2/
│               └── campus-view.fxml
├── .gitignore
├── pom.xml
├── README.md
└── pruebas.md
```

### Responsabilidad de los archivos principales

| Archivo | Responsabilidad |
|---|---|
| `HelloApplication.java` | Inicia JavaFX, carga `campus-view.fxml` y muestra la ventana. |
| `CampusController.java` | Atiende eventos, valida los campos, maneja la selección, actualiza la tabla y muestra mensajes. |
| `Producto.java` | Representa un producto con sus seis campos; usa BigDecimal para el precio. |
| `ProductoDAO.java` | Ejecuta las consultas y operaciones CRUD mediante PreparedStatement. |
| `ConexionBD.java` | Lee la configuración externa y obtiene una conexión JDBC. |
| `campus-view.fxml` | Define el formulario, los botones, los filtros, la tabla y el contador. |
| `module-info.java` | Declara los módulos necesarios y el acceso de JavaFX a los paquetes. |

Si la clase principal se renombró a `CampusApplication`, su nombre debe coincidir tanto en el archivo Java como en la configuración `mainClass` del `pom.xml`.

## Preparación de la base de datos

1. Iniciar el servicio local de PostgreSQL y conectarse desde pgAdmin con un usuario autorizado para crear la base de datos.
2. Abrir Query Tool conectado a la base `postgres` y ejecutar esta sentencia por separado, con autocommit habilitado y fuera de un bloque de transacción:

```sql
CREATE DATABASE papeleria_campus;
```

Si la base ya existe, omitir este paso.

3. Actualizar el árbol de pgAdmin, seleccionar **papeleria_campus** y abrir un nuevo Query Tool para esa base. No basta con crear la base: las siguientes sentencias deben ejecutarse conectadas a ella.
4. Ejecutar `sql/estructura.sql` para crear `public.producto` y sus restricciones.
5. Ejecutar `sql/datos.sql` para cargar los 12 productos iniciales. Ejecutarlo una sola vez sobre la tabla vacía.
6. Comprobar el resultado:

```sql
SELECT CURRENT_DATABASE() AS base_de_datos,
       COUNT(*) AS total_productos
FROM public.producto;

SELECT *
FROM public.producto
ORDER BY codigo ASC;
```

La primera consulta debe mostrar `papeleria_campus` y un total de **12**.

### Modelo de datos

| Campo | Tipo | Restricciones |
|---|---|---|
| `id_producto` | INTEGER GENERATED ALWAYS AS IDENTITY | Clave primaria automática. |
| `codigo` | VARCHAR(10) | NOT NULL, UNIQUE, de 1 a 10 caracteres, sin espacios laterales y en mayúsculas. |
| `nombre` | VARCHAR(80) | NOT NULL, de 3 a 80 caracteres y sin espacios laterales. |
| `categoria` | VARCHAR(20) | NOT NULL; Papelería, Tecnología o Accesorios. |
| `precio` | NUMERIC(10,2) | NOT NULL; entre 0.01 y 99999999.99. |
| `stock` | INTEGER | NOT NULL; mayor o igual que cero. |

El controlador aplica trim a código y nombre y convierte el código a mayúsculas antes de validar. Los CHECK de PostgreSQL rechazan datos que incumplen las reglas; no normalizan automáticamente las entradas. La interfaz rechaza precios con más de dos decimales antes de enviarlos a la base.

## Configuración de la conexión

1. Copiar `config/database.properties.example` como `config/database.properties`.
2. Completar el archivo local con los datos del servidor:

```properties
db.url=jdbc:postgresql://localhost:5432/papeleria_campus
db.user=postgres
db.password=CAMBIAR_CONTRASENA
```

Reemplazar el valor de `db.password` por la contraseña real y ajustar el usuario o puerto si corresponde. Escribir los valores sin comillas. Guardar el archivo en UTF-8.

La configuración real debe quedar excluida mediante esta línea en `.gitignore`:

```gitignore
/config/database.properties
```

Versionar únicamente el archivo de ejemplo sin secretos. Excluir también el archivo real al preparar el ZIP de entrega; `.gitignore` no lo elimina automáticamente de un ZIP.

Por defecto, `ConexionBD` busca `config/database.properties` respecto al directorio de ejecución. También admite una ruta alternativa mediante la opción de la JVM `-Ddb.config=ruta/al/archivo.properties`.

## Ejecución en IntelliJ IDEA

1. Abrir el proyecto desde su `pom.xml` y cargarlo como proyecto Maven.
2. Seleccionar JDK 21 como SDK del proyecto y comprobar el JDK utilizado por Maven.
3. Recargar Maven para descargar las dependencias.
4. Completar la base de datos y la configuración indicadas anteriormente.
5. En **Run → Edit Configurations**, establecer el directorio de trabajo en la carpeta que contiene `pom.xml` y `config`. Puede utilizarse `$PROJECT_DIR$`.
6. Ejecutar el método `main` de `HelloApplication`.
7. Comprobar que aparezcan los 12 productos y el contador correspondiente.

El recurso FXML debe estar en `src/main/resources/ni/uam/edu/evaluacion_corte2/campus-view.fxml`, y su controlador debe ser `ni.uam.edu.evaluacion_corte2.controller.CampusController`.

Como alternativa, desde la raíz del proyecto se puede utilizar Maven:

```bash
mvn clean javafx:run
```

Si se utiliza el wrapper incluido, el comando en PowerShell es:

```powershell
.\mvnw.cmd clean javafx:run
```

## Flujo de la aplicación

1. El usuario interactúa con `campus-view.fxml`.
2. `CampusController` recibe el evento y valida los datos cuando corresponde.
3. El controlador llama a `ProductoDAO`.
4. El DAO obtiene una conexión mediante `ConexionBD` y ejecuta SQL en PostgreSQL.
5. En las consultas, el DAO transforma cada fila del ResultSet en un objeto Producto y devuelve la lista.
6. El controlador actualiza la ObservableList que alimenta al TableView y recalcula el contador.

Las consultas usan `executeQuery`; las escrituras usan `executeUpdate` y devuelven el número de filas afectadas. Connection, PreparedStatement y ResultSet se cierran mediante try-with-resources. Las SQLException se propagan al controlador, donde se registra la operación y el SQLState y se muestra un mensaje al usuario.

## Pruebas y evidencias

Registrar en `pruebas.md` la entrada, el resultado esperado, el resultado obtenido, el estado y el archivo de evidencia de cada prueba. No marcar una prueba como aprobada antes de ejecutarla.

- **B1–B7:** búsquedas por texto, categorías, existencias, filtros combinados, cero coincidencias y limpieza de filtros.
- **CRUD:** crear X001, Producto de prueba, Accesorios, 10.00 y stock 1; actualizarlo a 15.50 y stock 3; cancelar su eliminación y luego confirmarla. Verificar con SELECT y reiniciar la aplicación para comprobar persistencia.
- **V1–V10:** entradas inválidas de código, nombre, categoría, precio y stock; códigos duplicados; actualizar y eliminar sin selección. Aplicar los casos al crear y actualizar cuando corresponda.
- **E1–E4:** conexión fallida, restricción SQL provocada mediante una llamada de prueba al DAO, cero filas afectadas y diagnóstico de errores. No agregar botones de prueba a la interfaz final.
- Verificar también que un producto agregado externamente aparezca al recargar y que una actualización respete los filtros vigentes.

Guardar en `evidencias/` capturas legibles del listado inicial, el filtro combinado, una validación y el ciclo CRUD. Las capturas complementan el proyecto ejecutable y la matriz de pruebas.

### Restauración de los datos de prueba

Usar exclusivamente la base local de pruebas. Para volver a los 12 registros iniciales, ejecutar conectado a `papeleria_campus`:

```sql
TRUNCATE TABLE public.producto RESTART IDENTITY;
```

Esta operación elimina todos los productos de la tabla de prueba. Después ejecutar nuevamente `sql/datos.sql` y verificar que el total sea 12. No es necesario volver a ejecutar `estructura.sql`.

## Solución de problemas frecuentes

| Problema | Revisión |
|---|---|
| No se cargan los productos | Comprobar que el servicio PostgreSQL esté activo y que host, puerto y nombre de base sean correctos. |
| Credenciales rechazadas | Revisar el usuario y la contraseña del archivo local de configuración. |
| Archivo de configuración no localizado | Confirmar la existencia de `config/database.properties` y el directorio de trabajo. |
| Dependencia PostgreSQL marcada en rojo | Recargar Maven y revisar la descarga de las dependencias. |
| Error al cargar el FXML | Comprobar la ruta, el nombre de CampusController, los fx:id y los métodos onAction. |
| Código duplicado | Utilizar un código diferente; la restricción UNIQUE también considera el código normalizado que envía la interfaz. |
| Producto eliminado externamente | Recargar el listado; el controlador comprueba cero filas afectadas antes de anunciar una actualización o eliminación exitosa. |

Un error de consulta debe mostrarse como fallo de carga, no como una búsqueda exitosa con cero resultados.

## Entrega y repositorio

Entregar `Mayorga_Rolando_LabTableView.zip` con el código fuente, recursos FXML, `pom.xml`, scripts SQL, configuración de ejemplo, este README, `pruebas.md` y `evidencias/`. Compartir por separado el enlace del repositorio GitHub con acceso de lectura para el docente.

El repositorio debe tener al menos tres commits no vacíos de avances reales sobre búsquedas, validaciones e integración CRUD. No incluir contraseñas reales ni sustituir el proyecto fuente por archivos compilados o capturas.

## Fuentes y herramientas de apoyo

- Guía del docente: **Laboratorio TableView - CRUD**, utilizada como referencia para el modelo de datos, requisitos, pruebas y entrega.
- [Documentación de OpenJFX: TableView](https://openjfx.io/javadoc/21/javafx.controls/javafx/scene/control/TableView.html).
- [Documentación de pgJDBC: ejecución de consultas](https://jdbc.postgresql.org/documentation/query/).
- [PostgreSQL: restricciones de integridad](https://www.postgresql.org/docs/15/ddl-constraints.html).
- [PostgreSQL: coincidencia de patrones e ILIKE](https://www.postgresql.org/docs/15/functions-matching.html).
- [Oracle: manejo de SQLException](https://docs.oracle.com/javase/tutorial/jdbc/basics/sqlexception.html).
- [Oracle: try-with-resources](https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html).
