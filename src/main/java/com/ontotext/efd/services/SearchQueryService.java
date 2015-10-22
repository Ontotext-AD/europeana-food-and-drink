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

//    @Value("${facet.county}")
//    private String countQuery;

    @Value("${facets.query}")
    private String facetsQuery;

    private Logger logger = Logger.getLogger(String.valueOf(SearchQueryService.class));

    private final String categoryArticleFilterExist = "filter exists {\n" +
            "      ?cho dct:subject ?art. \n" +
            "      ?art efd:subject ?cat.\n" +
            "      ?cat efd:ancestor ?ancestor.\n" +
            "      {articleFacet}\n";
    private boolean categoryArticleFilterIsSet = false;

    public SearchModel ftsSearch(String queryString, Integer offset, Integer limit, HttpServletRequest request) {
        categoryArticleFilterIsSet = false;
        TupleQueryResult tupleQueryResult = null;
        List<FTSSearchResults> searchResults = null;
//        Map<String, FTSSearchResults> searchResults = null;
        String query = decorateFilters(request, searchQuery);
        query = prepareSearchQuery(queryString, query, offset, limit);
        if (query != null && !query.isEmpty()) {

            try {
                searchResults = new ArrayList<>();
                tupleQueryResult = connectionService.evaluateQuery(query);
                while (tupleQueryResult != null && tupleQueryResult.hasNext()) {
                    String resource = "";
                    String title = "";
                    String description = "";
                    String picture = "";
                    String date = "";
                    String mediaType = "";
                    int size = 0;
                    BindingSet bindingSet = tupleQueryResult.next();

                    if (bindingSet.getValue("entity") != null) {
                        resource = bindingSet.getValue("entity").stringValue();
                        if (!resource.isEmpty()) size++;

                    }
                    if (bindingSet.getValue("title") != null) {
                        title = bindingSet.getValue("title").stringValue();
                        if (!title.isEmpty()) size++;
                    }
                    if (bindingSet.getValue("description") != null) {
                        description = bindingSet.getValue("description").stringValue();
                        if (!description.isEmpty()) size++;
                    }
                    if (bindingSet.getValue("picture") != null) {
                        picture = bindingSet.getValue("picture").stringValue();
                        if (!picture.isEmpty()) size++;
                    }
                    if (bindingSet.getValue("date") != null) {
                        date = bindingSet.getValue("date").stringValue();
                        if (!date.isEmpty()) size++;
                    }
                    if (bindingSet.getValue("mediaType") != null) {
                        mediaType = bindingSet.getValue("mediaType").stringValue();
                        if (!mediaType.isEmpty()) size++;
                    }

//                    if (searchResults.containsKey(resource)) {
//                        searchResults.get(resource).addDescription(description);  //TODO add all fields which are multiple value
//                    } else {
//                        searchResults.put(resource, new FTSSearchResults(title, description, picture, date));
//                    }
                    if (size > 0) {
                        searchResults.add(new FTSSearchResults(resource, title, description, picture, date, mediaType));
                    }
                }

            }  catch (QueryEvaluationException e) {
                logger.info("No CHO results in the query!");
            }
        }

        SearchModel searchModel = new SearchModel(searchResults, searchFacets(queryString));
        if (searchModel.getSearchResults().size() == 0) return null;
        return searchModel;
    }

//    public String count(String queryString, HttpServletRequest request) {
//        TupleQueryResult tupleQueryResult = null;
//        String query = decorateFilters(request, countQuery);
//        if (query != null && !query.isEmpty()) {
//
//        }
//    }

    public List<FTSSearchResults> autocomplete(String queryString) {
        TupleQueryResult tupleQueryResult = null;
        List<FTSSearchResults> searchResults = null;
        String query = prepareSearchQuery(queryString, searchQuery, null, 10);

        if (query != null && !query.isEmpty()) {
            try {
                searchResults = new ArrayList<>();
                tupleQueryResult = connectionService.evaluateQuery(query);
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
                tupleQueryResult = connectionService.evaluateQuery(query);
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



    private String prepareSearchQuery(String queryString, String q, Integer offset, Integer limit) {
        String query = "";
        if (q != null && !queryString.isEmpty()) {
            queryString = StringUtils.join(queryString.split("[\\s]"), "* AND ");
            query = q.replace("{q}", ":query \"title:" + queryString + "\" ;");

            if (offset != null) {
               query =  query.replace("{OFFSET}", " OFFSET " + offset);
            } else {
                query = query.replace("{OFFSET}", "");
            }
            if (limit != null) {
               query =  query.replace("{LIMIT}", " LIMIT " + limit);
            } else {
                query =  query.replace("{LIMIT}", "");
            }

            return query;
        }
        else if (q != null && queryString.isEmpty()) {
            query = q.replace("{q}", "");

            if (offset != null) {
                query =  query.replace("{OFFSET}", " OFFSET " + offset);
            } else {
                query = query.replace("{OFFSET}", "");
            }
            if (limit != null) {
                query =  query.replace("{LIMIT}", " LIMIT " + limit);
            } else {
                query =  query.replace("{LIMIT}", "");
            }

            return query;
        }
        return null;
    }

    private String decorateFilters(HttpServletRequest request, String query) {
        Map<String, String[]> filterParams = request.getParameterMap();
        FacetFilterModel filterModel = new FacetFilterModel();
        for (Map.Entry<String, String[]> entry : filterParams.entrySet()) {
            switch (entry.getKey()) {
                case "mediaType" :
                    filterModel.setMediaTypeFilter(entry.getValue()[0].split(","));
                    break;
                case "provider" :
                    filterModel.setProviderFilter(entry.getValue()[0].split(","));
                    break;
                case "dataProvider" :
                    filterModel.setDataProviderFilter(entry.getValue()[0].split(","));
                    break;
                case "language" :
                    filterModel.setLanguageFilter(entry.getValue()[0].split(","));
                    break;
                case "article" :
                    filterModel.setArticleFilter(entry.getValue()[0].split(","));
                    break;
                case "country" :
                    filterModel.setCountryFilter(entry.getValue()[0].split(","));
                    break;
                case "category" :
                    filterModel.setCategoryFacetFilter(entry.getValue()[0].split(","));
                    break;

            }
        }

        String q = addTypeFilter(query, filterModel.getMediaTypeFilter());
        q = addProviderFilter(q, filterModel.getProviderFilter());
        q = addDataProviderFilter(q, filterModel.getDataProviderFilter());
        q = addLanguageFilter(q, filterModel.getLanguageFilter());
        q = addCountryFilter(q, filterModel.getCountryFilter());
        q = addCategoryFacetFilter(q, filterModel.getCategoryFacetFilter());
        q = addArticleFacetFilter(q, filterModel.getArticleFilter());

        return q;
    }

    private String addTypeFilter(String query, String types[]) {
        String q = query;
        String filter =  "optional {?cho edm:type ?type}";

        if (types != null && types.length > 0) {
            for(String type : types) {
                filter += "\n  filter(?type = \"" + type.toUpperCase() + "\").";
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
            filter += "\n  filter(";
            for(int i = 0; i < providers.length; i++){
                filter += "?provider = \"" + providers[i] + "\"";
                if (i < providers.length - 1) filter += " || ";
            }
            filter += ").";
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
            filter += "\n  filter(";
            for(int i = 0; i < dataProviders.length; i++){
                filter += "?dataProvider = \"" + dataProviders[i] + "\"";
                if (i < dataProviders.length - 1) filter += " || ";
            }
            filter += ").";
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
        String filter =  "optional {?cho dc:language ?language}";

        if (languages != null && languages.length > 0) {
            filter += "\n  filter(";
            for(int i = 0; i < languages.length; i++){
                filter += "?language = \"" + languages[i] + "\"";
                if (i < languages.length - 1) filter += " || ";
            }
            filter += ").";
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
            filter += "\n  filter(";
            for(int i = 0; i < countries.length; i++){
                filter += "?providingCountry = \"" + countries[i] + "\"";
                if (i < countries.length - 1) filter += " || ";
            }
            filter += ").";
            q = q.replace("{providingCountry}", filter);
            q = q.replace("{providingCountry_h}", "?providingCountry");
            q += " ?providingCountry";
        } else {
            q = q.replace("{providingCountry}", "");
            q = q.replace("{providingCountry_h}", "");
        }
        return q;
    }

    private String addCategoryFacetFilter(String query, String category[]) {
        String q = query;
        String filter = "";
        if (categoryArticleFilterIsSet == false) {
            filter = categoryArticleFilterExist;
            categoryArticleFilterIsSet = true;
        }

        if (category != null && category.length > 0) {
            filter +=  " filter(?ancestor in (";
            for (int i = 0; i < category.length; i++){
                filter += " dbc:" + category[i].replaceAll(" ", "_");
                if (i < category.length - 1) filter += ", ";
            }
            filter += ")).\n}";
            q = q.replace("{categoryFacet}", filter);

        }
        else {
            q = q.replace("{categoryFacet}", filter + "}");
            }
        return q;
    }

    private String addArticleFacetFilter(String query, String articles[]) {
        String q = query;
        String filter = "";
        if (categoryArticleFilterIsSet == false) {
            filter = categoryArticleFilterExist;
            q = q.replace("categoryFacet", filter);
            filter = "";
            categoryArticleFilterIsSet = true;
        }

        if (articles != null && articles.length > 0) {
            filter += "filter(?art in (";
            for (int i = 0; i < articles.length; i++) {
                filter += " dbr:" + articles[i].replaceAll(" ", "_");
                if (i < articles.length - 1) filter += ", ";
            }
            filter += ")).";
            q = q.replace("{articleFacet}", filter);

        }
        else {
            q = q.replace("{articleFacet}", "");
        }
        return q;
    }

    private String decorateCountQuery(String query) {
        String filter = "filter(uri(?key) in (dbc:Food_and_drink, dbc:Agriculture, dbr:Wood))";

        return null;
    }
}
