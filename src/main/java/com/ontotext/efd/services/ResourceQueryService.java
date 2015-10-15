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

    private static Map<String, String> resourceMap;

    {
        resourceMap = initResourceMap();
    }

    public Map<String, List<String>> getResource(String resource) {

        Map<String, List<String>> resourceMap = new HashMap<>();
        getTriples(resourceMap, resource, aggregationQuery);
        getTriples(resourceMap, resource, choQuery);

        return resourceMap;
    }

    private void getTriples(Map<String, List<String>> resourceMapLocal, String resource, String query) {
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
                        predicate = bindings.getValue("p").stringValue();
                    }
                    if (bindings.getValue("o") != null) {
                        object = bindings.getValue("o").stringValue();
                    }

                    String key = resourceMap.get(predicate);
                    if (key == null) continue;
                    if (resourceMapLocal.containsKey(key)) {
                        resourceMapLocal.get(key).add(object);
                    } else {
                        List<String> list = new ArrayList<>();
                        list.add(object);
                        resourceMapLocal.put(key, list);
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

    private Map<String,String> initResourceMap() {
        Map<String, String> resourceMap = new HashMap<>();
        resourceMap.put("http://purl.org/dc/elements/1.1/contributor", "contributor");
        resourceMap.put("http://purl.org/dc/elements/1.1/description", "description");
        resourceMap.put("http://purl.org/dc/elements/1.1/identifier", "identifier");
        resourceMap.put("http://purl.org/dc/elements/1.1/language", "language");
        resourceMap.put("http://purl.org/dc/elements/1.1/rights", "rights");
        resourceMap.put("http://purl.org/dc/elements/1.1/subject", "subject");
        resourceMap.put("http://purl.org/dc/elements/1.1/title", "title");
        resourceMap.put("http://purl.org/dc/elements/1.1/type", "type");
        resourceMap.put("http://purl.org/dc/terms/isPartOf", "isPartOf");
        resourceMap.put("http://purl.org/dc/terms/medium", "medium");
        resourceMap.put("http://purl.org/dc/terms/spatial", "spatial");
        resourceMap.put("http://purl.org/dc/terms/subject", "termsSubject");
        resourceMap.put("http://www.europeana.eu/schemas/edm/dataProvider", "dataProvider");
        resourceMap.put("http://www.europeana.eu/schemas/edm/hasView", "hasView");
        resourceMap.put("http://www.europeana.eu/schemas/edm/isShownAt", "externalLink");
        resourceMap.put("http://www.europeana.eu/schemas/edm/provider", "provider");
        resourceMap.put("http://www.europeana.eu/schemas/edm/rights", "EDMRights");
        resourceMap.put("http://www.europeana.eu/schemas/edm/object", "object");
        resourceMap.put("http://www.europeana.eu/schemas/edm/isShownBy", "isShownBy");


        return resourceMap;
    }
}
