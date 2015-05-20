package com.ontotext.efd.model;

import org.openrdf.model.URI;

public class URIIntPair {
    
    private URI subject;
    private Integer object;
    
    public URIIntPair(URI subject, Integer object) {
        this.subject = subject;
        this.object = object;
    }
    
    public URI getSubject() {
        return subject;
    }
    
    public Integer getObject() {
        return object;
    }

    public boolean equals(URIIntPair op) {
        return (subject.equals(op.getSubject()) && object.equals(op.getObject())) ? true : false;
    }

}
