package kz.bsbnb.usci.model.eav.data;

public enum DataOperationType {
    NEW(1),
    DELETE(2),
    OPEN(3),
    CLOSE(4),
    INSERT(5),
    UPDATE(6),
    CHECKED_REMOVE(7);

    private int value;

    DataOperationType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
