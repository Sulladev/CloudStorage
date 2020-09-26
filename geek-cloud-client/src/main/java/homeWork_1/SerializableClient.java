package homeWork_1;

import java.io.*;
import java.net.Socket;
import java.nio.file.Paths;

public class SerializableClient {

    private static final String PATH = "C:\\Users\\mi\\Desktop\\Client\\Betafpv_MagicTree.mov";

    public static void main(String[] args) {
        SerializableClient client = new SerializableClient();
        Socket socket = client.createClientSocket();
        client.sendFile(socket);
    }

    public Socket createClientSocket () {
        Socket socket = null;
        try {
            socket = new Socket("localhost",2000);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return socket;
    }

    public void sendFile(Socket socket)  {
        try ( ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream())))
        {
           MyFile file = new MyFile(Paths.get(PATH));
           out.writeObject(file);

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
