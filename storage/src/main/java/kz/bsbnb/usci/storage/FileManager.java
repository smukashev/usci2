package kz.bsbnb.usci.storage;

import kz.bsbnb.usci.model.storage.Directory;

import java.awt.Component;

import java.util.List;
import java.io.*;

public class FileManager {
    
    String path;
    
    /**
     * @associates <{kz.bsbnb.usci.storage.Directory}>
     */
    private List directories;
    
    void put(Directory dir, String filePath, InputStream is) {
        
    }
    
    void get(Directory dir, String filePath, OutputStream os) {
        
    }
    
    void del(Directory dir, String filePath) {
        
    }
    
    Directory mkdir(String dirName, String path) {
        return null;
    }
    
}

