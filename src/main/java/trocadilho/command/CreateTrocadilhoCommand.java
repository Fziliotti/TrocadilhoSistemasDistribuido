package trocadilho.command;

import io.atomix.copycat.Command;

public class CreateTrocadilhoCommand implements Command<String> {
    public String username;
    public String content;

    public CreateTrocadilhoCommand(String username, String content) {
        this.username = username;
        this.content = content;
    }
}
