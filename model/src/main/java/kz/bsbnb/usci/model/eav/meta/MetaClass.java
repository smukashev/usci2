package kz.bsbnb.usci.model.eav.meta;

import kz.bsbnb.usci.model.Errors;
import kz.bsbnb.usci.model.Persistable;

import java.time.LocalDate;
import java.util.*;

/**
 * @author BSB
 */

public class MetaClass extends Persistable implements MetaType {
    private String className;
    private String classTitle;
    private LocalDate beginDate;
    private String schemaData;
    private String schemaXml;
    private String tableName;
    private boolean deleted;
    private boolean searchable = false;
    private boolean dictionary = false;
    private boolean parentIsKey = false;
    private Map<String, MetaAttribute> attributes = new HashMap<>();

    public MetaClass() {
        super();
        this.beginDate = LocalDate.now();
        //DataUtils.toBeginningOfTheDay(beginDate); TODO:
    }

    public MetaClass(long id) {
        super(id);
        this.beginDate = LocalDate.now();
    }

    public MetaClass(String className) {
        this.className = className;
        this.beginDate = LocalDate.now();
        //DataUtils.toBeginningOfTheDay(beginDate); TODO:
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        MetaClass meta = new MetaClass();
        
        meta.className = this.className;
        meta.classTitle = this.classTitle;
        meta.beginDate = this.beginDate;
        meta.dictionary = this.dictionary;
        meta.parentIsKey = this.parentIsKey;

        meta.attributes.putAll(this.attributes);
        
        return meta;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public LocalDate getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(LocalDate beginDate) {
        //Date newBeginDate = (LocalDate) beginDate.clone();
        //DataUtils.toBeginningOfTheDay(newBeginDate);

        this.beginDate = beginDate;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getSchemaData() {
        return schemaData;
    }

    public void setSchemaData(String schemaData) {
        this.schemaData = schemaData;
    }

    public String getSchemaXml() {
        return schemaXml;
    }

    public void setSchemaXml(String schemaXml) {
        this.schemaXml = schemaXml;
    }

    public void removeAttribute(String name) {
        attributes.remove(name);

        searchable = false;

        for (MetaAttribute metaAttribute : attributes.values()) {
            if (metaAttribute.isKey()) {
                searchable = true;
                break;
            }
        }
    }

    public void setMetaAttribute(String name, MetaAttribute metaAttribute) {
        if (!searchable && metaAttribute.isKey())
            searchable = metaAttribute.isKey();

        attributes.put(name, metaAttribute);
        metaAttribute.setName(name);
    }

    public void removeAttributes() {
        searchable = false;
        attributes.clear();
    }

    public Collection<MetaAttribute> getAttributes() {
        return attributes.values();
    }

    @Override
    public boolean isSet() {
        return false;
    }

    @Override
    public boolean isComplex() {
        return true;
    }

    public boolean isSearchable() {
        return searchable;
    }

    public boolean isDictionary() {
        return dictionary;
    }

    public void setDictionary(boolean value) {
        dictionary = value;
    }

    public String getClassTitle() {
        return classTitle;
    }

    public void setClassTitle(String classTitle) {
        this.classTitle = classTitle;
    }

    public boolean parentIsKey() {
        return parentIsKey;
    }

    public void setParentIsKey(boolean parentIsKey) {
        this.parentIsKey = parentIsKey;
    }

    public Set<String> getAttributeNames() {
        return attributes.keySet();
    }

    public MetaAttribute getMetaAttribute(String name) {
        return attributes.get(name);
    }

    public MetaType getAttributeType(String name) {
        MetaAttribute metaAttribute = attributes.get(name);

        if (metaAttribute == null)
            throw new IllegalArgumentException(Errors.compose(Errors.E45,name, this.getClassName()));

        return metaAttribute.getMetaType();
    }

    @Override
    public boolean equals(Object obj) {
        //TODO:
        return false;
    }
    
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + className.hashCode();
        result = 31 * result + beginDate.hashCode();
        result = 31 * result + (searchable ? 1 : 0);
        result = 31 * result + attributes.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return toString("");
    }

    @Override
    public String toString(String prefix) {
        //TODO: добавить поддержку complexKeyType
        String str = className + ":metaClass(" + getId() + "_" + searchable + "_" /*+ complexKeyType + ");"*/;

        String[] names;

        names = attributes.keySet().toArray(new String[attributes.keySet().size()]);

        Arrays.sort(names);

        for (String memberName : names) {
            MetaAttribute attribute = attributes.get(memberName);
            MetaType type = attribute.getMetaType();

            String key = "";

            if (attribute.isKey()) key = "*";

            str += "\n" + prefix + memberName + "(" + attribute.getId() + ")" + key + ": " +
                    type.toString(prefix + "\t");
        }

        return str;
    }

}
