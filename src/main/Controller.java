package main;

import datamodel.DATABASE_LOCAL;
import datamodel.HTTP_DATA_STATUS;
import datamodel.INTERNET_ACCESS;
import datamodel.SQLITEPopularQuery;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.*;
import views.Alerts;
import views.Colors;
import javafx.scene.image.Image;
import views.WindowSizes;
import views.WindowTitles;
import webphone.webphone;

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
    public Pane PANE_call;
    public Pane PANE_contact_list_wrapper;
    public Circle AvatarCallUser;
    public Label LBL_call_pane_user_name;
    public Label LBL_call_pane_phone_number;
    public Pane PANE_keyboard;
    public GridPane GRID_keyboardPane;
    public Button BTN_call;
    private INTERNET_ACCESS internet_access = null;
    private ProgressIndicator progressIndicator = new ProgressIndicator();
    private Timer timer = new Timer();
    ObservableList<String> contact_list = FXCollections.observableArrayList();
    ListView<String> contactList = null;
    ContextMenu contextMenuForContactList = new ContextMenu();
    private boolean keyboardVisible = true;
    webphone wobj = new webphone();
    boolean conversation = false;


    public void setContactList () throws SQLException {
        contact_list.clear();

        DatabaseManagerLocal databaseManagerLocal = new DatabaseManagerLocal();
        String query = "SELECT * FROM " + DATABASE_LOCAL.tables.contacts;
        ResultSet resultSet = databaseManagerLocal.select(query);
        while (resultSet.next()) {
            String user_name = resultSet.getString("user_name");
            contact_list.add(user_name);
        }
        resultSet.close();
        databaseManagerLocal.close();

        contactList = new ListView<>(contact_list);

        contactList.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                return new ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
                        } else {
                            setText(item);
                            setTextFill(Color.WHITE);
                            setFont(Font.font(14));
                            setCursor(Cursor.HAND);
                            setContextMenu(contextMenuForContactList);
                        }
                    }
                };
            }
        });
        contactList.setStyle("-fx-background-color: " + Colors.CYANDARKhex00aadc);
        contactList.setPrefWidth(180.0);
        System.out.println(contactList.getItems().getClass());

        PANE_contact_list_wrapper.getChildren().add(contactList);
        contactList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

                try {
                    setCallPane(newValue);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setCallPane (String userName) throws SQLException {
        DatabaseManagerLocal databaseManagerLocal = new DatabaseManagerLocal();
        String query = "SELECT * FROM " + DATABASE_LOCAL.tables.contacts + " WHERE user_name = '" + userName + "' LIMIT 1";
        ResultSet resultSet = databaseManagerLocal.select(query);
        LBL_call_pane_user_name.setText(resultSet.getString("user_name"));
        LBL_call_pane_phone_number.setText(resultSet.getString("user_phone_number"));
        resultSet.close();
        databaseManagerLocal.close();
        PANE_call.setVisible(true);
        PANE_keyboard.setVisible(false);
        keyboardVisible = false;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        internet_access = INTERNET_ACCESS.INIT;
        LBL_net_info.setText(internet_access.message());
        ICON_net_status.setFill(Paint.valueOf(Colors.YELLOWhexffbd2e));
        ICON_felg_user_status.setFill(Paint.valueOf(Colors.YELLOWhexffbd2e));
        Tooltip netStatusTooltip = new Tooltip("Stan połączenia z Internetem");
        LBL_net_info.setTooltip(netStatusTooltip);

        try {
            setViewElements();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }

        checkFelgLoginStatus ();

        try {
            setContactList();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        NetStatusObservable netStatusObservable = new NetStatusObservable();
        netStatusObservable.attach(this);
    }

    public void showThisViewController () throws IOException, SQLException {
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

        DatabaseManagerLocal databaseManagerLocal = new DatabaseManagerLocal();
        String query = "SELECT COUNT(*) AS count FROM " + DATABASE_LOCAL.tables.sip_account + " LIMIT 1";
        ResultSet resultSet = databaseManagerLocal.select(query);
        if (resultSet.getInt("count") == 0) {
            editSIPLoginPane();
        }

        resultSet.close();
        databaseManagerLocal.close();
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

    private void setViewElements () throws SQLException, IOException {
        // Set bottom loading group
        GR_loading_bottom.setVisible(false);
        progressIndicator.setPrefSize(10.0, 10.0);
        progressIndicator.setStyle("-fx-progress-color: white");
        PANE_progress_ind_wrap.getChildren().add(progressIndicator);

        Image avatar_call_user = new Image("call_user_avatar.png");
        AvatarCallUser.setFill(new ImagePattern(avatar_call_user));
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


        // ***
        MenuItem menuItemDeleteContactUser  = new MenuItem("Usuń");
        contextMenuForContactList.getItems().add(menuItemDeleteContactUser);

         GRID_keyboardPane.getChildren().forEach((b) -> {
             if (b instanceof Button) {
                 ((Button) b).setOnAction(new EventHandler<ActionEvent>() {
                     @Override
                     public void handle(ActionEvent event) {
                         printCustomNumber (((Button) b).getText());
                     }
                 });
             }
         });

    }

    private void checkFelgLoginStatus () {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (internet_access.equals(INTERNET_ACCESS.NO_CONNECTION)) return;
                getFelgLoginStatus();
            }
        };
        timer.schedule(timerTask, 60000, 60000);
    }

    private void getFelgLoginStatus () {
        GR_loading_bottom.setVisible(true);

        Runnable runnable = () -> {
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
                    Platform.runLater(() -> {  Avatar.setFill(new ImagePattern(image));});
                }

                Platform.runLater(() -> { LBL_user_name.setText(userName); });

                resultSet.close();
                databaseManagerLocal.close();

                HTTP_DATA_STATUS internet_access = networkDataManager.loginFelg(login, password);
                if (internet_access == HTTP_DATA_STATUS.SERVER_DATA_OK) {
                    Platform.runLater(() -> { ICON_felg_user_status.setFill(Paint.valueOf(Colors.GREENhex28ca42));});
                } else if (internet_access == HTTP_DATA_STATUS.LOGIN_PASSWORD_ERROR) {
                    Platform.runLater(() -> { ICON_felg_user_status.setFill(Paint.valueOf(Colors.GRREYhexa1a0a5));});
                } else {
                    Platform.runLater(() -> { ICON_felg_user_status.setFill(Paint.valueOf(Colors.REDhexff6059));});
                }

               Platform.runLater(() -> { GR_loading_bottom.setVisible(false);});
            }
            resultSet.close();
            databaseManagerLocal.close();


        } catch (SQLException e) {
            e.printStackTrace();
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    private void deleteContactUser() {

    }

    public void toogleKeyboard(ActionEvent actionEvent) {
        keyboardVisible = !keyboardVisible;
        if ((keyboardVisible)) {
            PANE_keyboard.setVisible(true);
            LBL_call_pane_user_name.setText("Nieznany");
            LBL_call_pane_phone_number.setText("+");
        } else {
            PANE_keyboard.setVisible(false);
        }
    }

    public void printCustomNumber (String keybordLetter) {
        String phoneNumberText =  LBL_call_pane_phone_number.getText();


        if (keybordLetter.equals("+48")) {
            LBL_call_pane_phone_number.setText("+48 ");
            return;
        }

        if (keybordLetter.equals("Clear")) {
            LBL_call_pane_phone_number.setText("+");
            return;
        }

        if (keybordLetter.equals("Back")) {
            if (phoneNumberText.length() < 2) return;
            LBL_call_pane_phone_number.setText(removeLastChar(phoneNumberText));
            return;
        }


        if (phoneNumberText.length() > 15) return;
        LBL_call_pane_phone_number.setText(phoneNumberText + keybordLetter);
    }

    private static String removeLastChar(String str) {
        return str.substring(0, str.length() - 1);
    }

    public void showSipPane(ActionEvent actionEvent) {
//        DatabaseManagerLocal databaseManagerLocal = new DatabaseManagerLocal();
//        String query = "SELECT * FROM " + DATABASE_LOCAL.tables.sip_account + " LIMIT 1";
    }

    private void editSIPLoginPane () throws IOException {
        Stage stage = new Stage();
        Parent root = FXMLLoader.load(getClass().getResource("ViewControllerSIPLoginPanel.fxml"));
        stage.setTitle(WindowTitles.login_sip);
        stage.setScene(WindowSizes.login.INSTANCE.create(root));
        stage.setResizable(false);
        stage.getIcons().add(new Image("felgdent_logo_as_icon.png"));
        stage.show();
    }


    public void call(ActionEvent actionEvent) throws SQLException, IOException {
        String phone_number = LBL_call_pane_phone_number.getText();

        if (phone_number.length() < 10) {
            Alerts.simple.INSTANCE.show(Alert.AlertType.WARNING, "Nieprawidłowe dane!", "WPROWADŹ POPRAWNY NUMER TELEFONU");
            return;
        }

        DatabaseManagerLocal databaseManagerLocal = new DatabaseManagerLocal();
        String query = "SELECT COUNT(*) AS isset FROM " + DATABASE_LOCAL.tables.sip_account + " LIMIT 1";
        ResultSet resultSet = databaseManagerLocal.select(query);

        if (resultSet.getInt("isset") == 0) {
            resultSet.close();
            editSIPLoginPane();
            databaseManagerLocal.close();
            return;
        }

        if (resultSet.getInt("isset") == 1) {
            resultSet.close();
            conversation = !conversation;

            if (conversation) {
                BTN_call.getStyleClass().add("buttonCallConversation");
            } else {
                BTN_call.getStyleClass().remove("buttonCallConversation");
                wobj.API_Stop();
                return;
            }
            query = "SELECT * FROM " + DATABASE_LOCAL.tables.sip_account + " LIMIT 1";
            resultSet = databaseManagerLocal.select(query);

            String server   = resultSet.getString("server");
            String user     = resultSet.getString("user_name");
            String password = resultSet.getString("password");
            resultSet.close();


            if (PANE_keyboard.isVisible()) {
                phone_number = phone_number.replaceAll("\\s", "");
                phone_number = phone_number.replace("+", "");
                System.out.println(phone_number);
            } else {

                query = "SELECT phone_number FROM " + DATABASE_LOCAL.tables.contacts + " WHERE user_phone_number = '" + phone_number + "' LIMIT 1";
                resultSet = databaseManagerLocal.select(query);
                phone_number = resultSet.getString("phone_number");
                resultSet.close();
                System.out.println(phone_number);
            }
            databaseManagerLocal.close();

            wobj.API_SetParameter("serveraddress", server);
            wobj.API_SetParameter("username", user);
            wobj.API_SetParameter("password", password);
            wobj.API_AudioDevice();
            wobj.API_Start();
            wobj.API_Call(-1, phone_number);

        }

    }
}
