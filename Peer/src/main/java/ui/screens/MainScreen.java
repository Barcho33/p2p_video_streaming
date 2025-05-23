package ui.screens;

import com.sun.net.httpserver.HttpServer;
import domain.Thumbnail;
import domain.Video;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import domain.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import logic.screen.MainScreenService;
import logic.screen.ScreenUtils;
import logic.video.FFmpegUtils;
import metadata.SQLiteManager;
import peer_server.PeerServer;
import peer_server.PeerTrackerCommunication;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;

public class MainScreen {

    private Stage stage;
    private Socket clientSocket;
    private User user;

    private HttpServer peerServer;
    private static List<Video> listOfVideos;
    private static List<Thumbnail> listOfThumbnails;
    private boolean currentMainPage;

    @FXML
    private Button btnSearch;
    @FXML
    private TextField txtSearch;
    @FXML
    private VBox videoBox;
    @FXML
    private Label lblHome;
    @FXML
    private Label lblMyVideos;
    @FXML
    private Label lblUploadVideo;
    @FXML
    private ImageView imgLogo;
    @FXML
    private Label lblVideoLibrary;
    @FXML
    private HBox horizontalMenu;

    public void initialize(Stage stage, Socket clientSocket, User user) {
        try {
            this.stage = stage;
            this.clientSocket = clientSocket;
            this.user = user;
            this.peerServer = PeerServer.startServer(clientSocket);
            this.imgLogo.setImage(new Image(getClass().getResource("/images/transparent-logo.png").toExternalForm()));
            PeerTrackerCommunication.getInstance().initSocket(clientSocket);
            stage.setOnCloseRequest(windowEvent -> {
                PeerServer.stopServer(peerServer);
                System.exit(0);
            });
            showHomePage();


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void handleSearch() throws Exception {
        this.videoBox.getChildren().clear();

        listOfVideos = MainScreenService.getAllVideos(this.clientSocket);
        listOfThumbnails = MainScreenService.getAllThumbnails(this.clientSocket);
        for(Video video : listOfVideos){
            if(video.getVideoTitle() != null && video.getVideoTitle().toLowerCase().contains(txtSearch.getText().toLowerCase()))
                videoBox.getChildren().add(createVideoContainer(video));
        }
    }
    public void refreshMainScreen() throws Exception {
        showHomePage();
    }
    @FXML
    private void homeHoverIn(){
        TranslateTransition tt = new TranslateTransition(Duration.millis(150), lblHome);
        tt.setToY(-5);
        tt.play();

    }
    @FXML
    private void homeHoverOut(){
        TranslateTransition tt = new TranslateTransition(Duration.millis(150), lblHome);
        tt.setToY(0);
        tt.play();
    }
    @FXML
    private void myVideosHoverIn(){
        TranslateTransition tt = new TranslateTransition(Duration.millis(150), lblMyVideos);
        tt.setToY(-5);
        tt.play();
    }
    @FXML
    private void myVideosHoverOut(){
        TranslateTransition tt = new TranslateTransition(Duration.millis(150), lblMyVideos);
        tt.setToY(0);
        tt.play();
    }
    @FXML
    private void uploadHoverIn(){
        TranslateTransition tt = new TranslateTransition(Duration.millis(150), lblUploadVideo);
        tt.setToY(-5);
        tt.play();
    }
    @FXML
    private void uploadHoverOut(){
        TranslateTransition tt = new TranslateTransition(Duration.millis(150), lblUploadVideo);
        tt.setToY(0);
        tt.play();
    }
    @FXML
    private void searchHoverIn(){
        String currentStyle = btnSearch.getStyle();
        String updatedStyle = currentStyle.replaceAll(  "-fx-background-color:.*?;",
                "-fx-background-color: linear-gradient(to right, #d17fb2, #9254c9);");
        btnSearch.setStyle(updatedStyle);
    }
    @FXML
    private void searchHoverOut(){
        String currentStyle = btnSearch.getStyle();
        String updatedStyle = currentStyle.replaceAll(  "-fx-background-color:.*?;",
                "-fx-background-color:  linear-gradient(to right, #a4508b, #5f0a87);");
        btnSearch.setStyle(updatedStyle);
    }
    @FXML
    private void handleMyVideos() throws Exception {
        showMyVideosPage();
    }
    @FXML
    private void handleHome() throws Exception {
        showHomePage();
    }
    @FXML
    private void handleUpload() throws IOException {
        showUploadScreen(user, clientSocket);
    }
    private void updateVideoList() throws Exception {

        listOfVideos = MainScreenService.getAllVideos(this.clientSocket);
        listOfThumbnails = MainScreenService.getAllThumbnails(this.clientSocket);
        for(Video video : listOfVideos){
            if(video.getVideoTitle() != null)
                videoBox.getChildren().add(createVideoContainer(video));
        }

    }
    private HBox createVideoContainer(Video video) throws Exception {
        FXMLLoader loader = new FXMLLoader(VideoContainer.class.getResource("/fxml_files/video_container.fxml"));

        HBox root = loader.load();
        VideoContainer vc = loader.getController();
        vc.initialize(this.clientSocket, this.stage, videoBox, video.getVideoId(), this, currentMainPage);
        int downloadedSegments = SQLiteManager.getNumOfChunks(video.getVideoId());
        double progress = (double) downloadedSegments / video.getSegmentNum();
        vc.setProgressBar(progress);
        Label lblTitle = (Label) root.lookup("#videoTitle");

        if(lblTitle != null)
            lblTitle.setText(video.getVideoTitle());
        else
            System.err.println("Label was not found");
        ImageView seedersIcon = (ImageView) root.lookup("#seedersIcon");
        Label lblSeeders = (Label) root.lookup("#lblSeeders");
        if(seedersIcon != null && lblSeeders != null){
            seedersIcon.setImage(new Image(getClass().getResource("/images/seeders-icon.png").toExternalForm()));
            int numOfPeers = MainScreenService.numberOfOnlinePeers(video.getVideoId(), clientSocket);
            lblSeeders.setText(numOfPeers + " peers with video segments were recently online.");
        }
        ImageView videoImage = (ImageView) root.lookup("#videoImage");
        if(videoImage != null && listOfThumbnails != null) {
            if(!listOfThumbnails.isEmpty())
                for(Thumbnail thumbnail : listOfThumbnails)
                    if(thumbnail.getVideoId().equals(video.getVideoId()))
                        videoImage.setImage(new Image( new ByteArrayInputStream(thumbnail.getImageData())));
        }

        return root;
    }

    private void showHomePage() throws Exception {
        lblMyVideos.setStyle("-fx-text-fill: #ffffff;");
        lblUploadVideo.setStyle("-fx-text-fill: #ffffff;");
        lblHome.setStyle("-fx-text-fill: #FF6910;");
        currentMainPage = true;

        if(horizontalMenu.getChildren().contains(lblVideoLibrary))
            this.horizontalMenu.getChildren().remove(this.lblVideoLibrary);
        if(!horizontalMenu.getChildren().contains(txtSearch) && !horizontalMenu.getChildren().contains(btnSearch)){
            this.horizontalMenu.getChildren().add(this.txtSearch);
            this.horizontalMenu.getChildren().add(this.btnSearch);
        }
        this.videoBox.getChildren().clear();
        updateVideoList();

    }
    private void showMyVideosPage() throws Exception {
        lblHome.setStyle("-fx-text-fill: #ffffff;");
        lblUploadVideo.setStyle("-fx-text-fill: #ffffff;");
        lblMyVideos.setStyle("-fx-text-fill: #FF6910;");
        currentMainPage = false;
        this.videoBox.getChildren().clear();

        if(!horizontalMenu.getChildren().contains(lblVideoLibrary))
            this.horizontalMenu.getChildren().add(this.lblVideoLibrary);
        if(horizontalMenu.getChildren().contains(txtSearch) && horizontalMenu.getChildren().contains(btnSearch)){
            this.horizontalMenu.getChildren().remove(this.txtSearch);
            this.horizontalMenu.getChildren().remove(this.btnSearch);
        }

        List<Video> myVideos = SQLiteManager.getVideos();

        for(Video video : listOfVideos){
            for (Video myVideo : myVideos){
                if(myVideo.getVideoId().equals(video.getVideoId()))
                    this.videoBox.getChildren().add(createVideoContainer(video));

            }
        }

    }
    private void showUploadScreen(User user, Socket clientSocket) throws IOException {
        lblHome.setStyle("-fx-text-fill: #ffffff;");
        lblMyVideos.setStyle("-fx-text-fill: #ffffff;");
        lblUploadVideo.setStyle("-fx-text-fill: #FF6910;");

        ScreenUtils.setStageDisabled(this.stage, true);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml_files/uploading_screen.fxml"));
        Parent root = loader.load();
        UploadScreen uploadScreen = loader.getController();

        Stage uploadStage = new Stage();
        uploadStage.setTitle("Upload");
        uploadStage.setScene(new Scene(root));
        uploadStage.setOnHiding(_ -> {
            ScreenUtils.setStageDisabled(this.stage, false);
            try {
                showHomePage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        uploadScreen.initialize(user, clientSocket, uploadStage);
        uploadStage.setResizable(false);
        uploadStage.show();




    }

}
