package com.ontotext.efd.services;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import com.ontotext.efd.rdf.EFDTaxonomy;

public class TreeBuilderServiceTest {

    @Test
    public void test() {
        URI levelPred = new URIImpl(EFDTaxonomy.EFD + "testTreeLevel");
        URI childPred = new URIImpl(EFDTaxonomy.EFD + "testChild");
        URI rootName = new URIImpl(EFDTaxonomy.DBCAT + "Turkish_desserts");
        TreeBuilderService builder = new TreeBuilderService(levelPred, childPred);
        builder.buildTreeFromRoot(rootName);
    }

}
