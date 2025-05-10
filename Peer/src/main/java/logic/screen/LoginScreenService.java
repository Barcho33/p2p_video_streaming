package logic.screen;

import java.net.Socket;
import Communication.Response;
import Communication.Operations;
import Communication.Receiver;
import Communication.Sender;
import Communication.Request;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import ui.screens.LoginScreen;

public class LoginScreenService {

    public static boolean createAccount(String videoId, String username, Socket clientSocket) throws Exception {

        String[] credentials = {videoId, username};

        Sender sender = new Sender(clientSocket);
        Request request = new Request(Operations.REGISTER, credentials);
        sender.send(request);

        Receiver receiver = new Receiver(clientSocket);
        Response response = (Response) receiver.receive();

        if(response.getEx() != null)
            throw response.getEx();

        return  (boolean) response.getResult();
    }

    public static boolean checkUsername(String username, Socket clientSocket) throws Exception {

        Sender sender = new Sender(clientSocket);
        Request request = new Request(Operations.VERIFY_USER_EXISTS, username);
        sender.send(request);

        Receiver receiver = new Receiver(clientSocket);
        Response response = (Response) receiver.receive();

        if(response.getEx() != null)
            throw response.getEx();

        return  (boolean) response.getResult();
    }

    public static void showScreen(Stage primaryStage, Socket clientSocket){
        try{

            FXMLLoader loader = ScreenUtils.openNewScene("/fxml_files/login_screen.fxml", "", primaryStage);
            primaryStage.show();

            LoginScreen loginScreen = ScreenUtils.getController(loader);
            loginScreen.initialize(ScreenUtils.getStage(loader), clientSocket);

        }catch (Exception ex){
            ScreenUtils.showDialogMessage("Login", null, ex.getMessage(), Alert.AlertType.ERROR);
        }
    }
}
