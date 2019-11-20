package trocadilho.db.trocadilho;

public interface TrocadilhoRepository  {
    void create(String content, User author, LocalDateTime createdAt);
    void find(String name);
}
