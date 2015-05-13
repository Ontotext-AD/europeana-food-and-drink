package com.ontotext.efd.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import com.ontotext.efd.model.WorkCat;
import com.ontotext.efd.rdf.EFDTaxonomy;
import com.ontotext.efd.services.EFDRepositoryConnection;

public class TreeBuilder {
    
    Map<Integer, WorkCat> efdCategories;
    CandidateList efdCandidates;
    List<Integer>[] efdParents;
    long timeForDb = 0;
    int catId = 0;
    
    EFDRepositoryConnection repoConn;
    URI predicate;

    public TreeBuilder(URI rootName) {
        repoConn = new EFDRepositoryConnection();
        efdCategories = new HashMap<Integer, WorkCat>(250000);
        efdCandidates = new CandidateList();
        efdParents = new LinkedList[1000000];
        predicate = new URIImpl(EFDTaxonomy.EFD_CHILD);
        
        WorkCat root = new WorkCat(catId++, 0, rootName);
        efdParents[root.id] = new LinkedList<Integer>();
        efdCategories.put(root.id, root);
        processCatChildren(root);
        
        int i = 0;
        int ll = 0;
        long start = System.currentTimeMillis();
        while (!efdCandidates.isEmpty()) {
            // Just debugging/tracking progress.
            if (ll > 5)
                break;
            if (i++%1000 == 0) {
                System.out.print("Processsed node number " + i + ". ");
                System.out.print("Max depth reached is " + ll + ". ");
                System.out.println("Candidates to look at " + efdCandidates.size() + ".");
            }
            
            try {
                WorkCat cat = efdCandidates.poll();
                efdCategories.put(cat.id, cat);
                processCatChildren(cat);
                if (cat.level > ll)
                    ll = cat.level;
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
        long end = System.currentTimeMillis();
        System.out.print("I made a tree in " + (end-start)/1000 + " seconds ");
        System.out.println("of which " + timeForDb/1000 + " seconds were DB retrieval.");
        System.out.println("Deepest level reached is " + ll);
    }
    
    private void processCatChildren(WorkCat cat) {
        URI catUri = cat.uri;
        long stime = System.currentTimeMillis();
        List<URI> skosChildren = repoConn.getSkosChildren(catUri);
        long etime = System.currentTimeMillis();
        timeForDb += etime - stime;
        for (URI skosChild : skosChildren) {
            if (efdCategories.containsKey(skosChild)) {
                // Node has been encountered and processed before. Ensure it is not
                // ancestor of the current node before adding the connection to
                // avoid adding a loop to the EFD graph.
                
                WorkCat propChild = efdCategories.get(skosChild);
                if (!checkAncestry(cat, propChild)) {
                    makeConnection(cat, propChild);
                }
                
            } else if (efdCandidates.contains(skosChild)) {
                // Node has already been encountered but not yet processed,
                // therefore it is not an ancestor and we can safely add the connection.
                WorkCat child = efdCandidates.get(skosChild);
                makeConnection(cat, child);
            } else {
                // First time we encounter this URI. Add it to candidates.
                WorkCat child = new WorkCat(catId++, cat.level + 1, skosChild);
                efdParents[child.id] = new LinkedList<Integer>();
                efdCandidates.add(child);
                makeConnection(cat, child);
            }
        }
    }
    
    /**
     * Checks whether the proposed child is already an ancestor of this node.
     * @param propChild The already considered category that might be an EFD child.
     * @param currCat The category being currently considered.
     * @return Returns true if the proposed child is already an ancestor and false otherwise.
     */
    private boolean checkAncestry(WorkCat currCat, WorkCat propChild) {
        // Check that this category is not the proposed child (recursion is fun).
        if (currCat.equals(propChild))
            return true;
        
        // Check whether one of the parent branches contains the proposed child.
        for (int catId : efdParents[currCat.id]) {
            WorkCat cat = efdCategories.get(catId);
            if (checkAncestry(cat, propChild))
                return true;
        }
        // We have checked everything up from this node and propChild is not an ancestor.
        return false;
    }
    
    /**
     * Make a connection between an EFD parent and child category.
     * @param parent The object in the skos:broader relationship that we judge relevant to EFD.
     * @param child The subject in the skos:broader relationship that we judge relevant to EFD.
     */
    private void makeConnection(WorkCat parent, WorkCat child) {
        /*
        Set<URI> children = parent.getChildren();
        children.add(child.getUri());
        parent.setChildren(children);
        Set<URI> parents = child.getParents();
        parents.add(parent.getUri());
        child.setParents(parents);
        */
        
        // Add to GraphDb. Hopefully it works :D
        EFDRepositoryConnection repoConn = new EFDRepositoryConnection();
        repoConn.addStatementWithURI(parent.uri, predicate, child.uri);
    }
}
