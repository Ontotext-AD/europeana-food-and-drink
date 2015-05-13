package com.ontotext.efd.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.openrdf.model.URI;

import com.ontotext.efd.model.WorkCat;

public class CandidateList {
    
    Map<URI, WorkCat> candidateSet;
    Queue<URI> candidateQueue;
    
    public CandidateList() {
        candidateSet = new HashMap<URI, WorkCat>();
        candidateQueue = new LinkedList<URI>();
    }
    
    public boolean add(WorkCat candidate) {
        if (candidate == null || candidate.uri == null) {
            System.err.println("Received invalid candidate to add to CandidateList.");
            return false;
        } else if (candidateSet.containsKey(candidate)) {
            System.err.println("Received repeat candidate to add to CandidateList");
            return false;
        }
        
        URI candidateName = candidate.uri;
        candidateSet.put(candidateName, candidate);
        candidateQueue.add(candidateName);
        return true;
    }
    
    public boolean contains(URI candidateUri) {
        return candidateSet.containsKey(candidateUri);
    }
    
    public boolean isEmpty() {
        return candidateQueue.isEmpty();
    }
    
    public WorkCat poll() throws Exception {
        if (candidateQueue.isEmpty())
            return null;
        
        URI nextCandidateName = candidateQueue.poll();
        if (!candidateSet.containsKey(nextCandidateName)) {
            throw new Exception("Internal discrepancy in CandidateList data. Cannot find candidate.");
        }
        WorkCat nextCandidate = candidateSet.get(nextCandidateName);
        candidateSet.remove(nextCandidateName);
        return nextCandidate;
    }
    
    public WorkCat get(URI catName) {
        if (!candidateSet.containsKey(catName))
            return null;
        return candidateSet.get(catName);
    }
    
    public int size() {
        return candidateQueue.size();
    }

}
