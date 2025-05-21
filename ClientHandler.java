import java.io.*;
import java.net.*;
import java.util.*;

public class ClientHandler extends Thread {
    Socket socket;
    String name;
    String identifier;
    int mode;
    DataInputStream in;
    DataOutputStream out;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void send(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            System.out.println("Error enviando mensaje a " + name);
        }
    }

    public void sendFile(String header, byte[] data) {
        try {
            out.writeUTF(header);
            out.writeInt(data.length);
            out.write(data);
        } catch (IOException e) {
            System.out.println("Error enviando archivo a " + name);
        }
    }

    public void run() {
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            mode = Integer.parseInt(in.readUTF());  // Cliente envía 1 o 2
            identifier = in.readUTF();              // Nombre único (modo 1) o grupo (modo 2)
            name = identifier + "_" + UUID.randomUUID();  // Internamente es único

            Server.groups.putIfAbsent(identifier, new ArrayList<>());
            Server.groups.get(identifier).add(this);

            System.out.println("Conectado: " + name + " en modo " + mode);

            while (true) {
                String command = in.readUTF();

                if (command.startsWith("MSG|")) {
                    String[] parts = command.split("\\|", 3);
                    String to = parts[1];
                    String message = parts[2];
                    Server.broadcast(name, to, message, mode);

                } else if (command.startsWith("FILE|")) {
                    String[] parts = command.split("\\|", 3);
                    String to = parts[1];
                    String filename = parts[2];
                    int size = in.readInt();
                    byte[] data = new byte[size];
                    in.readFully(data);
                    Server.sendFile(name, to, filename, data, mode);
                }
            }

        } catch (IOException e) {
            System.out.println(name + " desconectado.");
        } finally {
            try {
                if (Server.groups.containsKey(identifier)) {
                    Server.groups.get(identifier).remove(this);
                    if (Server.groups.get(identifier).isEmpty()) {
                        Server.groups.remove(identifier);
                    }
                }
                socket.close();
            } catch (IOException ignored) {}
        }
    }
}
