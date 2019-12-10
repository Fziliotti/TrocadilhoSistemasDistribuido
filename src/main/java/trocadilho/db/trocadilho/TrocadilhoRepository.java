package trocadilho.db.trocadilho;

import trocadilho.domain.Trocadilho;
import trocadilho.domain.User;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public interface TrocadilhoRepository  {
    void create(String content, String username) throws IOException;
    void update(String code, String content) throws IOException;
    void deleteById(String id) throws IOException;
    void findByUser(String username);
    List<Trocadilho> listAll() throws IOException;
}
