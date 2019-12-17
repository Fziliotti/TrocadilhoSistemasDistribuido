package trocadilho.command;

import io.atomix.copycat.Command;

public class UpdateTrocadilhoCommand implements Command<String> {
    public String code;
    public String content;

    public UpdateTrocadilhoCommand(String code, String content) {
        this.code = code;
        this.content = content;
    }
}
