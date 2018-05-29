package kz.bsbnb.usci.model.meta;

import kz.bsbnb.usci.model.Persistable;

public class MetaAttribute extends Persistable {

    private String name;
    private String title;
    private MetaType metaType;
    private boolean isKey = false;
    private int keySet = 1;
    private boolean isFinal = false;
    private boolean isImmutable = false;
    private boolean isCumulative = false;
    private boolean isNullable = true;
    private boolean isDisabled = false;

    public MetaAttribute() {
        super();
    }

    public boolean isKey() {
        return isKey;
    }

    public void setKey(boolean isKey) {
        this.isKey = isKey;
    }


    public void setKeySet(int keySet) {
        this.keySet = keySet;
    }

    public int getKeySet() {
        return keySet;
    }

    public boolean isNullable() {
        return isNullable;
    }

    public void setNullable(boolean isNullable) {
        this.isNullable = isNullable;
    }


    public MetaType getMetaType() {
        return metaType;
    }

    public void setMetaType(MetaType metaType) {
        this.metaType = metaType;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        if (title != null) return title;
        return getName();
    }

    public boolean isFinal() {
        return isFinal;
    }

    public void setFinal(boolean aFinal) {
        isFinal = aFinal;
    }

    public boolean isImmutable() {
        return isImmutable;
    }

    public void setImmutable(boolean isImmutable) {
        this.isImmutable = isImmutable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public boolean isCumulative() {
        return isCumulative;
    }


    public void setCumulative(boolean isCumulative) {
        this.isCumulative = isCumulative;
    }

    public boolean isDisabled() {
        return isDisabled;
    }

    public void setDisabled(boolean isDisabled) {
        this.isDisabled = isDisabled;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        MetaAttribute that = (MetaAttribute) o;

        if (isKey != that.isKey) return false;
        if (keySet != that.keySet) return false;
        if (isFinal != that.isFinal) return false;
        if (isImmutable != that.isImmutable) return false;
        if (isCumulative != that.isCumulative) return false;
        if (isNullable != that.isNullable) return false;
        if (isDisabled != that.isDisabled) return false;
        if (!name.equals(that.name)) return false;
        if (!title.equals(that.title)) return false;
        return metaType.equals(that.metaType);

    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + title.hashCode();
        result = 31 * result + metaType.hashCode();
        result = 31 * result + (isKey ? 1 : 0);
        result = 31 * result + (isFinal ? 1 : 0);
        result = 31 * result + (isImmutable ? 1 : 0);
        result = 31 * result + (isCumulative ? 1 : 0);
        result = 31 * result + (isNullable ? 1 : 0);
        result = 31 * result + (isDisabled ? 1 : 0);
        return result;
    }

    public String toString() {
        return "MetaAttribute{" +
                "name='" + name + '\'' +
                ", title='" + title + '\'' +
                ", metaType=" + metaType +
                ", isKey=" + isKey +
                ", isOptionKey=" + keySet +
                ", isFinal=" + isFinal +
                ", isImmutable=" + isImmutable +
                ", isCumulative=" + isCumulative +
                ", isNullable=" + isNullable +
                ", isDisabled=" + isDisabled +
                '}';
    }

}



