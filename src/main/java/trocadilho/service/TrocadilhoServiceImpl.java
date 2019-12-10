package trocadilho.service;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import trocadilho.*;
import trocadilho.db.trocadilho.TrocadilhoRepository;
import trocadilho.db.trocadilho.TrocadilhoRepositoryImpl;
import trocadilho.domain.Trocadilho;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TrocadilhoServiceImpl extends TrocadilhoServiceGrpc.TrocadilhoServiceImplBase implements Serializable {
    public static String GREETING_OK = "OK";
    public static String LOCALHOST = "localhost";

    private List<Trocadilho> trocadilhoList = new ArrayList<>();
    private TrocadilhoRepository trocadilhoRepository = new TrocadilhoRepositoryImpl();
    private List<Integer> neighborServers;
    private Integer serverId;
    private String port;
    int serversQuantity;
    int allServersBasePort;

    public TrocadilhoServiceImpl(Integer serverId) {
        this.serversQuantity = ServerGRPC.getServersQuantity();
        this.serverId = serverId;
        this.allServersBasePort = ServerGRPC.getBasePort();
        this.port = String.valueOf(serverId + allServersBasePort);
        this.neighborServers = doGreeting();


    }


    public List<Integer> doGreeting() {
        List<Integer> neighbourServers = new ArrayList<>();
        for (int i = 0; i < serversQuantity; i++) {
            if (i == serverId) continue;

            Integer port = i + allServersBasePort;

            try {
                GreetingRequest greetingRequest = GreetingRequest.newBuilder().setPort(this.port).build();
                TrocadilhoServiceGrpc.TrocadilhoServiceBlockingStub stub = getBlockingStubByHostAndPort(LOCALHOST, port);
                APIResponse apiResponse = stub.doGreeting(greetingRequest);
                if (apiResponse.getMessage().equals(GREETING_OK)) {
                    neighbourServers.add(port);
                }
            } catch (Exception e) {
                System.out.println("Cannot reach server with port: " + port);
                removeFromOnlineServers(port);
            }
        }
        return neighbourServers;
    }

    private TrocadilhoServiceGrpc.TrocadilhoServiceBlockingStub getBlockingStubByHostAndPort(String host, Integer port) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        return TrocadilhoServiceGrpc.newBlockingStub(channel);
    }

    private void removeFromOnlineServers(int port) {
        ServerGRPC.removePortFromOnlineServers(port);
    }

    @Override
    public void doGreeting(GreetingRequest request, StreamObserver<APIResponse> responseObserver) {
        System.out.println("Server with port " + request.getPort() + " connecting.");
        this.neighborServers.add(Integer.parseInt(request.getPort()));
        System.out.println("Updated neighbours: " + this.neighborServers.toString());
        APIResponse apiResponse = APIResponse.newBuilder().setMessage(GREETING_OK).build();
        responseObserver.onNext(apiResponse);
        responseObserver.onCompleted();
    }

    int stringHash(String value) {
        return Math.abs(value.hashCode() % serversQuantity);
    }

    private int getRightPort(String name) {
        int port = stringHash(name) + this.allServersBasePort;

        Optional<Integer> neighbourPort = this.neighborServers.stream().filter(neighbour -> neighbour == port).findFirst();
        return neighbourPort.orElseGet(() -> neighborServers.get(0));
    }

    private Boolean thisIsTheRightServer(String name) {
        int nameHash = stringHash(name);

        if (nameHash == this.serverId) return true;

        List<Integer> allServers = new ArrayList<>(this.neighborServers);
        allServers.add(this.serverId + allServersBasePort);
        Collections.sort(allServers);

        List<Integer> candidateServers = allServers.stream()
                .filter(server -> server % serversQuantity >= nameHash)
                .map(server -> server % serversQuantity)
                .collect(Collectors.toList());

        if (candidateServers.size() == 0) candidateServers.add(allServers.get(0));

        return candidateServers.get(0).equals(this.serverId);
    }

    @Override
    public void insertTrocadilho(TrocadilhoRequest request, StreamObserver<APIResponse> responseObserver) {
        StringBuilder message = new StringBuilder("");
        if (thisIsTheRightServer(request.getUsername())) {
            try {
                trocadilhoRepository.create(request.getTrocadilho(), request.getUsername());
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
        if (thisIsTheRightServer(request.getTrocadilho())) {
            try {
                trocadilhoRepository.update(request.getCode(), request.getTrocadilho());
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
                trocadilhoRepository.deleteById(request.getCode());
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
        if (thisIsTheRightServer(request.getName())) {

            try {
                List<Trocadilho> base = trocadilhoRepository.listAll();
                base.forEach(trocadilho -> {
                    message.append("\nId: ").append(trocadilho.getCode()).append(" --Autor -> ").append(trocadilho.getUsername()).append(" --- ").append(trocadilho.getContent());
                });
            } catch (IOException e) {
                e.printStackTrace();
                message.append("Sorry! Cannot list all trocadilhos.");
            }
        } else {
            message.append(this.listAllFromRightServer(request));
        }
        APIResponse apiResponse = APIResponse.newBuilder().setMessage(message.toString()).build();
        responseObserver.onNext(apiResponse);
        responseObserver.onCompleted();
    }

    public String insertTrocadilhoFromRightServer(TrocadilhoRequest request) {
        String name = request.getUsername();
        int port = getRightPort(name);

        TrocadilhoServiceGrpc.TrocadilhoServiceBlockingStub stub = getBlockingStubByHostAndPort(LOCALHOST, port);
        System.out.println("Delegating Create Trocadilho to server with port: " + port);

        APIResponse apiResponse = stub.insertTrocadilho(request);
        return apiResponse.getMessage();
    }

    public String listAllFromRightServer(GetTrocadilhoRequest getTrocadilhoRequest) {
        String name = getTrocadilhoRequest.getName();
        int port = getRightPort(name);

        TrocadilhoServiceGrpc.TrocadilhoServiceBlockingStub stub = getBlockingStubByHostAndPort(LOCALHOST, port);
        System.out.println("Delegating List Trocadilhos to server with port: " + port);

        APIResponse apiResponse = stub.listTrocadilhos(getTrocadilhoRequest);
        return apiResponse.getMessage();
    }

    public String deleteTrocadilhoByIdFromRightServer(DeleteTrocadilhoRequest request) {
        String name = request.getCode();
        int port = getRightPort(name);

        TrocadilhoServiceGrpc.TrocadilhoServiceBlockingStub stub = getBlockingStubByHostAndPort(LOCALHOST, port);
        System.out.println("Delegating Delete Trocadilho to server with port: " + port);

        APIResponse apiResponse = stub.deleteTrocadilho(request);
        return apiResponse.getMessage();
    }

    public String updateTrocadilhoByIdFromRightServer(UpdateTrocadilhoRequest request) {
        String name = request.getTrocadilho();
        int port = getRightPort(name);

        TrocadilhoServiceGrpc.TrocadilhoServiceBlockingStub stub = getBlockingStubByHostAndPort(LOCALHOST, port);
        System.out.println("Delegating Update Trocadilho to server with port: " + port);

        APIResponse apiResponse = stub.updateTrocadilho(request);
        return apiResponse.getMessage();
    }


}
