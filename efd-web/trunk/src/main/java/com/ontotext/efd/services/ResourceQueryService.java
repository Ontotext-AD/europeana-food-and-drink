package com.ontotext.efd.services;

import org.openrdf.model.Statement;
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

    @Value("${get.resource.construct}")
    private String resourceConstruct;

    private static Map<String, String> resourceMap;

    {
        resourceMap = initResourceMap();
    }

    public Map<String, List<String>> getResource(String resource) {

        Map<String, List<String>> resourceMap = new HashMap<>();
//        getTriples(resourceMap, resource, aggregationQuery);
//        getTriples(resourceMap, resource, choQuery);
        getTriples(resourceMap, resource, resourceConstruct);

        if (resourceMap.size() == 0) return null;
        return resourceMap;
    }

    private void getTriples(Map<String, List<String>> resourceMapLocal, String resource, String query) {
        GraphQueryResult tupleQueryResult = null;
        String aggregationQ = preprocessQuery(resource, query);
        if (aggregationQ != null && !aggregationQ.isEmpty()) {
            try{
                 tupleQueryResult = evaluateQuery(aggregationQ);
                while (tupleQueryResult.hasNext()) {
                    String predicate = "";
                    String object = "";

                    Statement bindings = tupleQueryResult.next();
                    predicate = bindings.getPredicate().stringValue();
                    object = bindings.getObject().stringValue();


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


    private GraphQueryResult evaluateQuery(String query) {
        Repository repository = connectionService.getRepository();;
        RepositoryConnection repositoryConnection = null;
        GraphQueryResult  graphQueryResult = null;
        try {
            repositoryConnection = repository.getConnection();

            GraphQuery graphQuery =   repositoryConnection.prepareGraphQuery(QueryLanguage.SPARQL, query);
            graphQueryResult = graphQuery.evaluate();
        } catch (RepositoryException e) {
            e.printStackTrace();
        } catch (QueryEvaluationException e) {
            e.printStackTrace();
        } catch (MalformedQueryException e) {
            e.printStackTrace();
        }
        return graphQueryResult;
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
        resourceMap.put("http://www.europeana.eu/schemas/edm/isShownAt", "isShownAt");
        resourceMap.put("http://www.europeana.eu/schemas/edm/provider", "provider");
        resourceMap.put("http://www.europeana.eu/schemas/edm/rights", "EDMRights");
        resourceMap.put("http://www.europeana.eu/schemas/edm/object", "object");
        resourceMap.put("http://www.europeana.eu/schemas/edm/isShownBy", "isShownBy");
        resourceMap.put("http://www.europeana.eu/schemas/edm/type", "EDMType");
        resourceMap.put("http://purl.org/dc/elements/1.1/date", "date");
        resourceMap.put("http://www.europeana.eu/schemas/edm/aggregatedCHO", "aggregatedCHO");
        resourceMap.put("http://www.europeana.eu/schemas/edm/country", "country");
        resourceMap.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "RDFType");
        resourceMap.put("http://purl.org/dc/elements/1.1/publisher", "publisher");



        return resourceMap;
    }
}
