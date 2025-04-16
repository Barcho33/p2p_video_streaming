package domain;

import java.io.Serial;
import java.io.Serializable;

public class VideoChunk
implements Serializable {
@Serial
private static final long serialVersionUID = 1L;

    private String videoId;
    private String chunk_name;

    public VideoChunk() {
    }

    public VideoChunk(String videoId, String chunk_name) {
        this.videoId = videoId;
        this.chunk_name = chunk_name;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getChunk_name() {
        return chunk_name;
    }

    public void setChunk_name(String chunk_name) {
        this.chunk_name = chunk_name;
    }
}
