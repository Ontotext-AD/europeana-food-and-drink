package com.ontotext.efd.services;

import com.ontotext.efd.model.FacetModel;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by boyan on 19.10.15.
 */
@Service
public class CategoryFacetSearchService {

    @Autowired
    private RepositoryConnectionService connectionService;

    private Logger logger = Logger.getLogger(String.valueOf(CategoryFacetSearchService.class));

    public List<FacetModel> getCategoryArticleFacets(String query){
        TupleQueryResult tupleQueryResult = null;
        List<FacetModel> categoryFacet = null;

        if (query != null && !query.isEmpty()) {
            tupleQueryResult = connectionService.evaluateQuery(query);
            categoryFacet = new ArrayList<>();
            try {
                while (tupleQueryResult.hasNext()) {
                    String category = "";
                    String count = "";
                    BindingSet bindings = tupleQueryResult.next();
                    if (bindings.getValue("sub") != null) {
                        category = bindings.getValue("sub").stringValue();
                        category = processArticleCategoryName(category);
                    }
                    if (bindings.getValue("count") != null) {
                        count = bindings.getValue("count").stringValue();
                    }
                    categoryFacet.add(new FacetModel(category, count));

                }
            } catch (QueryEvaluationException e) {
                e.printStackTrace();
            }
        }

        return categoryFacet;
    }

    private String processArticleCategoryName(String category) {
        String delimiter = "";
        if (category.contains("Category")) delimiter = ":";
        else delimiter = "/";
        String cat[] = category.split(delimiter);
        category = cat[cat.length - 1];
        category = category.replaceAll("_", " ");

        return category;
    }

    public String preprocessCategoryQuery(String query, String category, String queryString){
        String q = "";
        category = category.replace(" ", "_");
        if (query != null && !query.isEmpty())
            if (category.equals("Food_and_drink")) {
                q = query.replace("{categoryFilter}", "optional{?sub efd:treeLevel ?level}.\n" +
                                                      "filter(xsd:integer(?level) = 1).");
            }
            else {
                q = query.replace("{categoryFilter}", "optional{?sub efd:child ?cat}\n" +
                                      " filter(?cat =  <http://dbpedia.org/resource/Category:" + category +">).");
            }
            if (queryString != null && !queryString.isEmpty()) {
                q = q.replace("{query}", "title:" + queryString);
            }
            else {
                q = q.replace("{query}", "*:*");
            }
        return q;
    }

    public String preprocessArticleQuery(String query, String category, String queryString) {
        String artQuery = "";
        if (query != null && !query.isEmpty()) {
            artQuery = query.replace("{category}", category);

            if (queryString != null && !queryString.isEmpty()) {
                artQuery = artQuery.replace("{query}", "title:" + queryString);
            }
            else {
                artQuery = artQuery.replace("{query}", "*:*");
            }
        }
        return artQuery;
    }
}
