package logic;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import Communication.*;
import domain.Video;

public class ClientHandler implements Runnable{

    private final Socket clientSocket;
    private static List<Socket> listOfClients = new ArrayList<>();
    private static final ConcurrentHashMap<String, ConcurrentHashMap<String, List<String>>> videoMetadata = new ConcurrentHashMap<>();
    private final DatabaseController controller;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.controller = new DatabaseController(clientSocket);
        listOfClients.add(clientSocket);

        if(videoMetadata.isEmpty()){
            List<Video> videoList =  controller.getAllVideos();
            for(Video video : videoList)
                initializeVideoMetadata(video);
        }
    }

    @Override
    public void run() {
        try {
            while(true){
                Request request = (Request) new Receiver(clientSocket).receive();

                if(request == null)
                    break;

                switch (request.getOperation()) {
                    case Operations.REGISTER:
                        controller.createUser((String[]) request.getArgument());
                        break;
                    case Operations.VERIFY_USER_EXISTS:
                        controller.isUserPresent((String) request.getArgument());
                        break;
                    case Operations.UPLOAD_VIDEO:
                        controller.sendVideo((Video) request.getArgument());
                        break;
                    case Operations.PEER_HANDSHAKE:
                        savePeerHandshakeData(request);
                        break;
                    case Operations.FETCH_ALL_VIDEOS:
                        controller.sendAllVideos();
                        break;
                    case Operations.FETCH_PEER_IPS:
                        sendPeersIpListForVideo((String) request.getArgument());
                        break;
                    case Operations.UPDATE_VIDEO_METADATA:
                        syncVideoMetadata((String []) request.getArgument(), this.clientSocket.getInetAddress().getHostName());
                        break;
                    default:
                        throw new AssertionError();
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            close();
        }
    }
    private void initializeVideoMetadata(Video video){
        int segmentsNum = video.getSegmentNum();
        ConcurrentHashMap<String, List<String>> clientIp = new ConcurrentHashMap<>();

        for(int i = 0; i < segmentsNum; ++i){
            String segmentName = "playlist" + i + ".ts";
            clientIp.put(segmentName, new ArrayList<>());
        }
        videoMetadata.put(video.getVideoId(), clientIp);
    }

    private void savePeerHandshakeData(Request request) {
        ConcurrentHashMap<String, List<String>> segments = (ConcurrentHashMap<String, List<String>>) request.getArgument();

        List<String> keyList = new ArrayList<>(segments.keySet());
        for(String videoId : keyList){
            if(videoMetadata.containsKey(videoId)){
                List<String> segmentNames = segments.get(videoId);
                for (String segmentName : segmentNames){
                    if(videoMetadata.get(videoId).containsKey(segmentName))
                        videoMetadata.get(videoId).get(segmentName).add(clientSocket.getInetAddress().getHostName());

                }
            }
        }
        Response response = new Response(true, null);
        Sender sender = new Sender(this.clientSocket);
        try {
            sender.send(response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void sendPeersIpListForVideo(String videoId){
        ConcurrentHashMap<String, List<String>> segments = videoMetadata.get(videoId);

        Sender sender = new Sender(clientSocket);
        Response response = new Response(segments, null);
        sender.send(response);
    }

    private void syncVideoMetadata(String[] data, String peerIp){
        Sender sender = new Sender(clientSocket);
        try {
            videoMetadata.get(data[0]).get(data[1]).add(peerIp);
            Response response = new Response(true, null);
            sender.send(response);
        } catch (Exception ex) {
            Response response = new Response(null, ex);
            sender.send(response);
        }
    }

    private void pruneVideoMetadata(String peerIp){
        List<String> keyList = new ArrayList<>(videoMetadata.keySet());
        for(String videoId : keyList){
            List<String> keySegments = new ArrayList<>(videoMetadata.get(videoId).keySet());
            for(String segment : keySegments){
                videoMetadata.get(videoId).get(segment).remove(peerIp);
            }
        }
    }

    public void close(){
        try {
            listOfClients.remove(this.clientSocket);
            pruneVideoMetadata(this.clientSocket.getInetAddress().getHostName());

            if(!clientSocket.isClosed())
                clientSocket.close();

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
}

