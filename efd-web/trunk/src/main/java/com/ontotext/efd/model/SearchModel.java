package com.ontotext.efd.model;

import java.util.List;
import java.util.Map;

/**
 * Created by boyan on 15-10-5.
 */
public class SearchModel {

    private List<FTSSearchResults> searchResults;
    private Map<String, List<FacetModel>> facets;

    public SearchModel(List<FTSSearchResults> searchResults, Map<String, List<FacetModel>> facets) {
        this.searchResults = searchResults;
        this.facets = facets;
    }

    public List<FTSSearchResults> getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(List<FTSSearchResults> searchResults) {
        this.searchResults= searchResults;
    }

    public Map<String, List<FacetModel>> getFacets() {
        return facets;
    }

    public void setFacets(Map<String, List<FacetModel>> facets) {
        this.facets = facets;
    }
}
