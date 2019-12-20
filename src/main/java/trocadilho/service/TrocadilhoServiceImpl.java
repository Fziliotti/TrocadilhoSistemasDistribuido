package trocadilho.service;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import trocadilho.*;
import trocadilho.domain.Trocadilho;

import java.io.*;
import java.util.*;

import static trocadilho.utils.FileUtils.*;

public class TrocadilhoServiceImpl extends TrocadilhoServiceGrpc.TrocadilhoServiceImplBase implements Serializable {
    public static String LOCALHOST = "localhost";
    public static String SNAPSHOT = "snapshot";
    public static String LOG = "log";

    public TrocadilhoStateServer trocadilhoStateServer = new TrocadilhoStateServer();
    private Integer serverId;
    private String port;
    int serversQuantity;
    int allServersBasePort;

    public TrocadilhoServiceImpl(Integer serverId) {
        this.serversQuantity = getServersQuantity();
        this.serverId = serverId;
        this.allServersBasePort = getBasePort();
        this.port = String.valueOf(serverId + allServersBasePort);
        this.runActualServerState();
    }

    public void runActualServerState() {
        File snapshotDirectory = new File(SNAPSHOT);
        File[] files = snapshotDirectory.listFiles();
        List<File> filesList = new ArrayList<>();
        if (files != null && files.length != 0)
            filesList = Arrays.asList(files);
        Optional<Integer> maxSnapshotId = filesList.stream()
                .filter(file -> file.getName().contains("snapshot"))
                .map(file -> file.getName().split("_")[1])
                .map(file -> Integer.parseInt(file.split(".t")[0]))
                .max(Comparator.naturalOrder());
        if (maxSnapshotId.isPresent()) {
            String snapshotFile = "snapshot/snapshot_" + maxSnapshotId.get().toString() + ".txt";
            trocadilhoStateServer.beginStateControl(maxSnapshotId.get()+1);
            trocadilhoStateServer.recoverDB(snapshotFile);
        } else {
            trocadilhoStateServer.beginStateControl(1);
        }

    }

    private TrocadilhoServiceGrpc.TrocadilhoServiceBlockingStub getBlockingStubByHostAndPort(String host, Integer port) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext(true).build();
        return TrocadilhoServiceGrpc.newBlockingStub(channel);
    }

    private void removeFromOnlineServers(int port) {
        removePortFromOnlineServers(port);
    }

    String getClusterIdResponsibleForThisCode(String code) {
        return String.valueOf(Math.abs(code.hashCode() % serversQuantity));
    }

    private Integer getTheRightPort(String code) {
        List<String> onlineServersByClusterId = new ArrayList<>();

        String responsibleClusterId = getClusterIdResponsibleForThisCode(code);

        try {
            File file = new File("constants.txt");
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            while (br.ready()) {
                String[] line = br.readLine().split(":");
                if (line[1].equals(responsibleClusterId)) {
                    onlineServersByClusterId.add(line[0]);
                }
            }
            br.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return Integer.parseInt(onlineServersByClusterId.get(new Random().nextInt(onlineServersByClusterId.size())));
    }

    private Boolean thisIsTheRightServer(String code) {
        String responsibleClusterId = getClusterIdResponsibleForThisCode(code);

        return responsibleClusterId == this.clusterId;
    }

    @Override
    public void insertTrocadilho(CreateTrocadilhoRequest request, StreamObserver<APIResponse> responseObserver) {
        StringBuilder message = new StringBuilder("");
        if (thisIsTheRightServer(request.getCode())) {
            try {
                trocadilhoStateServer.createTrocadilho(request);
                message.append("OK - Created new trocadilho!");
                System.out.println("Created trocadilho for user: " + request.getUsername());
            } catch (Exception e) {
                e.printStackTrace();
                message.append("Sorry! Cannot create this trocadilho!");
            }
        } else {
            message.append(this.insertTrocadilhoFromRightServer(request));
        }
        APIResponse apiResponse = APIResponse.newBuilder().setMessage(message.toString()).build();
        responseObserver.onNext(apiResponse);
        responseObserver.onCompleted();

    }

    @Override
    public void updateTrocadilho(UpdateTrocadilhoRequest request, StreamObserver<APIResponse> responseObserver) {
        StringBuilder message = new StringBuilder("");
        if (thisIsTheRightServer(request.getCode())) {
            try {
                trocadilhoStateServer.updateTrocadilho(request);
                message.append("OK - Trocadilho updated!");
                System.out.println("Update trocadilho for code: " + request.getCode());
            } catch (Exception e) {
                e.printStackTrace();
                message.append("Sorry! Cannot update this trocadilho!");
            }
        } else {
            message.append(this.updateTrocadilhoByIdFromRightServer(request));
        }
        APIResponse apiResponse = APIResponse.newBuilder().setMessage(message.toString()).build();
        responseObserver.onNext(apiResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteTrocadilho(DeleteTrocadilhoRequest request, StreamObserver<APIResponse> responseObserver) {
        StringBuilder message = new StringBuilder("");
        if (thisIsTheRightServer(request.getCode())) {
            try {
                trocadilhoStateServer.deleteTrocadilho(request);
                message.append("OK - Trocadilho deleted!");
                System.out.println("Delete trocadilho for code: " + request.getCode());
            } catch (Exception e) {
                e.printStackTrace();
                message.append("Sorry! Cannot delete this trocadilho!");
            }
        } else {
            message.append(this.deleteTrocadilhoByIdFromRightServer(request));
        }
        APIResponse apiResponse = APIResponse.newBuilder().setMessage(message.toString()).build();
        responseObserver.onNext(apiResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void listTrocadilhos(GetTrocadilhoRequest request, StreamObserver<APIResponse> responseObserver) {
        StringBuilder message = new StringBuilder("");
        if (thisIsTheRightServer(request.getCode())) {

            List<Trocadilho> base = trocadilhoStateServer.getTrocadilhos();
            base.forEach(trocadilho -> {
                message.append("\nId: ").append(trocadilho.getCode()).append(" --Autor -> ").append(trocadilho.getUsername()).append(" --- ").append(trocadilho.getContent());
            });
        } else {
            message.append(this.listAllFromRightServer(request));
        }
        APIResponse apiResponse = APIResponse.newBuilder().setMessage(message.toString()).build();
        responseObserver.onNext(apiResponse);
        responseObserver.onCompleted();
    }

    public String insertTrocadilhoFromRightServer(CreateTrocadilhoRequest request) {
        String name = request.getUsername();
        int port = getTheRightPort(name);

        TrocadilhoServiceGrpc.TrocadilhoServiceBlockingStub stub = getBlockingStubByHostAndPort(LOCALHOST, port);
        System.out.println("Delegating Create Trocadilho to server with port: " + port);

        APIResponse apiResponse = stub.insertTrocadilho(request);
        return apiResponse.getMessage();
    }

    public String listAllFromRightServer(GetTrocadilhoRequest getTrocadilhoRequest) {
        String name = getTrocadilhoRequest.getName();
        int port = getTheRightPort(name);

        TrocadilhoServiceGrpc.TrocadilhoServiceBlockingStub stub = getBlockingStubByHostAndPort(LOCALHOST, port);
        System.out.println("Delegating List Trocadilhos to server with port: " + port);

        APIResponse apiResponse = stub.listTrocadilhos(getTrocadilhoRequest);
        return apiResponse.getMessage();
    }

    public String deleteTrocadilhoByIdFromRightServer(DeleteTrocadilhoRequest request) {
        String name = request.getCode();
        int port = getTheRightPort(name);

        TrocadilhoServiceGrpc.TrocadilhoServiceBlockingStub stub = getBlockingStubByHostAndPort(LOCALHOST, port);
        System.out.println("Delegating Delete Trocadilho to server with port: " + port);

        APIResponse apiResponse = stub.deleteTrocadilho(request);
        return apiResponse.getMessage();
    }

    public String updateTrocadilhoByIdFromRightServer(UpdateTrocadilhoRequest request) {
        String name = request.getTrocadilho();
        int port = getTheRightPort(name);

        TrocadilhoServiceGrpc.TrocadilhoServiceBlockingStub stub = getBlockingStubByHostAndPort(LOCALHOST, port);
        System.out.println("Delegating Update Trocadilho to server with port: " + port);

        APIResponse apiResponse = stub.updateTrocadilho(request);
        return apiResponse.getMessage();
    }


}
