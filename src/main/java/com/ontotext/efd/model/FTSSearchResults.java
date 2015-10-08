package com.ontotext.efd.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by boyan on 15-10-5.
 */
public class FTSSearchResults {

    private String resource;
    private String title;
    private String description;
    private String picture;
    private String date;
    private String mediaType;


    public FTSSearchResults(String resource, String title, String description, String picture, String date, String mediaType) {
        this.resource = resource;
        this.title = title;
        this.description = description;
        this.picture = picture;
        this.date = date;
        this.mediaType = mediaType;
    }

    public FTSSearchResults(String resource, String title) {
//        this.resource = resource;
        this.title = title;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
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

    public void addDescription(String description) {
        this.description = description;
    }

}
