package com.ontotext.efd.model;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import com.ontotext.efd.rdf.EFDTaxonomy;

public class EFDCategoryTest {

    @Test
    public void test() {
        EFDCategory cat = new EFDCategory(new URIImpl(EFDTaxonomy.DBCAT + "Beer_logos"));
        System.out.println("Pref label: " + cat.getPrefLabel());
        System.out.println("Tree level: " + cat.getTreeLevel());
        System.out.print("Parents: ");
        for (URI parent : cat.getParents()) {
            System.out.print(parent.toString() + "  ");
        }
        System.out.println();
        System.out.print("Children: ");
        for (URI child : cat.getChildren()) {
            System.out.print(child.toString() + "  ");
        }
        System.out.println();
        System.out.print("Articles: ");
        for (URI art : cat.getLocalArticles()) {
            System.out.print(art.toString() + "  ");
        }
        System.out.println();
        System.out.println("Descended categories: " + cat.getDescCategoryCount());
        System.out.println("Descended articles: " + cat.getDescArticleCount());
        
        URI topic = new URIImpl(EFDTaxonomy.DBCAT + "SomethingTotallyUnrelatedToBeer");
        boolean irrelevant = cat.isIrrelevant();
        assertTrue("Category is marked as irrelevant from the start!", !irrelevant);
        System.out.println("Category correctly idetified as relevant.");
        
        cat.markAsIrrelevant(topic);
        irrelevant = cat.isIrrelevant();
        assertTrue("Category is marked as relevant when it shouldn't be!", irrelevant);
        System.out.println("Category succesfully marked as irrelevant.");
        
        cat.markAsRelevant(topic);
        irrelevant = cat.isIrrelevant();
        assertTrue("Category is marked as irrelevant when it shouldn't be", !irrelevant);
        System.out.println("Irrelevant marking has been successfully removed.");
    }

}
