package com.ontotext.efd.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import com.ontotext.efd.model.WorkCat;
import com.ontotext.efd.rdf.EFDTaxonomy;
import com.ontotext.efd.services.EFDRepositoryConnection;

public class AltTreeBuilder {
    
    int nextId;
    Map<Integer, WorkCat> idToCatMap;
    Map<Integer, WorkCat> idToCandMap;
    Map<URI, Integer> uriToIdMap;
    Queue<Integer> candQueue;
    List<Integer>[] efdParents;
    
    EFDRepositoryConnection repoConn;
    int ll = 0; // Lowest level in our tree.
    long timeForDb = 0;
    
    // EFD predicates we are recording.
    URI predLevel = new URIImpl(EFDTaxonomy.EFD_LEVEL);
    URI predChild = new URIImpl(EFDTaxonomy.EFD_CHILD);

    public AltTreeBuilder(URI rootName) {
        nextId = 0;
        repoConn = new EFDRepositoryConnection();
        idToCatMap = new HashMap<Integer, WorkCat>();
        idToCandMap = new HashMap<Integer, WorkCat>();
        uriToIdMap = new HashMap<URI, Integer>();
        candQueue = new LinkedList<Integer>();
        efdParents = new LinkedList[1000000];
        
        processNewCategory(null, rootName);
        
        int i = 0;
        long start = System.currentTimeMillis();
        while (!candQueue.isEmpty()) {
            Integer candId = candQueue.poll();
            WorkCat cand = idToCandMap.get(candId);
            processCatChildren(cand);
            idToCatMap.put(candId, cand);
            
            if (++i%2500 == 0) {
                System.out.print("Processed " + i + " categories so far. ");
                System.out.print("Queue size: " + candQueue.size() + ". ");
                System.out.println("Curren level: " + cand.level + ". ");
            }
            
            if (i == 10000)
                break;
        }
        long end = System.currentTimeMillis();
        System.out.print("I made a tree in " + (end-start)/1000 + " seconds ");
        System.out.println("of which " + timeForDb/1000 + " seconds were DB retrieval.");
        System.out.println("Deepest level reached is " + ll);
    }
    
    private WorkCat processNewCategory(WorkCat parent, URI name) {
        int level;
        if (parent == null)
            level = 0;
        else
            level = parent.level + 1;
        
        WorkCat cat = new WorkCat(nextId++, level, name);
        uriToIdMap.put(cat.uri, cat.id);
        idToCandMap.put(cat.id, cat);
        candQueue.add(cat.id);
        repoConn.addStatementWithLiteral(cat.uri, predLevel, Integer.toString(cat.level));
        return cat;
    }
    
    private void processCatChildren(WorkCat cat) {
        if (cat.level > ll)
            ll = cat.level;
        long stime = System.currentTimeMillis();
        List<URI> skosChildren = repoConn.getSkosChildren(cat.uri);
        timeForDb += System.currentTimeMillis() - stime;
        for (URI skosChild: skosChildren) {
            Integer childId = uriToIdMap.get(skosChild);
            if (childId == null) {
                // Never before encountered category. Create it before adding connection.
                WorkCat newCat = processNewCategory(cat, skosChild);
                makeConnection(cat, newCat);
            } else if (idToCandMap.containsKey(childId)) {
                // Encountered but not processed. Just add the connection.
                makeConnection(cat, idToCandMap.get(childId));
            } else {
                // It's already processed.
                if (!isAncestor(cat, childId)) {
                    makeConnection(cat, idToCatMap.get(childId));
                }
            }
        }
    }
    
    private boolean isAncestor(WorkCat potParent, Integer potChild) {
        
        return true;
    }
    
    private void makeConnection(WorkCat parent, WorkCat child) {
        efdParents[child.id].add(parent.id);
        repoConn.addStatementWithURI(parent.uri, predChild, child.uri);
    }
}
