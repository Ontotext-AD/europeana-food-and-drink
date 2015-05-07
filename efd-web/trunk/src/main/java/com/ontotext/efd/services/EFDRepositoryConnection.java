package com.ontotext.efd.services;

import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * Created by boyan on 15-5-7.
 */
@Service
public class EFDRepositoryConnection {

    @Value("${sesame.server}")
    private String sesameServer;

    @Value("${repository.id}")
    private String repositoryID;

    private Repository getRepository(){
        Repository repo = new HTTPRepository(sesameServer, repositoryID);
        try {
            repo.initialize();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        return repo;
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
