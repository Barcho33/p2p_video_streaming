package logic.video;

import java.io.*;

public class FFmpegUtils{

    private static final String VIDEO_STORAGE = "src/main/resources/video_storage/";
    private static final String TEMP_DIR = "src/main/resources/temp_files/";

    public static void formatVideo(File videoFile, String uuid) throws IOException, InterruptedException {

        ProcessBuilder pb = new ProcessBuilder();
        File storage = new File(VIDEO_STORAGE + uuid);

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
    public static byte[] extractThumbnail(String videoPath) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg", "-i", videoPath,
                "-vf", "scale=477:318",
                "-vframes", "1",
                TEMP_DIR + "thumbnail.png"
        );
        pb.redirectErrorStream(true);
        Process proc = pb.start();
        proc.waitFor();

      File image = new File(TEMP_DIR + "thumbnail.png");
      byte[] imageData = new byte[(int) image.length()];
      if(image.exists()){
          try(FileInputStream fis = new FileInputStream(image)){
              fis.read(imageData);
              if(image.delete())
                  System.out.println("Temp file successfully removed");


          }

      }else
        System.out.println("Temp thumbnail file doesn't exist");

      return imageData;

    }

    public static byte[] resizeImageToBytes(String inputImagePath, int width, int height) throws IOException, InterruptedException {
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-i", inputImagePath,
                    "-vf", "scale=" + width + ":" + height,
                    TEMP_DIR + "thumbnail.png"
            );

            pb.redirectErrorStream(true);
            Process proc = pb.start();
            proc.waitFor();

            File image = new File(TEMP_DIR + "thumbnail.png");
            byte[] imageData = new byte[(int) image.length()];
            if(image.exists()){
                try(FileInputStream fis = new FileInputStream(image)){
                    fis.read(imageData);
                    if(image.delete())
                        System.out.println("Temp file successfully removed");


                }

            }else
                System.out.println("Temp thumbnail file doesn't exist");

            return imageData;
        }

}
