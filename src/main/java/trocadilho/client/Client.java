package trocadilho.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import trocadilho.*;

import java.util.Random;
import java.util.Scanner;

import static trocadilho.service.TrocadilhoServiceImpl.LOCALHOST;
import static trocadilho.utils.FileUtils.getClusterSize;
import static trocadilho.utils.FileUtils.getServersQuantity;

public class Client {

    public static String LIST_ALL = "LIST_ALL";
    private TrocadilhoServiceGrpc.TrocadilhoServiceBlockingStub blockingStub;
    private final ManagedChannel channel;
    private Scanner sc;

    public Client(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext().build());
    }

    private Client(ManagedChannel channel) {
        this.channel = channel;
        blockingStub = TrocadilhoServiceGrpc.newBlockingStub(channel);
    }


    public static void main(String[] args) {
        Random random = new Random();
        int port = random.nextInt((7000 + (getServersQuantity() * getClusterSize())) - 7000) + 7000;
        Client client = new Client(LOCALHOST, port);
        client.sc = new Scanner(System.in);
        client.run();
    }


    private void run() {

        while (true) {

            System.out.println("\n\n--------Bem vindo ao Rei dos Trocadilhos.--------\n" +
                    "1(listar)  - Buscar um trocadilho pelo código\n" +
                    "2(criar)   - Criar um trocadilho\n" +
                    "3(editar)  - Editar um trocadilho\n" +
                    "4(deletar) - Deletar um trocadilho\n" +
                    "9(sair)    - Sair\n" +
                    "\nDigite o número ou escreva a opção desejada: ");
            String option = this.sc.next();
            if (option == null) System.out.println("Opção inválida!");

            else if (option.equals("1") || option.toLowerCase().equals("buscar")) {
                this.findByCode();
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

    private void findByCode() {
        System.out.println("Digite o ID do trocadilho: ");
        String code = sc.next();
        GetTrocadilhoRequest getTrocadilhoRequest = GetTrocadilhoRequest.newBuilder().setCode(code).build();
        TrocadilhoResponse trocadilhoResponse = blockingStub.getTrocadilho(getTrocadilhoRequest);
        System.out.println(trocadilhoResponse.getMessage());
    }

    private void create() {
        System.out.println("Digite o codigo do trocadilho: ");
        String code = sc.next();
        System.out.println("Digite seu nome ou nickname: ");
        String username = sc.next();
        System.out.println("Agora pode escrever o trocadilho:");
        String trocadilho = sc.next();
        CreateTrocadilhoRequest trocadilhoRequest = CreateTrocadilhoRequest.newBuilder().setCode(code).setUsername(username).setTrocadilho(trocadilho).build();
        APIResponse apiResponse = blockingStub.insertTrocadilho(trocadilhoRequest);
        System.out.println(apiResponse.getMessage());
    }

    private void update() {
        System.out.println("Digite o ID do trocadilho: ");
        String username = sc.next();
        System.out.println("Agora pode escrever o novo trocadilho:");
        String trocadilho = sc.next();
        UpdateTrocadilhoRequest updateTrocadilhoRequest = UpdateTrocadilhoRequest.newBuilder().setCode(username).setTrocadilho(trocadilho).build();
        APIResponse apiResponse = blockingStub.updateTrocadilho(updateTrocadilhoRequest);
        System.out.println(apiResponse.getMessage());
    }

    private void delete() {
        System.out.println("Digite o ID do trocadilho: ");
        String username = sc.next();
        DeleteTrocadilhoRequest deleteTrocadilhoRequest = DeleteTrocadilhoRequest.newBuilder().setCode(username).build();
        APIResponse apiResponse = blockingStub.deleteTrocadilho(deleteTrocadilhoRequest);
        System.out.println(apiResponse.getMessage());
    }

    private void quit() {
        this.channel.shutdown();
        System.out.println("Desconectando...");
        System.out.println("Até a próxima!");
    }


}
