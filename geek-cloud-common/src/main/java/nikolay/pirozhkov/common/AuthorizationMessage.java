package nikolay.pirozhkov.common;

public class AuthorizationMessage extends AbstractMessage {
    private String command;

    public AuthorizationMessage(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
