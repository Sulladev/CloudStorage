package homework_1;

import homeWork_1.MyFile;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class BynaryServer {
    public static void main(String[] args) {
        BynaryServer server = new BynaryServer();
        Socket socket = server.createServerSocket();;
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
        try (DataInputStream in = new DataInputStream(socket.getInputStream())
        )
        {
            int fileNameSize = in.readInt();
            String fileName = in.readUTF();
            long fileSize = in.readLong();

            FileOutputStream fos = new FileOutputStream("C:\\Users\\mi\\Desktop\\Server\\" + fileName);

            int chunk = 4096;
            byte[] buffer = new byte[chunk];
            while (fileSize / chunk >= 1) {
                in.read(buffer);
                fos.write(buffer);
                fileSize -= chunk;
            }
            byte[] b = new byte[(int) fileSize % chunk];
            in.read(b);
            fos.write(b);


        } catch (IOException e) {
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
