package kz.bsbnb.usci.model.reciver;


import java.io.Serializable;

public class LoaderPriority implements Serializable {
    public Integer creditorId;
    public int priority;
    public String fileMask;
    public int priorityType;
    
}
