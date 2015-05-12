package com.ontotext.efd.services;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

import com.ontotext.efd.rdf.EFDTaxonomy;

public class EFDRepositoryConnectionTest {

    /*
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
    */

    ValueFactory factory = ValueFactoryImpl.getInstance();
    URI parent = factory.createURI(EFDTaxonomy.DBCAT + "Made_up_parent");
    URI pred = factory.createURI(EFDTaxonomy.EFD + "testPredicate");
    URI child =  factory.createURI(EFDTaxonomy.DBCAT + "Made_up_child");
    Integer childLit = 7;
    
    @Test
    public void removeStatementsWithPredicateTest() {
        EFDRepositoryConnection repoConn = new EFDRepositoryConnection();
        repoConn.removeStatementsWithPredicate(pred);
        System.out.println("Successfully removed all triples with test predicate.");
    }
    
    @Test
    public void statementWithURIObjectTest() {
        EFDRepositoryConnection repoConn = new EFDRepositoryConnection();
        
        // Just remove everything with test predicate to ensure clean repo.
        repoConn.removeStatementsWithPredicate(pred);
        
        // Test adding the triple to repository.
        repoConn.addStatementWithURI(parent, pred, child);
        System.out.println("Succesfully added a test statement with URI object.");
        
        // Test reading the object from repository.
        Set<URI> objs = repoConn.readObjectsAsURI(parent, pred);
        assertTrue("Returned multiple objects but we have only written one!", objs.size() == 1);
        assertTrue("Object retrieved is not object written!", objs.contains(child));
        
        // Test reading the subject from repository.
        Set<URI> subjs = repoConn.readSubjectsAsURI(pred, child);
        assertTrue("Returned multiple objects but we have only written one!", subjs.size() == 1);
        assertTrue("Subject retrieved is not subject written!", subjs.contains(parent));
        
        // Test removing the triple from repository.
        repoConn.removeStatementWithURI(parent, pred, child);
        Set<URI> emptyObjs = repoConn.readObjectsAsURI(parent, pred);
        assertTrue("Failed to remove the triple!", emptyObjs == null || emptyObjs.size() == 0);
        System.out.println("Successfully removed an EFD child statement.");
    }
    
    @Test
    public void statementWithLiteralObjectTest() {
        EFDRepositoryConnection repoConn = new EFDRepositoryConnection();
        
        // Just remove everything with test predicate to ensure clean repo.
        repoConn.removeStatementsWithPredicate(pred);
        
        // Test adding the triple to the repository.
        repoConn.addStatementWithLiteral(parent, pred, childLit.toString());
        System.out.println("Succesfully added a test statement with Literal object.");
        
        // Test reading the literal from the repository.
        String result = repoConn.readObjectAsLiteral(parent, pred);
        Integer bloop = Integer.parseInt(result);
        assertTrue("Retrieved literal does not match written literal!", childLit.equals(bloop));
        System.out.println("Succesfully retrieved a test statement with Literal object.");
        
        // Test removing the triple from the repository.
        repoConn.removeStatementWithLiteral(parent, pred, childLit.toString());
        String resp = repoConn.readObjectAsLiteral(parent, pred);
        assertTrue("Failed to remove the triple!", resp == null);
        System.out.println("Successfully removed an EFD child statement.");
    }
}
