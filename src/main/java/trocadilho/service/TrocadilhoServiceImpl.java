package trocadilho.service;

import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.netty.NettyTransport;
import io.atomix.copycat.client.CopycatClient;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import trocadilho.*;
import trocadilho.command.CreateTrocadilhoCommand;
import trocadilho.command.DeleteTrocadilhoCommand;
import trocadilho.command.FindByCodeQuery;
import trocadilho.command.UpdateTrocadilhoCommand;
import trocadilho.exception.NotFoundException;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

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
    private String clusterId;
    private CopycatClient atomixClient;

    public TrocadilhoServiceImpl(Integer serverId, String clusterId) {
        this.serversQuantity = getServersQuantity();
        this.serverId = serverId;
        this.allServersBasePort = getBasePort();
        this.port = String.valueOf(serverId + allServersBasePort);
        this.runActualServerState();
        this.clusterId = clusterId;
    }


    private void createAtomixClient() {
        if (this.atomixClient == null) {
            List<Address> addresses = new LinkedList<>();
            getClusterPorts(Integer.parseInt(clusterId)).forEach(port1 -> addresses.add(new Address("127.0.0.1", port1 + 1000)));
            CopycatClient.Builder builder = CopycatClient.builder()
                    .withTransport(NettyTransport.builder()
                            .withThreads(4)
                            .build());
            this.atomixClient = builder.build();


            CompletableFuture<CopycatClient> future = this.atomixClient.connect(addresses);
            future.join();
        }
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
            trocadilhoStateServer.beginStateControl(maxSnapshotId.get() + 1);
            trocadilhoStateServer.recoverDB(snapshotFile);
        } else {
            trocadilhoStateServer.beginStateControl(1);
        }

    }

    private TrocadilhoServiceGrpc.TrocadilhoServiceBlockingStub getBlockingStubByHostAndPort(String host, Integer port) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
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
            File file = new File("servers_online.txt");
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
        if (onlineServersByClusterId.size() == 0)
            throw new NotFoundException("Oops, cannot reach any server from a cluster!");
        return Integer.parseInt(onlineServersByClusterId.get(new Random().nextInt(onlineServersByClusterId.size())));
    }

    private Boolean thisIsTheRightServer(String code) {
        String responsibleClusterId = getClusterIdResponsibleForThisCode(code);

        return responsibleClusterId.equals(this.clusterId);
    }

    @Override
    public void insertTrocadilho(CreateTrocadilhoRequest request, StreamObserver<APIResponse> responseObserver) {
        StringBuilder message = new StringBuilder("");
        if (thisIsTheRightServer(request.getCode())) {
            try {
                this.createAtomixClient();
                CompletableFuture<String> submit = this.atomixClient.submit(new CreateTrocadilhoCommand(request.getCode(), request.getUsername(), request.getTrocadilho()));
                message.append(submit.get());
            } catch (Exception e) {
                e.printStackTrace();
                message.append("Sorry! Cannot create this trocadilho!");
            }
        } else {
            Integer rightPort = this.getTheRightPort(request.getCode());
            try {
                message.append(this.insertTrocadilhoFromRightServer(request));
            } catch (Exception ex) {
                removeFromOnlineServers(rightPort);
                message.append(this.insertTrocadilhoFromRightServer(request));
            }
        }
        APIResponse apiResponse = APIResponse.newBuilder().setMessage(message.toString()).build();
        responseObserver.onNext(apiResponse);
        responseObserver.onCompleted();

    }

    @Override
    public void getTrocadilho(GetTrocadilhoRequest request, StreamObserver<TrocadilhoResponse> responseObserver) {
        StringBuilder message = new StringBuilder("");
        if (thisIsTheRightServer(request.getCode())) {
            try {
                this.createAtomixClient();
                CompletableFuture<String> submit = this.atomixClient.submit(new FindByCodeQuery(request.getCode()));
                message.append(submit.get());
            } catch (Exception e) {
                e.printStackTrace();
                message.append("Sorry! Trocadilho not found!");
            }
        } else {
            Integer rightPort = this.getTheRightPort(request.getCode());
            try {
                message.append(this.findByCodeFromRightServer(request));
            } catch (Exception ex) {
                removeFromOnlineServers(rightPort);
                message.append(this.findByCodeFromRightServer(request));
            }
        }
        TrocadilhoResponse trocadilhoResponse = TrocadilhoResponse.newBuilder().setMessage(message.toString()).build();
        responseObserver.onNext(trocadilhoResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void updateTrocadilho(UpdateTrocadilhoRequest request, StreamObserver<APIResponse> responseObserver) {
        StringBuilder message = new StringBuilder("");
        if (thisIsTheRightServer(request.getCode())) {
            try {
                this.createAtomixClient();
                CompletableFuture<String> submit = this.atomixClient.submit(new UpdateTrocadilhoCommand(request.getCode(), request.getTrocadilho()));
                message.append(submit.get());
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
                this.createAtomixClient();
                CompletableFuture<String> submit = this.atomixClient.submit(new DeleteTrocadilhoCommand(request.getCode()));
                message.append(submit.get());
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

    public String insertTrocadilhoFromRightServer(CreateTrocadilhoRequest request) {
        String name = request.getCode();
        for (int i = 0; i < getClusterSize(); i++) {
            int port = getTheRightPort(name);
            try {
                TrocadilhoServiceGrpc.TrocadilhoServiceBlockingStub stub = getBlockingStubByHostAndPort(LOCALHOST, port);
                System.out.println("Delegating Create Trocadilho to server with port: " + port);
                APIResponse apiResponse = stub.insertTrocadilho(request);
                return apiResponse.getMessage();
            } catch (Exception ex) {
                System.out.println("Oops, failed delegation!");
                removeFromOnlineServers(port);
            }
        }
        return "Sorry! Cannot insert trocadilho!";
    }

    public String findByCodeFromRightServer(GetTrocadilhoRequest request) {
        String code = request.getCode();
        for (int i = 0; i < getClusterSize(); i++) {
            int port = getTheRightPort(code);
            try {
                TrocadilhoServiceGrpc.TrocadilhoServiceBlockingStub stub = getBlockingStubByHostAndPort(LOCALHOST, port);
                System.out.println("Delegating Find By Code to server with port: " + port);
                TrocadilhoResponse trocadilhoResponse = stub.getTrocadilho(request);
                return trocadilhoResponse.getMessage();
            } catch (Exception ex) {
                System.out.println("Oops, failed delegation!");
                removeFromOnlineServers(port);
            }
        }
        return "Sorry! Cannot insert trocadilho!";
    }

    public String deleteTrocadilhoByIdFromRightServer(DeleteTrocadilhoRequest request) {
        String name = request.getCode();
        for (int i = 0; i < getClusterSize(); i++) {
            int port = getTheRightPort(name);
            try {
                TrocadilhoServiceGrpc.TrocadilhoServiceBlockingStub stub = getBlockingStubByHostAndPort(LOCALHOST, port);
                System.out.println("Delegating Delete Trocadilho to server with port: " + port);
                APIResponse apiResponse = stub.deleteTrocadilho(request);
                return apiResponse.getMessage();
            } catch (Exception ex) {
                System.out.println("Oops, failed delegation!");
                removeFromOnlineServers(port);
            }
        }
        return "Sorry! Cannot delete trocadilho!";
    }

    public String updateTrocadilhoByIdFromRightServer(UpdateTrocadilhoRequest request) {
        String name = request.getTrocadilho();
        for (int i = 0; i < getClusterSize(); i++) {
            int port = getTheRightPort(name);
            try {
                TrocadilhoServiceGrpc.TrocadilhoServiceBlockingStub stub = getBlockingStubByHostAndPort(LOCALHOST, port);
                System.out.println("Delegating Update Trocadilho to server with port: " + port);
                APIResponse apiResponse = stub.updateTrocadilho(request);
                return apiResponse.getMessage();
            } catch (Exception ex) {
                System.out.println("Oops, failed delegation!");
                removeFromOnlineServers(port);
            }
        }
        return "Sorry! Cannot update trocadilho!";
    }


}
