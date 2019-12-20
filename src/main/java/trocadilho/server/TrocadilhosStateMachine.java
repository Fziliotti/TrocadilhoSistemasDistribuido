package trocadilho.server;

import io.atomix.copycat.server.Commit;
import io.atomix.copycat.server.StateMachine;
import trocadilho.*;
import trocadilho.command.CreateTrocadilhoCommand;
import trocadilho.command.DeleteTrocadilhoCommand;
import trocadilho.command.ListTrocadilhosQuery;
import trocadilho.command.UpdateTrocadilhoCommand;
import trocadilho.domain.Trocadilho;
import trocadilho.service.TrocadilhoServiceImpl;
import trocadilho.db.trocadilho.TrocadilhoRepositoryImpl;

import java.io.IOException;
import java.util.List;
import java.util.Random;


public class TrocadilhosStateMachine extends StateMachine {


    private TrocadilhoRepositoryImpl trocadilhoRepository = new TrocadilhoRepositoryImpl();

    public String listTrocadilhos(Commit<ListTrocadilhosQuery> commit) throws IOException, InterruptedException {
        try {
            StringBuilder message = new StringBuilder("");
            List<Trocadilho> trocadilhos = trocadilhoRepository.listAll();

            trocadilhos.forEach(trocadilho -> {
                message.append("\nId: ").append(trocadilho.getCode()).append(" --Autor -> ").append(trocadilho.getUsername()).append(" --- ").append(trocadilho.getContent());
            });

            return message.toString();
        } finally {
            commit.close();
        }
    }


    public String updateTrocadilho(Commit<UpdateTrocadilhoCommand> commit) throws IOException, InterruptedException {
        try {
            StringBuilder message = new StringBuilder("");
            UpdateTrocadilhoCommand operation = commit.operation();
            trocadilhoRepository.update( operation.code, operation.content );
            message.append("OK - Trocadilho "+ operation.code + " updated!");
            return message.toString();
        } finally {
            commit.close();
        }
    }

    public String deleteTrocadilho(Commit<DeleteTrocadilhoCommand> commit) throws IOException, InterruptedException {
        try {
            StringBuilder message = new StringBuilder("");
            DeleteTrocadilhoCommand operation = commit.operation();
            trocadilhoRepository.deleteById(operation.code);
            message.append("OK - Trocadilho "+ operation.code + " deleted!");

            return message.toString();
        } finally {
            commit.close();
        }
    }

    public String createTrocadilho(Commit<CreateTrocadilhoCommand> commit) throws IOException, InterruptedException {
        try {
            StringBuilder message = new StringBuilder("");
            CreateTrocadilhoCommand operation = commit.operation();
            trocadilhoRepository.create(operation.content, operation.username);
            message.append("OK - Trocadilho created!");

            return message.toString();
        } finally {
            commit.close();
        }
    }





}