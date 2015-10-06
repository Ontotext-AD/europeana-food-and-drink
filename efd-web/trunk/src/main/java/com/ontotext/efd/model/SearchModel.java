package com.ontotext.efd.model;

import java.util.List;
import java.util.Map;

/**
 * Created by boyan on 15-10-5.
 */
public class SearchModel {

    private Map<String, FTSSearchResults> searchResultses;
    private Map<String, List<FacetModel>> facets;

    public SearchModel(Map<String, FTSSearchResults> searchResultses, Map<String, List<FacetModel>> facets) {
        this.searchResultses = searchResultses;
        this.facets = facets;
    }

    public Map<String, FTSSearchResults> getSearchResultses() {
        return searchResultses;
    }

    public void setSearchResultses(Map<String, FTSSearchResults> searchResultses) {
        this.searchResultses = searchResultses;
    }

    public Map<String, List<FacetModel>> getFacets() {
        return facets;
    }

    public void setFacets(Map<String, List<FacetModel>> facets) {
        this.facets = facets;
    }
}
