package ni.uam.edu.evaluacion_corte2.database;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class ConexionBD {

    private ConexionBD() {
    }

    public static Connection getConexion() throws SQLException {

        String rutaConfiguracion = System.getProperty(
                "db.config",
                "config/database.properties"
        );

        Path archivo = Path.of(rutaConfiguracion);
        Properties configuracion = new Properties();

        try (
                Reader lector = Files.newBufferedReader(
                        archivo,
                        StandardCharsets.UTF_8
                )
        ) {
            configuracion.load(lector);

        } catch (IOException e) {
            throw new SQLException(
                    "No se pudo leer el archivo de configuración "
                            + "de la base de datos.",
                    "08001",
                    e
            );
        }

        String url = configuracion.getProperty("db.url", "").trim();
        String usuario = configuracion.getProperty("db.user", "").trim();
        String contrasena = configuracion.getProperty("db.password");

        if (url.isEmpty()
                || usuario.isEmpty()
                || contrasena == null) {

            throw new SQLException(
                    "La configuración debe incluir db.url, "
                            + "db.user y db.password.",
                    "08001"
            );
        }

        Properties propiedadesConexion = new Properties();

        propiedadesConexion.setProperty("user", usuario);
        propiedadesConexion.setProperty("password", contrasena);

        propiedadesConexion.setProperty("connectTimeout", "5");

        return DriverManager.getConnection(
                url,
                propiedadesConexion
        );
    }
}