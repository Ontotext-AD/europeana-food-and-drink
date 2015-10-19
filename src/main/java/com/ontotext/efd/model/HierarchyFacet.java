package com.ontotext.efd.model;

import java.util.List;

/**
 * Created by boyan on 15-10-19.
 */
public class HierarchyFacet {

    private List<FacetModel> categoryFacet;
    private List<FacetModel> articleModel;

    public List<FacetModel> getCategoryFacet() {
        return categoryFacet;
    }

    public void setCategoryFacet(List<FacetModel> categoryFacet) {
        this.categoryFacet = categoryFacet;
    }

    public List<FacetModel> getArticleModel() {
        return articleModel;
    }

    public void setArticleModel(List<FacetModel> articleModel) {
        this.articleModel = articleModel;
    }
}
