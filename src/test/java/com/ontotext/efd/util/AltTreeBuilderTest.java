package com.ontotext.efd.util;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

public class AltTreeBuilderTest {

    @Test
    public void test() {
        URI root = new URIImpl("http://dbpedia.org/resource/Category:Food_and_drink");
        new AltTreeBuilder(root);
        System.out.println("Finished test.");
    }

}
