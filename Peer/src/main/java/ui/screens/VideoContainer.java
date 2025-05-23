package ui.screens;

import javafx.animation.TranslateTransition;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import logic.screen.MainScreenService;
import logic.screen.ScreenUtils;
import logic.video.FileUtils;
import metadata.SQLiteManager;
import peer_server.PeerServer;

import java.io.File;
import java.net.Socket;


public class VideoContainer {

    private Socket clientSocket;
    private Stage mainStage;
    private String videoId;
    private MainScreen mainScreenController;
    private boolean currentMainPage;
    private VBox parentContainer;
    @FXML
    private ImageView imgTrash;

    public void initialize(Socket clientSocket, Stage mainStage, VBox parentContainer, String videoId, MainScreen mainScreenController, boolean currentMainPage){
        this.clientSocket = clientSocket;
        this.mainStage = mainStage;
        this.videoId = videoId;
        this.mainScreenController = mainScreenController;
        this.currentMainPage = currentMainPage;
        this.parentContainer = parentContainer;

        if(!currentMainPage)
            imgTrash.setImage(new Image(getClass().getResource("/images/trash-icon.png").toExternalForm()));
    }
    @FXML
    private HBox videoContainer;
    @FXML
    private ProgressBar progressBar;


    public void setProgressBar(double value){
        this.progressBar.setProgress(value);
    }

    @FXML
    private void handleHoverIn(){
        if(currentMainPage){
            TranslateTransition tt = new TranslateTransition(Duration.millis(150), videoContainer);
            tt.setToY(-5);
            tt.play();
        }
    }
    @FXML
    private void handleHoverOut(){
        if(currentMainPage){
            TranslateTransition tt = new TranslateTransition(Duration.millis(150), videoContainer);
            tt.setToY(0);
            tt.play();
        }
    }
    @FXML
    private void handleTrashHoverIn(){
        if(!currentMainPage){
            TranslateTransition tt = new TranslateTransition(Duration.millis(150), imgTrash);
            tt.setToY(-5);
            tt.play();
        }
    }
    @FXML
    private void handleTrashHoverOut(){
        if(!currentMainPage){
            TranslateTransition tt = new TranslateTransition(Duration.millis(150), imgTrash);
            tt.setToY(0);
            tt.play();
        }
    }
    @FXML
    private void handleVideoPlayer(){
        if(!currentMainPage)
            return;
        try {
            PeerServer.getPeerIps(videoId, clientSocket);
            ScreenUtils.setStageDisabled(mainStage, true);

            Task<Void> playVideo = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    ProcessBuilder pb = new ProcessBuilder(
                            "npm",
                            "start",
                            "--prefix",
                            "src/main/resources/video_player/electron-media-player",
                            clientSocket.getInetAddress().getHostAddress(),
                            videoId);

                    pb.redirectOutput(new File("src/main/resources/video_player" + "/player_msg.log"));
                    pb.redirectError(new File("src/main/resources/video_player" + "/player_error.log"));

                    Process process = pb.start();
                    int exitCode = process.waitFor();

                    if(exitCode == 0)
                        System.out.println("Player is closed!");
                    else
                        System.err.println("There is problem with starting player");
                    return null;
                }
            };

            playVideo.setOnSucceeded(_ -> {
                ScreenUtils.setStageDisabled(this.mainStage, false);
                try {
                    mainScreenController.refreshMainScreen();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            playVideo.setOnFailed(_ -> {
                ScreenUtils.setStageDisabled(this.mainStage, false);
                try {
                    mainScreenController.refreshMainScreen();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Thread thread = new Thread(playVideo);
            thread.setDaemon(true);
            thread.start();

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    @FXML
    private void handleRemoveVideo() throws Exception {
        boolean response = ScreenUtils.showYesNoDialog("Delete video", null, "Do you want to delete this video?");
        if(response) {
            //todo loading screen and updating Tracker metadata
            FileUtils.deleteDirectoryRecursively(new File("src/main/resources/video_storage/" + videoId));
            SQLiteManager.deleteVideo(videoId);
            parentContainer.getChildren().remove(videoContainer);
            MainScreenService.deletePeersMetadata(videoId, clientSocket);

        }
        else
            System.out.println("No");
    }

}
