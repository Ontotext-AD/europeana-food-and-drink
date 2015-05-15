package com.ontotext.efd.services;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import com.ontotext.efd.model.EFDTree;
import com.ontotext.efd.model.TreeBuildingCat;
import com.ontotext.efd.rdf.EFDTaxonomy;

public class TreeBuilderService {

    private EFDTree tree;
    private Set<URI> irrelevantCategories;
    
    private EFDRepositoryConnection repoConn;
    private long timeForDb = 0;
    
    private int ll = 0; // Lowest level in our tree.
    
    // EFD predicates we are recording.
    private URI predLevel = new URIImpl(EFDTaxonomy.EFD_LEVEL);
    private URI predChild = new URIImpl(EFDTaxonomy.EFD_CHILD);

    /**
     * 
     * @param predLevel Which predicate to use for X hasTreeLevel Y.
     * @param predChild Which predicate to use for X hasChild Y.
     */
    public TreeBuilderService(URI predLevel, URI predChild) {
        this.predLevel = predLevel;
        this.predChild = predChild;
    }
    
    public void buildTreeFromRoot(URI rootName) {
        repoConn = new EFDRepositoryConnection();
        clearOldTree();
        
        // Data structures for the tree.
        tree = new EFDTree();
        irrelevantCategories = getIrrelevantCategories();
        
        processNewCategory(null, rootName);
        
        int i = 0;
        long start = System.currentTimeMillis();
        while (tree.hasCandidates()) {
            TreeBuildingCat cand = tree.popCandidate();
            processCatChildren(cand);
            
            if (++i%2500 == 0) {
                System.out.print("Processed " + i + " categories so far. ");
                System.out.print("Queue size: " + tree.queueSize() + ". ");
                System.out.print("Time spent with DB so far " + timeForDb/60000.0 + " minutes. ");
                System.out.println("Current level: " + cand.level + ". ");
            }
            
            if (tree.nearCapacity())
                break;
        }

        long stime = System.currentTimeMillis();
        repoConn.flushWriteQueue();
        timeForDb += System.currentTimeMillis() - stime;
        
        long end = System.currentTimeMillis();
        System.out.print("I made a tree in " + (end-start)/60000.0 + " minutes ");
        System.out.println("of which " + timeForDb/60000.0 + " were repository interactions.");
        System.out.println("Deepest level reached is " + ll);        
    }
    
    /**
     * Removes all previous efd:child, efd:treeLevel, efd:descCount and
     * similar triples from the repository so we can build the tree from
     * a clean slate. 
     */
    private void clearOldTree() {
        repoConn.removeStatementsWithPredicate(predChild);
        repoConn.removeStatementsWithPredicate(predLevel);
    }
    
    /**
     * Retrieves the list of all irrelevant categories so we can
     * compare any potential new categories and ensure we only create
     * the ones we are not supposed to ignore.
     * @return
     */
    private Set<URI> getIrrelevantCategories() {
        URI pred = new URIImpl(EFDTaxonomy.EFD_IRRELEVANT);
        Set<URI> irrCats = repoConn.readSubjectsAsURI(pred, null);
        return (irrCats != null) ? irrCats : new HashSet<URI>();
    }
    
    /**
     * Retrieves the minimum amount of data about the category
     * sufficient for tree building. In this case that means
     * just checking that the category is NOT marked irrelevant.
     * @param parent
     * @param name
     * @return
     */
    private TreeBuildingCat processNewCategory(TreeBuildingCat parent, URI name) {
        if (irrelevantCategories.contains(name))
            return null;
        
        int level;
        if (parent == null)
            level = 0;
        else
            level = parent.level + 1;
        
        TreeBuildingCat cat = tree.addCandidate(name, level);
        long stime = System.currentTimeMillis();
        repoConn.queueAddStatement(name, predLevel, Integer.toString(level));
        timeForDb += System.currentTimeMillis() - stime;
        return cat;
    }
    
    private void processCatChildren(TreeBuildingCat cat) {
        if (cat.level > ll)
            ll = cat.level;
        long stime = System.currentTimeMillis();
        List<URI> skosChildren = repoConn.getSkosChildren(cat.uri);
        timeForDb += System.currentTimeMillis() - stime;
        for (URI skosChild: skosChildren) {
            Integer childId = tree.getCategoryIdByUri(skosChild);
            if (childId == null) {
                // Never before encountered category. Create it before adding connection.
                TreeBuildingCat newCat = processNewCategory(cat, skosChild);
                if (newCat == null)
                    continue;
                stime = System.currentTimeMillis();
                makeConnection(cat, newCat);
                timeForDb += System.currentTimeMillis() - stime;
            } else if (tree.isCandidate(childId)) {
                // Encountered but not processed. Just add the connection.
                stime = System.currentTimeMillis();
                makeConnection(cat, tree.getCategory(childId));
                timeForDb += System.currentTimeMillis() - stime;
            } else {
                // It's already processed.
                if (!isAncestor(cat, childId)) {
                    stime = System.currentTimeMillis();
                    makeConnection(cat, tree.getCategory(childId));
                    timeForDb += System.currentTimeMillis() - stime;
                }
            }
        }
    }
    
    /**
     * Checks whether a category connection we are considering leads back
     * in the same branch (i.e. potential child is ancestor of potential
     * parent) or is cross-branch (i.e. the potential child is not an ancestor).
     * @param potParent this category's ancestors are found and compared.
     * @param potChild this category is compared to the ancestors.
     * @return
     */
    private boolean isAncestor(TreeBuildingCat potParent, Integer potChild) {
        
        Set<Integer> checked = new HashSet<Integer>();
        Queue<Integer> toCheck = new LinkedList<Integer>();
        toCheck.add(potParent.id);
        while(!toCheck.isEmpty()) {
            int ancestor = toCheck.poll();
            if (checked.contains(ancestor))
                continue;
            if (potChild.equals(ancestor))
                return true;
            checked.add(ancestor);
            toCheck.addAll(tree.getCategoryParents(ancestor));
        }
        return false;
    }
    
    private void makeConnection(TreeBuildingCat parent, TreeBuildingCat child) {
        tree.addConnection(parent.id, child.id);
        repoConn.queueAddStatement(parent.uri, predChild, child.uri);
    }
}
