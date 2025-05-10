package ui.screens;

import domain.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import logic.screen.LoginScreenService;
import logic.screen.ScreenUtils;
import metadata.SQLiteManager;


import java.net.Socket;
import java.nio.file.Path;

public class LoginScreen {

    private Stage stage;
    private Socket clientSocket;

    public void initialize(Stage stage, Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.stage = stage;
        this.logo.setImage(new Image(getClass().getResource("/images/logo.png").toExternalForm()));

        stage.setResizable(false);
    }

    @FXML
    private TextField txtUsername;
    @FXML
    private Button btnNext;
    @FXML
    private ImageView logo;
    @FXML
    private void handleHoverIn(){
        String currentStyle = btnNext.getStyle();
        String updatedStyle = currentStyle.replaceAll(  "-fx-background-color:.*?;",
                "-fx-background-color: linear-gradient(to right, #d17fb2, #9254c9);");
        btnNext.setStyle(updatedStyle);
    }
    @FXML
    private void handleHoverOut(){
        String currentStyle = btnNext.getStyle();
        String updatedStyle = currentStyle.replaceAll(  "-fx-background-color:.*?;",
                "-fx-background-color: linear-gradient(to right, #a4508b, #5f0a87);");
        btnNext.setStyle(updatedStyle);
    }
    @FXML
    private void handleLogin() {
        String username = txtUsername.getText();

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