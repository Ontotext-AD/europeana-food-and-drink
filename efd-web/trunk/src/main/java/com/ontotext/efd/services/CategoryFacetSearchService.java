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
}
