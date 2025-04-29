package ui.screens;

import com.sun.net.httpserver.HttpServer;
import domain.Video;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import domain.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import logic.screen.MainScreenService;
import logic.screen.ScreenUtils;
import org.w3c.dom.Node;
import org.w3c.dom.events.MouseEvent;
import peer_server.PeerServer;
import peer_server.PeerTrackerCommunication;


import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;

public class MainScreen {

    private Stage stage;
    private Socket clientSocket;
    private User user;

    private File selectedFile;
    private HttpServer peerServer;
    private static List<Video> listOfVideos;
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

            updateVideoList();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void updateVideoList() throws Exception {

        listOfVideos = MainScreenService.getAllVideos(this.clientSocket);

        for(Video video : listOfVideos){
            if(video.getVideoTitle() != null)
                videoBox.getChildren().add(createVideoContainer(video.getVideoId(), video.getVideoTitle()));
        }

    }
    private HBox createVideoContainer(String videoId, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(VideoContainer.class.getResource("/fxml_files/video_container.fxml"));

        HBox root = loader.load();
        VideoContainer vc = loader.getController();
        vc.initialize(this.clientSocket, this.stage, videoId);

        Label lblTitle = (Label) root.lookup("#videoTitle");

        if(lblTitle != null)
            lblTitle.setText(title);
        else
            System.err.println("Label was not found");


        return root;
    }


}
