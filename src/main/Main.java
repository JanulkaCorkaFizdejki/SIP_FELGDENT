package main;

import datamodel.DATABASE_LOCAL;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import model.DatabaseManagerLocal;
import views.Alerts;
import views.WindowSizes;
import views.WindowTitles;

import java.io.File;
import java.sql.ResultSet;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        File dbSQLitefile = new File(DATABASE_LOCAL.name);
        if (!dbSQLitefile.exists()) {
            Alerts.simple.INSTANCE.show(Alert.AlertType.ERROR, "Błąd Aplikacji", "Brak pliku bazy danyych!");
            Platform.exit();
            System.exit(0);
        }

        DatabaseManagerLocal databaseManagerLocal = new DatabaseManagerLocal();
        String query = "SELECT COUNT(uid) AS isset FROM " + DATABASE_LOCAL.tables.sip_user + " LIMIT 1";
        ResultSet resultSet = databaseManagerLocal.select(query);

        if (resultSet.getInt("isset") == 0) {
            Parent root = FXMLLoader.load(getClass().getResource("ViewControllerFelgLoginPanel.fxml"));
            primaryStage.setTitle(WindowTitles.main);
            primaryStage.setScene(WindowSizes.login.INSTANCE.create(root));
            primaryStage.setResizable(false);
            primaryStage.show();
            primaryStage.setOnCloseRequest(event -> {
                Platform.exit();
                System.exit(0);
            });
        } else {
            Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
            primaryStage.setTitle(WindowTitles.main);
            primaryStage.setScene(WindowSizes.main.INSTANCE.create(root));
            primaryStage.setResizable(false);
            primaryStage.show();
            primaryStage.setOnCloseRequest(event -> {
                Platform.exit();
                System.exit(0);
            });
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
