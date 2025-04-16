package Communication;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.net.Socket;

public class Sender implements Serializable{
@Serial
private static final long serialVersionUID = 1L;

    private final Socket socket;

    public Sender(Socket socket) {
        this.socket = socket;
    }

    public void send(Object obj){
        ObjectOutputStream out;
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(obj);
            out.flush();
        } catch (IOException ex) {
            throw new RuntimeException();
        }

    }
}
