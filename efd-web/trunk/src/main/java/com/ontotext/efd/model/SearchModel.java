package com.ontotext.efd.model;

import java.util.List;
import java.util.Map;

/**
 * Created by boyan on 15-10-5.
 */
public class SearchModel {

    private List<FTSSearchResults> searchResults;
    private Map<String, List<FacetModel>> facets;

    public SearchModel(List<FTSSearchResults> searchResultses, Map<String, List<FacetModel>> facets) {
        this.searchResults = searchResultses;
        this.facets = facets;
    }

    public List<FTSSearchResults> getSearchResultses() {
        return searchResults;
    }

    public void setSearchResultses(List<FTSSearchResults> searchResultses) {
        this.searchResults= searchResultses;
    }

    public Map<String, List<FacetModel>> getFacets() {
        return facets;
    }

    public void setFacets(Map<String, List<FacetModel>> facets) {
        this.facets = facets;
    }
}
