package homework_1;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class BynaryServerVersionTwo {
    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(2000)){
            System.out.println("Server is listening");
            try (Socket socket = server.accept();
                 DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()))) {
                int x = in.read();
                char fileNameSize = in.readChar();
                byte[] fileNameBytes = new byte[fileNameSize];
                in.read(fileNameBytes);
                String fileName = new String(fileNameBytes);
                long fileSize = in.readLong();
                try (OutputStream out = new BufferedOutputStream(new FileOutputStream("C:\\Users\\mi\\Desktop\\Server\\" + fileName))) {
                    for (int i = 0; i < fileSize; i++) {
                        out.write(in.read());
                    }
                }

            }
        }  catch (IOException e) {
            e.printStackTrace();
        }
    }
}
