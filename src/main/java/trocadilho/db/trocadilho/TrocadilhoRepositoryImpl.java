package trocadilho.db.trocadilho;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import trocadilho.domain.Trocadilho;
import trocadilho.domain.TrocadilhoDBRepresentation;
import trocadilho.domain.User;
import trocadilho.exception.NotFoundException;

import java.io.*;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TrocadilhoRepositoryImpl implements TrocadilhoRepository {

    @Override
    public void create(String content, String username) throws IOException {
        File file = new File("src/main/java/trocadilho/db/trocadilho/trocadilhoDB.json");
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        StringBuilder json = new StringBuilder();
        while (br.ready()) {
            json.append(br.readLine());
        }
        br.close();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        TrocadilhoDBRepresentation trocadilhoDBRepresentation = new TrocadilhoDBRepresentation();
        if (!String.valueOf(json).equals("")) {
            trocadilhoDBRepresentation = mapper.readValue(String.valueOf(json), TrocadilhoDBRepresentation.class);
        }
        trocadilhoDBRepresentation.getTrocadilhoList().sort(Comparator.comparing(Trocadilho::getCode).reversed());
        Optional<Trocadilho> lastTrocadilho = trocadilhoDBRepresentation.getTrocadilhoList().stream().findFirst();
        int code = 1;
        if (lastTrocadilho.isPresent()) code = lastTrocadilho.get().getCode() + 1;
        Trocadilho trocadilho = new Trocadilho(code, content, username);
        trocadilhoDBRepresentation.getTrocadilhoList().add(trocadilho);

        ObjectMapper writeMapper = new ObjectMapper();
        writeMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        writeMapper.writeValue(new File("src/main/java/trocadilho/db/trocadilho/trocadilhoDB.json"), trocadilhoDBRepresentation);

    }

    @Override
    public void replaceAll(TrocadilhoDBRepresentation trocadilhoDBRepresentation) throws IOException {

        ObjectMapper writeMapper = new ObjectMapper();
        writeMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        writeMapper.writeValue(new File("src/main/java/trocadilho/db/trocadilho/trocadilhoDB.json"), trocadilhoDBRepresentation);
    }

    @Override
    public void update(String code, String content) throws IOException {
        File file = new File("src/main/java/trocadilho/db/trocadilho/trocadilhoDB.json");
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        StringBuilder json = new StringBuilder();
        while (br.ready()) {
            json.append(br.readLine());
        }
        br.close();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        TrocadilhoDBRepresentation trocadilhoDBRepresentation = new TrocadilhoDBRepresentation();
        if (!String.valueOf(json).equals("")) {
            trocadilhoDBRepresentation = mapper.readValue(String.valueOf(json), TrocadilhoDBRepresentation.class);
        }
        Optional<Trocadilho> trocadilho = trocadilhoDBRepresentation.getTrocadilhoList().stream().filter(tr -> tr.getCode().toString().equals(code)).findFirst();
        List<Trocadilho> newTrocadilhos = trocadilhoDBRepresentation.getTrocadilhoList().stream().filter(tr -> !tr.getCode().toString().equals(code)).collect(Collectors.toList());
        if (!trocadilho.isPresent()) throw new NotFoundException("Code not found");
        trocadilho.get().setContent(content);
        newTrocadilhos.add(trocadilho.get());
        TrocadilhoDBRepresentation newTrocadilhoDBRepresentation = new TrocadilhoDBRepresentation(newTrocadilhos);
        ObjectMapper writeMapper = new ObjectMapper();
        writeMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        writeMapper.writeValue(new File("src/main/java/trocadilho/db/trocadilho/trocadilhoDB.json"), newTrocadilhoDBRepresentation);
    }

    @Override
    public void deleteById(String id) throws IOException {
        File file = new File("src/main/java/trocadilho/db/trocadilho/trocadilhoDB.json");
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        StringBuilder json = new StringBuilder();
        while (br.ready()) {
            json.append(br.readLine());
        }
        br.close();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        TrocadilhoDBRepresentation trocadilhoDBRepresentation = new TrocadilhoDBRepresentation();
        if (!String.valueOf(json).equals("")) {
            trocadilhoDBRepresentation = mapper.readValue(String.valueOf(json), TrocadilhoDBRepresentation.class);
        }
        List<Trocadilho> newTrocadilhos = trocadilhoDBRepresentation.getTrocadilhoList().stream().filter(tr -> !tr.getCode().toString().equals(id)).collect(Collectors.toList());
        if (newTrocadilhos.size() == trocadilhoDBRepresentation.getTrocadilhoList().size())
            throw new NotFoundException("Code not found");

        TrocadilhoDBRepresentation newTrocadilhoDBRepresentation = new TrocadilhoDBRepresentation(newTrocadilhos);
        ObjectMapper writeMapper = new ObjectMapper();
        writeMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        writeMapper.writeValue(new File("src/main/java/trocadilho/db/trocadilho/trocadilhoDB.json"), newTrocadilhoDBRepresentation);
    }

    @Override
    public void findByUser(String username) {

    }

    @Override
    public List<Trocadilho> listAll() throws IOException {
        File file = new File("src/main/java/trocadilho/db/trocadilho/trocadilhoDB.json");
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        StringBuilder json = new StringBuilder();
        while (br.ready()) {
            json.append(br.readLine());
        }
        br.close();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        TrocadilhoDBRepresentation trocadilhoDBRepresentation = new TrocadilhoDBRepresentation();
        if (!String.valueOf(json).equals("")) {
            trocadilhoDBRepresentation = mapper.readValue(String.valueOf(json), TrocadilhoDBRepresentation.class);
        }
        if(trocadilhoDBRepresentation == null) return new TrocadilhoDBRepresentation().getTrocadilhoList();
        return trocadilhoDBRepresentation.getTrocadilhoList().stream().sorted(Comparator.comparing(Trocadilho::getCode)).collect(Collectors.toList());
    }
}
