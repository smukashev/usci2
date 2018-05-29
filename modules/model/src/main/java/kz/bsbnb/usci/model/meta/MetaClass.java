package kz.bsbnb.usci.model.meta;

import java.util.*;

import kz.bsbnb.usci.util.DataUtils;

public class MetaClass implements MetaType {

    private String className;

    private String classTitle;

    private Date beginDate;

    private boolean disabled = false;

    private boolean searchable = false;

    private boolean reference = false;

    private boolean parentIsKey = false;

    /**
     * @associates <{kz.bsbnb.usci.model.meta.MetaAttribute}>
     */
    private Map<String, MetaAttribute> attributes = new HashMap<>();

    public MetaClass() {
        this.beginDate = new Date();
        DataUtils.toBeginningOfTheDay(beginDate);
    }


    @Override
    protected Object clone() throws CloneNotSupportedException {

        MetaClass meta = new MetaClass();
        
        meta.className = this.className;
        meta.classTitle = this.classTitle;
        meta.disabled = this.disabled;
        meta.beginDate = this.beginDate;
        meta.reference = this.reference;
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

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        Date newBeginDate = (Date) beginDate.clone();
        DataUtils.toBeginningOfTheDay(newBeginDate);

        this.beginDate = newBeginDate;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public void removeAttributes() {
        searchable = false;
        attributes.clear();
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

    public boolean isReference() {
        return reference;
    }

    public void setReference(boolean value) {
        reference = value;
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
    
    @Override
    public boolean equals(Object obj) {
    //        if (obj == this)
    //            return true;
    //        if (obj == null)
    //            return false;
    //        if (!(getClass() == obj.getClass()))
    //            return false;
    //        else {
    //            MetaClass tmp = (MetaClass) obj;
    //            if (this.getId() > 0 && tmp.getId() > 0 && this.getId() == tmp.getId())
    //                return true;
    //
    //            if (tmp.getAttributesCount() != this.getAttributesCount())
    //                return false;
    //
    //            Set<String> thisNames = this.attributes.keySet();
    //            for (String name : thisNames) {
    //                if (!(this.getAttributeType(name).equals(tmp.getAttributeType(name))))
    //                    return false;
    //            }
    //            return !(tmp.isDisabled() != this.isDisabled() ||
    //                    !tmp.getBeginDate().equals(this.getBeginDate()) ||
    //                    !tmp.getClassName().equals(this.getClassName()));
    //        }
        return super.equals(obj);
    }
    
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + className.hashCode();
        result = 31 * result + beginDate.hashCode();
        result = 31 * result + (disabled ? 1 : 0);
        result = 31 * result + (searchable ? 1 : 0);
        result = 31 * result + attributes.hashCode();
        return result;
    }

    public String toString() {
        return null;
    }

}
