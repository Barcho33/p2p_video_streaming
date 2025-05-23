package logic;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import Communication.*;
import domain.Thumbnail;
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
                    case Operations.SAVE_THUMBNAIL:
                        controller.sendThumbnail((Thumbnail) request.getArgument());
                        break;
                    case Operations.SAVE_VIDEO_METADATA:
                        savePeersMetadata(request);
                        break;
                    case Operations.DELETE_VIDEO_METADATA:
                        deletePeersMetadata(request);
                        break;
                    case Operations.UPDATE_VIDEO_METADATA:
                        syncVideoMetadata((String []) request.getArgument(), this.clientSocket.getInetAddress().getHostName());
                        break;
                    case Operations.FETCH_ALL_VIDEOS:
                        controller.sendAllVideos();
                        break;
                    case Operations.FETCH_ALL_THUMBNAILS:
                        controller.getAllThumbnails();
                        break;
                    case Operations.NUMBER_OF_ONLINE_PEERS:
                        getNumberOfOnlinePeers((String) request.getArgument());
                        break;
                    case Operations.FETCH_PEER_IPS:
                        sendPeersIpListForVideo((String) request.getArgument());
                        break;

                    case Operations.GET_VIDEO_METADATA:
                        controller.getVideo((String) request.getArgument());
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

    private void deletePeersMetadata(Request request) {
        String videoId = (String) request.getArgument();
        String hostname = clientSocket.getInetAddress().getHostName();
        //test
        System.out.println(videoMetadata);
        //
        ConcurrentHashMap<String, List<String>> clientIps = videoMetadata.get(videoId);
        List<String> keyList = new ArrayList<>(clientIps.keySet());
        for(String segmentName : keyList){
           List<String> listOfIps = clientIps.get(segmentName);
           if(listOfIps.contains(hostname))
               videoMetadata.get(videoId).get(segmentName).remove(hostname);
        }
        //test
        System.out.println(videoMetadata);
        //
        try {
            Response response = null;
            try {
                response = new Response(true, null);
            } catch (Exception ex) {
                response = new Response(null, ex);
                throw new RuntimeException(ex);
            }finally {
                Sender sender = new Sender(this.clientSocket);
                sender.send(response);
            }

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void getNumberOfOnlinePeers(String videoId) {

        try {
            Response response = null;
            try {
                List<String> peersList = videoMetadata.get(videoId).get("playlist0.ts");
                int number;
                if(peersList == null)
                    number = 0;
                else{
                    if(peersList.contains(clientSocket.getInetAddress().getHostName()))
                        number = peersList.size() - 1;
                    else
                        number = peersList.size();
                }
                response = new Response(number, null);
            } catch (Exception ex) {
                response = new Response(null, ex);
                throw new RuntimeException(ex);
            }finally {
                Sender sender = new Sender(this.clientSocket);
                sender.send(response);
            }

        } catch (Exception ex) {
            throw new RuntimeException(ex);
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

    private void savePeersMetadata(Request request) {
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

