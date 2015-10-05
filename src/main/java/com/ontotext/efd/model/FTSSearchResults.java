package com.ontotext.efd.model;

/**
 * Created by boyan on 15-10-5.
 */
public class FTSSearchResults {

    private String resource;
    private String title;
    private String description;


    public FTSSearchResults(String resource, String title, String description) {
        this.resource = resource;
        this.title = title;
        this.description = description;
    }

    public FTSSearchResults(String resource, String title) {
        this.resource = resource;
        this.title = title;
    }

    public String getresource() {
        return resource;
    }

    public void setresource(String resource) {
        this.resource = resource;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
