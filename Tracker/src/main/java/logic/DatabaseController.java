package logic;

import database.DatabaseBroker;
import java.net.Socket;
import java.util.List;

import Communication.Response;
import Communication.Sender;
import domain.Video;

public class DatabaseController {


    private final DatabaseBroker dbb;
    private final Sender sender;

    public DatabaseController(Socket client_socket) {
        this.dbb = new DatabaseBroker();
        this.sender = new Sender(client_socket);
    }

    public void createUser(String[] credentials){

        try {
            Response response = null;
            try {
                boolean result = dbb.saveUser(credentials[0], credentials[1]);
                response = new Response(result, null);
            } catch (Exception ex) {
                response = new Response(null, ex);
                throw new RuntimeException(ex);
            }finally {
                sender.send(response);
            }

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void isUserPresent(String username){

        try {
            Response response = null;
            try {
                boolean result = dbb.existsUserByUsername(username);
                response = new Response(result, null);
            } catch (Exception ex) {
                response = new Response(null, ex);
                throw new RuntimeException(ex);
            }finally {
                sender.send(response);
            }

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }


    public void sendVideo(Video video){

        try {
            Response response = null;
            try {
                boolean result = dbb.saveVideo(video);
                response = new Response(result, null);
            } catch (Exception ex) {
                response = new Response(null, ex);
                throw new RuntimeException(ex);
            }finally {
                sender.send(response);
            }

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void sendAllVideos(){


        try {
            Response response = null;
            try {
                List<Video> videoList = dbb.getAllVideos();
                response = new Response(videoList, null);
            } catch (Exception ex) {
                response = new Response(null, ex);
                throw new RuntimeException(ex);
            }finally {
                sender.send(response);
            }

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

    public List<Video> getAllVideos(){
        return dbb.getAllVideos();
    }
}
