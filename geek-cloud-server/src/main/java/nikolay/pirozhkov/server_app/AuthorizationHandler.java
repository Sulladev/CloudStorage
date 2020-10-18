package nikolay.pirozhkov.server_app;

import nikolay.pirozhkov.common.AuthorizationMessage;
import nikolay.pirozhkov.common.CommandMessage;
import nikolay.pirozhkov.common.FileMessage;
import nikolay.pirozhkov.common.FileRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class AuthorizationHandler extends ChannelInboundHandlerAdapter {

    private boolean isAuthorized;
    String rootDir = "server_repository/";


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (!isAuthorized && msg instanceof AuthorizationMessage) {
            AuthorizationMessage authorizationMessage = (AuthorizationMessage) msg;
            readCommand(ctx, authorizationMessage);
        }

        if (isAuthorized ) {
             ctx.fireChannelRead(msg);
        }
    }

    private void readCommand(ChannelHandlerContext ctx, AuthorizationMessage authorizationMessage) {
        if (authorizationMessage.getCommand().startsWith("/")) {
            String[] command = authorizationMessage.getCommand().split(" ");
            if (command[0].equals("/auth")) {
                authorization(command[1], ctx);
            }
        }
    }

    private boolean authorization(String msg, ChannelHandlerContext ctx) {
        String[] arr = msg.split("±");
        String login = arr[0];
        String password = arr[1];
        String username = SqlClient.getNickname(login, password);
        if (username == null) {
            System.out.println("Invalid login attempt: " + login);
            return isAuthorized = false;
        } else {
            System.out.println("Authorization successful");
            if (!Files.exists(Paths.get(rootDir + username +"/"))) {
                try {
                    Files.createDirectory(Paths.get(rootDir + username + "/"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            ctx.pipeline().addLast(new ServerAppHandler(username));
            return isAuthorized = true;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

}
