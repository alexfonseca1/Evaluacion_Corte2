module ni.uam.edu.evaluacion_corte2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.postgresql.jdbc;

    opens ni.uam.edu.evaluacion_corte2 to javafx.fxml;
    opens ni.uam.edu.evaluacion_corte2.controller to javafx.fxml;
    opens ni.uam.edu.evaluacion_corte2.model to javafx.base;

    exports ni.uam.edu.evaluacion_corte2;
}