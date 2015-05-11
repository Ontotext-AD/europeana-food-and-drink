package com.ontotext.efd.model;

import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.URI;

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
    
    /**
     * Default builder which tries to extract all available data about
     * a category from the Food and Drink repository.
     * @param uri The URI of the category we are trying to represent.
     */
    public EFDCategory(URI uri, int level) {
        this.uri = uri;
        this.level = level;
        this.prefLabel = retrievePrefLabel();
        this.parents = retrieveParents();
        this.children = retrieveChildren();
        this.localArticles = retrieveLocArticles();
        this.descArticleCount = retrieveArtCount();
        this.descCategoryCount = retrieveCatCount();
    }
    
    /**
     * Searches the repository for a prefLabel associated with category.
     * @return
     */
    private String retrievePrefLabel() {
        return "";
    }
    
    /**
     * Searches the repository for EFD-designated parents.
     * @return
     */
    private Set<URI> retrieveParents() {
        Set<URI> emptySet = new HashSet<URI>();
        return emptySet;
    }
    
    /**
     * Searches the repository for EFD-designated children.
     * @return
     */
    private Set<URI> retrieveChildren() {
        Set<URI> emptySet = new HashSet<URI>();
        return emptySet;
    }
    
    /**
     * Searches the repository for an entry indicating the number
     * of unique categories designated as EFD-descendants of this category.
     * @return
     */
    private int retrieveCatCount() {
        return 0;
    }

    /**
     * Searches the repository for an entry indicating the number
     * of articles connected to this node.
     * @return
     */
    private Set<URI> retrieveLocArticles() {
        Set<URI> emptySet = new HashSet<URI>();
        return emptySet;
    }
    
    /**
     * Searches the repository for an entry indicating the 
     * number of unique articles under categories designated 
     * as EFD-descendants of this category.
     * @return
     */
    private int retrieveArtCount() {
        return 0;
    }
    
    public boolean equals(EFDCategory t) {
        if (t.getUri().equals(this.uri))
            return true;
        return false;
    }
}