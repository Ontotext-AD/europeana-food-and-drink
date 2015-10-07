package com.ontotext.efd.services;

import com.ontotext.efd.model.FTSSearchResults;
import com.ontotext.efd.model.FacetModel;
import com.ontotext.efd.model.SearchModel;
import org.apache.commons.lang3.StringUtils;
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
 * Created by boyan on 15-9-18.
 */
@Service
public class SearchQueryService {

    @Autowired
    private RepositoryConnectionService connectionService;

    @Value("${repository.id}")
    private String repositoryID;

    @Value("${search.query}")
    private String searchQuery;

    @Value("${facets.query}")
    private String facetsQuery;

    public SearchModel ftsSearch(String queryString, Integer offset, Integer limit) {
        TupleQueryResult tupleQueryResult = null;
//        List<FTSSearchResults> searchResults = null;
        Map<String, FTSSearchResults> searchResults = null;
        String query = prepareSearchQuery(queryString, searchQuery, offset, limit);
        if (query != null && !query.isEmpty()) {

            try {
//                searchResults = new ArrayList<>();
                searchResults = new HashMap<>();
                tupleQueryResult = evaluateQuery(query);
                while (tupleQueryResult.hasNext()) {
                    String resource = "";
                    String title = "";
                    String description = "";
                    String picture = "";
                    String date = "";
                    BindingSet bindingSet = tupleQueryResult.next();

                    if (bindingSet.getValue("entity") != null) {
                        resource = bindingSet.getValue("entity").stringValue();
                    }
                    if (bindingSet.getValue("title") != null) {
                        title = bindingSet.getValue("title").stringValue();
                    }
                    if (bindingSet.getValue("description") != null) {
                        description = bindingSet.getValue("description").stringValue();
                    }
                    if (bindingSet.getValue("picture") != null) {
                        picture = bindingSet.getValue("picture").stringValue();
                    }
                    if (bindingSet.getValue("date") != null) {
                        date = bindingSet.getValue("date").stringValue();
                    }

                    if (searchResults.containsKey(resource)) {
                        searchResults.get(resource).addDescription(description);  //TODO add all fields which are multiple value
                    } else {
                        searchResults.put(resource, new FTSSearchResults(title, description, picture, date));
                    }

//                    searchResults.add(new FTSSearchResults(resource, title, description, picture));
                }

            }  catch (QueryEvaluationException e) {
                e.printStackTrace();
            }
        }

        SearchModel searchModel = new SearchModel(searchResults, searchFacets(queryString));
        return searchModel;
    }

    public List<FTSSearchResults> autocomplete(String queryString) {
        TupleQueryResult tupleQueryResult = null;
        List<FTSSearchResults> searchResults = null;
        String query = prepareSearchQuery(queryString, searchQuery, null, 10);

        if (query != null && !query.isEmpty()) {
            try {
                searchResults = new ArrayList<>();
                tupleQueryResult = evaluateQuery(query);
                while (tupleQueryResult.hasNext()) {
                    String resource = "";
                    String title = "";
                    BindingSet bindingSet = tupleQueryResult.next();

                    if (bindingSet.getValue("entity") != null) {
                        resource = bindingSet.getValue("entity").stringValue();
                    }
                    if (bindingSet.getValue("title") != null) {
                        title = bindingSet.getValue("title").stringValue();
                    }

                    searchResults.add(new FTSSearchResults(resource, title));
                }

            }  catch (QueryEvaluationException e) {
                e.printStackTrace();
            }
        }
        return searchResults;

    }

    private Map<String, List<FacetModel>> searchFacets(String queryString){
        TupleQueryResult tupleQueryResult = null;
        String query = prepareSearchQuery(queryString, facetsQuery, null, null);
        Map<String, List<FacetModel>> map = new HashMap<>();
        if (query != null && !query.isEmpty()) {
            try {
                tupleQueryResult = evaluateQuery(query);
                while (tupleQueryResult.hasNext()) {
                    String facetName = "";
                    String facetValue = "";
                    String facetCount = "";
                    BindingSet bindings = tupleQueryResult.next();

                    if (bindings.getValue("facetName") != null) {
                        facetName = bindings.getValue("facetName").stringValue();
                    }
                    if (bindings.getValue("facetValue") != null) {
                        facetValue = bindings.getValue("facetValue").stringValue();
                    }
                    if (bindings.getValue("facetCount") != null) {
                        facetCount = bindings.getValue("facetCount").stringValue();
                    }

                    if (map.containsKey(facetName)) {
                        map.get(facetName).add(new FacetModel(facetValue, facetCount));
                    }
                    else {
                        List <FacetModel> list = new ArrayList();
                        list.add(new FacetModel(facetValue, facetCount));
                        map.put(facetName, list);
                    }
                }
            } catch (QueryEvaluationException e) {
                e.printStackTrace();
            }
        }
        return map;
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

    private String prepareSearchQuery(String queryString, String q, Integer offset, Integer limit) {
        String query = "";
        if (q != null && !queryString.isEmpty()) {
            queryString = StringUtils.join(queryString.split("[\\s]"), "* AND ");
            query = q.replace("{q}", queryString);
            if (offset != null) query = query.replace("{offset}", "OFFSET" + offset);
            else query = query.replace("{offset}", "");

            if (limit != null) query = query.replace("{limit}", "LIMIT" + limit);
            else query = query.replace("{limit}", "");

            return query;
        }
        return null;
    }
}
