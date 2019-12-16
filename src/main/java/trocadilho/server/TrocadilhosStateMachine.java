package trocadilho.server;

import io.atomix.copycat.server.Commit;
import io.atomix.copycat.server.StateMachine;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import trocadilho.APIResponse;
import trocadilho.GetTrocadilhoRequest;
import trocadilho.TrocadilhoServiceGrpc;
import trocadilho.command.ListTrocadilhosQuery;
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
        ListTrocadilhosQuery operation = commit.operation();
        generatePort();
        startGrpcServer();
        setBlockingStub();

        GetTrocadilhoRequest getTrocadilhoRequest = GetTrocadilhoRequest.newBuilder().setName(LIST_ALL).build();
        APIResponse apiResponse = blockingStub.listTrocadilhos(getTrocadilhoRequest);
        return apiResponse.getMessage();
    }

    private void setBlockingStub() {
        if (blockingStub == null) {
            ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", grpcPort).usePlaintext(true).build();
            blockingStub = TrocadilhoServiceGrpc.newBlockingStub(channel);
        }
    }

    private void startGrpcServer() throws IOException, InterruptedException {
        if(grpcServer == null) {
            grpcServer = ServerBuilder.forPort(grpcPort)
                    .addService(new TrocadilhoServiceImpl(grpcPort))
                    .build();
            System.out.println("Starting grpcServer...");
            grpcServer.start();
            System.out.println("Server started on port " + (grpcPort));
            grpcServer.awaitTermination();
        }
    }

    private void generatePort() {
        Random random = new Random();
        this.grpcPort = random.nextInt((40000) - 10000) + 10000;
    }

}