package org.onlineshop.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductDto {
    private Integer id;

    @NotBlank
    @Size(max = 100)
    private String  name;

    @Size(max = 1000)
    private String  description;

    @NotNull
    @DecimalMin(value = "0.01", inclusive = true)
    private BigDecimal price;

    @Min(0)
    private int stockQty;
    private boolean active;
    private LocalDateTime createdAt;


    public Integer getId() {
        return id;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getStockQty() {
        return stockQty;
    }

    public void setStockQty(int stockQty) {
        this.stockQty = stockQty;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
