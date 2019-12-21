package trocadilho.command;

import io.atomix.copycat.Query;

public class FindByCodeQuery implements Query<String> {
    public String code;

    public FindByCodeQuery(String code) {
        this.code = code;
    }
}
