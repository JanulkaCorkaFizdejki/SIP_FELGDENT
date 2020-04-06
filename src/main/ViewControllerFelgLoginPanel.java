package main;

import datamodel.HTTP_DATA_STATUS;
import datamodel.INTERNET_ACCESS;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import model.CheckNetworkAccess;
import model.NetworkDataManager;
import views.Alerts;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ViewControllerFelgLoginPanel implements Initializable {
    public Button BTN_logIn;
    public TextField LBL_login;
    public PasswordField LBL_password;
    public Pane PANE_progress_ind_wrap;
    public Group GR_loading;
    public Label LBL_error_message;
    private ProgressIndicator progressIndicator = new ProgressIndicator();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LBL_error_message.setVisible(false);
        GR_loading.setVisible(false);
        progressIndicator.setPrefSize(10.0, 10.0);
        progressIndicator.setStyle("-fx-progress-color:  #00b2e7");
        PANE_progress_ind_wrap.getChildren().add(progressIndicator);

    }


    public void loginFelgAction(ActionEvent actionEvent) {
        LBL_error_message.setVisible(false);
        LBL_error_message.setText("");

        if (CheckNetworkAccess.INSTANCE.check().equals(INTERNET_ACCESS.NO_CONNECTION)) {
            Alerts.simple.INSTANCE.show(Alert.AlertType.WARNING, "Uwaga!", INTERNET_ACCESS.NO_CONNECTION.message());
            return;
        }

        String login = LBL_login.getText();
        String password = LBL_password.getText();

        if (login.isEmpty()  && password.isEmpty()) return;

        login = login.replaceAll("\\s", "");
        password = password.replaceAll("\\s", "");

        if (login.length() < 3 && password.length() < 3) {
            LBL_error_message.setText("Minimalna liczba znaków w loginie i haśle = 3!");
            LBL_error_message.setVisible(true);
            return;
        }


        GR_loading.setVisible(true);

        String finalLogin = login;
        String finalPassword = password;
        Runnable runnable = () -> {
            NetworkDataManager networkDataManager = new NetworkDataManager();

            HTTP_DATA_STATUS http = networkDataManager.loginFelg(finalLogin, finalPassword);
            Platform.runLater(() -> {
                GR_loading.setVisible(false);
                if (http == HTTP_DATA_STATUS.LOGIN_PASSWORD_ERROR || http == HTTP_DATA_STATUS.SERVER_ERROR || http == HTTP_DATA_STATUS.DATA_ERROR) {
                    LBL_error_message.setText(http.message());
                    LBL_error_message.setVisible(true);
                    System.out.println(http.message());
                } else if (http == HTTP_DATA_STATUS.SERVER_DATA_OK) {
                    try {
                        goToContoller ();
                    } catch (IOException | SQLException e) {
                        e.printStackTrace();
                    }
                }
            });
        };
        Thread thread = new Thread(runnable);
        thread.start();

    }

    private void goToContoller () throws IOException, SQLException {
        Stage stage = (Stage) BTN_logIn.getScene().getWindow();
        stage.close();
        Controller controller = new Controller();
        controller.showThisViewController();
    }
}
