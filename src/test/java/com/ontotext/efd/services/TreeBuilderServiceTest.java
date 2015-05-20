package com.ontotext.efd.services;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;

import com.ontotext.efd.rdf.EFDTaxonomy;

public class TreeBuilderServiceTest {

    @Test
    public void test() {
        URI levelPred = new URIImpl(EFDTaxonomy.EFD_LEVEL);
        URI childPred = new URIImpl(EFDTaxonomy.EFD_CHILD);
        URI rootName = new URIImpl(EFDTaxonomy.DBCAT + "Food_and_drink");
        TreeBuilderService builder = new TreeBuilderService(levelPred, childPred);
        try {
            builder.buildTreeFromRoot(rootName);
        } catch (RepositoryException e) {
            System.err.println("Failed to build a tree from node!");
            e.printStackTrace();
            fail();
        }
    }

}
