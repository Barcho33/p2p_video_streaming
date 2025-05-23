package metadata;

import domain.User;
import domain.Video;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SQLiteManager {

    private static final String URL = "jdbc:sqlite:sqlite_db/metadata.db";

    public static void createTables(){

        String videoTable = """
            CREATE TABLE IF NOT EXISTS video (
                uuid TEXT PRIMARY KEY,
                video_title TEXT NOT NULL,
                upload_date DATETIME NOT NULL,
                uploader VARCHAR NOT NULL,
                size INTEGER NOT NULL,
                segment_num INTEGER NOT NULL
            );
        """;

        String chunkTable = """
        CREATE TABLE IF NOT EXISTS video_chunk (
            uuid TEXT NOT NULL,
            chunk_name VARCHAR NOT NULL,
            PRIMARY KEY (uuid, chunk_name),
            FOREIGN KEY(uuid) REFERENCES video(uuid) ON DELETE CASCADE
        );
    """;
        String userTable = """
            CREATE TABLE IF NOT EXISTS user (
                uuid TEXT PRIMARY KEY,
                username TEXT NOT NULL UNIQUE
            );
        """;

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {

            stmt.execute(videoTable);
            stmt.execute(chunkTable);
            stmt.execute(userTable);

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public static User getUser(){
        String queryUser = "SELECT uuid, username FROM user LIMIT 1";
        String userId = null;
        String username = null;

        try (Connection conn = DriverManager.getConnection(URL);
            Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery(queryUser);

            while(rs.next()){
                 userId = rs.getString("uuid");
                 username = rs.getString("username");
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }


        return new User(userId, username);
    }
    public static String insertUser(String username) {
        if (userExists()) {
            return null;
        }
        String userId = UUID.randomUUID().toString();
        String query = "INSERT INTO user (uuid, username) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, userId);
            pstmt.setString(2, username);
            pstmt.executeUpdate();


            return userId;

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }
    public static boolean userExists() {
        String query = "SELECT * FROM user";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            ResultSet rs = pstmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }
    public static boolean videoExists(String videoId) {
        String query = "SELECT * FROM video WHERE uuid = ?;";

        try (Connection conn = DriverManager.getConnection(URL);
            PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, videoId);

            ResultSet rs = pstmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    public static int insertVideoData(Video video) throws SQLException{
        String query = "INSERT OR IGNORE INTO video VALUES (?, ?, ?, ?, ?, ?)";

        try(Connection conn = DriverManager.getConnection(URL)) {

            PreparedStatement pstm = conn.prepareStatement(query);
            pstm.setString(1, video.getVideoId());
            pstm.setString(2, video.getVideoTitle());
            pstm.setString(3, video.getUploadTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            pstm.setString(4, video.getUploader());
            pstm.setLong(5, video.getSize());
            pstm.setLong(6, video.getSegmentNum());

            return pstm.executeUpdate();
        }

    }

    private static List<String> getSegmentNames(String videoId) throws SQLException {
        String query = "SELECT chunk_name FROM video_chunk WHERE uuid = ?";
        List<String> segments = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, videoId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next())
                    segments.add(rs.getString("chunk_name"));
            }
        } catch (SQLException e) {
            System.err.println("Error reading segments from database: " + e.getMessage());
            throw e;
        }

        return segments;
    }
    public static List<Video> getVideos() throws SQLException {

        String query = "SELECT * FROM video";

        try (Connection conn = DriverManager.getConnection(URL);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query)) {
            List<Video> list_of_videos = new ArrayList<>();

            while (rs.next()) {

                Timestamp timestamp = rs.getTimestamp("upload_date");
                LocalDateTime uploadTime = timestamp.toLocalDateTime();

                Video video = new Video(
                        rs.getString("uuid"),
                        rs.getString("video_title"),
                        uploadTime,
                        rs.getString("uploader"),
                        rs.getLong("size"),
                        rs.getInt("segment_num"));

                list_of_videos.add(video);
            }

            return list_of_videos;
        } catch (SQLException e) {
            System.err.println("Error reading from database in ReadVideos: " + e.getMessage());
            throw e;

        }

    }
    public static boolean insertUploadedVideo(Video video) throws SQLException {


            int affected_row = insertVideoData(video);
            System.out.println("Video row affected: " + affected_row);
            int affectedSegments = insertVideoSegments(video);
            System.out.println("Segment rows affected: " + affectedSegments);

            return affected_row > 0 && affectedSegments == video.getSegmentNum();


    }
    public static ConcurrentHashMap<String, List<String>> getHandshakeData(){
        try {
            List<Video> videoList = getVideos();
            ConcurrentHashMap<String, List<String>> videoData = new ConcurrentHashMap<>();

            for (Video video : videoList){
                List<String> segmentNames = getSegmentNames(video.getVideoId());
                videoData.put(video.getVideoId(), segmentNames);
            }

            return videoData;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static int insertVideoSegments(Video video){
        int segmentNum = video.getSegmentNum();
        int affectedSegments = 0;

        try (Connection conn = DriverManager.getConnection(URL)){

            String query = "INSERT INTO video_chunk VALUES (?,?)";
            PreparedStatement pstm = conn.prepareStatement(query);
            pstm.setString(1, video.getVideoId());

            for (int i = 0; i < segmentNum; ++i){
                String segmentName = "playlist" + i + ".ts";
                pstm.setString(2, segmentName);

                affectedSegments += pstm.executeUpdate();
            }

            return affectedSegments;
        }catch (SQLException ex){
            throw new RuntimeException(ex);
        }
    }

    public static boolean insertSegmentData(String videoId, String segmentName) throws SQLException{

        try (Connection conn = DriverManager.getConnection(URL)){

            String query = "INSERT INTO video_chunk VALUES (?,?)";
            PreparedStatement pstm = conn.prepareStatement(query);
            pstm.setString(1, videoId);
            pstm.setString(2, segmentName);

            return pstm.executeUpdate() > 0;
        }

    }

    public static int getNumOfChunks(String uuid) {
        String query = """
        SELECT COUNT(*) AS chunk_count
        FROM video_chunk
        WHERE uuid = ?;
    """;

        int chunkCount = 0;

        try (Connection con = DriverManager.getConnection(URL);
             PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setString(1, uuid);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    chunkCount = rs.getInt("chunk_count");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching chunk count: " + e.getMessage(), e);
        }

        return chunkCount;
    }


    public static void deleteVideo(String videoId) {
        String query = "DELETE FROM video WHERE uuid = ?;";


        try (Connection con = DriverManager.getConnection(URL)){

            try (Statement pragmaStmt = con.createStatement()) {
                pragmaStmt.execute("PRAGMA foreign_keys = ON");
            }

            try (PreparedStatement stmt = con.prepareStatement(query)) {
                stmt.setString(1, videoId);
                stmt.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
