package com.ontotext.efd.model;

import org.openrdf.model.URI;

public class URIPair {
    
    private URI subject;
    private URI object;
    
    public URIPair(URI subject, URI object) {
        this.subject = subject;
        this.object = object;
    }
    
    public URI getSubject() {
        return subject;
    }
    
    public URI getObject() {
        return object;
    }

    public boolean equals(URIPair op) {
        return (subject.equals(op.getSubject()) && object.equals(op.getObject())) ? true : false;
    }
}
