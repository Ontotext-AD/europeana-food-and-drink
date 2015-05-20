package com.ontotext.efd.services;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.RepositoryException;

import com.ontotext.efd.rdf.EFDTaxonomy;

public class TreeCounterServiceTest {

    @Test
    public void test() {
        ValueFactory factory = ValueFactoryImpl.getInstance();
        URI childPred = factory.createURI(EFDTaxonomy.EFD_CHILD);
        URI levelPred = factory.createURI(EFDTaxonomy.EFD_LEVEL);
        URI descArtCntPred = factory.createURI(EFDTaxonomy.EFD_DESC_ART_CNT);
        URI descCatCntPred = factory.createURI(EFDTaxonomy.EFD_DESC_CNT);
        TreeCounterService counter = new TreeCounterService(childPred, levelPred, 
                                                            descArtCntPred, descCatCntPred);
        
        URI root = factory.createURI(EFDTaxonomy.DBCAT  + "Food_and_drink");
        try {
            counter.calculateTreeStats(root);
        } catch (RepositoryException e) {
            System.err.println("Failed to calculate tree stats.");
            e.printStackTrace();
            fail();
        } catch (OutOfMemoryError e) {
            System.err.println("Failed to calculate unique descendant count as we ran out of memory.");
            e.printStackTrace();
            fail();
        }
    }

}
