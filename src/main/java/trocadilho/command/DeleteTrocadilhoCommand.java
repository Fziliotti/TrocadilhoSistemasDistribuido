package trocadilho.command;

import io.atomix.copycat.Command;

public class DeleteTrocadilhoCommand implements Command<String> {
    public String code;

    public DeleteTrocadilhoCommand(String code) {
        this.code = code;
    }
}
