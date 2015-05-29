package com.ontotext.efd.model;

import org.openrdf.model.URI;

public class EFDChild {
    
    private URI uri;
    private Double artCount;
    
    public EFDChild(URI uri, Double artCount) {
        this.uri = uri;
        this.artCount = artCount;
    }
    
    public URI getUri() {
        return uri;
    }
    
    public Double getArtCount() {
        return artCount;
    }

    public boolean equals(URIIntPair op) {
        return (uri.equals(op.getSubject()) && artCount.equals(op.getObject())) ? true : false;
    }

}
