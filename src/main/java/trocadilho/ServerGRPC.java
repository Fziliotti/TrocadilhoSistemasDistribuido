package trocadilho;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class ServerGRPC {

    public static void main(String[] args) throws IOException {

        try{
//            Server server = ServerBuilder.forPort(8080)
//                    .addService(new TrocadilhosServiceImpl()).build();

            System.out.println("Starting server...");
            server.start();


        } catch (IOException ex) {
            System.out.println("Erro de conexão!");
        }

        System.out.println("Server started!");
        server.awaitTermination();
    }


    public void login(String name, String password) {

    }
}
