import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    public static final int PORT = Integer.parseInt(System.getenv().getOrDefault("PORT", "3000"));
    public static Map<String, ClientHandler> clients = new HashMap<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Servidor escuchando en puerto " + PORT);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            new ClientHandler(clientSocket).start();
        }
    }

    public static synchronized void broadcast(String from, String to, String message) {
        if (to == null || to.equalsIgnoreCase("ALL")) {
            for (ClientHandler ch : clients.values()) {
                if (!ch.name.equals(from)) ch.send("MSG|" + from + "|" + message);
            }
        } else {
            ClientHandler ch = clients.get(to);
            if (ch != null) {
                ch.send("MSG|" + from + "|" + message);
            }
        }
    }

    public static synchronized void sendFile(String from, String to, String filename, byte[] data) {
        if (to == null || to.equalsIgnoreCase("ALL")) {
            for (ClientHandler ch : clients.values()) {
                if (!ch.name.equals(from)) ch.sendFile("FILE|" + from + "|" + filename, data);
            }
        } else {
            ClientHandler ch = clients.get(to);
            if (ch != null) {
                ch.sendFile("FILE|" + from + "|" + filename, data);
            }
        }
    }
}
