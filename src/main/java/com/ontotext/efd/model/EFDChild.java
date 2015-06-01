package com.ontotext.efd.model;

import org.openrdf.model.URI;

public class EFDChild implements Comparable<EFDChild>{
    
    private URI uri;
    private Integer artCount;
    
    public EFDChild(URI uri, Integer artCount) {
        this.uri = uri;
        this.artCount = artCount;
    }
    
    public URI getUri() {
        return uri;
    }
    
    public Integer getArtCount() {
        return artCount;
    }

    public boolean equals(URIIntPair op) {
        return (uri.equals(op.getSubject()) && artCount.equals(op.getObject())) ? true : false;
    }

    @Override
    public int compareTo(EFDChild child) {

        Integer compareQuantity = child.getArtCount().intValue();
        return compareQuantity - this.getArtCount().intValue();

    }
}
