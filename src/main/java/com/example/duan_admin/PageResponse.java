package com.example.duan_admin;
import java.util.List;

public class PageResponse<T> {
    private List<T> content;          
    private int number;               
    private int size;                 
    private int totalPages;           
    private long totalElements;       

   
    public List<T> getContent() { return content; }
    public void setContent(List<T> content) { this.content = content; }
    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
}