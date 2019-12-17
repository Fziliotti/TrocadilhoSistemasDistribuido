package trocadilho.server;

import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.netty.NettyTransport;
import io.atomix.copycat.server.CopycatServer;
import io.atomix.copycat.server.cluster.Member;
import io.atomix.copycat.server.storage.Storage;
import io.atomix.copycat.server.storage.StorageLevel;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import trocadilho.service.TrocadilhoServiceImpl;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class ServerGRPC {
    public static String SERVERS_QUANTITY = "SERVERS_QUANTITY";
    public static String BASE_PORT = "BASE_PORT";
    public static String INTERVAL_TO_SNAPSHOT = "INTERVAL_TO_SNAPSHOT";
    public static String INTERVAL_TO_DB = "INTERVAL_TO_DB";

    public static void main(String[] args) throws IOException, InterruptedException {

        int port = getAvailablePort();
        if (port == -1) {
            System.out.println("Todos os servidores já estão funcionando.");
            return;
        }
        int myId = port - getBasePort();
        List<Address> addresses = new LinkedList<>();
        getPorts().forEach(port1 -> addresses.add(new Address("localhost", port1)));

        addPortIntoOnlineServers(port);
        CopycatServer.Builder builder = CopycatServer.builder(addresses.get(myId))
                .withStateMachine(TrocadilhosStateMachine::new)
                .withTransport(NettyTransport.builder()
                        .withThreads(4)
                        .build())
                .withStorage(Storage.builder()
                        .withDirectory(new File("logs_" + myId)) //Must be unique
                        .withStorageLevel(StorageLevel.DISK)
                        .build());
        CopycatServer server = builder.build();
        if (myId == 0) {
            server.bootstrap().join();

        } else {
            server.join(addresses).join();
        }


//        Server grpcServer = ServerBuilder.forPort(port)
//                .addService(new TrocadilhoServiceImpl(port - getBasePort()))
//                .build();
//
//        System.out.println("Starting grpcServer...");
//        grpcServer.start();
//        System.out.println("Server started on port " + (port));
//        grpcServer.awaitTermination();

    }

    public static int getServersQuantity() {
        try {
            File file = new File("constants.txt");
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            while (br.ready()) {
                String[] line = br.readLine().split("=");
                if (line[0].equals(SERVERS_QUANTITY))
                    return Integer.parseInt(line[1]);
            }
            br.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return 1;
    }

    public static List<Integer> getPorts() {
        List<Integer> ports = new ArrayList<>();
        int serversQuantity = getServersQuantity();
        for (int i = 0; i < serversQuantity; i++) {
            ports.add(i + getBasePort());
        }
        return ports;
    }

    public static Integer getBasePort() {
        try {
            File file = new File("constants.txt");
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            while (br.ready()) {
                String[] line = br.readLine().split("=");
                if (line[0].equals(BASE_PORT))
                    return Integer.parseInt(line[1]);
            }
            br.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return 7000;
    }

    public static Integer getIntervalToSnapshot() {
        try {
            File file = new File("constants.txt");
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            while (br.ready()) {
                String[] line = br.readLine().split("=");
                if (line[0].equals(INTERVAL_TO_SNAPSHOT))
                    return Integer.parseInt(line[1]);
            }
            br.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return 7000;
    }

    public static Integer getIntervalToDb() {
        try {
            File file = new File("constants.txt");
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            while (br.ready()) {
                String[] line = br.readLine().split("=");
                if (line[0].equals(INTERVAL_TO_DB))
                    return Integer.parseInt(line[1]);
            }
            br.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return 7000;
    }


    private static int getAvailablePort() {
        List<Integer> ports = getPorts();
        try {
            File file = new File("servers_online.txt");
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            while (br.ready()) {
                String[] line = br.readLine().split(",");
                List<String> onlinePorts = Arrays.asList(line);
                Optional<Integer> port = ports.stream().filter(fport -> !onlinePorts.contains(fport.toString())).findFirst();
                return port.orElse(-1);
            }
            br.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return 7000;
    }

    public static void addPortIntoOnlineServers(int port) {
        List<Integer> ports = getPorts();
        try {
            File file = new File("servers_online.txt");
            FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.append(String.valueOf(port));
            bw.append(',');
            bw.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    public static void removePortFromOnlineServers(int port) {
        List<Integer> ports = getPorts();
        try {
            File file = new File("servers_online.txt");
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            while (br.ready()) {
                String[] line = br.readLine().split(",");
                List<String> onlinePorts = Arrays.asList(line);
                List<String> newOnlineServers = onlinePorts.stream().filter(onlinePort -> !onlinePort.equals(String.valueOf(port))).collect(Collectors.toList());
                if (newOnlineServers.size() != onlinePorts.size()) {
                    FileWriter fw = new FileWriter(file);
                    BufferedWriter bw = new BufferedWriter(fw);
                    StringBuilder newOnlineServersString = new StringBuilder(",.");
                    newOnlineServers.forEach(newOnlineServer -> newOnlineServersString.append(newOnlineServer).append(","));
                    newOnlineServersString.deleteCharAt(newOnlineServersString.length() - 1);
                    bw.write(newOnlineServersString.toString());
                }
            }
            br.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
