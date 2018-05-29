package kz.bsbnb.usci.model;

public class Persistable {
    public static final long DEFAULT_ID = 0;

    protected long id = 0;

    protected Persistable() {
        super();
    }

    protected Persistable(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Persistable)) return false;

        Persistable that = (Persistable) o;

        if (id != that.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}