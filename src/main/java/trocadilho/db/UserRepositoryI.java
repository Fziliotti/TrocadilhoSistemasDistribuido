package trocadilho.db;

public interface UserRepositoryI {
    void create(String name, String password);
    void find(String name);
}
