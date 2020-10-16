package nikolay.pirozhkov.server_app;

import nikolay.pirozhkov.common.CommandMessage;
import nikolay.pirozhkov.common.FileMessage;
import nikolay.pirozhkov.common.FileRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public class ServerAppHandler extends ChannelInboundHandlerAdapter {
    ArrayList<String> fileList = new ArrayList<>();
    String rootDir = "server_repository/";
    private final String username;

    public ServerAppHandler(String username) {
        this.username = username;
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {


        if (msg instanceof FileRequest) {
            FileRequest fileRequest = (FileRequest) msg;
            sendFileToClient(ctx,fileRequest);
        }

        if (msg instanceof FileMessage) {
            FileMessage fileMessage = (FileMessage) msg;
            writeFileFromClient(ctx, fileMessage);
        }

        if (msg instanceof CommandMessage) {
            CommandMessage commandMessage = (CommandMessage) msg;
            readCommand(ctx, commandMessage);
        }
    }

    // отправляем обновленный список файлов на сервере - клиенту
    private void sendFileListUpdate(ChannelHandlerContext ctx) {
        refreshFilesList(rootDir + username +"/");
        CommandMessage commandMessage = new CommandMessage("/list");
        commandMessage.setCommandList(fileList);
        ctx.writeAndFlush(commandMessage);
    }

    //  отправляем файл клиенту
    private void sendFileToClient(ChannelHandlerContext ctx, FileRequest fileRequest) {
        try {
            if (Files.exists(Paths.get(rootDir + fileRequest.getFileName()))) {
                FileMessage fileMessage = new FileMessage(Paths.get(rootDir + username +"/" +  fileRequest.getFileName()));
                ctx.writeAndFlush(fileMessage);
                System.out.println("File was sent");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // принимаем файл от клиента
    private void writeFileFromClient(ChannelHandlerContext ctx, FileMessage fileMessage) {
        try {
            if (!Files.exists(Paths.get(rootDir + username +"/" + fileMessage.getFileName()))) {
                Files.write(Paths.get(rootDir + username +"/" +  fileMessage.getFileName()), fileMessage.getData(), StandardOpenOption.CREATE);
                sendFileListUpdate(ctx);
                System.out.println("File received");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // принимаем и парсим команды на удаление файла на сервере и получение списка файлов с сервера
    private void readCommand(ChannelHandlerContext ctx, CommandMessage commandMessage) {
        if (commandMessage.getCommand().startsWith("/")) {
            String[] command = commandMessage.getCommand().split(" ");
            if (command[0].equals("/del")) {
                deleteFile(command[1]);
                sendFileListUpdate(ctx);
            }
            if (command[0].equals("/list")) {
                sendFileListUpdate(ctx);
                System.out.println("File list was updated");
            }
        }
    }

    //обновляем список файлов на сервере
    private void refreshFilesList(String path) {
        fileList.clear();
        try {
            Files.list(Paths.get(path))
                    .filter(p -> !Files.isDirectory(p))
                    .map(p -> p.getFileName().toString())
                    .forEach(o -> fileList.add(o));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // удаляем файл на сервере
    private void deleteFile (String fileToDelete) {
        if (Files.exists(Paths.get(rootDir + username +"/" +  fileToDelete))) {
            try {
                Files.delete(Paths.get(rootDir + username +"/" +  fileToDelete));
                System.out.println("File " + fileToDelete + " deleted");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
