package nioChatClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
            this.clientSocketChannel.register(selector, SelectionKey.OP_CONNECT|SelectionKey.OP_READ);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        handleSendMessage(clientSocketChannel);
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
                    if (key.isReadable()) this.handleReadMessage(clientSocketChannel);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleReadMessage(SocketChannel ch) throws IOException {

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

    private void handleSendMessage(SocketChannel channel)  {
        Scanner scanner = new Scanner(System.in);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Runnable r = ()-> {
            while(true) {
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
                buf.clear();
            }
        };
        executor.submit(r);
        executor.shutdown();

    }



    

    public static void main(String[] args) {
        new Thread(new NioChatClientExample()).start();
    }
}


//        Zdravstvuite, Nikolai. Sovershenno verno, konturi pravilnie. U vas viletaet oshibka potomu,
//                chto vi zakrivaete kanal kotoryi poluchaete iz klucha. Vam v printsipe net neobhodimosti
//                na kliente poluchat kanal iz klucha. U vas zhe klient obshaetsya tolko s serverom,
//                nikto drugoi emu ne mozhet napisat. Tak chto vi mozhete postoyanno ispolzovat tot zhe
//                samyi channel, kotoryi vi vnachale otkrivaete. Naprimer :
//        this.handleSendMessage(this.clientSocketChannel);
//        i zatem v metode :
//        private void handleSendMessage(SocketChannel channel) {
//            Scanner scanner = new Scanner(System.in);
//            System.out.println("Enter message to server: ");
//            String output = scanner.nextLine();
//            buf.put(output.getBytes());
//            buf.flip();
//            while(buf.hasRemaining()) {
//            try {
//            channel.write(buf);
//            } catch (IOException e) {
//            e.printStackTrace();
//            }
//            }
//            System.out.println("Message send");
//            buf.clear();
//        }
//
//        Tak uzhe vse rabotaet, edinstvennoe chto ostaetsya zaderzhka pechati soobshenyi iz servera.
//        Eto iz-za blokirovki klaviaturi skanerom. Chtobi etogo izbezhat mozhno chtenie sdelat otdelnim potokom.
//        I sobitiya na zapis ne obyazatelno v selector dobavlyat. Vi zhe po idee v chate pishete,
//        kogda vam hochetsya, v luboi moment, i tolko vi (to est klient) znaet, kogda on hochet prinyat uchastie
//        v diskussii. Vot vash variant s nebolshimi modifikatsiyami, kotoryi rabotaet :