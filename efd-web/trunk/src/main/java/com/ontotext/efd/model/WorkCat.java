package com.ontotext.efd.model;

import org.openrdf.model.URI;

public class WorkCat {
    
    public int id;
    public int level;
    public URI uri;
    
    public WorkCat(int id, int level, URI uri) {
        this.id = id;
        this.level = level;
        this.uri = uri;
    }

}
