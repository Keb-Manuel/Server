import java.io.*;
import java.net.*;

public class ClientHandler extends Thread {
    Socket socket;
    String name;
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

            name = in.readUTF();  // El cliente debe enviar su nombre al conectar
            Server.clients.put(name, this);
            System.out.println("Conectado: " + name);

            while (true) {
                String command = in.readUTF(); // MSG|destino|contenido o FILE|destino|nombre.ext

                if (command.startsWith("MSG|")) {
                    String[] parts = command.split("\\|", 3);
                    String to = parts[1];
                    String message = parts[2];
                    Server.broadcast(name, to, message);

                } else if (command.startsWith("FILE|")) {
                    String[] parts = command.split("\\|", 3);
                    String to = parts[1];
                    String filename = parts[2];
                    int size = in.readInt();
                    byte[] data = new byte[size];
                    in.readFully(data);
                    Server.sendFile(name, to, filename, data);
                }
            }

        } catch (IOException e) {
            System.out.println(name + " desconectado.");
        } finally {
            try {
                Server.clients.remove(name);
                socket.close();
            } catch (IOException ignored) {}
        }
    }
}
