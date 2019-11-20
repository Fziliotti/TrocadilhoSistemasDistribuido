package trocadilho;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import trocadilho.service.LoginServiceImpl;
import trocadilho.service.TrocadilhoServiceImpl;

import java.io.IOException;
import java.net.ServerSocket;

public class ServerGRPC {

    public static void main(String[] args) throws IOException {

        try{
            Server server = ServerBuilder.forPort(8080)
                    .addService(new LoginServiceImpl())
                    .addService(new TrocadilhoServiceImpl())
                    .build();

            System.out.println("Starting server...");
            server.start();



            log.info("Servidor iniciado na porta 12345");


        } catch (IOException ex) {
            log.info("Erro de conexão");
        }

        server.close();
    }


    public void login(String name, String password) {

    }
}
