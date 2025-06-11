package org.onlineshop.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDto {

    @Positive
    private int id;

    @NotNull
    private LocalDateTime orderDate;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal total;

    @NotNull
    @Size(min = 1)
    private List<@Valid OrderItemDto> items;

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
