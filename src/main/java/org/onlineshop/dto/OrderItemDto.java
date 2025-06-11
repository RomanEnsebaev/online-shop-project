package org.onlineshop.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class OrderItemDto {
    @Positive
    private int productId;

    @NotBlank
    private String productName;

    @Min(1)
    private int quantity;

    @NotNull
    @DecimalMin(value = "0.01", inclusive = true)
    private BigDecimal price;

    public OrderItemDto() { }

    public OrderItemDto(int productId, String productName, int quantity, BigDecimal price) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }
    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
