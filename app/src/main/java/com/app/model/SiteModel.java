package com.app.model;

/**
 * Created by VIJAY on 03-09-2016.
 */
public class SiteModel {

    String id;
    String name;

    public SiteModel(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }
}
