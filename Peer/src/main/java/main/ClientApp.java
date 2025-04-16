package main;

import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import logic.screen.LoginScreenService;
import logic.screen.MainScreenService;
import logic.screen.ScreenUtils;
import metadata.SQLiteManager;

import java.net.InetAddress;
import java.net.Socket;

public class ClientApp extends Application {

    private static final String TRACKER_ADDRESS = "localhost";
    private static final int PORT = 8004;
    private static final String HOSTNAME = "192.168.1.101";

    @Override
    public void start(Stage primaryStage){

        Socket clientSocket;
        try {
            clientSocket = new Socket(
                    TRACKER_ADDRESS,
                    PORT,
                    InetAddress.getByName(HOSTNAME),
                    0);

            SQLiteManager.createTables();

            if(SQLiteManager.userExists())
                MainScreenService.showScreen(primaryStage, "NUCLEUSTREAM", clientSocket);
            else
                LoginScreenService.showScreen(primaryStage, clientSocket);

        } catch (Exception ex) {
            ScreenUtils.showDialogMessage("Error", null, ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}