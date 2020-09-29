package nioChatClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;

public class NioChatClientExample implements  Runnable{
    private SocketChannel clientSocketChannel;
    private Selector selector;
    private ByteBuffer buf = ByteBuffer.allocate(256);
    private int ClientIndex = 1;

    public NioChatClientExample() {
        try {
            this.clientSocketChannel = SocketChannel.open();
            this.clientSocketChannel.socket().connect(new InetSocketAddress("localhost", 8189));
            this.clientSocketChannel.configureBlocking(false);
            this.selector = Selector.open();
            this.clientSocketChannel.register(selector, SelectionKey.OP_CONNECT|SelectionKey.OP_READ|SelectionKey.OP_WRITE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            Iterator<SelectionKey> iter;
            SelectionKey key;
            while (this.clientSocketChannel.isOpen()) {
                selector.select();
                iter = this.selector.selectedKeys().iterator();
                while (iter.hasNext()) {
                    key = iter.next();
                    iter.remove();
                    if (key.isConnectable())  this.handleConnect(key);
                    if (key.isWritable()) this.handleSendMessage(key);
                    if (key.isReadable()) this.handleReadMessage(key);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleReadMessage(SelectionKey key) throws IOException {
        SocketChannel ch = (SocketChannel) key.channel();
        StringBuilder sb = new StringBuilder();
        buf.clear();
        int read = 0;
        while ((read = ch.read(buf)) > 0) {
            buf.flip();
            byte[] bytes = new byte[buf.limit()];
            buf.get(bytes);
            sb.append(new String(bytes));
            buf.clear();
        }

        String msg;
        msg = sb.toString();
        System.out.println(msg);

    }

    private void handleConnect(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        try {
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_WRITE|SelectionKey.OP_READ);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleSendMessage(SelectionKey key)  {
        SocketChannel channel = (SocketChannel)key.channel();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter message to server: ");
        String output = scanner.nextLine();
        buf.put(output.getBytes());
        buf.flip();
        while(buf.hasRemaining()) {
           try {
               channel.write(buf);
           } catch (IOException e) {
               e.printStackTrace();
           }
        }
           System.out.println("Message send");
           buf.clear();
        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    

    public static void main(String[] args) {
        new Thread(new NioChatClientExample()).start();
    }
}

