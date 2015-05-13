package com.ontotext.efd.model;

import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.SKOS;

import com.ontotext.efd.rdf.EFDTaxonomy;
import com.ontotext.efd.services.EFDRepositoryConnection;

public class EFDCategory {
    
    private URI uri;
    private int level;
    private String prefLabel;
    private Set<URI> parents;
    private Set<URI> children;
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
    
    public Set<URI> getChildren() {
        return children;
    }
    
    public void setChildren(Set<URI> children) {
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
    
    public EFDCategory(URI uri, int level) {
        this.uri = uri;
        this.level = level;
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
    private Set<URI> retrieveChildren(EFDRepositoryConnection repo) {
        URI predicate = new URIImpl(EFDTaxonomy.EFD_CHILD);
        Set<URI> children = repo.readObjectsAsURI(this.uri, predicate);
        return children;
    }
    
    /**
     * Searches the repository for an entry indicating the number
     * of unique categories designated as EFD-descendants of this category.
     * @param repo 
     * @return
     */
    private int retrieveCatCount(EFDRepositoryConnection repo) {
        URI predicate = new URIImpl(EFDTaxonomy.EFD_DESC_NUM);
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
        URI predicate = new URIImpl(EFDTaxonomy.EFD_ART_NUM);
        String resp = repo.readObjectAsLiteral(this.uri, predicate);
        return (resp != null) ? Integer.parseInt(resp) : -1;
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
     */
    public void markAsIrrelevant(URI irrelevantTo) {
        if (irrelevantTo == null || irrelevantTo.toString() == null)
            return;
        EFDRepositoryConnection repo = new EFDRepositoryConnection();
        URI pred = new URIImpl(EFDTaxonomy.EFD_IRRELEVANT);
        repo.addStatementWithURI(this.uri, pred, irrelevantTo);
    }
    
    /**
     * Removes a triple marking the category as irrelevant to a specified topic.
     * If null is passed, all irrelevant triples about category will be removed.
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