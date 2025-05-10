package ui.screens;

import domain.Thumbnail;
import domain.User;
import domain.Video;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import logic.screen.MainScreenService;
import logic.screen.ScreenUtils;
import logic.screen.UploadScreenService;
import logic.video.FFmpegUtils;
import metadata.SQLiteManager;

import java.io.File;
import java.net.Socket;

public class UploadScreen {
    private Socket clientSocket;
    private Stage uploadStage;
    private User user;
    private File selectedFile;
    private File selectedImage;

    @FXML
    private Button btnChoose;
    @FXML
    private Button btnUpload;
    @FXML
    private Button btnExit;
    @FXML
    private TextField txtVideoTitle;
    @FXML
    private Label lblName;
    @FXML
    private Label lblPath;
    @FXML
    private Label lblSize;
    @FXML
    private Label lblUploader;
    @FXML
    private ImageView imgImport;

    public void initialize(User user, Socket clientSocket, Stage uploadStage){
        this.user = user;
        this.clientSocket = clientSocket;
        this.uploadStage = uploadStage;
        this.imgImport.setImage(new Image(getClass().getResource("/images/import-img.png").toExternalForm()));
        setDefaultLabelsValues();
    }

    @FXML
    private void handleChoose(){
        openVideoFile();
    }
    @FXML
    private void handleExit(){
        this.uploadStage.close();
    }
    @FXML
    private void handleThumbnail(){
        openImageFile();
    }
    @FXML
    private void handleUpload(){

        if(selectedFile == null){
            ScreenUtils.showDialogMessage("Video file", null, "You must choose video file!", Alert.AlertType.ERROR);
            return;
        }
        if(txtVideoTitle == null || txtVideoTitle.getText().isEmpty()){
            ScreenUtils.showDialogMessage("Video title", null, "You must name your video title!", Alert.AlertType.ERROR);
            return;
        }


        Video uploadedVideo = UploadScreenService.uploadVideo(selectedFile, txtVideoTitle.getText(), this.user.getUsername(), this.uploadStage);
        if(uploadedVideo == null){
            ScreenUtils.showDialogMessage("Upload video", null, "Video is not uploaded successfully!", Alert.AlertType.ERROR);
            return;
        }

        try {
            byte[] imageData;
            if(selectedImage == null)
                imageData = FFmpegUtils.extractThumbnail(selectedFile.getAbsolutePath());
            else
                imageData = FFmpegUtils.resizeImageToBytes(selectedImage.getAbsolutePath(), 477, 381);

            if (    SQLiteManager.insertUploadedVideo(uploadedVideo)
                    && UploadScreenService.sendVideoToTracker(uploadedVideo, this.clientSocket)
                    && UploadScreenService.sendThumbnailToTracker(new Thumbnail(uploadedVideo.getVideoId(), imageData), this.clientSocket)){

                ScreenUtils.showDialogMessage("Upload video", null, "Video is successfully uploaded!", Alert.AlertType.INFORMATION);
            }
            else
                ScreenUtils.showDialogMessage("Upload video", null, "Video is not uploaded successfully!", Alert.AlertType.ERROR);

            selectedFile = null;
            setDefaultLabelsValues();

        }catch (Exception ex){
            ScreenUtils.showDialogMessage("Upload video", null, "Video is not upload successfully!", Alert.AlertType.ERROR);
            throw new RuntimeException(ex);
        }
    }
    private void openImageFile(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Image File");

        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png", "*.bmp", "*.gif", "*.tiff", "*.webp")
        );

        selectedImage = fileChooser.showOpenDialog(this.uploadStage);
        if(selectedImage == null){
            this.imgImport.setImage(new Image(getClass().getResource("/images/import-img.png").toExternalForm()));
            return;
        }
        this.imgImport.setImage(new Image(selectedImage.toURI().toString()));
    }
    private void openVideoFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Video File");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.mkv", "*.avi"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );


        selectedFile = fileChooser.showOpenDialog(this.uploadStage);
        String videoPath;
        if (selectedFile != null) {
            lblName.setText(selectedFile.getName());
            videoPath = selectedFile.getAbsolutePath();
            lblPath.setText(videoPath);
            long file_size_mb = Math.round((float) selectedFile.length() / (1024*1024));
            lblSize.setText(file_size_mb + "MB");
            lblUploader.setText(this.user.getUsername());

        } else {
            setDefaultLabelsValues();
        }

    }

    private void setDefaultLabelsValues() {
        lblName.setText("");
        lblPath.setText("");
        lblSize.setText("");
        lblUploader.setText("");
        txtVideoTitle.setText("");

    }
}
