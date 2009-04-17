package com.wordpress.model;


public class Category {

    
    private String id;
    private String label;


    public Category(String aId, String aLabel) {
        id = aId;
        label = aLabel;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String toString() {
        return '[' + id + '/' + label + ']';
    }

    public boolean equals(Object aObj) {
        return (aObj != null &&
                aObj instanceof Category &&
                id.equals(((Category) aObj).id));
    }

    public int hashCode() {
        return id.hashCode();
    }

}

