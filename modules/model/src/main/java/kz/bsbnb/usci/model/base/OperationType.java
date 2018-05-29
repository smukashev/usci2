package kz.bsbnb.usci.model.base;

public enum OperationType {
    NEW(1),
    DELETE(2),
    OPEN(3),
    CLOSE(4),
    INSERT(5),
    UPDATE(6),
    CHECKED_REMOVE(7);

    private int value;

    OperationType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
