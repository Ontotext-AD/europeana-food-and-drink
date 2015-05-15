package com.ontotext.efd.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.openrdf.model.URI;

public class EFDTree {
    
    private static final int TREE_MAX_SIZE = 1000000;

    private int nextId;
    private Map<Integer, TreeBuildingCat> idToCatMap;
    private Set<Integer> processedSet;
    private Set<Integer> candidateSet;
    private Map<URI, Integer> uriToIdMap;
    private Queue<Integer> candQueue;
    private List<Integer>[] efdParents;

    @SuppressWarnings("unchecked")
    public EFDTree() {        
        nextId = 0;
        idToCatMap = new HashMap<Integer, TreeBuildingCat>(TREE_MAX_SIZE*2);
        processedSet = new HashSet<Integer>();
        candidateSet = new HashSet<Integer>();
        uriToIdMap = new HashMap<URI, Integer>();
        candQueue = new LinkedList<Integer>();
        efdParents = (List<Integer>[]) new LinkedList<?>[TREE_MAX_SIZE];

    }
    
    public boolean isCandidate(int candId) {
        return candidateSet.contains(candId);
    }
    
    public boolean isProcessed(int procId) {
        return processedSet.contains(procId);
    }
    
    public boolean hasCandidates() {
        return !candQueue.isEmpty();
    }
    
    public TreeBuildingCat addCandidate(URI name, int level) {
        TreeBuildingCat cat = new TreeBuildingCat(nextId++, level, name);
        idToCatMap.put(cat.id, cat);
        uriToIdMap.put(cat.uri, cat.id);
        candidateSet.add(cat.id);
        candQueue.add(cat.id);
        efdParents[cat.id] = new LinkedList<Integer>();
        return cat;
    }
    
    public TreeBuildingCat popCandidate() {
        if (candQueue.isEmpty())
            return null;
        
        Integer candId = candQueue.poll();
        candidateSet.remove(candId);
        processedSet.add(candId);
        return idToCatMap.get(candId);
    }
    
    public int queueSize() {
        return candQueue.size();
    }
    
    public void addConnection(int parentId, int childId) {
        if (childId >= nextId || parentId >= nextId)
            return;
        efdParents[childId].add(parentId);
    }
    
    public TreeBuildingCat getCategory(int catId) {
        return idToCatMap.get(catId);
    }
    
    public Integer getCategoryIdByUri(URI name) {
        return uriToIdMap.get(name);
    }
    
    public List<Integer> getCategoryParents(int catId) {
        if (catId >= nextId)
            return null;
        return efdParents[catId];
    }
    
    public boolean nearCapacity() {
        return (nextId + 50 >= TREE_MAX_SIZE) ? true : false;
    }
}
