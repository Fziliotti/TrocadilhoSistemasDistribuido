package trocadilho.command;

import io.atomix.copycat.Query;
import trocadilho.domain.Trocadilho;

import java.util.List;

public class ListTrocadilhosQuery implements Query<String> {

    public String ip;
    public int port;

    public ListTrocadilhosQuery(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }
}
