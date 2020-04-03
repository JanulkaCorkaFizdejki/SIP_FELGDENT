package main;

import datamodel.DATABASE_LOCAL;
import datamodel.HTTP_DATA_STATUS;
import datamodel.INTERNET_ACCESS;
import datamodel.SQLITEPopularQuery;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import model.*;
import views.Colors;
import javafx.scene.image.Image;
import views.WindowSizes;
import views.WindowTitles;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class Controller implements Initializable, NetworkObserver {
    public Circle ICON_net_status;
    public Label LBL_net_info;
    public Circle Avatar;
    public Pane PANE_progress_ind_wrap;
    public Group GR_loading_bottom;
    public Label LBL_user_name;
    public Circle ICON_felg_user_status;
    private INTERNET_ACCESS internet_access = null;
    private ProgressIndicator progressIndicator = new ProgressIndicator();
    private Timer timer = new Timer();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        NetStatusObservable netStatusObservable = new NetStatusObservable();
        netStatusObservable.attach(this);
        internet_access = INTERNET_ACCESS.INIT;

        LBL_net_info.setText(internet_access.message());
        ICON_net_status.setFill(Paint.valueOf(Colors.YELLOWhexffbd2e));
        ICON_felg_user_status.setFill(Paint.valueOf(Colors.YELLOWhexffbd2e));

        Tooltip netStatusTooltip = new Tooltip("Stan połączenia z Internetem");
        LBL_net_info.setTooltip(netStatusTooltip);

        try {
            setViewElements();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        checkFelgLoginStatus ();

    }

    public void showThisViewController () throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"));
        Parent root = (Parent) fxmlLoader.load();
        Stage stage = new Stage();
        stage.setScene(WindowSizes.main.INSTANCE.create(root));
        stage.setTitle(WindowTitles.main);
        stage.getIcons().add(new Image("felgdent_logo_as_icon.png"));
        stage.show();
        stage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });

    }

    @Override
    public void update(INTERNET_ACCESS internetAccess) {

        internet_access = internetAccess;

        Platform.runLater(() -> {

            LBL_net_info.setText(internet_access.message());

            if (internetAccess == INTERNET_ACCESS.CONNECTION) {
                ICON_net_status.setFill(Paint.valueOf(Colors.GREENhex28ca42));
                getFelgLoginStatus();
            } else if (internetAccess == INTERNET_ACCESS.NO_CONNECTION) {
                ICON_net_status.setFill(Paint.valueOf(Colors.REDhexff6059));
                ICON_felg_user_status.setFill(Paint.valueOf(Colors.REDhexff6059));
            } else {
                ICON_net_status.setFill(Paint.valueOf(Colors.YELLOWhexffbd2e));
                ICON_felg_user_status.setFill(Paint.valueOf(Colors.YELLOWhexffbd2e));
            }
        });
    }

    private void setViewElements () throws SQLException {
        // Set bottom loading group
        GR_loading_bottom.setVisible(false);
        progressIndicator.setPrefSize(10.0, 10.0);
        progressIndicator.setStyle("-fx-progress-color: white");
        PANE_progress_ind_wrap.getChildren().add(progressIndicator);
        // ***

        // Set avatar
        DatabaseManagerLocal databaseManagerLocal = new DatabaseManagerLocal();
        String query = "SELECT * FROM " + DATABASE_LOCAL.tables.sip_user + " LIMIT 1";
        ResultSet resultSet = databaseManagerLocal.select(query);
        if (!(resultSet.getObject("avatar") instanceof Double)) {
            InputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(resultSet.getString("avatar")));
            Image image = new Image(inputStream);
            Avatar.setFill(new ImagePattern(image));
        }
        LBL_user_name.setText(resultSet.getString("user_name"));
        resultSet.close();
        databaseManagerLocal.close();
        // ***

    }

    private void checkFelgLoginStatus () {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (internet_access.equals(INTERNET_ACCESS.NO_CONNECTION)) return;
                getFelgLoginStatus();
            }
        };
        timer.schedule(timerTask, 20000, 20000);
    }
    private void getFelgLoginStatus () {
        GR_loading_bottom.setVisible(true);
        DatabaseManagerLocal databaseManagerLocal = new DatabaseManagerLocal();
        ResultSet resultSet = databaseManagerLocal.select(SQLITEPopularQuery.select_count_sip_user);
        NetworkDataManager networkDataManager = new NetworkDataManager();
        try {
            if (resultSet.getInt("isset") == 1) {
                resultSet.close();
                String query = "SELECT * FROM " + DATABASE_LOCAL.tables.sip_user + " LIMIT 1";
                resultSet = databaseManagerLocal.select(query);
                String login = resultSet.getString("appuid");
                String password = resultSet.getString("password");
                String userName = resultSet.getString("user_name");

                if (!(resultSet.getObject("avatar") instanceof Double)) {
                    InputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(resultSet.getString("avatar")));
                    Image image = new Image(inputStream);
                    Avatar.setFill(new ImagePattern(image));
                }
                Platform.runLater(() -> {
                    LBL_user_name.setText(userName);
                });


                resultSet.close();
                databaseManagerLocal.close();

                HTTP_DATA_STATUS internet_access = networkDataManager.loginFelg(login, password);
                if (internet_access == HTTP_DATA_STATUS.SERVER_DATA_OK) {
                    ICON_felg_user_status.setFill(Paint.valueOf(Colors.GREENhex28ca42));
                } else if (internet_access == HTTP_DATA_STATUS.LOGIN_PASSWORD_ERROR) {
                    ICON_felg_user_status.setFill(Paint.valueOf(Colors.GRREYhexa1a0a5));
                } else {
                    ICON_felg_user_status.setFill(Paint.valueOf(Colors.REDhexff6059));
                }

                GR_loading_bottom.setVisible(false);
            }
            resultSet.close();
            databaseManagerLocal.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
