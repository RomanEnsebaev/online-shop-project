package org.onlineshop.dto.order;

import java.util.List;

public class OrderPageDto {
    private List<OrderDto> orders;
    private int currentPage;
    private int totalPages;

    public OrderPageDto() {}

    public OrderPageDto(List<OrderDto> orders, int currentPage, int totalPages) {
        this.orders = orders;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
    }

    public List<OrderDto> getOrders() {
        return orders;
    }
    public void setOrders(List<OrderDto> orders) {
        this.orders = orders;
    }

    public int getCurrentPage() {
        return currentPage;
    }
    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}
