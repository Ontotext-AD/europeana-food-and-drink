package com.ontotext.efd.model;

import org.openrdf.model.URI;

public class TreeBuildingCat {
    
    public int id;
    public int level;
    public URI uri;
    
    public TreeBuildingCat(int id, int level, URI uri) {
        this.id = id;
        this.level = level;
        this.uri = uri;
    }

}
