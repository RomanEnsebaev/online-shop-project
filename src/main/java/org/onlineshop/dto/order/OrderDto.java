package org.onlineshop.dto.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDto {
    private int id;
    private LocalDateTime orderDate;
    private BigDecimal total;
    private List<OrderItemDto> items;

    public OrderDto() { }

    public OrderDto(int id, LocalDateTime orderDate, BigDecimal total, List<OrderItemDto> items) {
        this.id = id;
        this.orderDate = orderDate;
        this.total = total;
        this.items = items;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }
    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public BigDecimal getTotal() {
        return total;
    }
    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public List<OrderItemDto> getItems() {
        return items;
    }
    public void setItems(List<OrderItemDto> items) {
        this.items = items;
    }
}
