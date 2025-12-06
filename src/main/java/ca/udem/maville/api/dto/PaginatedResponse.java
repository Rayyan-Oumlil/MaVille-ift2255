package ca.udem.maville.api.dto;

import java.util.List;

/**
 * Réponse paginée standardisée pour l'API
 */
public class PaginatedResponse<T> {
    private List<T> data;
    private int page;
    private int pageSize;
    private long totalItems;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
    
    public PaginatedResponse() {}
    
    public PaginatedResponse(List<T> data, int page, int pageSize, long totalItems) {
        this.data = data;
        this.page = page;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
        this.totalPages = (int) Math.ceil((double) totalItems / pageSize);
        this.hasNext = page < (totalPages - 1);
        this.hasPrevious = page > 0;
    }
    
    // Getters et Setters
    public List<T> getData() {
        return data;
    }
    
    public void setData(List<T> data) {
        this.data = data;
    }
    
    public int getPage() {
        return page;
    }
    
    public void setPage(int page) {
        this.page = page;
    }
    
    public int getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
    
    public long getTotalItems() {
        return totalItems;
    }
    
    public void setTotalItems(long totalItems) {
        this.totalItems = totalItems;
    }
    
    public int getTotalPages() {
        return totalPages;
    }
    
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
    
    public boolean isHasNext() {
        return hasNext;
    }
    
    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }
    
    public boolean isHasPrevious() {
        return hasPrevious;
    }
    
    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }
}

