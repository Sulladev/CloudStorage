package homeWork_1;

import java.io.*;
import java.net.Socket;

public class BynaryClientVersionTwo {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 2000);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())){
            out.write(15);
            String fileName = "Betafpv_MagicTree.mov";
            char fileNameLength = (char)fileName.length();
            out.writeShort(fileNameLength);
            out.write(fileName.getBytes());
            out.writeLong(new File("C:\\Users\\mi\\Desktop\\Client\\" + fileName).length());
            byte[] buf = new byte[4096];
            try (FileInputStream in = new FileInputStream("C:\\Users\\mi\\Desktop\\Client\\" + fileName)) {
                int n;
                while (( n = in.read(buf)) != -1) {
                    out.write(buf,0,n);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
