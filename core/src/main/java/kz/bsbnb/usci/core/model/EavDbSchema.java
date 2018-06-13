package kz.bsbnb.usci.core.model;

public enum EavDbSchema {
    EAV_DATA("EAV_DATA"),
    EAV_XML("EAV_XML");

    private String name;

    EavDbSchema(String name) {
        this.name = name;
    }

}
