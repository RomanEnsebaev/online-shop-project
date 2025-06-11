package org.onlineshop.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Objects;

public class CartItemDto {
    @NotNull
    private Integer productId;

    @NotBlank
    private String name;

    @NotNull
    @DecimalMin(value = "0.01", inclusive = true)
    private BigDecimal price;

    @Min(1)
    private int qty;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal lineTotal;


    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CartItemDto)) return false;
        CartItemDto that = (CartItemDto) o;
        return qty == that.qty
                && Objects.equals(productId, that.productId)
                && Objects.equals(name, that.name)
                && Objects.equals(price, that.price)
                && Objects.equals(lineTotal, that.lineTotal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, name, price, qty, lineTotal);
    }
}
