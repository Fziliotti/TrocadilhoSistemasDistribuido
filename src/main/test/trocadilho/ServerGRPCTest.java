package trocadilho;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import trocadilho.client.Client;
import trocadilho.server.ServerGRPC;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

class ServerGRPCTest {

    private final InputStream systemIn = System.in;
    private final PrintStream systemOut = System.out;

    private ByteArrayInputStream testIn;
    private ByteArrayOutputStream testOut;

    @BeforeEach
    void setup(){
        testOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(testOut));
    }

    @Test
    void success() {
        System.setIn(systemIn);
        System.setOut(systemOut);
        ServerGRPC.main(new String[]{});
        ServerGRPC.main(new String[]{});
        ServerGRPC.main(new String[]{});
        ServerGRPC.main(new String[]{});
        ServerGRPC.main(new String[]{});
        ServerGRPC.main(new String[]{});
        ServerGRPC.main(new String[]{});
        ServerGRPC.main(new String[]{});
        ServerGRPC.main(new String[]{});

        provideInput("criar 1 raimondi lazaroi buscar 1 editar 1 novoTrocadilho deletar 1 buscar 1 sair");

        Client.main(new String[]{});
    }

    @AfterEach
    void cleanUp(){
        System.setIn(systemIn);
        System.setOut(systemOut);
    }

    private void provideInput(String data) {
        testIn = new ByteArrayInputStream(data.getBytes());
        System.setIn(testIn);
    }
}