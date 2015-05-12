package com.ontotext.efd.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.openrdf.model.URI;

import com.ontotext.efd.model.WorkCat;
import com.ontotext.efd.services.EFDRepositoryConnection;

public class AltTreeBuilder {
    
    int nextId;
    Map<Integer, WorkCat> idToCatMap;
    Map<Integer, WorkCat> idToCandMap;
    Map<URI, Integer> uriToIdMap;
    Queue<Integer> candQueue;
    
    EFDRepositoryConnection repoConn;
    int ll = 0; // Lowest level in our tree.
    long timeForDb = 0;

    public AltTreeBuilder(URI rootName) {
        nextId = 0;
        repoConn = new EFDRepositoryConnection();
        idToCatMap = new HashMap<Integer, WorkCat>();
        idToCandMap = new HashMap<Integer, WorkCat>();
        uriToIdMap = new HashMap<URI, Integer>();
        candQueue = new LinkedList<Integer>();
        
        WorkCat root = new WorkCat(nextId++, 0, rootName);
        uriToIdMap.put(root.uri, root.id);
        idToCandMap.put(root.id, root);
        candQueue.add(root.id);
        processCatChildren(root);
        
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
        }
        long end = System.currentTimeMillis();
        System.out.print("I made a tree in " + (end-start)/1000 + " seconds ");
        System.out.println("of which " + timeForDb/1000 + " seconds were DB retrieval.");
        System.out.println("Deepest level reached is " + ll);
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
                WorkCat newCat = new WorkCat(nextId++, cat.level+1, skosChild);
                idToCandMap.put(newCat.id, newCat);
                uriToIdMap.put(newCat.uri, newCat.id);
                candQueue.add(newCat.id);
            } else if (idToCandMap.containsKey(childId)) {
                // Encountered but not processed. Just add the connection.
            } else {
                // It's already processed.
            }
        }
    }
}
