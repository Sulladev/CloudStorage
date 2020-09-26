package homework_1;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


public class InputOutputServer {

  public static void main(String[] args) {
       InputOutputServer serializableServer = new InputOutputServer();
        Socket socket = serializableServer.createServerSocket();
        serializableServer.readFile(socket);
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

      File file = new File("C:\\Users\\mi\\Desktop\\Server\\Betafpv_MagicTree_Copy.mov");


      try (FileOutputStream fos = new FileOutputStream(file);
           DataInputStream in = new DataInputStream(socket.getInputStream()))
      {
          int n;
          byte[] buffer = new byte[4096];
          while(( n = in.read(buffer)) > 0) {
              fos.write(buffer,0,n);
          }

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
