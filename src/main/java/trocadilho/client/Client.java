package trocadilho.client;

import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.netty.NettyTransport;
import io.atomix.copycat.client.CopycatClient;
import trocadilho.*;
import trocadilho.command.CreateTrocadilhoCommand;
import trocadilho.command.DeleteTrocadilhoCommand;
import trocadilho.command.ListTrocadilhosQuery;
import trocadilho.command.UpdateTrocadilhoCommand;
import trocadilho.server.ServerGRPC;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public class Client {

    public static String LIST_ALL = "LIST_ALL";
    private TrocadilhoServiceGrpc.TrocadilhoServiceBlockingStub blockingStub;
    private Boolean loggined = false;
    private CopycatClient copycatClient;


    public Client() {
    }



    public static void main(String[] args) {


        Random random = new Random();
        int port = random.nextInt((7000 + ServerGRPC.getServersQuantity()) - 7000) + 7000;

        Client client = new Client();
        List<Address> addresses = new LinkedList<>();

        CopycatClient.Builder builder = CopycatClient.builder()
                .withTransport( NettyTransport.builder()
                        .withThreads(4)
                        .build());
        client.copycatClient = builder.build();

        addresses.add(new Address("localhost", port));
        CompletableFuture<CopycatClient> future = client.copycatClient.connect(addresses);
        future.join();
        client.run();
    }


    private void run() {

        while (true) {

            System.out.println("\n\n--------Bem vindo ao Rei dos Trocadilhos.--------\n" +
                    "1(listar)  - Listar todos os trocadilhos\n" +
                    "2(criar)   - Criar um trocadilho\n" +
                    "3(editar)  - Editar um trocadilho\n" +
                    "4(deletar) - Deletar um trocadilho\n" +
                    "9(sair)    - Sair\n" +
                    "\nDigite o número ou escreva a opção desejada: ");
            Scanner sc = new Scanner(System.in);
            String option = sc.nextLine();
            if (option == null) System.out.println("Opção inválida!");

            else if (option.equals("1") || option.toLowerCase().equals("listar")) {
                this.listAll();
            } else if (option.equals("2") || option.toLowerCase().equals("criar")) {
                this.create();
            } else if (option.equals("3") || option.toLowerCase().equals("editar")) {
                this.update();
            } else if (option.equals("4") || option.toLowerCase().equals("deletar")) {
                this.delete();
            } else if (option.equals("9") || option.toLowerCase().equals("sair")) {
                this.quit();
                return;
            } else System.out.println("Opção inválida!");
        }
    }

    private void listAll() {
        CompletableFuture<String> future = copycatClient.submit(new ListTrocadilhosQuery("123", 1));
        String apiResponse = null;
        try {
            apiResponse = future.get();
            System.out.println(apiResponse);
        } catch (Exception e) {
            System.out.println("Não foi possivel exibir os trocadilhos.");
        }
//        GetTrocadilhoRequest getTrocadilhoRequest = GetTrocadilhoRequest.newBuilder().setName(LIST_ALL).build();
//        APIResponse apiResponse = blockingStub.listTrocadilhos(getTrocadilhoRequest);

    }

    private void create() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Digite seu nome ou nickname: ");
        String username = sc.nextLine();
        System.out.println("Agora pode escrever o trocadilho:");
        String trocadilho = sc.nextLine();
        CompletableFuture<String> future = copycatClient.submit(new CreateTrocadilhoCommand(username, trocadilho));
        String apiResponse = null;
        try {
            apiResponse = future.get();
            System.out.println(apiResponse);
        } catch (Exception e) {
            System.out.println("Não foi possivel criar o trocadilho.");
        }
    }

    private void update() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Digite o ID do trocadilho: ");
        String code = sc.nextLine();
        System.out.println("Agora pode escrever o novo trocadilho:");
        String trocadilho = sc.nextLine();
        CompletableFuture<String> future = copycatClient.submit(new UpdateTrocadilhoCommand(code, trocadilho));
        String apiResponse = null;
        try {
            apiResponse = future.get();
            System.out.println(apiResponse);
        } catch (Exception e) {
            System.out.println("Não foi possivel atualizar o trocadilho.");
        }
    }

    private void delete() {

        Scanner sc = new Scanner(System.in);
        System.out.println("Digite o ID do trocadilho: ");
        String code = sc.nextLine();
        CompletableFuture<String> future = copycatClient.submit(new DeleteTrocadilhoCommand(code));
        String apiResponse = null;
        try {
            apiResponse = future.get();
            System.out.println(apiResponse);
        } catch (Exception e) {
            System.out.println("Não foi possivel deletar o trocadilho.");
        }
    }

    private void quit() {
        System.out.println("Desconectando...");
        System.out.println("Até a próxima!");
    }


}
