package trocadilho.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Trocadilho {
    private Integer code;
    private String content;
    private String username;


    public Trocadilho(String username, String content) {
        this.username = username;
        this.content = content;
    }
}
