package trocadilho.command;

import io.atomix.copycat.Command;

public class DeleteTrocadilhoCommand implements Command<String> {
    public String ip;

    public DeleteTrocadilhoCommand(String code) {
        this.ip = ip;
    }
}
