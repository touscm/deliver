package com.touscm.deliver.base.entry;

public class ElasticEntry<T> {
    private String id;
    private T entry;

    public ElasticEntry() {
    }

    public ElasticEntry(String id, T entry) {
        this.id = id;
        this.entry = entry;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public T getEntry() {
        return entry;
    }

    public void setEntry(T entry) {
        this.entry = entry;
    }
}
