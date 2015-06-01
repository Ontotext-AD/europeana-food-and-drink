package com.ontotext.efd.services;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.http.HTTPRepository;
import org.springframework.stereotype.Service;

import com.ontotext.efd.model.URIIntPair;
import com.ontotext.efd.model.URIPair;

@Service
public class EFDRepositoryConnection {

    //@org.springframework.beans.factory.annotation.Value("${sesame.server}")
    private String sesameServer = "http://192.168.130.19:8087/openrdf-sesame/";

    //@org.springframework.beans.factory.annotation.Value("${repository.id}")
    private String repositoryID = "DBpedia-efd-inf";
    
    private Model queuedWrites;
    private int WRITE_QUEUE_THRESHOLD = 5000;
    
    public EFDRepositoryConnection() {
        super();
        queuedWrites = new LinkedHashModel();
    }

    /**
     * Open a connection to the repository. Used by all the methods for
     * reading, writing and deleting stuff from the repository.
     * @return
     */
    private Repository getRepository(){
        Repository repo = new HTTPRepository(sesameServer, repositoryID);
        try {
            repo.initialize();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        return repo;
    }
    
    /**
     * Creates a statement object from the three provided strings.
     * Any nulls are replaced by a wildcard in the statement.
     * @param subject
     * @param predicate
     * @param object
     * @return
     */
    /*
    private Statement createStatement(String subject, String predicate, String object) {
        ValueFactory factory = ValueFactoryImpl.getInstance();
        URI s = (subject != null) ? factory.createURI(subject) : null;
        URI p = (predicate != null) ? factory.createURI(predicate) : null;
        URI o = (object != null) ? factory.createURI(object) : null;
        Statement statement = new StatementImpl(s, p, o);
        return statement;
    }
    */

    /**
     * Used during the tree building stage when we have skos:broader
     * connections to go off of. Shouldn't be used when actually 
     * displaying the tree to the UI.
     * @param uri The URI of the category whose children we are retrieving.
     * @return A list of the category's children as URIs.
     */
    public List<URI> getSkosChildren(URI uri) {
        List<URI> candidates = new LinkedList<URI>();
        try {
            Repository repo = getRepository();
            RepositoryConnection con = repo.getConnection();
            try {
                String rq = "SELECT * WHERE { " + 
                        "?cat <http://www.w3.org/2004/02/skos/core#broader> <" +
                        uri.toString() + "> . }";
                TupleQuery tupleQuery = con.prepareTupleQuery(
                        QueryLanguage.SPARQL, rq);
                TupleQueryResult result = tupleQuery.evaluate();
                try {
                    // Extract the potential children URIs from the response.
                    while (result.hasNext()) {
                        BindingSet bindingSet = result.next();
                        candidates.add((URI) bindingSet.getValue("cat"));
                    }
                } finally {
                    result.close();
                }
            } finally {
                con.close();
            }
        } catch (OpenRDFException e) {
            // handle exception

            System.err.println("Oops.");
            e.printStackTrace();
        }
        return candidates;
    }

    /**
     * Insert the provided triple into the database where
     * all three part are represented as a URI.
     * @param s
     * @param p
     * @param o
     */
    public void addStatementWithURI(URI s, URI p, URI o) { 
        // Check that we are given actual strings for the URIs.
        if (s == null || s.toString() == null || 
            p == null || p.toString() == null || 
            o == null  || o.toString() == null) {
            return;
        }

        Repository repo = getRepository();
        RepositoryConnection connection;

        try {
            connection = repo.getConnection();
            connection.begin();
            Statement statement = new StatementImpl(s, p, o);
            connection.add(statement);
            connection.commit();
            connection.close();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Insert the provided triple into the databse where
     * the object is treated as a literal.
     * @param s
     * @param p
     * @param o
     */
    public void addStatementWithLiteral(URI s, URI p, String o) { 
        // Check that we are given actual strings for the URIs.
        if (s == null || s.toString() == null || 
            p == null || p.toString() == null || o == null) {
            return;
        }

        Repository repo = getRepository();
        RepositoryConnection connection;

        try {
            connection = repo.getConnection();
            connection.begin();
            ValueFactory factory = ValueFactoryImpl.getInstance();
            Statement statement = new StatementImpl(s, p, factory.createLiteral(o));
            connection.add(statement);
            connection.commit();
            connection.close();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Adds a statement to the writing queue. If the queue
     * is full, writes all statements from it into the repo.
     * This version of the method deals with a URI object.
     * If any of the provided URIs are null, the triple
     * will not be added to the writing queue.
     * @param s
     * @param p
     * @param o
     */
    public void queueAddStatement(URI s, URI p, URI o) {
        if (s == null || p == null || o == null)
            return;
        queuedWrites.add(s, p, o);
        if (queuedWrites.size() >= WRITE_QUEUE_THRESHOLD)
            flushWriteQueue();
    }
    
    /**
     * Adds a statement to the writing queue. If the queue
     * is full, writes all statements from it into the repo.
     * This version of the method deals with a Literal object.
     * If any of the provided URIs or Label are null, the
     * triple will not be added to the writing queue.
     * @param s URI subject
     * @param p URI predicate
     * @param l String literal
     */
    public void queueAddStatement(URI s, URI p, String l) {
        ValueFactory factory = ValueFactoryImpl.getInstance();
        queueAddStatement(s, p, factory.createLiteral(l));
    }

    /**
     * Adds a statement to the writing queue. If the queue
     * is full, writes all statements from it into the repo.
     * This version of the method deals with a Literal object.
     * If any of the provided URIs or Label are null, the
     * triple will not be added to the writing queue.
     * @param s URI subject
     * @param p URI predicate
     * @param l Integer literal
     */
    public void queueAddStatement(URI s, URI p, Integer l) {
        ValueFactory factory = ValueFactoryImpl.getInstance();
        queueAddStatement(s, p, factory.createLiteral(l));
    }

    /**
     * Adds a statement to the writing queue. If the queue
     * is full, writes all statements from it into the repo.
     * This version of the method deals with a Literal object.
     * If any of the provided URIs or Label are null, the
     * triple will not be added to the writing queue.
     * @param s URI subject
     * @param p URI predicate
     * @param l Double literal
     */
    public void queueAddStatement(URI s, URI p, Double l) {
        ValueFactory factory = ValueFactoryImpl.getInstance();
        queueAddStatement(s, p, factory.createLiteral(l));
    }
    
    private void queueAddStatement(URI s, URI p, Literal l) {
        if (s == null || p == null || l == null)
            return;
        queuedWrites.add(s, p, l);
        if (queuedWrites.size() >= WRITE_QUEUE_THRESHOLD)
            flushWriteQueue();
    }
 
    /**
     * Writes all the statements queued up for the database.
     */
    public void flushWriteQueue() {
        
        if (queuedWrites.isEmpty())
            return;

        Repository repo = getRepository();
        RepositoryConnection connection;

        try {
            connection = repo.getConnection();
            connection.begin();
            connection.add(queuedWrites);
            connection.commit();
            connection.close();
            queuedWrites.clear();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        
    }
 
    /**
     * Looks for a triple with the specified subject and predicate and,
     * assuming it is unique, returns the object interpreted as a literal.
     * If the subject or predicate are null, returns null.
     * @param subject URI of the triple's subject.
     * @param predicate URi of the triple's predicate.
     * @return a String value representing the object literal.
     */
    public String readObjectAsLiteral(URI subject, URI predicate) {
        if (subject == null || predicate == null)
            return null;
        
        Repository repo = getRepository();
        RepositoryConnection connection;
        
        try {
            connection = repo.getConnection();
            connection.begin();
            RepositoryResult<Statement> statements = 
                    connection.getStatements(subject, predicate, null, false);
            if (statements.hasNext()) {
                Statement s = statements.next();
                Value r = s.getObject();
                return r.stringValue();
            }
            
            connection.close();            
        } catch (RepositoryException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println(e.getCause());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Looks for triples with the specified subject and predicate and
     * tries to interpret their objects as URIs that are all returned.
     * If the subject or predicate are null, returns null.
     * @param subject URI of the triple's subject.
     * @param predicate URI of the triple's predicate.
     * @return Set of all URIs encountered as objects in the triples.
     */
    public Set<URI> readObjectsAsURI(URI subject, URI predicate) {
        if (subject == null || predicate == null)
            return null;
        
        Set<URI> resp = new HashSet<URI>();
        Repository repo = getRepository();
        RepositoryConnection connection;
        
        try {
            connection = repo.getConnection();
            connection.begin();
            RepositoryResult<Statement> statements = 
                    connection.getStatements(subject, predicate, null, false);
            while (statements.hasNext()) {
                Statement s = statements.next();
                String obj = s.getObject().stringValue();
                ValueFactory factory = ValueFactoryImpl.getInstance();
                resp.add(factory.createURI(obj));
            }
            connection.close();            
        } catch (RepositoryException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println(e.getCause());
            e.printStackTrace();
        }
        
        return resp;
    }
    
    /**
     * Looks for triples with the specified predicate and object and
     * tries to interpret their subjects as URIs that are all returned.
     * If the predicate is null, returns null. If object is null, returns
     * all subjects without duplicates (because Set<URI).
     * @param predicate URI of the triple's predicate.
     * @param object URI of the triple's object.
     * @return Set of all URIs encountered as subjects in the triples.
     */
    public Set<URI> readSubjectsAsURI(URI predicate, URI object) {
        if (predicate == null) 
            return null;
        
        Set<URI> resp = new HashSet<URI>();
        Repository repo = getRepository();
        RepositoryConnection connection;
        
        try {
            connection = repo.getConnection();
            connection.begin();
            RepositoryResult<Statement> statements = 
                    connection.getStatements(null, predicate, object, false);
            while (statements.hasNext()) {
                Statement s = statements.next();
                String subj = s.getSubject().stringValue();
                ValueFactory factory = ValueFactoryImpl.getInstance();
                resp.add(factory.createURI(subj));
            }
            connection.close();            
        } catch (RepositoryException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println(e.getCause());
            e.printStackTrace();
        }
        
        return resp;
    }
    
    /**
     * Retrieves all triples connected with the provided predicate and
     * returns the list of all subject-object pairs in the repository.
     * Assumes all the subjects and objects are valid URIs.
     * @param predicate
     * @return
     */
    public List<URIPair> readURIStatementsWithPredicate(URI predicate) {
        if (predicate == null) 
            return null;
        
        List<URIPair> resp = new LinkedList<URIPair>();
        Repository repo = getRepository();
        RepositoryConnection connection;
        
        try {
            connection = repo.getConnection();
            connection.begin();
            RepositoryResult<Statement> statements = 
                    connection.getStatements(null, predicate, null, false);
            while (statements.hasNext()) {
                Statement s = statements.next();
                String subj = s.getSubject().stringValue();
                String obj = s.getObject().stringValue();
                ValueFactory factory = ValueFactoryImpl.getInstance();
                resp.add(new URIPair(factory.createURI(subj), factory.createURI(obj)));
            }
            connection.close();            
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        
        return resp;        
    }
    
    /**
     * Retrieves all triples connected with the provided predicate and
     * returns the list of all subject-object pairs in the repository.
     * Assumes all subjects are valid URIs and all objects are xsd:int.
     * @param predicate
     * @return
     */
    public List<URIIntPair> readIntStatementsWithPredicate(URI predicate) {
        if (predicate == null) 
            return null;
        
        List<URIIntPair> resp = new LinkedList<URIIntPair>();
        Repository repo = getRepository();
        RepositoryConnection connection;
        
        try {
            connection = repo.getConnection();
            connection.begin();
            RepositoryResult<Statement> statements = 
                    connection.getStatements(null, predicate, null, false);
            while (statements.hasNext()) {
                Statement s = statements.next();
                String subj = s.getSubject().stringValue();
                String obj = s.getObject().stringValue();
                ValueFactory factory = ValueFactoryImpl.getInstance();
                resp.add(new URIIntPair(factory.createURI(subj), Integer.parseInt(obj)));
            }
            connection.close();            
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        
        return resp;              
    }
    
    /**
     * Removes a specified triple with a URI object. If 
     * predicate is null or subject AND object are null, 
     * does nothing. If only one of subject and object 
     * are null, removes all triples the two specified URIs.
     * @param subject
     * @param predicate
     * @param object
     */
    public void removeStatementWithURI(URI subject, URI predicate, URI object) {
        if (predicate == null || (subject == null && object == null))
            return;

        Repository repo = getRepository();
        RepositoryConnection connection;
        
        try {
            connection = repo.getConnection();
            connection.begin();
            Statement statement = new StatementImpl(subject, predicate, object);
            connection.remove(statement);
            connection.commit();
            connection.close();            
        } catch (RepositoryException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println(e.getCause());
            e.printStackTrace();
        }
        
    }
    /**
     * Removes a specified triple with a Literal object. If predicate 
     * or subject are null, the method does nothing. If the object 
     * is null, removes all triples the two specified URIs.
     * @param subject
     * @param predicate
     * @param objectStr
     * @throws RepositoryException 
     */
    public void removeStatementWithLiteral(URI subject, URI predicate, String objectStr) throws RepositoryException {
        if (predicate == null || subject == null)
            return;

        Repository repo = getRepository();
        RepositoryConnection connection;
        
        try {
            connection = repo.getConnection();
            connection.begin();
            ValueFactory factory = ValueFactoryImpl.getInstance();
            Literal object = (objectStr != null) ? factory.createLiteral(objectStr) : null;
            Statement statement = new StatementImpl(subject, predicate, object);
            connection.remove(statement);
            connection.commit();
            connection.close();            
        } catch (RepositoryException e) {
            e.printStackTrace();
            throw e;
        }
        
    }
    
    /**
     * Remove all triples that contain the specified predicate.
     * This can be dangerous and should only be run whenever we are
     * about to rebuild the whole EFD category tree.
     * @param predicate triples with this predicate will be removed
     * @throws RepositoryException 
     */
    public void removeStatementsWithPredicate(URI predicate) throws RepositoryException {

        Repository repo = getRepository();
        RepositoryConnection connection;
        
        try {
            connection = repo.getConnection();
            connection.begin();
            Statement statement = new StatementImpl(null, predicate, null);
            connection.remove(statement);
            connection.commit();
            connection.close();            
        } catch (RepositoryException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Counts the number of articles per category for each English
     * DBPedia category in the repository and returns the counts.
     * @return
     */
    public List<URIIntPair> countArticleToCategoryConnections(URI pred) {
        if (pred == null || pred.stringValue() == null)
            return null;

        List<URIIntPair> counts = new LinkedList<URIIntPair>();
        try {
            Repository repo = getRepository();
            RepositoryConnection con = repo.getConnection();
            try {
                String rq = "SELECT ?cat (COUNT (*) as ?count) WHERE " +
                            "{ ?s <" + pred.stringValue() + ">+ ?cat ." +
                            "FILTER(strstarts(str(?cat), \"http://dbpedia.org/resource/Category:\"))" +
                            " } GROUP BY ?cat";
                TupleQuery tupleQuery = con.prepareTupleQuery(
                        QueryLanguage.SPARQL, rq);
                TupleQueryResult result = tupleQuery.evaluate();
                try {
                    // Extract the potential children URIs from the response.
                    while (result.hasNext()) {
                        BindingSet bindingSet = result.next();
                        URI cat = (URI) bindingSet.getValue("cat");
                        String countStr = bindingSet.getValue("count").stringValue();
                        Integer count = Integer.parseInt(countStr);
                        counts.add(new URIIntPair(cat, count));
                    }
                } finally {
                    result.close();
                }
            } catch (MalformedQueryException e) {
                System.err.println("Issue encountered with the efd:child+ query string.");
                e.printStackTrace();
            } catch (QueryEvaluationException e) {
                System.err.println("Issue evaluating the efd:child+ query.");
                e.printStackTrace();
            } finally {
                con.close();
            }
        } catch (RepositoryException e) {
            System.err.println("Failed to make connection to the repository.");
            e.printStackTrace();
        }
        
        return counts;
    }
    
}
