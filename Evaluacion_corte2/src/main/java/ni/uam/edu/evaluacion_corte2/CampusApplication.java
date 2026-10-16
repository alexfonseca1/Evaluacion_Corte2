package ni.uam.edu.evaluacion_corte2;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class CampusApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader loader = new FXMLLoader(
                CampusApplication.class.getResource("campus-view.fxml")
        );

        Scene scene = new Scene(loader.load(), 600, 700);

        stage.setTitle("Papelería Campus - Gestión de productos");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}