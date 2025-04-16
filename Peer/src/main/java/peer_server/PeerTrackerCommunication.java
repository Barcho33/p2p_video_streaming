package peer_server;

import Communication.*;
import metadata.SQLiteManager;

import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;


public class PeerTrackerCommunication {

    private static Socket socket;
    private static PeerTrackerCommunication instance;

    public static PeerTrackerCommunication getInstance(){
        if(instance == null)
            instance = new PeerTrackerCommunication();
        return instance;
    }

    public void initSocket(Socket socket){
        PeerTrackerCommunication.socket = socket;
    }

    public void updateMetaData(String videoId, String segmentName, Path localPath) throws Exception {
        if(!Files.exists(localPath)){
            System.err.println("Segment " + segmentName + " is not saved locally!");
            return;
        }

        if(SQLiteManager.insertSegmentData(videoId, segmentName)) {
            String[] data = {videoId, segmentName};

            Sender sender = new Sender(socket);
            Request request = new Request(Operations.UPDATE_VIDEO_METADATA, data);
            sender.send(request);

            Receiver receiver = new Receiver(socket);
            Response response = (Response) receiver.receive();
            if((boolean) response.getResult())
                System.out.println("Metadata is successfully updated.");
            else
                System.err.println("Metadata is not successfully updated.");
        }
        else
            System.err.println("Segment insertion error.");
    }
}
