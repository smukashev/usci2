package kz.bsbnb.usci.model.eav.meta;

import kz.bsbnb.usci.model.Persistable;

/**
 * @author BSB
 */

public class MetaAttribute extends Persistable {
    private String name;
    private String title;
    private MetaType metaType;
    private boolean isKey = false;
    private Short keySet = 1;
    private boolean isFinal = false;
    private boolean isImmutable = false;
    private boolean isCumulative = false;
    private boolean isNullable = true;
    private String columnName;
    private String columnType;

    public MetaAttribute() {
        /*An empty constructor*/
    }

    public MetaAttribute(long id) {
        super(id);
    }

    public boolean isKey() {
        return isKey;
    }

    public void setKey(boolean isKey) {
        this.isKey = isKey;
    }

    public void setKeySet(Short keySet) {
        this.keySet = keySet;
    }

    public Short getKeySet() {
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

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
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
                '}';
    }

}



