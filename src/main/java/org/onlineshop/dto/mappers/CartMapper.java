package org.onlineshop.dto.mappers;

import org.onlineshop.dto.CartDto;
import org.onlineshop.dto.CartItemDto;
import org.onlineshop.model.Cart;
import org.onlineshop.model.CartItem;

import java.util.List;
import java.util.stream.Collectors;

public class CartMapper {

    public static CartDto toDto(Cart cart) {
        if (cart == null) return null;
        CartDto dto = new CartDto();
        dto.setCartId(cart.getId());
        dto.setSessionId(cart.getSessionId());
        dto.setUserId(cart.getUserId());
        dto.setCreatedAt(cart.getCreatedAt());

        List<CartItemDto> items = cart.getItems().stream()
                .map(item -> CartItemMapper.toDto(item))
                .collect(Collectors.toList());
        dto.setItems(items);
        return dto;
    }

    public static Cart toEntity(CartDto dto) {
        if (dto == null) return null;
        Cart cart = new Cart();
        cart.setId(dto.getCartId());
        cart.setSessionId(dto.getSessionId());
        cart.setUserId(dto.getUserId());
        cart.setCreatedAt(dto.getCreatedAt());

        List<CartItem> items = dto.getItems().stream()
                .map(itemDto -> CartItemMapper.toEntity(itemDto, dto.getCartId()))
                .collect(Collectors.toList());
        cart.setItems(items);
        return cart;
    }
}
