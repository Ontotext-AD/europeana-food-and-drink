package com.ontotext.efd.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.openrdf.model.URI;

import com.ontotext.efd.model.EFDCategory;

public class CandidateList {
    
    Map<URI, EFDCategory> candidateSet;
    Queue<URI> candidateQueue;
    
    public CandidateList() {
        candidateSet = new HashMap<URI, EFDCategory>();
        candidateQueue = new LinkedList<URI>();
    }
    
    public boolean add(EFDCategory candidate) {
        if (candidate == null || candidate.getUri() == null) {
            System.err.println("Received invalid candidate to add to CandidateList.");
            return false;
        } else if (candidateSet.containsKey(candidate)) {
            System.err.println("Received repeat candidate to add to CandidateList");
            return false;
        }
        
        URI candidateName = candidate.getUri();
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
    
    public EFDCategory poll() throws Exception {
        if (candidateQueue.isEmpty())
            return null;
        
        URI nextCandidateName = candidateQueue.poll();
        if (!candidateSet.containsKey(nextCandidateName)) {
            throw new Exception("Internal discrepancy in CandidateList data. Cannot find candidate.");
        }
        EFDCategory nextCandidate = candidateSet.get(nextCandidateName);
        candidateSet.remove(nextCandidateName);
        return nextCandidate;
    }
    
    public EFDCategory get(URI catName) {
        if (!candidateSet.containsKey(catName))
            return null;
        return candidateSet.get(catName);
    }
    
    public int size() {
        return candidateQueue.size();
    }

}
