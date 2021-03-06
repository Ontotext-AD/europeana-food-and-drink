package com.ontotext.efd.model;

import java.util.*;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.repository.RepositoryException;

import com.ontotext.efd.rdf.EFDTaxonomy;
import com.ontotext.efd.services.EFDRepositoryConnection;

public class EFDCategory {
    
    private URI uri;
    private int level;
    private String prefLabel;
    private Set<URI> parents;
    private List<EFDChild> children;
    private Set<URI> localArticles;
    private int descCategoryCount;
    private int descArticleCount;
    
    public URI getUri() {
        return uri;
    }
    
    public void setUri(URI uri) {
        this.uri = uri;
    }
    
    public int getTreeLevel() {
        return level;
    }
    
    public void setTreeLevel(int level) {
        this.level = level;
    }

    public String getPrefLabel() {
        return prefLabel;
    }

    public void setPrefLabel(String prefLabel) {
        this.prefLabel = prefLabel;
    }
    
    public Set<URI> getParents() {
        return parents;
    }
    
    public void setParents(Set<URI> parents) {
        this.parents = parents;
    }
    
    public List<EFDChild> getChildren() {
        return children;
    }
    
    public void setChildren(List<EFDChild> children) {
        this.children = children;
    }
    
    public int getDescCategoryCount() {
        return descCategoryCount;
    }
    
    public void setDescCateogyrCount(int c) {
        this.descCategoryCount = c;
    }
    
    public Set<URI> getLocalArticles() {
        return localArticles;
    }
    
    public void setLocalArticles(Set<URI> c) {
        this.localArticles = c;
    }
    
    public int getDescArticleCount() {
        return descArticleCount;
    }
    
    public void setDescArticleCount(int c) {
        this.descArticleCount = c;
    }
    
    /**
     * Searches the repository for a treeLevel associated with the category.
     * @param repo 
     * @return
     */
    private int retrieveLevel(EFDRepositoryConnection repo) {
        URI pred = new URIImpl(EFDTaxonomy.EFD_LEVEL);
        String resp = repo.readObjectAsLiteral(this.uri, pred);
        return (resp != null) ? Integer.parseInt(resp) : -1;
    }
    
    /**
     * Searches the repository for a prefLabel associated with category.
     * @param repo 
     * @return
     */
    private String retrievePrefLabel(EFDRepositoryConnection repo) {
        return repo.readObjectAsLiteral(this.uri, SKOS.PREF_LABEL);
    }
    
    /**
     * Searches the repository for EFD-designated parents.
     * @param repo 
     * @return
     */
    private Set<URI> retrieveParents(EFDRepositoryConnection repo) {
        URI predicate = new URIImpl(EFDTaxonomy.EFD_CHILD);
        Set<URI> parents = repo.readSubjectsAsURI(predicate, this.uri);
        return parents;
    }
    
    /**
     * Searches the repository for EFD-designated children.
     * @param repo 
     * @return
     */
    private List<EFDChild> retrieveChildren(EFDRepositoryConnection repo) {
        URI predChild = new URIImpl(EFDTaxonomy.EFD_CHILD);
        URI predCount = new URIImpl(EFDTaxonomy.EFD_DESC_ART_CNT);
        Set<URI> children = repo.readObjectsAsURI(this.uri, predChild);
        List<EFDChild> childrenFull = new ArrayList<>();
        for (URI child : children) {
            String cntStr = repo.readObjectAsLiteral(child, predCount);
            Double cnt = Double.parseDouble(cntStr);
            childrenFull.add(new EFDChild(child, cnt.intValue()));
        }
        Collections.sort(childrenFull);
        return childrenFull;
    }
    
    /**
     * Searches the repository for an entry indicating the number
     * of unique categories designated as EFD-descendants of this category.
     * @param repo 
     * @return
     */
    private int retrieveCatCount(EFDRepositoryConnection repo) {
        URI predicate = new URIImpl(EFDTaxonomy.EFD_DESC_CNT);
        String resp = repo.readObjectAsLiteral(this.uri, predicate);
        return (resp != null) ? Integer.parseInt(resp) : -1;
    }

    /**
     * Searches the repository for an entry indicating the number
     * of articles connected to this node.
     * @param repo 
     * @return
     */
    private Set<URI> retrieveLocArticles(EFDRepositoryConnection repo) {
        URI predicate = new URIImpl(EFDTaxonomy.DCT_SUBJECT);
        Set<URI> articles = repo.readSubjectsAsURI(predicate, this.uri);
        return articles;
    }
    
    /**
     * Searches the repository for an entry indicating the 
     * number of unique articles under categories designated 
     * as EFD-descendants of this category.
     * @param repo 
     * @return
     */
    private int retrieveArtCount(EFDRepositoryConnection repo) {
        URI predicate = new URIImpl(EFDTaxonomy.EFD_DESC_ART_CNT);
        String resp = repo.readObjectAsLiteral(this.uri, predicate);
        if (resp != null) {
            Double d = Double.parseDouble(resp);
            return d.intValue();
        }
        return -1;
    }
    
    /**
     * Default builder which tries to extract all available data about
     * a category from the Food and Drink repository.
     * @param uri The URI of the category we are trying to represent.
     */
    public EFDCategory(URI uri) {
        EFDRepositoryConnection repo = new EFDRepositoryConnection();
        
        this.uri = uri;
        this.level = retrieveLevel(repo);
        this.prefLabel = retrievePrefLabel(repo);
        this.parents = retrieveParents(repo);
        this.children = retrieveChildren(repo);
        this.localArticles = retrieveLocArticles(repo);
        this.descArticleCount = retrieveArtCount(repo);
        this.descCategoryCount = retrieveCatCount(repo);
    }
    
    /**
     * Checks whether the category has been marked as irrelevant in the repository.
     * @return true if the category is marked as irrelevant in the repository
     */
    public boolean isIrrelevant() {
        EFDRepositoryConnection repo = new EFDRepositoryConnection();
        URI pred = new URIImpl(EFDTaxonomy.EFD_IRRELEVANT);
        String resp = repo.readObjectAsLiteral(this.uri, pred);
        if (resp != null) 
            return true;
        return false;
    }
    
    /**
     * Adds a triple marking the category as irrelevant to a specified topic.
     * A specific topic needs to be specified as passing null will do nothing.
     * @param irrelevantTo the topic to which the category is irrelevant
     * @throws RepositoryException 
     */
    public void markAsIrrelevant(URI irrelevantTo) throws RepositoryException {
        if (irrelevantTo == null || irrelevantTo.toString() == null)
            return;
        EFDRepositoryConnection repo = new EFDRepositoryConnection();
        
        // Add triple marking it as irrelevant to the topic.
        URI irrPred = new URIImpl(EFDTaxonomy.EFD_IRRELEVANT);
        repo.addStatementWithURI(this.uri, irrPred, irrelevantTo);
        
        // Remove all efd:child nodes leading to or from this category.
        URI childPred = new URIImpl(EFDTaxonomy.EFD_CHILD);
        repo.removeStatementWithURI(this.uri, childPred, null);
        repo.removeStatementWithURI(null, childPred, this.uri);
        
        // Remove level, category count and article count literals about category.
        URI levelPred = new URIImpl(EFDTaxonomy.EFD_LEVEL);
        repo.removeStatementWithLiteral(this.uri, levelPred, null);
        URI descCatPred = new URIImpl(EFDTaxonomy.EFD_DESC_CNT);
        repo.removeStatementWithLiteral(this.uri, descCatPred, null);
        URI descArtPred = new URIImpl(EFDTaxonomy.EFD_DESC_ART_CNT);
        repo.removeStatementWithLiteral(this.uri, descArtPred, null);
    }
    
    /**
     * Removes a triple marking the category as irrelevant to a specified topic.
     * If null is passed, all irrelevant triples about category will be removed.
     * NOTE: This is NOT the inverse of markAsIrrelevant as removed efd:child
     * connection will not be restored until a full tree rebuild has been performed.
     * @param relevantTo
     */
    public void markAsRelevant(URI relevantTo) {
        EFDRepositoryConnection repo = new EFDRepositoryConnection();
        URI pred = new URIImpl(EFDTaxonomy.EFD_IRRELEVANT);
        repo.removeStatementWithURI(this.uri, pred, relevantTo);
    }
    
    public boolean equals(EFDCategory t) {
        if (t.getUri().equals(this.uri))
            return true;
        return false;
    }
}