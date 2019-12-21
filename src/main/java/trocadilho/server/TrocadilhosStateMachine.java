package trocadilho.server;

import io.atomix.copycat.server.Commit;
import io.atomix.copycat.server.StateMachine;
import trocadilho.CreateTrocadilhoRequest;
import trocadilho.DeleteTrocadilhoRequest;
import trocadilho.GetTrocadilhoRequest;
import trocadilho.UpdateTrocadilhoRequest;
import trocadilho.command.CreateTrocadilhoCommand;
import trocadilho.command.DeleteTrocadilhoCommand;
import trocadilho.command.FindByCodeQuery;
import trocadilho.command.UpdateTrocadilhoCommand;
import trocadilho.db.trocadilho.TrocadilhoRepositoryImpl;
import trocadilho.domain.Trocadilho;

import java.io.IOException;


public class TrocadilhosStateMachine extends StateMachine {


    private TrocadilhoRepositoryImpl trocadilhoRepository = new TrocadilhoRepositoryImpl();

    public String findByCode(Commit<FindByCodeQuery> commit) throws IOException, InterruptedException {
        try {
            StringBuilder message = new StringBuilder("");
            FindByCodeQuery operation = commit.operation();
            Trocadilho trocadilho = trocadilhoRepository.findByCode(GetTrocadilhoRequest.newBuilder().setCode(operation.code).build());

            message.append("\nId: ").append(trocadilho.getCode()).append(" --Autor -> ").append(trocadilho.getUsername()).append(" --- ").append(trocadilho.getContent());
            return message.toString();
        } finally {
            commit.close();
        }
    }


    public String updateTrocadilho(Commit<UpdateTrocadilhoCommand> commit) throws IOException, InterruptedException {
        try {
            StringBuilder message = new StringBuilder("");
            UpdateTrocadilhoCommand operation = commit.operation();
            trocadilhoRepository.update(
                    UpdateTrocadilhoRequest.newBuilder()
                            .setCode(operation.code)
                            .setTrocadilho(operation.content)
                            .build()
            );
            message.append("OK - Trocadilho " + operation.code + " updated!");
            return message.toString();
        } finally {
            commit.close();
        }
    }

    public String deleteTrocadilho(Commit<DeleteTrocadilhoCommand> commit) throws IOException, InterruptedException {
        try {
            StringBuilder message = new StringBuilder("");
            DeleteTrocadilhoCommand operation = commit.operation();
            trocadilhoRepository.deleteById(
                    DeleteTrocadilhoRequest.newBuilder()
                            .setCode(operation.code)
                            .build()
            );
            message.append("OK - Trocadilho ").append(operation.code).append(" deleted!");

            return message.toString();
        } finally {
            commit.close();
        }
    }

    public String createTrocadilho(Commit<CreateTrocadilhoCommand> commit) throws IOException, InterruptedException {
        try {
            StringBuilder message = new StringBuilder("");
            CreateTrocadilhoCommand operation = commit.operation();
            trocadilhoRepository.create(
                    CreateTrocadilhoRequest.newBuilder()
                            .setCode(operation.code)
                            .setUsername(operation.username)
                            .setTrocadilho(operation.content)
                            .build()
            );
            message.append("OK - Trocadilho created!");

            return message.toString();
        } finally {
            commit.close();
        }
    }


}