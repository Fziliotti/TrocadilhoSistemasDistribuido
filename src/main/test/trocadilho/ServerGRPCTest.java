package trocadilho;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import trocadilho.client.Client;
import trocadilho.server.ServerGRPC;

import java.io.InputStream;

class ServerGRPCTest {

    private InputStream sysInBackup;
    @BeforeEach
    void setup(){
        sysInBackup = System.in;
    }

    @Test
    void main() {
        ServerGRPC.main(new String[]{});
        ServerGRPC.main(new String[]{});
        ServerGRPC.main(new String[]{});
        ServerGRPC.main(new String[]{});
        ServerGRPC.main(new String[]{});
        ServerGRPC.main(new String[]{});
        ServerGRPC.main(new String[]{});
        ServerGRPC.main(new String[]{});
        ServerGRPC.main(new String[]{});

        Client.main(new String[]{});

//        ByteArrayInputStream in = new ByteArrayInputStream("Criar");
//        System.setIn(in);
    }

    @AfterEach
    void cleanUp(){
        System.setIn(sysInBackup);
    }
}