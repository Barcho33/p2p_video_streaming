package logic.screen;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Control;
import javafx.stage.Stage;


import java.io.IOException;

public class ScreenUtils {


    public static void showDialogMessage(String title, String headerText, String contentText, Alert.AlertType messageStatus){

        Alert alert = new Alert(messageStatus);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        alert.showAndWait();
    }

    public static FXMLLoader openNewScene(String fxmlFile, String title, Stage stage) throws IOException {
        try {

            FXMLLoader loader = new FXMLLoader(ScreenUtils.class.getResource(fxmlFile));
            Parent root = loader.load();

            if(stage == null)
                stage = new Stage();

            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();

            return loader;

        } catch (IOException ex) {
            System.err.println("I/O error: " + ex);
            throw ex;
        }

    }

    public static <T> T getController(FXMLLoader loader) {
        try {
            return loader.getController();
        } catch (NullPointerException e) {
            throw new NullPointerException("Controller could not be retrieved: " + e.getMessage());
        }
    }

    public static Stage getStage(FXMLLoader loader) {
        try {
            Stage stage;
            Parent root = loader.getRoot();

            if (root == null) {

                root = loader.load();
                Scene scene = new Scene(root);
                stage = new Stage();
                stage.setScene(scene);

            }else
                stage = (Stage) root.getScene().getWindow();

            return stage;

        } catch (IOException ex) {
            System.err.println("Get stage error: ");
            throw new RuntimeException(ex.getMessage());
        }
    }
   public static void setStageDisabled(Stage stage, boolean status) {
        for (Node node : stage.getScene().getRoot().lookupAll("*")) {
            if (node instanceof Control control) {
                control.setDisable(status);
            }
        }
    }
}
