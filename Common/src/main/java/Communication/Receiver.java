package Communication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.net.Socket;

public class Receiver implements Serializable{
@Serial
private static final long serialVersionUID = 1L;

    private final Socket socket;

    public Receiver(Socket socket) {
        this.socket = socket;
    }

    public Object receive() throws Exception{

        try {
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            return in.readObject();

        } catch (IOException ex) {
            System.out.println("Client " + "\u001B[31m"+ socket.getInetAddress().getHostAddress()+"\u001B[0m" + ": The I/O stream is closed. The connection with the client was terminated.");

        }

        return null;
    }


}