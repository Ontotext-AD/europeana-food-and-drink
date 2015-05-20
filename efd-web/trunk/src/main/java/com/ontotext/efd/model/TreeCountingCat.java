package com.ontotext.efd.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;

public class TreeCountingCat {
    
    private int id;
    private URI uri;
    private int treeLevel;
    private double articleCount;
    private List<Integer> parents;
    private List<Integer> children;
    private Set<Integer> descendants;
    
    boolean processedForTreeLevel;

    public TreeCountingCat(int id, URI uri) {
        this.id = id;
        this.uri = uri;
        this.treeLevel = -1;
        this.articleCount = 0;
        this.parents = new LinkedList<Integer>();
        this.children = new LinkedList<Integer>();
        this.descendants = new HashSet<Integer>();
        this.processedForTreeLevel = false;
    }
    
    public int getId() {
        return id;
    }
    
    public URI getUri() {
        return uri;
    }
    
    public void setTreeLevel(int treeLevel) {
        this.treeLevel = treeLevel;
        this.processedForTreeLevel = true;
    }
    
    public int getTreeLevel() {
        return treeLevel;
    }
    
    /*
     * These functions deal with managing the tree structure.
     */
    
    public void addParent(Integer parId) {
        parents.add(parId);
    }
    
    public List<Integer> getParents() {
        return parents;
    }
    
    public void addChild(Integer childId) {
        children.add(childId);
    }
    
    public List<Integer> getChildren() {
        return children;
    }
    
    public boolean isProcessedForTreeLevel() {
        return processedForTreeLevel;
    }
    
    /*
     * These functions deal with managing the article counting.
     */
    public void setArticleCount(double newArtCount) {
        this.articleCount = newArtCount;
    }
    
    public void addToArticleCount(double addArtCount) {
        this.articleCount += addArtCount;
    }
    
    public double getArticleCount() {
        return articleCount;
    }
    
    /*
     * These functions deal with managing the descendant counting.
     */
    public void addDescendants(Collection<Integer> descIds) {
        descendants.addAll(descIds);
    }
    
    public Set<Integer> getDescendants() {
        return descendants;
    }
    
    public boolean hasUnprocessedChildren() {
        return (children.size() != 0);
    }
    
    public int getDescendantCount() {
        if (children.size() != 0) {
            System.err.println("Reading desc size from category with unprocessed children!");
        }
        return descendants.size();
    }
    
    public void purgeDescendants() {
        descendants.clear();
        descendants = null;
    }
    
    public void removeChild(Integer descId) {
        children.remove(descId);
    }
}
