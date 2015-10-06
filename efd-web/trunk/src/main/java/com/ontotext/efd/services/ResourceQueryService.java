package com.ontotext.efd.services;

import org.openrdf.query.*;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by boyan on 15-10-6.
 */
@Service
public class ResourceQueryService {

    @Autowired
    private RepositoryConnectionService connectionService;

    @Value("${resource.aggregation.query}")
    private String aggregationQuery;

    @Value("${resource.cho.query}")
    private String choQuery;

    public Map<String, List<String>> getResource(String resource) {

        Map<String, List<String>> resourceMap = new HashMap<>();
        getTriples(resourceMap, resource, aggregationQuery);
        getTriples(resourceMap, resource, choQuery);

        return resourceMap;
    }

    private void getTriples(Map<String, List<String>> resourceMap, String resource, String query) {
        TupleQueryResult tupleQueryResult = null;
        String aggregationQ = preprocessQuery(resource, query);
        if (aggregationQ != null && !aggregationQ.isEmpty()) {
            try{
                tupleQueryResult = evaluateQuery(aggregationQ);
                while (tupleQueryResult.hasNext()) {
                    String predicate = "";
                    String object = "";

                    BindingSet bindings = tupleQueryResult.next();
                    if (bindings.getValue("p") != null) {
                        predicate = bindings.getValue("p").stringValue().split(":")[1];
                    }
                    if (bindings.getValue("o") != null) {
                        object = bindings.getValue("o").stringValue();
                    }

                    if (resourceMap.containsKey(predicate)) {
                        resourceMap.get(predicate).add(object);
                    } else {
                        List<String> list = new ArrayList<>();
                        list.add(object);
                        resourceMap.put(predicate, list);
                    }
                }
            } catch (QueryEvaluationException e) {
                e.printStackTrace();
            }
        }

    }

    private String preprocessQuery(String resource, String query) {

        return query.replace("OBJ", resource);
    }


    private TupleQueryResult evaluateQuery(String query) {
        Repository repository = connectionService.getRepository();;
        RepositoryConnection repositoryConnection = null;
        TupleQueryResult tupleQueryResult = null;
        try {
            repositoryConnection = repository.getConnection();
            TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, query);
            tupleQueryResult = tupleQuery.evaluate();
        } catch (RepositoryException e) {
            e.printStackTrace();
        } catch (QueryEvaluationException e) {
            e.printStackTrace();
        } catch (MalformedQueryException e) {
            e.printStackTrace();
        }
        return tupleQueryResult;
    }
}
