package clientApp;


import common.AbstractMessage;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;

import java.io.IOException;
import java.net.Socket;

public class Network {
   private static Socket socket;
   private static ObjectEncoderOutputStream out;
   private static ObjectDecoderInputStream in;

   private Network() {

   }

   public static void start() {
      try {
         socket = new Socket("localhost", 8189);
         out = new ObjectEncoderOutputStream(socket.getOutputStream());
         in = new ObjectDecoderInputStream(socket.getInputStream(), 100 * 1024 * 1024);

      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   public static void stop () {
      try {
         out.close();
      } catch (IOException e) {
         e.printStackTrace();
      }
      try {
         in.close();
      } catch (IOException e) {
         e.printStackTrace();
      }
      try {
         socket.close();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   public static boolean sendMessage (AbstractMessage msg) {
      try {
         out.writeObject(msg);
         return  true;
      } catch (IOException e) {
         e.printStackTrace();
      }
      return false;
   }

   public static AbstractMessage readObject () throws ClassNotFoundException, IOException {
      Object obj = in.readObject();
      return (AbstractMessage) obj;
   }

}
