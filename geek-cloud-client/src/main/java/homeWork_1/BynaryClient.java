package homeWork_1;

import java.io.*;
import java.net.Socket;

public class BynaryClient {

    public static void main(String[] args) {
        BynaryClient client = new BynaryClient();
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

    public void sendFile (Socket socket) {
        File file = new File ("C:\\Users\\mi\\Desktop\\Client\\Betafpv_MagicTree.mov");
        try (DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             FileInputStream fis = new FileInputStream(file))
        {
            int fileNameSize = file.getName().getBytes().length;
            out.writeInt(fileNameSize);
            out.writeUTF(file.getName());
            long fileSize = file.length();
            out.writeLong(fileSize);

            int chunk = 4096;
            byte[] buffer = new byte[chunk];
            while (fileSize / chunk >= 1) {
                fis.read(buffer);
                out.write(buffer);
                fileSize -= chunk;
            }
            byte[] b = new byte[(int) fileSize % chunk];
            fis.read(b);
            out.write(b);

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
