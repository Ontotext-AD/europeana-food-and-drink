package com.ontotext.efd.services;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.RepositoryException;

import com.ontotext.efd.model.TreeCountingCat;
import com.ontotext.efd.model.URIIntPair;
import com.ontotext.efd.model.URIPair;
import com.ontotext.efd.rdf.EFDTaxonomy;

public class TreeCounterService {
    
    private EFDRepositoryConnection repoConn;
    ValueFactory factory;
    private URI childPred;
    private URI levelPred;
    private URI localArtCntPred;
    private URI descArtCntPred;
    private URI descCatCntPred;
    private URI localEvdCntPred;
    private URI evdScore1Pred;
    private URI evdScore2Pred;
    
    private boolean calcCategoryLevel;
    private boolean countDescArticles;
    private boolean countDescCategories;
    private boolean propagateEvidence;
    
    private static final double decay1 = 0.8;
    private static final double decay2 = 0.7;
    
    private int nextId;
    private Map<URI, Integer> uriToCatIdMap;
    private Map<Integer, TreeCountingCat> idToCatMap;
    
    public TreeCounterService(URI childPred, URI levelPred, URI descArtCntPred, URI descCatCntPred) {
        repoConn = new EFDRepositoryConnection();
        factory = ValueFactoryImpl.getInstance();
        this.childPred = childPred;
        this.levelPred = levelPred;
        this.localArtCntPred = factory.createURI(EFDTaxonomy.EFD_LOC_ART_CNT);
        this.descArtCntPred = descArtCntPred;
        this.descCatCntPred = descCatCntPred;
        this.localEvdCntPred = factory.createURI(EFDTaxonomy.EFD_LOC_EVD_CNT);
        this.evdScore1Pred = factory.createURI(EFDTaxonomy.EFD_EVD_PROP_1);
        this.evdScore2Pred = factory.createURI(EFDTaxonomy.EFD_EVD_PROP_2);
        
        this.calcCategoryLevel = true;
        this.countDescArticles = true;
        this.countDescCategories = true;
        this.propagateEvidence = true;
    }
    
    public void calculateTreeStats(URI root) throws RepositoryException {
        nextId = 0;
        uriToCatIdMap = new HashMap<URI, Integer>();
        idToCatMap = new HashMap<Integer, TreeCountingCat>();
        loadTree();
        
        if (calcCategoryLevel)
            calculateLevels(root);
        if (countDescArticles)
            loadLocalArticleCounts();
        if (propagateEvidence)
            loadLocalEvidenceCounts();
        if (countDescArticles || countDescCategories || propagateEvidence)
            calculateDescendants();
    }
    
    /**
     * Retrieves all efd:child triples from the repository and uses them to
     * build a directed graph (which should be a tree but the method 
     * doesn't check for that).
     */
    private void loadTree() {
        List<URIPair> resp = repoConn.readURIStatementsWithPredicate(childPred);
        for (URIPair p : resp) {
            URI parent = p.getSubject();
            if (!uriToCatIdMap.containsKey(parent))
                addNewCat(parent);
            
            URI child =  p.getObject();
            if (!uriToCatIdMap.containsKey(child))
                addNewCat(child);
            
            TreeCountingCat parCat = idToCatMap.get(uriToCatIdMap.get(parent));
            TreeCountingCat childCat = idToCatMap.get(uriToCatIdMap.get(child));
            makeConnection(parCat, childCat);
        }
    }

    private void addNewCat(URI uri) {
        TreeCountingCat nc = new TreeCountingCat(nextId++, uri);
        uriToCatIdMap.put(nc.getUri(), nc.getId());
        idToCatMap.put(nc.getId(), nc);        
    }
    
    private void makeConnection(TreeCountingCat parent, TreeCountingCat child) {
        child.addParent(parent.getId());
        parent.addChild(child.getId());
    }
    
    /**
     * Starting at a particular node in our category tree, calculates
     * the shortest path to all nodes that can be reached from it using BFS.
     * @param rootUri
     * @throws RepositoryException
     */
    private void calculateLevels(URI rootUri) throws RepositoryException {
        repoConn.removeStatementsWithPredicate(levelPred);
        Integer rootId = uriToCatIdMap.get(rootUri);
        if (rootId == null)
            return;
        TreeCountingCat root = idToCatMap.get(rootId);
        root.setTreeLevel(0);
        Queue<TreeCountingCat> queue = new LinkedList<TreeCountingCat>();
        queue.add(root);
        repoConn.queueAddStatement(root.getUri(), levelPred, root.getTreeLevel());
        
        while(!queue.isEmpty()) {
            TreeCountingCat cat = queue.poll();
            for (Integer potId : cat.getChildren()) {
                TreeCountingCat pot = idToCatMap.get(potId);
                if (pot == null) {
                    System.err.println("What? Why is there a null here?!?");
                    continue;
                }
                if (pot.isProcessedForTreeLevel())
                    continue;
                
                pot.setTreeLevel(cat.getTreeLevel() + 1);
                queue.add(pot);
                repoConn.queueAddStatement(pot.getUri(), levelPred, pot.getTreeLevel());
            }
        }
        repoConn.flushWriteQueue();
    }
    
    /**
     * Reads the number of articles that have each category as a dct:subject target.
     * This data is not usually available in dbpedia and is slow to calculate so it
     * assumes the counting has been performed previously by the LocalArticleCounterService.
     */
    private void loadLocalArticleCounts() {
        List<URIIntPair> r = repoConn.readIntStatementsWithPredicate(localArtCntPred);
        for (URIIntPair p : r) {
            URI catUri = p.getSubject();
            int count = p.getObject();
            Integer catId = uriToCatIdMap.get(catUri);
            if (catId != null) {
                TreeCountingCat cat = idToCatMap.get(catId);
                cat.setArticleCount(count);
            }
        }
    }
    
    
    private void loadLocalEvidenceCounts() {
        List<URIIntPair> r = repoConn.readIntStatementsWithPredicate(localEvdCntPred);
        for (URIIntPair p : r) {
            URI catUri = p.getSubject();
            int count = p.getObject();
            Integer catId = uriToCatIdMap.get(catUri);
            if (catId != null) {
                TreeCountingCat cat = idToCatMap.get(catId);
                cat.setLocalEvidenceCount(count);
            }
        }
    }
    
    /**
     * Function that performs bottom up counting of both the number of
     * descended categories (unique descended categories by id but
     * runs into trouble with memory if the tree is bigger than a few
     * hundred thousand categories) and the number of descended articles
     * (a heuristic that estimates the number of dct:subject triples
     * with a descended category as target).
     * @throws RepositoryException
     */
    private void calculateDescendants() throws RepositoryException {
        repoConn.removeStatementsWithPredicate(descArtCntPred);
        repoConn.removeStatementsWithPredicate(descCatCntPred);
        repoConn.removeStatementsWithPredicate(evdScore1Pred);
        repoConn.removeStatementsWithPredicate(evdScore2Pred);
        
        Queue<Integer> queue = new LinkedList<Integer>();
        
        // Find all leaf categories to start bottom up counting.
        for (Integer candId : idToCatMap.keySet()) {
            TreeCountingCat cat = idToCatMap.get(candId);
            cat.addDescendants(cat.getChildren());
            if (!cat.hasUnprocessedChildren()) {
                queue.add(candId);
            }
        }
        
        // 
        while (!queue.isEmpty()) {
            Integer leafId = queue.poll();
            TreeCountingCat leaf = idToCatMap.get(leafId);            
            double contrib = leaf.getArticleCount() / leaf.getParents().size();
            int descCount = leaf.getDescendantCount();
            double localS1 = 1.0 - Math.pow(Math.E, -leaf.getLocalEvidenceCount());
            double inherS1 = leaf.getEvidenceScore1();
            double s1 = (localS1 > inherS1) ? localS1 : inherS1;
            double s2 = leaf.getLocalEvidenceCount() + leaf.getEvidenceScore2();
            
            // Mark category as processed with all parents and add
            // parents with no unprocessed children to the queue.
            for (Integer parId : leaf.getParents()) {
                TreeCountingCat par = idToCatMap.get(parId);
                par.removeChild(leafId);
                
                // Perform descended article counting (heuristic).
                if (countDescArticles)
                    par.addToArticleCount(contrib);
              
                // Perform descended article counting (resource intensive).
                if (countDescCategories)
                    par.addDescendants(leaf.getDescendants());
                
                // Perform evidence propagation.
                if (propagateEvidence) {
                    // Evidence propagation with first approach.
                    double ps1 = par.getEvidenceScore1();
                    if (ps1 < s1*decay1)
                        par.setEvidenceScore1(s1*decay1);
                    
                    // Evidence propagation with second approach
                    double ps2 = par.getEvidenceScore2();
                    if (par.getTreeLevel() < leaf.getTreeLevel())
                        par.setEvidenceScore2(ps2 + s2);
                    else if (par.getTreeLevel() == leaf.getTreeLevel())
                        par.setEvidenceScore2(ps2 + decay2*s2);
                }

                if (!par.hasUnprocessedChildren())
                    queue.add(parId);
            }
            if (countDescArticles)
                repoConn.queueAddStatement(leaf.getUri(), descArtCntPred, leaf.getArticleCount());
            if (countDescCategories) {
                repoConn.queueAddStatement(leaf.getUri(), descCatCntPred, descCount);
                leaf.purgeDescendants(); // Saves memory space.
            }
            if (propagateEvidence) {
                repoConn.queueAddStatement(leaf.getUri(), evdScore1Pred, s1);
                repoConn.queueAddStatement(leaf.getUri(), evdScore2Pred, s2);
            }
        }
        repoConn.flushWriteQueue();
    }
}
