package com.touscm.deliver.base.entry;

import java.util.List;

public class PagingEntry<T> {
    private int page;
    private int size;
    private long count;
    private long pageCount;
    private List<T> entries;

    public PagingEntry() {
    }

    public PagingEntry(int page, int size, long count, List<T> entries) {
        this.page = page;
        this.size = size;
        this.count = count;
        this.entries = entries;

        if (0 < this.size && 0 < this.count) {
            this.pageCount = this.count % this.size == 0 ? this.count / this.size : (this.count / this.size) + 1;
        }
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getPageCount() {
        return pageCount;
    }

    public void setPageCount(long pageCount) {
        this.pageCount = pageCount;
    }

    public List<T> getEntries() {
        return entries;
    }

    public void setEntries(List<T> entries) {
        this.entries = entries;
    }
}
