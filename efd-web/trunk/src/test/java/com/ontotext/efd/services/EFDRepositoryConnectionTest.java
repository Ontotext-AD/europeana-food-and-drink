package com.ontotext.efd.services;

import java.util.List;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

public class EFDRepositoryConnectionTest {

    @Test
    public void getSkosChildrenTest() {
        URI testUri = new URIImpl("http://dbpedia.org/resource/Category:Food_and_drink");
        EFDRepositoryConnection repoConn = new EFDRepositoryConnection();
        List<URI> children = repoConn.getSkosChildren(testUri);
        
        System.out.println(testUri.toString() + " has " + children.size() + " children." );
        for (URI child : children) {
            System.out.println(child.toString());
        }
    }

}
