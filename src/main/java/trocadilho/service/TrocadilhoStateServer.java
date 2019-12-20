package trocadilho.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Synchronized;
import trocadilho.CreateTrocadilhoRequest;
import trocadilho.DeleteTrocadilhoRequest;
import trocadilho.UpdateTrocadilhoRequest;
import trocadilho.db.trocadilho.TrocadilhoRepository;
import trocadilho.db.trocadilho.TrocadilhoRepositoryImpl;
import trocadilho.domain.Trocadilho;
import trocadilho.domain.TrocadilhoDBRepresentation;
import trocadilho.domain.enums.OperationTypeEnum;
import trocadilho.server.ServerGRPC;

import java.io.*;
import java.util.*;

import static trocadilho.domain.enums.OperationTypeEnum.*;
import static trocadilho.service.TrocadilhoServiceImpl.LOG;
import static trocadilho.service.TrocadilhoServiceImpl.SNAPSHOT;

public class TrocadilhoStateServer {

    private TrocadilhoRepository trocadilhoRepository = new TrocadilhoRepositoryImpl();
    private Boolean isOnline = true;
    private Integer id;
    private Integer intervalToSnapshot;


    public void beginStateControl(Integer id) {
        this.id = id;
        this.intervalToSnapshot = ServerGRPC.getIntervalToSnapshot() * 1000;
        this.startSnapshotJob();

    }

    public List<Trocadilho> getTrocadilhos() {
        try {
            return trocadilhoRepository.listAll();
        } catch (IOException e) {
            System.out.println("Cannot list all trocadilhos!");
            return new ArrayList<>();
        }
    }

    @Synchronized
    public void createTrocadilho(CreateTrocadilhoRequest request) throws IOException {
        trocadilhoRepository.create(request);
        insertLog(CREATE, request.getCode(), request.getUsername(), request.getTrocadilho());
    }

    @Synchronized
    public void updateTrocadilho(UpdateTrocadilhoRequest request) throws IOException {
        trocadilhoRepository.update(request);
        insertLog(UPDATE, request.getCode(), ".", request.getTrocadilho());
    }

    @Synchronized
    public void deleteTrocadilho(DeleteTrocadilhoRequest request) throws IOException {
        trocadilhoRepository.deleteById(request);
        insertLog(DELETE, request.getCode(), ".", ".");
    }

    @Synchronized
    private void insertLog(OperationTypeEnum operation, String code, String username, String content) {
        Optional<Integer> maxFileId = getMaxFileId(LOG);
        int fileId = 1;
        if (maxFileId.isPresent()) fileId = maxFileId.get();
        String log = operation.value + ';' + code + ';' + username + ';' + content;
        String snapshotFile = LOG + '/' + LOG + '_' + (fileId) + ".txt";
        File file = new File(snapshotFile);

        try {
            FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.append(log);
            bw.append('\n');
            bw.close();
        } catch (IOException e) {
            System.out.println("Cannot write a log with username: " + username);
        }
    }

    public void startSnapshotJob() {
        new Thread(() -> {
            while (isOnline) {
                try {
                    System.out.println("Snapshot created! id:" + this.id);
                    this.createSnapshot(this.id);
                    this.createLog(this.id);
                    this.id++;
                    Thread.sleep(intervalToSnapshot);
                } catch (IOException | InterruptedException e) {
                    break;
                }

            }
        }).start();
    }

    @Synchronized
    public void createLog(Integer id) {
        String newLogFile = LOG + '/' + LOG + '_' + (id) + ".txt";
        File newFile = new File(newLogFile);

        ObjectMapper writeMapper = new ObjectMapper();
        writeMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        try {
            newFile.createNewFile();
        } catch (IOException e) {
            System.out.println("Cannot create log file!");
        }

        synchronized (this) {
            deleteFirstFile(id, LOG);
        }
    }

    private void deleteFirstFile(Integer id, String fileType) {
        File directory = new File(fileType);
        File[] files = directory.listFiles();
        List<File> filesList = new ArrayList<>();
        if (files != null && files.length != 0)
            filesList = Arrays.asList(files);
        if (files.length > 3) {
            Optional<Integer> minLogId = filesList.stream()
                    .filter(file -> file.getName().contains(fileType))
                    .map(file -> file.getName().split("_")[1])
                    .map(file -> Integer.parseInt(file.split(".t")[0]))
                    .min(Comparator.naturalOrder());
            if (minLogId.isPresent()) {
                String snapshotFile = fileType + '/' + fileType + '_' + (minLogId.get()) + ".txt";
                File file = new File(snapshotFile);
                file.delete();
            }
        }
    }

    @Synchronized
    public void createSnapshot(Integer id) throws IOException {
        List<Trocadilho> trocadilhos = trocadilhoRepository.listAll();
        TrocadilhoDBRepresentation trocadilhoDBRepresentation = new TrocadilhoDBRepresentation(trocadilhos);


        String newSnapshotFile = SNAPSHOT + '/' + SNAPSHOT + '_' + (id) + ".txt";
        File newFile = new File(newSnapshotFile);

        ObjectMapper writeMapper = new ObjectMapper();
        writeMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        writeMapper.writeValue(newFile, trocadilhoDBRepresentation);

        synchronized (this) {
            deleteFirstFile(id, SNAPSHOT);
        }
    }

    @Synchronized
    public void recoverDB(String snapshotFile) {
        try {
            File file = new File(snapshotFile);
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
            trocadilhoRepository.replaceAll(trocadilhoDBRepresentation);
            this.runLog();
        } catch (IOException ex) {
            System.out.println("Cannot recover DB!");
        }
    }

    @Synchronized
    public void runLog() {
        try {
            Optional<Integer> maxSnapshotId = getMaxFileId(LOG);

            if (maxSnapshotId.isPresent()) {
                String snapshotFile = LOG + '/' + LOG + '_' + maxSnapshotId.get().toString() + ".txt";
                File file = new File(snapshotFile);
                FileReader fr = new FileReader(file);
                BufferedReader br = new BufferedReader(fr);
                while (br.ready()) {
                    String[] content = br.readLine().replaceAll("\"", "").split(";");
                    OperationTypeEnum option = OperationTypeEnum.valueOf(content[0]);
                    String code = content[1];
                    String username = content[2];
                    String trocadilho = content[3];


                    if (option.equals(CREATE)) {
                        trocadilhoRepository.create(
                                CreateTrocadilhoRequest.newBuilder()
                                        .setCode(code)
                                        .setUsername(username)
                                        .setTrocadilho(trocadilho)
                                        .build()
                        );
                    } else if (option.equals(UPDATE)) {
                        trocadilhoRepository.update(
                                UpdateTrocadilhoRequest.newBuilder()
                                        .setCode(code)
                                        .setTrocadilho(trocadilho)
                                        .build()
                        );
                    }
                    else if (option.equals(DELETE)) {
                        trocadilhoRepository.deleteById(
                                DeleteTrocadilhoRequest.newBuilder()
                                        .setCode(code)
                                        .build()
                        );
                    }

                }
                this.createSnapshot(id);
                this.createLog(id);
                this.id++;
            }
        } catch (IOException ex) {
            System.out.println("Cannot execute log.");
        }
    }

    private Optional<Integer> getMaxFileId(String fileType) {
        File directory = new File(fileType);
        File[] files = directory.listFiles();
        List<File> filesList = new ArrayList<>();
        if (files != null && files.length != 0)
            filesList = Arrays.asList(files);
        return filesList.stream()
                .filter(file -> file.getName().contains("log"))
                .map(file -> file.getName().split("_")[1])
                .map(file -> Integer.parseInt(file.split(".t")[0]))
                .max(Comparator.naturalOrder());
    }

}
