package com.ontotext.efd.model;

/**
 * Created by boyan on 15-10-5.
 */
public class FacetModel {

    private String facetName;
    private String facetValue;

    public FacetModel(String facetName, String facetValue) {
        this.facetName = facetName;
        this.facetValue = facetValue;
    }

    public String getFacetName() {
        return facetName;
    }

    public void setFacetName(String facetName) {
        this.facetName = facetName;
    }

    public String getFacetValue() {
        return facetValue;
    }

    public void setFacetValue(String facetValue) {
        this.facetValue = facetValue;
    }
}
