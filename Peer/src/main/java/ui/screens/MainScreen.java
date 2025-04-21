package ui.screens;

import com.sun.net.httpserver.HttpServer;
import domain.Video;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import domain.User;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import logic.screen.MainScreenService;
import logic.screen.ScreenUtils;
import metadata.SQLiteManager;
import peer_server.PeerServer;
import peer_server.PeerTrackerCommunication;


import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MainScreen {

    private Stage stage;
    private Socket clientSocket;
    private User user;

    private File selectedFile;
    private HttpServer peerServer;
    private static List<Video> listOfVideos;

    public void initialize(Stage stage, Socket clientSocket, User user) {
        try {
            this.stage = stage;
            this.clientSocket = clientSocket;
            this.user = user;
            this.peerServer = PeerServer.startServer(clientSocket);
            PeerTrackerCommunication.getInstance().initSocket(clientSocket);
            stage.setOnCloseRequest(windowEvent -> {
                PeerServer.stopServer(peerServer);
                System.exit(0);
            });
            stage.setResizable(false);
            updateVideoList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @FXML private Label lbl_file_name;
    @FXML private Label lbl_uploader;
    @FXML private Label lbl_file_path;
    @FXML private Label lbl_file_size;
    @FXML private TextField txt_video_title;
    @FXML private ListView<Video> videoList;
    @FXML private TextField txtSearch;
    @FXML private VBox vbVideoList;

    @FXML
    private void handlePlay() throws SQLException {

      Video video = videoList.getSelectionModel().getSelectedItem();

      if(video == null){
          ScreenUtils.showDialogMessage("Video", null, "You must select video!", Alert.AlertType.ERROR);
          return;
      }

      openVideoPlayer(clientSocket.getInetAddress().getHostName(), video.getVideoId());
      SQLiteManager.insertVideoData(video);
    }
    @FXML
    private void handleChooseFile(){
        openVideoFile();
    }
    @FXML
    private void searchForVideo(){
        if(txtSearch.getText().isEmpty()){
            ObservableList<Video> videos = FXCollections.observableArrayList(listOfVideos);
            videoList.setItems(videos);
            return;
        }
        List<Video> vl = new ArrayList<>();
        for(Video video : listOfVideos) {
            if (video.getVideoTitle().toLowerCase()
                    .contains(txtSearch.getText().toLowerCase()))
                vl.add(video);
            }
            ObservableList<Video> videos = FXCollections.observableArrayList(vl);
            videoList.setItems(videos);
        }

    @FXML
    private void handleUpload(){

        if(selectedFile == null){
            ScreenUtils.showDialogMessage("Video file", null, "You must choose video file!", Alert.AlertType.ERROR);
            return;
        }
        if(txt_video_title == null || txt_video_title.getText().isEmpty()){
            ScreenUtils.showDialogMessage("Video title", null, "You must name your video title!", Alert.AlertType.ERROR);
            return;
        }


        Video uploadedVideo = MainScreenService.uploadVideo(selectedFile, txt_video_title.getText(), this.user.getUsername(), this.stage);
        if(uploadedVideo == null){
            ScreenUtils.showDialogMessage("Upload video", null, "Video is not uploaded successfully!", Alert.AlertType.ERROR);
            return;
        }
        try {
            if (SQLiteManager.insertUploadedVideo(uploadedVideo) && MainScreenService.sendVideoToTracker(uploadedVideo, this.clientSocket)){
                ScreenUtils.showDialogMessage("Upload video", null, "Video is successfully uploaded!", Alert.AlertType.INFORMATION);
            }
            else
                ScreenUtils.showDialogMessage("Upload video", null, "Video is not uploaded successfully!", Alert.AlertType.ERROR);

            selectedFile = null;
            setDefaultStringValues();
            updateVideoList();//temp
        }catch (Exception ex){
            ScreenUtils.showDialogMessage("Upload video", null, "Video is not upload successfully!", Alert.AlertType.ERROR);
            throw new RuntimeException(ex);
        }
    }

    private void setDefaultStringValues() {
        lbl_file_name.setText("File name:");
        lbl_file_path.setText("File path:");
        lbl_file_size.setText("File size:");
        lbl_uploader.setText("Uploader:");
        txt_video_title.setText("");

    }


    private void openVideoFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Video File");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.mkv", "*.avi"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );


        selectedFile = fileChooser.showOpenDialog(this.stage);
        String videoPath;
        if (selectedFile != null) {
            lbl_file_name.setText("File name: " + selectedFile.getName());
            videoPath = selectedFile.getAbsolutePath();
            lbl_file_path.setText("File path: " + videoPath);
            long file_size_mb = Math.round((float) selectedFile.length() / (1024*1024));
            lbl_file_size.setText("File size: " + file_size_mb + "MB");
            lbl_uploader.setText("Uploader: " + this.user.getUsername());

        } else {
            setDefaultStringValues();
        }

    }

    private void updateVideoList() throws Exception {

        listOfVideos = MainScreenService.getAllVideos(this.clientSocket);
        //ObservableList<Video> videos = FXCollections.observableArrayList(listOfVideos);

        //videoList.setItems(videos);

        VideoContainer vc = new VideoContainer();
        for(Video video : listOfVideos){
            System.out.println(video.getVideoTitle());
            if(video.getVideoTitle() != null)
                vbVideoList.getChildren().add(vc.getContainer(video.getVideoTitle()));
        }

    }

    private void openVideoPlayer(String hostname, String videoId){
            try {
                PeerServer.getPeerIps(videoId, clientSocket);
                ScreenUtils.setStageDisabled(stage, true);

                Task<Void> playVideo = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        ProcessBuilder pb = new ProcessBuilder(
                                "npm",
                                "start",
                                "--prefix",
                                "src/main/resources/video_player/electron-media-player",
                                "192.168.1.101",
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

                playVideo.setOnSucceeded(_ -> ScreenUtils.setStageDisabled(stage, false));
                playVideo.setOnFailed(_ -> ScreenUtils.setStageDisabled(stage, false));

                Thread thread = new Thread(playVideo);
                thread.setDaemon(true);
                thread.start();

            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }

    }
}
