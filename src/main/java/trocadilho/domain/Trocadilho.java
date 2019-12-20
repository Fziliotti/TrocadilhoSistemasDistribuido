package trocadilho.domain;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Trocadilho {
    private String code;
    private String content;
    private String username;
}
