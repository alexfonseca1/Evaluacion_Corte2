package ni.uam.edu.evaluacion_corte2.controller;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ni.uam.edu.evaluacion_corte2.DAO.ProductoDAO;
import ni.uam.edu.evaluacion_corte2.model.Producto;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

public class CampusController {

    @FXML
    private TextField txtCodigo;
    @FXML
    private TextField txtNombre;
    @FXML
    private ComboBox<String> cmbCategoria;
    @FXML
    private TextField txtPrecio;
    @FXML
    private TextField txtStock;
    @FXML
    private TextField txtBuscar;
    @FXML
    private ComboBox<String> cmbFiltroCategoria;
    @FXML
    private CheckBox chkSoloExistencias;
    @FXML
    private TableView<Producto> tblProductos;
    @FXML
    private TableColumn<Producto, Integer> colId;
    @FXML
    private TableColumn<Producto, String> colCodigo;
    @FXML
    private TableColumn<Producto, String> colNombre;
    @FXML
    private TableColumn<Producto, String> colCategoria;
    @FXML
    private TableColumn<Producto, BigDecimal> colPrecio;
    @FXML
    private TableColumn<Producto, Integer> colStock;
    @FXML
    private Label lblContador;

    private static final List<String> CATEGORIAS = List.of(
            "Papelería",
            "Tecnología",
            "Accesorios"
    );

    private static final BigDecimal PRECIO_MINIMO =
            new BigDecimal("0.01");

    private static final BigDecimal PRECIO_MAXIMO =
            new BigDecimal("99999999.99");

    private final ProductoDAO productoDAO = new ProductoDAO();

    private final ObservableList<Producto> productos =
            FXCollections.observableArrayList();

    private Integer idSeleccionado;

    @FXML
    private void initialize() {
        configurarCategorias();
        configurarTabla();
        configurarSeleccion();

        cargarProductos(false);
    }

    private void configurarCategorias() {
        cmbCategoria.setItems(
                FXCollections.observableArrayList(CATEGORIAS)
        );

        cmbCategoria.setEditable(false);

        cmbFiltroCategoria.setItems(
                FXCollections.observableArrayList(
                        "Todas",
                        "Papelería",
                        "Tecnología",
                        "Accesorios"
                )
        );

        cmbFiltroCategoria.setEditable(false);
        cmbFiltroCategoria.setValue("Todas");
    }

    private void configurarTabla() {
        colId.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(
                        data.getValue().getIdProducto()
                )
        );

        colCodigo.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getCodigo()
                )
        );

        colNombre.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getNombre()
                )
        );

        colCategoria.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getCategoria()
                )
        );

        colPrecio.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(
                        data.getValue().getPrecio()
                )
        );

        colStock.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(
                        data.getValue().getStock()
                )
        );

        // Muestra siempre dos decimales.
        colPrecio.setCellFactory(columna ->
                new TableCell<Producto, BigDecimal>() {
                    @Override
                    protected void updateItem(
                            BigDecimal precio,
                            boolean empty
                    ) {
                        super.updateItem(precio, empty);

                        if (empty || precio == null) {
                            setText(null);
                        } else {
                            setText(precio.setScale(2).toPlainString());
                        }
                    }
                }
        );

        tblProductos.setEditable(false);
        tblProductos.getSelectionModel()
                .setSelectionMode(SelectionMode.SINGLE);

        tblProductos.setItems(productos);
    }

    private void configurarSeleccion() {
        tblProductos.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, anterior, seleccionado) -> {
                    if (seleccionado != null) {
                        cargarFormulario(seleccionado);
                    } else {
                        vaciarFormulario();
                    }
                });
    }

    private void cargarFormulario(Producto producto) {
        idSeleccionado = producto.getIdProducto();

        txtCodigo.setText(producto.getCodigo());
        txtNombre.setText(producto.getNombre());
        cmbCategoria.setValue(producto.getCategoria());

        txtPrecio.setText(
                producto.getPrecio().setScale(2).toPlainString()
        );

        txtStock.setText(
                String.valueOf(producto.getStock())
        );
    }

    // Vacía el formulario sin modificar los filtros.
    private void vaciarFormulario() {
        idSeleccionado = null;

        txtCodigo.clear();
        txtNombre.clear();

        cmbCategoria.getSelectionModel().clearSelection();
        cmbCategoria.setValue(null);

        txtPrecio.clear();
        txtStock.clear();
    }

    @FXML
    private void limpiarFormulario() {
        tblProductos.getSelectionModel().clearSelection();
        vaciarFormulario();

        txtCodigo.requestFocus();
    }

    @FXML
    private void buscarProductos() {
        cargarProductos(true);
    }

    @FXML
    private void limpiarFiltros() {
        txtBuscar.clear();
        cmbFiltroCategoria.setValue("Todas");
        chkSoloExistencias.setSelected(false);

        cargarProductos(false);
    }

    @FXML
    private void recargarProductos() {
        cargarProductos(true);
    }

    // Consulta siempre al DAO y conserva los filtros actuales.
    private void cargarProductos(boolean avisarSinCoincidencias) {
        String texto = txtBuscar.getText().trim();
        String categoria = cmbFiltroCategoria.getValue();
        boolean soloExistencias = chkSoloExistencias.isSelected();

        try {
            List<Producto> resultado = productoDAO.buscar(
                    texto,
                    categoria,
                    soloExistencias
            );

            // El orden inicial lo determina el ORDER BY del DAO.
            tblProductos.getSortOrder().clear();

            limpiarFormulario();
            productos.setAll(resultado);

            lblContador.setText(
                    "Registros visibles: " + productos.size()
            );

            tblProductos.setPlaceholder(
                    new Label("Sin coincidencias")
            );

            if (avisarSinCoincidencias && resultado.isEmpty()) {
                mostrarAlerta(
                        Alert.AlertType.INFORMATION,
                        "Búsqueda",
                        "Sin coincidencias."
                );
            }

        } catch (SQLException e) {
            // Un fallo de conexión no se presenta como cero resultados.
            limpiarFormulario();
            productos.clear();

            lblContador.setText(
                    "Registros visibles: no disponibles"
            );

            tblProductos.setPlaceholder(
                    new Label("No se pudo cargar la información")
            );

            manejarErrorSQL("consultar productos", e);
        }
    }

    @FXML
    private void guardarProducto() {
        Producto producto;

        try {
            producto = leerYValidarFormulario();
        } catch (IllegalArgumentException e) {
            mostrarValidacion(e.getMessage());
            return;
        }

        try {
            int filas = productoDAO.insertar(producto);

            if (filas != 1) {
                mostrarAlerta(
                        Alert.AlertType.WARNING,
                        "Registro no guardado",
                        "No se pudo confirmar el registro del producto. "
                                + "Recargá el listado para verificar."
                );
                return;
            }

        } catch (SQLException e) {
            manejarErrorSQL("guardar producto", e);
            return;
        }

        mostrarAlerta(
                Alert.AlertType.INFORMATION,
                "Producto guardado",
                "El producto se guardó correctamente."
        );

        cargarProductos(false);
    }

    @FXML
    private void actualizarProducto() {
        if (!comprobarSeleccion()) {
            return;
        }

        Producto producto;

        try {
            producto = leerYValidarFormulario();
            producto.setIdProducto(idSeleccionado);

        } catch (IllegalArgumentException e) {
            mostrarValidacion(e.getMessage());
            return;
        }

        try {
            int filas = productoDAO.actualizar(producto);

            if (filas == 0) {
                informarProductoInexistente();
                return;
            }

            if (filas != 1) {
                mostrarAlerta(
                        Alert.AlertType.WARNING,
                        "Actualización",
                        "No se pudo confirmar la actualización. "
                                + "Recargá el listado para verificar."
                );
                return;
            }

        } catch (SQLException e) {
            manejarErrorSQL("actualizar producto", e);
            return;
        }

        mostrarAlerta(
                Alert.AlertType.INFORMATION,
                "Producto actualizado",
                "El producto se actualizó correctamente."
        );

        cargarProductos(false);
    }

    @FXML
    private void eliminarProducto() {
        if (!comprobarSeleccion()) {
            return;
        }

        Alert confirmacion = new Alert(
                Alert.AlertType.CONFIRMATION
        );

        confirmacion.setTitle("Eliminar producto");
        confirmacion.setHeaderText(
                "¿Querés eliminar el producto seleccionado?"
        );

        confirmacion.setContentText(
                "Código: " + txtCodigo.getText()
                        + "\nNombre: " + txtNombre.getText()
        );

        ButtonType respuesta = confirmacion.showAndWait()
                .orElse(ButtonType.CANCEL);

        if (respuesta != ButtonType.OK) {
            return;
        }

        try {
            int filas = productoDAO.eliminar(idSeleccionado);

            if (filas == 0) {
                informarProductoInexistente();
                return;
            }

            if (filas != 1) {
                mostrarAlerta(
                        Alert.AlertType.WARNING,
                        "Eliminación",
                        "No se pudo confirmar la eliminación. "
                                + "Recargá el listado para verificar."
                );
                return;
            }

        } catch (SQLException e) {
            manejarErrorSQL("eliminar producto", e);
            return;
        }

        mostrarAlerta(
                Alert.AlertType.INFORMATION,
                "Producto eliminado",
                "El producto se eliminó correctamente."
        );

        cargarProductos(false);
    }

    private boolean comprobarSeleccion() {
        if (idSeleccionado == null
                || tblProductos.getSelectionModel()
                .getSelectedItem() == null) {

            mostrarValidacion(
                    "Debés seleccionar un producto de la tabla."
            );

            return false;
        }

        return true;
    }

    private Producto leerYValidarFormulario() {
        // Primero normaliza el código y el nombre.
        String codigo = txtCodigo.getText()
                .trim()
                .toUpperCase(Locale.ROOT);

        String nombre = txtNombre.getText().trim();
        String categoria = cmbCategoria.getValue();

        String textoPrecio = txtPrecio.getText().trim();
        String textoStock = txtStock.getText().trim();

        txtCodigo.setText(codigo);
        txtNombre.setText(nombre);

        if (codigo.isEmpty() || codigo.length() > 10) {
            throw new IllegalArgumentException(
                    "El código debe tener entre 1 y 10 caracteres."
            );
        }

        if (nombre.length() < 3 || nombre.length() > 80) {
            throw new IllegalArgumentException(
                    "El nombre debe tener entre 3 y 80 caracteres."
            );
        }

        if (categoria == null || !CATEGORIAS.contains(categoria)) {
            throw new IllegalArgumentException(
                    "Seleccioná una categoría válida: "
                            + "Papelería, Tecnología o Accesorios."
            );
        }

        // Admite números decimales escritos con punto.
        // Rechaza letras, comas y notación científica.
        if (!textoPrecio.matches("[+-]?[0-9]+(\\.[0-9]+)?")) {
            throw new IllegalArgumentException(
                    "El precio debe ser un número. "
                            + "Usá punto como separador decimal: 65.00."
            );
        }

        BigDecimal precio;

        try {
            precio = new BigDecimal(textoPrecio);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "El precio ingresado no es válido."
            );
        }

        // Se comprueba antes de guardar para evitar redondeos.
        if (precio.scale() > 2) {
            throw new IllegalArgumentException(
                    "El precio admite como máximo dos decimales."
            );
        }

        if (precio.compareTo(PRECIO_MINIMO) < 0
                || precio.compareTo(PRECIO_MAXIMO) > 0) {

            throw new IllegalArgumentException(
                    "El precio debe estar entre 0.01 y 99999999.99."
            );
        }

        if (!textoStock.matches("[+-]?[0-9]+")) {
            throw new IllegalArgumentException(
                    "El stock debe ser un número entero, por ejemplo: 25."
            );
        }

        int stock;

        try {
            stock = Integer.parseInt(textoStock);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "El stock debe estar entre 0 y 2147483647."
            );
        }

        if (stock < 0) {
            throw new IllegalArgumentException(
                    "El stock no puede ser negativo. Se permite cero."
            );
        }

        Producto producto = new Producto();

        producto.setCodigo(codigo);
        producto.setNombre(nombre);
        producto.setCategoria(categoria);
        producto.setPrecio(precio.setScale(2));
        producto.setStock(stock);

        return producto;
    }

    private void informarProductoInexistente() {
        mostrarAlerta(
                Alert.AlertType.WARNING,
                "Producto inexistente",
                "El producto seleccionado ya no existe en la base "
                        + "de datos. Se recargará el listado."
        );

        cargarProductos(false);
    }

    private void mostrarValidacion(String mensaje) {
        mostrarAlerta(
                Alert.AlertType.WARNING,
                "Revisá los datos",
                mensaje
        );
    }

    private void manejarErrorSQL(
            String operacion,
            SQLException excepcion
    ) {
        // Registra operación y SQLState sin imprimir contraseñas.
        for (SQLException error = excepcion;
             error != null;
             error = error.getNextException()) {

            System.err.println(
                    "Operación: " + operacion
                            + " | SQLState: " + error.getSQLState()
                            + " | Código: " + error.getErrorCode()
            );
        }

        String estado = excepcion.getSQLState();
        String mensaje;

        if ("23505".equals(estado)) {
            mensaje = "Ya existe un producto con ese código. "
                    + "Ingresá un código diferente.";

        } else if ("23514".equals(estado)) {
            mensaje = "Los datos incumplen una restricción "
                    + "de la base de datos. Revisá los valores.";

        } else if ("23502".equals(estado)) {
            mensaje = "Falta un dato obligatorio.";

        } else if ("22003".equals(estado)) {
            mensaje = "Uno de los valores numéricos supera "
                    + "el rango permitido.";

        } else if (estado != null && estado.startsWith("28")) {
            mensaje = "PostgreSQL rechazó las credenciales. "
                    + "Revisá el usuario y la contraseña "
                    + "en la configuración de conexión.";

        } else if ("3D000".equals(estado)) {
            mensaje = "La base de datos configurada no existe. "
                    + "Revisá el nombre papeleria_campus.";

        } else if (estado != null && estado.startsWith("08")) {
            mensaje = "No se pudo establecer o mantener la conexión "
                    + "con PostgreSQL. Revisá el servicio, "
                    + "el host y el puerto.";

        } else {
            mensaje = "No se pudo completar la operación. "
                    + "Revisá la configuración y volvé a intentarlo.";
        }

        mostrarAlerta(
                Alert.AlertType.ERROR,
                "Error al " + operacion,
                mensaje
        );
    }

    private void mostrarAlerta(
            Alert.AlertType tipo,
            String titulo,
            String mensaje
    ) {
        Alert alerta = new Alert(tipo);

        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);

        alerta.showAndWait();
    }
}