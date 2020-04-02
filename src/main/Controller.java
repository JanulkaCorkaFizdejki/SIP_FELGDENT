package main;

import datamodel.DATABASE_LOCAL;
import datamodel.INTERNET_ACCESS;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import model.*;
import views.Colors;
import javafx.scene.image.Image;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.ResourceBundle;

public class Controller implements Initializable, NetworkObserver {
    public Circle ICON_net_status;
    public Label LBL_net_info;
    public Circle Avatar;
    public Pane PANE_progress_ind_wrap;
    private INTERNET_ACCESS internet_access = null;
    private ProgressIndicator progressIndicator = new ProgressIndicator();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        NetStatusObservable netStatusObservable = new NetStatusObservable();
        netStatusObservable.attach(this);
        internet_access = INTERNET_ACCESS.INIT;

        LBL_net_info.setText(internet_access.message());
        ICON_net_status.setFill(Paint.valueOf(Colors.YELLOWhexffbd2e));

        Tooltip netStatusTooltip = new Tooltip("Stan połączenia z Internetem");
        LBL_net_info.setTooltip(netStatusTooltip);

//        NetworkDataManager networkDataManager = new NetworkDataManager();
//        networkDataManager.getAvatar();
        try {
            setViewElements();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        progressIndicator.setPrefSize(10.0, 10.0);
        progressIndicator.setStyle("-fx-progress-color: white");
        PANE_progress_ind_wrap.getChildren().add(progressIndicator);
    }

    @Override
    public void update(INTERNET_ACCESS internetAccess) {

        internet_access = internetAccess;

        Platform.runLater(() -> {

            LBL_net_info.setText(internet_access.message());

            if (internetAccess == INTERNET_ACCESS.CONNECTION) {
                ICON_net_status.setFill(Paint.valueOf(Colors.GREENhex28ca42));
            } else if (internetAccess == INTERNET_ACCESS.NO_CONNECTION) {
                ICON_net_status.setFill(Paint.valueOf(Colors.REDhexff6059));
            } else {
                ICON_net_status.setFill(Paint.valueOf(Colors.YELLOWhexffbd2e));
            }
        });
    }

    private void setViewElements () throws SQLException {
        DatabaseManagerLocal databaseManagerLocal = new DatabaseManagerLocal();
        String query = "SELECT * FROM " + DATABASE_LOCAL.tables.sip_user + " LIMIT 1";
        ResultSet resultSet = databaseManagerLocal.select(query);
        InputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(resultSet.getString("avatar")));

        resultSet.close();
        databaseManagerLocal.close();

        Image image = new Image(inputStream);
        Avatar.setFill(new ImagePattern(image));
    }
}
