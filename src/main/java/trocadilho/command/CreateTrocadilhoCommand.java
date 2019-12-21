package trocadilho.command;

import io.atomix.copycat.Command;

public class CreateTrocadilhoCommand implements Command<String> {
    public String code;
    public String username;
    public String content;

    public CreateTrocadilhoCommand(String code, String username, String content) {
        this.code = code;
        this.username = username;
        this.content = content;
    }
}
