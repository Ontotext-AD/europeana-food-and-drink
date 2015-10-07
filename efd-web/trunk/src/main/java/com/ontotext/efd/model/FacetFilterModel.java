package com.ontotext.efd.model;

/**
 * Created by boyan on 15-10-8.
 */
public class FacetFilterModel {

    private String mediaTypeFilter[];
    private String providerFilter[];
    private String dataProviderFilter[];
    private String languageFilter[];
    private String articleFilter[];

    public String[] getMediaTypeFilter() {
        return mediaTypeFilter;
    }

    public void setMediaTypeFilter(String[] mediaTypeFilter) {
        this.mediaTypeFilter = mediaTypeFilter;
    }

    public String[] getProviderFilter() {
        return providerFilter;
    }

    public void setProviderFilter(String[] providerFilter) {
        this.providerFilter = providerFilter;
    }

    public String[] getDataProviderFilter() {
        return dataProviderFilter;
    }

    public void setDataProviderFilter(String[] dataProviderFilter) {
        this.dataProviderFilter = dataProviderFilter;
    }

    public String[] getLanguageFilter() {
        return languageFilter;
    }

    public void setLanguageFilter(String[] languageFilter) {
        this.languageFilter = languageFilter;
    }

    public String[] getArticleFilter() {
        return articleFilter;
    }

    public void setArticleFilter(String[] articleFilter) {
        this.articleFilter = articleFilter;
    }
}
