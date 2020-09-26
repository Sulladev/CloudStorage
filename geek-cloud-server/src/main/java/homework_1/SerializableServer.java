package homework_1;

import homeWork_1.MyFile;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SerializableServer {

    private static final String PATH = "C:\\Users\\mi\\Desktop\\Server\\Betafpv_MagicTree_Copy.mov";

    public static void main(String[] args) {
        SerializableServer server = new SerializableServer();
        Socket socket = server.createServerSocket();
        server.readFile(socket);
    }

    public Socket createServerSocket () {
        Socket socket = null;
        try {
            ServerSocket server = new ServerSocket(2000);
            System.out.println("Server started");
            socket = server.accept();
            System.out.println("Client connected");
        } catch (IOException e) {
            System.out.println("Server exception");
            e.printStackTrace();
        }

        return socket;
    }

    public void readFile (Socket socket) {
        try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream())))
        {
          MyFile file = (MyFile) in.readObject();
          byte[] bytes = file.getBytes();
          Files.write(Paths.get(PATH), bytes);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        System.out.println("File received");

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
