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

import static trocadilho.utils.FileUtils.*;

public class ServerGRPC {
    public static String SERVERS_QUANTITY = "SERVERS_QUANTITY";
    public static String BASE_PORT = "BASE_PORT";
    public static String INTERVAL_TO_SNAPSHOT = "INTERVAL_TO_SNAPSHOT";

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

    }







}
