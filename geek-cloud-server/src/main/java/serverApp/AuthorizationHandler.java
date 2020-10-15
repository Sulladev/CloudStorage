package serverApp;

import common.AuthorizationMessage;
import common.CommandMessage;
import common.FileMessage;
import common.FileRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.file.Files;
import java.nio.file.Paths;


public class AuthorizationHandler extends ChannelInboundHandlerAdapter {

    private boolean isAuthorized;
    private String username;
    String rootDir = "server_repository/";


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (!isAuthorized && msg instanceof AuthorizationMessage) {
            AuthorizationMessage authorizationMessage = (AuthorizationMessage) msg;
            readCommand(ctx, authorizationMessage);
        }

        if (isAuthorized ) {

            if (!Files.exists(Paths.get(rootDir + username +"/"))) {
                Files.createDirectory(Paths.get(rootDir + username + "/"));
            }

            ctx.pipeline().addLast(new ServerAppHandler(username));

             if (msg instanceof FileRequest) {
                FileRequest fileRequest = (FileRequest) msg;
                ctx.fireChannelRead(msg);
            }

            if (msg instanceof FileMessage) {
                FileMessage fileMessage = (FileMessage) msg;
                ctx.fireChannelRead(msg);
            }

            if (msg instanceof CommandMessage) {
                CommandMessage commandMessage = (CommandMessage) msg;
                ctx.fireChannelRead(msg);
            }

        }
    }

    private void readCommand(ChannelHandlerContext ctx, AuthorizationMessage authorizationMessage) {
        if (authorizationMessage.getCommand().startsWith("/")) {
            String[] command = authorizationMessage.getCommand().split(" ");
            if (command[0].equals("/auth")) {
                authorization(command[1]);
            }
        }
    }

    private boolean authorization(String msg) {
        String[] arr = msg.split("±");
        String login = arr[0];
        String password = arr[1];
        username = SqlClient.getNickname(login, password);
        if (username == null) {
            System.out.println("Invalid login attempt: " + login);
            return isAuthorized = false;
        } else {
            System.out.println("Authorization successful");
            return isAuthorized = true;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

}
