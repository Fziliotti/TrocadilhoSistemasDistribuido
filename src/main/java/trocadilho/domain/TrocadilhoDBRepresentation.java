package trocadilho.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class TrocadilhoDBRepresentation {
    List<Trocadilho> trocadilhoList;

    public TrocadilhoDBRepresentation() {
        this.trocadilhoList = new ArrayList<>();
    }
}
