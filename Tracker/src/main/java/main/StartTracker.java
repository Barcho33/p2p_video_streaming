package main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import logic.ClientHandler;

public class StartTracker {
    private static final int PORT = 8004;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            System.out.println("Waiting for clients...");
            while (true) {

                Socket clientSocket = serverSocket.accept();
                System.out.println("Client " + "\u001B[31m"
                        + clientSocket.getInetAddress().getHostAddress()
                        + "\u001B[0m"
                        + ": Connection is established.");

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                Thread thread = new Thread(clientHandler);
                thread.start();

            }
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }
}