package domain;

import java.io.Serial;
import java.io.Serializable;

public class Thumbnail implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String videoId;
    private byte[] imageData;

    public Thumbnail() {
    }

    public Thumbnail(String videoId, byte[] imageData) {
        this.videoId = videoId;
        this.imageData = imageData;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }
}
