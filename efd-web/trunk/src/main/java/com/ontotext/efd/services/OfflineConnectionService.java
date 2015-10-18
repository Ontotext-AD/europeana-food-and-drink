package com.ontotext.efd.services;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;

/**
 * Created by boyan on 17.10.15.
 */
public class OfflineConnectionService {

    private String sesameServer = "http://192.168.130.19:8087/openrdf-sesame";
    private String repositoryID = "efd_inf3";

    public Repository getRepository(){
        Repository repo = new HTTPRepository(sesameServer, repositoryID);
        try {
            repo.initialize();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        return repo;
    }
}
