package database;

import domain.Thumbnail;
import domain.Video;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DatabaseBroker {

    private static final String URL = "jdbc:mysql://localhost:3306/peer_to_peer";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public boolean saveUser(String videoId, String username){
        String query = "INSERT INTO user (uuid, username) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement preparedStatement = conn.prepareStatement(query)){

                preparedStatement.setString(1, videoId);
                preparedStatement.setString(2, username);

                return preparedStatement.executeUpdate() > 0;

        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        return false;
    }

    public boolean existsUserByUsername(String username) {
        String query = "SELECT *  FROM user WHERE username = ?;";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement preparedStatement = conn.prepareStatement(query)) {

                preparedStatement.setString(1, username);
                ResultSet resultSet = preparedStatement.executeQuery();

                return resultSet.next();

        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }

        return false;

    }


    public boolean saveVideo(Video video) {
        String query = "INSERT INTO video VALUES (?, ?, ?, ?, ?, ?)";

        try(Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            PreparedStatement preparedStatement = conn.prepareStatement(query)){

                preparedStatement.setString(1, video.getVideoId());
                preparedStatement.setString(2, video.getVideoTitle());
                preparedStatement.setString(3, video.getUploadTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                preparedStatement.setString(4, video.getUploader());
                preparedStatement.setLong(5, video.getSize());
                preparedStatement.setInt(6, video.getSegmentNum());

                return preparedStatement.executeUpdate() > 0;

        }catch (Exception ex){
            System.err.println(ex.getMessage());
        }

        return false;
    }
    public boolean saveThumbnail(String videoId, byte[] imageData) {
        String query = "INSERT INTO thumbnail VALUES (?, ?)";

        try(Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            PreparedStatement preparedStatement = conn.prepareStatement(query)){

            preparedStatement.setString(1, videoId);
            preparedStatement.setBytes(2, imageData);

            return preparedStatement.executeUpdate() > 0;

        }catch (Exception ex){
            System.err.println(ex.getMessage());
        }

        return false;
    }

    public List<Video> getAllVideos() {
        String query = "SELECT * FROM video";

        try(Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            Statement stmt = conn.createStatement()){

                ResultSet rs = stmt.executeQuery(query);
                List<Video> videoList = new ArrayList<>();

                while (rs.next()){
                    Video video = new Video(
                            rs.getString("uuid"),
                            rs.getString("video_title"),
                            rs.getTimestamp("upload_date").toLocalDateTime(),
                            rs.getString("uploader"),
                            rs.getLong("size"),
                            rs.getInt("segment_num")
                    );

                    videoList.add(video);
                }

                return videoList;

        }catch (SQLException ex){
            System.err.println(ex.getMessage());
        }

        return null;
    }

    public List<Thumbnail> getAllThumbnails() {
        String query = "SELECT * FROM thumbnail";

        try(Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            Statement stmt = conn.createStatement()){

            ResultSet rs = stmt.executeQuery(query);
            List<Thumbnail> thumbnailsList = new ArrayList<>();

            while (rs.next()){
                Thumbnail thumbnail = new Thumbnail(
                        rs.getString("videoId"),
                        rs.getBytes("image")
                );

                thumbnailsList.add(thumbnail);
            }

            return thumbnailsList;

        }catch (SQLException ex){
            System.err.println(ex.getMessage());
        }

        return null;
    }

    public Video getVideo(String videoId) {
        String query = "SELECT * FROM video WHERE uuid = ?";

        try(Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            PreparedStatement pstmt = conn.prepareStatement(query)){
            pstmt.setString(1, videoId);

            ResultSet rs = pstmt.executeQuery();

            if(rs.next()){
                Video video = new Video(
                        rs.getString("uuid"),
                        rs.getString("video_title"),
                        rs.getTimestamp("upload_date").toLocalDateTime(),
                        rs.getString("uploader"),
                        rs.getLong("size"),
                        rs.getInt("segment_num")
                );

                return video;
            }

        }catch (SQLException ex){
            System.err.println(ex.getMessage());
        }

        return null;
    }
}
