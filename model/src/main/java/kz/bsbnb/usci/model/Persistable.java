package kz.bsbnb.usci.model;

public class Persistable {
    protected Long id;

    protected Persistable() {
        super();
    }

    protected Persistable(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Persistable)) return false;

        Persistable that = (Persistable) o;

        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        if (id == null)
            return 0;

        return (int) (id ^ (id >>> 32));
    }

}