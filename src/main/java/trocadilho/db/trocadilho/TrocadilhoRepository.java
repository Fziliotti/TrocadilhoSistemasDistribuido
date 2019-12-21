package trocadilho.db.trocadilho;

import trocadilho.CreateTrocadilhoRequest;
import trocadilho.DeleteTrocadilhoRequest;
import trocadilho.GetTrocadilhoRequest;
import trocadilho.UpdateTrocadilhoRequest;
import trocadilho.domain.Trocadilho;
import trocadilho.domain.TrocadilhoDBRepresentation;

import java.io.IOException;
import java.util.List;

public interface TrocadilhoRepository  {
    void create(CreateTrocadilhoRequest request) throws IOException;
    void replaceAll(TrocadilhoDBRepresentation trocadilhoDBRepresentation) throws IOException;
    void update(UpdateTrocadilhoRequest request) throws IOException;
    void deleteById(DeleteTrocadilhoRequest request) throws IOException;
    Trocadilho findByCode(GetTrocadilhoRequest request) throws IOException;
    List<Trocadilho> listAll() throws IOException;
}
