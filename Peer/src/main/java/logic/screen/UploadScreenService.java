package logic.screen;

import Communication.*;
import domain.Thumbnail;
import domain.Video;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import logic.video.FFmpegUtils;
import logic.video.FileUtils;

import java.io.File;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.UUID;

public class UploadScreenService {
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
    public static boolean sendThumbnailToTracker(Thumbnail thumbnail, Socket clientSocket) throws Exception {

        Sender sender = new Sender(clientSocket);
        Request request = new Request(Operations.SAVE_THUMBNAIL, thumbnail);
        sender.send(request);

        Receiver receiver = new Receiver(clientSocket);
        Response response = (Response) receiver.receive();

        if(response.getEx() != null)
            throw response.getEx();

        return (boolean) response.getResult();
    }
}
