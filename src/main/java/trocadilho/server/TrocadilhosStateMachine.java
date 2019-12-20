package trocadilho.server;

import io.atomix.copycat.server.Commit;
import io.atomix.copycat.server.StateMachine;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import trocadilho.*;
import trocadilho.command.CreateTrocadilhoCommand;
import trocadilho.command.DeleteTrocadilhoCommand;
import trocadilho.command.ListTrocadilhosQuery;
import trocadilho.command.UpdateTrocadilhoCommand;
import trocadilho.service.TrocadilhoServiceImpl;

import java.io.IOException;
import java.util.Random;

import static trocadilho.server.ServerGRPC.getBasePort;

public class TrocadilhosStateMachine extends StateMachine {

    public static String LIST_ALL = "LIST_ALL";

    private TrocadilhoServiceGrpc.TrocadilhoServiceBlockingStub blockingStub = null;
    private Server grpcServer = null;
    private ManagedChannel channel = null;
    private int grpcPort;

    public String listTrocadilhos(Commit<ListTrocadilhosQuery> commit) throws IOException, InterruptedException {
        try {
            ListTrocadilhosQuery operation = commit.operation();
            generatePort();
            startGrpcServer();
            setBlockingStub();

            GetTrocadilhoRequest getTrocadilhoRequest = GetTrocadilhoRequest.newBuilder().setName(LIST_ALL).build();
            APIResponse apiResponse = blockingStub.listTrocadilhos(getTrocadilhoRequest);
            return apiResponse.getMessage();
        } finally {
            commit.close();
        }
    }

    public String updateTrocadilho(Commit<UpdateTrocadilhoCommand> commit) throws IOException, InterruptedException {
        try {
            UpdateTrocadilhoCommand operation = commit.operation();
            generatePort();
            startGrpcServer();
            setBlockingStub();

            UpdateTrocadilhoRequest updateTrocadilhoRequest = UpdateTrocadilhoRequest.newBuilder().setCode(operation.code).setTrocadilho(operation.content).build();
            APIResponse apiResponse = blockingStub.updateTrocadilho(updateTrocadilhoRequest);
            return apiResponse.getMessage();
        } finally {
            commit.close();
        }
    }

    public String deleteTrocadilho(Commit<DeleteTrocadilhoCommand> commit) throws IOException, InterruptedException {
        try {
            DeleteTrocadilhoCommand operation = commit.operation();
            generatePort();
            startGrpcServer();
            setBlockingStub();

            DeleteTrocadilhoRequest deleteTrocadilhoRequest = DeleteTrocadilhoRequest.newBuilder().setCode(operation.code).build();
            APIResponse apiResponse = blockingStub.deleteTrocadilho(deleteTrocadilhoRequest);
            return apiResponse.getMessage();
        } finally {
            commit.close();
        }
    }

    public String createTrocadilho(Commit<CreateTrocadilhoCommand> commit) throws IOException, InterruptedException {
        try {
            CreateTrocadilhoCommand operation = commit.operation();
            generatePort();
            startGrpcServer();
            setBlockingStub();

            CreateTrocadilhoRequest trocadilhoRequest = CreateTrocadilhoRequest.newBuilder().setUsername(operation.username).setTrocadilho(operation.content).build();
            APIResponse apiResponse = blockingStub.insertTrocadilho(trocadilhoRequest);
            return apiResponse.getMessage();
        } finally {
            commit.close();
        }
    }

    private void setBlockingStub() {
        if (blockingStub == null) {
            ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", grpcPort).usePlaintext().build();
            blockingStub = TrocadilhoServiceGrpc.newBlockingStub(channel);
        }
    }

    private void startGrpcServer() {

        new Thread(() -> {
            if (grpcServer == null) {
                grpcServer = ServerBuilder.forPort(grpcPort)
                        .addService(new TrocadilhoServiceImpl(grpcPort))
                        .build();
                System.out.println("Starting grpcServer...");
                try {
                    grpcServer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ServerGRPC.addPortIntoOnlineServers(grpcPort);
                System.out.println("Server started on port " + (grpcPort));
                try {
                    grpcServer.awaitTermination();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ServerGRPC.removePortFromOnlineServers(grpcPort);
            }
        }).start();
    }

    private void generatePort() {
        Random random = new Random();
        this.grpcPort = random.nextInt((40000) - 10000) + 10000;
    }

}