package com.ontotext.efd.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Created by boyan on 15-9-18.
 */
@Service
public class SearchQueryService {

    @Value("${repository.id}")
    private String repositoryID;

    public void sayHi() {
        System.out.println(repositoryID);
    }
}
