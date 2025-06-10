package org.onlineshop.dto;

import org.onlineshop.model.CartItem;

import java.time.LocalDateTime;
import java.util.List;

public class CartDto {
    private Integer cartId;
    private String sessionId;
    private Integer userId;
    private LocalDateTime createdAt;
    private List<CartItemDto> items;

    public Integer getCartId() { return cartId; }
    public void setCartId(Integer cartId) { this.cartId = cartId; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<CartItemDto> getItems() { return items; }
    public void setItems(List<CartItemDto> items) { this.items = items; }
}
