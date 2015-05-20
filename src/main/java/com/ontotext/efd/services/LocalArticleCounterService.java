package com.ontotext.efd.services;

import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.repository.RepositoryException;

import com.ontotext.efd.model.URIIntPair;
import com.ontotext.efd.rdf.EFDTaxonomy;


/**
 * This class is intended to count every dct:subject connection made
 * to a category and upload the counts to the repository in the form
 * of dbcat:X efd:localArticleCount xsd:int triples. This is used to
 * make an estimate of the number of descended categories under each
 * node but the counting is a slow process that involves a lot of 
 * reading from the repository (well over 20 million dct:subject triples)
 * so should only be run when the underlying dbpedia data is changed.
 * @author atagarev
 *
 */
public class LocalArticleCounterService {
  
  private URI articlePred;
  private URI countPred;
  private EFDRepositoryConnection repoConn;
  
  public LocalArticleCounterService() {
    this.articlePred = DCTERMS.SUBJECT;
    ValueFactory factory = ValueFactoryImpl.getInstance();
    this.countPred = factory.createURI(EFDTaxonomy.EFD_LOC_ART_CNT);
    repoConn = new EFDRepositoryConnection();
  }
  
  public void countArticles() throws RepositoryException {
      try {
          clearOldCounts();
      } catch (RepositoryException e) {
          System.err.println("Failed to remove old entries from database.");
          e.printStackTrace();
          throw e;
      }
      System.out.println("Cleared all count entries.");
      List<URIIntPair> counts = repoConn.countArticleToCategoryConnections(articlePred);
      System.out.println("Finished counting connections.");
      writeArticleCounts(counts);
      System.out.println("Wrote the new count entries to repo.");
  }

  private void clearOldCounts() throws RepositoryException {
      repoConn.removeStatementsWithPredicate(countPred);
  }

  private void writeArticleCounts(List<URIIntPair> counts) {
      System.out.println("About to write " + counts.size() + " article counts for categories.");
      int i = 0;
      for (URIIntPair countEntry : counts) {
          URI cat = countEntry.getSubject();
          Integer count = countEntry.getObject();
          repoConn.queueAddStatement(cat, countPred, count);
          i++;
      }
      repoConn.flushWriteQueue();
      System.out.println("Wrote " + i + " article counts for categories.");
  }
  
}
