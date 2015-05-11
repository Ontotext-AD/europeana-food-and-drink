package com.ontotext.efd.services;

import java.util.LinkedList;
import java.util.List;



import org.openrdf.OpenRDFException;
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
import org.openrdf.repository.http.HTTPRepository;
import org.springframework.stereotype.Service;

import com.ontotext.efd.model.EFDCategory;

/**
 * Created by boyan on 15-5-7.
 */
@Service
public class EFDRepositoryConnection {

    //@org.springframework.beans.factory.annotation.Value("${sesame.server}")
    private String sesameServer = "http://192.168.130.19:8087/openrdf-sesame/";

    //@org.springframework.beans.factory.annotation.Value("${repository.id}")
    private String repositoryID = "DBpedia-efd";

    private Repository getRepository(){
        Repository repo = new HTTPRepository(sesameServer, repositoryID);
        try {
            repo.initialize();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        return repo;
    }

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
    
    public boolean writeEFDRelation(EFDCategory cat) {
        
        
        return true;
    }
    
    public void repositoryCheck(){

        Repository repo = getRepository();
        RepositoryConnection connection;

        try {
            connection = repo.getConnection();
            connection.begin();
            connection.add(createStatement());
            connection.commit();
            connection.close();


        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    public Statement createStatement() {
        ValueFactory factory = ValueFactoryImpl.getInstance();
        Statement statement = new StatementImpl(factory.createURI("http://efd.ontotext.com/subject"), factory.createURI("http://efd.ontotext.com/predicate"), factory.createURI("http://efd.ontotext.com/object"));

        return statement;
    }

}
