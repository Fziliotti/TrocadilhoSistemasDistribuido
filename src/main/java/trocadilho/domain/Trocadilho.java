package trocadilho.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class Trocadilho {
    private String content;
    private User author;
    private LocalDateTime createdAt;
}
