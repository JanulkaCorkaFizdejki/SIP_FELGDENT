package main;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.net.URL;
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
        String login = LBL_login.getText();
        String password = LBL_password.getText();

        if (login.isEmpty()  || password.isEmpty()) return;

        login = login.replaceAll("\\s", "");
        password = password.replaceAll("\\s", "");

        if (login.length() > 4 || password.length() > 4) return;

        System.out.println(login + " == " + password);


        GR_loading.setVisible(true);
//        Stage stage = (Stage) BTN_logIn.getScene().getWindow();
//        stage.close();
    }
}
