package trocadilho.utils;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import trocadilho.service.TrocadilhoServiceImpl;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static trocadilho.server.ServerGRPC.*;

public class FileUtils {



    public FileUtils() {}


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
        int serversQuantity = getServersQuantity() * getClusterSize();
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


    public static int getAvailablePort() {
        List<Integer> ports = getPorts();
        try {
            File file = new File("servers_online.txt");
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            List<String> lines = new ArrayList<>();
            while (br.ready()) {
                lines.add(br.readLine());
            }
            br.close();
            List<String> onlinePorts = lines.stream().map(line -> line.split(":")[0]).collect(Collectors.toList());
            Optional<Integer> port = ports.stream().filter(fport -> !onlinePorts.contains(fport.toString())).findFirst();
            return port.orElse(-1);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return 7000;
    }

    public static void addPortIntoOnlineServers(String port, String clusterId) {
        try {
            File file = new File("servers_online.txt");
            FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.append(port);
            bw.append(':');
            bw.append(clusterId);
            bw.append('\n');
            bw.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void removePortFromOnlineServers(int port) {
        try {
            File file = new File("servers_online.txt");
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            List<String> lines = new ArrayList<>();
            while (br.ready()) {
                lines.add(br.readLine());
            }
            List<String> onlinePorts = lines.stream().map(line -> line.split(":")[0]).collect(Collectors.toList());
            List<String> newOnlineServers = onlinePorts.stream().filter(onlinePort -> !onlinePort.equals(String.valueOf(port))).collect(Collectors.toList());
            if (newOnlineServers.size() != onlinePorts.size()) {
                FileWriter fw = new FileWriter(file);
                BufferedWriter bw = new BufferedWriter(fw);
                StringBuilder newOnlineServersString = new StringBuilder(",.");
                newOnlineServers.forEach(newOnlineServer -> newOnlineServersString.append(newOnlineServer).append(","));
                newOnlineServersString.deleteCharAt(newOnlineServersString.length() - 1);
                bw.write(newOnlineServersString.toString());
            }

            br.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    public static void startGrpcServer(int port, int myId, String clusterId) {
        new Thread(() -> {
            Server grpcServer = ServerBuilder.forPort(port)
                    .addService(new TrocadilhoServiceImpl(port - getBasePort(), clusterId ))
                    .build();

            System.out.println("Starting grpcServer...");
            try {
                grpcServer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Server [" + myId + "] started on port " + (port));
            try {
                grpcServer.awaitTermination();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static int getClusterSize() {
        try {
            File file = new File("constants.txt");
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            while (br.ready()) {
                String[] line = br.readLine().split("=");
                if (line[0].equals(CLUSTER_SIZE))
                    return Integer.parseInt(line[1]);
            }
            br.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return 1;
    }

    public static List<Integer> getClusterOnlinePorts(String clusterId) {
        List<Integer> ports = new ArrayList<>();
        try {
            File file = new File("servers_online.txt");
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            while (br.ready()) {
                String[] line = br.readLine().split(":");

                if (line[1].equals(clusterId)) {
                    ports.add(Integer.parseInt(line[0]));
                }
            }
            br.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return ports;

    }

    public static List<Integer> getClusterPorts(Integer clusterId) {
        List<Integer> ports = new ArrayList<>();
        Integer clusterSize = getClusterSize();
        Integer basePort = getBasePort();
        for (Integer i = 0; i < clusterSize; i++) {
            ports.add(basePort + (clusterId * clusterSize) + i);
        }
        return ports;
    }

}
