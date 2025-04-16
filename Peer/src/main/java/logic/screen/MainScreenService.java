package logic.screen;

import Communication.*;
import domain.User;
import domain.Video;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import logic.video.FileUtils;
import logic.video.FFmpegUtils;
import metadata.SQLiteManager;
import ui.screens.MainScreen;

import java.io.File;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class MainScreenService {

    private static final String VIDEO_STORAGE = "src/main/resources/video_storage/";

    public static Video uploadVideo(File uploadedFile, String videoTitle, String uploader, Stage stage){

        Stage loadingStage = new Stage();
        Label loadingLabel = new Label("Uploading...");
        VBox vbox = new VBox(loadingLabel);
        vbox.setAlignment(Pos.CENTER);
        Scene scene = new Scene(vbox, 200, 100);
        loadingStage.setScene(scene);
        loadingStage.initModality(Modality.APPLICATION_MODAL);
        loadingStage.initOwner(stage);

        ScreenUtils.setStageDisabled(stage, true);
        Task<Video> uploadingTask = new Task<>() {
            @Override
            protected Video call() throws Exception {

                String videoId = String.valueOf(UUID.randomUUID());
                long size = uploadedFile.length();
                FFmpegUtils.formatVideo(uploadedFile, videoId);
                int segmentNum = FileUtils.countFiles(new File(VIDEO_STORAGE + videoId), ".ts");

                if(segmentNum <= 0){
                    System.err.println("No generated segments!");
                    FileUtils.deleteDirectoryRecursively(new File(VIDEO_STORAGE + videoId));
                    return null;
                }
                return new Video(videoId, videoTitle, LocalDateTime.now(), uploader, size, segmentNum);
            }
        };

        uploadingTask.setOnSucceeded(_ -> {
            ScreenUtils.setStageDisabled(stage, false);
            loadingStage.close();
        });
        uploadingTask.setOnFailed(_ -> {
            ScreenUtils.setStageDisabled(stage, false);
            loadingStage.close();
        });

        Thread thread = new Thread(uploadingTask);
        thread.setDaemon(true);
        thread.start();

        loadingStage.showAndWait();

        return uploadingTask.getValue();

    }

    public static boolean sendVideoToTracker(Video video, Socket clientSocket) throws Exception {

        Sender sender = new Sender(clientSocket);
        Request request = new Request(Operations.UPLOAD_VIDEO, video);
        sender.send(request);

        Receiver receiver = new Receiver(clientSocket);
        Response response = (Response) receiver.receive();

        if(response.getEx() != null)
            throw response.getEx();

        return (boolean) response.getResult();
    }

    public static List<Video> getAllVideos(Socket clientSocket) throws Exception {

        Sender sender = new Sender(clientSocket);
        Request request = new Request(Operations.FETCH_ALL_VIDEOS, null);
        sender.send(request);

        Receiver receiver = new Receiver(clientSocket);
        Response response = (Response) receiver.receive();

        if(response.getEx() != null)
            throw response.getEx();

        return (List<Video>) response.getResult();
    }

    public static void showScreen(Stage primaryStage,String title, Socket clientSocket) {
        try {

            FXMLLoader loader = ScreenUtils.openNewScene("/fxml_files/main_screen.fxml", title, primaryStage);
            primaryStage.show();

            User user = SQLiteManager.getUser();
            Sender sender = new Sender(clientSocket);
            sender.send(new Request(Operations.PEER_HANDSHAKE, SQLiteManager.getHandshakeData()));
            Receiver receiver = new Receiver(clientSocket);
            Response response = (Response) receiver.receive();

            if (!(boolean) response.getResult()) {
                ScreenUtils.showDialogMessage("Video data", null, "Handshake is not done.", Alert.AlertType.ERROR);
                return;
            }
            MainScreen main_screen = ScreenUtils.getController(loader);
            main_screen.initialize(ScreenUtils.getStage(loader), clientSocket, user);


        } catch (Exception ex) {
            ScreenUtils.showDialogMessage("Main", null, ex.getMessage(), Alert.AlertType.ERROR);
        }
    }
}
