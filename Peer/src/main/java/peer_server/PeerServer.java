package peer_server;

import Communication.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class PeerServer {

        private static final String BASED_PATH = "src/main/resources";
        private static final String VIDEO_PLAYER = "src/main/resources/video_player/player.html";
        private static final int PORT = 8000;
        private static Socket client;
        private static ConcurrentHashMap<String, List<String>> peerIps = new ConcurrentHashMap<>();
        private static final InetSocketAddress hostAddress = new InetSocketAddress("192.168.1.101", PORT); //temp


    public static HttpServer startServer(Socket socket){
            client = socket;
            try {
                System.out.println("Starting server...");
                HttpServer server = HttpServer.create(hostAddress, 0); //temp
                server.createContext("/video", new VideoHandler());
                server.setExecutor(Executors.newFixedThreadPool(10));
                server.start();
                System.out.println("Peer server is running on: " + hostAddress.getHostName() + ":" + PORT);


                return server;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
        public static void stopServer(HttpServer server){
            server.stop(0);
            System.out.println("Peer server is stopped.");
        }

        static class VideoHandler implements HttpHandler{


            @Override
            public void handle(HttpExchange exchange) throws IOException {

                String path = exchange.getRequestURI().getPath();
                String query = exchange.getRequestURI().getQuery();
                String videoId = extractVideoId(query);

                if (videoId == null) {
                    exchange.sendResponseHeaders(400, -1);
                    return;
                }

                if (path.endsWith("playlist.m3u8")) {
                    handlePlaylist(exchange, videoId);
                } else if (path.contains(".ts")) {
                    try {
                        handleSegment(exchange, videoId, path);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    exchange.sendResponseHeaders(404, -1);
                }
            }

            private void handlePlaylist(HttpExchange exchange, String videoId) throws  IOException{
                Path playlistPath = Paths.get(BASED_PATH + "/video_storage", videoId, "playlist.m3u8");

                if(!Files.exists(playlistPath)){
                    System.out.println("GETTING PLAYLIST FORM PEER!");
                    fetchMetadataFromPeer(exchange, playlistPath, videoId);
                    return;
                }

                String content = new String(Files.readAllBytes(playlistPath));
                content = formatPlaylist(content, videoId);


                exchange.getResponseHeaders().set("Content-Type", "application/vnd.apple.mpegurl");
                exchange.getResponseHeaders().set("Cache-Control", "no-cache");
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");


                byte[] responseBytes = content.getBytes(StandardCharsets.UTF_8);
                Files.write(playlistPath, responseBytes);
                exchange.sendResponseHeaders(200, responseBytes.length);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }

            }

            private void handleSegment(HttpExchange exchange, String videoId, String path) throws IOException, SQLException {
                String segmentName = path.substring(path.lastIndexOf("/") + 1).split("\\?")[0];
                Path segmentPath = Paths.get(BASED_PATH + "/video_storage", videoId, segmentName);

                if(!Files.exists(segmentPath)){
                    System.out.println("GETTING SEGMENT FROM PEER!");
                    fetchSegmentFromPeer(exchange, videoId, segmentName, segmentPath);
                    return;
                }

                sendSegment(exchange, segmentPath);
            }
            private void fetchMetadataFromPeer(HttpExchange exchange, Path playlistLocalPath, String videoId) throws IOException {
                String peerUrl = getMetadataFromPeer() + "/video/playlist.m3u8?videoId=" + videoId;
                System.out.println(peerUrl);
                HttpURLConnection conn = (HttpURLConnection) new URL(peerUrl).openConnection();

                if (conn.getResponseCode() != 200) {
                    System.out.println("NO RESPONSE!");
                    exchange.sendResponseHeaders(404, -1);
                    return;
                }

                Files.createDirectories(playlistLocalPath.getParent());

                String content;
                try (InputStream in = conn.getInputStream()) {
                    content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                }

                content = formatPlaylist(content, videoId);
                byte[] responseBytes = content.getBytes(StandardCharsets.UTF_8);

                exchange.getResponseHeaders().set("Content-Type", "application/vnd.apple.mpegurl");
                exchange.getResponseHeaders().set("Cache-Control", "no-cache");
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(200, responseBytes.length);

                try (OutputStream out = exchange.getResponseBody()) {
                    out.write(responseBytes);
                }

                Files.write(playlistLocalPath, responseBytes);
            }

            private String getMetadataFromPeer() {

                List<String> segments = new ArrayList<>(peerIps.keySet());

                int numOfSegments = segments.size();

                Random rand = new Random();

                String segment = segments.get(rand.nextInt(numOfSegments));
                return getPeerIpWithSegment(segment);

            }

            private String getPeerIpWithSegment(String segment) {

                List<String> ips = peerIps.get(segment);
                int numOfIps = ips.size();

                Random rand = new Random();

                while(!ips.isEmpty()) {

                    String ip = ips.get(rand.nextInt(numOfIps));
                    if(!ip.equals(hostAddress.getHostName())){
                        return "http://" + ip + ":8000";
                    }
                    ips.remove(ip);
                    --numOfIps;
                }

                return "";
            }

            private void sendSegment(HttpExchange exchange, Path segmentPath) throws IOException {
                byte[] segmentData = Files.readAllBytes(segmentPath);
                exchange.getResponseHeaders().set("Content-Type", "video/MP2T");
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");

                exchange.sendResponseHeaders(200, segmentData.length);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(segmentData);
                }
            }

            private String extractVideoId(String query){

                if(query == null) return null;
                for(String param : query.split("&")){
                    String[] pair = param.split("=");
                    if(pair.length == 2 && pair[0].equals("videoId"))
                        return pair[1];
                }
                return null;
            }

            private String formatPlaylist(String content, String videoId){
                StringBuilder formatted = new StringBuilder();
                int segmetnId = 0;

                for(String line : content.split("\n")){
                    if(line.contains(".ts")){
                        formatted.append("http://").append(hostAddress.getAddress().getHostName()).append(":").append(PORT).append("/video/")//temp
                                .append("playlist").append(segmetnId++).append(".ts")
                                .append("?videoId=").append(videoId)
                                .append("\n");

                    }else
                        formatted.append(line).append("\n");
                }
                return formatted.toString();
            }

            private void fetchSegmentFromPeer(HttpExchange exchange, String videoId, String segmentName, Path localPath) throws IOException {
                String peerUrl = getPeerIpWithSegment(segmentName) + "/video/" + segmentName + "?videoId=" + videoId;
                HttpURLConnection conn = (HttpURLConnection) new URL(peerUrl).openConnection();

                if(conn.getResponseCode() != 200){
                    //TODO traziti koji peer ima
                    System.out.println("SEGMENT 404");
                    exchange.sendResponseHeaders(404, -1);
                    return;
                }

                Files.createDirectories(localPath.getParent());
                try(InputStream in = conn.getInputStream();
                    OutputStream out = exchange.getResponseBody();
                    FileOutputStream fos = new FileOutputStream(localPath.toFile())){

                    exchange.getResponseHeaders().set("Content-Type", "video/MP2T");
                    exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                    exchange.sendResponseHeaders(200, 0);

                    byte[] buffer = new byte[8192];
                    int byteRead;
                    while((byteRead = in.read(buffer)) != -1){
                        out.write(buffer, 0, byteRead);
                        fos.write(buffer, 0, byteRead);
                    }

                    new Thread(
                            () -> {
                                try {
                                    PeerTrackerCommunication.getInstance().updateMetaData(videoId, segmentName, localPath);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    ).start();

                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    public static void getPeerIps(String videoId, Socket client) throws Exception {
        Sender sender = new Sender(client);
        Request request = new Request(Operations.FETCH_PEER_IPS, videoId);
        sender.send(request);

        Receiver receiver = new Receiver(client);
        Response response = (Response) receiver.receive();
        if(response.getEx() != null)
            throw response.getEx();

        peerIps = (ConcurrentHashMap<String, List<String>>) response.getResult();
        if(peerIps == null || peerIps.isEmpty())
            System.err.println("There is no peers for provided videoId!");
        else
            System.out.println(peerIps);
    }
}


