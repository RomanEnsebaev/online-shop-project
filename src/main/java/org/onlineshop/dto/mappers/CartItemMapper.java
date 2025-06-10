package org.onlineshop.dto.mappers;

import org.onlineshop.dto.CartItemDto;
import org.onlineshop.model.CartItem;

import java.math.BigDecimal;

public class CartItemMapper {
    public static CartItemDto toDto(CartItem item) {
        if (item == null) return null;
        CartItemDto dto = new CartItemDto();
        dto.setProductId(item.getProductId());
        dto.setName(item.getName());
        dto.setPrice(item.getPrice());
        dto.setQty(item.getQty());
        dto.setLineTotal(item.getPrice().multiply(
                BigDecimal.valueOf(item.getQty())
        ));
        return dto;
    }

    public static CartItem toEntity(CartItemDto dto, int cartId) {
        if (dto == null) return null;
        CartItem item = new CartItem();
        item.setCartId(cartId);
        item.setProductId(dto.getProductId());
        item.setName(dto.getName());
        item.setPrice(dto.getPrice());
        item.setQty(dto.getQty());
        return item;
    }
}
