package trocadilho.command;

import io.atomix.copycat.Command;

public class UpdateTrocadilhoCommand implements Command<String> {
    public String code;
    public String ip;

    public UpdateTrocadilhoCommand(String code, String ip) {
        this.code = code;
        this.ip = ip;
    }
}
