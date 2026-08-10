package com.abrar.BOOKSTORE.service;

import java.util.List;

// Simple in-memory pager over an already-computed list, not a JPA-level
// Pageable query. BookService.search() already has to fetch and sort in
// memory for read-time ordering (see its Javadoc - estimatedReadMinutes
// isn't a persisted column), so slicing the finished list here avoids
// running two different pagination strategies depending on which sort was
// picked. At the shelf sizes this app is built for, computing the full
// sorted/filtered list before slicing is cheap; if the catalog grows large
// enough for that to matter, this is the seam where a real Pageable-backed
// query would go instead.
public class PagedResult<T> {

    private final List<T> content;
    private final int page; // 1-indexed - the page currently shown
    private final int size; // items per page
    private final long totalItems;

    public PagedResult(List<T> content, int page, int size, long totalItems) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalItems = totalItems;
    }

    public List<T> getContent() {
        return content;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public int getTotalPages() {
        return size <= 0 ? 1 : (int) Math.max(1, Math.ceil((double) totalItems / size));
    }

    public boolean isHasPrevious() {
        return page > 1;
    }

    public boolean isHasNext() {
        return page < getTotalPages();
    }
}