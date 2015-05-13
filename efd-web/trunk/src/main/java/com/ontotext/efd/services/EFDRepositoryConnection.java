package com.ontotext.efd.services;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.http.HTTPRepository;
import org.springframework.stereotype.Service;

import com.ontotext.efd.rdf.EFDTaxonomy;

@Service
public class EFDRepositoryConnection {

    //@org.springframework.beans.factory.annotation.Value("${sesame.server}")
    private String sesameServer = "http://192.168.130.19:8087/openrdf-sesame/";

    //@org.springframework.beans.factory.annotation.Value("${repository.id}")
    private String repositoryID = "DBpedia-efd-inf";

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
    private Statement createStatement(String subject, String predicate, String object) {
        ValueFactory factory = ValueFactoryImpl.getInstance();
        URI s = (subject != null) ? factory.createURI(subject) : null;
        URI p = (predicate != null) ? factory.createURI(predicate) : null;
        URI o = (object != null) ? factory.createURI(object) : null;
        Statement statement = new StatementImpl(s, p, o);
        return statement;
    }

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
     * If the object or predicate are null, returns null.
     * @param predicate URI of the triple's predicate.
     * @param object URI of the triple's object.
     * @return Set of all URIs encountered as subjects in the triples.
     */
    public Set<URI> readSubjectsAsURI(URI predicate, URI object) {
        if (predicate == null || object == null) 
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
     */
    public void removeStatementWithLiteral(URI subject, URI predicate, String objectStr) {
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
        } catch (Exception e) {
            System.err.println(e.getCause());
            e.printStackTrace();
        }
        
    }
    
    /**
     * Remove all triples that contain the specified predicate.
     * This can be dangerous and should only be run whenever we are
     * about to rebuild the whole EFD category tree.
     * @param predicate triples with this predicate will be removed
     */
    public void removeStatementsWithPredicate(URI predicate) {

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
        } catch (Exception e) {
            System.err.println(e.getCause());
            e.printStackTrace();
        }
    }

}
