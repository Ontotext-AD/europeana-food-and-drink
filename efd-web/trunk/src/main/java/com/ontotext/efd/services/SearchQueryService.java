package com.ontotext.efd.services;

import com.ontotext.efd.model.FTSSearchResults;
import com.ontotext.efd.model.FacetFilterModel;
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

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.logging.Logger;

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

    private Logger logger = Logger.getLogger(String.valueOf(SearchQueryService.class));

    public SearchModel ftsSearch(String queryString, Integer offset, Integer limit, HttpServletRequest request) {
        TupleQueryResult tupleQueryResult = null;
        List<FTSSearchResults> searchResults = null;
//        Map<String, FTSSearchResults> searchResults = null;
        String query = decorateFilters(request, searchQuery);
        query = prepareSearchQuery(queryString, query, offset, limit);
        if (query != null && !query.isEmpty()) {

            try {
                searchResults = new ArrayList<>();
                tupleQueryResult = evaluateQuery(query);
                while (tupleQueryResult.hasNext()) {
                    String resource = "";
                    String title = "";
                    String description = "";
                    String picture = "";
                    String date = "";
                    String mediaType = "";
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
                    if (bindingSet.getValue("mediaType") != null) {
                        mediaType = bindingSet.getValue("mediaType").stringValue();
                    }

//                    if (searchResults.containsKey(resource)) {
//                        searchResults.get(resource).addDescription(description);  //TODO add all fields which are multiple value
//                    } else {
//                        searchResults.put(resource, new FTSSearchResults(title, description, picture, date));
//                    }

                    searchResults.add(new FTSSearchResults(resource, title, description, picture, date, mediaType));
                }

            }  catch (QueryEvaluationException e) {
                logger.info("No CHO results in the query!");
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
            query = q.replace("{q}", ":query \"title:" + queryString + "\" ;");
            if (offset != null) query +=  " OFFSET " + offset;

            if (limit != null) query +=  " LIMIT " + limit;

            return query;
        }
        else if (q != null && queryString.isEmpty()) {
            query = q.replace("{q}", "");

            return query;
        }
        return null;
    }

    private String decorateFilters(HttpServletRequest request, String query) {
        Map<String, String[]> filterParams = request.getParameterMap();
        FacetFilterModel filterModel = new FacetFilterModel();
        for (Map.Entry<String, String[]> entry : filterParams.entrySet()) {
            switch (entry.getKey()) {
                case "type" :
                    filterModel.setMediaTypeFilter(entry.getValue()[0].split(","));
                    break;
                case "provider" :
                    filterModel.setProviderFilter(entry.getValue());
                    break;
                case "dataProvider" :
                    filterModel.setDataProviderFilter(entry.getValue());
                    break;
                case "language" :
                    filterModel.setLanguageFilter(entry.getValue());
                    break;
                case "article" :
                    filterModel.setArticleFilter(entry.getValue());
                    break;
                case "country" :
                    filterModel.setCountryFilter(entry.getValue());
                    break;
            }
        }

        String q = addTypeFilter(query, filterModel.getMediaTypeFilter());
        q = addProviderFilter(q, filterModel.getProviderFilter());
        q = addDataProviderFilter(q, filterModel.getDataProviderFilter());
        q = addLanguageFilter(q, filterModel.getLanguageFilter());
        q = addCountryFilter(q, filterModel.getCountryFilter());

        return q;
    }

    private String addTypeFilter(String query, String types[]) {
        String q = query;
        String filter =  "optional {?entity edm:aggregatedCHO/edm:type ?type}";

        if (types != null && types.length > 0) {
            for(String type : types) {
                filter += "\n  filter(?type = \"" + type + "\").";
            }
            q = q.replace("{mediaType}", filter);
            q += "?type";
        } else {
            q = q.replace("{mediaType}", "");
        }
        return q;
    }

    private String addProviderFilter(String query, String providers[]) {
        String q = query;
        String filter =  "optional {?entity edm:provider ?provider}";

        if (providers != null && providers.length > 0) {
            for(String provider : providers) {
                filter += "\n  filter(?type = \"" + provider + "\").";
            }
            q = q.replace("{provider}", filter);
            q = q.replace("{provider_h}", "?provider");
            q += " ?provider";
        } else {
            q = q.replace("{provider}", "");
            q = q.replace("{provider_h}", "");
        }
        return q;
    }

    private String addDataProviderFilter(String query, String dataProviders[]) {
        String q = query;
        String filter =  "optional {?entity edm:dataProvider ?dataProvider}";

        if (dataProviders != null && dataProviders.length > 0) {
            for(String dataProvider : dataProviders) {
                filter += "\n  filter(?type = \"" + dataProvider + "\").";
            }
            q = q.replace("{dataProvider}", filter);
            q = q.replace("{dataProvider_h}", "?dataProvider");
            q += " ?dataProvider";
        } else {
            q = q.replace("{dataProvider}", "");
            q = q.replace("{dataProvider_h}", "");
        }
        return q;
    }

    private String addLanguageFilter(String query, String languages[]) {
        String q = query;
        String filter =  "optional {?entity edm:aggregatedCHO/dc:language ?language}";

        if (languages != null && languages.length > 0) {
            for(String language : languages) {
                filter += "\n  filter(?type = \"" + language + "\").";
            }
            q = q.replace("{language}", filter);
            q = q.replace("{language_h}", "?language");
            q += " ?language";
        } else {
            q = q.replace("{language}", "");
            q = q.replace("{language_h}", "");
        }
        return q;
    }

    private String addCountryFilter(String query, String countries[]) {
        String q = query;
        String filter =  "optional {?entity edm:country ?providingCountry}";

        if (countries != null && countries.length > 0) {
            for(String country : countries) {
                filter += "\n  filter(?type = \"" + country + "\").";
            }
            q = q.replace("{providingCountry}", filter);
            q = q.replace("{providingCountry_h}", "?providingCountry");
            q += " ?providingCountry";
        } else {
            q = q.replace("{providingCountry}", "");
            q = q.replace("{providingCountry_h}", "");
        }
        return q;
    }
}
