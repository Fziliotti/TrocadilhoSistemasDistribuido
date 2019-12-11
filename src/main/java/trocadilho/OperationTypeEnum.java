package trocadilho;

public enum OperationTypeEnum {
    CREATE("1"),
    UPDATE("2"),
    DELETE("3");

    public String value;
    OperationTypeEnum(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
}
