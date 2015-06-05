package com.ontotext.efd.model;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;

import com.ontotext.efd.rdf.EFDTaxonomy;

public class EFDCategoryTest {

    @Test
    public void test() {
        EFDCategory cat = new EFDCategory(new URIImpl(EFDTaxonomy.DBCAT + "Turkish_cuisine"));
        System.out.println("Pref label: " + cat.getPrefLabel());
        System.out.println("Tree level: " + cat.getTreeLevel());
        System.out.print("Parents: ");
        for (URI parent : cat.getParents()) {
            System.out.print(parent.toString() + "  ");
        }
        System.out.println();
        System.out.print("Children: ");
        for (EFDChild child : cat.getChildren()) {
            System.out.print(child.getUri() + "(" + child.getArtCount() + ")  ");
        }
        System.out.println();
        System.out.print("Articles: ");
        for (URI art : cat.getLocalArticles()) {
            System.out.print(art.toString() + "  ");
        }
        System.out.println();
        System.out.println("Descended categories: " + cat.getDescCategoryCount());
        System.out.println("Descended articles: " + cat.getDescArticleCount());
        
        cat  = new EFDCategory(new URIImpl(EFDTaxonomy.DBCAT + "SomethingTotallyUnrelatedToEFD"));
        boolean irrelevant = cat.isIrrelevant();
        assertTrue("Category is marked as irrelevant from the start!", !irrelevant);
        System.out.println("Category correctly idetified as relevant.");
        
        try {
            cat.markAsIrrelevant(new URIImpl(EFDTaxonomy.EFD_ANNOTATOR_AUTOMATED));
        } catch (RepositoryException e) {
            System.err.println("Failed to mark topic as irrelevant.");
            e.printStackTrace();
            fail();
        }
        irrelevant = cat.isIrrelevant();
        assertTrue("Category is marked as relevant when it shouldn't be!", irrelevant);
        System.out.println("Category succesfully marked as irrelevant.");
        
        cat.markAsRelevant(new URIImpl(EFDTaxonomy.EFD_ANNOTATOR_AUTOMATED));
        irrelevant = cat.isIrrelevant();
        assertTrue("Category is marked as irrelevant when it shouldn't be", !irrelevant);
        System.out.println("Irrelevant marking has been successfully removed.");
    }

}
