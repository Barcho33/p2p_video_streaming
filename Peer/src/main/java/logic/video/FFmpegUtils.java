package logic.video;

import java.io.File;
import java.io.IOException;

public class FFmpegUtils{

    private static final String VIDEO_STORAGE = "src/main/resources/video_storage";

    public static void formatVideo(File videoFile, String uuid) throws IOException, InterruptedException {

        ProcessBuilder pb = new ProcessBuilder();
        File storage = new File(VIDEO_STORAGE + "/" + uuid);

        if(!storage.exists())
            if(!storage.mkdirs()){
                System.out.println("Failing to create directory on path: " + storage.getAbsolutePath());
                return;
            }

        pb.redirectOutput(new File(storage.getAbsolutePath() + "/ffmpeg_output.log"));
        pb.redirectError(new File(storage.getAbsolutePath() + "/ffmpeg_error.log"));
        pb.command("ffmpeg",
                "-i", videoFile.getAbsolutePath(),
                "-map", "0:v", "-map", "0:a", "-codec:v", "copy", "-codec:a", "aac", "-b:a", "192k", "-hls_time", "30", "-hls_playlist_type", "vod", "-f", "hls",
                storage.getAbsolutePath() + "/playlist.m3u8");

        Process proc = pb.start();
        proc.waitFor();
    }


}
