package com.ontotext.efd.services;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openrdf.repository.RepositoryException;

/**
 * This does test the local counter service but since
 * the service operates with all wikipedia categories
 * by default, there is really no way to test it on a
 * subset of the available data. The test will run a
 * long time and probably should not be included in test suites.
 * @author atagarev
 *
 */
public class LocalArticleCounterServiceTest {

    @Test
    public void test() {
        LocalArticleCounterService counter = new LocalArticleCounterService();
        try {
            long sT = System.currentTimeMillis();
            counter.countArticles();
            long eT = System.currentTimeMillis();
            System.out.print("Counted all local articles and wrote numbers to DB in ");
            System.out.println((eT-sT)/60000.0 + " minutes.");
        } catch (RepositoryException e) {
            System.err.println("The counter encountered some trouble with the repo.");
            e.printStackTrace();
            fail();
        }
    }

}
