package trocadilho.server;

import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.netty.NettyTransport;
import io.atomix.copycat.server.CopycatServer;
import io.atomix.copycat.server.storage.Storage;
import io.atomix.copycat.server.storage.StorageLevel;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static trocadilho.utils.FileUtils.*;

public class ServerGRPC {
    public static String SERVERS_QUANTITY = "SERVERS_QUANTITY";
    public static String CLUSTER_SIZE = "CLUSTER_SIZE";
    public static String BASE_PORT = "BASE_PORT";
    public static String INTERVAL_TO_SNAPSHOT = "INTERVAL_TO_SNAPSHOT";

    public static void main(String[] args) throws IOException, InterruptedException {

        int port = getAvailablePort();
        if (port == -1) {
            System.out.println("Todos os servidores já estão funcionando.");
            return;
        }
        int myId = (port - getBasePort()) % getClusterSize();
        int clusterId = (port - getBasePort()) / getClusterSize();
        startGrpcServer(port, myId, String.valueOf(clusterId));
        List<Address> addresses = new LinkedList<>();
        getClusterPorts(clusterId).forEach(port1 -> addresses.add(new Address("localhost", port1 + 1000)));

        addPortIntoOnlineServers(String.valueOf(port), String.valueOf(clusterId));
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
            System.out.println("Starting Atomix server [" + myId + "] on port " + port);
            server.bootstrap().join();

        } else {
            System.out.println("Starting Atomix server [" + myId + "] on port " + port);
            server.join(addresses).join();
        }
    }




}
