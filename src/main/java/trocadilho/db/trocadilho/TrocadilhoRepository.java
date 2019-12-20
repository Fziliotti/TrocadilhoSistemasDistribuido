package trocadilho.db.trocadilho;

import trocadilho.CreateTrocadilhoRequest;
import trocadilho.DeleteTrocadilhoRequest;
import trocadilho.GetTrocadilhoRequest;
import trocadilho.UpdateTrocadilhoRequest;
import trocadilho.domain.Trocadilho;
import trocadilho.domain.TrocadilhoDBRepresentation;
import trocadilho.domain.User;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public interface TrocadilhoRepository  {
    void create(CreateTrocadilhoRequest request) throws IOException;
    void replaceAll(TrocadilhoDBRepresentation trocadilhoDBRepresentation) throws IOException;
    void update(UpdateTrocadilhoRequest request) throws IOException;
    void deleteById(DeleteTrocadilhoRequest request) throws IOException;
    void findByUser(GetTrocadilhoRequest request);
    List<Trocadilho> listAll() throws IOException;
}
