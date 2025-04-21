package ui.screens;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class VideoContainer {

    public HBox getContainer(String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(VideoContainer.class.getResource("/fxml_files/video_container.fxml"));

        HBox root = loader.load();
        Label lbl = (Label) root.lookup("#videoTitle");
        if(lbl != null)
            lbl.setText(title);
        else
            System.err.println("Label was not found");

        return root;
    }
}
