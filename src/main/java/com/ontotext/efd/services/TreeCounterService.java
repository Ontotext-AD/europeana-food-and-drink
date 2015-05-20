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
    
    private boolean calcCategoryLevel;
    private boolean countDescArticles;
    private boolean countDescCategories;
    
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
        
        // TODO: Allow these variables to be set by the user.
        this.calcCategoryLevel = true;
        this.countDescArticles = true;
        this.countDescCategories = false;
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
        if (countDescArticles || countDescCategories)
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
            
            // Mark category as processed with all parents and add
            // and parents with no unprocessed children to the queue.
            for (Integer parId : leaf.getParents()) {
                TreeCountingCat par = idToCatMap.get(parId);
                par.removeChild(leafId);
                
                // Perform descended article counting (heuristic).
                if (countDescArticles)
                    par.addToArticleCount(contrib);
              
                // Perform descended article counting (resource intensive).
                if (countDescCategories)
                    par.addDescendants(leaf.getDescendants());

                if (!par.hasUnprocessedChildren())
                    queue.add(parId);
            }
            if (countDescArticles)
                repoConn.queueAddStatement(leaf.getUri(), descArtCntPred, leaf.getArticleCount());
            if (countDescCategories) {
                repoConn.queueAddStatement(leaf.getUri(), descCatCntPred, descCount);
                leaf.purgeDescendants(); // Saves memory space.
            }
        }
        repoConn.flushWriteQueue();
    }
}
