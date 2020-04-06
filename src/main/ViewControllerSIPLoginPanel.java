package main;

import datamodel.DATABASE_LOCAL;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.DatabaseManagerLocal;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ViewControllerSIPLoginPanel implements Initializable {

    public TextField LBL_sip_server;
    public Label LBL_sip_user_name;
    public Label LBL_sip_password;
    public Label LBL_error_message;
    public TextField LBL_login;
    public PasswordField LBL_password;
    public Button BTN_logIn;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LBL_error_message.setVisible(false);

    }


    public void addSIPAccount(ActionEvent actionEvent) throws SQLException {
        LBL_error_message.setVisible(false);
        String server = LBL_sip_server.getText();
        String user = LBL_login.getText();
        String password = LBL_password.getText();

        if (server.isEmpty() || user.isEmpty() || password.isEmpty()) {
            LBL_error_message.setVisible(true);
            return;
        }
        DatabaseManagerLocal databaseManagerLocal = new DatabaseManagerLocal();
        String query = "SELECT COUNT(*) AS isset FROM " + DATABASE_LOCAL.tables.sip_account + " LIMIT 1";
        ResultSet resultSet = databaseManagerLocal.select(query);
        if (resultSet.getInt("isset") == 0) {
            resultSet.close();
            query = "INSERT INTO " + DATABASE_LOCAL.tables.sip_account + "(server, user_name, password) VALUES('" + server + "', " +
                    "'" + user + "', '" + password + "')";
            databaseManagerLocal.insert(query);
        } else {
            resultSet.close();
            query = "UPDATE " + DATABASE_LOCAL.tables.sip_account + " SET server = '" + server + "', user_name = '" + user + "', password = '" + password + "'";
            databaseManagerLocal.updaate(query);
        }
        Stage stage = (Stage) BTN_logIn.getScene().getWindow();
        stage.close();
        databaseManagerLocal.close();
    }
}
