package com.ontotext.efd.services;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.springframework.beans.factory.annotation.Value;

import javax.xml.ws.ServiceMode;

/**
 * Created by boyan on 15-9-18.
 */
@ServiceMode
public class RepositoryConnectionService {

    @Value("${sesame.server}")
    private String sesameServer;

    @Value("${repository.id}")
    private String repositoryID;

    public Repository getRepository(){
        Repository repository = new HTTPRepository(sesameServer, repositoryID);
        try {
            repository.initialize();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        return repository;
    }
}
