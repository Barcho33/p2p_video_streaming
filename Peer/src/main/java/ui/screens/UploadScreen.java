package ui.screens;

import domain.Video;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import logic.screen.MainScreenService;
import logic.screen.ScreenUtils;
import metadata.SQLiteManager;

public class UploadScreen {
//    private void openVideoFile() {
//        FileChooser fileChooser = new FileChooser();
//        fileChooser.setTitle("Open Video File");
//
//        fileChooser.getExtensionFilters().addAll(
//                new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.mkv", "*.avi"),
//                new FileChooser.ExtensionFilter("All Files", "*.*")
//        );
//
//
//        selectedFile = fileChooser.showOpenDialog(this.stage);
//        String videoPath;
//        if (selectedFile != null) {
//            lbl_file_name.setText("File name: " + selectedFile.getName());
//            videoPath = selectedFile.getAbsolutePath();
//            lbl_file_path.setText("File path: " + videoPath);
//            long file_size_mb = Math.round((float) selectedFile.length() / (1024*1024));
//            lbl_file_size.setText("File size: " + file_size_mb + "MB");
//            lbl_uploader.setText("Uploader: " + this.user.getUsername());
//
//        } else {
//            setDefaultStringValues();
//        }
//
//    }

    //******************************************************************
//    @FXML
//    private void handleUpload(){
//
//        if(selectedFile == null){
//            ScreenUtils.showDialogMessage("Video file", null, "You must choose video file!", Alert.AlertType.ERROR);
//            return;
//        }
//        if(txt_video_title == null || txt_video_title.getText().isEmpty()){
//            ScreenUtils.showDialogMessage("Video title", null, "You must name your video title!", Alert.AlertType.ERROR);
//            return;
//        }
//
//
//        Video uploadedVideo = MainScreenService.uploadVideo(selectedFile, txt_video_title.getText(), this.user.getUsername(), this.stage);
//        if(uploadedVideo == null){
//            ScreenUtils.showDialogMessage("Upload video", null, "Video is not uploaded successfully!", Alert.AlertType.ERROR);
//            return;
//        }
//        try {
//            if (SQLiteManager.insertUploadedVideo(uploadedVideo) && MainScreenService.sendVideoToTracker(uploadedVideo, this.clientSocket)){
//                ScreenUtils.showDialogMessage("Upload video", null, "Video is successfully uploaded!", Alert.AlertType.INFORMATION);
//            }
//            else
//                ScreenUtils.showDialogMessage("Upload video", null, "Video is not uploaded successfully!", Alert.AlertType.ERROR);
//
//            selectedFile = null;
//            setDefaultStringValues();
//            updateVideoList();//temp
//        }catch (Exception ex){
//            ScreenUtils.showDialogMessage("Upload video", null, "Video is not upload successfully!", Alert.AlertType.ERROR);
//            throw new RuntimeException(ex);
//        }
//    }
//
//    private void setDefaultStringValues() {
//        lbl_file_name.setText("File name:");
//        lbl_file_path.setText("File path:");
//        lbl_file_size.setText("File size:");
//        lbl_uploader.setText("Uploader:");
//        txt_video_title.setText("");
//
//    }
}
