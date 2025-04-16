package ui.screens;

import domain.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import logic.screen.LoginScreenService;
import logic.screen.ScreenUtils;
import metadata.SQLiteManager;


import java.net.Socket;

public class LoginScreen {

    private Stage stage;
    private Socket clientSocket;

    public void initialize(Stage stage, Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.stage = stage;

        stage.setResizable(false);
    }

    @FXML
    private TextField txt_username;

    @FXML
    private void handleLogin() {
        String username = txt_username.getText();

        try {
            if(LoginScreenService.checkUsername(username, clientSocket))
                ScreenUtils.showDialogMessage("Username", null, "Username already exist!", Alert.AlertType.ERROR);

            else{
                String uuid = SQLiteManager.insertUser(username);
                LoginScreenService.createAccount(uuid, username, clientSocket);

                stage.close();
                FXMLLoader loader = ScreenUtils.openNewScene("/fxml_files/main_screen.fxml",
                        "Main", null);

                User user = SQLiteManager.getUser();

                MainScreen main_screen = ScreenUtils.getController(loader);
                main_screen.initialize(ScreenUtils.getStage(loader), this.clientSocket, user);


            }
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }


    @FXML
    private void handleQuit(){
        Platform.exit();
        System.exit(0);
    }

}