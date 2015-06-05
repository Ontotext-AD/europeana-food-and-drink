package com.ontotext.efd;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.RepositoryException;

import com.ontotext.efd.model.URIPair;
import com.ontotext.efd.rdf.EFDTaxonomy;
import com.ontotext.efd.services.EFDRepositoryConnection;

public class addEvidenceFromFile {
    
    private static final String ARTICLE_URI = EFDTaxonomy.DBR;
    private static final String EFD_CHILD_URI = EFDTaxonomy.EFD_CHILD;
    private static final String EFD_EVIDENCE_URI = EFDTaxonomy.EFD_LOC_EVD_CNT;
    private static final String DCT_SUBJECT_URI = EFDTaxonomy.DCT_SUBJECT;
    private static final String NO_MATCH = "NA";
    
    private static EFDRepositoryConnection repoConn;
    private static ValueFactory factory;
    
    public static void main(String[] args) throws IOException, RepositoryException {
        if (args == null || args.length != 1)
            return;
        
        factory = ValueFactoryImpl.getInstance();
        
        // Read concept evidence from file.
        Map<URI, Integer> artEvidenceCounts = parseEvidenceFromFile(args[0]);
        
        // Load in all Food and Drink categories.
        repoConn = new EFDRepositoryConnection();
        Map<URI, Integer> catEvidenceCounts = fetchEFDCategoryTree();
        
        // Connect concepts (articles) to their respective categories.
        URI artSubject = factory.createURI(DCT_SUBJECT_URI);
        for (URI art : artEvidenceCounts.keySet()) {
            Set<URI> cats = repoConn.readObjectsAsURI(art, artSubject);
            int fdCats = 0;
            for (URI cat : cats) {
                if (catEvidenceCounts.containsKey(cat)) {
                    fdCats++;
                    int c = catEvidenceCounts.get(cat);
                    catEvidenceCounts.put(cat, c+1);
                }
            }
            if (fdCats == 0) {
                System.out.println("Concept tagged with no FD categories: " + art);
            }
        }
        
        // Enter the newly calculated data into the repository, replacing old entries.
        updateRepository(catEvidenceCounts);
    }
    
    private static Map<URI, Integer> parseEvidenceFromFile(String path) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line;
        Map<URI, Integer> artEvidenceCounts = new HashMap<URI, Integer>();
        while ((line = br.readLine()) != null) {
            if (line.equals(NO_MATCH))
                continue;
            URI artUri = factory.createURI(ARTICLE_URI + line.trim());
            if (artEvidenceCounts.containsKey(artUri)) {
                int c = artEvidenceCounts.get(artUri);
                artEvidenceCounts.put(artUri, c+1);
            } else {
                artEvidenceCounts.put(artUri, 1);
            }
        }
        return artEvidenceCounts;
    }
    
    private static Map<URI, Integer> fetchEFDCategoryTree() {
        URI child = factory.createURI(EFD_CHILD_URI);
        List<URIPair> l = repoConn.readURIStatementsWithPredicate(child);
        Map<URI, Integer> catEvidenceCounts = new HashMap<URI, Integer>();
        for (URIPair p : l) {
            URI s = p.getSubject();
            if (!catEvidenceCounts.containsKey(s))
                catEvidenceCounts.put(s, 0);
            URI o = p.getObject();
            if (!catEvidenceCounts.containsKey(o)) 
                catEvidenceCounts.put(o, 0);
        }
        return catEvidenceCounts;
    }
    
    private static void updateRepository(Map<URI, Integer> counts) throws RepositoryException {
        URI pred = factory.createURI(EFD_EVIDENCE_URI);
        repoConn.removeStatementsWithPredicate(pred);
        
        for (URI cat : counts.keySet()) {
            int c = counts.get(cat);
            repoConn.queueAddStatement(cat, pred, c);
        }
        repoConn.flushWriteQueue();
    }

}
