package common;

import java.util.ArrayList;

public class CommandMessage extends AbstractMessage {
    private String command;
    private ArrayList commandList;

    public CommandMessage(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public ArrayList getCommandList() {
        return commandList;
    }

    public void setCommandList(ArrayList commandList) {
        this.commandList = commandList;
    }
}
