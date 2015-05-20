package com.ontotext.efd;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import com.ontotext.efd.rdf.EFDTaxonomy;
import com.ontotext.efd.services.EFDRepositoryConnection;

public class markIrrelevantFromCSV {

    /**
     * @param args
     * @throws IOException 
     */ 
    public static void main(String[] args) throws IOException {
        String line;
        String cvsDelim = ",";
        List<String> cats = new LinkedList<String>();
        BufferedReader br = new BufferedReader(new FileReader("/home/atagarev/test/efd/Level4-judgements.csv"));
        br.readLine(); // Discard first line.
        while ((line = br.readLine()) != null) {
            String[] tokens = line.split(cvsDelim);
            if (tokens.length < 3 || tokens[2] == null || tokens[0] == null) 
                continue;
            if (tokens[2].equals("no"))
                cats.add(tokens[0]);
        }
        System.out.println("Total categories to marked as irrelevant: " + cats.size());
        
        EFDRepositoryConnection repoConn = new EFDRepositoryConnection();
        URI pred = new URIImpl(EFDTaxonomy.EFD_IRRELEVANT);
        URI obj = new URIImpl(EFDTaxonomy.EFD_ANNOTATOR_MANUAL);
        for (String catStr : cats) {
            URI catUri = new URIImpl(EFDTaxonomy.DBCAT + catStr);
            repoConn.addStatementWithURI(catUri, pred, obj);
        }
        repoConn.flushWriteQueue();
    }

}
