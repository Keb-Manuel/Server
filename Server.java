import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    public static final int PORT = Integer.parseInt(System.getenv().getOrDefault("PORT", "3000"));
    public static Map<String, List<ClientHandler>> groups = new HashMap<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Servidor escuchando en puerto " + PORT);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            new ClientHandler(clientSocket).start();
        }
    }

    public static synchronized void broadcast(String from, String to, String message, int mode) {
        if (mode == 1) {
            // Modo 1: individual o ALL
            List<ClientHandler> recipients = groups.get(to);
            if (to.equalsIgnoreCase("ALL")) {
                for (List<ClientHandler> handlers : groups.values()) {
                    for (ClientHandler ch : handlers) {
                        if (!ch.name.equals(from)) {
                            ch.send("MSG|" + from + "|" + message);
                        }
                    }
                }
            } else if (recipients != null) {
                for (ClientHandler ch : recipients) {
                    if (!ch.name.equals(from)) {
                        ch.send("MSG|" + from + "|" + message);
                    }
                }
            }
        } else if (mode == 2) {
            // Modo 2: solo usuarios del mismo grupo
            List<ClientHandler> group = groups.get(to);
            if (group != null) {
                for (ClientHandler ch : group) {
                    if (!ch.name.equals(from)) {
                        ch.send("MSG|" + from + "|" + message);
                    }
                }
            }
        }
    }

    public static synchronized void sendFile(String from, String to, String filename, byte[] data, int mode) {
        if (mode == 1) {
            List<ClientHandler> recipients = groups.get(to);
            if (to.equalsIgnoreCase("ALL")) {
                for (List<ClientHandler> handlers : groups.values()) {
                    for (ClientHandler ch : handlers) {
                        if (!ch.name.equals(from)) {
                            ch.sendFile("FILE|" + from + "|" + filename, data);
                        }
                    }
                }
            } else if (recipients != null) {
                for (ClientHandler ch : recipients) {
                    if (!ch.name.equals(from)) {
                        ch.sendFile("FILE|" + from + "|" + filename, data);
                    }
                }
            }
        } else if (mode == 2) {
            List<ClientHandler> group = groups.get(to);
            if (group != null) {
                for (ClientHandler ch : group) {
                    if (!ch.name.equals(from)) {
                        ch.sendFile("FILE|" + from + "|" + filename, data);
                    }
                }
            }
        }
    }
}
