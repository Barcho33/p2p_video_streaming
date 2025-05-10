package domain;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;


public class Video
implements Serializable{
@Serial
private static final long serialVersionUID = 1L;

    private String videoId;
    private String videoTitle;
    private LocalDateTime uploadTime;
    private String uploader;
    private long size;
    private int segmentNum;


    public Video() {
    }

    public Video(String videoId, String videoTitle, LocalDateTime uploadTime, String uploader, long size, int segmentNum) {
        this.videoId = videoId;
        this.videoTitle = videoTitle;
        this.uploadTime = uploadTime;
        this.uploader = uploader;
        this.size = size;
        this.segmentNum = segmentNum;
    }

    public int getSegmentNum() {
        return segmentNum;
    }

    public void setSegmentNum(int segmentNum) {
        this.segmentNum = segmentNum;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getVideoTitle() {
        return videoTitle;
    }

    public void setVideoTitle(String videoTitle) {
        this.videoTitle = videoTitle;
    }

    public LocalDateTime getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }

    public String getUploader() {
        return uploader;
    }

    public void setUploader(String uploader) {
        this.uploader = uploader;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return videoTitle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VideoChunk that)) return false;
        return videoId.equals(that.getVideoId());
    }


}
