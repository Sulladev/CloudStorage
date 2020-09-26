package homeWork_1;

import java.io.*;
import java.net.Socket;


public class InputOutputClient {
    public static void main(String[] args) {
        InputOutputClient serializableClient = new InputOutputClient();
        Socket socket = serializableClient.createClientSocket();
        serializableClient.sendFile(socket);
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
        File file = new File("C:\\Users\\mi\\Desktop\\Client\\Betafpv_MagicTree.mov");

        try (FileInputStream fis = new FileInputStream(file);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream()))
        {
            int n = -1;
            byte[] buffer = new byte[4096];
            while(( n = fis.read(buffer)) > -1) {
               out.write(buffer,0,n);
            }

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
