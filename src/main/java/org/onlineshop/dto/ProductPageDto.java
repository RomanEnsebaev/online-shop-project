package org.onlineshop.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class ProductPageDto {

    @NotNull
    private List<@Valid ProductDto> products;

    @Min(0)
    private int currentPage;

    @Min(1)
    private int totalPages;

    public ProductPageDto() {
    }

    public ProductPageDto(List<@Valid ProductDto> products, int currentPage, int totalPages) {
        this.products = products;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
    }

    public @NotNull List<@Valid ProductDto> getProducts() {
        return products;
    }

    public void setProducts(@NotNull List<@Valid ProductDto> products) {
        this.products = products;
    }

    @Min(0)
    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(@Min(0) int currentPage) {
        this.currentPage = currentPage;
    }

    @Min(1)
    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(@Min(1) int totalPages) {
        this.totalPages = totalPages;
    }
}
