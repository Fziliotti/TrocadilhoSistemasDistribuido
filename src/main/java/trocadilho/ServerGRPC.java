package trocadilho;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import trocadilho.service.TrocadilhoServiceImpl;
import trocadilho.service.LoginServiceImpl;

import java.io.IOException;

public class ServerGRPC {

    public static void main(String[] args) throws IOException {


//        Server server = ServerBuilder.forPort(8080)
//                .addService(new LoginServiceImpl())
//                .addService(new TrocadilhoServiceImpl())
//                .build();
//
//
//        System.out.println("Starting server...");
//        server.start();
//
//
//        System.out.println("Server started!");
//        server.awaitTermination();
    }


    public void login(String name, String password) {

    }
}
