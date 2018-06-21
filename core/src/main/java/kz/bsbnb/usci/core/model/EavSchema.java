package kz.bsbnb.usci.core.model;

/**
 * @author Jandos Iskakov
 */

public enum EavSchema {
    EAV_DATA("EAV_DATA"),
    EAV_XML("EAV_XML");

    private String name;

    EavSchema(String name) {
        this.name = name;
    }

}
